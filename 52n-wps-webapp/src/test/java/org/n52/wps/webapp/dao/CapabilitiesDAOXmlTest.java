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

import java.io.IOException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.n52.wps.webapp.common.AbstractTest;
import org.n52.wps.webapp.entities.ServiceIdentification;
import org.n52.wps.webapp.entities.ServiceProvider;
import org.n52.wps.webapp.util.JDomUtil;
import org.n52.wps.webapp.util.ResourcePathUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class CapabilitiesDAOXmlTest extends AbstractTest {

	@Autowired
	@InjectMocks
	CapabilitiesDAO capabilitiesDAO;

	@Mock
	JDomUtil jDomUtil;

	@Autowired
	ResourcePathUtil resourcePathUtil;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testGetServiceIdentification() throws JDOMException, IOException {
		String absolutePath = resourcePathUtil.getWebAppResourcePath(CapabilitiesDAOXml.FILE_NAME);
		when(jDomUtil.load(absolutePath)).thenReturn(createTestServiceIdentificationDoc());
		ServiceIdentification serviceIdentification = capabilitiesDAO.getServiceIdentification();
		assertEquals(serviceIdentification.getTitle(), "Created Doc Title");
		assertEquals(serviceIdentification.getServiceAbstract(), "Created Doc Abstract");
	}

	@Test
	public void testGetServiceProvider() throws IOException, JDOMException {
		String absolutePath = resourcePathUtil.getWebAppResourcePath(CapabilitiesDAOXml.FILE_NAME);
		when(jDomUtil.load(absolutePath)).thenReturn(createTestServiceProviderDoc());
		ServiceProvider serviceProvider = capabilitiesDAO.getServiceProvider();
		assertEquals(serviceProvider.getProviderName(), "Created Doc Provider Name");
		assertEquals(serviceProvider.getProviderSite(), "www.createdtestlink.com");
	}

	@Test
	public void testSaveServiceIdentification() throws JDOMException, IOException {
		Document testDoc = createTestServiceIdentificationDoc();
		String absolutePath = resourcePathUtil.getWebAppResourcePath(CapabilitiesDAOXml.FILE_NAME);
		when(jDomUtil.load(absolutePath)).thenReturn(testDoc);
		ServiceIdentification serviceIdentification = new ServiceIdentification();
		serviceIdentification.setTitle("New Test Title");
		serviceIdentification.setServiceAbstract("New Test Abstract");
		capabilitiesDAO.save(serviceIdentification);
		Element root = testDoc.getRootElement();
		Element serviceIdentificationElement = root.getChild("ServiceIdentification",
				Namespace.getNamespace("ows", CapabilitiesDAOXml.NAMESPACE));
		assertEquals(
				serviceIdentificationElement.getChildText("Title",
						Namespace.getNamespace("ows", CapabilitiesDAOXml.NAMESPACE)), "New Test Title");
		assertEquals(
				serviceIdentificationElement.getChildText("Abstract",
						Namespace.getNamespace("ows", CapabilitiesDAOXml.NAMESPACE)), "New Test Abstract");
		verify(jDomUtil).write(testDoc, absolutePath);
	}

	@Test
	public void testSaveServiceProvider() throws JDOMException, IOException {
		Document testDoc = createTestServiceProviderDoc();
		String absolutePath = resourcePathUtil.getWebAppResourcePath(CapabilitiesDAOXml.FILE_NAME);
		when(jDomUtil.load(absolutePath)).thenReturn(testDoc);
		ServiceProvider serviceProvider = new ServiceProvider();
		serviceProvider.setProviderName("Test Provider Name");
		serviceProvider.setProviderSite("www.test.com");
		capabilitiesDAO.save(serviceProvider);
		Element root = testDoc.getRootElement();
		Element serviceProviderElement = root.getChild("ServiceProvider",
				Namespace.getNamespace("ows", CapabilitiesDAOXml.NAMESPACE));
		Element providerSite = serviceProviderElement.getChild("ProviderSite",
				Namespace.getNamespace("ows", CapabilitiesDAOXml.NAMESPACE));
		assertEquals(
				serviceProviderElement.getChildText("ProviderName",
						Namespace.getNamespace("ows", CapabilitiesDAOXml.NAMESPACE)), "Test Provider Name");
		assertEquals(
				providerSite.getAttributeValue("href", Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink")),
				"www.test.com");
		verify(jDomUtil).write(testDoc, absolutePath);
	}

	private Document createTestServiceIdentificationDoc() {
		Document document = new Document().setRootElement(new Element("Capabilities", Namespace.getNamespace("wps",
				"http://www.opengis.net/wps/1.0.0")));
		Element root = document.getRootElement();
		Element serviceIdentification = new Element("ServiceIdentification", Namespace.getNamespace("ows",
				CapabilitiesDAOXml.NAMESPACE));
		Element title = new Element("Title", Namespace.getNamespace("ows", CapabilitiesDAOXml.NAMESPACE))
				.setText("Created Doc Title");
		Element serviceAbstract = new Element("Abstract", Namespace.getNamespace("ows", CapabilitiesDAOXml.NAMESPACE))
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
				CapabilitiesDAOXml.NAMESPACE));
		Element providerName = new Element("ProviderName", Namespace.getNamespace("ows", CapabilitiesDAOXml.NAMESPACE))
				.setText("Created Doc Provider Name");
		Element providerSite = new Element("ProviderSite", Namespace.getNamespace("ows", CapabilitiesDAOXml.NAMESPACE))
				.setAttribute("href", "www.createdtestlink.com",
						Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink"));
		serviceProvider.addContent(providerName);
		serviceProvider.addContent(providerSite);
		root.addContent(serviceProvider);
		return document;
	}
}
