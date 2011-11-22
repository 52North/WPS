/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. 

 Copyright (C) 2006 by con terra GmbH

 Authors: 
	Bastian Schaeffer, Institute for Geoinformatics, Muenster, Germany

 Contact: Albert Remke, con terra GmbH, Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 52n@conterra.de

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt); if not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA or visit the web page of the Free
 Software Foundation, http://www.fsf.org.

 ***************************************************************/

package org.n52.wps.server.profiles;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.opengis.ows.x11.ExceptionReportDocument;
import net.opengis.wps.x100.AuditTraceType;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.OutputDataType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionDocument;
import net.opengis.wps.x100.ProcessDescriptionType;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.complex.URLListDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractTransactionalAlgorithm;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.repository.TransactionalRepositoryManager;
import org.n52.wps.server.request.ExecuteRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import xint.esa.ssegrid.wps.javaSAGAProfile.URLListDocument;

public class DefaultTransactionalAlgorithm extends
		AbstractTransactionalAlgorithm {

	private List<String> errors;
	private static Logger LOGGER = Logger
			.getLogger(DefaultTransactionalAlgorithm.class);
	private ProcessDescriptionType processDescription;
	private String workspace;
	private IProcessManager processManager;

	private static final String OGC_OWS_URI = "http://www.opengeospatial.net/ows";

	public DefaultTransactionalAlgorithm(String processID) {
		super(processID);
		WPSConfig wpsConfig = WPSConfig.getInstance();
		Property[] properties = wpsConfig.getPropertiesForAlgorithm(processID);
		setWorkspace(wpsConfig.getPropertyForKey(properties,
				"WorkspaceLocationRoot").getStringValue());
		try {
			setProcessManager(TransactionalRepositoryManager
					.getProcessManagerForSchema(wpsConfig.getPropertyForKey(
							properties, "supportedFormat").getStringValue()));
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.errors = new ArrayList<String>();
		processDescription = initializeDescription();
	}

	public ProcessDescriptionType getDescription() {
		return processDescription;
	}

	// TODO : BPEL has nothing to do here...
	public HashMap<String, IData> run(ExecuteRequest req)
			throws ExceptionReport {
		ExecuteResponseDocument responseDocument;
		HashMap<String, IData> resultHash = new HashMap<String, IData>();
		/**
		 * Note cnl : The DefaultTransactionAlgorithm receives an
		 * ExecuteResponseDocument from the backend process manager If the
		 * process manager returns another kind of DOM document, another
		 * TransactionalAlgorithm should handle this.
		 */
		Document invokeResponse;
		LOGGER.info("DefaultTransactionAlgo Run");
		try {
			invokeResponse = getProcessManager().invoke(req,
					getAlgorithmID());
			/**
			 * Parsing
			 */
			LOGGER.info("invoke response");
			responseDocument = ExecuteResponseDocument.Factory
					.parse(invokeResponse);
			LOGGER.info(responseDocument.toString());
			OutputDataType[] resultValues = responseDocument
					.getExecuteResponse().getProcessOutputs().getOutputArray();
			for (int i = 0; i < resultValues.length; i++) {
				OutputDataType ioElement = resultValues[i];
				String key = ioElement.getIdentifier().getStringValue();
				if (ioElement.getData().isSetLiteralData()) {
					resultHash.put(key,
							OutputParser.handleLiteralValue(ioElement));
				}
				if (ioElement.getData().isSetComplexData()) {
					resultHash.put(key, OutputParser.handleComplexValue(
							ioElement, getDescription()));
				}
				/**
				 * TODO if(ioElement.isSetReference()){ resultHash.put(key,
				 * OutputParser.handleComplexValueReference(ioElement)); }
				 */
				if (ioElement.getData().getBoundingBoxData() != null) {
					resultHash
							.put(key, OutputParser.handleBBoxValue(ioElement));
				}
			}
		} catch (ExceptionReport e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			String error = "Could not create ExecuteResponseDocument";
			errors.add(error);
			LOGGER.warn(error + " Reason: " + e.getMessage());
			throw new ExceptionReport(error,
					ExceptionReport.REMOTE_COMPUTATION_ERROR);

		}
		return resultHash;
	}

	public List<String> getErrors() {
		return errors;
	}

	protected ProcessDescriptionType initializeDescription() {
		String fullPath = DefaultTransactionalAlgorithm.class
				.getProtectionDomain().getCodeSource().getLocation().toString();
		int searchIndex = fullPath.indexOf("WEB-INF");
		String subPath = fullPath.substring(0, searchIndex);
		subPath = subPath.replaceFirst("file:", "");
		String processID = getAlgorithmID();
		// sanitize processID: strip version number and namespace if passed in
		if (processID.contains("-"))
			processID = processID.split("-")[0];
		if (processID.contains("}"))
			processID = processID.split("}")[1];
		try {
			File xmlDesc = new File(subPath + File.separator + "WEB-INF"
					+ File.separator + "ProcessDescriptions" + File.separator
					+ processID + ".xml");
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();
			ProcessDescriptionDocument doc = ProcessDescriptionDocument.Factory
					.parse(xmlDesc, option);
			if (doc == null) {
				LOGGER.warn("ProcessDescription does not contain any description");
				return null;
			}

			doc.getProcessDescription().getIdentifier()
					.setStringValue(processID);

			return doc.getProcessDescription();
		} catch (IOException e) {
			LOGGER.warn("Could not initialize algorithm, parsing error: "
					+ getAlgorithmID(), e);
		} catch (XmlException e) {
			LOGGER.warn("Could not initialize algorithm, parsing error: "
					+ getAlgorithmID(), e);
		}
		return null;

	}

	public boolean processDescriptionIsValid() {
		return processDescription.validate();
	}

	private Document checkResultDocument(Document doc) {
		if (getFirstElementNode(doc.getFirstChild()).getNodeName().equals(
				"ExceptionReport")
				&& getFirstElementNode(doc.getFirstChild()).getNamespaceURI()
						.equals(OGC_OWS_URI)) {
			try {
				ExceptionReportDocument exceptionDoc = ExceptionReportDocument.Factory
						.parse(doc);
				throw new RuntimeException(
						"Error occured while executing query");
			} catch (Exception e) {
				throw new RuntimeException(
						"Error while parsing ExceptionReport retrieved from server",
						e);
			}
		}
		return doc;
	}

	private Node getFirstElementNode(Node node) {
		if (node == null) {
			return null;
		}
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			return node;
		} else {
			return getFirstElementNode(node.getNextSibling());
		}

	}

	public String getWellKnownName() {
		return "";
	}

	public Class getInputDataType(String id) {
		InputDescriptionType[] inputs = processDescription.getDataInputs()
				.getInputArray();
		for (InputDescriptionType input : inputs) {
			if (input.getIdentifier().getStringValue().equals(id)) {
				if (input.isSetLiteralData()) {
					String datatype = input.getLiteralData().getDataType()
							.getStringValue();
					if (datatype.contains("tring")) {
						return LiteralStringBinding.class;
					}
					if (datatype.contains("ollean")) {
						return LiteralBooleanBinding.class;
					}
					if (datatype.contains("loat") || datatype.contains("ouble")) {
						return LiteralDoubleBinding.class;
					}
					if (datatype.contains("nt")) {
						return LiteralIntBinding.class;
					}
				}
				if (input.isSetComplexData()) {
					LOGGER.info("-------- complex --- --- ");
					LOGGER.info(input.getComplexData().toString());
					LOGGER.info("-------- def--- --- ");
					LOGGER.info(input.getComplexData().getDefault().toString());
					LOGGER.info("--------format --- --- ");
					LOGGER.info(input.getComplexData().getDefault().getFormat()
							.toString());
					String mimeType = input.getComplexData().getDefault()
							.getFormat().getMimeType();
					if (mimeType.contains("xml") || (mimeType.contains("XML"))) {
						return URLListDataBinding.class;
					} else {
						return URLListDataBinding.class;
					}
				}
			}
		}
		throw new RuntimeException("Could not determie internal inputDataType");
	}

	public Class getOutputDataType(String id) {
		OutputDescriptionType[] outputs = processDescription
				.getProcessOutputs().getOutputArray();

		for (OutputDescriptionType output : outputs) {
			if(output.getIdentifier().getStringValue().equals(id)) {
				LOGGER.info("output is :"+id);
			if (output.isSetLiteralOutput()) {
				// Missing case when dataType is not present
				if(output.getLiteralOutput().getDataType() == null) {
					return LiteralStringBinding.class;
				}
				String datatype = output.getLiteralOutput().getDataType()
						.getStringValue();
				if (datatype.contains("tring")) {
					return LiteralStringBinding.class;
				}
				if (datatype.contains("ollean")) {
					return LiteralBooleanBinding.class;
				}
				if (datatype.contains("loat") || datatype.contains("ouble")) {
					return LiteralDoubleBinding.class;
				}
				if (datatype.contains("nt")) {
					return LiteralIntBinding.class;
				}
			}
			if (output.isSetComplexOutput()) {
				String mimeType = output.getComplexOutput().getDefault()
						.getFormat().getMimeType();
				if (mimeType.contains("xml") || (mimeType.contains("XML"))) {
					if (output.getComplexOutput().getDefault().getFormat()
							.getSchema().contains("wps")) {
						LOGGER.info("Output Data Type found is URLListDataBinding");
						return URLListDataBinding.class;
						
					} else {
						return GenericFileDataBinding.class;
					}

				} else {
					return GenericFileDataBinding.class;
				}
			}
			}
		}
		throw new RuntimeException("Could not determie internal inputDataType");
	}

	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		// TODO Auto-generated method stub
		// processManager
		return null;
	}

	public void setProcessManager(IProcessManager o) {
		this.processManager = o;
	}

	public IProcessManager getProcessManager() {
		return processManager;
	}

	public void setWorkspace(String workspace) {
		this.workspace = workspace;
	}

	public String getWorkspace() {
		return workspace;
	}

	public AuditTraceType getAudit() throws Exception {
		LOGGER.info("short");
		return getProcessManager().getAudit();
	}

	public AuditTraceType getAuditLongForm() throws Exception {
		LOGGER.info("long");
		return getProcessManager().getAuditLongForm();
	}
public void callback(ExecuteResponseDocument execRespDom) {
	getProcessManager().callback(execRespDom);
	return;
	}
}
