/**
 * Copyright (C) 2013 - 2014 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.matlab.description;


import java.util.Collection;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.n52.matlab.connector.client.MatlabClient;

import com.github.autermann.wps.commons.description.impl.ProcessDescriptionImpl;
import com.github.autermann.wps.commons.description.ows.OwsCodeType;
import com.github.autermann.wps.commons.description.xml.ProcessDescriptionEncoder;
import com.github.autermann.yaml.YamlNode;
import com.google.common.base.Supplier;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class MatlabProcessDescription extends ProcessDescriptionImpl {
    private final String function;
    private final Supplier<MatlabClient> clientProvider;
    private final ProcessDescriptionType processDescription;

    private MatlabProcessDescription(AbstractMatlabProcessDescriptionBuilder<?, ?> builder) {
        super(builder);
        this.function = builder.getFunction();
        this.clientProvider = builder.getClientProvider();
        this.processDescription = new ProcessDescriptionEncoder().encodeProcessDescription(this);
    }

    public Supplier<MatlabClient> getClientProvider() {
        return clientProvider;
    }

    public String getFunction() {
        return function;
    }

    public ProcessDescriptionType getProcessDescription() {
        return processDescription;
    }

    @Override
    public MatlabProcessOutputDescription getOutput(OwsCodeType id) {
        return (MatlabProcessOutputDescription) super.getOutput(id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<MatlabProcessOutputDescription> getOutputDescriptions() {
        return (Collection<MatlabProcessOutputDescription>) super.getOutputDescriptions();
    }

    @Override
    public MatlabProcessInputDescription getInput(OwsCodeType id) {
        return (MatlabProcessInputDescription) super.getInput(id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<MatlabProcessInputDescription> getInputDescriptions() {
        return (Collection<MatlabProcessInputDescription>) super.getInputDescriptions();
    }

    public static MatlabProcessDescription load(YamlNode definition) {
        return new MatlabDescriptionGenerator()
                .createProcessDescription(definition);
    }

    public static MatlabProcessDescriptionBuilder<?,?> builder() {
        return new BuilderImpl();
    }


    private static class BuilderImpl extends AbstractMatlabProcessDescriptionBuilder<MatlabProcessDescription, BuilderImpl> {
        @Override
        public MatlabProcessDescription build() {
            return new MatlabProcessDescription(this);
        }
    }

}
