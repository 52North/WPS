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

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.n52.wps.webapp.entities.ServiceIdentification;
import org.n52.wps.webapp.entities.ServiceProvider;
import org.n52.wps.webapp.util.JDomUtil;
import org.n52.wps.webapp.util.ResourcePathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CapabilitiesDAOXml implements CapabilitiesDAO {

	public static final String FILE_NAME = "config" + File.separator + "wpsCapabilitiesSkeleton.xml";
	public static final String NAMESPACE = "http://www.opengis.net/ows/1.1";
	private static Logger LOGGER = Logger.getLogger(CapabilitiesDAOXml.class);

	@Autowired
	JDomUtil jDomUtil;

	@Autowired
	ResourcePathUtil resourcePathUtil;

	@Override
	public ServiceIdentification getServiceIdentification() {
		Document document = null;
		ServiceIdentification serviceIdentification = new ServiceIdentification();
		try {
			String absolutePath = resourcePathUtil.getWebAppResourcePath(FILE_NAME);
			document = jDomUtil.load(absolutePath);
			if (document != null) {
				Element root = document.getRootElement();
				Element serviceIdentificationElement = root.getChild("ServiceIdentification",
						Namespace.getNamespace(NAMESPACE));
				serviceIdentification.setTitle(getValue(serviceIdentificationElement, "Title"));
				serviceIdentification.setServiceAbstract(getValue(serviceIdentificationElement, "Abstract"));
				serviceIdentification.setServiceType(getValue(serviceIdentificationElement, "ServiceType"));
				serviceIdentification
						.setServiceTypeVersion(getValue(serviceIdentificationElement, "ServiceTypeVersion"));
				serviceIdentification.setFees(getValue(serviceIdentificationElement, "Fees"));
				serviceIdentification.setAccessConstraints(getValue(serviceIdentificationElement, "AccessConstraints"));

				// keywords
				Element keywords = serviceIdentificationElement.getChild("Keywords", Namespace.getNamespace(NAMESPACE));
				if (keywords != null) {
					Set<String> keywrodsSet = new HashSet<String>();
					for (Object keyword : keywords.getChildren()) {
						keywrodsSet.add(((Element) keyword).getValue());
					}
					serviceIdentification.setKeywords(keywrodsSet);
				}
			}
		} catch (JDOMException | IOException e) {
			LOGGER.error(e.getMessage());
		}
		return serviceIdentification;
	}

	@Override
	public ServiceProvider getServiceProvider() {
		Document document = null;
		ServiceProvider serviceProvider = new ServiceProvider();
		try {
			String absolutePath = resourcePathUtil.getWebAppResourcePath(FILE_NAME);
			document = jDomUtil.load(absolutePath);
			if (document != null) {
				Element root = document.getRootElement();
				Element serviceProviderElement = getElement(root, "ServiceProvider");;
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
			}
		} catch (JDOMException | IOException e) {
			LOGGER.error(e.getMessage());
		}
		return serviceProvider;
	}

	@Override
	public void save(ServiceIdentification serviceIdentification) {
		Document document = null;
		try {
			String absolutePath = resourcePathUtil.getWebAppResourcePath(FILE_NAME);
			document = jDomUtil.load(absolutePath);
			if (document != null) {
				if (serviceIdentification != null) {
					Element root = document.getRootElement();
					Element serviceIdentificationElement = getElement(root, "ServiceIdentification");
					setElement(getElement(serviceIdentificationElement, "Title"), serviceIdentification.getTitle());
					setElement(getElement(serviceIdentificationElement, "Abstract"), serviceIdentification.getServiceAbstract());
					setElement(getElement(serviceIdentificationElement, "ServiceType"), serviceIdentification.getServiceType());
					setElement(getElement(serviceIdentificationElement, "ServiceTypeVersion"), serviceIdentification.getServiceTypeVersion());
					setElement(getElement(serviceIdentificationElement, "Fees"), serviceIdentification.getFees());
					setElement(getElement(serviceIdentificationElement, "AccessConstraints"), serviceIdentification.getAccessConstraints());
					setElement(getElement(serviceIdentificationElement, "Title"), serviceIdentification.getTitle());
					setElement(getElement(serviceIdentificationElement, "Title"), serviceIdentification.getTitle());

					Element keywords = getElement(serviceIdentificationElement, "Keywords");
					if (keywords != null) {
						keywords.removeChildren("Keyword", Namespace.getNamespace(NAMESPACE));
						for (String newKeyword : serviceIdentification.getKeywords()) {
							Element keyword = new Element("Keyword", Namespace.getNamespace("ows", NAMESPACE))
									.setText(newKeyword);
							keywords.addContent(keyword);
						}
					}
					
					jDomUtil.write(document, absolutePath);
				}
			}
		} catch (JDOMException | IOException e) {
			LOGGER.error(e.getMessage());
		}
	}

	@Override
	public void save(ServiceProvider serviceProvider) {
		Document document = null;
		try {
			String absolutePath = resourcePathUtil.getWebAppResourcePath(FILE_NAME);
			document = jDomUtil.load(absolutePath);
			if (document != null) {
				if (serviceProvider != null) {
					Element root = document.getRootElement();
					Element serviceProviderElement = getElement(root, "ServiceProvider");
					
					setElement(getElement(serviceProviderElement, "ProviderName"), serviceProvider.getProviderName());
					getElement(serviceProviderElement, "ProviderSite").setAttribute("href",
							serviceProvider.getProviderSite(),
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
				}
			}
		} catch (JDOMException | IOException e) {
			LOGGER.error(e.getMessage());
		}
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
