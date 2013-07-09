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
package org.n52.wps.server.grass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionsDocument;
import net.opengis.wps.x100.SupportedComplexDataInputType;
import net.opengis.wps.x100.SupportedComplexDataType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.xmlbeans.XmlException;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.data.GenericFileDataConstants;
import org.n52.wps.io.datahandler.parser.GenericFileParser;
import org.n52.wps.server.grass.io.GrassIOHandler;
import org.n52.wps.server.grass.util.StreamGobbler;

public class GrassProcessDescriptionCreator {
	
	private final String fileSeparator = System.getProperty("file.separator");
	private final String lineSeparator = System.getProperty("line.separator");
	private String grassHome = "";
	private String pythonHome = "";	
	private String pythonPath = "";	
	private String addonPath = "";	

	private String[] envp = null;
	
	private static Logger LOGGER = LoggerFactory.getLogger(GrassProcessDescriptionCreator.class);
	private final String wpsProcessDescCmd = " --wps-process-description";
	private Runtime rt = Runtime.getRuntime();
	private ExecutorService executor = Executors.newFixedThreadPool(10);
	private String gisrcDir;

	public GrassProcessDescriptionCreator() {

		grassHome = GrassProcessRepository.grassHome;
		pythonHome = GrassProcessRepository.pythonHome;
		pythonPath = GrassProcessRepository.pythonPath;
		gisrcDir = GrassProcessRepository.gisrcDir;
		addonPath = GrassProcessRepository.addonPath;
	}

	public ProcessDescriptionType createDescribeProcessType(String identifier, boolean addon)
			throws IOException, XmlException {

		Process proc = null;

		//addons have their own directory and are python scripts
		if(addon){

			if (!GrassIOHandler.OS_Name.startsWith("Windows")) {
				proc = rt.exec(addonPath + fileSeparator +
						identifier + wpsProcessDescCmd,
						getEnvp());
			} else {
				proc = rt.exec(pythonHome  + fileSeparator + "python.exe " + addonPath
						+ fileSeparator + identifier + ".py"
						+ wpsProcessDescCmd, getEnvp());
			}			
			
		} else {

			if (!GrassIOHandler.OS_Name.startsWith("Windows")) {
				proc = rt.exec(grassHome + fileSeparator + "bin"
						+ fileSeparator + identifier + wpsProcessDescCmd,
						getEnvp());
			} else {
				proc = rt.exec(grassHome + fileSeparator + "bin"
						+ fileSeparator + identifier + ".exe"
						+ wpsProcessDescCmd, getEnvp());
			}
		}
		
		PipedOutputStream pipedOut = new PipedOutputStream();

		PipedInputStream pipedIn = new PipedInputStream(pipedOut);

		PipedOutputStream pipedOutError = new PipedOutputStream();

		PipedInputStream pipedInError = new PipedInputStream(pipedOutError);
		
		// any error message?
		StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(),
				"ERROR", pipedOutError);

		// any output?
		StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(),
				"OUTPUT", pipedOut);

		executor.execute(errorGobbler);
		executor.execute(outputGobbler);

		BufferedReader xmlReader = new BufferedReader(new InputStreamReader(
				pipedIn));

		String line = xmlReader.readLine();

		String xml = "";

		while (line != null) {

			xml = xml.concat(line + lineSeparator);

			line = xmlReader.readLine();
		}

		pipedIn.close();
		pipedOut.close();
		xmlReader.close();

		BufferedReader errorReader = new BufferedReader(new InputStreamReader(
				pipedInError));

		String errorLine = errorReader.readLine();

		String errors = "";

		while (errorLine != null) {

			errors = errors.concat(errorLine + lineSeparator);

			errorLine = errorReader.readLine();
		}

		if (errors != "") {
			LOGGER.error("Error while creating processdescription for process "
					+ identifier + ": " + errors);
		}
		
		pipedInError.close();
		pipedOutError.close();
		errorReader.close();
		
		try {
			proc.waitFor();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		proc.destroy();

		ProcessDescriptionsDocument pDoc = ProcessDescriptionsDocument.Factory
				.parse(xml);

		int i = pDoc.getProcessDescriptions().getProcessDescriptionArray().length;

		if (i == 1) {

			ProcessDescriptionType result = pDoc.getProcessDescriptions()
					.getProcessDescriptionArray()[0];

			InputDescriptionType[] inputs = result.getDataInputs().getInputArray();
			
			for (InputDescriptionType inputDescriptionType : inputs) {
				checkForBase64Encoding(inputDescriptionType);
				checkForKMLMimeType(inputDescriptionType);					
			}			
			
			SupportedComplexDataType outputType = result.getProcessOutputs()
					.getOutputArray(0).getComplexOutput();

			if (outputType != null) {

				String schema = outputType.getDefault().getFormat().getSchema();

				if (schema != null) {

					if (schema
							.contains("http://schemas.opengis.net/gml/2.0.0/feature.xsd")
							|| schema
									.contains("http://schemas.opengis.net/gml/2.1.1/feature.xsd")
							|| schema
									.contains("http://schemas.opengis.net/gml/2.1.2/feature.xsd")
							|| schema
									.contains("http://schemas.opengis.net/gml/2.1.2.1/feature.xsd")
							|| schema
									.contains("http://schemas.opengis.net/gml/3.0.0/base/feature.xsd")
							|| schema
									.contains("http://schemas.opengis.net/gml/3.0.1/base/feature.xsd")
							|| schema
									.contains("http://schemas.opengis.net/gml/3.1.1/base/feature.xsd")) {

						ComplexDataDescriptionType xZippedShapeType = outputType.getSupported().addNewFormat();
																		
						xZippedShapeType.setMimeType(IOHandler.MIME_TYPE_ZIPPED_SHP);
						xZippedShapeType.setEncoding(IOHandler.ENCODING_BASE64);

					}
				}
				
				checkForBase64Encoding(result.getProcessOutputs()
						.getOutputArray(0));
				checkForKMLMimeType(result.getProcessOutputs()
						.getOutputArray(0));

			}

			return result;
		}

		return null;
	}

	private void checkForBase64Encoding(InputDescriptionType inputDescriptionType){
		SupportedComplexDataInputType complexData = inputDescriptionType.getComplexData();
		
		if(complexData == null){
			return;
		}
		
		String[] genericFileParserMimeTypes = new GenericFileParser().getSupportedFormats();
		
		String defaultMimeType = complexData.getDefault().getFormat().getMimeType();
		
		String defaultEncoding = complexData.getDefault().getFormat().getEncoding();
		
		if(defaultMimeType != null && defaultEncoding == null){			
			for (String mimeType : genericFileParserMimeTypes) {
				if(mimeType.equals(defaultMimeType)){
					complexData.getDefault().getFormat().setEncoding(IOHandler.ENCODING_BASE64);
				}
			}			
		}
		
		ComplexDataDescriptionType[] supportedTypes = complexData.getSupported().getFormatArray();
		
		for (ComplexDataDescriptionType complexDataDescriptionType : supportedTypes) {
			String supportedMimeType = complexDataDescriptionType.getMimeType();
			
			String supportedEncoding = complexDataDescriptionType.getEncoding();
			
			if(supportedMimeType != null && supportedEncoding == null){			
				for (String mimeType : genericFileParserMimeTypes) {
					if(mimeType.equals(supportedMimeType)){
						complexDataDescriptionType.setEncoding(IOHandler.ENCODING_BASE64);
					}
				}			
			}
		}
	}
	
	private void checkForBase64Encoding(OutputDescriptionType outputDescriptionType){
		SupportedComplexDataType complexData = outputDescriptionType.getComplexOutput();
		
		if(complexData == null){
			return;
		}
		
		String[] genericFileParserMimeTypes = new GenericFileParser().getSupportedFormats();
		
		String defaultMimeType = complexData.getDefault().getFormat().getMimeType();
		
		String defaultEncoding = complexData.getDefault().getFormat().getEncoding();
		
		if(defaultMimeType != null && defaultEncoding == null){			
			for (String mimeType : genericFileParserMimeTypes) {
				if(mimeType.equals(defaultMimeType)){
					complexData.getDefault().getFormat().setEncoding(IOHandler.ENCODING_BASE64);
				}
			}			
		}
		
		ComplexDataDescriptionType[] supportedTypes = complexData.getSupported().getFormatArray();
		
		for (ComplexDataDescriptionType complexDataDescriptionType : supportedTypes) {
			String supportedMimeType = complexDataDescriptionType.getMimeType();
			
			String supportedEncoding = complexDataDescriptionType.getEncoding();
			
			if(supportedMimeType != null && supportedEncoding == null){			
				for (String mimeType : genericFileParserMimeTypes) {
					if(mimeType.equals(supportedMimeType)){
						complexDataDescriptionType.setEncoding(IOHandler.ENCODING_BASE64);
					}
				}			
			}
		}
	}
	
	private void checkForKMLMimeType(InputDescriptionType inputDescriptionType) {
		
		SupportedComplexDataInputType complexData = inputDescriptionType.getComplexData();
		
		if(complexData == null){
			return;
		}
		
		if(complexData.getDefault().getFormat().getSchema() != null && complexData.getDefault().getFormat().getSchema().equals("http://schemas.opengis.net/kml/2.2.0/ogckml22.xsd")){
			complexData.getDefault().getFormat().setMimeType(GenericFileDataConstants.MIME_TYPE_KML);
			return; 
		}
		ComplexDataDescriptionType[] supportedTypes = complexData.getSupported().getFormatArray();
		
		for (ComplexDataDescriptionType complexDataDescriptionType : supportedTypes) {
			if(complexDataDescriptionType.getSchema() != null && complexDataDescriptionType.getSchema().equals("http://schemas.opengis.net/kml/2.2.0/ogckml22.xsd")){
				complexDataDescriptionType.setMimeType(GenericFileDataConstants.MIME_TYPE_KML);
				return;
			}
		}
		
	}

	private void checkForKMLMimeType(OutputDescriptionType outputDescriptionType) {
		
		SupportedComplexDataType complexData = outputDescriptionType.getComplexOutput();
		
		if(complexData == null){
			return;
		}
		
		if(complexData.getDefault().getFormat().getSchema() != null && complexData.getDefault().getFormat().getSchema().equals("http://schemas.opengis.net/kml/2.2.0/ogckml22.xsd")){
			complexData.getDefault().getFormat().setMimeType(GenericFileDataConstants.MIME_TYPE_KML);
			return; 
		}
		ComplexDataDescriptionType[] supportedTypes = complexData.getSupported().getFormatArray();
		
		for (ComplexDataDescriptionType complexDataDescriptionType : supportedTypes) {
			if(complexDataDescriptionType.getSchema() != null && complexDataDescriptionType.getSchema().equals("http://schemas.opengis.net/kml/2.2.0/ogckml22.xsd")){
				complexDataDescriptionType.setMimeType(GenericFileDataConstants.MIME_TYPE_KML);
				return;
			}
		}
		
	}
	
	/**
	 * @param args
	 * @throws IOException
	 * @throws XmlException
	 */
	public static void main(String[] args) throws IOException {

		ProcessDescriptionType type;
		try {
			type = new GrassProcessDescriptionCreator()
					.createDescribeProcessType("v.buffer", false);

			for (int i = 0; i < type.getDataInputs().getInputArray().length; i++) {

				InputDescriptionType inputDescType = type.getDataInputs()
						.getInputArray()[i];

				SupportedComplexDataInputType supCDataType = inputDescType
						.getComplexData();

				System.out.println(supCDataType);
			}
		} catch (XmlException e) {
			e.printStackTrace();
		}

	}

	private String[] getEnvp() {

		if (envp == null) {

			
			if (GrassIOHandler.OS_Name.startsWith("Windows")) {

				envp = new String[] {
						"GISRC=" + gisrcDir,
						"GDAL_DATA=" + grassHome + fileSeparator + "etc"
								+ fileSeparator + "ogr_csv",
						"GISBASE=" + grassHome,
						"PATH=" + grassHome + fileSeparator + "lib;"
								+ grassHome + fileSeparator + "bin;"
								+ grassHome + fileSeparator + "scripts;"
								+ pythonHome + ";" + grassHome + fileSeparator
								+ "extralib;" + grassHome + fileSeparator
								+ "extrabin",
						"LD_LIBRARY_PATH=" + grassHome + fileSeparator + "lib",
						"PWD=" + grassHome,
						"PYTHONHOME=" + pythonHome,
						"PYTHONPATH=" + grassHome + fileSeparator + "etc"
								+ fileSeparator + "python",
						"GRASS_CONFIG_DIR=.grass7",
						"GRASS_GNUPLOT=gnuplot -persist", "GRASS_PAGER=less",
						"GRASS_PYTHON=python", "GRASS_SH=/bin/sh",
						"GRASS_VERSION=7.0.svn", "WINGISBASE=" + grassHome };
			}else{
				
				envp = new String[] {
						"GISRC=" + gisrcDir,
						"GDAL_DATA=" + grassHome + fileSeparator + "etc"
								+ fileSeparator + "ogr_csv",
						"GISBASE=" + grassHome,
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
				
			}
		}
		return envp;
	}
	
}