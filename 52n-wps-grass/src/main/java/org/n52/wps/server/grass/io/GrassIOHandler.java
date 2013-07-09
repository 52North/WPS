/***************************************************************
Copyright © 2009 52∞North Initiative for Geospatial Open Source Software GmbH

 Author: Benjamin Proﬂ, 52∞North

 Contact: Andreas Wytzisk, 
 52∞North Initiative for Geospatial Open Source SoftwareGmbH, 
 Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 
 info@52north.org

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; even without the implied WARRANTY OF
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt). If not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA 02111-1307, USA or visit the Free
 Software Foundationís web page, http://www.fsf.org.

 ***************************************************************/
package org.n52.wps.server.grass.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.GenericFileDataConstants;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.server.WebProcessingService;
import org.n52.wps.server.grass.GrassProcessRepository;
import org.n52.wps.server.grass.util.StreamGobbler;

public class GrassIOHandler {
	
	private String grassHome;
	private String pythonHome;	
	private String pythonPath;	
	private String grassModuleStarterHome;	
	private String tmpDir;
	private String inputTxtFilename;
	private String command;	
	private String systemBlock;
	private String grassBlock;
	private String complexInputDataBlock;
	private String literalInputDataBlock;
	private String outputDataBlock;	
	private String uuid;	
	private String pythonName = "python.exe";	
	private String addonPath;	
	private String[] envp;
	private boolean isAddon;
	private static Logger LOGGER = LoggerFactory.getLogger(GrassIOHandler.class);
	
	private final String logFilename = ".log";
	private final String stdErrorFilename = "_stderr.log";
	private final String stdOutFilename = "_stdout.log ";	
	private final String fileSeparator = System.getProperty("file.separator");
	private final String lineSeparator = System.getProperty("line.separator");
	private final String appDataDir = System.getenv("APPDATA");
	
	public static final String GRASS_ADDON_PATH = "ßaddonPathß";	
	public static final String PROCESS_IDENTIFIER = "ßprocess_identifierß";
	public static final String INPUT_IDENTIFIER = "ßinput_identifierß";
	public static final String INPUT_PATH = "ßinput_pathß";
	public static final String DATA_TYPE = "ßdatatypeß";
	public static final String VALUE = "ßvalueß";
	public static final String OUTPUT_IDENTIFIER = "ßoutput_identifierß";
	public static final String OUTPUT_PATH = "ßoutput_pathß";
	public static final String SCHEMA = "ßschemaß";
	public static final String ENCODING = "ßencodingß";
	public static final String MIMETYPE = "ßmimetypeß";
	public static final String WORKDIR = "ßworkdirß";
	public static final String OUTPUTDIR = "ßoutputdirß";	
	public static final String OS_Name = System.getProperty("os.name");
	
	public GrassIOHandler(){		
		
		if(!OS_Name.startsWith("Windows")){
			pythonName = "python";	
		}
		
		grassHome = GrassProcessRepository.grassHome;
		grassModuleStarterHome = GrassProcessRepository.grassModuleStarterHome;
		tmpDir = GrassProcessRepository.tmpDir;
		pythonHome = GrassProcessRepository.pythonHome;
		pythonPath = GrassProcessRepository.pythonPath;
		addonPath = GrassProcessRepository.addonPath;

		File tmpDirectory = new File(tmpDir);
		
		if(!tmpDirectory.exists()){
			tmpDirectory.mkdir();
		}
	}	
	
	/**
	 * Method to execute a GRASS GIS process.
	 * 
	 * @param processID the name of the process
	 * @param complexInputData complex inputdata for the process
	 * @param literalInputData literal inputdata for the process
	 * @param outputID the ID of the output
	 * @param outputMimeType the mimetype of the output
	 * @param outputSchema the schema of the output
	 * @return a GenericFileDataBinding containing the generated ouput
	 */
	public IData executeGrassProcess(String processID, Map<String, IData> complexInputData, Map<String, IData> literalInputData, String outputID, String outputMimeType, String outputSchema, boolean isAddon){
		
		String outputFileName = "";
		
		this.isAddon = isAddon;
		
		outputFileName = tmpDir + fileSeparator + "out" + UUID.randomUUID().toString().substring(0, 5) + "." + GenericFileDataConstants.mimeTypeFileTypeLUT().get(outputMimeType);
		
		boolean success = createInputTxt(processID, complexInputData, literalInputData, outputID, outputFileName, outputMimeType, outputSchema);
		
		if(!success){
			inputTxtFilename = null;	
			return null;
		}
		
		//start grassmodulestarter.py 
		executeGrassModuleStarter();
		
		File outputFile = new File(outputFileName);
		
		if(!outputFile.exists()){
			inputTxtFilename = null;	
			return null;
		}
		
		//give back genericfiledatabinding with the outputfile created by grass
		try {
			
			GenericFileData outputFileData = new GenericFileData(outputFile, outputMimeType);
			
			GenericFileDataBinding outputData = new GenericFileDataBinding(outputFileData);	
			
			return outputData;
			
		} catch (IOException e) {
			e.printStackTrace();
			
			return null;
		}finally{
			inputTxtFilename = null;
		}		
	}
	
	private String getCommand() {
		
		if(command == null){
			uuid = UUID.randomUUID().toString().substring(0, 7);
			command  = getPythonHome() + fileSeparator + pythonName + " "
			+ grassModuleStarterHome + fileSeparator + "GrassModuleStarter.py -f " + getInputTxtFilename() +" -l " + tmpDir + fileSeparator + uuid + logFilename + " -o " + tmpDir + fileSeparator + stdOutFilename + " -e " + tmpDir + fileSeparator + stdErrorFilename;
		}
		
		return command;
	}

	private String[] getEnvp() {

		if (envp == null) {

			if (!OS_Name.startsWith("Windows")) {

				envp = new String[] {
						"GDAL_DATA=" + grassHome + fileSeparator + "etc"
								+ fileSeparator + "ogr_csv",
						"PATH=" + grassHome + fileSeparator + "lib:"
								+ grassHome + fileSeparator + "bin:"
								+ grassHome + fileSeparator + "scripts:"
								+ pythonHome + ":" + grassHome + fileSeparator
								+ "extralib:" + grassHome + fileSeparator
								+ "extrabin",
						"LD_LIBRARY_PATH=" + grassHome + fileSeparator + "lib",
						"PWD=" + grassHome,
						"PYTHONHOME=" + pythonHome,
						"PYTHONPATH=" + grassHome + fileSeparator + "etc"
								+ fileSeparator + "python" + ":" + pythonPath,
						"GRASS_CONFIG_DIR=.grass7",
						"GRASS_GNUPLOT=gnuplot -persist", "GRASS_PAGER=less",
						"GRASS_PYTHON=python", "GRASS_SH=/bin/sh",
						"GRASS_VERSION=7.0.svn"};		
				
			} else {

				envp = new String[] {
						"APPDATA=" + appDataDir,
						"GDAL_DATA=" + grassHome + "\\etc\\ogr_csv",
						"PWD=" + grassHome,
//						"PATH=C:\\grass\\wps-grass-bridge2\\gms\\Testing\\Python\\GrassAddons;C:\\Programme\\GRASS-70-SVN\\lib;C:\\Programme\\GRASS-70-SVN\\bin;C:\\Programme\\GRASS-70-SVN\\scripts;C:\\OSGeo4W\\bin;",
//						"PYTHONPATH=" + grassHome + "\\etc\\python;c:\\python25",
//						"PYTHONHOME=C:\\OSGeo4W\\apps\\Python25",
						"PYTHONHOME=" + pythonHome,
						"GRASS_CONFIG_DIR=.grass7",
						"GRASS_GNUPLOT=gnuplot -persist", "GRASS_PAGER=less",
						"GRASS_PYTHON=python", "GRASS_SH=/bin/sh",
						"GRASS_VERSION=7.0.svn", "SystemRoot=" + System.getenv("SystemRoot"),
						"WINGISBASE=" + grassHome 
						};				
			}
		}

		return envp;
	}

	private String getInputTxtFilename() {
		
		if(inputTxtFilename == null){
			
			String txtID = UUID.randomUUID().toString();
			
			txtID = txtID.substring(0, 5);
			
			inputTxtFilename = tmpDir + fileSeparator + txtID + ".txt";
		}
		return inputTxtFilename;
	}

	private String getSystemBlock() {
		if(systemBlock == null){
			systemBlock = "[System]" + lineSeparator + " WorkDir=" + WORKDIR
			+ lineSeparator + " OutputDir=" + OUTPUTDIR + lineSeparator;
		}
		return systemBlock;
	}

	private String getGrassBlock() {
		if(grassBlock == null){
			grassBlock = "[GRASS]"
				+ lineSeparator
				+ " GISBASE=" + grassHome
				+ lineSeparator + " GRASS_ADDON_PATH=" + GRASS_ADDON_PATH  + lineSeparator + " GRASS_VERSION=7.0.svn"
				+ lineSeparator + " Module=" + PROCESS_IDENTIFIER + lineSeparator + " LOCATION="
				+ lineSeparator + " LinkInput=FALSE" + lineSeparator + " IgnoreProjection=FALSE"
				+ lineSeparator + " UseXYLocation=FALSE" + lineSeparator;
		}
		return grassBlock;
	}

	private String getComplexInputDataBlock() {
		if(complexInputDataBlock == null){
			complexInputDataBlock  = "[ComplexData]" + lineSeparator
			+ " Identifier=" + INPUT_IDENTIFIER + lineSeparator
			+ " MaxOccurs=1" + lineSeparator
			+ " PathToFile=" + INPUT_PATH + lineSeparator + " MimeType=" + MIMETYPE + lineSeparator
			+ " Encoding=" + ENCODING + lineSeparator
			+ " Schema=" + SCHEMA + lineSeparator;
		}
		return complexInputDataBlock;
	}

	private String getLiteralInputDataBlock() {
		if(literalInputDataBlock == null){
			literalInputDataBlock = "[LiteralData]" + lineSeparator
			+ " Identifier=" + INPUT_IDENTIFIER + lineSeparator + " DataType=" + DATA_TYPE + lineSeparator
			+ " Value=" + VALUE + lineSeparator;
		}
		return literalInputDataBlock;
	}

	private String getOutputDataBlock() {
		if(outputDataBlock == null){
			outputDataBlock = "[ComplexOutput]" + lineSeparator
			+ " Identifier=" + OUTPUT_IDENTIFIER + lineSeparator
			+ " PathToFile=" + OUTPUT_PATH + lineSeparator + " MimeType=" + MIMETYPE + lineSeparator
			+ " Encoding=" + ENCODING + lineSeparator
			+ " Schema=" + SCHEMA + lineSeparator;
		}
		return outputDataBlock;
	}

	private String getPythonHome() {
		if(pythonHome == null){
			pythonHome = grassHome + "\\extrabin";
		}
		return pythonHome;
	}
	
	/**
	 * Creates the txt-file required by the GrassModuleStarter.py
	 * 
	 * @param processID
	 *            ID of the process
	 * @param complexInputData
	 *            complexinputdata for the process
	 * @param literalInputData
	 *            literalinputdata for the process
	 * @param outputID
	 *            ID of the outputdata
	 * @param outputFileName
	 *            name and path to the result of the GRASS-process
	 * @param outputMimeType
	 *            suggested mimetype of the result of the GRASS-process
	 * @return true, if everything worked, otherwise false
	 */
	private boolean createInputTxt(String processID, Map<String, IData> complexInputData, Map<String, IData> literalInputData, String outputID, String outputFileName, String outputMimeType, String outputSchema){
	
		try {
			
			LOGGER.info("Creating input.txt.");
			
			BufferedWriter inputTxtWriter = new BufferedWriter(new FileWriter(new File(getInputTxtFilename())));
			
			String tmpBlock;
			
			tmpBlock = getSystemBlock().replace(WORKDIR, tmpDir);
			tmpBlock = tmpBlock.replace(OUTPUTDIR, tmpDir);
			
			inputTxtWriter.write(tmpBlock);
			inputTxtWriter.write(lineSeparator);
			
			tmpBlock  = getGrassBlock().replace(PROCESS_IDENTIFIER, processID); 
			if(isAddon){
				tmpBlock  = tmpBlock.replace(GRASS_ADDON_PATH, addonPath); 			
			}else{				
				tmpBlock  = tmpBlock.replace(GRASS_ADDON_PATH, ""); 			
			}
			
			inputTxtWriter.write(tmpBlock);
			inputTxtWriter.write(lineSeparator);
			
			for (String key : complexInputData.keySet()) {
				
				IData data = complexInputData.get(key);
				
				if(!(data instanceof GenericFileDataBinding)){
					continue;
				}
				
				String mimetype = ((GenericFileDataBinding)data).getPayload().getMimeType();
				
				if(mimetype.equals(GenericFileDataConstants.MIME_TYPE_TIFF)){					
					tmpBlock = getComplexInputDataBlock().replace(MIMETYPE, mimetype);
					tmpBlock = tmpBlock.replace(ENCODING, "");
					tmpBlock = tmpBlock.replace(SCHEMA, "");
				}else if( mimetype.equals(GenericFileDataConstants.MIME_TYPE_ZIPPED_SHP)){
					tmpBlock = getComplexInputDataBlock().replace(MIMETYPE, "APPLICATION/SHP");
					tmpBlock = tmpBlock.replace(ENCODING, "");
					tmpBlock = tmpBlock.replace(SCHEMA, "");
				}else if(mimetype.equals(GenericFileDataConstants.MIME_TYPE_PLAIN_TEXT)){
					tmpBlock = getComplexInputDataBlock().replace(MIMETYPE, GenericFileDataConstants.MIME_TYPE_PLAIN_TEXT);
					tmpBlock = tmpBlock.replace(ENCODING, "UTF-8");
					tmpBlock = tmpBlock.replace(SCHEMA, "");
				}else if(mimetype.equals(GenericFileDataConstants.MIME_TYPE_TEXT_XML)){
					tmpBlock = getComplexInputDataBlock().replace(MIMETYPE, "TEXT/XML");
					tmpBlock = tmpBlock.replace(ENCODING, "UTF-8");
					tmpBlock = tmpBlock.replace(SCHEMA, "GML");
				}else if(mimetype.equals(GenericFileDataConstants.MIME_TYPE_KML)){
					tmpBlock = getComplexInputDataBlock().replace(MIMETYPE, "TEXT/XML");
					tmpBlock = tmpBlock.replace(ENCODING, "UTF-8");
					tmpBlock = tmpBlock.replace(SCHEMA, "KML");
				}else if(mimetype.equals(GenericFileDataConstants.MIME_TYPE_IMAGE_PNG)){
					tmpBlock = getComplexInputDataBlock().replace(MIMETYPE, "IMAGE/PNG");
					tmpBlock = tmpBlock.replace(ENCODING, "");
					tmpBlock = tmpBlock.replace(SCHEMA, "");
				}else if(mimetype.equals(GenericFileDataConstants.MIME_TYPE_IMAGE_JPEG)){
					tmpBlock = getComplexInputDataBlock().replace(MIMETYPE, "IMAGE/JPEG");
					tmpBlock = tmpBlock.replace(ENCODING, "");
					tmpBlock = tmpBlock.replace(SCHEMA, "");
				}else if(mimetype.equals(GenericFileDataConstants.MIME_TYPE_IMAGE_GIF)){
					tmpBlock = getComplexInputDataBlock().replace(MIMETYPE, "IMAGE/GIF");
					tmpBlock = tmpBlock.replace(ENCODING, "");
					tmpBlock = tmpBlock.replace(SCHEMA, "");
				}else if(mimetype.equals(GenericFileDataConstants.MIME_TYPE_IMAGE_GEOTIFF)){
					tmpBlock = getComplexInputDataBlock().replace(MIMETYPE, "IMAGE/GEOTIFF");
					tmpBlock = tmpBlock.replace(ENCODING, "");
					tmpBlock = tmpBlock.replace(SCHEMA, "");
				}else if(mimetype.equals(GenericFileDataConstants.MIME_TYPE_HDF)){
					tmpBlock = getComplexInputDataBlock().replace(MIMETYPE, "APPLICATION/HDF4Image");
					tmpBlock = tmpBlock.replace(ENCODING, "");
					tmpBlock = tmpBlock.replace(SCHEMA, "");
				}else if(mimetype.equals(GenericFileDataConstants.MIME_TYPE_X_ERDAS_HFA)){
					tmpBlock = getComplexInputDataBlock().replace(MIMETYPE, "APPLICATION/X-ERDAS-HFA");
					tmpBlock = tmpBlock.replace(ENCODING, "");
					tmpBlock = tmpBlock.replace(SCHEMA, "");
				}else if(mimetype.equals(GenericFileDataConstants.MIME_TYPE_NETCDF)){
					tmpBlock = getComplexInputDataBlock().replace(MIMETYPE, "APPLICATION/NETCDF");
					tmpBlock = tmpBlock.replace(ENCODING, "");
					tmpBlock = tmpBlock.replace(SCHEMA, "");
				}else if(mimetype.equals(GenericFileDataConstants.MIME_TYPE_DGN)){
					tmpBlock = getComplexInputDataBlock().replace(MIMETYPE, "APPLICATION/DGN");
					tmpBlock = tmpBlock.replace(ENCODING, "");
					tmpBlock = tmpBlock.replace(SCHEMA, "");
				}					
				
				String filename = ((GenericFileDataBinding)data).getPayload().writeData(new File(tmpDir));
				
				tmpBlock = tmpBlock.replace(INPUT_IDENTIFIER, key);
				tmpBlock = tmpBlock.replace(INPUT_PATH, filename);
				
				inputTxtWriter.write(tmpBlock);
				inputTxtWriter.write(lineSeparator);
			}
			
			for (String key : literalInputData.keySet()) {
				
				IData data = literalInputData.get(key);
							
				tmpBlock = getLiteralInputDataBlock().replace(INPUT_IDENTIFIER, key);
				
				Class<?> supportedClass = data.getSupportedClass();
				
				if(supportedClass.equals(Float.class)){
					tmpBlock = tmpBlock.replace(DATA_TYPE, "float");
				}else if(supportedClass.equals(Double.class)){
					tmpBlock = tmpBlock.replace(DATA_TYPE, "double");
				}else if(supportedClass.equals(Integer.class)){
					tmpBlock = tmpBlock.replace(DATA_TYPE, "integer");
				}else if(supportedClass.equals(Long.class)){
					tmpBlock = tmpBlock.replace(DATA_TYPE, "integer");
				}else if(supportedClass.equals(String.class)){
					tmpBlock = tmpBlock.replace(DATA_TYPE, "string");
				}else if(supportedClass.equals(Boolean.class)){
					tmpBlock = tmpBlock.replace(DATA_TYPE, "boolean");
				}
				
				tmpBlock = tmpBlock.replace(VALUE, String.valueOf(data.getPayload()));
				
				inputTxtWriter.write(tmpBlock);
				inputTxtWriter.write(lineSeparator);
			}
			
			tmpBlock = getOutputDataBlock().replace(OUTPUT_IDENTIFIER, outputID);
			tmpBlock = tmpBlock.replace(OUTPUT_PATH, outputFileName);
			
			if(outputMimeType.equals(GenericFileDataConstants.MIME_TYPE_PLAIN_TEXT)){
				tmpBlock = tmpBlock.replace(MIMETYPE, "text/plain");
				tmpBlock = tmpBlock.replace(ENCODING, "");
				tmpBlock = tmpBlock.replace(SCHEMA, "");				
			}else if(outputMimeType.equals(GenericFileDataConstants.MIME_TYPE_ZIPPED_SHP)){
				tmpBlock = tmpBlock.replace(MIMETYPE, "APPLICATION/SHP");
				tmpBlock = tmpBlock.replace(ENCODING, "");
				tmpBlock = tmpBlock.replace(SCHEMA, "");
			}else if(outputMimeType.equals(GenericFileDataConstants.MIME_TYPE_TIFF) || outputMimeType.equals(GenericFileDataConstants.MIME_TYPE_GEOTIFF)){
				tmpBlock = tmpBlock.replace(MIMETYPE, GenericFileDataConstants.MIME_TYPE_TIFF);
				tmpBlock = tmpBlock.replace(ENCODING, "");
				tmpBlock = tmpBlock.replace(SCHEMA, "");
			}else if(outputMimeType.equals(GenericFileDataConstants.MIME_TYPE_TEXT_XML)){ 
				tmpBlock = tmpBlock.replace(MIMETYPE, GenericFileDataConstants.MIME_TYPE_TEXT_XML);
				tmpBlock = tmpBlock.replace(ENCODING, "UTF-8");
				tmpBlock = tmpBlock.replace(SCHEMA, "http://schemas.opengis.net/gml/3.1.0/polygon.xsd");//TODO change to gml2.1.2?!
			}else if(outputMimeType.equals(GenericFileDataConstants.MIME_TYPE_KML)){
				tmpBlock = tmpBlock.replace(MIMETYPE, "TEXT/XML");
				tmpBlock = tmpBlock.replace(ENCODING, "UTF-8");
				tmpBlock = tmpBlock.replace(SCHEMA, "KML");
			}else if(outputMimeType.equals(GenericFileDataConstants.MIME_TYPE_IMAGE_PNG)){
				tmpBlock = tmpBlock.replace(MIMETYPE, "IMAGE/PNG");
				tmpBlock = tmpBlock.replace(ENCODING, "");
				tmpBlock = tmpBlock.replace(SCHEMA, "");
			}else if(outputMimeType.equals(GenericFileDataConstants.MIME_TYPE_IMAGE_JPEG)){
				tmpBlock = tmpBlock.replace(MIMETYPE, "IMAGE/JPEG");
				tmpBlock = tmpBlock.replace(ENCODING, "");
				tmpBlock = tmpBlock.replace(SCHEMA, "");
			}else if(outputMimeType.equals(GenericFileDataConstants.MIME_TYPE_IMAGE_GIF)){
				tmpBlock = tmpBlock.replace(MIMETYPE, "IMAGE/GIF");
				tmpBlock = tmpBlock.replace(ENCODING, "");
				tmpBlock = tmpBlock.replace(SCHEMA, "");
			}else if(outputMimeType.equals(GenericFileDataConstants.MIME_TYPE_IMAGE_GEOTIFF)){
				tmpBlock = tmpBlock.replace(MIMETYPE, "IMAGE/GEOTIFF");
				tmpBlock = tmpBlock.replace(ENCODING, "");
				tmpBlock = tmpBlock.replace(SCHEMA, "");
			}else if(outputMimeType.equals(GenericFileDataConstants.MIME_TYPE_HDF)){
				tmpBlock = tmpBlock.replace(MIMETYPE, "APPLICATION/HDF4Image");
				tmpBlock = tmpBlock.replace(ENCODING, "");
				tmpBlock = tmpBlock.replace(SCHEMA, "");
			}else if(outputMimeType.equals(GenericFileDataConstants.MIME_TYPE_X_ERDAS_HFA)){
				tmpBlock = tmpBlock.replace(MIMETYPE, "APPLICATION/X-ERDAS-HFA");
				tmpBlock = tmpBlock.replace(ENCODING, "");
				tmpBlock = tmpBlock.replace(SCHEMA, "");
			}else if(outputMimeType.equals(GenericFileDataConstants.MIME_TYPE_NETCDF)){
				tmpBlock = tmpBlock.replace(MIMETYPE, "APPLICATION/NETCDF");
				tmpBlock = tmpBlock.replace(ENCODING, "");
				tmpBlock = tmpBlock.replace(SCHEMA, "");
			}else if(outputMimeType.equals(GenericFileDataConstants.MIME_TYPE_DGN)){
				tmpBlock = tmpBlock.replace(MIMETYPE, "APPLICATION/DGN");
				tmpBlock = tmpBlock.replace(ENCODING, "");
				tmpBlock = tmpBlock.replace(SCHEMA, "");
			}
			
			inputTxtWriter.write(tmpBlock);
			
			inputTxtWriter.flush();
			inputTxtWriter.close();
			
			return true;
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private void executeGrassModuleStarter() {

		try {

			LOGGER.info("Executing GRASS module starter.");
			
			Runtime rt = Runtime.getRuntime();
			
			Process proc = rt.exec(getCommand(), getEnvp());
			
	        PipedOutputStream pipedOut = new PipedOutputStream();
	        
	        PipedInputStream pipedIn = new PipedInputStream(pipedOut);  
			
			// any error message?
			StreamGobbler errorGobbler = new StreamGobbler(proc
					.getErrorStream(), "ERROR", pipedOut);

			// any output?
			StreamGobbler outputGobbler = new StreamGobbler(proc
					.getInputStream(), "OUTPUT");
			
			// kick them off
			errorGobbler.start();
			outputGobbler.start();
			
			//fetch errors if there are any
			BufferedReader errorReader = new BufferedReader(new InputStreamReader(pipedIn));
			
			String line = errorReader.readLine();
			
			String errors = "";		
			
			while(line != null){			
				
				errors = errors.concat(line + lineSeparator);
				
				line = errorReader.readLine();
			}			
			
			try {
				proc.waitFor();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}finally{
				proc.destroy();
			}

			if(!errors.equals("")){
				String baseDir = WebProcessingService.BASE_DIR + File.separator + "GRASS_LOGS";
				File baseDirFile = new File(baseDir);
				if(!baseDirFile.exists()){
					baseDirFile.mkdir();
				}
				String host = WPSConfig.getInstance().getWPSConfig().getServer().getHostname();
				if(host == null) {
					host = InetAddress.getLocalHost().getCanonicalHostName();
				}
				String hostPort = WPSConfig.getInstance().getWPSConfig().getServer().getHostport();
				File tmpLog = new File(tmpDir + fileSeparator + uuid + logFilename);
				File serverLog = new File(baseDir + fileSeparator + uuid + logFilename);
				
				if(tmpLog.exists()){
				    FileInputStream fis  = new FileInputStream(tmpLog);
				    FileOutputStream fos = new FileOutputStream(serverLog);
				    try {
				        byte[] buf = new byte[1024];
				        int i = 0;
				        while ((i = fis.read(buf)) != -1) {
				            fos.write(buf, 0, i);
				        }
				    } 
				    catch (Exception e) {
				        e.printStackTrace();
				    }
				    finally {
				        if (fis != null) fis.close();
				        if (fos != null) fos.close();
				    }

				}else{
					BufferedWriter bufWrite = new BufferedWriter(new FileWriter(serverLog));
					bufWrite.write(errors);
					bufWrite.flush();
					bufWrite.close();
				}
				LOGGER.error("An error occured while executing the GRASS GIS process.");
				throw new RuntimeException("An error occured while executing the GRASS GIS process. See the log under " + "http://" + host + ":" + hostPort+ "/" + WebProcessingService.WEBAPP_PATH + "/GRASS_LOGS/" + uuid + logFilename + " for more details.");
			}
			
		} catch (IOException e) {
			LOGGER.error("An error occured while executing the GRASS GIS process.", e);
			throw new RuntimeException(e);
		}
	}
	
}
