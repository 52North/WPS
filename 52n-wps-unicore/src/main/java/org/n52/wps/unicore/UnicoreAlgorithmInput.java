
package org.n52.wps.unicore;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.n52.wps.io.data.IData;

public class UnicoreAlgorithmInput implements Serializable
{
	protected transient Map<String, List<IData>> data;
	protected transient String embeddedAlgorithm;

	public String getEmbeddedAlgorithm()
	{
		return embeddedAlgorithm;
	}

	public UnicoreAlgorithmInput(Map<String, List<IData>> pData, String pEmbeddedAlgorithm)
	{
		data = pData;
		embeddedAlgorithm = pEmbeddedAlgorithm;
	}

	public Map<String, List<IData>> getData()
	{
		return data;
	}

	public static List<UnicoreAlgorithmInput> transform(List<Map<String, List<IData>>> pData, String pAlgorithmIdentifier)
	{
		List<UnicoreAlgorithmInput> result = new ArrayList<UnicoreAlgorithmInput>();
		for (Map<String, List<IData>> input : pData)
		{
			result.add(new UnicoreAlgorithmInput(input, pAlgorithmIdentifier));
		}
		return result;
	}

	private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException
	{
		oos.writeObject(data);
		oos.writeObject(embeddedAlgorithm);
	}

	private synchronized void readObject(java.io.ObjectInputStream oos) throws IOException, ClassNotFoundException
	{
		data = (Map<String, List<IData>>) oos.readObject();
		embeddedAlgorithm = (String) oos.readObject();
	}
}
