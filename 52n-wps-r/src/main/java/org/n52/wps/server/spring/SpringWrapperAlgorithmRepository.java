/**
 * ﻿Copyright (C) 2010 - 2014 52°North Initiative for Geospatial Open Source
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

package org.n52.wps.server.spring;

import org.n52.wps.server.ITransactionalAlgorithmRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 
 * A wrapper for @ITransactionalAlgorithmRepository which are managed by Spring.
 * 
 * @author Daniel Nüst
 *
 */
@Component
public class SpringWrapperAlgorithmRepository extends AbstractWrapperAlgorithmRepository implements
        ApplicationContextAware {

    private static Logger LOGGER = LoggerFactory.getLogger(SpringWrapperAlgorithmRepository.class);

    public SpringWrapperAlgorithmRepository() {
        LOGGER.info("NEW {}", this);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        LOGGER.debug("Receiving application context: {}", applicationContext);

        if ( !isActive())
            return;

        try {
            Class< ? > repositoryClass = getWrappeeClass();
            Component componentAnnotation = repositoryClass.getAnnotation(Component.class);
            if (componentAnnotation != null) {
                String componentName = componentAnnotation.value();
                Object bean = applicationContext.getBean(componentName);
                if (bean instanceof ITransactionalAlgorithmRepository) {
                    ITransactionalAlgorithmRepository repo = (ITransactionalAlgorithmRepository) bean;
                    LOGGER.debug("Loaded repository from context: {}", repo);
                    setWrappedRepository(repo);
                }
            }
        }
        catch (ClassNotFoundException e) {
            LOGGER.error("Problem wrapping algorithm repository with name '{}'", getWrappeeName(), e);
        }
    }

}
