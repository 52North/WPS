
package org.n52.wps.unicore;

import java.util.List;
import java.util.Map;

import org.n52.wps.io.data.IData;
import org.n52.wps.server.IAlgorithm;

public interface IUnicoreAlgorithm extends IAlgorithm
{
	List<Map<String, List<IData>>> split(Map<String, List<IData>> inputData);

	Map<String, IData> merge(List<Map<String, IData>> outputData);
}
