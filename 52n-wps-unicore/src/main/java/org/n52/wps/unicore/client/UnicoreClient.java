
package org.n52.wps.unicore.client;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.unicore.UnicoreAlgorithmInput;
import org.n52.wps.unicore.UnicoreAlgorithmOutput;
import org.n52.wps.unicore.UnicoreAlgorithmRepository;
import org.oasisOpen.docs.wsrf.rl2.TerminationTimeDocument.TerminationTime;
import org.unigrids.services.atomic.types.GridFileType;
import org.unigrids.services.atomic.types.ProtocolType;
import org.unigrids.x2006.x04.services.sms.ImportFileDocument;
import org.unigrids.x2006.x04.services.sms.ImportFileResponseDocument;
import org.unigrids.x2006.x04.services.tsf.CreateTSRDocument;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import de.fzj.unicore.uas.TargetSystemFactory;
import de.fzj.unicore.uas.client.RByteIOClient;
import de.fzj.unicore.uas.client.RegistryClient;
import de.fzj.unicore.uas.client.StorageClient;
import de.fzj.unicore.uas.client.TSFClient;
import de.fzj.unicore.uas.client.TSSClient;
import de.fzj.unicore.uas.security.DSigOutHandler;
import de.fzj.unicore.uas.security.IUASSecurityProperties;
import de.fzj.unicore.uas.security.TDOutHandler;
import de.fzj.unicore.uas.security.UASSecurityProperties;
import de.fzj.unicore.wsrflite.xmlbeans.BaseFault;

public class UnicoreClient
{
	private static Logger LOGGER = LoggerFactory.getLogger(UnicoreClient.class);

	protected IUASSecurityProperties securityProperties;

	public UnicoreClient()
	{
		Properties unicoreProperties = UnicoreAlgorithmRepository.getInstance().getUnicoreProperties();
		securityProperties = createSecurityProperties(unicoreProperties.getProperty(UnicoreAlgorithmRepository.CFG_KEYSTORE), unicoreProperties
				.getProperty(UnicoreAlgorithmRepository.CFG_ALIAS), unicoreProperties.getProperty(UnicoreAlgorithmRepository.CFG_PASSWORD), unicoreProperties
				.getProperty(UnicoreAlgorithmRepository.CFG_TYPE));
	}

	public List<UnicoreAlgorithmOutput> perform(List<UnicoreAlgorithmInput> pInputDataList) throws Exception
	{
		// create a target system
		TSSClient targetSystem = createTargetSystem();

		// submit application files
		submitApplicationFiles(securityProperties, targetSystem);

		// create task list
		Collection<Callable<Object>> taskList = new ArrayList<Callable<Object>>();
		for (UnicoreAlgorithmInput input : pInputDataList)
		{
			taskList.add(new UnicoreTask(securityProperties, targetSystem, input));
		}

		// start task execution
		ExecutorService executor = Executors.newCachedThreadPool();
		List<Future<Object>> resultList;
		try
		{
			resultList = executor.invokeAll(taskList);
		}
		catch (InterruptedException e)
		{
			LOGGER.error(e);
			throw new RuntimeException("Error .", e);
		}

		// fetch output of tasks
		List<UnicoreAlgorithmOutput> outputDataList = new ArrayList<UnicoreAlgorithmOutput>();

		for (Future<Object> result : resultList)
		{
			UnicoreAlgorithmOutput output;
			try
			{
				output = (UnicoreAlgorithmOutput) result.get();
			}
			catch (InterruptedException e)
			{
				LOGGER.debug("InterruptedException:" + e.getMessage());
				throw new RuntimeException("Error .", e);
			}
			catch (ExecutionException e)
			{
				LOGGER.debug("ExecutionException:" + e.getMessage());
				throw new RuntimeException("Error .", e);
			}
			outputDataList.add(output);
		}

		// destroy remote target system
		try
		{
			LOGGER.info("Destroy Target System Service (TSS) at '" + targetSystem.getEPR().getAddress().getStringValue() + "'.");
			targetSystem.destroy();
		}
		catch (Exception e)
		{
			LOGGER.error("Unable to destroy Target System Service (TSS).", e);
			throw new RuntimeException("Unable to destroy Target System Service (TSS).", e);
		}

		return outputDataList;
	}

	protected IUASSecurityProperties createSecurityProperties(String pKeystore, String pAlias, String pPassword, String pType)
	{
		UASSecurityProperties secProps = new UASSecurityProperties();

		secProps.setProperty(IUASSecurityProperties.WSRF_SSL_KEYSTORE, pKeystore);
		secProps.setProperty(IUASSecurityProperties.WSRF_SSL_KEYPASS, pPassword);
		secProps.setProperty(IUASSecurityProperties.WSRF_SSL_KEYTYPE, pType);
		secProps.setProperty(IUASSecurityProperties.WSRF_SSL_KEYALIAS, pAlias);
		secProps.setProperty(IUASSecurityProperties.WSRF_SSL_TRUSTSTORE, pKeystore);
		secProps.setProperty(IUASSecurityProperties.WSRF_SSL_TRUSTPASS, pPassword);
		secProps.setProperty(IUASSecurityProperties.WSRF_SSL_TRUSTTYPE, pType);

		secProps.setProperty(IUASSecurityProperties.WSRF_SSL, "true");
		secProps.setProperty(IUASSecurityProperties.WSRF_SSL_CLIENTAUTH, "true");

		String outHandlers = DSigOutHandler.class.getName() + " " + TDOutHandler.class.getName();
		secProps.setProperty(IUASSecurityProperties.UAS_OUTHANDLER_NAME, outHandlers);

		secProps.setSignMessage(true);
		secProps.setAddTrustDelegation(true);

		return secProps;
	}

	private TSSClient createTargetSystem()
	{
		Random random = new Random();

		RegistryClient registry;
		try
		{
			Properties unicoreProperties = UnicoreAlgorithmRepository.getInstance().getUnicoreProperties();

			// connect to registry
			EndpointReferenceType registryEpr = EndpointReferenceType.Factory.newInstance();
			registryEpr.addNewAddress().setStringValue(unicoreProperties.getProperty(UnicoreAlgorithmRepository.CFG_REGISTRY));
			LOGGER.info("Connect to UNCIORE Registry Service at '" + registryEpr.getAddress().getStringValue() + "'.");
			registry = new RegistryClient(registryEpr.getAddress().getStringValue(), registryEpr, securityProperties);
		}
		catch (Exception e)
		{
			LOGGER.error("Unable to connect to UNCIORE Registry Service.");
			throw new RuntimeException(e);
		}

		// get target system factory
		TSFClient tsf;
		try
		{
			List<EndpointReferenceType> tsfList = registry.listServices(TargetSystemFactory.TSF_PORT);
			for (EndpointReferenceType tsfEpr : tsfList)
			{
				LOGGER.info("Found Target System Factory (TSF) at '" + tsfEpr.getAddress().getStringValue() + "'.");
			}
			EndpointReferenceType tsfEpr = tsfList.get(random.nextInt(tsfList.size()));
			LOGGER.info("Select Target System Factory (TSF)  at '" + tsfEpr.getAddress().getStringValue() + "'.");
			tsf = new TSFClient(tsfEpr.getAddress().getStringValue(), tsfEpr, securityProperties);
		}
		catch (Exception e)
		{
			LOGGER.error("No Target System Factory (TSF) found.");
			throw new RuntimeException(e);
		}

		// select target system
		List<EndpointReferenceType> tssList = tsf.getTargetSystems();
		for (EndpointReferenceType tssEpr : tssList)
		{
			LOGGER.info("Found Target System Service (TSS) at '" + tssEpr.getAddress().getStringValue() + "'.");
		}

		TSSClient tss;
		try
		{
			// create target system
			CreateTSRDocument in = getCreateDocument();
			tss = tsf.createTSS(in);
			LOGGER.info("Create Target System Service (TSS) at '" + tss.getEPR().getAddress().getStringValue() + "'.");
		}
		catch (Exception e)
		{
			LOGGER.error("Unable to create Target System Service (TSS).");
			throw new RuntimeException(e);
		}

		return tss;
	}

	protected void submitApplicationFiles(IUASSecurityProperties pSecurityProperties, TSSClient pTargetSystem) throws Exception
	{
		Properties unicoreProperties = UnicoreAlgorithmRepository.getInstance().getUnicoreProperties();

		// create storage client for home folder
		StorageClient smsHome = getHomeStorageClient(pTargetSystem, pSecurityProperties);
			
		// check if remote application file already exist
		boolean overwriteRemoteFile = Boolean.parseBoolean(unicoreProperties.getProperty(UnicoreAlgorithmRepository.CFG_OVERWRITE));

		if (overwriteRemoteFile)
		{
			LOGGER.info("Overwrite existing remote application files.");
		}
		else
		{
			LOGGER.info("Do not overwrite existing remote application files.");
		}
	
	
		// check if lib directory exists
		GridFileType[] directoryList;
		try
		{
			directoryList = smsHome.listDirectory(".");
		}
		catch (BaseFault e)
		{
			LOGGER.error("Error while accessing home storage.");
			throw new ExceptionReport("Error while accessing home storage.", ExceptionReport.REMOTE_COMPUTATION_ERROR, e);
		}
		
		boolean libDirectoryExists = false;
		for (GridFileType gridFile : directoryList)
		{
			String path = gridFile.getPath();
			path = path.replaceAll("./", "");
			if (path.equals("/lib"))
			{
				libDirectoryExists = true;
				break;
			}
		}
		if (!libDirectoryExists)
		{
			smsHome.createDirectory("./lib");
		}

		// copy
		try
		{
			directoryList = smsHome.listDirectory("./lib");
		}
		catch (BaseFault e)
		{
			LOGGER.error("Error while accessing home storage.");
			throw new ExceptionReport("Error while accessing home storage.", ExceptionReport.REMOTE_COMPUTATION_ERROR, e);
		}
		
		for (String applicationFile : getApplicationFiles())
		{
			/* check if remote file exists */
			boolean remoteFileExist = false;
			for (GridFileType entry : directoryList)
			{
				String path = entry.getPath();
				path = path.replaceAll("/./lib/", "");
				if (path.equalsIgnoreCase(applicationFile))
				{
					remoteFileExist = true;
					break;
				}
			}

			File sourceFile = new File(getLibraryPath() + "/" + applicationFile);

			/* submit file */
			if (!remoteFileExist || overwriteRemoteFile)
			{
				FileInputStream source = new FileInputStream(sourceFile);

				LOGGER.info("Copy application file '" + applicationFile + "' to target system.");

				ImportFileDocument out = ImportFileDocument.Factory.newInstance();
				out.addNewImportFile().setProtocol(ProtocolType.RBYTEIO);
				out.getImportFile().setDestination("./lib/" + applicationFile);
				ImportFileResponseDocument outDoc = smsHome.ImportFile(out);
				EndpointReferenceType outEpr = outDoc.getImportFileResponse().getImportEPR();
				RByteIOClient fileClient = new RByteIOClient(outEpr.getAddress().getStringValue(), outEpr, pSecurityProperties);
				fileClient.writeAllData(source);
			}
			else
			{
				LOGGER.info("Latest version of application file '" + applicationFile + "' already on target system.");
			}

		}
	}

	private List<String> getApplicationFiles()
	{
		List<String> applicationFiles = new ArrayList<String>();

		File directory = new File(getLibraryPath());
		for (String file : directory.list())
		{
			if (file.endsWith(".jar"))
			{
				applicationFiles.add(file);
			}
		}
		
		return applicationFiles;
	}

	private CreateTSRDocument getCreateDocument()
	{
		CreateTSRDocument in = CreateTSRDocument.Factory.newInstance();
		TerminationTime tt = TerminationTime.Factory.newInstance();
		tt.setCalendarValue(getTerminationTime());
		in.addNewCreateTSR().setTerminationTime(tt);
		return in;
	}

	private Calendar getTerminationTime()
	{
		int initialLifeTime = 1;
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, initialLifeTime);
		return c;
	}

	private StorageClient getHomeStorageClient(TSSClient tss, IUASSecurityProperties pSecurityProperties)
	{
		List<EndpointReferenceType> storageList = tss.getStorages();
		if (storageList.size() < 1)
		{
			LOGGER.error("No home storage found at target system '" + tss.getTargetSystemName() + "'.");
			throw new RuntimeException("No home storage found at target system '" + tss.getTargetSystemName() + "'.");
		}
		if (storageList.size() > 1)
		{
			LOGGER.error("More than one home storage found at target system '" + tss.getTargetSystemName() + "'.");
			throw new RuntimeException("More than one home storage found at target system '" + tss.getTargetSystemName() + "'.");
		}
		EndpointReferenceType smsEpr = storageList.get(0);
		LOGGER.info("Create Storage Management Service (SMS) at '" + smsEpr.getAddress().getStringValue() + "'.");
		StorageClient client;
		try
		{
			client = new StorageClient(smsEpr.getAddress().getStringValue(), smsEpr, pSecurityProperties);
		}
		catch (Exception e)
		{
			LOGGER.error("Error during creation of Storage Management Service (SMS).");
			throw new RuntimeException("Error during creation of Storage Management Service (SMS).");
		}

		return client;
	}

	private String getLibraryPath()
	{
		String domain = UnicoreClient.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		int index = domain.indexOf("WEB-INF");
		if (index < 0)
		{
			throw new RuntimeException("Unable to find the 'WEB-INF' folder.");
		}
		String substring = domain.substring(0, index);
		if (!substring.endsWith("/"))
		{
			substring = substring + "/";
		}
		return substring + "WEB-INF/lib";
	}
}
