
package org.n52.wps.unicore;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.IAlgorithmRepository;

public class UnicoreAlgorithmRepository implements IAlgorithmRepository
{
	private static Logger LOGGER = Logger.getLogger(UnicoreAlgorithmRepository.class);

	public static String CFG_REGISTRY = "Registry";
	public static String CFG_KEYSTORE = "Keystore";
	public static String CFG_ALIAS = "Alias";
	public static String CFG_PASSWORD = "Password";
	public static String CFG_TYPE = "Type";
	
	public static String CFG_OVERWRITE = "OverwriteRemoteFile";
	public static String CFG_COMPRESSION = "CompressInputData";

	private static UnicoreAlgorithmRepository instance;

	private Map<String, IAlgorithm> algorithmMap;
	private Properties unicoreProperties;

	public UnicoreAlgorithmRepository()
	{
		algorithmMap = new HashMap<String, IAlgorithm>();

		Property[] propertyArray = WPSConfig.getInstance().getPropertiesForRepositoryClass(this.getClass().getCanonicalName());

		unicoreProperties = createUnicoreProperties(propertyArray);

		for (Property property : propertyArray)
		{
			if (property.getName().equalsIgnoreCase("Algorithm"))
			{
				addAlgorithm(property.getStringValue());
			}
		}
	}

	public UnicoreAlgorithmRepository(String wpsConfigPath)
	{
		algorithmMap = new HashMap<String, IAlgorithm>();

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

	public boolean containsAlgorithm(String processID)
	{
		return algorithmMap.containsKey(processID);
	}

	public IAlgorithm getAlgorithm(String processID)
	{
		return algorithmMap.get(processID);
	}

	public Collection<String> getAlgorithmNames()
	{
		return algorithmMap.keySet();
	}

	public Collection<IAlgorithm> getAlgorithms()
	{
		return algorithmMap.values();
	}

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
