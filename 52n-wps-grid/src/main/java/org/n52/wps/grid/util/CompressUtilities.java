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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author bastian
 *
 */
public class CompressUtilities
{
	/**
	 * @param pData
	 * @return
	 * @throws IOException
	 */
	public static synchronized byte[] serialize(final Object pData) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(pData);
		oos.close();
		return baos.toByteArray();
	}
	
	/**
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public static synchronized byte[] createCompressedData(byte[] data) throws IOException
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
	
	/**
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static synchronized byte[] createUncompressedData(InputStream is) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GZIPInputStream zis = new GZIPInputStream(is);
		byte[] buffer = new byte[1024];
		for (int length; (length = zis.read(buffer, 0, 1024)) > 0;)
		{
			baos.write(buffer, 0, length);
		}
		zis.close();
		baos.close();
		return baos.toByteArray();
	}
}
