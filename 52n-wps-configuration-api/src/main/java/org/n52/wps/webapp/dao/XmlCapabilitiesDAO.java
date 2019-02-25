/*
 * Copyright (C) 2007-2018 52°North Initiative for Geospatial Open Source
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
 * An implementation for the {@link CapabilitiesDAO} interface. This
 * implementation uses {@code JDom} to parse the
 * {@code wpsCapabilitiesSkeleton.xml} file.
 */
@Repository("capabilitiesDAO")
public class XmlCapabilitiesDAO implements CapabilitiesDAO {

    public static final String FILE_NAME = "config/wpsCapabilitiesSkeleton.xml";

    public static final String NAMESPACE = "http://www.opengis.net/ows/1.1";

    private static final String FEES = "Fees";
    private static final String ACCESS_CONSTRAINTS = "AccessConstraints";
    private static final String SERVICE_IDENTIFICATION = "ServiceIdentification";
    private static final String TITLE = "Title";
    private static final String ABSTRACT = "Abstract";
    private static final String SERVICE_TYPE = "ServiceType";
    private static final String SERVICE_TYPE_VERSION = "ServiceTypeVersion";
    private static final String KEYWORDS = "Keywords";
    private static final String KEYWORD = "Keyword";
    private static final String SERVICE_PROVIDER = "ServiceProvider";
    private static final String SEMICOLON = ";";
    private static final String OWS = "ows";
    private static final String NAMESPACE_XLINK = "http://www.w3.org/1999/xlink";
    private static final String PROVIDER_NAME = "ProviderName";
    private static final String PROVIDER_SITE = "ProviderSite";
    private static final String HREF = "href";
    private static final String SERVICE_CONTACT = "ServiceContact";
    private static final String INDIVIDUAL_NAME = "IndividualName";
    private static final String POSITION_NAME = "PositionName";
    private static final String CONTACT_INFO = "ContactInfo";
    private static final String PHONE = "Phone";
    private static final String VOICE = "Voice";
    private static final String FACSIMILE = "Facsimile";
    private static final String ADDRESS = "Address";
    private static final String DELIVERY_POINT = "DeliveryPoint";
    private static final String CITY = "City";
    private static final String ADMINISTRATIVE_AREA = "AdministrativeArea";
    private static final String POSTAL_CODE = "PostalCode";
    private static final String COUNTRY = "Country";
    private static final String ELECTRONIC_MAIL_ADDRESS = "ElectronicMailAddress";
    private static final String SUFFIX = SEMICOLON + " ";

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
        Element serviceIdentificationElement =
                root.getChild(SERVICE_IDENTIFICATION, Namespace.getNamespace(NAMESPACE));
        serviceIdentification.setTitle(getValue(serviceIdentificationElement, TITLE));
        serviceIdentification.setServiceAbstract(getValue(serviceIdentificationElement, ABSTRACT));
        serviceIdentification.setServiceType(getValue(serviceIdentificationElement, SERVICE_TYPE));

        // versions
        List<?> versions =
                serviceIdentificationElement.getChildren(SERVICE_TYPE_VERSION, Namespace.getNamespace(NAMESPACE));
        if (versions != null) {
            StringBuilder sb = new StringBuilder();
            Iterator<?> versionIterator = versions.iterator();
            while (versionIterator.hasNext()) {
                Object version = versionIterator.next();
                String suffix = "";
                if (versionIterator.hasNext()) {
                    suffix = SUFFIX;
                }
                sb.append(((Element) version).getValue() + suffix);
            }
            serviceIdentification.setServiceTypeVersions(sb.toString());
        }

        serviceIdentification.setFees(getValue(serviceIdentificationElement, FEES));
        serviceIdentification.setAccessConstraints(getValue(serviceIdentificationElement, ACCESS_CONSTRAINTS));

        // keywords
        Element keywords = serviceIdentificationElement.getChild(KEYWORDS, Namespace.getNamespace(NAMESPACE));
        if (keywords != null) {
            StringBuilder sb = new StringBuilder();
            Iterator<?> keywordIterator = keywords.getChildren().iterator();
            while (keywordIterator.hasNext()) {
                Object keyword = keywordIterator.next();
                String suffix = "";
                if (keywordIterator.hasNext()) {
                    suffix = SUFFIX;
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
        Element serviceIdentificationElement = getElement(root, SERVICE_IDENTIFICATION);
        setElement(getElement(serviceIdentificationElement, TITLE), serviceIdentification.getTitle());
        setElement(getElement(serviceIdentificationElement, ABSTRACT), serviceIdentification.getServiceAbstract());
        setElement(getElement(serviceIdentificationElement, SERVICE_TYPE), serviceIdentification.getServiceType());

        serviceIdentificationElement.removeChildren(SERVICE_TYPE_VERSION, Namespace.getNamespace(NAMESPACE));

        String[] versionArray = serviceIdentification.getServiceTypeVersions() != null
                ? serviceIdentification.getServiceTypeVersions().split(SEMICOLON)
                : new String[0];

        for (String version : versionArray) {
            Element versionElement =
                    new Element(SERVICE_TYPE_VERSION, Namespace.getNamespace(OWS, NAMESPACE)).setText(version);
            serviceIdentificationElement.addContent(versionElement);
        }

        setElement(getElement(serviceIdentificationElement, FEES), serviceIdentification.getFees());
        setElement(getElement(serviceIdentificationElement, ACCESS_CONSTRAINTS),
                serviceIdentification.getAccessConstraints());

        Element keywords = getElement(serviceIdentificationElement, KEYWORDS);
        if (keywords != null) {
            keywords.removeChildren(KEYWORD, Namespace.getNamespace(NAMESPACE));
        }

        if (serviceIdentification.getKeywords() != null) {
            String[] keywordsArray = serviceIdentification.getKeywords().trim().split(";");
            for (String newKeyword : keywordsArray) {
                Element keyword = new Element(KEYWORD, Namespace.getNamespace(OWS, NAMESPACE)).setText(newKeyword);
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
        Element serviceProviderElement = getElement(root, SERVICE_PROVIDER);

        serviceProvider.setProviderName(getValue(serviceProviderElement, PROVIDER_NAME));

        // a special case, an attribute with a namespace
        serviceProvider
                .setProviderSite(serviceProviderElement.getChild(PROVIDER_SITE, Namespace.getNamespace(NAMESPACE))
                        .getAttributeValue(HREF, Namespace.getNamespace(NAMESPACE_XLINK)));

        // contact info
        Element serviceContact = getElement(serviceProviderElement, SERVICE_CONTACT);
        serviceProvider.setIndividualName(getValue(serviceContact, INDIVIDUAL_NAME));
        serviceProvider.setPosition(getValue(serviceContact, POSITION_NAME));

        // phone
        Element contactInfo = getElement(serviceContact, CONTACT_INFO);
        Element phone = getElement(contactInfo, PHONE);
        serviceProvider.setPhone(getValue(phone, VOICE));
        serviceProvider.setFacsimile(getValue(phone, FACSIMILE));

        // address
        Element address = getElement(contactInfo, ADDRESS);
        serviceProvider.setDeliveryPoint(getValue(address, DELIVERY_POINT));
        serviceProvider.setCity(getValue(address, CITY));
        serviceProvider.setAdministrativeArea(getValue(address, ADMINISTRATIVE_AREA));
        serviceProvider.setPostalCode(getValue(address, POSTAL_CODE));
        serviceProvider.setCountry(getValue(address, COUNTRY));
        serviceProvider.setEmail(getValue(address, ELECTRONIC_MAIL_ADDRESS));
        LOGGER.info("'{}' is parsed and a ServiceProvider object is returned", absolutePath);
        return serviceProvider;
    }

    @Override
    public void saveServiceProvider(ServiceProvider serviceProvider) {
        Document document = null;
        String absolutePath = resourcePathUtil.getWebAppResourcePath(FILE_NAME);
        document = jDomUtil.parse(absolutePath);
        Element root = document.getRootElement();
        Element serviceProviderElement = getElement(root, SERVICE_PROVIDER);

        setElement(getElement(serviceProviderElement, PROVIDER_NAME), serviceProvider.getProviderName());
        getElement(serviceProviderElement, PROVIDER_SITE).setAttribute(HREF, serviceProvider.getProviderSite(),
                Namespace.getNamespace("xlink", NAMESPACE_XLINK));

        Element serviceContact = getElement(serviceProviderElement, SERVICE_CONTACT);
        setElement(getElement(serviceContact, INDIVIDUAL_NAME), serviceProvider.getIndividualName());
        setElement(getElement(serviceContact, POSITION_NAME), serviceProvider.getPosition());

        Element contactInfo = getElement(serviceContact, CONTACT_INFO);
        Element phone = getElement(contactInfo, PHONE);
        setElement(getElement(phone, VOICE), serviceProvider.getPhone());
        setElement(getElement(phone, FACSIMILE), serviceProvider.getFacsimile());

        Element address = getElement(contactInfo, ADDRESS);
        setElement(getElement(address, DELIVERY_POINT), serviceProvider.getDeliveryPoint());
        setElement(getElement(address, CITY), serviceProvider.getCity());
        setElement(getElement(address, ADMINISTRATIVE_AREA), serviceProvider.getAdministrativeArea());
        setElement(getElement(address, POSTAL_CODE), serviceProvider.getPostalCode());
        setElement(getElement(address, COUNTRY), serviceProvider.getCountry());
        setElement(getElement(address, ELECTRONIC_MAIL_ADDRESS), serviceProvider.getEmail());
        jDomUtil.write(document, absolutePath);
        LOGGER.info("ServiceProvider values written to '{}'", absolutePath);
    }

    private String getValue(Element element,
            String child) {
        if (element != null) {
            Element childElement = element.getChild(child, Namespace.getNamespace(NAMESPACE));
            if (childElement != null) {
                return childElement.getValue();
            }
        }
        return null;
    }

    private Element getElement(Element element,
            String child) {
        if (element != null) {
            return element.getChild(child, Namespace.getNamespace(NAMESPACE));
        }
        return null;
    }

    private void setElement(Element element,
            String value) {
        if (element != null) {
            element.setText(value);
        }
    }
}
