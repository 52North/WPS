
package org.n52.wps.unicore.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ApplicationDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ApplicationType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdlPosix.EnvironmentType;
import org.ggf.schemas.jsdl.x2005.x11.jsdlPosix.POSIXApplicationDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdlPosix.POSIXApplicationType;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.unicore.UnicoreAlgorithmInput;
import org.n52.wps.unicore.UnicoreAlgorithmOutput;
import org.n52.wps.unicore.UnicoreAlgorithmRepository;
import org.n52.wps.unicore.utilities.Compression;
import org.unigrids.x2006.x04.services.tss.SubmitDocument;
import org.unigrids.x2006.x04.services.tss.SubmitResponseDocument;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import de.fzj.unicore.uas.client.JobClient;
import de.fzj.unicore.uas.client.RByteIOClient;
import de.fzj.unicore.uas.client.StorageClient;
import de.fzj.unicore.uas.client.TSSClient;
import de.fzj.unicore.uas.security.IUASSecurityProperties;

public class UnicoreTask implements Callable<Object>
{
	private static Logger LOGGER = LoggerFactory.getLogger(UnicoreTask.class);

	public static final String TARGET_SYSTEM_INPUT_FILE_NAME = "input";
	public static final String TARGET_SYSTEM_OUTPUT_FILE_NAME = "output";

	public static String JOB_EXECUTION_SCRIPT = "UnicoreExecutor.sh";

	protected static int WAIT_UNTIL_READY_TIMEOUT = 1800 * 1000;
	protected static int WAIT_UNTIL_DONE_TIMEOUT = 1800 * 1000;

	protected IUASSecurityProperties securityProperties;
	protected TSSClient targetSystem;
	protected UnicoreAlgorithmInput inputData;

	public UnicoreTask(IUASSecurityProperties pSecurityProperties, TSSClient pTargetSystem, UnicoreAlgorithmInput pInputData)
	{
		securityProperties = pSecurityProperties;
		targetSystem = pTargetSystem;
		inputData = pInputData;
	}

	public Object call() throws Exception
	{
		// submit job
		JobClient job = submitJob(targetSystem);

		// stage in input data
		StorageClient smsUspace = job.getUspaceClient();
		submitInputData(smsUspace, inputData);

		// stage in configuration
		submitWpsConfiguration(smsUspace);

		// stage in execution script
		submitExecutionScript(smsUspace);

		// run job
		LOGGER.info("Run job.");
		job.waitUntilReady(WAIT_UNTIL_READY_TIMEOUT);
		job.start();
		job.waitUntilDone(WAIT_UNTIL_DONE_TIMEOUT);

		// fetch process outcome
		UnicoreAlgorithmOutput processOutput = fetchOutputData(smsUspace);

		// destroy job at target system
		job.destroy();

		IAlgorithm alg = new org.n52.wps.server.algorithm.SimpleBufferAlgorithm();
		return new UnicoreAlgorithmOutput(alg.run(inputData.getData()));
	}

	protected JobClient submitJob(final TSSClient tss) throws Exception
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

	protected JobDefinitionDocument getJobDefinition()
	{
		JobDefinitionDocument job = JobDefinitionDocument.Factory.newInstance();

		ApplicationDocument ad = ApplicationDocument.Factory.newInstance();
		ApplicationType app = ad.addNewApplication();

		app.setApplicationName("Bash shell");

		POSIXApplicationDocument pAppD = POSIXApplicationDocument.Factory.newInstance();
		POSIXApplicationType pApp = pAppD.addNewPOSIXApplication();

		EnvironmentType e1 = pApp.addNewEnvironment();
		e1.setName("SOURCE");
		e1.setStringValue("./UnicoreExecutor.sh");

		de.fzj.unicore.wsrflite.utils.Utilities.append(pAppD, ad);

		job.addNewJobDefinition().addNewJobDescription().setApplication(ad.getApplication());

		return job;
	}

	protected UnicoreAlgorithmOutput fetchOutputData(final StorageClient uspace) throws ExceptionReport
	{
		// fetch and display standard input and output
		byte[] stdout = stageOut(uspace, "stdout");
		byte[] stderr = stageOut(uspace, "stderr");

		LOGGER.info(new String(stdout));
		LOGGER.info(new String(stderr));

		// fetch output data
		LOGGER.info("Fetch algorithm output data.");
		byte[] dataCompressed = stageOut(uspace, "/" + TARGET_SYSTEM_OUTPUT_FILE_NAME);

		try
		{
			Properties unicoreProperties = UnicoreAlgorithmRepository.getInstance().getUnicoreProperties();

			// create uncompressed output data
			InputStream is = new ByteArrayInputStream(dataCompressed);
			byte[] data = Compression.createUncompressedData(is, Boolean
					.parseBoolean(unicoreProperties.getProperty(UnicoreAlgorithmRepository.CFG_COMPRESSION)));
			LOGGER.info("Decompress output data (ratio: " + ((double) data.length) / ((double) dataCompressed.length) + ").");

			// deserialize output data
			LOGGER.info("Serialize output data.");
			is = new ByteArrayInputStream(data);
			ObjectInputStream ois = new ObjectInputStream(is);
			return (UnicoreAlgorithmOutput) ois.readObject();
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

	private void submitExecutionScript(StorageClient pSms) throws Exception
	{
		LOGGER.info("Submit job execution script.");
		InputStream source = UnicoreClient.class.getResourceAsStream(JOB_EXECUTION_SCRIPT);
		RByteIOClient fileClient = pSms.getImport("/" + JOB_EXECUTION_SCRIPT);
		fileClient.writeAllData(source);
	}

	protected void submitInputData(StorageClient pSms, UnicoreAlgorithmInput pAlgorithmInput) throws Exception
	{
		RByteIOClient fileClient = pSms.getImport("/" + TARGET_SYSTEM_INPUT_FILE_NAME);
		// create serialized input data
		LOGGER.info("Serialize input data.");
		byte[] data = Compression.toByteArray(pAlgorithmInput);
		// create input data
		Properties unicoreProperties = UnicoreAlgorithmRepository.getInstance().getUnicoreProperties();
		byte[] dataCompressed = null;
		if (Boolean.parseBoolean(unicoreProperties.getProperty(UnicoreAlgorithmRepository.CFG_COMPRESSION)))
		{
			dataCompressed = Compression.createCompressedData(data, Boolean.parseBoolean(unicoreProperties
					.getProperty(UnicoreAlgorithmRepository.CFG_COMPRESSION)));
			LOGGER.info("Compress input data (ratio: " + ((double) dataCompressed.length) / ((double) data.length) + ").");
		}
		// submit input data
		if (Boolean.parseBoolean(unicoreProperties.getProperty(UnicoreAlgorithmRepository.CFG_COMPRESSION)))
		{
			LOGGER.info("Submit compressed input data.");
			fileClient.write(dataCompressed);
		}
		else
		{
			LOGGER.info("Submit input data.");
			fileClient.write(data);
		}
	}

	protected void submitWpsConfiguration(StorageClient pSms) throws Exception
	{
		RByteIOClient fileClient = pSms.getImport("/wps_config.xml");
		File f = new File(WPSConfig.getConfigPath());
		FileInputStream is = new FileInputStream(f);
		fileClient.writeAllData(is);
		is.close();
	}
}
