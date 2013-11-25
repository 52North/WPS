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

package org.n52.wps.webapp.dao;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.n52.wps.webapp.entities.ServiceIdentification;
import org.n52.wps.webapp.entities.ServiceProvider;
import org.n52.wps.webapp.util.JDomUtil;
import org.n52.wps.webapp.util.ResourcePathUtil;

public class XmlCapabilitiesDAOTest {

	@InjectMocks
	private CapabilitiesDAO capabilitiesDAO;

	@Mock
	private JDomUtil jDomUtil;

	@Mock
	private ResourcePathUtil resourcePathUtil;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void setup() throws Exception {
		capabilitiesDAO = new XmlCapabilitiesDAO();
		MockitoAnnotations.initMocks(this);
	}

	@After
	public void tearDown() {
		capabilitiesDAO = null;
	}

	@Test
	public void getServiceIdentification() throws Exception {
		when(resourcePathUtil.getWebAppResourcePath(XmlCapabilitiesDAO.FILE_NAME)).thenReturn(
				"mocked_wpsCapabilitiesSkeleton_xml_absolute_path");
		when(jDomUtil.parse("mocked_wpsCapabilitiesSkeleton_xml_absolute_path")).thenReturn(
				createTestServiceIdentificationDoc());
		ServiceIdentification serviceIdentification = capabilitiesDAO.getServiceIdentification();
		assertEquals("Created Doc Title", serviceIdentification.getTitle());
		assertEquals("Created Doc Abstract", serviceIdentification.getServiceAbstract());
	}

	@Test
	public void saveServiceIdentification_validServiceIdentification() throws Exception {
		when(resourcePathUtil.getWebAppResourcePath(XmlCapabilitiesDAO.FILE_NAME)).thenReturn(
				"mocked_wpsCapabilitiesSkeleton_xml_absolute_path");
		Document testDoc = createTestServiceIdentificationDoc();
		when(jDomUtil.parse("mocked_wpsCapabilitiesSkeleton_xml_absolute_path")).thenReturn(testDoc);
		ServiceIdentification serviceIdentification = new ServiceIdentification();
		serviceIdentification.setTitle("New Test Title");
		serviceIdentification.setServiceAbstract("New Test Abstract");
		capabilitiesDAO.saveServiceIdentification(serviceIdentification);
		Element root = testDoc.getRootElement();
		Element serviceIdentificationElement = root.getChild("ServiceIdentification",
				Namespace.getNamespace("ows", XmlCapabilitiesDAO.NAMESPACE));
		assertEquals(
				"New Test Title",
				serviceIdentificationElement.getChildText("Title",
						Namespace.getNamespace("ows", XmlCapabilitiesDAO.NAMESPACE)));
		assertEquals(
				"New Test Abstract",
				serviceIdentificationElement.getChildText("Abstract",
						Namespace.getNamespace("ows", XmlCapabilitiesDAO.NAMESPACE)));
		verify(jDomUtil).write(testDoc, "mocked_wpsCapabilitiesSkeleton_xml_absolute_path");
	}
	
	@Test
	public void saveServiceIdentification_nullServiceIdentification() throws Exception {
		when(resourcePathUtil.getWebAppResourcePath(XmlCapabilitiesDAO.FILE_NAME)).thenReturn(
				"mocked_wpsCapabilitiesSkeleton_xml_absolute_path");
		Document testDoc = createTestServiceIdentificationDoc();
		when(jDomUtil.parse("mocked_wpsCapabilitiesSkeleton_xml_absolute_path")).thenReturn(testDoc);
		ServiceIdentification serviceIdentification = null;
		exception.expect(NullPointerException.class);
		capabilitiesDAO.saveServiceIdentification(serviceIdentification);
	}
	
	@Test
	public void getServiceProvider() throws Exception {
		when(resourcePathUtil.getWebAppResourcePath(XmlCapabilitiesDAO.FILE_NAME)).thenReturn(
				"mocked_wpsCapabilitiesSkeleton_xml_absolute_path");
		when(jDomUtil.parse("mocked_wpsCapabilitiesSkeleton_xml_absolute_path")).thenReturn(
				createTestServiceProviderDoc());
		ServiceProvider serviceProvider = capabilitiesDAO.getServiceProvider();
		assertEquals("Created Doc Provider Name", serviceProvider.getProviderName());
		assertEquals("www.createdtestlink.com", serviceProvider.getProviderSite());
	}

	@Test
	public void saveServiceProvider_validServiceProvider() throws Exception {
		when(resourcePathUtil.getWebAppResourcePath(XmlCapabilitiesDAO.FILE_NAME)).thenReturn(
				"mocked_wpsCapabilitiesSkeleton_xml_absolute_path");
		Document testDoc = createTestServiceProviderDoc();
		when(jDomUtil.parse("mocked_wpsCapabilitiesSkeleton_xml_absolute_path")).thenReturn(testDoc);
		ServiceProvider serviceProvider = new ServiceProvider();
		serviceProvider.setProviderName("Test Provider Name");
		serviceProvider.setProviderSite("www.test.com");
		capabilitiesDAO.saveServiceProvider(serviceProvider);
		Element root = testDoc.getRootElement();
		Element serviceProviderElement = root.getChild("ServiceProvider",
				Namespace.getNamespace("ows", XmlCapabilitiesDAO.NAMESPACE));
		Element providerSite = serviceProviderElement.getChild("ProviderSite",
				Namespace.getNamespace("ows", XmlCapabilitiesDAO.NAMESPACE));
		assertEquals(
				"Test Provider Name",
				serviceProviderElement.getChildText("ProviderName",
						Namespace.getNamespace("ows", XmlCapabilitiesDAO.NAMESPACE)));
		assertEquals("www.test.com",
				providerSite.getAttributeValue("href", Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink")));
		verify(jDomUtil).write(testDoc, "mocked_wpsCapabilitiesSkeleton_xml_absolute_path");
	}
	
	@Test
	public void saveServiceIdentification_nullServiceProvider() throws Exception {
		when(resourcePathUtil.getWebAppResourcePath(XmlCapabilitiesDAO.FILE_NAME)).thenReturn(
				"mocked_wpsCapabilitiesSkeleton_xml_absolute_path");
		Document testDoc = createTestServiceProviderDoc();
		when(jDomUtil.parse("mocked_wpsCapabilitiesSkeleton_xml_absolute_path")).thenReturn(testDoc);
		ServiceProvider serviceProvider = null;
		exception.expect(NullPointerException.class);
		capabilitiesDAO.saveServiceProvider(serviceProvider);
	}

	private Document createTestServiceIdentificationDoc() {
		Document document = new Document().setRootElement(new Element("Capabilities", Namespace.getNamespace("wps",
				"http://www.opengis.net/wps/1.0.0")));
		Element root = document.getRootElement();
		Element serviceIdentification = new Element("ServiceIdentification", Namespace.getNamespace("ows",
				XmlCapabilitiesDAO.NAMESPACE));
		Element title = new Element("Title", Namespace.getNamespace("ows", XmlCapabilitiesDAO.NAMESPACE))
				.setText("Created Doc Title");
		Element serviceAbstract = new Element("Abstract", Namespace.getNamespace("ows", XmlCapabilitiesDAO.NAMESPACE))
				.setText("Created Doc Abstract");
		serviceIdentification.addContent(title);
		serviceIdentification.addContent(serviceAbstract);
		root.addContent(serviceIdentification);
		return document;
	}

	private Document createTestServiceProviderDoc() {
		Document document = new Document().setRootElement(new Element("Capabilities", Namespace.getNamespace("wps",
				"http://www.opengis.net/wps/1.0.0")));
		Element root = document.getRootElement();
		Element serviceProvider = new Element("ServiceProvider", Namespace.getNamespace("ows",
				XmlCapabilitiesDAO.NAMESPACE));
		Element providerName = new Element("ProviderName", Namespace.getNamespace("ows", XmlCapabilitiesDAO.NAMESPACE))
				.setText("Created Doc Provider Name");
		Element providerSite = new Element("ProviderSite", Namespace.getNamespace("ows", XmlCapabilitiesDAO.NAMESPACE))
				.setAttribute("href", "www.createdtestlink.com",
						Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink"));
		serviceProvider.addContent(providerName);
		serviceProvider.addContent(providerSite);
		root.addContent(serviceProvider);
		return document;
	}
}
