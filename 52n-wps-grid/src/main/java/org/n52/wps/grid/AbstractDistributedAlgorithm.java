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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionsDocument;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.IData;
import org.n52.wps.server.IDistributedAlgorithm;
import org.n52.wps.server.request.InputHandler;

/**
 * @author bastian
 * 
 */
public abstract class AbstractDistributedAlgorithm implements IDistributedAlgorithm
{
	private static Logger LOGGER = Logger.getLogger(AbstractDistributedAlgorithm.class);

	private IDistributedComputingClient distributedClient;
	private ProcessDescriptionType processDescription;
	private String wellKnownName;
	
	public String uncioreClientClassName;
	public Properties uncioreProperties;
	
//	public String registry;
//	public String keystore;
//	public String alias;
//	public String password;
//	public String type;
//	public boolean overwriteRemoteFile;
//	public int maximumNumberOfNodes;

	public AbstractDistributedAlgorithm()
	{
		distributedClient = null;
		processDescription = initializeDescription();
		wellKnownName = "";
	}

	public void setDistributedComputingClient(String pUncioreClientClassName, Properties pUncioreProperties)
	{
		uncioreClientClassName = pUncioreClientClassName;
		uncioreProperties = pUncioreProperties;
	}

	public WebProcessingServiceOutput run(ExecuteDocument pExecuteDocument) throws Exception
	{
		// create distributed client
		LOGGER.info("Load dynamic class <" + uncioreClientClassName + ">.");
		Class<?> algorithmClass = AbstractDistributedAlgorithm.class.getClassLoader().loadClass(uncioreClientClassName);
		distributedClient = (IDistributedComputingClient)algorithmClass.newInstance();
		distributedClient.setConfiguration(uncioreProperties);
		
		// get additional information
		String algorithmIdentifier = pExecuteDocument.getExecute().getIdentifier().getStringValue();
		List<String> applicationFiles = getApplicationFiles(algorithmIdentifier);

		// create standard web processing service input data structure
		InputHandler parser = new InputHandler(pExecuteDocument.getExecute().getDataInputs().getInputArray(), algorithmIdentifier);		
		Map<String, List<IData>> inputParameter = parser.getParsedInputData();
		WebProcessingServiceInput input = new WebProcessingServiceInput(inputParameter);

		// split standard web processing service input data into smaller chuncks
		List<WebProcessingServiceInput> inputList = split(input, distributedClient.getMaximumNumberOfNodes());

		// TODO free unused memory (for example remove inline data from
		// execution document)

		// create distributable algorithm input data structure
		List<DistributedAlgorithmInput> algorithmInputList = new ArrayList<DistributedAlgorithmInput>();
		for (WebProcessingServiceInput currentInput : inputList)
		{
			DistributedAlgorithmInput algorithmInput = new DistributedAlgorithmInput(currentInput, processDescription, pExecuteDocument, applicationFiles,
					WPSConfig.getInstance());
			algorithmInputList.add(algorithmInput);
		}
	
		// perform distributed computation
		List<DistributedAlgorithmOutput> algorithmOutputList;
		try
		{
			algorithmOutputList = distributedClient.run(algorithmInputList);
		}
		catch (Exception e)
		{
			LOGGER.error("Error while creating and executing UNICORE 6 client for process '" + algorithmIdentifier + "'.");
			LOGGER.error(e);
			throw new RuntimeException("Error while creating and executing UNICORE 6 client for process '" + algorithmIdentifier + "'.", e);
		}

		// merge output of tasks
		List<WebProcessingServiceOutput> outputList = new ArrayList<WebProcessingServiceOutput>();
		for (DistributedAlgorithmOutput output : algorithmOutputList)
		{
			outputList.add(output.getOutput());
		}
		WebProcessingServiceOutput output = merge(outputList);

		return output;
	}

	public List<String> getErrors()
	{
		return new ArrayList<String>();
	}

	public ProcessDescriptionType getDescription()
	{
		return processDescription;
	}

	public String getWellKnownName()
	{
		return wellKnownName;
	}

	public boolean processDescriptionIsValid()
	{
		return processDescription.validate();
	}

	private ProcessDescriptionType initializeDescription()
	{
		String className = this.getClass().getName().replace(".", "/");
		InputStream xmlDesc = this.getClass().getResourceAsStream("/" + className + ".xml");

		try
		{
			/* read process description from configuration file */
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();

			ProcessDescriptionsDocument processDescriptionDocument = ProcessDescriptionsDocument.Factory.parse(xmlDesc, option);

			if (processDescriptionDocument.getProcessDescriptions().getProcessDescriptionArray().length == 0)
			{
				LOGGER.warn("ProcessDescription does not contain correct any description");
				return null;
			}

			ProcessDescriptionType processDescription = processDescriptionDocument.getProcessDescriptions().getProcessDescriptionArray(0);

			if (!processDescription.getIdentifier().getStringValue().equals(this.getClass().getName())
					&& !processDescription.getIdentifier().getStringValue().equals(this.getWellKnownName()))
			{
				processDescription.getIdentifier().setStringValue(this.getClass().getName());
				LOGGER.warn("Identifier was not correct, was changed now temporary for server use to " + this.getClass().getName()
						+ ". Please change it later in the description!");
			}

			/* extend process description with grid specific parameters */

			// InputDescriptionType input = processDescription.getDataInputs()
			// .addNewInput();
			// input.setMinOccurs(BigInteger.valueOf(1));
			// input.setMaxOccurs(BigInteger.valueOf(1));
			// input.addNewIdentifier().setStringValue(PARAM_GRID_NAME);
			// input.addNewTitle().setStringValue(PARAM_GRID_TITLE);
			// input.addNewAbstract().setStringValue(PARAM_GRID_ABSTRACT);
			// LiteralInputType literal = input.addNewLiteralData();
			// DomainMetadataType metadata = literal.addNewDataType();
			// metadata.setReference("xs:int");
			// metadata.setStringValue("1");
			// AllowedValues allowed = literal.addNewAllowedValues();
			// RangeType range = allowed.addNewRange();
			// range.addNewMinimumValue().setStringValue("1");
			// range.addNewMaximumValue().setStringValue("10");
			/* validate new process description */

			ArrayList validationErrors = new ArrayList();
			XmlOptions validationOptions = new XmlOptions();
			validationOptions.setErrorListener(validationErrors);

			boolean isValid = processDescription.validate(validationOptions);

			if (!isValid)
			{
				Iterator iter = validationErrors.iterator();
				while (iter.hasNext())
				{
					LOGGER.error(iter.next());
				}
			}

			return processDescription;
		}
		catch (IOException e)
		{
			LOGGER.warn("Could not initialize algorithm, parsing error: " + this.getClass().getName(), e);
		}
		catch (XmlException e)
		{
			LOGGER.warn("Could not initialize algorithm, parsing error: " + this.getClass().getName(), e);
		}
		return null;
	}

	private List<String> getApplicationFiles(String pAlgorithmIdentifier) throws RuntimeException
	{
		/* read algorithm specific application description file */
		List<String> applicationFiles = new ArrayList<String>();

		String className = this.getClass().getName().replace(".", "/");
		InputStream applicationDescription = this.getClass().getResourceAsStream("/" + className + ".application");

		Document document;
		try
		{
			document = new SAXBuilder().build(applicationDescription);
		}
		catch (IOException e1)
		{
			LOGGER.error("Error while reading application description file of process '" + pAlgorithmIdentifier + "'.");
			throw new RuntimeException("Error while reading application description file of process '" + pAlgorithmIdentifier + "'.");
		}
		catch (JDOMException e1)
		{
			LOGGER.error("Error while parsing application description file of process '" + pAlgorithmIdentifier + "'.");
			throw new RuntimeException("Error while parsing application description file of process '" + pAlgorithmIdentifier + "'.");
		}

		List children = document.getRootElement().getChildren("file_ref");

		/* create list of need libraries */
		Iterator childrenIterator = children.iterator();
		while (childrenIterator.hasNext())
		{
			Element fileRef = (Element) childrenIterator.next();
			Element fileName = fileRef.getChild("file_name");

			if (fileName != null)
			{
				applicationFiles.add(fileName.getValue());
			}
		}

		return applicationFiles;
	}
}
