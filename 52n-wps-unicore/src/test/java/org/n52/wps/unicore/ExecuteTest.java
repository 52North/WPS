
package org.n52.wps.unicore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class ExecuteTest
{
	public static void main(String[] args) throws MalformedURLException, IOException
	{
		String sUrl = "http://localhost:8761/wps/WebProcessingService";
		String sXml = "ExecuteDocument.xml";

		new ExecuteTest().exampleRequest(sUrl, sXml);
	}

	private void exampleRequest(String sUrl, String sXml) throws MalformedURLException, IOException
	{
		URL url = new URL(sUrl);

		URLConnection connection = url.openConnection();

		connection.setDoInput(true);
		connection.setDoOutput(true);

		OutputStream os = connection.getOutputStream();

		InputStream is = ExecuteTest.class.getResourceAsStream(sXml);
		byte[] buffer = new byte[1024];
		int c = is.read(buffer);
		while (c > 0)
		{
			System.out.print(new String(buffer, 0, c));
			os.write(buffer, 0, c);
			c = is.read(buffer);
		}
		os.close();
		System.out.println();

		StringBuffer result = new StringBuffer();
		is = connection.getInputStream();
		buffer = new byte[1];
		c = is.read(buffer);
		while (c > 0)
		{
			result.append(new String(buffer, 0, c));
			c = is.read(buffer);
		}
		System.out.println(result.toString());
	}
}
