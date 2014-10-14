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

import java.util.ArrayList;
import java.util.Collection;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.RepositoryDocument.Repository;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.ITransactionalAlgorithmRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * A wrapper class for {@link ITransactionalAlgorithmRepository} which is managed by some other framework.
 * this class can be instantiated by the WPS's @RepositoryManager and the other framework can then inject the
 * actual repository afterwards into a static variable. An example configuration might look like this:
 * 
 * <pre>
 * {@code
 * <Repository name="RAlgorithmRepository" className="org.n52.wps.server.spring.SpringWrapperAlgorithmRepository" active="true">
 *      <Property active="true" name="wrappeeName">org.n52.wps.server.r.LocalRAlgorithmRepository</Property>
 * </Repository>
 * }
 * </pre>
 * 
 * The wrapped repository must be inactive, otherwise it will be loaded by @RepositoryManager as well.
 * 
 * @author Daniel Nüst
 *
 */
public class AbstractWrapperAlgorithmRepository implements ITransactionalAlgorithmRepository {

    private static Logger LOGGER = LoggerFactory.getLogger(AbstractWrapperAlgorithmRepository.class);

    public static final String WRAPPEE_NAME = "wrappeeName";

    private static ITransactionalAlgorithmRepository wrappedRepository = null;

    private WPSConfig config;

    public AbstractWrapperAlgorithmRepository() {
        config = WPSConfig.getInstance();
    }

    @Override
    public Collection<String> getAlgorithmNames() {
        if (wrappedRepository != null)
            return wrappedRepository.getAlgorithmNames();

        return new ArrayList<String>();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ClassicRAlgorithmRepository [");
        if (wrappedRepository != null)
            builder.append("wrappedRepository=").append(wrappedRepository);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public IAlgorithm getAlgorithm(String processID) {
        if (wrappedRepository != null)
            return wrappedRepository.getAlgorithm(processID);

        return null;
    }

    @Override
    public ProcessDescriptionType getProcessDescription(String processID) {
        if (wrappedRepository != null)
            return wrappedRepository.getProcessDescription(processID);

        return null;
    }

    @Override
    public boolean containsAlgorithm(String processID) {
        if (wrappedRepository != null)
            return wrappedRepository.containsAlgorithm(processID);

        return false;
    }

    @Override
    public void shutdown() {
        if (wrappedRepository != null)
            wrappedRepository.shutdown();
    }

    @Override
    public boolean addAlgorithm(Object processID) {
        if (wrappedRepository != null)
            return wrappedRepository.addAlgorithm(processID);

        return false;
    }

    @Override
    public boolean removeAlgorithm(Object processID) {
        if (wrappedRepository != null)
            return wrappedRepository.removeAlgorithm(processID);

        return false;
    }

    protected boolean isActive() {
        String className = this.getClass().getCanonicalName();
        WPSConfig conf = WPSConfig.getInstance();
        if ( !conf.isRepositoryActive(className)) {
            LOGGER.debug("{} is inactive.", className);
            return false;
        }
        return true;
    }

    public static boolean wrapperRepositoryActiveAndConfiguredForRepo(ITransactionalAlgorithmRepository repo,
                                                                      Repository[] registeredAlgorithmRepositories) {
        for (Repository repository : registeredAlgorithmRepositories) {
            if (repository.getActive()) {
                Property[] propertyArray = repository.getPropertyArray();
                for (Property property : propertyArray) {
                    if (property.getActive() && property.getName().equals(WRAPPEE_NAME)) {
                        String propValue = property.getStringValue();
                        String className = repo.getClass().getName();
                        if (propValue.equals(className)) {
                            LOGGER.info("Wrapper for {} found: {} (class: {})",
                                        className,
                                        repository.getName(),
                                        repository.getClass());
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    protected Class< ? > getWrappeeClass() throws ClassNotFoundException {
        String wrappeeName = getWrappeeName();
        Class< ? > repositoryClass = AbstractWrapperAlgorithmRepository.class.getClassLoader().loadClass(wrappeeName);
        return repositoryClass;
    }

    protected String getWrappeeName() {
        String name = null;
        Property[] properties = config.getPropertiesForRepositoryClass(this.getClass().getName());
        for (Property property : properties) {
            if (property.getActive() && property.getName().equals(WRAPPEE_NAME))
                name = property.getStringValue();
        }
        LOGGER.debug("Wrappee name loaded from config: '{}'", name);
        return name;
    }

    protected static void setWrappedRepository(ITransactionalAlgorithmRepository wrappee) {
        LOGGER.info("Wrapping {}", wrappee);
        AbstractWrapperAlgorithmRepository.wrappedRepository = wrappee;
    }

}
