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

package org.n52.wps.webapp.entities;

import org.hibernate.validator.constraints.NotBlank;

public class ServiceIdentification {

	private final String blankErrorMessage = "Field cannot be blank.";

	@NotBlank(message = blankErrorMessage)
	private String title;

	@NotBlank(message = blankErrorMessage)
	private String serviceAbstract;

	private String keywords;

	@NotBlank(message = blankErrorMessage)
	private String serviceType;

	@NotBlank(message = blankErrorMessage)
	private String serviceTypeVersion;

	@NotBlank(message = blankErrorMessage)
	private String fees;

	@NotBlank(message = blankErrorMessage)
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

	public String getServiceTypeVersion() {
		return serviceTypeVersion;
	}

	public void setServiceTypeVersion(String serviceTypeVersion) {
		this.serviceTypeVersion = serviceTypeVersion;
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
