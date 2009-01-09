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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ApplicationDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ApplicationType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdlPosix.EnvironmentType;
import org.ggf.schemas.jsdl.x2005.x11.jsdlPosix.POSIXApplicationDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdlPosix.POSIXApplicationType;
import org.n52.wps.grid.DistributedAlgorithmInput;
import org.n52.wps.grid.DistributedAlgorithmOutput;
import org.n52.wps.grid.util.CompressUtilities;
import org.n52.wps.server.ExceptionReport;
import org.unigrids.x2006.x04.services.tss.SubmitDocument;
import org.unigrids.x2006.x04.services.tss.SubmitResponseDocument;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import de.fzj.unicore.uas.client.JobClient;
import de.fzj.unicore.uas.client.RByteIOClient;
import de.fzj.unicore.uas.client.StorageClient;
import de.fzj.unicore.uas.client.TSSClient;
import de.fzj.unicore.uas.security.IUASSecurityProperties;

/**
 * @author bastian
 * 
 */
public class Unicore6Task implements Callable<Object>
{
	private static Logger LOGGER = Logger.getLogger(Unicore6Task.class);

	protected static int WAIT_UNTIL_READY_TIMEOUT = 1800 * 1000;
	protected static int WAIT_UNTIL_DONE_TIMEOUT = 1800 * 1000;
	
	public static final String TARGET_SYSTEM_INPUT_FILE_NAME = "input";
	public static final String TARGET_SYSTEM_OUTPUT_FILE_NAME = "output";

	public static String JOB_EXECUTION_SCRIPT = "Unicore6Executor.sh";

	private IUASSecurityProperties securityProperties;
	private TSSClient targetSystem;
	private final DistributedAlgorithmInput algorithmInput;
	
	/**
	 * @param pJobNumber
	 * @param pGridClient
	 * @param pGridInput
	 */
	public Unicore6Task(IUASSecurityProperties pSecurityProperties, TSSClient pTargetSystem, DistributedAlgorithmInput pAlgorithmInput)
	{
		securityProperties = pSecurityProperties;
		targetSystem = pTargetSystem;
		algorithmInput = pAlgorithmInput;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Callable#call()
	 */
	public DistributedAlgorithmOutput call() throws Exception
	{
		return run(algorithmInput);
	}
	
	/**
	 * @param pGridInput
	 * @return
	 * @throws Exception
	 */
	private DistributedAlgorithmOutput run(DistributedAlgorithmInput pGridInput) throws Exception
	{
		// submit job
		JobClient job = submitJob(targetSystem);

		// stage in input files
		StorageClient smsUspace = job.getUspaceClient();
		submitInputFiles(smsUspace, pGridInput);

		// stage in execution script
		submitExecutionScript(smsUspace);

		// run job
		LOGGER.info("Run job.");
		job.waitUntilReady(WAIT_UNTIL_READY_TIMEOUT);
		job.start();
		job.waitUntilDone(WAIT_UNTIL_DONE_TIMEOUT);

		// fetch process outcome
		DistributedAlgorithmOutput processOutput = getAlgorithmOutput(smsUspace);

		// destroy job at target system
		job.destroy();

		return processOutput;
	}
	
	/**
	 * @param tss
	 * @param tc
	 * @return
	 * @throws Exception
	 */
	private JobClient submitJob(final TSSClient tss) throws Exception
	{
		// create job definition document
		LOGGER.info("Create job definition document.");
		JobDefinitionDocument definition = getJobDefinition();

		// submit job definition document
		LOGGER.info("Submit job definition document.");
		SubmitDocument submit = SubmitDocument.Factory.newInstance();
		submit.addNewSubmit().setJobDefinition(definition.getJobDefinition());
		SubmitResponseDocument response = tss.Submit(submit);

		// create job client
		EndpointReferenceType jobEpr = response.getSubmitResponse().getJobReference();
		LOGGER.info("Create job at '" + jobEpr.getAddress().getStringValue() + "'.");
		return new JobClient(jobEpr.getAddress().getStringValue(), jobEpr, securityProperties);
	}
	
	/**
	 * @return
	 */
	private JobDefinitionDocument getJobDefinition()
	{
		JobDefinitionDocument job = JobDefinitionDocument.Factory.newInstance();

		ApplicationDocument ad = ApplicationDocument.Factory.newInstance();
		ApplicationType app = ad.addNewApplication();

		app.setApplicationName("Bash shell");

		POSIXApplicationDocument pAppD = POSIXApplicationDocument.Factory.newInstance();
		POSIXApplicationType pApp = pAppD.addNewPOSIXApplication();

		EnvironmentType e1 = pApp.addNewEnvironment();
		e1.setName("SOURCE");
		e1.setStringValue("./Unicore6Executor.sh");

		de.fzj.unicore.wsrflite.utils.Utilities.append(pAppD, ad);

		job.addNewJobDefinition().addNewJobDescription().setApplication(ad.getApplication());

		return job;
	}
	
	/**
	 * @param pSms
	 * @param pAlgorithmInput
	 * @param tc
	 * @throws Exception
	 */
	private void submitInputFiles(StorageClient pSms, DistributedAlgorithmInput pAlgorithmInput) throws Exception
	{
		// create compressed input data
		LOGGER.info("Serialize input data.");
		byte[] data = CompressUtilities.serialize(pAlgorithmInput);
		byte[] dataCompressed = CompressUtilities.createCompressedData(data);
		LOGGER.info("Compress input data (ratio: " + ((double) dataCompressed.length) / ((double) data.length) + ").");

		// submit input data
		LOGGER.info("Submit input data.");
		RByteIOClient fileClient = pSms.getImport("/" + TARGET_SYSTEM_INPUT_FILE_NAME);
		fileClient.write(dataCompressed);
	}
	
	/**
	 * @param pSms
	 * @param tc
	 * @throws Exception
	 */
	private void submitExecutionScript(StorageClient pSms) throws Exception
	{
		LOGGER.info("Submit job execution script.");
		InputStream source = Unicore6Client.class.getResourceAsStream(JOB_EXECUTION_SCRIPT);
		RByteIOClient fileClient = pSms.getImport("/" + JOB_EXECUTION_SCRIPT);
		fileClient.writeAllData(source);
	}
	
	/**
	 * @param uspace
	 * @param tc
	 * @return
	 * @throws Exception
	 */
	protected DistributedAlgorithmOutput getAlgorithmOutput(final StorageClient uspace) throws ExceptionReport
	{
		// fetch and display standard input and output
		 byte[] stdout = stageOut(uspace, "stdout");
		 byte[] stderr = stageOut(uspace, "stderr");
		
		 System.out.println(new String(stdout));
		 System.out.println(new String(stderr));

		// fetch output data
		LOGGER.info("Fetch job output data.");
		byte[] dataCompressed = stageOut(uspace, "/" + TARGET_SYSTEM_OUTPUT_FILE_NAME);

		try
		{
			// create uncompressed output data
			ByteArrayInputStream bais = new ByteArrayInputStream(dataCompressed);
			byte[] data = CompressUtilities.createUncompressedData(bais);
			LOGGER.info("Decompress output data (ratio: " + ((double) data.length) / ((double) dataCompressed.length) + ").");

			// deserialize output data
			LOGGER.info("Serialize output data.");
			bais = new ByteArrayInputStream(data);
			ObjectInputStream ois = new ObjectInputStream(bais);
			return (DistributedAlgorithmOutput) ois.readObject();
		}
		catch (IOException e)
		{
			LOGGER.error("Error while deserialization of job output data.");
			throw new ExceptionReport("Error while deserialization of job output data.", ExceptionReport.REMOTE_COMPUTATION_ERROR, e);
		}
		catch (ClassNotFoundException e)
		{
			LOGGER.error("Error while deserialization of job output data.");
			throw new ExceptionReport("Error while deserialization of job output data.", ExceptionReport.REMOTE_COMPUTATION_ERROR, e);
		}
	}
	
	/**
	 * @param uspace
	 * @param pFileName
	 * @return
	 * @throws IOException
	 * @throws Exception
	 */
	protected byte[] stageOut(final StorageClient uspace, final String pFileName) throws ExceptionReport
	{
		try
		{
			RByteIOClient fileClient = uspace.getExport(pFileName);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			fileClient.readAllData(bos);
			return bos.toByteArray();
		}
		catch (IOException e)
		{
			LOGGER.error("Error while fetching remote file '" + pFileName + "'.");
			throw new ExceptionReport("Error while fetching remote file '" + pFileName + "'.", ExceptionReport.REMOTE_COMPUTATION_ERROR, e);
		}
	}
}
