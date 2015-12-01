/**
 * Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.webadmin;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Paths;

import org.n52.wps.commons.WPSConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

@Controller
@RequestMapping(value = "/" + ConfigApi.ENDPOINT)
public class ConfigApi {

    public static final String ENDPOINT = "webAdmin";

    protected static Logger LOGGER = LoggerFactory.getLogger(ConfigApi.class);

    public ConfigApi() {
        LOGGER.info("NEW {}", this);
    }

    @RequestMapping(value = "/file", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> getConfig() throws MalformedURLException, IOException {
        String configPath = WPSConfig.getConfigPath();
        String configFileString = Resources.toString(Paths.get(configPath).toUri().toURL(), Charsets.UTF_8);
        ResponseEntity<String> entity = new ResponseEntity<String>(configFileString, HttpStatus.OK);
        return entity;
    }

    @RequestMapping(value = "/file", method = RequestMethod.POST)
    public ResponseEntity<String> setConfig(@RequestParam("serializedWPSConfiguraton") String formData) {
        LOGGER.debug("Incoming config file: {}", formData);
        ChangeConfigurationBean configurationBean = new ChangeConfigurationBean();
        configurationBean.setSerializedWPSConfiguraton(formData);
        ResponseEntity<String> entity = new ResponseEntity<String>("Saved config.", HttpStatus.OK);
        return entity;
    }

}
