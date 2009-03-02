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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

import org.apache.log4j.Logger;
import org.n52.wps.grid.DistributedAlgorithmInput;
import org.n52.wps.grid.DistributedAlgorithmOutput;
import org.n52.wps.grid.IDistributedComputingClient;
import org.n52.wps.grid.UnicoreAlgorithmRepository;
import org.n52.wps.server.ExceptionReport;
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

public class Unicore6Client implements IDistributedComputingClient
{
	private static Logger LOGGER = Logger.getLogger(Unicore6Client.class);

	public String registry;
	public String keystore;
	public String alias;
	public String password;
	public String type;
	public boolean overwriteRemoteFile;
	public int maximumNumberOfNodes;

	public Unicore6Client()
	{

	}

	/**
	 * @throws Exception
	 */
	public void setConfiguration(Properties pProperties)
	{
		registry = pProperties.getProperty(UnicoreAlgorithmRepository.CFG_REGISTRY);
		keystore = pProperties.getProperty(UnicoreAlgorithmRepository.CFG_KEYSTORE);
		alias = pProperties.getProperty(UnicoreAlgorithmRepository.CFG_ALIAS);
		password = pProperties.getProperty(UnicoreAlgorithmRepository.CFG_PASSWORD);
		type = pProperties.getProperty(UnicoreAlgorithmRepository.CFG_TYPE);
		overwriteRemoteFile = Boolean.parseBoolean(pProperties.getProperty(UnicoreAlgorithmRepository.CFG_OVERWRITE));
		maximumNumberOfNodes = Integer.parseInt(pProperties.getProperty(UnicoreAlgorithmRepository.CFG_NODES));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.wps.grid.IDistributedComputingClient#getMaximumNumberOfNodes()
	 */
	public int getMaximumNumberOfNodes()
	{
		return maximumNumberOfNodes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.wps.grid.IDistributedComputingClient#run(java.util.List)
	 */
	public List<DistributedAlgorithmOutput> run(List<DistributedAlgorithmInput> pInputList) throws Exception
	{
		// create security properties
		IUASSecurityProperties securityProperties = createSecurityProperties(keystore, alias, password, type);

		// create target system
		TSSClient targetSystem = createTargetSystem(securityProperties);

		String algorithmIdentifier = pInputList.get(0).getExecuteDocument().getExecute().getIdentifier().getStringValue();

		// stage in application files
		submitApplicationFiles(targetSystem, securityProperties, algorithmIdentifier, pInputList.get(0).getApplicationFiles());

		// create task list
		Collection<Callable<Object>> taskList = new ArrayList<Callable<Object>>();
		for (DistributedAlgorithmInput algorithmInput : pInputList)
		{
			Unicore6Task task = new Unicore6Task(securityProperties, targetSystem, algorithmInput);
			taskList.add(task);
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
			e.printStackTrace();
			LOGGER.debug(e);
			throw new RuntimeException("Error while distributed executing of process '" + algorithmIdentifier + "'.", e);
		}

		// fetch output of tasks
		List<DistributedAlgorithmOutput> algorithmOutputList = new ArrayList<DistributedAlgorithmOutput>();
		for (Future<Object> result : resultList)
		{
			DistributedAlgorithmOutput output;
			try
			{
				output = (DistributedAlgorithmOutput) result.get();
			}
			catch (InterruptedException e)
			{
				LOGGER.debug("InterruptedException:" + e.getMessage());
				throw new RuntimeException("Error while distributed executing of process '" + algorithmIdentifier + "'.", e);
			}
			catch (ExecutionException e)
			{
				LOGGER.debug("ExecutionException:" + e.getMessage());
				throw new RuntimeException("Error while distributed executing of process '" + algorithmIdentifier + "'.", e);
			}
			algorithmOutputList.add(output);
		}

		// destroy remote target system
		LOGGER.info("Destroy Target System Service (TSS) at '" + targetSystem.getEPR().getAddress().getStringValue() + "'.");
		targetSystem.destroy();

		return algorithmOutputList;
	}

	/**
	 * @return
	 */
	private IUASSecurityProperties createSecurityProperties(String pKeystore, String pAlias, String pPassword, String pType)
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

	/**
	 * @return
	 * @throws Exception
	 */
	private TSSClient createTargetSystem(IUASSecurityProperties pSecurityProperties) throws Exception
	{
		Random random = new Random();

		// connect to registry
		EndpointReferenceType registryEpr = EndpointReferenceType.Factory.newInstance();
		registryEpr.addNewAddress().setStringValue(registry);
		LOGGER.info("Connect UNCIORE Registry Service at '" + registryEpr.getAddress().getStringValue() + "'.");
		RegistryClient registry = new RegistryClient(registryEpr.getAddress().getStringValue(), registryEpr, pSecurityProperties);

		// get target system factory
		List<EndpointReferenceType> tsfList = registry.listServices(TargetSystemFactory.TSF_PORT);
		for (EndpointReferenceType tsfEpr : tsfList)
		{
			LOGGER.info("Found Target System Factory (TSF) at '" + tsfEpr.getAddress().getStringValue() + "'.");
		}
		EndpointReferenceType tsfEpr = tsfList.get(random.nextInt(tsfList.size()));
		LOGGER.info("Select Target System Factory (TSF)  at '" + tsfEpr.getAddress().getStringValue() + "'.");
		TSFClient tsf = new TSFClient(tsfEpr.getAddress().getStringValue(), tsfEpr, pSecurityProperties);

		// select target system
		List<EndpointReferenceType> tssList = tsf.getTargetSystems();
		for (EndpointReferenceType tssEpr : tssList)
		{
			LOGGER.info("Found Target System Service (TSS) at '" + tssEpr.getAddress().getStringValue() + "'.");
		}

		// create target system
		CreateTSRDocument in = getCreateDocument();
		TSSClient tss = tsf.createTSS(in);
		LOGGER.info("Create Target System Service (TSS) at '" + tss.getEPR().getAddress().getStringValue() + "'.");

		return tss;
	}

	/**
	 * @return
	 */
	private CreateTSRDocument getCreateDocument()
	{
		CreateTSRDocument in = CreateTSRDocument.Factory.newInstance();
		TerminationTime tt = TerminationTime.Factory.newInstance();
		tt.setCalendarValue(getTerminationTime());
		in.addNewCreateTSR().setTerminationTime(tt);
		return in;
	}

	/**
	 * @return
	 */
	private Calendar getTerminationTime()
	{
		int initialLifeTime = 1;
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, initialLifeTime);
		return c;
	}

	/**
	 * @param pApplicationFiles
	 * @throws Exception
	 */
	private synchronized void submitApplicationFiles(TSSClient pTargetSystem, IUASSecurityProperties pSecurityProperties, String pAlgortihmIdentifier,
			List<String> pApplicationFiles) throws ExceptionReport
	{
		// TODO remove synchronized tag
		
		// create storage client for home folder
		StorageClient smsHome = getHomeStorageClient(pTargetSystem, pSecurityProperties);

		// check if remote application file already exist
		if (overwriteRemoteFile)
		{
			LOGGER.info("Overwrite existing remote application files.");
		}
		else
		{
			LOGGER.info("Do not overwrite existing remote application files.");
		}

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

		for (String applicationFile : pApplicationFiles)
		{
			/* check if remote file exists */
			boolean remoteFileExist = false;
			for (GridFileType entry : directoryList)
			{
				String path = entry.getPath();
				path = path.replaceAll("./", "");
				if (path.equalsIgnoreCase("/" + applicationFile))
				{
					remoteFileExist = true;
					break;
				}
			}

			try
			{
				File sourceFile = new File(getLibraryPath() + applicationFile);

				/* submit file */
				if (!remoteFileExist || overwriteRemoteFile)
				{
					FileInputStream source = new FileInputStream(sourceFile);

					LOGGER.info("Copy application file '" + applicationFile + "' to target system.");

					ImportFileDocument out = ImportFileDocument.Factory.newInstance();
					out.addNewImportFile().setProtocol(ProtocolType.RBYTEIO);
					out.getImportFile().setDestination("./" + applicationFile);
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
			catch (FileNotFoundException e)
			{
				LOGGER.error("Error while submitting application files to home storage.");
				throw new ExceptionReport("Error while submitting application files to home storage.", ExceptionReport.REMOTE_COMPUTATION_ERROR, e);
			}
			catch (BaseFault e)
			{
				LOGGER.error("Error while submitting application files to home storage.");
				throw new ExceptionReport("Error while submitting application files to home storage.", ExceptionReport.REMOTE_COMPUTATION_ERROR, e);
			}
			catch (IOException e)
			{
				LOGGER.error("Error while submitting application files to home storage.");
				throw new ExceptionReport("Error while submitting application files to home storage.", ExceptionReport.REMOTE_COMPUTATION_ERROR, e);
			}
			catch (Exception e)
			{
				LOGGER.error("Error while submitting application files to home storage.");
				throw new ExceptionReport("Error while submitting application files to home storage.", ExceptionReport.REMOTE_COMPUTATION_ERROR, e);
			}
		}
	}

	/**
	 * @param tss
	 * @return
	 * @throws Exception
	 */
	private StorageClient getHomeStorageClient(TSSClient tss, IUASSecurityProperties pSecurityProperties) throws ExceptionReport
	{
		try
		{
			List<EndpointReferenceType> storageList = tss.getStorages();
			if (storageList.size() < 1)
			{
				LOGGER.error("No home storage found at target system '" + tss.getTargetSystemName() + "'.");
				throw new Exception("No home storage found at target system '" + tss.getTargetSystemName() + "'.");
			}
			if (storageList.size() > 1)
			{
				LOGGER.warn("More than one home storage found at target system '" + tss.getTargetSystemName() + "'.");
			}
			EndpointReferenceType smsEpr = storageList.get(0);
			LOGGER.info("Select Storage Management Service (SMS) at '" + smsEpr.getAddress().getStringValue() + "'.");
			StorageClient client = new StorageClient(smsEpr.getAddress().getStringValue(), smsEpr, pSecurityProperties);
			return client;
		}
		catch (Exception e)
		{
			LOGGER.error("Error while accessing home storage.");
			throw new ExceptionReport("Error while accessing home storage.", ExceptionReport.REMOTE_COMPUTATION_ERROR, e);
		}
	}

	/**
	 * @return
	 * @throws IOException
	 */
	private String getLibraryPath() throws IOException
	{
		String domain = Unicore6Client.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		int index = domain.indexOf("WEB-INF");
		if (index < 0)
		{
			throw new IOException("Error while reading WPS configuration file.");
		}
		String substring = domain.substring(0, index);
		if (!substring.endsWith("/"))
		{
			substring = substring + "/";
		}
		substring = substring + "WEB-INF/lib/";
		return substring;
	}
}
