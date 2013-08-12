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

import java.util.Arrays;
import java.util.List;

import org.n52.wps.webapp.api.AlgorithmEntry;
import org.n52.wps.webapp.api.ConfigurationCategory;
import org.n52.wps.webapp.api.ConfigurationKey;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.n52.wps.webapp.api.types.ConfigurationEntry;
import org.n52.wps.webapp.api.types.StringConfigurationEntry;

public class ServiceIdentification implements ConfigurationModule {
	private ConfigurationEntry<String> titleEntry = new StringConfigurationEntry("title", "Title", "The title of the service", true,
			"52°North WPS ${version}");
	private ConfigurationEntry<String> serviceAbstractEntry = new StringConfigurationEntry("service_abstract",
			"Abstract", "A brief description of the service", true, "Service based on the 52°North implementation of WPS 1.0.0");
	private ConfigurationEntry<String> keywordsEntry = new StringConfigurationEntry("keywords", "Keywords", "Separated by a semicolon ';'", true,
			"52°North WPS ${version}");
	private ConfigurationEntry<String> serviceTypeEntry = new StringConfigurationEntry("service_type", "Service Type",
			"", true, "WPS");
	private ConfigurationEntry<String> serviceTypeVersionEntry = new StringConfigurationEntry("service_type_version",
			"Service Type Version", "", true, "1.0.0");
	private ConfigurationEntry<String> feesEntry = new StringConfigurationEntry("fees", "Fees", "", true, "NONE");
	private ConfigurationEntry<String> accessConstraintsEntry = new StringConfigurationEntry("access_constraints",
			"Access Constraints", "", true, "NONE");

	private List<? extends ConfigurationEntry<?>> configurationEntries = Arrays.asList(titleEntry,
			serviceAbstractEntry, serviceTypeEntry, serviceTypeVersionEntry, keywordsEntry, feesEntry,
			accessConstraintsEntry);

	private String title;
	private String serviceAbstract;
	private String[] keywords;
	private String serviceType;
	private String serviceTypeVersion;
	private String fees;
	private String accessConstraints;

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

	public String getTitle() {
		return title;
	}

	@ConfigurationKey(key = "title")
	public void setTitle(String title) {
		this.title = title;
	}

	public String getServiceAbstract() {
		return serviceAbstract;
	}

	@ConfigurationKey(key = "service_abstract")
	public void setServiceAbstract(String serviceAbstract) {
		this.serviceAbstract = serviceAbstract;
	}

	public String[] getKeywords() {
		return keywords;
	}

	@ConfigurationKey(key = "keywords")
	public void setKeywords(String keywords) {
		this.keywords = keywords.split(";");
		;
	}

	public String getServiceType() {
		return serviceType;
	}

	@ConfigurationKey(key = "service_type")
	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getServiceTypeVersion() {
		return serviceTypeVersion;
	}

	@ConfigurationKey(key = "service_type_version")
	public void setServiceTypeVersion(String serviceTypeVersion) {
		this.serviceTypeVersion = serviceTypeVersion;
	}

	public String getFees() {
		return fees;
	}

	@ConfigurationKey(key = "fees")
	public void setFees(String fees) {
		this.fees = fees;
	}

	public String getAccessConstraints() {
		return accessConstraints;
	}

	@ConfigurationKey(key = "access_constraints")
	public void setAccessConstraints(String accessConstraints) {
		this.accessConstraints = accessConstraints;
	}

}
