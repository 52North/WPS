/*******************************************************************************
 * Copyright (C) 2008
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 * 
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 * 
 * This program is free software; you can redistribute and/or modify it under 
 * the terms of the GNU General Public License version 2 as published by the 
 * Free Software Foundation.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 * 
 * Author: Bastian Baranski (Bastian.Baranski@uni-muenster.de)
 * Created: 03.09.2008
 * Modified: 03.09.2008
 *
 ******************************************************************************/

package org.n52.wps.grid.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author bastian
 * 
 */
public class ExecuteTest 
{
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			URL url = new URL("http://localhost:8761/wps/WebProcessingService");

			URLConnection connection = url.openConnection();

			connection.setDoInput(true);
			connection.setDoOutput(true);

			OutputStream os = connection.getOutputStream();

			InputStream is = ExecuteTest.class.getResourceAsStream("executeDocument.xml");
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
			System.out.print(result.toString().substring(0, 200) + " (...)");
		}
		catch (MalformedURLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
