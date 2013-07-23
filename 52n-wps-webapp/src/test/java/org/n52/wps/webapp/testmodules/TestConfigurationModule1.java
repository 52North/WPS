package org.n52.wps.webapp.testmodules;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.n52.wps.webapp.api.AlgorithmEntry;
import org.n52.wps.webapp.api.ConfigurationCategory;
import org.n52.wps.webapp.api.ConfigurationKey;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.n52.wps.webapp.api.types.BooleanConfigurationEntry;
import org.n52.wps.webapp.api.types.ConfigurationEntry;
import org.n52.wps.webapp.api.types.DoubleConfigurationEntry;
import org.n52.wps.webapp.api.types.FileConfigurationEntry;
import org.n52.wps.webapp.api.types.IntegerConfigurationEntry;
import org.n52.wps.webapp.api.types.StringConfigurationEntry;
import org.n52.wps.webapp.api.types.URIConfigurationEntry;

public class TestConfigurationModule1 implements ConfigurationModule {
	private String stringMember;
	private int intMember;
	private double doubleMember;
	private boolean booleanMember;
	private File fileMember;
	private URI uriMember;
	@SuppressWarnings("unused")
	private int intInvalidMember;

	private static ConfigurationEntry<String> entry1 = new StringConfigurationEntry("test.string.key", "String Title",
			"Desc", true, "Initial Value");
	private static ConfigurationEntry<Integer> entry2 = new IntegerConfigurationEntry("test.integer.key",
			"Integer Title", "Integer Desc", true, 44);
	private static ConfigurationEntry<Double> entry3 = new DoubleConfigurationEntry("test.double.key", "Double Title",
			"Double Desc", true, 10.4);
	private static ConfigurationEntry<Boolean> entry4 = new BooleanConfigurationEntry("test.boolean.key",
			"Boolean Title", "Boolean Desc", true, true);
	private static ConfigurationEntry<File> entry5 = new FileConfigurationEntry("test.file.key", "File Title",
			"File Desc", true, new File("path"));

	private static ConfigurationEntry<URI> entry6 = new URIConfigurationEntry("test.uri.key", "URI Title");

	static {
		try {
			entry6.setDescription("URI Desc");
			entry6.setRequired(true);
			entry6.setValue(new URI("path"));
		} catch (URISyntaxException e) {
			// do nothing
		}
	}

	private static ConfigurationEntry<String> entry7 = new StringConfigurationEntry("test.string.key2", "String Title",
			"Desc", true, "Initial Value 2");
	private static IntegerConfigurationEntry entry8 = new IntegerConfigurationEntry("test.integer.key2",
			"Integer Title", "Integer Desc", true, 15);

	private static AlgorithmEntry algorithmEntry = new AlgorithmEntry("name1", true);
	private static AlgorithmEntry algorithmEntry2 = new AlgorithmEntry("name2", true);

	private static List<? extends ConfigurationEntry<?>> configurationEntries = Arrays.asList(entry1, entry2, entry3,
			entry4, entry5, entry6, entry7, entry8);

	private static List<AlgorithmEntry> algorithmEntries = Arrays.asList(algorithmEntry, algorithmEntry2);

	@Override
	public String getModuleName() {
		return "Test Module Name 1";
	}

	@Override
	public boolean isActive() {
		return true;
	}
	
	@Override
	public List<? extends ConfigurationEntry<?>> getConfigurationEntries() {
		return configurationEntries;
	}

	@Override
	public List<AlgorithmEntry> getAlgorithmEntries() {
		return algorithmEntries;
	}

	@Override
	public ConfigurationCategory getCategory() {
		return ConfigurationCategory.REPOSITORY;
	}

	public String getStringMember() {
		return stringMember;
	}

	@ConfigurationKey(key = "test.string.key")
	public void setStringMember(String stringMember) {
		this.stringMember = stringMember;
	}

	public int getIntMember() {
		return intMember;
	}

	@ConfigurationKey(key = "test.integer.key")
	public void setIntMember(int intMember) {
		this.intMember = intMember;
	}

	public double getDoubleMember() {
		return doubleMember;
	}

	@ConfigurationKey(key = "test.double.key")
	public void setDoubleMember(double doubleMember) {
		this.doubleMember = doubleMember;
	}

	public boolean isBooleanMember() {
		return booleanMember;
	}

	@ConfigurationKey(key = "test.boolean.key")
	public void setBooleanMemver(boolean booleanMember) {
		this.booleanMember = booleanMember;
	}

	public File getFileMember() {
		return fileMember;
	}

	@ConfigurationKey(key = "test.file.key")
	public void setFileMember(File fileMember) {
		this.fileMember = fileMember;
	}

	public URI getUriMember() {
		return uriMember;
	}

	@ConfigurationKey(key = "test.uri.key")
	public void setUriMember(URI uriMember) {
		this.uriMember = uriMember;
	}

	@ConfigurationKey(key = "test.string.key2")
	public void setIntInvalidMember(int intInvalidMember) {
		this.intInvalidMember = intInvalidMember;
	}

	@ConfigurationKey(key = "test.integer.key2")
	public void setIntInvalidMember(int intInvalidMember, int secondParameter) {
		this.intInvalidMember = intInvalidMember;
	}
}