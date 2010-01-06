
package org.n52.wps.unicore;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.wps.io.data.IData;

public class UnicoreAlgorithmOutput implements Serializable
{
	protected transient Map<String, IData> data;

	public UnicoreAlgorithmOutput()
	{
		data = new HashMap<String, IData>();
	}

	public UnicoreAlgorithmOutput(Map<String, IData> pData)
	{
		data = pData;
	}

	public Map<String, IData> getData()
	{
		return data;
	}

	public static List<Map<String, IData>> transform(List<UnicoreAlgorithmOutput> pData)
	{
		List<Map<String, IData>> result = new ArrayList<Map<String, IData>>();
		for (UnicoreAlgorithmOutput output : pData)
		{
			result.add(output.getData());
		}
		return result;
	}

	private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException
	{
		oos.writeObject(data);
	}

	private synchronized void readObject(java.io.ObjectInputStream oos) throws IOException, ClassNotFoundException
	{
		data = (HashMap<String, IData>) oos.readObject();
	}
}
