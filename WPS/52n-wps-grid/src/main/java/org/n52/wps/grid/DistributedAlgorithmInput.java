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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.log4j.Logger;
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
import org.n52.wps.server.IDistributedAlgorithm.WebProcessingServiceInput;
import org.n52.wps.util.BasicXMLTypeFactory;
import org.w3c.dom.Node;

/**
 * @author bastian
 * 
 */
public class DistributedAlgorithmInput implements Serializable
{
	private static transient Logger LOGGER = Logger.getLogger(DistributedAlgorithmInput.class);

	protected transient WebProcessingServiceInput input;
	protected transient ProcessDescriptionType processDescription;
	protected transient ExecuteDocument executeDocument;
	protected transient List<String> applicationFiles;
	protected transient WPSConfig config;

	public DistributedAlgorithmInput(WebProcessingServiceInput pInput, ProcessDescriptionType pProcessDescription, ExecuteDocument pExecuteDocument,
			List<String> pApplicationFiles, WPSConfig pConfig)
	{
		input = pInput;
		processDescription = pProcessDescription;
		executeDocument = pExecuteDocument;
		applicationFiles = pApplicationFiles;
		config = pConfig;
	}

	private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException
	{
		try
		{
			oos.writeObject(processDescription.xmlText());
			oos.writeObject(executeDocument.xmlText());

			oos.writeObject(applicationFiles);

			oos.writeObject(config);

			Map<String, List<String>> serializedInputData = new HashMap<String, List<String>>();

			createSerialized(input, processDescription, executeDocument, serializedInputData);

			oos.writeObject(serializedInputData);
		}
		catch (ExceptionReport e)
		{
			LOGGER.error(e);
			throw new IOException(e.getMessage());
		}
	}

	private synchronized void readObject(java.io.ObjectInputStream oos) throws IOException, ClassNotFoundException
	{
		try
		{
			System.out.println("Read processDescription");
			String processDescriptionAsXml = (String) oos.readObject();
			processDescription = ProcessDescriptionType.Factory.parse(processDescriptionAsXml);

			System.out.println("Read executeDocument");
			String executeDocumentAsXml = (String) oos.readObject();
			executeDocument = ExecuteDocument.Factory.parse(executeDocumentAsXml);

			System.out.println("Read applicationFiles");
			applicationFiles = (List<String>) oos.readObject();

			System.out.println("Read config");
			config = (WPSConfig) oos.readObject();

			System.out.println("Read serializedInputData");
			Map<String, List<String>> serializedInputData = (Map<String, List<String>>) oos.readObject();

			Map<String, List<IData>> deserializedInputData = new HashMap<String, List<IData>>();

			System.out.println("createDeserialized ");
			createDeserialized(processDescription, executeDocument, serializedInputData, deserializedInputData);

			input = new WebProcessingServiceInput(deserializedInputData);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}

	private void createSerialized(WebProcessingServiceInput pInput, ProcessDescriptionType pProcessDescription, ExecuteDocument pExecuteDocument,
			Map<String, List<String>> pSerializedInputData) throws ExceptionReport
	{
		InputType[] inputParameterList = pExecuteDocument.getExecute().getDataInputs().getInputArray();

		for (InputType inputParameter : inputParameterList)
		{
			String inputId = inputParameter.getIdentifier().getStringValue();

			if (inputParameter.getData() != null)
			{
				if (inputParameter.getData().getComplexData() != null)
				{
					String data = createSerializedComplexData(pInput, pProcessDescription, pExecuteDocument, inputParameter);
					List<String> dataList = new ArrayList<String>();
					dataList.add(data);
					pSerializedInputData.put(inputId, dataList);
				}
				else if (inputParameter.getData().getLiteralData() != null)
				{
					String data = createSerializedLiteralData(pInput, pProcessDescription, pExecuteDocument, inputParameter);
					List<String> dataList = new ArrayList<String>();
					dataList.add(data);
					pSerializedInputData.put(inputId, dataList);
				}
				else if (inputParameter.getData().getBoundingBoxData() != null)
				{
					LOGGER.error("Serialization of BBOX is not supported.");
					throw new ExceptionReport("Serialization of BBOX is not supported.", ExceptionReport.OPERATION_NOT_SUPPORTED);
				}
			}
			else if (inputParameter.getReference() != null)
			{
				// String data = createSerializedReferenceData(pInput,
				// pProcessDescription, pExecuteDocument, inputParameter);
				String data = createSerializedComplexData(pInput, pProcessDescription, pExecuteDocument, inputParameter);
				List<String> dataList = new ArrayList<String>();
				dataList.add(data);
				pSerializedInputData.put(inputId, dataList);
			}
			else
			{
				LOGGER.error("Error while accessing input parameter '" + inputId + "'.");
				throw new ExceptionReport("Error while accessing input parameter '" + inputId + "'.", ExceptionReport.INVALID_PARAMETER_VALUE);
			}
		}
	}

	private void createDeserialized(ProcessDescriptionType pProcessDescription, ExecuteDocument pExecuteDocument,
			Map<String, List<String>> pSerializedInputData, Map<String, List<IData>> pDeSerializedInputData) throws ExceptionReport
	{
		InputType[] inputParameterList = pExecuteDocument.getExecute().getDataInputs().getInputArray();

		for (InputType inputParameter : inputParameterList)
		{
			String inputId = inputParameter.getIdentifier().getStringValue();

			if (inputParameter.getData() != null)
			{
				if (inputParameter.getData().getComplexData() != null)
				{
					IData obj = createDeserializedComplexData(pProcessDescription, pExecuteDocument, inputParameter, pSerializedInputData);
					List<IData> dataList = new ArrayList<IData>();
					dataList.add(obj);
					pDeSerializedInputData.put(inputId, dataList);
				}
				else if (inputParameter.getData().getLiteralData() != null)
				{
					IData obj = createDeserializedLiteralData(pProcessDescription, pExecuteDocument, inputParameter, pSerializedInputData);
					List<IData> dataList = new ArrayList<IData>();
					dataList.add(obj);
					pDeSerializedInputData.put(inputId, dataList);
				}
				else if (inputParameter.getData().getBoundingBoxData() != null)
				{
					System.out.println("Deserialization of BBOX is not supported.");
					throw new ExceptionReport("Serialization of BBOX is not supported.", ExceptionReport.OPERATION_NOT_SUPPORTED);
				}
			}
			else if (inputParameter.getReference() != null)
			{
				IData obj = createDeserializedComplexData(pProcessDescription, pExecuteDocument, inputParameter, pSerializedInputData);
				List<IData> dataList = new ArrayList<IData>();
				dataList.add(obj);
				pDeSerializedInputData.put(inputId, dataList);
			}
			else
			{
				LOGGER.error("Error while accessing input parameter '" + inputId + "'.");
				throw new ExceptionReport("Error while accessing input parameter '" + inputId + "'.", ExceptionReport.INVALID_PARAMETER_VALUE);
			}
		}
	}

	private String createSerializedComplexData(WebProcessingServiceInput pInput, ProcessDescriptionType pProcessDescription, ExecuteDocument pExecuteDocument,
			InputType pInputParameter) throws ExceptionReport
	{
		String inputId = pInputParameter.getIdentifier().getStringValue();
		
		InputDescriptionType inputDescription = null;
		for (InputDescriptionType tempInputDescription : pProcessDescription.getDataInputs().getInputArray())
		{
			if (inputId.equals(tempInputDescription.getIdentifier().getStringValue()))
			{
				inputDescription = tempInputDescription;
				break;
			}
		}

		if (inputDescription == null)
		{
			LOGGER.debug("Error while accessing input parameter description of parameter '" + inputId + "'.");
		}

		String schema = null;
		String encoding = null;
		String mimeType = null;

		if (pInputParameter.getData() != null)
		{
			schema = pInputParameter.getData().getComplexData().getSchema();
			encoding = pInputParameter.getData().getComplexData().getEncoding();
			mimeType = pInputParameter.getData().getComplexData().getMimeType();
		}
		else if (pInputParameter.getReference() != null)
		{
			schema = pInputParameter.getReference().getSchema();
			encoding = pInputParameter.getReference().getEncoding();
			mimeType = pInputParameter.getReference().getMimeType();
		}

		String algorithmIdentifier = pExecuteDocument.getExecute().getIdentifier().getStringValue();

		Generator[] generators = config.getRegisteredGenerators();
		GeneratorFactory.initialize(generators);

		Class algorithmInput = RepositoryManager.getInstance().getInputDataTypeForAlgorithm(algorithmIdentifier, inputId);
		IGenerator generator = GeneratorFactory.getInstance().getGenerator(schema, mimeType, encoding, algorithmInput);

		if (generator == null)
		{
			generator = GeneratorFactory.getInstance().getSimpleXMLGenerator();
		}

		if (generator instanceof AbstractXMLGenerator)
		{
			// TODO does not support multiple input parameters with same id
			IData data = pInput.getInputData().get(inputId).get(0);
			Node xmlNode = ((AbstractXMLGenerator) generator).generateXML(data, null);
			try
			{
				XmlObject xmlObj = XmlObject.Factory.parse(xmlNode);
				return xmlObj.xmlText();
			}
			catch (Exception e)
			{
				LOGGER.error("Error while converting raw data to XML representation for input parameter '" + inputId + "'.");
				throw new ExceptionReport("Error while converting raw data to XML representation for input parameter '" + inputId + "'.",
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

	/**
	 * @param pInput
	 * @param pProcessDescription
	 * @param pExecuteDocument
	 * @param pInputParameter
	 * @return
	 * @throws ExceptionReport
	 */
	private String createSerializedLiteralData(WebProcessingServiceInput pInput, ProcessDescriptionType pProcessDescription, ExecuteDocument pExecuteDocument,
			InputType pInputParameter) throws ExceptionReport
	{
		String inputId = pInputParameter.getIdentifier().getStringValue();

		String xmlDataType = pInputParameter.getData().getLiteralData().getDataType();
		if (xmlDataType == null)
		{
			InputDescriptionType inputDescription = null;
			for (InputDescriptionType tempInputDescription : pProcessDescription.getDataInputs().getInputArray())
			{
				if (inputId.equals(tempInputDescription.getIdentifier().getStringValue()))
				{
					inputDescription = tempInputDescription;
					break;
				}
			}
			xmlDataType = inputDescription.getLiteralData().getDataType().getReference();
		}

		IData data = pInput.getInputData().get(inputId).get(0);
		return BasicXMLTypeFactory.getStringRepresentation(xmlDataType, data);
	}

	/**
	 * @param pInput
	 * @param pProcessDescription
	 * @param pExecuteDocument
	 * @param pInputParameter
	 * @return
	 * @throws ExceptionReport
	 */
	private IData createDeserializedComplexData(ProcessDescriptionType pProcessDescription, ExecuteDocument pExecuteDocument, InputType pInputParameter,
			Map<String, List<String>> pSerializedInputData) throws ExceptionReport
	{
		String inputId = pInputParameter.getIdentifier().getStringValue();

		InputDescriptionType inputDescription = null;
		for (InputDescriptionType tempInputDescription : pProcessDescription.getDataInputs().getInputArray())
		{
			if (inputId.equals(tempInputDescription.getIdentifier().getStringValue()))
			{
				inputDescription = tempInputDescription;
				break;
			}
		}

		if (inputDescription == null)
		{
			System.out.println("Error while accessing input parameter description of parameter '" + inputId + "'.");

		}

		String schema = null;
		String encoding = null;
		String mimeType = null;

		if (pInputParameter.getData() != null)
		{
			schema = pInputParameter.getData().getComplexData().getSchema();
			encoding = pInputParameter.getData().getComplexData().getEncoding();
			mimeType = pInputParameter.getData().getComplexData().getMimeType();
		}
		else if (pInputParameter.getReference() != null)
		{
			schema = pInputParameter.getReference().getSchema();
			encoding = pInputParameter.getReference().getEncoding();
			mimeType = pInputParameter.getReference().getMimeType();
		}

		if (schema == null)
		{
			schema = inputDescription.getComplexData().getDefault().getFormat().getSchema();
		}
		if (mimeType == null)
		{
			mimeType = inputDescription.getComplexData().getDefault().getFormat().getMimeType();
		}
		if (encoding == null)
		{
			encoding = inputDescription.getComplexData().getDefault().getFormat().getEncoding();
		}

		Parser[] parsers = config.getRegisteredParser();
		ParserFactory.initialize(parsers);

		String algorithmIdentifier = pExecuteDocument.getExecute().getIdentifier().getStringValue();

		Class algorithmInput = RepositoryManager.getInstance().getInputDataTypeForAlgorithm(algorithmIdentifier, inputId);
		IParser parser = ParserFactory.getInstance().getParser(schema, mimeType, encoding, algorithmInput);

		if (parser == null)
		{
			parser = ParserFactory.getInstance().getSimpleParser();
		}

		if (parser instanceof AbstractXMLParser)
		{
			// TODO does not support multiple input parameter with same id
			String data = pSerializedInputData.get(inputId).get(0);
			return ((AbstractXMLParser) parser).parseXML(data);
		}
		else if (parser instanceof AbstractBinaryParser)
		{
			System.out.println("Binary response data is not supported.");
			throw new ExceptionReport("Binary response data is not supported.", ExceptionReport.OPERATION_NOT_SUPPORTED);
		}
		else
		{
			System.out.println("The parser '" + parser.getClass().getName() + "' does not support deserialization.");
			throw new ExceptionReport("The parser '" + parser.getClass().getName() + "' does not support deserialization.",
					ExceptionReport.OPERATION_NOT_SUPPORTED);
		}
	}

	/**
	 * @param pInput
	 * @param pProcessDescription
	 * @param pExecuteDocument
	 * @param pInputParameter
	 * @return
	 * @throws ExceptionReport
	 */
	private IData createDeserializedLiteralData(ProcessDescriptionType pProcessDescription, ExecuteDocument pExecuteDocument, InputType pInputParameter,
			Map<String, List<String>> pSerializedInputData) throws ExceptionReport
	{
		String inputId = pInputParameter.getIdentifier().getStringValue();

		String xmlDataType = pInputParameter.getData().getLiteralData().getDataType();
		if (xmlDataType == null)
		{
			InputDescriptionType inputDescription = null;
			for (InputDescriptionType tempInputDescription : pProcessDescription.getDataInputs().getInputArray())
			{
				if (inputId.equals(tempInputDescription.getIdentifier().getStringValue()))
				{
					inputDescription = tempInputDescription;
					break;
				}
			}
			xmlDataType = inputDescription.getLiteralData().getDataType().getReference();
		}

		String data = pSerializedInputData.get(inputId).get(0);
		return BasicXMLTypeFactory.getBasicJavaObject(xmlDataType, data);
	}

	public WebProcessingServiceInput getInput()
	{
		return input;
	}

	/**
	 * @return
	 */
	public ProcessDescriptionType getProcessDescription()
	{
		return processDescription;
	}

	/**
	 * @return
	 */
	public ExecuteDocument getExecuteDocument()
	{
		return executeDocument;
	}

	/**
	 * @return
	 */
	public List<String> getApplicationFiles()
	{
		return applicationFiles;
	}

	/**
	 * @return
	 */
	public WPSConfig getConfig()
	{
		return config;
	}
}