
package org.n52.wps.unicore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.slf4j.Logger;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.IAlgorithmRepository;
import org.n52.wps.server.request.ExecuteRequest;

public class UnicoreAlgorithmRepository implements IAlgorithmRepository
{
	private static Logger LOGGER = LoggerFactory.getLogger(UnicoreAlgorithmRepository.class);

	public static String CFG_REGISTRY = "Registry";
	public static String CFG_KEYSTORE = "Keystore";
	public static String CFG_ALIAS = "Alias";
	public static String CFG_PASSWORD = "Password";
	public static String CFG_TYPE = "Type";
	
	public static String CFG_OVERWRITE = "OverwriteRemoteFile";
	public static String CFG_COMPRESSION = "CompressInputData";

	private static UnicoreAlgorithmRepository instance;

	//private Map<String, IAlgorithm> algorithmMap;
	private Properties unicoreProperties;
	protected Map<String, ProcessDescriptionType> processDescriptionMap;

	public UnicoreAlgorithmRepository()
	{
		processDescriptionMap = new HashMap<String, ProcessDescriptionType>();

		if(WPSConfig.getInstance().isRepositoryActive(this.getClass().getCanonicalName())){
			Property[] propertyArray = WPSConfig.getInstance().getPropertiesForRepositoryClass(this.getClass().getCanonicalName());
			unicoreProperties = createUnicoreProperties(propertyArray);
			for(Property property : propertyArray){
				if(property.getName().equalsIgnoreCase("Algorithm") && property.getActive()){
					addAlgorithm(property.getStringValue());
				}
			}
		} else {
			LOGGER.debug("Local Algorithm Repository is inactive.");
		}
	}

	public UnicoreAlgorithmRepository(String wpsConfigPath)
	{
		processDescriptionMap = new HashMap<String, ProcessDescriptionType>();

		Property[] propertyArray = WPSConfig.getInstance(wpsConfigPath).getPropertiesForRepositoryClass(this.getClass().getCanonicalName());

		unicoreProperties = createUnicoreProperties(propertyArray);

		for (Property property : propertyArray)
		{
			if (property.getName().equalsIgnoreCase("Algorithm"))
			{
				addAlgorithm(property.getStringValue());
			}
		}
	}

	private Properties createUnicoreProperties(Property[] propertyArray)
	{
		Properties result = new Properties();

		for (Property property : propertyArray)
		{
			if (property.getName().equalsIgnoreCase(CFG_REGISTRY))
			{
				result.setProperty(CFG_REGISTRY, property.getStringValue());
			}
			else if (property.getName().equalsIgnoreCase(CFG_KEYSTORE))
			{
				result.setProperty(CFG_KEYSTORE, property.getStringValue());
			}
			else if (property.getName().equalsIgnoreCase(CFG_ALIAS))
			{
				result.setProperty(CFG_ALIAS, property.getStringValue());
			}
			else if (property.getName().equalsIgnoreCase(CFG_PASSWORD))
			{
				result.setProperty(CFG_PASSWORD, property.getStringValue());
			}
			else if (property.getName().equalsIgnoreCase(CFG_TYPE))
			{
				result.setProperty(CFG_TYPE, property.getStringValue());
			}
			else if (property.getName().equalsIgnoreCase(CFG_OVERWRITE))
			{
				result.setProperty(CFG_OVERWRITE, property.getStringValue());
			}
			else if (property.getName().equalsIgnoreCase(CFG_COMPRESSION))
			{
				result.setProperty(CFG_COMPRESSION, property.getStringValue());
			}
			else
			{
				LOGGER.warn("Unsupported configuration paramter '" + property.getName() + "'.");
			}
		}

		return result;
	}

	public static UnicoreAlgorithmRepository getInstance()
	{
		if (instance == null)
		{
			instance = new UnicoreAlgorithmRepository();
		}
		return instance;
	}

	public static UnicoreAlgorithmRepository getInstance(String wpsConfigPath)
	{
		if (instance == null)
		{
			instance = new UnicoreAlgorithmRepository(wpsConfigPath);
		}
		return instance;
	}

	public Properties getUnicoreProperties()
	{
		return unicoreProperties;
	}

	public boolean addAlgorithm(Object processID)
	{
		if (!(processID instanceof String))
		{
			return false;
		}

		String algorithmClassName = (String) processID;

		try
		{
			IUnicoreAlgorithm algorithm = (IUnicoreAlgorithm) UnicoreAlgorithmRepository.class.getClassLoader().loadClass(algorithmClassName).newInstance();

			if (!algorithm.processDescriptionIsValid())
			{
				LOGGER.warn("Algorithm description is not valid: " + algorithmClassName);
				return false;
			}
			
			processDescriptionMap.put(algorithmClassName, algorithm.getDescription());
			LOGGER.info("Algorithm class registered: " + algorithmClassName);

			if (algorithm.getWellKnownName().length() != 0)
			{
				processDescriptionMap.put(algorithm.getWellKnownName(), algorithm.getDescription());
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

	public boolean containsAlgorithm(String processID)
	{
		return processDescriptionMap.containsKey(processID);
	}

	public IAlgorithm getAlgorithm(String processID, ExecuteRequest executeRequest)
	{
		try
		{
			IUnicoreAlgorithm algorithm = (IUnicoreAlgorithm) UnicoreAlgorithmRepository.class.getClassLoader().loadClass(processID).newInstance();

			return algorithm;
		}
		catch (ClassNotFoundException e)
		{
			LOGGER.warn("Could not find algorithm class: " + processID, e);
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e)
		{
			LOGGER.warn("Access error occured while registering algorithm: " + processID);
			throw new RuntimeException(e);
		}
		catch (InstantiationException e)
		{
			LOGGER.warn("Could not instantiate algorithm: " + processID);
			throw new RuntimeException(e);
		}
	}

	public Collection<String> getAlgorithmNames()
	{
		return processDescriptionMap.keySet();
	}

	public Collection<IAlgorithm> getAlgorithms()
	{
		List<IAlgorithm> algorithmList = new ArrayList<IAlgorithm>();
		for(String algorithmName : getAlgorithmNames()){
			try
			{
				IUnicoreAlgorithm algorithm = (IUnicoreAlgorithm) UnicoreAlgorithmRepository.class.getClassLoader().loadClass(algorithmName).newInstance();

				algorithmList.add(algorithm);
			}
			catch (ClassNotFoundException e)
			{
				LOGGER.warn("Could not find algorithm class: " + algorithmName, e);
				throw new RuntimeException(e);
			}
			catch (IllegalAccessException e)
			{
				LOGGER.warn("Access error occured while registering algorithm: " + algorithmName);
				throw new RuntimeException(e);
			}
			catch (InstantiationException e)
			{
				LOGGER.warn("Could not instantiate algorithm: " + algorithmName);
				throw new RuntimeException(e);
			}
		}
		return algorithmList;
	}

	public boolean removeAlgorithm(Object processID)
	{
		if (!(processID instanceof String))
		{
			return false;
		}
		String className = (String) processID;
		if (processDescriptionMap.containsKey(className))
		{
			processDescriptionMap.remove(className);
			return true;
		}
		return false;
	}

	
	public ProcessDescriptionType getProcessDescription(String processID) {
		if(!processDescriptionMap.containsKey(processID)){
			processDescriptionMap.put(processID, getAlgorithm(processID, null).getDescription());
		}
		return processDescriptionMap.get(processID);
	}
	
}
