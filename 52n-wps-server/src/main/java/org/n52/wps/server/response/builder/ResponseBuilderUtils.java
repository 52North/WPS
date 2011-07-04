package org.n52.wps.server.response.builder;

import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.StatusDocumentType;

public class ResponseBuilderUtils {

	/**
	 * Fill the StatusDocument (of CancelResponse or GetStatusResponse) with the element of ExecuteResponse
	 * @param statusDom status document
	 * @param execDom execute response document
	 */
	public static void fillStatusDocument(StatusDocumentType statusDom,
			ExecuteResponseDocument execDom) {
		// TODO check what happens if null
		/**
		 * Remark (Christophe Noel, Spacebel)
		 * The reasons why the following code is so long :
		 * 1. First time I used XMLBeans, so it is maybe not optimized
		 * 2. ExecuteResponse proposed is the CR is nearly the same in 1.0.0
		 * In my opinion, a consistancy improvement would be that the ExecuteResponse include a
		 * Status Document, as it is the case for the GetStatusResponse and CancelResponse document.
		 * In that case, following code would be also much simpler.
		 */
		statusDom.setService(execDom.getExecuteResponse().getService());
		statusDom.setVersion(execDom.getExecuteResponse().getVersion());
		statusDom.setLang(execDom.getExecuteResponse().getLang());
		statusDom.setServiceInstance(execDom.getExecuteResponse().getServiceInstance());
		statusDom.setStatusLocation(execDom.getExecuteResponse().getStatusLocation());
		statusDom.addNewProcess();
		statusDom.getProcess().set(execDom.getExecuteResponse().getProcess());
		statusDom.addNewStatus();
		statusDom.getStatus().set(execDom.getExecuteResponse().getStatus());
		statusDom.addNewDataInputs();
		statusDom.getDataInputs().set(execDom.getExecuteResponse().getDataInputs());
		statusDom.addNewOutputDefinitions();
		statusDom.getOutputDefinitions().set(execDom.getExecuteResponse().getOutputDefinitions());
		statusDom.addNewProcessOutputs();
		statusDom.getProcessOutputs().set(execDom.getExecuteResponse().getProcessOutputs());
		statusDom.addNewProcessInstanceIdentifier();
		statusDom.getProcessInstanceIdentifier().set(execDom.getExecuteResponse().getProcessInstanceIdentifier());

		
	}

}
