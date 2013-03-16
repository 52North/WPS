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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
//import java.nio.file.Files;
import java.util.HashMap;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.ServerDocument.Server;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.WebProcessingService;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

public class R_Config {

    private static final int TEMPDIR_NAME_LENGTH = 8;

    private static Logger LOGGER = Logger.getLogger(R_Config.class);

    public final String SCRIPT_FILE_EXTENSION = "R";

    public final String SCRIPT_FILE_SUFFIX = "." + SCRIPT_FILE_EXTENSION;

    public final String WKN_PREFIX = "org.n52.wps.server.r.";

    // important directories:

    // FIXME for resources to be downloadable the cannot be in WEB-INF, or this must be handled with a
    // servlet, which is probably a better solution to keep track of files, see
    // http://www.jguru.com/faq/view.jsp?EID=10646
    private final String R_BASE_DIR = "R";

    // private final String WORK_DIR = "workdir";

    private final String UTILS_DIR = "utils";

    public String SCRIPT_DIR = null;

    /**
     * Base directory for WPS4R resources
     */
    public String BASE_DIR_FULL = new File(WebProcessingService.BASE_DIR, R_BASE_DIR).getAbsolutePath();

    /**
     * Work directory, e.g. for streaming files from Rserve to WPS. Not to confuse with the Rserve work
     * directory
     */
    // public String WORK_DIR_FULL = new File(BASE_DIR_FULL, WORK_DIR).getAbsolutePath();

    /** R scripts with utility functions to pre-load */
    public String UTILS_DIR_FULL = new File(BASE_DIR_FULL, UTILS_DIR).getAbsolutePath();

    /** Location of all R process scripts, cannot be in WEB-INF so that they can easily be downloaded **/
    // private static String SCRIPT_DIR_FULL = BASE_DIR_FULL + "/" + SCRIPT_DIR;
    // public static String SCRIPT_DIR_URL = "wps/rscripts";

    /** Host IP for Rserve **/
    public String RSERVE_HOST = "localhost";

    /** Port were Rserve is listening **/
    public int RSERVE_PORT = 6311;

    /** Applied user ID for Rserve (if login needed) **/
    public String RSERVE_USER;

    /** Applied user password for Rserve (if login needed) **/
    public String RSERVE_PASSWORD;

    /** Starts R serve via batch file **/
    public boolean enableBatchStart = false;

    // private String batchStartFile = "Rserve.bat";

    public String RESOURCE_DIR;

    private HashMap<RWPSConfigVariables, String> configVariables = new HashMap<RWPSConfigVariables, String>();

    private static R_Config instance = null;

    private R_Config() {
        // singleton pattern > private constructor

        Property[] rConfig = WPSConfig.getInstance().getPropertiesForRepositoryClass(LocalRAlgorithmRepository.class.getName());
        for (Property property : rConfig) {
            if (property.getName().equalsIgnoreCase(RWPSConfigVariables.SCRIPT_DIR.toString())) {
                SCRIPT_DIR = property.getStringValue();
            }
        }
    }

    public static R_Config getInstance() {
        if (instance == null)
            instance = new R_Config();

        return instance;
    }

    public RConnection openRConnection() throws RserveException {
        RConnection con = null;
        try {
            con = newConnection();
        }
        catch (RserveException e) {
            if (e.getMessage().startsWith("Cannot connect") && enableBatchStart) {
                try {
                    startRserve();
                    // try to establish RServe connection
                    int attempt = 1;
                    while (attempt <= 5) {
                        try {
                            Thread.sleep(1000); // wait for R to startup, then establish connection
                            con = newConnection();
                            break;
                        }
                        catch (RserveException rse) {
                            if (attempt == 5) {
                                throw e;
                            }

                            attempt++;
                        }

                    }
                }
                catch (Exception e2) {
                    LOGGER.error("Attempt to start Rserve and establish a connection failed", e2);
                    throw e;
                }
            }
            else
                throw e;
        }
        finally {
            if (con != null && con.needLogin())
                con.login(RSERVE_USER, RSERVE_PASSWORD);
        }
        return con;
    }

    private RConnection newConnection() throws RserveException {
        LOGGER.debug("Creating new RConnection");
        
        RConnection con;
        con = new RConnection(RSERVE_HOST, RSERVE_PORT);
        RUtil.log(con, "New connection from WPS4R");

        REXP info = con.eval("capture.output(sessionInfo())");
        try {
            LOGGER.info("sessionInfo:\n" + Arrays.deepToString(info.asStrings()));
        }
        catch (REXPMismatchException e) {
            // do nothing
        }
        return con;
    }

    public void setConfigVariable(RWPSConfigVariables key, String value) {
        configVariables.put(key, value);
    }

    public String getConfigVariable(RWPSConfigVariables key) {
        return configVariables.get(key);
    }

    public String getConfigVariableFullPath(RWPSConfigVariables key) throws ExceptionReport {
        String path = getConfigVariable(key);
        if (path == null)
            throw new ExceptionReport("Config variable is not set!", "Inconsistent property");
        File testFile = new File(path);
        if ( !testFile.isAbsolute()) {
            testFile = new File(WebProcessingService.BASE_DIR, path);
        }
        if ( !testFile.exists())
            throw new ExceptionReport("Invalid config property of name \"" + key + "\" and value \"" + path
                    + "\". It denotes a non-existent path.", "Inconsistent property");

        return testFile.getAbsolutePath();
    }

    /**
     * Captures the Console printout of R for a specific string
     * 
     * @param rCon
     * @param cmd
     *        command which has been used
     * @return R text output as formatted string
     * @throws RserveException
     * @throws REXPMismatchException
     */
    public String getConsoleOutput(RConnection rCon, String cmd) throws RserveException, REXPMismatchException {
        return rCon.eval("paste(capture.output(print(" + cmd + ")),collapse='\\n')").asString();

    }

    /**
     * Retrieves R session info from current R session
     * 
     * @param rcon
     *        Open R connection
     * @return R text output as formatted string
     * @throws RserveException
     * @throws REXPMismatchException
     * 
     * @see getConsoleOutput(RConnection rCon, String cmd)
     */
    public String getSessionInfo(RConnection rCon) throws RserveException, REXPMismatchException {
        return getConsoleOutput(rCon, "sessionInfo()");
    }

    /**
     * "Quiet" retrieval of the R session information
     * 
     * @return R text output as formatted string
     * @throws RuntimeException
     *         if session info cannot be retrieved
     * @see getConsoleOutput(RConnection rCon, String cmd)
     * @see getSessionInfo(RConnection rCon)
     */
    public String getSessionInfo() {
        RConnection rCon = null;
        String sessionInfo = "";
        try {
            rCon = openRConnection();
            sessionInfo = getSessionInfo(rCon);
        }
        catch (Exception e) {
            LOGGER.error("Could not open session.", e);
            throw new RuntimeException("Error: R session info cannot be retrieved.", e);
        }
        finally {
            if (rCon != null)
                rCon.close();
        }

        return sessionInfo;
    }

    public String getSessionInfoURL() {
        return getUrlPathUpToWebapp() + "/R/sessioninfo.jsp";
    }

    public String getResourceDirURL() {
        return getUrlPathUpToWebapp() + "/" + RESOURCE_DIR.replace("\\", "/");
    }

    public URL getScriptURL(String wkn) throws MalformedURLException, ExceptionReport {
        String fname = null;
        try {
            fname = wknToFile(wkn).getName();
        }
        catch (IOException e) {
            LOGGER.error("Could not open session.", e);
            throw new ExceptionReport("Could not open script file.", "Input/Output", e);
        }

        if (fname == null)
            return null;

        URL url = new URL(getUrlPathUpToWebapp() + "/" + SCRIPT_DIR.replace("\\", "/") + "/" + fname);
        return url;
    }

    public String getUrlPathUpToWebapp() {
        Server server = WPSConfig.getInstance().getWPSConfig().getServer();
        String host = server.getHostname();
        String port = server.getHostport();
        String webapppath = server.getWebappPath();

        return "http://" + host + ":" + port + "/" + webapppath;
    }

    public URL getOutputFileURL(String currentWorkdir, String filename) throws IOException {
        // check if file exists
        String path = currentWorkdir + "/" + filename;
        File out = new File(path);
        if ( ! (out.isFile() && out.canRead()))
            throw new IOException("Error in creating URL: " + currentWorkdir + " / " + path + " not found or broken.");

        // create URL
        path = path.substring(WebProcessingService.BASE_DIR.length() + 1, path.length());
        String urlString = getUrlPathUpToWebapp() + "/" + path;

        return new URL(urlString);
    }

    /**
     * start RServe on Linux
     * 
     * @throws RserveException
     * @throws InterruptedException
     * @throws IOException
     */
    private void startRServeOnLinux() throws RserveException, InterruptedException, IOException {
        String rserveStartCMD = "R CMD Rserve --vanilla --slave";
        Runtime.getRuntime().exec(rserveStartCMD).waitFor();
    }

    /**
     * start RServe on Windows
     * 
     * @throws RserveException
     * @throws IOException
     */
    private void startRServeOnWindows() throws RserveException, IOException {
        String rserveStartCMD = "cmd /c start R -e library(Rserve);Rserve() --vanilla --slave";
        Runtime.getRuntime().exec(rserveStartCMD);
    }

    /**
     * tries to start Rserve (runs "Rserve.bat" if batchfile.exists() && RSERVE_HOST == "localhost")
     * 
     * @throws IOException
     * @throws InterruptedException
     * @throws RserveException
     */
    public void startRserve() throws RserveException, InterruptedException, IOException {
        if (enableBatchStart) {
            LOGGER.debug("Trying to start Rserve locally");
            if (System.getProperty("os.name").toLowerCase().indexOf("linux") > -1) {
                startRServeOnLinux();
            }
            else if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1) {
                startRServeOnWindows();
            }
        }
    }

    /**
     * Filters R scripts as files with suffix ".R"
     */
    public static class RFileExtensionFilter implements FileFilter {

        @Override
        public boolean accept(File f) {
            if (f.isFile() && f.canRead()) {
                String name = f.getName();
                if (name.endsWith(getInstance().SCRIPT_FILE_SUFFIX))
                    return true;
            }
            return false;
        }
    }

    /**
     * @param file
     *        points to an R scripts
     * @return wellknownName is the corresponding process identifier
     */
    public String FileToWkn(File file) {
        String fileName = file.getName();
        // remove suffix, usually file ending ".R":
        int index = fileName.lastIndexOf('.');
        if (index > 0)
            fileName = fileName.substring(0, index);
        return WKN_PREFIX + fileName;
    }

    /**
     * @param wkn
     *        Process identifier
     * @return File points to corresponding R process script
     * @throws IOException
     */
    public File wknToFile(String wkn) throws IOException {
        String fname = wkn.replaceFirst(WKN_PREFIX, "");
        fname = getScriptDirFullPath() + "/" + fname;
        fname = fname + SCRIPT_FILE_SUFFIX;
        File out = new File(fname);
        if (out.isFile() && out.canRead()) {
            return out;
        }
        else
            throw new IOException("Error in Process: " + wkn + ", File " + fname + " not found or broken.");
    }

    public String createTemporaryWPSWorkDir() throws IOException {
        File tempdir = new File(System.getProperty("java.io.tmpdir"), "wps4r-wps-workdir-tmp-"
                + UUID.randomUUID().toString().substring(0, TEMPDIR_NAME_LENGTH)); // + ".tmp");
        tempdir.mkdir();
        return tempdir.getAbsolutePath();
    }

//    public String createTemporaryRWorkDir() throws IOException {
//        File tempdir = new File(System.getProperty("java.io.tmpdir"), "wps4r-r-workdir-"
//                + UUID.randomUUID().toString().substring(0, TEMPDIR_NAME_LENGTH)); // + ".tmp");
//        tempdir.mkdir();
//        return tempdir.getAbsolutePath();
//    }

    public String getScriptDirFullPath() {
        return new File(WebProcessingService.BASE_DIR, SCRIPT_DIR).getAbsolutePath();
        // return SCRIPT_DIR_FULL;
    }

    /**
     * 
     * @param wkn
     * @return
     */
    public boolean isScriptAvailable(String wkn) {
        try {
            wknToFile(wkn);
            return true;
        }
        catch (IOException e) {
            LOGGER.error("Script file unavailable for process id " + wkn, e);
            return false;
        }
    }

    /**
     * Tests if a script associated with a process is valid Any errors will be logged
     * 
     * @param wkn
     * @return
     */
    public boolean isScriptValid(String wkn) {
        try {
            File file = wknToFile(wkn);
            RAnnotationParser.validateScript(new FileInputStream(file), wkn);
            return true;
        }
        catch (IOException e) {
            LOGGER.error("Script file unavailable for process " + wkn + ".", e);
            return false;
        }
        catch (Exception e) {
            LOGGER.error("Validation of process " + wkn + " failed.", e);
            return false;
        }
    }

    public void killRserveOnWindows() {
        try {
            if (Runtime.getRuntime().exec("taskkill /IM RServe.exe /T /F").waitFor() == 0)
                ;
            return;
        }
        catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
