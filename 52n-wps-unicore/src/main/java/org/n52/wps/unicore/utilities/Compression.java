
package org.n52.wps.unicore.utilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Compression
{
	public static synchronized byte[] toByteArray(final Object pData) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(pData);
		oos.close();
		return baos.toByteArray();
	}
	
	public static synchronized byte[] createCompressedData(byte[] data, boolean compressionEnabled) throws IOException
	{
		if (compressionEnabled)
		{
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			GZIPOutputStream zos = new GZIPOutputStream(baos);
			byte[] buffer = new byte[1024];
			for (int length; (length = bais.read(buffer, 0, 1024)) > 0;)
			{
				zos.write(buffer, 0, length);
			}
			zos.close();
			baos.close();
			return baos.toByteArray();
		}
		else
		{
			return data;
		}

	}

	public static synchronized byte[] createUncompressedData(InputStream is, boolean compressionEnabled) throws IOException
	{
		if (compressionEnabled)
		{
			is = new GZIPInputStream(is);
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		for (int length; (length = is.read(buffer, 0, 1024)) > 0;)
		{
			baos.write(buffer, 0, length);
		}
		is.close();
		baos.close();
		return baos.toByteArray();
	}
}
