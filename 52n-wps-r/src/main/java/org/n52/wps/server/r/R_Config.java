package org.n52.wps.server.r;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.n52.wps.ServerDocument.Server;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.WebProcessingService;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

public class R_Config {

	//important directories:
	/**
	 * Base directory for WPS4R resources
	 */
	public static String R_DIR = (WebProcessingService.BASE_DIR + "/WEB-INF/R").replace("\\", "/");
	
	/**
	 * Work directory, e.g. for streaming files from Rserve to WPS
	 * Not to confuse with the Rserve work directory 
	 * (except if you want to use it as such)
	 */
	public static String WORK_DIR = R_DIR + "/workdir";
	
	/** R scripts with utility functions to pre-load*/
	public static String UTILS_DIR = R_DIR + "/utils";
	
	/**Location of all R process scripts**/
	public static String SCRIPT_DIR = (WebProcessingService.BASE_DIR +"/R/r_scripts").replace("\\", "/");
	
	//public static String SCRIPT_DIR_URL = "wps/rscripts";
	
	/** Host IP for Rserve **/
	public static String RSERVE_HOST = "localhost";
	
	/** Port were Rserve is listening**/
	public static int RSERVE_PORT = 6311;
	
	/** Applied user ID for Rserve (if login needed) **/
	public static String RSERVE_USER;
	
	/** Applied user password for Rserve (if login needed) **/
	public static String RSERVE_PASSWORD;
	
	/** Starts R serve via batch file**/
	public static boolean enableBatchStart = false;
	
	
	public static RConnection openRConnection() throws RserveException{
		RConnection con =  new RConnection(RSERVE_HOST, RSERVE_PORT);
		if(con.needLogin())
			con.login(RSERVE_USER, RSERVE_PASSWORD);
		return con;
	}
	
	/**
	 * Captures the Console printout of R for a specific string
	 * @param rCon
	 * @param cmd command which has been used
	 * @return R text output as formatted string
	 * @throws RserveException
	 * @throws REXPMismatchException
	 */
	public static String getConsoleOutput(RConnection rCon, String cmd) throws RserveException, REXPMismatchException{
		return rCon.eval("paste(capture.output(print("+cmd+")),collapse='\\n')").asString();
		
	}
	
	/**
	 * Retrieves R session info from current R session
	 * 
	 * @param rcon Open R connection
	 * @return R text output as formatted string
	 * @throws RserveException
	 * @throws REXPMismatchException
	 * 
	 * @see getConsoleOutput(RConnection rCon, String cmd)
	 */
	public static String getSessionInfo(RConnection rCon) throws RserveException, REXPMismatchException{
		return getConsoleOutput(rCon, "sessionInfo()");
	}
	
	/**
	 * "Quiet" retrieval of the R session information
	 * @return R text output as formatted string
	 * @throws RuntimeException if session info cannot be retrieved
	 * @see getConsoleOutput(RConnection rCon, String cmd)
	 * @see getSessionInfo(RConnection rCon)
	 */
	public static String getSessionInfo(){
		RConnection rCon = null;
		String sessionInfo = "";
		try {
			rCon = openRConnection();
			sessionInfo = getSessionInfo(rCon);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error: R session info cannot be retrieved.");
		}finally{
			if(rCon!=null)
				rCon.close();
		}
		
		return sessionInfo;
	}
	
	public static String getSessionInfoURL(){
		Server server = WPSConfig.getInstance().getWPSConfig().getServer();
		String host = server.getHostname();
		String port = server.getHostport();
		String webapppath =server.getWebappPath();

		return "http://"+host+":"+port+"/"+webapppath+"/R/sessioninfo.jsp";
	}
	
	public static String getScriptURL(String wkn){
		String fname = null;
		try {
			fname = R_Config.wknToFile(wkn).getName();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String bdir = WebProcessingService.BASE_DIR.replace("\\", "/")+"/";
		String folder = SCRIPT_DIR.replace(bdir, "");
		
		Server server = WPSConfig.getInstance().getWPSConfig().getServer();
		String host = server.getHostname();
		String port = server.getHostport();
		String webapppath =server.getWebappPath();
		if(fname == null )
			return "not available";
		else
			return "http://"+host+":"+port+"/"+webapppath+"/"+folder+"/"+fname;
	}
	/**
	 * tries to start Rserve 
	 * (runs "Rserve.bat" if batchfile.exists() && RSERVE_HOST == "localhost")
	 */
	public static void startRserve(){
		try {
			String batch = R_DIR + "/Rserve.bat";
			File batchfile = new File(batch);
			if(batchfile.exists() && enableBatchStart){
				Runtime.getRuntime().exec(batch);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Filters R scripts as files with suffix ".R"
	 */
	public static class ScriptFilter implements FileFilter{
		@Override
		public boolean accept(File f) {
			if(f.isFile() && f.canRead()){
				String name = f.getName();
				if(name.endsWith(".R"))
					return true;
			}
			return false;
		}
	}
	
	/**
	 * @param file points to an R scripts
	 * @return wellknownName is the corresponding process identifier
	 */
	public static String FileToWkn(File file){
		String fileName = file.getName();
		//remove suffix, usually file ending ".R":
		int index = fileName.lastIndexOf('.');
		if(index > 0)
			fileName = fileName.substring(0, index);
		return "org.n52.wps.server.r."+fileName;
	}
	
	/**
	 * @param wkn Process identifier
	 * @return File points to corresponding R process script
	 * @throws IOException
	 */
	public static File wknToFile(String wkn)throws IOException{
		String fname = wkn;
		fname = fname.replaceFirst("org.n52.wps.server.r.",SCRIPT_DIR+"/");
		fname = fname + ".R";
		File out = new File(fname);
		if(out.isFile() && out.canRead()){
			return out;
		}else
			throw new IOException("Error in Process: "+wkn+", File "+fname+" not found or broken.");
	}
}



