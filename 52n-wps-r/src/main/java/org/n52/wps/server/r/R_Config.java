/**
 * ﻿Copyright (C) 2010
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

package org.n52.wps.server.r;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.n52.wps.ServerDocument.Server;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.WebProcessingService;
import org.n52.wps.server.r.metadata.RAnnotationParser;
import org.n52.wps.server.r.syntax.RAnnotation;
import org.n52.wps.server.r.syntax.RAnnotationException;
import org.n52.wps.server.r.syntax.RAnnotationType;
import org.n52.wps.server.r.syntax.RAttribute;
import org.n52.wps.server.r.util.RConnector;
import org.n52.wps.server.r.util.RSessionInfo;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import java.nio.file.Files;

public class R_Config {

    private final int TEMPDIR_NAME_LENGTH = 8;

    private static Logger LOGGER = LoggerFactory.getLogger(R_Config.class);

    public final String SCRIPT_FILE_EXTENSION = "R";

    public final String SCRIPT_FILE_SUFFIX = "." + SCRIPT_FILE_EXTENSION;

    public final String WKN_PREFIX = "org.n52.wps.server.r.";

    // TODO for resources to be downloadable the cannot be in WEB-INF, or this
    // must be handled with a
    // servlet, which is probably a better solution to keep track of files, see
    // http://www.jguru.com/faq/view.jsp?EID=10646
    private final String R_BASE_DIR = "R";

    // private final String WORK_DIR = "workdir";
    // public static String SCRIPT_DIR = null;

    private final String UTILS_DIR = "utils";

    private final String DEFAULT_RSERVE_HOST = "localhost";

    private final int DEFAULT_RSERVE_PORT = 6311;

    private final boolean DEFAULT_ENABLEBATCHSTART = false;

    /**
     * Base directory for WPS4R resources
     */
    private String baseDirFull;

    /**
     * Work directory, e.g. for streaming files from Rserve to WPS. Not to
     * confuse with the Rserve work directory
     */
    // public String WORK_DIR_FULL = new File(BASE_DIR_FULL,
    // WORK_DIR).getAbsolutePath();

    /** R scripts with utility functions to pre-load */
    public String utilsDirFull;

    /**
     * Location of all R process scripts, cannot be in WEB-INF so that they can
     * easily be downloaded
     **/
    // private static String SCRIPT_DIR_FULL = BASE_DIR_FULL + "/" + SCRIPT_DIR;
    // public static String SCRIPT_DIR_URL = "wps/rscripts";

    // public String rServeHost = DEFAULT_RSERVE_HOST;

    // public int rServePort = DEFAULT_RSERVE_PORT;

    // public String rServeUser;

    // public String rServePassword;

    // /** Starts R serve via batch file **/

    //public String resourceDirectory;

    private HashMap<RWPSConfigVariables, String> configVariables = new HashMap<RWPSConfigVariables, String>();

    private static R_Config instance = null;

    private RConnector connector = new RConnector();

    private RAnnotationParser annotationParser = new RAnnotationParser();

    /** Maps current R-script files to identifiers**/
    private HashMap<File, String> fileToWknMap = new HashMap<File, String>();

    /** Maps each identifier to an R script file **/
    private HashMap<String, File> wknToFileMap = new HashMap<String, File>();
    
    /** caches conflicts for the wkn-Rscript mapping until resetWknFileMapping is invoked**/
    private HashMap<String, ExceptionReport> wknConflicts = new HashMap<String, ExceptionReport>();

    private R_Config() {
        // singleton pattern > private constructor

        // TODO: obsolete code, script dir is retrieved by
        // RPropertychangemangager; delete if no errors occur
        // WPSConfig wpsConfig = WPSConfig.getInstance();
        // Property[] rConfig =
        // wpsConfig.getPropertiesForRepositoryClass(LocalRAlgorithmRepository.class.getName());
        // for (Property property : rConfig) {
        // if
        // (property.getName().equalsIgnoreCase(RWPSConfigVariables.SCRIPT_DIR.toString()))
        // {
        // R_Config.SCRIPT_DIR = property.getStringValue();
        // }
        // }

        try {
            String wpsBasedir = WebProcessingService.BASE_DIR;
            if (wpsBasedir != null) {
                File f = new File(wpsBasedir, R_BASE_DIR);
                this.baseDirFull = f.getAbsolutePath();
                f = new File(this.baseDirFull, UTILS_DIR);
                this.utilsDirFull = f.getAbsolutePath();
            } else
                LOGGER.error("Could not get basedir from WPS!");
        } catch (Exception e) {
            LOGGER.error("Error getting full path of baseDir and configDir.", e);
        }
    }

    public static R_Config getInstance()
    {
        if (instance == null)
            instance = new R_Config();

        return instance;
    }

    public void setConfigVariable(RWPSConfigVariables key,
            String value)
    {
        this.configVariables.put(key, value);
    }

    /**
     * Retrieves configuration parameters as they were assigned to the WPS
     * config
     * 
     * @param key
     * @return
     */
    public String getConfigVariable(RWPSConfigVariables key)
    {
        return this.configVariables.get(key);
    }

    public String getConfigVariableFullPath(RWPSConfigVariables key) throws ExceptionReport
    {
        String path = getConfigVariable(key);
        if (path == null)
            throw new ExceptionReport("Config variable is not set!", "Inconsistent property");
        File testFile = new File(path);
        if (!testFile.isAbsolute()) {
            testFile = new File(WebProcessingService.BASE_DIR, path);
        }
        if (!testFile.exists())
            throw new ExceptionReport("Invalid config property of name \"" + key + "\" and value \"" + path + "\". It denotes a non-existent path.", "Inconsistent property");

        return testFile.getAbsolutePath();
    }

    public URL getSessionInfoURL() throws MalformedURLException
    {
        return new URL(getUrlPathUpToWebapp() + "/R/sessioninfo.jsp");
    }

    public String getResourceDirURL()
    {
        return getUrlPathUpToWebapp() + "/" + this.getResourceDirectory().replace("\\", "/");
    }

    public String getResourceDirectory()
    {
        return getConfigVariable(RWPSConfigVariables.RESOURCE_DIR);
    }

    public URL getScriptURL(String wkn) throws MalformedURLException, ExceptionReport
    {
        String fname = null;
        try {
            fname = wknToFile(wkn).getName();
        } catch (IOException e) {
            LOGGER.error("Could not open session.", e);
            throw new ExceptionReport("Could not open script file.", "Input/Output", e);
        }

        if (fname == null)
            return null;
        String script_dir = getConfigVariable(RWPSConfigVariables.SCRIPT_DIR);
        URL url = new URL(getUrlPathUpToWebapp() + "/" + script_dir.replace("\\", "/") + "/" + fname);
        return url;
    }

    public String getUrlPathUpToWebapp()
    {
        Server server = WPSConfig.getInstance().getWPSConfig().getServer();
        String host = server.getHostname();
        String port = server.getHostport();
        String webapppath = server.getWebappPath();

        return "http://" + host + ":" + port + "/" + webapppath;
    }

    public URL getOutputFileURL(String currentWorkdir,
            String filename) throws IOException
    {
        // check if file exists
        String path = currentWorkdir + "/" + filename;
        File out = new File(path);
        if (!(out.isFile() && out.canRead()))
            throw new IOException("Error in creating URL: " + currentWorkdir + " / " + path + " not found or broken.");

        // create URL
        path = path.substring(WebProcessingService.BASE_DIR.length() + 1, path.length());
        String urlString = getUrlPathUpToWebapp() + "/" + path;

        return new URL(urlString);
    }

    /**
     * Filters R scripts as files with suffix ".R"
     */
    public static class RFileExtensionFilter implements FileFilter {

        @Override
        public boolean accept(File f)
        {
            if (f.isFile() && f.canRead()) {
                String name = f.getName();
                if (name.endsWith(R_Config.getInstance().SCRIPT_FILE_SUFFIX))
                    return true;
            }
            return false;
        }
    }

    void registerScript(File file) throws FileNotFoundException, RAnnotationException, IOException, ExceptionReport
    {
        if (!fileToWknMap.containsKey(file.getAbsoluteFile())) {
            FileInputStream fis = new FileInputStream(file);
            List<RAnnotation> annotations = annotationParser.parseAnnotationsfromScript(fis);
            RAnnotation description = RAnnotation.filterAnnotations(annotations, RAnnotationType.DESCRIPTION).get(0);
            String process_id = description.getStringValue(RAttribute.IDENTIFIER);
            String wkn = WKN_PREFIX + process_id;

            if (fileToWknMap.containsValue(wkn)) {
                File conflictFile = wknToFile(wkn);
                if (!conflictFile.exists()) {
                    LOGGER.info("Cached mapping from " + wkn + " to file " + conflictFile.getName() + " replaced by file " + file.getName());
                } else if (!file.equals(conflictFile)) {
                    String e_message = "Conflicting identifier '" + wkn + "' detected " + "for R scripts '" + file.getName() + "' and '" + conflictFile.getName()+"'";
                    ExceptionReport e = new ExceptionReport(e_message, ExceptionReport.NO_APPLICABLE_CODE);
                    LOGGER.error(e_message);
                    wknConflicts.put(wkn, e);
                    throw e;
                }
            }

            fileToWknMap.put(file.getAbsoluteFile(), wkn);
            wknToFileMap.put(wkn, file.getAbsoluteFile());
            fis.close();
        }
    }

    /**
     * @param file
     *            points to an R scripts
     * @return wellknownName is the corresponding process identifier
     * @throws RAnnotationException
     * @throws IOException
     * @throws ExceptionReport
     */
    public String FileToWkn(File file) throws RAnnotationException, IOException, ExceptionReport
    {
        if (!file.exists())
            throw new FileNotFoundException("File not found: " + file.getName());

        return fileToWknMap.get(file);

        // String fileName = file.getName();
        //
        // // remove suffix, usually file ending ".R":
        // int index = fileName.lastIndexOf('.');
        // if (index > 0)
        // fileName = fileName.substring(0, index);
        // return R_Config.WKN_PREFIX + fileName;
    }

    /**
     * @param wkn
     *            Process identifier
     * @return File points to corresponding R process script
     * @throws IOException
     * @throws ExceptionReport 
     */
    public File wknToFile(String wkn) throws IOException, ExceptionReport
    {
        //check for existing identifier conflicts
        if(wknConflicts.containsKey(wkn))
            throw wknConflicts.get(wkn);
        
        File out = wknToFileMap.get(wkn);
        if (out!=null && out.exists() && out.isFile() && out.canRead()) {
            return out;
        } else{
            String fname = out == null ? "(unknown)" : out.getName();
            throw new IOException("Error in Process: " + wkn + ", File " + fname + " not found or broken.");
            }
    }
    
    public void resetWknFileMapping(){
        this.wknToFileMap.clear();
        this.fileToWknMap.clear();
        this.wknConflicts.clear();
    }

    public String createTemporaryWPSWorkDir()
    {
        File tempdir = new File(System.getProperty("java.io.tmpdir"), "wps4r-wps-workdir-tmp-" + UUID.randomUUID().toString().substring(0, TEMPDIR_NAME_LENGTH)); // +
                                                                                                                                                                  // ".tmp");
        tempdir.mkdir();
        return tempdir.getAbsolutePath();
    }

    // public String createTemporaryRWorkDir() throws IOException {
    // File tempdir = new File(System.getProperty("java.io.tmpdir"),
    // "wps4r-r-workdir-"
    // + UUID.randomUUID().toString().substring(0, TEMPDIR_NAME_LENGTH)); // +
    // ".tmp");
    // tempdir.mkdir();
    // return tempdir.getAbsolutePath();
    // }

    public String getScriptDirFullPath()
    {
        return new File(WebProcessingService.BASE_DIR, getConfigVariable(RWPSConfigVariables.SCRIPT_DIR)).getAbsolutePath();
    }

    /**
     * 
     * @param identifier
     * @return
     */
    public boolean isScriptAvailable(String identifier)
    {
        try {
            File f = wknToFile(identifier);
            boolean out = f.exists();
            return out;
        } catch (Exception e) {
            LOGGER.error("Script file unavailable for process id " + identifier, e);
            return false;
        }
    }

    /**
     * Tests if a script associated with a process is valid Any errors will be
     * logged
     * 
     * @param wkn
     * @return
     */
    public boolean isScriptValid(String wkn)
    {
        FileInputStream fis = null;

        try {
            File file = wknToFile(wkn);
            // RAnnotationParser parser = new RAnnotationParser();
            fis = new FileInputStream(file);
            boolean valid = annotationParser.validateScript(fis, wkn);

            return valid;
        } catch (IOException e) {
            LOGGER.error("Script file unavailable for process " + wkn + ".", e);
            return false;
        } catch (Exception e) {
            LOGGER.error("Validation of process " + wkn + " failed.", e);
            return false;
        } finally {
            if (fis != null)
                try {
                    fis.close();
                } catch (IOException e) {
                    LOGGER.error("Could not flose file input.", e);
                }
        }
    }

    public void killRserveOnWindows()
    {
        try {
            if (Runtime.getRuntime().exec("taskkill /IM RServe.exe /T /F").waitFor() == 0)
                ;
            return;
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public RConnection openRConnection() throws RserveException
    {
        return this.connector.getNewConnection(this.getEnableBatchStart(), this.getRServeHost(), this.getRServePort(), this.getRServeUser(), this.getRServePassword());
    }

    public String getRServePassword()
    {
        return getConfigVariable(RWPSConfigVariables.RSERVE_PASSWORD);
    }

    public String getRServeUser()
    {
        return getConfigVariable(RWPSConfigVariables.RSERVE_USER);
    }

    public int getRServePort()
    {
        int port_number = this.DEFAULT_RSERVE_PORT;

        String port = getConfigVariable(RWPSConfigVariables.RSERVE_PORT);
        // try to retrieve config variable
        if (port != null && !port.equals("")) {
            try {
                port_number = Integer.parseInt(port);
            } catch (NumberFormatException e) {
                LOGGER.warn("Config variable " + RWPSConfigVariables.RSERVE_PORT + " does not contain a parseble integer. Using default port " + port_number);
            }
        }
        return port_number;
    }

    public String getRServeHost()
    {
        String host = getConfigVariable(RWPSConfigVariables.RSERVE_HOST);
        if (host == null || host.equals("")) {
            host = this.DEFAULT_RSERVE_HOST;
        }
        return host;
    }

    public boolean getEnableBatchStart()
    {
        boolean isBatch = this.DEFAULT_ENABLEBATCHSTART;
        // try to retrieve config variable
        String batch_c = getConfigVariable(RWPSConfigVariables.ENABLE_BATCH_START);
        if (batch_c != null && !batch_c.equals("")) {
            try {
                isBatch = Boolean.parseBoolean(batch_c);
            } catch (NumberFormatException e) {
                LOGGER.warn("Config variable " + RWPSConfigVariables.RSERVE_PORT + " does not contain a parseble boolean. Using default port " + isBatch);
            }
        }

        return isBatch;
    }

    public String getCurrentSessionInfo() throws RserveException, REXPMismatchException
    {
        RConnection rCon = openRConnection();
        String info = RSessionInfo.getSessionInfo(rCon);
        rCon.close();
        return info;
    }
}
