
package org.n52.wps.gridgain.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.gridgain.grid.GridException;
import org.gridgain.grid.GridJob;
import org.gridgain.grid.GridJobAdapter;
import org.gridgain.grid.GridJobResult;
import org.gridgain.grid.GridTaskSplitAdapter;
import org.n52.wps.gridgain.algorithm.GridGainSimpleBufferAlgorithm;
import org.n52.wps.io.data.IData;
import org.n52.wps.unicore.UnicoreAlgorithmInput;
import org.n52.wps.unicore.UnicoreAlgorithmOutput;

public class GridGainTask extends GridTaskSplitAdapter<UnicoreAlgorithmInput, UnicoreAlgorithmOutput>
{

	@Override
	protected Collection<? extends GridJob> split(int gridSize, UnicoreAlgorithmInput algorithmInput) throws GridException
	{
		// split the input data
		GridGainSimpleBufferAlgorithm algorithm = new GridGainSimpleBufferAlgorithm();
		List<Map<String, List<IData>>> inputDataList = algorithm.split(algorithmInput.getData());

		// convert data structure
		List<UnicoreAlgorithmInput> inputData = new ArrayList<UnicoreAlgorithmInput>();
		for (int i = 0; i < inputDataList.size(); i++)
		{
			inputData.add(new UnicoreAlgorithmInput(inputDataList.get(0), algorithmInput.getEmbeddedAlgorithm()));
		}

		List<GridJob> jobs = new ArrayList<GridJob>(inputData.size());
		for (UnicoreAlgorithmInput input : inputData)
		{
			jobs.add(new GridJobAdapter<UnicoreAlgorithmInput>(input)
			{
				public Serializable execute()
				{

					UnicoreAlgorithmInput input = getArgument();
					GridGainSimpleBufferAlgorithm algorithm = new GridGainSimpleBufferAlgorithm();
					Map<String, IData> result = algorithm.getEmbeddedAlgorithm().run(input.getData());
					UnicoreAlgorithmOutput output = new UnicoreAlgorithmOutput(result);
					return output;
				}
			});

		}
		return jobs;

	}

	public UnicoreAlgorithmOutput reduce(List<GridJobResult> resultList) throws GridException
	{
		// convert data structure
		List<Map<String, IData>> outputData = new ArrayList<Map<String, IData>>();

		for (GridJobResult result : resultList)
		{
			UnicoreAlgorithmOutput output = result.getData();
			outputData.add(output.getData());

		}

		// merge the output data
		GridGainSimpleBufferAlgorithm algorithm = new GridGainSimpleBufferAlgorithm();
		UnicoreAlgorithmOutput result = new UnicoreAlgorithmOutput(algorithm.merge(outputData));

		return result;

	}

}
