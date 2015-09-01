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

import java.util.Arrays;
import java.util.List;

import org.n52.wps.webapp.api.AlgorithmEntry;
import org.n52.wps.webapp.api.ConfigurationCategory;
import org.n52.wps.webapp.api.ConfigurationKey;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.n52.wps.webapp.api.FormatEntry;
import org.n52.wps.webapp.api.types.BooleanConfigurationEntry;
import org.n52.wps.webapp.api.types.ConfigurationEntry;
import org.n52.wps.webapp.api.types.DoubleConfigurationEntry;
import org.n52.wps.webapp.api.types.IntegerConfigurationEntry;
import org.n52.wps.webapp.api.types.StringConfigurationEntry;

/**
 * A {@link ConfigurationModule} implementation. This configuration module is used to configure the server.
 */
public class Server implements ConfigurationModule {

	private ConfigurationEntry<String> hostnameEntry = new StringConfigurationEntry("hostname", "Server Host Name", "",
			true, "localhost");	
	private ConfigurationEntry<String> protocolEntry = new StringConfigurationEntry("protocol", "Server protocol", "",
			true, "http");
	private ConfigurationEntry<Integer> hostportEntry = new IntegerConfigurationEntry("hostport", "Server Host Port",
			"", true, 8080);
	private ConfigurationEntry<Boolean> includeDataInputsInResponseEntry = new BooleanConfigurationEntry(
			"data_inputs_in_response", "Include Data Inputs", "", true, false);
	private ConfigurationEntry<Integer> computationTimeoutEntry = new IntegerConfigurationEntry("computation_timeout",
			"Computation Timeout", "In milli seconds", true, 5);
	private ConfigurationEntry<Boolean> cacheCapabilitesEntry = new BooleanConfigurationEntry("cache_capabilites",
			"Cache Capabilities", "", true, false);
	private ConfigurationEntry<String> weppappPathEntry = new StringConfigurationEntry("weppapp_path", "Webapp Path",
			"", true, "wps");
	private ConfigurationEntry<Double> repoReloadIntervalEntry = new DoubleConfigurationEntry("repo_reload_interval",
			"Repo Reload Interval", "(In hours. 0 = No Auto Reload)", true, 0.0);
	private ConfigurationEntry<Boolean> responseURLFilterEnabledEntry = new BooleanConfigurationEntry(
			"response_url_filter_enabled", "Response URL Filter Enabled", "", true, false);
	private ConfigurationEntry<Integer> minPoolSizeEntry = new IntegerConfigurationEntry("min_pool_size", "Minimum thread pool size",
			"Request executor core thread pool size", true, 10);
	private ConfigurationEntry<Integer> maxPoolSizeEntry = new IntegerConfigurationEntry("max_pool_size", "Maxmum thread pool size",
			"Request executor maximum thread pool size", true, 20);
	private ConfigurationEntry<Integer> keepAliveSecondsEntry = new IntegerConfigurationEntry("keep_alive_seconds", "Keep alive seconds",
			"Maximum time that excess idle threads are kept alive", true, 1000);
	private ConfigurationEntry<Integer> maxQueuedTasksEntry = new IntegerConfigurationEntry("max_queued_tasks", "Maximum queued tasks",
			"Maximum queued tasks of the work queue", true, 100);

	private List<? extends ConfigurationEntry<?>> configurationEntries = Arrays.asList(protocolEntry, hostnameEntry, hostportEntry,
			computationTimeoutEntry, weppappPathEntry, repoReloadIntervalEntry, includeDataInputsInResponseEntry,
			cacheCapabilitesEntry, responseURLFilterEnabledEntry, minPoolSizeEntry, maxPoolSizeEntry, keepAliveSecondsEntry, maxQueuedTasksEntry);

	private String hostname;
	private String protocol;
	private int hostport;
	private boolean includeDataInputsInResponse;
	private int computationTimeout;
	private boolean cacheCapabilites;
	private String webappPath;
	private double repoReloadInterval;
	private boolean responseURLFilterEnabled;
	private int minPoolSize;
	private int maxPoolSize;
	private int keepAliveSeconds;
	private int maxQueuedTasks;

    public Server() {
        //
    }

    public Server(String protocol, String hostname, int hostport, String webappPath) {
        super();
        this.protocol = protocol;
        this.hostname = hostname;
        this.hostport = hostport;
        this.webappPath = webappPath;
    }

    @Override
	public String getModuleName() {
		return "Server Configuration";
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

	public String getHostname() {
		return hostname;
	}

	@ConfigurationKey(key = "hostname")
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getHostport() {
		return hostport;
	}

	@ConfigurationKey(key = "hostport")
	public void setHostport(int hostport) {
		this.hostport = hostport;
	}

	public String getProtocol() {
		return protocol;
	}
	
	@ConfigurationKey(key = "protocol")
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public boolean isIncludeDataInputsInResponse() {
		return includeDataInputsInResponse;
	}

	@ConfigurationKey(key = "data_inputs_in_response")
	public void setIncludeDataInputsInResponse(boolean includeDataInputsInResponse) {
		this.includeDataInputsInResponse = includeDataInputsInResponse;
	}

	public int getComputationTimeout() {
		return computationTimeout;
	}

	@ConfigurationKey(key = "computation_timeout")
	public void setComputationTimeout(int computationTimeout) {
		this.computationTimeout = computationTimeout;
	}

	public boolean isCacheCapabilites() {
		return cacheCapabilites;
	}

	@ConfigurationKey(key = "cache_capabilites")
	public void setCacheCapabilites(boolean cacheCapabilites) {
		this.cacheCapabilites = cacheCapabilites;
	}

	public String getWebappPath() {
		return webappPath;
	}

	@ConfigurationKey(key = "weppapp_path")
	public void setWebappPath(String webappPath) {
		this.webappPath = webappPath;
	}

	public double getRepoReloadInterval() {
		return repoReloadInterval;
	}

	@ConfigurationKey(key = "repo_reload_interval")
	public void setRepoReloadInterval(double repoReloadInterval) {
		this.repoReloadInterval = repoReloadInterval;
	}

	public boolean isResponseURLFilterEnabled() {
		return responseURLFilterEnabled;
	}

	@ConfigurationKey(key = "response_url_filter_enabled")
	public void setResponseURLFilterEnabled(boolean responseURLFilterEnabled) {
		this.responseURLFilterEnabled = responseURLFilterEnabled;
	}

	@Override
	public List<FormatEntry> getFormatEntries() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getMinPoolSize() {
		return minPoolSize;
	}
	
	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public int getKeepAliveSeconds() {
		return keepAliveSeconds;
	}

	public int getMaxQueuedTasks() {
		return maxQueuedTasks;
	}

	@ConfigurationKey(key = "min_pool_size")
	public void setMinPoolSize(int minPoolSize) {
		this.minPoolSize = minPoolSize;
	}

	@ConfigurationKey(key = "max_pool_size")
	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	@ConfigurationKey(key = "keep_alive_seconds")
	public void setKeepAliveSeconds(int keepAliveSeconds) {
		this.keepAliveSeconds = keepAliveSeconds;
	}

	@ConfigurationKey(key = "max_queued_tasks")
	public void setMaxQueuedTasks(int maxQueuedTasks) {
		this.maxQueuedTasks = maxQueuedTasks;
	}

}
