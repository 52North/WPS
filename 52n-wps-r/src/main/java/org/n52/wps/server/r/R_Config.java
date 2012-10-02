
package org.n52.wps.server.r;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.ServerDocument.Server;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.WebProcessingService;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

public class R_Config {

    private static Logger LOGGER = Logger.getLogger(R_Config.class);

    public static final String SCRIPT_FILE_EXTENSION = "R";

    public static final String SCRIPT_FILE_SUFFIX = "." + SCRIPT_FILE_EXTENSION;

    public static final String WKN_PREFIX = "org.n52.wps.server.r.";

    // important directories:

    // FIXME for resources to be downloadable the cannot be in WEB-INF, or this must be handled with a
    // servlet, which is probably a better solution to keep track of files, see
    // http://www.jguru.com/faq/view.jsp?EID=10646
    private static final String R_BASE_DIR = "R";

    private static final String WORK_DIR = "workdir";

    private static final String UTILS_DIR = "utils";

    public static String SCRIPT_DIR = null;

    /**
     * Base directory for WPS4R resources
     */
    public static String BASE_DIR_FULL = (WebProcessingService.BASE_DIR + "/" + R_BASE_DIR).replace("\\", "/");

    /**
     * Work directory, e.g. for streaming files from Rserve to WPS. Not to confuse with the Rserve work
     * directory
     */
    public static String WORK_DIR_FULL = BASE_DIR_FULL + "/" + WORK_DIR;

    /** R scripts with utility functions to pre-load */
    public static String UTILS_DIR_FULL = BASE_DIR_FULL + "/" + UTILS_DIR;

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

    private String batchStartFile = "Rserve.bat";

    public String RESOURCE_DIR;

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
        RConnection con = new RConnection(RSERVE_HOST, RSERVE_PORT);
        if (con.needLogin())
            con.login(RSERVE_USER, RSERVE_PASSWORD);
        return con;
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
    public static String getConsoleOutput(RConnection rCon, String cmd) throws RserveException, REXPMismatchException {
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
    public static String getSessionInfo(RConnection rCon) throws RserveException, REXPMismatchException {
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
            e.printStackTrace();
            throw new RuntimeException("Error: R session info cannot be retrieved.");
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
        return (getUrlPathUpToWebapp() + "/" + RESOURCE_DIR).replace("\\", "/");
    }

    public URL getScriptURL(String wkn) throws MalformedURLException {
        String fname = null;
        try {
            fname = wknToFile(wkn).getName();
        }
        catch (IOException e) {
            e.printStackTrace();
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
     * tries to start Rserve (runs "Rserve.bat" if batchfile.exists() && RSERVE_HOST == "localhost")
     */
    public void startRserve() {
        try {
            if (enableBatchStart) {
                String batch = BASE_DIR_FULL + batchStartFile;
                File batchfile = new File(batch);
                if (batchfile.exists()) {
                    Runtime.getRuntime().exec(batch);
                }
                else
                    LOGGER.error("Batch file does not exist! " + batchfile);
            }
            else
                LOGGER.error("Batch start is disabled! (" + RWPSConfigVariables.ENABLE_BATCH_START.toString() + " = "
                        + enableBatchStart + ")");
        }
        catch (IOException e) {
            e.printStackTrace();
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
                if (name.endsWith(SCRIPT_FILE_SUFFIX))
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

    public String getTemporaryWPSWorkDirFullPath() {
        return WORK_DIR_FULL + "/" + UUID.randomUUID();
    }

    public static String getScriptDirFullPath() {
        return WebProcessingService.BASE_DIR + "/" + SCRIPT_DIR;
        // return SCRIPT_DIR_FULL;
    }
}
