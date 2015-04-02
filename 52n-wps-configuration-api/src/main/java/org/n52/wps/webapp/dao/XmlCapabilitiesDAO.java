/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *       • Apache License, version 2.0
 *       • Apache Software License, version 1.0
 *       • GNU Lesser General Public License, version 3
 *       • Mozilla Public License, versions 1.0, 1.1 and 2.0
 *       • Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.wps.webapp.dao;

import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.n52.wps.webapp.entities.ServiceIdentification;
import org.n52.wps.webapp.entities.ServiceProvider;
import org.n52.wps.webapp.util.JDomUtil;
import org.n52.wps.webapp.util.ResourcePathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * An implementation for the {@link CapabilitiesDAO} interface. This implementation uses {@code JDom} to parse the
 * {@code wpsCapabilitiesSkeleton.xml} file.
 */
@Repository("capabilitiesDAO")
public class XmlCapabilitiesDAO implements CapabilitiesDAO {

	public static final String FILE_NAME = "config/wpsCapabilitiesSkeleton.xml";
	public static final String NAMESPACE = "http://www.opengis.net/ows/1.1";
	private static Logger LOGGER = LoggerFactory.getLogger(XmlCapabilitiesDAO.class);

	@Autowired
	private JDomUtil jDomUtil;

	@Autowired
	private ResourcePathUtil resourcePathUtil;

	@Override
	public ServiceIdentification getServiceIdentification() {
		Document document = null;
		ServiceIdentification serviceIdentification = new ServiceIdentification();
		String absolutePath = resourcePathUtil.getWebAppResourcePath(FILE_NAME);
		document = jDomUtil.parse(absolutePath);
		Element root = document.getRootElement();
		Element serviceIdentificationElement = root
				.getChild("ServiceIdentification", Namespace.getNamespace(NAMESPACE));
		serviceIdentification.setTitle(getValue(serviceIdentificationElement, "Title"));
		serviceIdentification.setServiceAbstract(getValue(serviceIdentificationElement, "Abstract"));
		serviceIdentification.setServiceType(getValue(serviceIdentificationElement, "ServiceType"));
		
		// versions
		List<?> versions = serviceIdentificationElement.getChildren("ServiceTypeVersion", Namespace.getNamespace(NAMESPACE));
		if (versions != null) {
			StringBuilder sb = new StringBuilder();
			Iterator<?> versionIterator = versions.iterator();
			while (versionIterator.hasNext()) {
				Object version = versionIterator.next();				
				String suffix = "";
				if(versionIterator.hasNext()){
					suffix = "; ";
				}
				sb.append(((Element) version).getValue() + suffix);		
			}
			serviceIdentification.setServiceTypeVersions(sb.toString());
		}
		
		serviceIdentification.setFees(getValue(serviceIdentificationElement, "Fees"));
		serviceIdentification.setAccessConstraints(getValue(serviceIdentificationElement, "AccessConstraints"));

		// keywords
		Element keywords = serviceIdentificationElement.getChild("Keywords", Namespace.getNamespace(NAMESPACE));
		if (keywords != null) {
			StringBuilder sb = new StringBuilder();
			Iterator<?> keywordIterator = keywords.getChildren().iterator();
			while (keywordIterator.hasNext()) {
				Object keyword = keywordIterator.next();				
				String suffix = "";
				if(keywordIterator.hasNext()){
					suffix = "; ";
				}
				sb.append(((Element) keyword).getValue() + suffix);	
				serviceIdentification.setKeywords(sb.toString());			
			}
		}
		LOGGER.info("'{}' is parsed and a ServiceIdentification object is returned", absolutePath);
		return serviceIdentification;
	}

	@Override
	public void saveServiceIdentification(ServiceIdentification serviceIdentification) {
		Document document = null;
		String absolutePath = resourcePathUtil.getWebAppResourcePath(FILE_NAME);
		document = jDomUtil.parse(absolutePath);

		Element root = document.getRootElement();
		Element serviceIdentificationElement = getElement(root, "ServiceIdentification");
		setElement(getElement(serviceIdentificationElement, "Title"), serviceIdentification.getTitle());
		setElement(getElement(serviceIdentificationElement, "Abstract"), serviceIdentification.getServiceAbstract());
		setElement(getElement(serviceIdentificationElement, "ServiceType"), serviceIdentification.getServiceType());
		
		serviceIdentificationElement.removeChildren("ServiceTypeVersion", Namespace.getNamespace(NAMESPACE));
		
		String[] versionArray = serviceIdentification.getServiceTypeVersions() != null ? serviceIdentification.getServiceTypeVersions().split(";") : new String[0];
		
		for (String version : versionArray) {
			Element versionElement = new Element("ServiceTypeVersion", Namespace.getNamespace("ows", NAMESPACE)).setText(version);
			serviceIdentificationElement.addContent(versionElement);
		}
		
		setElement(getElement(serviceIdentificationElement, "Fees"), serviceIdentification.getFees());
		setElement(getElement(serviceIdentificationElement, "AccessConstraints"),
				serviceIdentification.getAccessConstraints());

		Element keywords = getElement(serviceIdentificationElement, "Keywords");
		if (keywords != null) {
			keywords.removeChildren("Keyword", Namespace.getNamespace(NAMESPACE));
		}

		if (serviceIdentification.getKeywords() != null) {
			String[] keywordsArray = serviceIdentification.getKeywords().trim().split(";");
			for (String newKeyword : keywordsArray) {
				Element keyword = new Element("Keyword", Namespace.getNamespace("ows", NAMESPACE)).setText(newKeyword);
				keywords.addContent(keyword);
			}
		}
		jDomUtil.write(document, absolutePath);
		LOGGER.info("ServiceIdentification values written to '{}'", absolutePath);
	}

	@Override
	public ServiceProvider getServiceProvider() {
		Document document = null;
		ServiceProvider serviceProvider = new ServiceProvider();

		String absolutePath = resourcePathUtil.getWebAppResourcePath(FILE_NAME);
		document = jDomUtil.parse(absolutePath);
		Element root = document.getRootElement();
		Element serviceProviderElement = getElement(root, "ServiceProvider");

		serviceProvider.setProviderName(getValue(serviceProviderElement, "ProviderName"));

		// a special case, an attribute with a namespace
		serviceProvider.setProviderSite(serviceProviderElement.getChild("ProviderSite",
				Namespace.getNamespace(NAMESPACE)).getAttributeValue("href",
				Namespace.getNamespace("http://www.w3.org/1999/xlink")));

		// contact info
		Element serviceContact = getElement(serviceProviderElement, "serviceContact");
		serviceProvider.setIndividualName(getValue(serviceContact, "IndividualName"));
		serviceProvider.setPosition(getValue(serviceContact, "PositionName"));

		// phone
		Element contactInfo = getElement(serviceContact, "ContactInfo");
		Element phone = getElement(contactInfo, "Phone");
		serviceProvider.setPhone(getValue(phone, "Voice"));
		serviceProvider.setFacsimile(getValue(phone, "Facsimile"));

		// address
		Element address = getElement(contactInfo, "Address");
		serviceProvider.setDeliveryPoint(getValue(address, "DeliveryPoint"));
		serviceProvider.setCity(getValue(address, "City"));
		serviceProvider.setAdministrativeArea(getValue(address, "AdministrativeArea"));
		serviceProvider.setPostalCode(getValue(address, "PostalCode"));
		serviceProvider.setCountry(getValue(address, "Country"));
		serviceProvider.setEmail(getValue(address, "ElectronicMailAddress"));
		LOGGER.info("'{}' is parsed and a ServiceProvider object is returned", absolutePath);
		return serviceProvider;
	}

	@Override
	public void saveServiceProvider(ServiceProvider serviceProvider) {
		Document document = null;
		String absolutePath = resourcePathUtil.getWebAppResourcePath(FILE_NAME);
		document = jDomUtil.parse(absolutePath);
		Element root = document.getRootElement();
		Element serviceProviderElement = getElement(root, "ServiceProvider");

		setElement(getElement(serviceProviderElement, "ProviderName"), serviceProvider.getProviderName());
		getElement(serviceProviderElement, "ProviderSite").setAttribute("href", serviceProvider.getProviderSite(),
				Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink"));

		Element serviceContact = getElement(serviceProviderElement, "ServiceContact");
		setElement(getElement(serviceContact, "IndividualName"), serviceProvider.getIndividualName());
		setElement(getElement(serviceContact, "PositionName"), serviceProvider.getPosition());

		Element contactInfo = getElement(serviceContact, "ContactInfo");
		Element phone = getElement(contactInfo, "Phone");
		setElement(getElement(phone, "Voice"), serviceProvider.getPhone());
		setElement(getElement(phone, "Facsimile"), serviceProvider.getFacsimile());

		Element address = getElement(contactInfo, "Address");
		setElement(getElement(address, "DeliveryPoint"), serviceProvider.getDeliveryPoint());
		setElement(getElement(address, "City"), serviceProvider.getCity());
		setElement(getElement(address, "AdministrativeArea"), serviceProvider.getAdministrativeArea());
		setElement(getElement(address, "PostalCode"), serviceProvider.getPostalCode());
		setElement(getElement(address, "Country"), serviceProvider.getCountry());
		setElement(getElement(address, "ElectronicMailAddress"), serviceProvider.getEmail());
		jDomUtil.write(document, absolutePath);
		LOGGER.info("ServiceProvider values written to '{}'", absolutePath);
	}

	private String getValue(Element element, String child) {
		if (element != null) {
			Element childElement = element.getChild(child, Namespace.getNamespace(NAMESPACE));
			if (childElement != null) {
				return childElement.getValue();
			}
		}
		return null;
	}

	private Element getElement(Element element, String child) {
		if (element != null) {
			return element.getChild(child, Namespace.getNamespace(NAMESPACE));
		}
		return null;
	}

	private void setElement(Element element, String value) {
		if (element != null) {
			element.setText(value);
		}
	}
}
