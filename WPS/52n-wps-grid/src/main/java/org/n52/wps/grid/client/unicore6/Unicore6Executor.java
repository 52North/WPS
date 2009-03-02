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

package org.n52.wps.grid.client.unicore6;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import org.apache.xmlbeans.XmlException;
import org.n52.wps.grid.DistributedAlgorithmInput;
import org.n52.wps.grid.DistributedAlgorithmOutput;
import org.n52.wps.grid.util.CompressUtilities;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.IDistributedAlgorithm.WebProcessingServiceOutput;

/**
 * @author bastian
 * 
 */
public class Unicore6Executor
{
	public static String TARGET_SYSTEM_INPUT_FILE_NAME = "input";
	public static String TARGET_SYSTEM_OUTPUT_FILE_NAME = "output";

	public static String METHOD_NAME = "run";
	public static Class<?>[] METHOD_PARAMETER = { Map.class };

	private String userDirectory;

	/**
	 * @throws XmlException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Unicore6Executor() throws IOException, ClassNotFoundException
	{
		userDirectory = System.getProperty("user.dir");

		Properties sysprops = System.getProperties();
		Enumeration propnames = sysprops.propertyNames();

		while (propnames.hasMoreElements())
		{
			String propname = (String) propnames.nextElement();
			// System.out.println(propname + "=" + System.getProperty(propname));
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			Unicore6Executor executor = new Unicore6Executor();

			// deserialize input data and process identifier
			System.out.println("Read job input data.");
			DistributedAlgorithmInput algorithmInput = executor.getAlgorithmInput();

			// load dynamic class
			System.out.println("Load dynamic class <" + algorithmInput.getProcessDescription().getIdentifier().getStringValue() + ">.");

			Class<?> algorithmClass = Unicore6Executor.class.getClassLoader()
					.loadClass(algorithmInput.getProcessDescription().getIdentifier().getStringValue());

			System.out.println("Create new instance of algorithm.");
			Object algorithmObject = algorithmClass.newInstance();

			System.out.println("Get run method.");
			Method algorithmMethod = algorithmClass.getMethod(METHOD_NAME, METHOD_PARAMETER);

			System.out.println("Create input data structure.");
			Object[] algorithmArguments = { algorithmInput.getInput().getInputData() };

			System.out.println("Invoke run method.");
			Map data = (Map) algorithmMethod.invoke(algorithmObject, algorithmArguments);
			WebProcessingServiceOutput processOutput = new WebProcessingServiceOutput(data);

			System.out.println("Create output data structure.");
			DistributedAlgorithmOutput algorithmOutput = new DistributedAlgorithmOutput(processOutput, algorithmInput.getProcessDescription(), algorithmInput
					.getExecuteDocument(), algorithmInput.getApplicationFiles(), algorithmInput.getConfig());

			System.out.println("Write job output data.");
			executor.putSimpleJobOutput(algorithmOutput);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	protected DistributedAlgorithmInput getAlgorithmInput() throws ExceptionReport
	{
		try
		{
			FileInputStream fis = new FileInputStream(getUserDirectory() + "/" + TARGET_SYSTEM_INPUT_FILE_NAME);
			System.out.println(getUserDirectory() + "/" + TARGET_SYSTEM_INPUT_FILE_NAME);
			byte[] data = CompressUtilities.createUncompressedData(fis);
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			ObjectInputStream ois = new ObjectInputStream(bais);
			DistributedAlgorithmInput algorithmInput = (DistributedAlgorithmInput) ois.readObject();
			return algorithmInput;
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			System.out.println("Error while accessing serialized algorithm input data file.");
			throw new ExceptionReport("Error while accessing serialized algorithm input data file.", ExceptionReport.REMOTE_COMPUTATION_ERROR, e);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.out.println("Error while accessing serialized algorithm input data file.");
			throw new ExceptionReport("Error while accessing serialized algorithm input data file.", ExceptionReport.REMOTE_COMPUTATION_ERROR, e);
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
			System.out.println("Error while accessing serialized algorithm input data file.");
			throw new ExceptionReport("Error while accessing serialized algorithm input data file.", ExceptionReport.REMOTE_COMPUTATION_ERROR, e);
		}
	}

	/**
	 * @param pAlgorithmOutput
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected void putSimpleJobOutput(final DistributedAlgorithmOutput pAlgorithmOutput) throws FileNotFoundException, IOException
	{
		byte[] data = CompressUtilities.serialize(pAlgorithmOutput);
		byte[] dataCompressed = CompressUtilities.createCompressedData(data);
		FileOutputStream fos = new FileOutputStream(getUserDirectory() + "/" + TARGET_SYSTEM_OUTPUT_FILE_NAME);
		fos.write(dataCompressed);
		fos.close();
	}

	/**
	 * @return
	 */
	protected String getUserDirectory()
	{
		return userDirectory;
	}
}
