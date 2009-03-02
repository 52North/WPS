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

package org.n52.wps.grid;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.IAlgorithmRepository;
import org.n52.wps.server.IDistributedAlgorithm;

public class UnicoreAlgorithmRepository implements IAlgorithmRepository
{
	private static Logger LOGGER = Logger.getLogger(UnicoreAlgorithmRepository.class);

	public static String CFG_REGISTRY = "Registry";
	public static String CFG_KEYSTORE = "Keystore";
	public static String CFG_ALIAS = "Alias";
	public static String CFG_PASSWORD = "Password";
	public static String CFG_TYPE = "Type";
	public static String CFG_OVERWRITE = "OverwriteRemoteFile";
	public static String CFG_NODES = "MaximumNumberOfNodes";
	
	private Map<String, IAlgorithm> algorithmMap;

	private Properties unicoreProperties;
	
//	public String registry;
//	public String keystore;
//	public String alias;
//	public String password;
//	public String type;
//	public boolean overwriteRemoteFile;
//	public int maximumNumberOfNodes;

	private static UnicoreAlgorithmRepository instance;

	public UnicoreAlgorithmRepository()
	{
		algorithmMap = new HashMap<String, IAlgorithm>();
		
		unicoreProperties = new Properties();
		
		Property[] propertyArray = WPSConfig.getInstance().getPropertiesForRepositoryClass(this.getClass().getCanonicalName());

		// import UNICORE configuration
		for (Property property : propertyArray)
		{
			if (property.getName().equalsIgnoreCase(CFG_REGISTRY))
			{
				unicoreProperties.setProperty(CFG_REGISTRY, property.getStringValue());
			}
			else if (property.getName().equalsIgnoreCase(CFG_KEYSTORE))
			{
				unicoreProperties.setProperty(CFG_KEYSTORE, property.getStringValue());
			}
			else if (property.getName().equalsIgnoreCase(CFG_ALIAS))
			{
				unicoreProperties.setProperty(CFG_ALIAS, property.getStringValue());
			}
			else if (property.getName().equalsIgnoreCase(CFG_PASSWORD))
			{
				unicoreProperties.setProperty(CFG_PASSWORD, property.getStringValue());
			}
			else if (property.getName().equalsIgnoreCase(CFG_TYPE))
			{
				unicoreProperties.setProperty(CFG_TYPE, property.getStringValue());
			}
			else if (property.getName().equalsIgnoreCase(CFG_OVERWRITE))
			{
				unicoreProperties.setProperty(CFG_OVERWRITE, property.getStringValue());
			}
			else if (property.getName().equalsIgnoreCase(CFG_NODES))
			{
				unicoreProperties.setProperty(CFG_NODES, property.getStringValue());
			}
		}

		// create algorithms
		for (Property property : propertyArray)
		{
			if (property.getName().equalsIgnoreCase("Algorithm"))
			{
				addAlgorithm(property.getStringValue());
			}
		}
	}

	/**
	 * @return
	 */
	public static UnicoreAlgorithmRepository getInstance()
	{
		if (instance == null)
		{
			instance = new UnicoreAlgorithmRepository();
		}
		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.wps.server.IAlgorithmRepository#addAlgorithm(java.lang.Object)
	 */
	public boolean addAlgorithm(Object processID)
	{
		if (!(processID instanceof String))
		{
			return false;
		}

		String algorithmClassName = (String) processID;

		try
		{
			IDistributedAlgorithm algorithm = (IDistributedAlgorithm) UnicoreAlgorithmRepository.class.getClassLoader().loadClass(algorithmClassName)
					.newInstance();

			try
			{
				// Unicore6Client distributedComputingClient = new Unicore6Client(registry, keystore, alias, password, type, overwriteRemoteFile,
				// maximumNumberOfNodes);

				algorithm.setDistributedComputingClient("org.n52.wps.grid.client.unicore6.Unicore6Client", unicoreProperties);
			}
			catch (Exception e)
			{
				LOGGER.error(e.getMessage(), e);
				return false;
			}

			if (!algorithm.processDescriptionIsValid())
			{
				LOGGER.warn("Algorithm description is not valid: " + algorithmClassName);
				return false;
			}

			algorithmMap.put(algorithmClassName, algorithm);
			LOGGER.info("Algorithm class registered: " + algorithmClassName);

			if (algorithm.getWellKnownName().length() != 0)
			{
				algorithmMap.put(algorithm.getWellKnownName(), algorithm);
			}
		}
		catch (ClassNotFoundException e)
		{
			LOGGER.warn("Could not find algorithm class: " + algorithmClassName, e);
			return false;
		}
		catch (IllegalAccessException e)
		{
			LOGGER.warn("Access error occured while registering algorithm: " + algorithmClassName);
			return false;
		}
		catch (InstantiationException e)
		{
			LOGGER.warn("Could not instantiate algorithm: " + algorithmClassName);
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.wps.server.IAlgorithmRepository#containsAlgorithm(java.lang.String )
	 */
	public boolean containsAlgorithm(String processID)
	{
		return algorithmMap.containsKey(processID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.wps.server.IAlgorithmRepository#getAlgorithm(java.lang.String)
	 */
	public IAlgorithm getAlgorithm(String processID)
	{
		return algorithmMap.get(processID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.wps.server.IAlgorithmRepository#getAlgorithmNames()
	 */
	public Collection<String> getAlgorithmNames()
	{
		return algorithmMap.keySet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.wps.server.IAlgorithmRepository#getAlgorithms()
	 */
	public Collection<IAlgorithm> getAlgorithms()
	{
		return algorithmMap.values();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.wps.server.IAlgorithmRepository#removeAlgorithm(java.lang.Object)
	 */
	public boolean removeAlgorithm(Object processID)
	{
		if (!(processID instanceof String))
		{
			return false;
		}
		String className = (String) processID;
		if (algorithmMap.containsKey(className))
		{
			algorithmMap.remove(className);
			return true;
		}
		return false;
	}

}
