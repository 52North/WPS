package org.n52.wps.server;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.opengis.wps.x100.ExecuteDocument;

import org.n52.wps.io.data.IData;

/**
 * @author bastian
 * 
 */
public interface IDistributedAlgorithm extends IAlgorithm
{
	void setDistributedComputingClient(String pUncioreClientClassName, Properties pProperties);

	WebProcessingServiceOutput run(ExecuteDocument pExecuteDocument) throws Exception;

	List<WebProcessingServiceInput> split(WebProcessingServiceInput pInput, int pMaximumNumberOfNodes);

	public WebProcessingServiceOutput merge(List<WebProcessingServiceOutput> pOutput);

	public class WebProcessingServiceInput
	{
		public Map<String, List<IData>> inputData;

		public WebProcessingServiceInput(Map<String, List<IData>> pInputData)
		{
			inputData = pInputData;
		}

		public Map<String, List<IData>> getInputData()
		{
			return inputData;
		}
	}

	public class WebProcessingServiceOutput
	{
		public Map<String, IData> outputData;

		public WebProcessingServiceOutput(Map<String, IData> pOutputData)
		{
			outputData = pOutputData;
		}

		public Map<String, IData> getOutputData()
		{
			return outputData;
		}
	}
}
