/**
 * ﻿Copyright (C) 2009 - 2014 52°North Initiative for Geospatial Open Source
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
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.3-hudson-jaxb-ri-2.2.3-3-
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2011.02.08 at 05:49:57 PM MEZ
//


package org.n52.wps.ags.algorithmpackage;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="workspaceLocation" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *         &lt;element name="algorithmLocation" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *         &lt;element name="containerType" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *         &lt;element name="requiredRuntimeComponent" type="{http://www.w3.org/2001/XMLSchema}anyURI" maxOccurs="unbounded"/>
 *         &lt;element name="algorithmParameters">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="parameter" type="{}AlgorithmParameterType" maxOccurs="unbounded"/>
 *                   &lt;element name="separatorString" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="sequential" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "workspaceLocation",
    "algorithmLocation",
    "containerType",
    "requiredRuntimeComponent",
    "algorithmParameters"
})
@XmlRootElement(name = "AlgorithmDescription")
public class AlgorithmDescription {

    @XmlElement(required = true)
    @XmlSchemaType(name = "anyURI")
    protected String workspaceLocation;
    @XmlElement(required = true)
    @XmlSchemaType(name = "anyURI")
    protected String algorithmLocation;
    @XmlElement(required = true)
    @XmlSchemaType(name = "anyURI")
    protected String containerType;
    @XmlElement(required = true)
    @XmlSchemaType(name = "anyURI")
    protected List<String> requiredRuntimeComponent;
    @XmlElement(required = true)
    protected AlgorithmDescription.AlgorithmParameters algorithmParameters;

    /**
     * Gets the value of the workspaceLocation property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getWorkspaceLocation() {
        return workspaceLocation;
    }

    /**
     * Sets the value of the workspaceLocation property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setWorkspaceLocation(String value) {
        this.workspaceLocation = value;
    }

    /**
     * Gets the value of the algorithmLocation property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getAlgorithmLocation() {
        return algorithmLocation;
    }

    /**
     * Sets the value of the algorithmLocation property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setAlgorithmLocation(String value) {
        this.algorithmLocation = value;
    }

    /**
     * Gets the value of the containerType property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getContainerType() {
        return containerType;
    }

    /**
     * Sets the value of the containerType property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setContainerType(String value) {
        this.containerType = value;
    }

    /**
     * Gets the value of the requiredRuntimeComponent property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the requiredRuntimeComponent property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRequiredRuntimeComponent().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     *
     *
     */
    public List<String> getRequiredRuntimeComponent() {
        if (requiredRuntimeComponent == null) {
            requiredRuntimeComponent = new ArrayList<String>();
        }
        return this.requiredRuntimeComponent;
    }

    /**
     * Gets the value of the algorithmParameters property.
     *
     * @return
     *     possible object is
     *     {@link AlgorithmDescription.AlgorithmParameters }
     *
     */
    public AlgorithmDescription.AlgorithmParameters getAlgorithmParameters() {
        return algorithmParameters;
    }

    /**
     * Sets the value of the algorithmParameters property.
     *
     * @param value
     *     allowed object is
     *     {@link AlgorithmDescription.AlgorithmParameters }
     *
     */
    public void setAlgorithmParameters(AlgorithmDescription.AlgorithmParameters value) {
        this.algorithmParameters = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="parameter" type="{}AlgorithmParameterType" maxOccurs="unbounded"/>
     *         &lt;element name="separatorString" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *       &lt;/sequence>
     *       &lt;attribute name="sequential" type="{http://www.w3.org/2001/XMLSchema}boolean" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "parameter",
        "separatorString"
    })
    public static class AlgorithmParameters {

        @XmlElement(required = true)
        protected List<AlgorithmParameterType> parameter;
        protected String separatorString;
        @XmlAttribute(name = "sequential")
        protected Boolean sequential;

        /**
         * Gets the value of the parameter property.
         *
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the parameter property.
         *
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getParameter().add(newItem);
         * </pre>
         *
         *
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link AlgorithmParameterType }
         *
         *
         */
        public List<AlgorithmParameterType> getParameter() {
            if (parameter == null) {
                parameter = new ArrayList<AlgorithmParameterType>();
            }
            return this.parameter;
        }

        /**
         * Gets the value of the separatorString property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getSeparatorString() {
            return separatorString;
        }

        /**
         * Sets the value of the separatorString property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setSeparatorString(String value) {
            this.separatorString = value;
        }

        /**
         * Gets the value of the sequential property.
         *
         * @return
         *     possible object is
         *     {@link Boolean }
         *
         */
        public Boolean isSequential() {
            return sequential;
        }

        /**
         * Sets the value of the sequential property.
         *
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *
         */
        public void setSequential(Boolean value) {
            this.sequential = value;
        }

    }

}