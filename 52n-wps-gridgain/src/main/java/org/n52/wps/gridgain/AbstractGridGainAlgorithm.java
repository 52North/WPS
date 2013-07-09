
package org.n52.wps.gridgain;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.gridgain.grid.Grid;
import org.gridgain.grid.GridException;
import org.gridgain.grid.GridFactory;
import org.gridgain.grid.GridTaskFuture;
import org.n52.wps.gridgain.client.GridGainTask;
import org.n52.wps.io.data.IData;
import org.n52.wps.server.AbstractObservableAlgorithm;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.unicore.UnicoreAlgorithmInput;
import org.n52.wps.unicore.UnicoreAlgorithmOutput;

public abstract class AbstractGridGainAlgorithm extends AbstractObservableAlgorithm implements IGridGainAlgorithm
{
	private static Logger LOGGER = LoggerFactory.getLogger(AbstractGridGainAlgorithm.class);
	
	protected IAlgorithm embeddedAlgorithm;

	public IAlgorithm getEmbeddedAlgorithm()
	{
		return embeddedAlgorithm;
	}

	public AbstractGridGainAlgorithm(IAlgorithm pEmbeddedAlgorithm)
	{
		super();
		this.embeddedAlgorithm = pEmbeddedAlgorithm;
	}

	public AbstractGridGainAlgorithm(String wellKnownName, IAlgorithm pEmbeddedAlgorithm)
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
		UnicoreAlgorithmOutput algorithmOutput;
		
		try
		{
			UnicoreAlgorithmInput algorithmInput = new UnicoreAlgorithmInput(pInputData, embeddedAlgorithm.getDescription().getIdentifier().getStringValue());
			
			GridFactory.start();
			Grid grid = GridFactory.getGrid();
			GridTaskFuture<UnicoreAlgorithmOutput> future = grid.execute(GridGainTask.class, algorithmInput);
			
			algorithmOutput = future.get();
		}
		catch (GridException e)
		{
			LOGGER.error(e);
			throw new RuntimeException(e);
		}
		finally
		{
			GridFactory.stop(true);
		}

		return algorithmOutput.getData();
	}
}
