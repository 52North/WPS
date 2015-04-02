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
package org.n52.wps.webapp.entities;

import org.hibernate.validator.constraints.NotBlank;

/**
 * Holds parsed service identification values.
 * 
 * @see CapabilitiesService
 * @see CapabilitiesDAO
 */
public class ServiceIdentification {

	private final String blankErrorMessage = "Field cannot be blank.";

	private String title;

	private String serviceAbstract;

	private String keywords;

	@NotBlank(message = blankErrorMessage)
	private String serviceType;

	@NotBlank(message = blankErrorMessage)
	private String serviceTypeVersions;

	private String fees;

	private String accessConstraints;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getServiceAbstract() {
		return serviceAbstract;
	}

	public void setServiceAbstract(String serviceAbstract) {
		this.serviceAbstract = serviceAbstract;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getServiceTypeVersions() {
		return serviceTypeVersions;
	}

	public void setServiceTypeVersions(String serviceTypeVersions) {
		this.serviceTypeVersions = serviceTypeVersions;
	}

	public String getFees() {
		return fees;
	}

	public void setFees(String fees) {
		this.fees = fees;
	}

	public String getAccessConstraints() {
		return accessConstraints;
	}

	public void setAccessConstraints(String accessConstraints) {
		this.accessConstraints = accessConstraints;
	}

}
