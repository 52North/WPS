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

/**
 * Holds parsed service provider values.
 * 
 * @see CapabilitiesService
 * @see CapabilitiesDAO
 */
public class ServiceProvider  {

	private final String blankErrorMessage = "Field cannot be blank.";

	@NotBlank(message = blankErrorMessage)
	private String providerName;
	
	@NotBlank(message = blankErrorMessage)
	private String providerSite;
	
	@NotBlank(message = blankErrorMessage)
	private String individualName;
	
	@NotBlank(message = blankErrorMessage)
	private String position;
	
	@NotBlank(message = blankErrorMessage)
	private String phone;
	
	@NotBlank(message = blankErrorMessage)
	private String facsimile;
	
	@NotBlank(message = blankErrorMessage)
	private String deliveryPoint;
	
	@NotBlank(message = blankErrorMessage)
	private String city;
	
	@NotBlank(message = blankErrorMessage)
	private String administrativeArea;
	
	@NotBlank(message = blankErrorMessage)
	private String postalCode;
	
	@NotBlank(message = blankErrorMessage)
	private String country;
	
	@NotBlank(message = blankErrorMessage)
	private String email;
	
	public String getProviderName() {
		return providerName;
	}

	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}

	public String getProviderSite() {
		return providerSite;
	}

	public void setProviderSite(String providerSite) {
		this.providerSite = providerSite;
	}

	public String getIndividualName() {
		return individualName;
	}

	public void setIndividualName(String individualName) {
		this.individualName = individualName;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getFacsimile() {
		return facsimile;
	}

	public void setFacsimile(String facsimile) {
		this.facsimile = facsimile;
	}

	public String getDeliveryPoint() {
		return deliveryPoint;
	}

	public void setDeliveryPoint(String deliveryPoint) {
		this.deliveryPoint = deliveryPoint;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getAdministrativeArea() {
		return administrativeArea;
	}

	public void setAdministrativeArea(String administrativeArea) {
		this.administrativeArea = administrativeArea;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
