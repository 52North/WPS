
package org.n52.wps.unicore;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.n52.wps.io.data.IData;
import org.n52.wps.server.AbstractObservableAlgorithm;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.unicore.client.UnicoreClient;

public abstract class AbstractUnicoreAlgorithm extends AbstractObservableAlgorithm implements IUnicoreAlgorithm
{
	private static Logger LOGGER = LoggerFactory.getLogger(AbstractUnicoreAlgorithm.class);

	protected IAlgorithm embeddedAlgorithm;

	public IAlgorithm getEmbeddedAlgorithm()
	{
		return embeddedAlgorithm;
	}

	public AbstractUnicoreAlgorithm(IAlgorithm pEmbeddedAlgorithm)
	{
		super();
		this.embeddedAlgorithm = pEmbeddedAlgorithm;
	}

	public AbstractUnicoreAlgorithm(String wellKnownName, IAlgorithm pEmbeddedAlgorithm)
	{
		super(wellKnownName);
		this.embeddedAlgorithm = pEmbeddedAlgorithm;
	}

	public List<String> getErrors()
	{
		return embeddedAlgorithm.getErrors();
	}

	public Class getInputDataType(String id)
	{
		return embeddedAlgorithm.getInputDataType(id);
	}

	public Class getOutputDataType(String id)
	{
		return embeddedAlgorithm.getOutputDataType(id);
	}

	public Map<String, IData> run(Map<String, List<IData>> pInputData)
	{
		try
		{

			List<Map<String, List<IData>>> inputDataList = split(pInputData);

			UnicoreClient client = new UnicoreClient();

			List<UnicoreAlgorithmOutput> outputDataList = client.perform(UnicoreAlgorithmInput.transform(inputDataList, embeddedAlgorithm.getDescription()
					.getIdentifier().getStringValue()));

			Map<String, IData> outputData = merge(UnicoreAlgorithmOutput.transform(outputDataList));

			return outputData;
		}
		catch (Exception e)
		{
			LOGGER.error(e);
			throw new RuntimeException(e);
		}
	}
}
