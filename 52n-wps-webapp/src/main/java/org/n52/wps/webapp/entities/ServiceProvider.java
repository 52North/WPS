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

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.n52.wps.webapp.api.AlgorithmEntry;
import org.n52.wps.webapp.api.ConfigurationCategory;
import org.n52.wps.webapp.api.ConfigurationKey;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.n52.wps.webapp.api.types.ConfigurationEntry;
import org.n52.wps.webapp.api.types.StringConfigurationEntry;
import org.n52.wps.webapp.api.types.URIConfigurationEntry;

public class ServiceProvider implements ConfigurationModule {
	private ConfigurationEntry<String> providerNameEntry = new StringConfigurationEntry("provider_name",
			"Provider Name", "Your or your company's name", true, "52North");
	private ConfigurationEntry<URI> providerSiteEntry = new URIConfigurationEntry("provider_site", "Provider Site",
			"Your website", true, URI.create("http://www.52north.org/"));
	private ConfigurationEntry<String> individualNameEntry = new StringConfigurationEntry("individual_name",
			"Responsible Person", "The name of the responsible person of this service", true, "");
	private ConfigurationEntry<String> positionEntry = new StringConfigurationEntry("position", "Position",
			"The position of the responsible person", true, "");
	private ConfigurationEntry<String> phoneEntry = new StringConfigurationEntry("phone", "Phone",
			"The phone number of the responsible person", true, "");
	private ConfigurationEntry<String> facsimileEntry = new StringConfigurationEntry("facsimile", "Fax",
			"The fax number of the responsible person", true, "");
	private ConfigurationEntry<String> deliveryPointEntry = new StringConfigurationEntry("delivery_point",
			"Delivery Point", "The street address of the responsible person", true, "");
	private ConfigurationEntry<String> cityEntry = new StringConfigurationEntry("city", "City",
			"The city the responsible person", true, "");
	private ConfigurationEntry<String> administrativeAreaEntry = new StringConfigurationEntry("administrative_area",
			"Administrative Area", "The administrative area of the responsible person", true, "");
	private ConfigurationEntry<String> postalCodeEntry = new StringConfigurationEntry("postal_code", "Postal Code",
			"The postal code of the responsible person", true, "");
	private ConfigurationEntry<String> countryEntry = new StringConfigurationEntry("country", "Country",
			"The country the responsible person", true, "");
	private ConfigurationEntry<String> emailEntry = new StringConfigurationEntry("email", "Email",
			"The e-mail address of the responsible person", true, "");

	private List<? extends ConfigurationEntry<?>> configurationEntries = Arrays.asList(providerNameEntry,
			providerSiteEntry, individualNameEntry, positionEntry, phoneEntry, emailEntry, facsimileEntry,
			deliveryPointEntry, cityEntry, administrativeAreaEntry, postalCodeEntry, countryEntry);

	private String providerName;
	private String providerSite;
	private String individualName;
	private String position;
	private String phone;
	private String facsimile;
	private String deliveryPoint;
	private String city;
	private String administrativeArea;
	private String postalCode;
	private String country;
	private String email;

	@Override
	public String getModuleName() {
		return "Service Identification Settings";
	}

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public void setActive(boolean active) {

	}

	@Override
	public ConfigurationCategory getCategory() {
		return ConfigurationCategory.GENERAL;
	}

	@Override
	public List<? extends ConfigurationEntry<?>> getConfigurationEntries() {
		return configurationEntries;
	}

	@Override
	public List<AlgorithmEntry> getAlgorithmEntries() {
		return null;
	}

	public String getProviderName() {
		return providerName;
	}

	@ConfigurationKey(key = "provider_name")
	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}

	public String getProviderSite() {
		return providerSite;
	}

	@ConfigurationKey(key = "provider_site")
	public void setProviderSite(String providerSite) {
		this.providerSite = providerSite;
	}

	public String getIndividualName() {
		return individualName;
	}

	@ConfigurationKey(key = "individual_name")
	public void setIndividualName(String individualName) {
		this.individualName = individualName;
	}

	public String getPosition() {
		return position;
	}

	@ConfigurationKey(key = "position")
	public void setPosition(String position) {
		this.position = position;
	}

	public String getPhone() {
		return phone;
	}

	@ConfigurationKey(key = "phone")
	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getFacsimile() {
		return facsimile;
	}

	@ConfigurationKey(key = "facsimile")
	public void setFacsimile(String facsimile) {
		this.facsimile = facsimile;
	}

	public String getDeliveryPoint() {
		return deliveryPoint;
	}

	@ConfigurationKey(key = "delivery_point")
	public void setDeliveryPoint(String deliveryPoint) {
		this.deliveryPoint = deliveryPoint;
	}

	public String getCity() {
		return city;
	}

	@ConfigurationKey(key = "city")
	public void setCity(String city) {
		this.city = city;
	}

	public String getAdministrativeArea() {
		return administrativeArea;
	}

	@ConfigurationKey(key = "administrative_area")
	public void setAdministrativeArea(String administrativeArea) {
		this.administrativeArea = administrativeArea;
	}

	public String getPostalCode() {
		return postalCode;
	}

	@ConfigurationKey(key = "postal_code")
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getCountry() {
		return country;
	}

	@ConfigurationKey(key = "country")
	public void setCountry(String country) {
		this.country = country;
	}

	public String getEmail() {
		return email;
	}

	@ConfigurationKey(key = "email")
	public void setEmail(String email) {
		this.email = email;
	}
}
