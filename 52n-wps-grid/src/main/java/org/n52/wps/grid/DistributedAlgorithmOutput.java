/*******************************************************************************
 * Copyright (C) 2008
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
 * 
 * Author: Bastian Baranski (Bastian.Baranski@uni-muenster.de)
 * Created: 03.09.2008
 * Modified: 03.09.2008
 *
 ******************************************************************************/

package org.n52.wps.grid;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.wps.GeneratorDocument.Generator;
import org.n52.wps.ParserDocument.Parser;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.GeneratorFactory;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.IParser;
import org.n52.wps.io.ParserFactory;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.binary.AbstractBinaryGenerator;
import org.n52.wps.io.datahandler.binary.AbstractBinaryParser;
import org.n52.wps.io.datahandler.xml.AbstractXMLGenerator;
import org.n52.wps.io.datahandler.xml.AbstractXMLParser;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.RepositoryManager;
import org.n52.wps.server.IDistributedAlgorithm.WebProcessingServiceOutput;
import org.w3c.dom.Node;

/**
 * @author bastian
 * 
 */
public class DistributedAlgorithmOutput implements Serializable
{
	private static transient Logger LOGGER = Logger.getLogger(DistributedAlgorithmOutput.class);

	protected transient WebProcessingServiceOutput output;
	protected transient ProcessDescriptionType processDescription;
	protected transient ExecuteDocument executeDocument;
	protected transient List<String> applicationFiles;
	protected transient WPSConfig config;

	public DistributedAlgorithmOutput(WebProcessingServiceOutput pOutput, ProcessDescriptionType pProcessDescription, ExecuteDocument pExecuteDocument,
			List<String> pApplicationFiles, WPSConfig pConfig)
	{
		output = pOutput;
		processDescription = pProcessDescription;
		executeDocument = pExecuteDocument;
		applicationFiles = pApplicationFiles;
		config = pConfig;
	}

	public WebProcessingServiceOutput getOutput()
	{
		return output;
	}

	public ProcessDescriptionType getProcessDescription()
	{
		return processDescription;
	}

	public ExecuteDocument getExecuteDocument()
	{
		return executeDocument;
	}

	public List<String> getApplicationFiles()
	{
		return applicationFiles;
	}

	private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException
	{
		try
		{
			oos.writeObject(processDescription.xmlText());
			oos.writeObject(executeDocument.xmlText());

			oos.writeObject(applicationFiles);

			oos.writeObject(config);

			Map serializedData = new HashMap();

			createSerialized(output, processDescription, executeDocument, serializedData);

			oos.writeObject(serializedData);
		}
		catch (ExceptionReport e)
		{
			LOGGER.error(e);
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * @param oos
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws XmlException
	 * @throws ExceptionReport
	 * @throws Exception
	 */
	private synchronized void readObject(java.io.ObjectInputStream oos) throws IOException, ClassNotFoundException
	{
		try
		{
			String processDescriptionAsXml = (String) oos.readObject();
			processDescription = ProcessDescriptionType.Factory.parse(processDescriptionAsXml);

			String executeDocumentAsXml = (String) oos.readObject();
			executeDocument = ExecuteDocument.Factory.parse(executeDocumentAsXml);

			applicationFiles = (List<String>) oos.readObject();

			config = (WPSConfig) oos.readObject();

			Map serializedData = (Map) oos.readObject();

			Map deserializedData = new HashMap();

			createDeserialized(processDescription, executeDocument, serializedData, deserializedData);

			output = new WebProcessingServiceOutput(deserializedData);
		}
		catch (XmlException e)
		{
			LOGGER.error(e);
			throw new IOException(e.getMessage());
		}
		catch (ExceptionReport e)
		{
			LOGGER.error(e);
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * @param pOutput
	 * @param pProcessDescription
	 * @param pExecuteDocument
	 * @param serializedData
	 * @throws ExceptionReport
	 */
	private void createSerialized(WebProcessingServiceOutput pOutput, ProcessDescriptionType pProcessDescription, ExecuteDocument pExecuteDocument,
			Map<String, String> pSerializedData) throws ExceptionReport
	{
		for (OutputDescriptionType outputDescription : processDescription.getProcessOutputs().getOutputArray())
		{
			String ouputId = outputDescription.getIdentifier().getStringValue();

			if (outputDescription.getComplexOutput() != null)
			{
				String data = createSerializedComplexOutput(pOutput, pProcessDescription, pExecuteDocument, outputDescription);
				pSerializedData.put(ouputId, data);
			}
			else

			if (outputDescription.getLiteralOutput() != null)
			{
				LOGGER.error("Deserialization of literal output data is not supported.");
				throw new ExceptionReport("Serialization of literal output data is not supported.", ExceptionReport.OPERATION_NOT_SUPPORTED);
			}
			else

			if (outputDescription.getBoundingBoxOutput() != null)
			{
				LOGGER.error("Deserialization of BBOX output data is not supported.");
				throw new ExceptionReport("Serialization of BBOX output data is not supported.", ExceptionReport.OPERATION_NOT_SUPPORTED);
			}
		}
	}

	private void createDeserialized(ProcessDescriptionType pProcessDescription, ExecuteDocument pExecuteDocument, Map pSerializedData, Map pDeserializedData)
			throws ExceptionReport
	{
		for (OutputDescriptionType outputDescription : processDescription.getProcessOutputs().getOutputArray())
		{
			String ouputId = outputDescription.getIdentifier().getStringValue();

			if (outputDescription.getComplexOutput() != null)
			{
				Object data = createDeserializedComplexData(pProcessDescription, pExecuteDocument, outputDescription, pSerializedData);
				pDeserializedData.put(ouputId, data);
			}
			else

			if (outputDescription.getLiteralOutput() != null)
			{
				LOGGER.error("Deserialization of literal output data is not supported.");
				throw new ExceptionReport("Serialization of literal output data is not supported.", ExceptionReport.OPERATION_NOT_SUPPORTED);
			}
			else

			if (outputDescription.getBoundingBoxOutput() != null)
			{
				LOGGER.error("Deserialization of BBOX output data is not supported.");
				throw new ExceptionReport("Serialization of BBOX output data is not supported.", ExceptionReport.OPERATION_NOT_SUPPORTED);
			}
		}
	}

	/**
	 * @param pOutput
	 * @param pProcessDescription
	 * @param pExecuteDocument
	 * @param pOutputDescription
	 * @return
	 * @throws ExceptionReport
	 */
	private String createSerializedComplexOutput(WebProcessingServiceOutput pOutput, ProcessDescriptionType pProcessDescription,
			ExecuteDocument pExecuteDocument, OutputDescriptionType pOutputDescription) throws ExceptionReport
	{
		String schema = null;
		String mimeType = null;
		String encoding = null;

		if (schema == null)
		{
			schema = pOutputDescription.getComplexOutput().getDefault().getFormat().getSchema();
		}

		if (mimeType == null)
		{
			mimeType = pOutputDescription.getComplexOutput().getDefault().getFormat().getMimeType();
		}

		if (encoding == null)
		{
			encoding = pOutputDescription.getComplexOutput().getDefault().getFormat().getEncoding();
		}

		String ouputId = pOutputDescription.getIdentifier().getStringValue();
		String algorithmIdentifier = pExecuteDocument.getExecute().getIdentifier().getStringValue();

		Generator[] generators = config.getRegisteredGenerators();
		GeneratorFactory.initialize(generators);

		Class algorithmOutput = RepositoryManager.getInstance().getOutputDataTypeForAlgorithm(algorithmIdentifier, ouputId);
		IGenerator generator = GeneratorFactory.getInstance().getGenerator(schema, mimeType, encoding, algorithmOutput);

		if (generator == null)
		{
			generator = GeneratorFactory.getInstance().getSimpleXMLGenerator();
		}

		if (generator instanceof AbstractXMLGenerator)
		{
			IData obj = pOutput.getOutputData().get(ouputId);
			Node xmlNode = ((AbstractXMLGenerator) generator).generateXML(obj, null);

			try
			{
				XmlObject xmlObj = XmlObject.Factory.parse(xmlNode);
				return xmlObj.xmlText();
			}
			catch (XmlException e)
			{
				LOGGER.error("Error while converting raw data to XML representation for ouput parameter '" + ouputId + "'.");
				throw new ExceptionReport("Error while converting raw data to XML representation for output parameter '" + ouputId + "'.",
						ExceptionReport.NO_APPLICABLE_CODE);
			}
		}
		else if (generator instanceof AbstractBinaryGenerator)
		{
			LOGGER.error("Binary response data is not supported.");
			throw new ExceptionReport("Binary response data is not supported.", ExceptionReport.OPERATION_NOT_SUPPORTED);
		}
		else
		{
			LOGGER.error("The generator '" + generator.getClass().getName() + "' does not support serialization.");
			throw new ExceptionReport("The generator '" + generator.getClass().getName() + "' does not support serialization.",
					ExceptionReport.OPERATION_NOT_SUPPORTED);
		}
	}

	private Object createDeserializedComplexData(ProcessDescriptionType pProcessDescription, ExecuteDocument pExecuteDocument,
			OutputDescriptionType pOutputDescription, Map pSerializedData) throws ExceptionReport
	{
		String schema = null;
		String mimeType = null;
		String encoding = null;

		if (schema == null)
		{
			schema = pOutputDescription.getComplexOutput().getDefault().getFormat().getSchema();
		}

		if (mimeType == null)
		{
			mimeType = pOutputDescription.getComplexOutput().getDefault().getFormat().getMimeType();
		}

		if (encoding == null)
		{
			encoding = pOutputDescription.getComplexOutput().getDefault().getFormat().getEncoding();
		}

		String ouputId = pOutputDescription.getIdentifier().getStringValue();
		String algorithmIdentifier = pExecuteDocument.getExecute().getIdentifier().getStringValue();
		
		Parser[] parsers = config.getRegisteredParser();
		ParserFactory.initialize(parsers);

		Class algorithmOutput = RepositoryManager.getInstance().getOutputDataTypeForAlgorithm(algorithmIdentifier, ouputId);
		IParser parser = ParserFactory.getInstance().getParser(schema, mimeType, encoding, algorithmOutput);
		if (parser == null)
		{
			parser = ParserFactory.getInstance().getSimpleParser();
		}

		if (parser instanceof AbstractXMLParser)
		{
			String data = (String) pSerializedData.get(ouputId);
			return ((AbstractXMLParser) parser).parseXML(data);
		}
		else if (parser instanceof AbstractBinaryParser)
		{
			LOGGER.error("Binary response data is not supported.");
			throw new ExceptionReport("Binary response data is not supported.", ExceptionReport.OPERATION_NOT_SUPPORTED);
		}
		else
		{
			LOGGER.error("The parser '" + parser.getClass().getName() + "' does not support deserialization.");
			throw new ExceptionReport("The parser '" + parser.getClass().getName() + "' does not support deserialization.",
					ExceptionReport.OPERATION_NOT_SUPPORTED);
		}
	}
}