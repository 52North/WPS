/**
 * Copyright (C) 2013
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
 */

package org.n52.wps.webapp.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.n52.wps.webapp.api.types.BooleanConfigurationEntry;
import org.n52.wps.webapp.api.types.ConfigurationEntry;
import org.n52.wps.webapp.api.types.DoubleConfigurationEntry;
import org.n52.wps.webapp.api.types.FileConfigurationEntry;
import org.n52.wps.webapp.api.types.IntegerConfigurationEntry;
import org.n52.wps.webapp.api.types.StringConfigurationEntry;
import org.n52.wps.webapp.api.types.URIConfigurationEntry;
import org.n52.wps.webapp.common.AbstractTest;
import org.n52.wps.webapp.service.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

public class ConfigurationManagerImplTest extends AbstractTest {

	@Autowired
	@InjectMocks
	ConfigurationManager configurationManager;

	TestConfigurationModule module = new TestConfigurationModule();

	@Mock
	ConfigurationModules configurationModules;

	@Mock
	RepositoryService repositoryService;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void initModule() {
		MockitoAnnotations.initMocks(this);
		when(configurationModules.getAllConfigurationModules()).thenReturn(createTestMap());
	}

	private class TestConfigurationModule implements ConfigurationModule {
		private String stringMember;
		private int intMember;
		private double doubleMember;
		private boolean booleanMember;
		private File fileMember;
		private URI uriMember;
		
		@SuppressWarnings("unused")
		private int intInvalidMember;

		public String getModuleName() {
			return "Test Module Name";
		}

		public boolean isActive() {
			return true;
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
		
		public List<ConfigurationEntry<?>> getConfigurationEntries() {
			StringConfigurationEntry entry1 = new StringConfigurationEntry("test.string.key", "String Title", "Desc",
					true, "Initial Value");
			IntegerConfigurationEntry entry2 = new IntegerConfigurationEntry("test.integer.key", "Integer Title",
					"Integer Desc", true, 44);
			DoubleConfigurationEntry entry3 = new DoubleConfigurationEntry("test.double.key", "Double Title",
					"Double Desc", true, 10.4);
			BooleanConfigurationEntry entry4 = new BooleanConfigurationEntry("test.boolean.key", "Boolean Title",
					"Boolean Desc", true, true);
			FileConfigurationEntry entry5 = new FileConfigurationEntry("test.file.key", "File Title", "File Desc",
					true, new File("path"));
			URIConfigurationEntry entry6 = null;
			try {
				entry6 = new URIConfigurationEntry("test.uri.key", "URI Title", "URI Desc", true, new URI("path"));
			} catch (URISyntaxException e) {
				// do nothing
			}
			StringConfigurationEntry entry7 = new StringConfigurationEntry("test.string.key2", "String Title", "Desc",
					true, "Initial Value 2");
			IntegerConfigurationEntry entry8 = new IntegerConfigurationEntry("test.integer.key2", "Integer Title",
					"Integer Desc", true, 15);
			
			List<ConfigurationEntry<?>> configurationEntries = new ArrayList<ConfigurationEntry<?>>();
			configurationEntries.add(entry1);
			configurationEntries.add(entry2);
			configurationEntries.add(entry3);
			configurationEntries.add(entry4);
			configurationEntries.add(entry5);
			configurationEntries.add(entry6);
			configurationEntries.add(entry7);
			configurationEntries.add(entry8);
			return configurationEntries;
		}

		public List<AlgorithmEntry> getAlgorithmEntries() {
			AlgorithmEntry algorithmEntry = new AlgorithmEntry("name1", true);
			AlgorithmEntry algorithmEntry2 = new AlgorithmEntry("name2", true);

			List<AlgorithmEntry> algorithmEntries = new ArrayList<AlgorithmEntry>();
			algorithmEntries.add(algorithmEntry);
			algorithmEntries.add(algorithmEntry2);
			return algorithmEntries;
		}
	}

	private Map<ConfigurationModule, Map<String, ConfigurationEntry<?>>> createTestMap() {
		Map<ConfigurationModule, Map<String, ConfigurationEntry<?>>> configurationModules = new HashMap<ConfigurationModule, Map<String, ConfigurationEntry<?>>>();
		Map<String, ConfigurationEntry<?>> keyEntryMap = new HashMap<String, ConfigurationEntry<?>>();

		List<ConfigurationEntry<?>> moduleConfigurationEntries = module.getConfigurationEntries();

		for (int i = 0; i < moduleConfigurationEntries.size(); i++) {
			keyEntryMap.put(moduleConfigurationEntries.get(i).getKey(), moduleConfigurationEntries.get(i));
		}
		configurationModules.put(module, keyEntryMap);
		return configurationModules;
	}

	@Test
	public void testModuleInformation() {
		assertEquals(module.getModuleName(), "Test Module Name");
		assertTrue(module.isActive());
	}

	@Test
	public void testGetAllConfigurationModules() {
		configurationManager.getAllConfigurationModules();
		assertNotNull(configurationManager.getAllConfigurationModules());
		assertNotNull(configurationManager.getAllConfigurationModules().get(module));
		assertNotNull(configurationManager.getAllConfigurationModules().get(module).values());
		assertEquals(configurationManager.getAllConfigurationModules().get(module).get("test.string.key").getTitle(),
				"String Title");
		assertEquals(configurationManager.getAllConfigurationModules().get(module).get("test.integer.key").getTitle(),
				"Integer Title");
		assertEquals(configurationManager.getAllConfigurationModules().get(module).get("test.double.key").getTitle(),
				"Double Title");
		assertEquals(configurationManager.getAllConfigurationModules().get(module).get("test.boolean.key").getTitle(),
				"Boolean Title");
		assertEquals(configurationManager.getAllConfigurationModules().get(module).get("test.file.key").getTitle(),
				"File Title");
		assertEquals(configurationManager.getAllConfigurationModules().get(module).get("test.uri.key").getTitle(),
				"URI Title");
	}
	
	@Test
	public void testGetConfigurationModuleByClass() {
		configurationManager.getConfigurationModule(module.getClass());
		verify(configurationModules).getConfigurationModule(module.getClass());
	}
	
	@Test
	public void testGetConfigurationModuleByName() {
		configurationManager.getConfigurationModuleByName(module.getClass().getName());
		verify(configurationModules).getConfigurationModuleByName(module.getClass().getName());
	}

	@Test
	public void testSetValidValues() throws WPSConfigurationException {
		configurationManager.setValue(module, "test.string.key", "test value");
		verify(repositoryService).storeValue(module, "test.string.key", "test value");

		configurationManager.setValue(module, "test.integer.key", 12);
		verify(repositoryService).storeValue(module, "test.integer.key", 12);

		configurationManager.setValue(module, "test.double.key", 14.2);
		verify(repositoryService).storeValue(module, "test.double.key", 14.2);

		configurationManager.setValue(module, "test.boolean.key", true);
		verify(repositoryService).storeValue(module, "test.boolean.key", true);

		configurationManager.setValue(module, "test.file.key", "file_path");
		verify(repositoryService).storeValue(module, "test.file.key", "file_path");

		configurationManager.setValue(module, "test.uri.key", "uri_path");
		verify(repositoryService).storeValue(module, "test.uri.key", "uri_path");
	}

	@Test
	public void testSetInvalidStringalue() throws WPSConfigurationException {
		exception.expect(WPSConfigurationException.class);
		configurationManager.setValue(module, "test.string.key", "");
	}

	@Test
	public void testSetInvalidIntegerValue() throws WPSConfigurationException {
		exception.expect(WPSConfigurationException.class);
		configurationManager.setValue(module, "test.integer.key", "invalid_integer");
	}

	@Test
	public void testSetInvalidDoubleValue() throws WPSConfigurationException {
		exception.expect(WPSConfigurationException.class);
		configurationManager.setValue(module, "test.double.key", "invalid_double");
	}

	@Test
	public void testSetInvalidBooleanValue() throws WPSConfigurationException {
		exception.expect(WPSConfigurationException.class);
		configurationManager.setValue(module, "test.boolean.key", "invalid_boolean");
	}

	@Test
	public void testSetInvalidFileValue() throws WPSConfigurationException {
		exception.expect(WPSConfigurationException.class);
		configurationManager.setValue(module, "test.file.key", "");
	}

	@Test
	public void testSetInvalidURIValue() throws WPSConfigurationException {
		exception.expect(WPSConfigurationException.class);
		configurationManager.setValue(module, "test.uri.key", "");
	}

	@Test
	public void testGetValidValue() throws WPSConfigurationException, URISyntaxException {
		assertEquals(configurationManager.getValue(module, "test.string.key", String.class), "Initial Value");
		assertEquals(configurationManager.getValue(module, "test.integer.key", Integer.class), Integer.valueOf(44));
		assertEquals(configurationManager.getValue(module, "test.double.key", Double.class), Double.valueOf(10.4));
		assertEquals(configurationManager.getValue(module, "test.boolean.key", Boolean.class), Boolean.valueOf(true));
		assertEquals(configurationManager.getValue(module, "test.file.key", File.class), new File("path"));
		assertEquals(configurationManager.getValue(module, "test.uri.key", URI.class), new URI("path"));
	}

	@Test
	public void testGetInvalidStringalue() throws WPSConfigurationException {
		exception.expect(WPSConfigurationException.class);
		configurationManager.getValue(module, "test.string.key", Integer.class);
	}

	@Test
	public void testGetInvalidIntegerValue() throws WPSConfigurationException {
		exception.expect(WPSConfigurationException.class);
		configurationManager.getValue(module, "test.integer.key", Boolean.class);
	}

	@Test
	public void testGetInvalidDoubleValue() throws WPSConfigurationException {
		exception.expect(WPSConfigurationException.class);
		configurationManager.getValue(module, "test.double.key", Boolean.class);
	}

	@Test
	public void testGetInvalidBooleanValue() throws WPSConfigurationException {
		exception.expect(WPSConfigurationException.class);
		configurationManager.getValue(module, "test.boolean.key", Double.class);
	}

	@Test
	public void testGetInvalidFileValue() throws WPSConfigurationException {
		exception.expect(WPSConfigurationException.class);
		configurationManager.getValue(module, "test.file.key", Integer.class);
	}

	@Test
	public void testGetInvalidURIValue() throws WPSConfigurationException {
		exception.expect(WPSConfigurationException.class);
		configurationManager.getValue(module, "test.uri.key", Integer.class);
	}

	@Test
	public void testPassValidValueToSetterMethods() throws WPSConfigurationException {

		configurationManager.setValue(module, "test.string.key", "test value");
		assertEquals(module.getStringMember(), "test value");

		configurationManager.setValue(module, "test.integer.key", 12);
		assertEquals(module.getIntMember(), 12);

		configurationManager.setValue(module, "test.double.key", 14.2);
		assertEquals(module.getDoubleMember(), 14.2, 0);

		configurationManager.setValue(module, "test.boolean.key", true);
		assertEquals(module.isBooleanMember(), true);

		configurationManager.setValue(module, "test.file.key", "file_path");
		assertEquals(module.getFileMember().getPath(), "file_path");

		configurationManager.setValue(module, "test.uri.key", "uri_path");
		assertEquals(module.getUriMember().getPath(), "uri_path");
	}
	
	@Test
	public void testPassInvalidValueToSetterMethod() throws WPSConfigurationException {
		exception.expect(WPSConfigurationException.class);
		configurationManager.setValue(module, "test.string.key2", "test value");
	}
	
	@Test
	public void testPassValueToSetterMethodWithMultipleParameters() throws WPSConfigurationException {
		exception.expect(WPSConfigurationException.class);
		configurationManager.setValue(module, "test.integer.key2", 12);
	}
}
