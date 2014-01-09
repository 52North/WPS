/**
 * ﻿Copyright (C) 2007
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */

package org.n52.wps.server.database;

import com.google.common.base.Joiner;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.io.IOUtils;
import org.n52.wps.DatabaseDocument.Database;
import org.n52.wps.ServerDocument.Server;
import org.n52.wps.commons.PropertyUtil;
import org.n52.wps.commons.MIMEUtil;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.commons.XMLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * @author tkunicki (Thomas Kunicki, USGS)
 *
 */
public final class FlatFileDatabase implements IDatabase {

    private final static Logger LOGGER = LoggerFactory.getLogger(FlatFileDatabase.class);

    private final static String KEY_DATABASE_ROOT = "org.n52.wps.server.database";
    private final static String KEY_DATABASE_PATH = "path";
    private final static String KEY_DATABASE_WIPE_ENABLED = "wipe.enabled";
    private final static String KEY_DATABASE_WIPE_PERIOD = "wipe.period";
    private final static String KEY_DATABASE_WIPE_THRESHOLD = "wipe.threshold";
    
    private final static String DEFAULT_DATABASE_PATH = 
            Joiner.on(File.separator).join(
                System.getProperty("java.io.tmpdir", "."),
                "Database",
                "Results");
    private final static boolean DEFAULT_DATABASE_WIPE_ENABLED = true;  // P1H
    private final static long DEFAULT_DATABASE_WIPE_PERIOD = 1000 * 60 * 60;  // P1H
    private final static long DEFAULT_DATABASE_WIPE_THRESHOLD = 1000 * 60 * 60 * 24 * 7; // P7D
    
    private final static String SUFFIX_MIMETYPE = "mime-type";
    private final static String SUFFIX_CONTENT_LENGTH = "content-length";
    private final static String SUFFIX_XML = "xml";
    private final static String SUFFIX_TEMP = "tmp";
    private final static String SUFFIX_GZIP = "gz";
    private final static String SUFFIX_PROPERTIES = "properties";

    // If the delimiter changes, examine Patterns below.
    private final static Joiner JOINER = Joiner.on(".");

    // Grouping is used to pull out integer index of response, if these patterns
    // change examine findLatestResponseIndex(...), generateResponseFile(...)
    // and generateResponseFile(...)
    private final static Pattern PATTERN_RESPONSE = Pattern.compile("([\\d]+)\\." + SUFFIX_XML);
    private final static Pattern PATTERN_RESPONSE_TEMP = Pattern.compile("([\\d]+)\\." + SUFFIX_XML + "(:?\\."
            + SUFFIX_TEMP + ")?");

    private static FlatFileDatabase instance;

    // This method is required by the DatabaseFactory, it is found using reflection
    public synchronized static IDatabase getInstance() {
        if (instance == null) {
            instance = new FlatFileDatabase();
        }
        return instance;
    }

    protected final File baseDirectory;

    protected final String baseResultURL;

    protected final boolean gzipComplexValues = true;

    protected final Object storeResponseSerialNumberLock;

    protected final boolean indentXML = true;

    protected final Timer wipeTimer;

    protected FlatFileDatabase() {
        
        Server server = WPSConfig.getInstance().getWPSConfig().getServer();
        Database database = server.getDatabase();
        PropertyUtil propertyUtil = new PropertyUtil(database.getPropertyArray(), KEY_DATABASE_ROOT);
        
        // NOTE: The hostname and port are hard coded as part of the 52n framework design/implementation.
        baseResultURL = String.format("http://%s:%s/%s/RetrieveResultServlet?id=",
                server.getHostname(), server.getHostport(), server.getWebappPath());
        LOGGER.info("Using \"{}\" as base URL for results", baseResultURL);
        
        String baseDirectoryPath = propertyUtil.extractString(KEY_DATABASE_PATH, DEFAULT_DATABASE_PATH);
        baseDirectory = new File(baseDirectoryPath);
        LOGGER.info("Using \"{}\" as base directory for results database", baseDirectoryPath);
        if ( !baseDirectory.exists()) {
            LOGGER.info("Results database does not exist, creating.", baseDirectoryPath);
            baseDirectory.mkdirs();
        }

        if (propertyUtil.extractBoolean(KEY_DATABASE_WIPE_ENABLED, DEFAULT_DATABASE_WIPE_ENABLED)) {
            
            long periodMillis = propertyUtil.extractPeriodAsMillis(KEY_DATABASE_WIPE_PERIOD, DEFAULT_DATABASE_WIPE_PERIOD);
            long thresholdMillis = propertyUtil.extractPeriodAsMillis(KEY_DATABASE_WIPE_THRESHOLD, DEFAULT_DATABASE_WIPE_THRESHOLD);

            wipeTimer = new Timer(getClass().getSimpleName() + " File Wiper", true);
            wipeTimer.scheduleAtFixedRate(new FlatFileDatabase.WipeTimerTask(thresholdMillis), 0, periodMillis);
            LOGGER.info("Started {} file wiper timer; period {} ms, threshold {} ms",
                    new Object[] {getDatabaseName(),periodMillis,thresholdMillis});
        } else {
            wipeTimer = null;
        }

        storeResponseSerialNumberLock = new Object();
    }

    @Override
    public String generateRetrieveResultURL(String id) {
        return baseResultURL + id;
    }

    @Override
    public String getDatabaseName() {
        return getClass().getSimpleName();
    }

    @Override
    public void insertRequest(String id, InputStream inputStream, boolean xml) {
        // store request in response directory...
        File responseDirectory = generateResponseDirectory(id);
        responseDirectory.mkdir();
        BufferedOutputStream outputStream = null;
        try {
            if (xml) {
                outputStream = new BufferedOutputStream(
                        new FileOutputStream(
                            new File(
                                responseDirectory,
                                JOINER.join("request", SUFFIX_XML)),
                        false));
                XMLUtil.copyXML(inputStream, outputStream, indentXML);
            } else {
                outputStream = new BufferedOutputStream(
                        new FileOutputStream(
                            new File(
                                responseDirectory,
                                JOINER.join("request", SUFFIX_PROPERTIES)),
                        false));
                IOUtils.copy(inputStream, outputStream);
            }
        }
        catch (Exception e) {
            LOGGER.error("Exception storing request for id {}: {}", id, e);
        }
        finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    @Override
    public String insertResponse(String id, InputStream outputStream) {
        return this.storeResponse(id, outputStream);
    }

    @Override
    public InputStream lookupRequest(String id) {
        File requestFile = lookupRequestAsFile(id);
        if (requestFile != null && requestFile.exists()) {
            LOGGER.debug("Request file for {} is {}", id, requestFile.getPath());
            try {
                return new FileInputStream(requestFile);
            }
            catch (FileNotFoundException ex) {
                // should never get here due to checks above...
                LOGGER.warn("Request not found for id {}", id);
            }
        }
        LOGGER.warn("Response not found for id {}", id);
        return null;
    }

    @Override
    public InputStream lookupResponse(String id) {
        File responseFile = lookupResponseAsFile(id);
        if (responseFile != null && responseFile.exists()) {
            LOGGER.debug("Response file for {} is {}", id, responseFile.getPath());
            try {
                return responseFile.getName().endsWith(SUFFIX_GZIP) ? new GZIPInputStream(new FileInputStream(responseFile))
                                                                   : new FileInputStream(responseFile);
            }
            catch (FileNotFoundException ex) {
                // should never get here due to checks above...
                LOGGER.warn("Response not found for id {}", id);
            }
            catch (IOException ex) {
                LOGGER.warn("Error processing response for id {}", id);
            }
        }
        LOGGER.warn("Response not found for id {}", id);
        return null;
    }

    @Override
    public File lookupRequestAsFile(String id) {
        File requestAsFile = null;
        // request is stored in response directory...
        File responseDirectory = generateResponseDirectory(id);
        if (responseDirectory.exists()) {
            synchronized (storeResponseSerialNumberLock) {
                requestAsFile = new File(responseDirectory, JOINER.join("request", SUFFIX_XML));
                if ( !requestAsFile.exists()) {
                    requestAsFile = new File(responseDirectory, JOINER.join("request", SUFFIX_PROPERTIES));
                }
                if ( !requestAsFile.exists()) {
                    requestAsFile = null;
                }
            }
        }
        return requestAsFile;
    }

    @Override
    public File lookupResponseAsFile(String id) {
        File responseFile = null;
        // if response resolved to directory, this means the response is a status update
        File responseDirectory = generateResponseDirectory(id);
        if (responseDirectory.exists()) {
            synchronized (storeResponseSerialNumberLock) {
                return findLatestResponseFile(responseDirectory);
            }
        }
        else {
            String mimeType = getMimeTypeForStoreResponse(id);
            if (mimeType != null) {
                // ignore gzipComplexValues in case file was stored when value
                // was inconsistent with current value;
                responseFile = generateComplexDataFile(id, mimeType, false);
                if ( !responseFile.exists()) {
                    responseFile = generateComplexDataFile(id, mimeType, true);
                }
                if ( !responseFile.exists()) {
                    responseFile = null;
                }
            }
        }
        return responseFile;
    }

    @Override
    public void shutdown() {
        if (wipeTimer != null) {
            wipeTimer.cancel();
        }
    }

    @Override
    public String storeComplexValue(String id, InputStream resultInputStream, String type, String mimeType) {

        String resultId = JOINER.join(id, UUID.randomUUID().toString());
        try {
            File resultFile = generateComplexDataFile(resultId, mimeType, gzipComplexValues);
            File mimeTypeFile = generateComplexDataMimeTypeFile(resultId);
            File contentLengthFile = generateComplexDataContentLengthFile(resultId);

            LOGGER.debug("initiating storage of complex value for {} as {}", id, resultFile.getPath());

            long contentLength = -1;

            OutputStream resultOutputStream = null;
            try {
                resultOutputStream = gzipComplexValues ? new GZIPOutputStream(new FileOutputStream(resultFile))
                                                      : new BufferedOutputStream(new FileOutputStream(resultFile));
                contentLength = IOUtils.copyLarge(resultInputStream, resultOutputStream);
            }
            finally {
                IOUtils.closeQuietly(resultInputStream);
                IOUtils.closeQuietly(resultOutputStream);
            }

            OutputStream mimeTypeOutputStream = null;
            try {
                mimeTypeOutputStream = new BufferedOutputStream(new FileOutputStream(mimeTypeFile));
                IOUtils.write(mimeType, mimeTypeOutputStream);
            }
            finally {
                IOUtils.closeQuietly(mimeTypeOutputStream);
            }

            OutputStream contentLengthOutputStream = null;
            try {
                contentLengthOutputStream = new BufferedOutputStream(new FileOutputStream(contentLengthFile));
                IOUtils.write(Long.toString(contentLength), contentLengthOutputStream);
            }
            finally {
                IOUtils.closeQuietly(contentLengthOutputStream);
            }

            LOGGER.debug("completed storage of complex value for {} as {}", id, resultFile.getPath());

        }
        catch (IOException e) {
            throw new RuntimeException("Error storing complex value for " + resultId, e);
        }
        return generateRetrieveResultURL(resultId);
    }

    @Override
    public String storeResponse(String id, InputStream inputStream) {

        try {
            File responseTempFile;
            File responseFile;
            synchronized (storeResponseSerialNumberLock) {
                File responseDirectory = generateResponseDirectory(id);
                responseDirectory.mkdir();
                int responseIndex = findLatestResponseIndex(responseDirectory, true);
                if (responseIndex < 0) {
                    responseIndex = 0;
                }
                else {
                    responseIndex++;
                }
                responseFile = generateResponseFile(responseDirectory, responseIndex);
                responseTempFile = generateResponseTempFile(responseDirectory, responseIndex);
                try {
                    // create the file so that the reponse serial number is correctly
                    // incremented if this method is called again for this reponse
                    // before this reponse is completed.
                    responseTempFile.createNewFile();
                }
                catch (IOException e) {
                    throw new RuntimeException("Error storing response to {}", e);
                }
                LOGGER.debug("Creating temp file for {} as {}", id, responseTempFile.getPath());
            }
            InputStream responseInputStream = null;
            OutputStream responseOutputStream = null;
            try {
                responseInputStream = inputStream;
                responseOutputStream = new BufferedOutputStream(new FileOutputStream(responseTempFile));
                // In order to allow the prior response to be available we write
                // to a temp file and rename these when completed. Large responses
                // can cause the call below to take a significant amount of time.
                XMLUtil.copyXML(responseInputStream, responseOutputStream, indentXML);
            }
            finally {
                IOUtils.closeQuietly(responseInputStream);
                IOUtils.closeQuietly(responseOutputStream);
            }

            synchronized (storeResponseSerialNumberLock) {
                responseTempFile.renameTo(responseFile);
                LOGGER.debug("Renamed temp file for {} to {}", id, responseFile.getPath());
            }

            return generateRetrieveResultURL(id);

        }
        catch (FileNotFoundException e) {
            throw new RuntimeException("Error storing response for " + id, e);
        }
        catch (IOException e) {
            throw new RuntimeException("Error storing response for " + id, e);
        }
    }

    @Override
    public void updateResponse(String id, InputStream inputStream) {
        this.storeResponse(id, inputStream);
    }

    @Override
    public String getMimeTypeForStoreResponse(String id) {

        File responseDirectory = generateResponseDirectory(id);
        if (responseDirectory.exists()) {
            return "text/xml";
        }
        else {
            File mimeTypeFile = generateComplexDataMimeTypeFile(id);
            if (mimeTypeFile.canRead()) {
                InputStream mimeTypeInputStream = null;
                try {
                    mimeTypeInputStream = new FileInputStream(mimeTypeFile);
                    return IOUtils.toString(mimeTypeInputStream);
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
                finally {
                    IOUtils.closeQuietly(mimeTypeInputStream);
                }
            }
        }
        return null;
    }

    @Override
    public long getContentLengthForStoreResponse(String id) {

        File responseDirectory = generateResponseDirectory(id);
        if (responseDirectory.exists()) {
            synchronized (storeResponseSerialNumberLock) {
                File responseFile = findLatestResponseFile(responseDirectory);
                return responseFile.length();
            }
        }
        else {
            File contentLengthFile = generateComplexDataContentLengthFile(id);
            if (contentLengthFile.canRead()) {
                InputStream contentLengthInputStream = null;
                try {
                    contentLengthInputStream = new FileInputStream(contentLengthFile);
                    return Long.parseLong(IOUtils.toString(contentLengthInputStream));
                }
                catch (IOException e) {
                    LOGGER.error("Unable to extract content-length for response id {} from {}, exception message: {}",
                                 new Object[] {id, contentLengthFile.getAbsolutePath(), e.getMessage()});
                }
                catch (NumberFormatException e) {
                    LOGGER.error("Unable to parse content-length for response id {} from {}, exception message: {}",
                                 new Object[] {id, contentLengthFile.getAbsolutePath(), e.getMessage()});
                }
                finally {
                    IOUtils.closeQuietly(contentLengthInputStream);
                }
            }
            return -1;
        }
    }

    @Override
    public boolean deleteStoredResponse(String id) {
        return false;
    }

    private int findLatestResponseIndex(File responseDirectory, boolean includeTemp) {
        int responseIndex = Integer.MIN_VALUE;
        for (File file : responseDirectory.listFiles()) {
            Matcher matcher = includeTemp ? PATTERN_RESPONSE_TEMP.matcher(file.getName())
                                         : PATTERN_RESPONSE.matcher(file.getName());
            if (matcher.matches()) {
                int fileIndex = Integer.parseInt(matcher.group(1));
                if (fileIndex > responseIndex) {
                    responseIndex = fileIndex;
                }
            }
        }
        return responseIndex;
    }

    private File findLatestResponseFile(File responseDirectory) {
        int responseIndex = findLatestResponseIndex(responseDirectory, false);
        return responseIndex < 0 ? null : generateResponseFile(responseDirectory, responseIndex);
    }

    private File generateResponseFile(File responseDirectory, int index) {
        return new File(responseDirectory, JOINER.join(index, SUFFIX_XML));
    }

    private File generateResponseTempFile(File responseDirectory, int index) {
        return new File(responseDirectory, JOINER.join(index, SUFFIX_XML, SUFFIX_TEMP));
    }

    private File generateResponseDirectory(String id) {
        return new File(baseDirectory, id);
    }

    private File generateComplexDataFile(String id, String mimeType, boolean gzip) {
        String fileName = gzip ? JOINER.join(id, MIMEUtil.getSuffixFromMIMEType(mimeType), SUFFIX_GZIP)
                              : JOINER.join(id, MIMEUtil.getSuffixFromMIMEType(mimeType));
        return new File(baseDirectory, fileName);
    }

    private File generateComplexDataMimeTypeFile(String id) {
        return new File(baseDirectory, JOINER.join(id, SUFFIX_MIMETYPE));
    }

    private File generateComplexDataContentLengthFile(String id) {
        return new File(baseDirectory, JOINER.join(id, SUFFIX_CONTENT_LENGTH));
    }

    private class WipeTimerTask extends TimerTask {

        public final long thresholdMillis;

        WipeTimerTask(long thresholdMillis) {
            this.thresholdMillis = thresholdMillis;
        }

        @Override
        public void run() {
            wipe(baseDirectory, thresholdMillis);
        }

        private void wipe(File rootFile, long thresholdMillis) {
            // SimpleDataFormat is not thread-safe.
            SimpleDateFormat iso8601DateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            long currentTimeMillis = System.currentTimeMillis();
            LOGGER.info(getDatabaseName() + " file wiper, checking {} for files older than {} ms",
                        rootFile.getAbsolutePath(),
                        thresholdMillis);

            File[] files = rootFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    long lastModifiedMillis = file.lastModified();
                    long ageMillis = currentTimeMillis - lastModifiedMillis;
                    if (ageMillis > thresholdMillis) {
                        LOGGER.info("Deleting {}, last modified date is {}",
                                    file.getName(),
                                    iso8601DateFormat.format(new Date(lastModifiedMillis)));
                        delete(file);
                        if (file.exists()) {
                            LOGGER.warn("Deletion of {} failed", file.getName());
                        }
                    }
                }
            } else {
                LOGGER.warn("Cannot delete files, no files in root directory {}  > file list is null. ", rootFile.getAbsolutePath());
            }
        }

        private void delete(File file) {
            if (file.isDirectory()) {
                for (File child : file.listFiles()) {
                    delete(child);
                }
            }
            file.delete();
        }
    }
}
