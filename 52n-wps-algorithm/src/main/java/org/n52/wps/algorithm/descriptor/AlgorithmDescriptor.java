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
package org.n52.wps.algorithm.descriptor;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author tkunicki
 */
public class AlgorithmDescriptor extends Descriptor {

    private final String version;
    private final boolean storeSupported;
    private final boolean statusSupported;
    private final Map<String, InputDescriptor> inputDescriptorMap;
    private final Map<String, OutputDescriptor> outputDescriptorMap;

	AlgorithmDescriptor(Builder<? extends Builder<?>> builder) {
        super(builder);
        this.version = builder.version;
        this.storeSupported = builder.storeSupported;
        this.statusSupported = builder.statusSupported;

        Preconditions.checkState(
                builder.outputDescriptors.size() > 0,
                "Need at minimum 1 output for algorithm.");
        
        // LinkedHaskMap to preserve order
        Map<String, InputDescriptor> iMap = new LinkedHashMap<String, InputDescriptor>();
        for (InputDescriptor iDescriptor : builder.inputDescriptors) {
            iMap.put(iDescriptor.getIdentifier(), iDescriptor);
        }
        inputDescriptorMap = Collections.unmodifiableMap(iMap);

        Map<String, OutputDescriptor> oMap = new LinkedHashMap<String, OutputDescriptor>();
        for (OutputDescriptor oDescriptor : builder.outputDescriptors) {
            oMap.put(oDescriptor.getIdentifier(), oDescriptor);
        }
        outputDescriptorMap = Collections.unmodifiableMap(oMap);
    }

    public String getVersion() {
        return version;
    }

    public boolean getStoreSupported() {
        return storeSupported;
    }

    public boolean getStatusSupported() {
        return statusSupported;
    }

    public List<String> getInputIdentifiers() {
        return Collections.unmodifiableList(new ArrayList<String>(inputDescriptorMap.keySet()));
    }

    public InputDescriptor getInputDescriptor(String identifier) {
        return inputDescriptorMap.get(identifier);
    }

    public Collection<InputDescriptor> getInputDescriptors() {
        return inputDescriptorMap.values();
    }

    public List<String> getOutputIdentifiers() {
        return Collections.unmodifiableList(new ArrayList<String>(outputDescriptorMap.keySet()));
    }

    public OutputDescriptor getOutputDescriptor(String identifier) {
        return outputDescriptorMap.get(identifier);
    }

    public Collection<OutputDescriptor> getOutputDescriptors() {
        return outputDescriptorMap.values();
    }

    public static Builder<?> builder(String identifier) {
        return new BuilderTyped(identifier);
    }

    public static Builder<?> builder(Class<?> clazz) {
        Preconditions.checkNotNull(clazz, "clazz may not be null");
        return new BuilderTyped(clazz.getCanonicalName());
    }

    private static class BuilderTyped extends Builder<BuilderTyped> {
        public BuilderTyped(String identifier) {
            super(identifier);
        }
        @Override
        protected BuilderTyped self() {
            return this;
        }
    }

    public static abstract class Builder<B extends Builder<B>> extends Descriptor.Builder<B>{

        private String version = "1.0.0";
        private boolean storeSupported = true;
        private boolean statusSupported = true;
        private List<InputDescriptor> inputDescriptors;
        private List<OutputDescriptor> outputDescriptors;

        protected Builder(String identifier) {
            super(identifier);
            title(identifier);
            inputDescriptors = new ArrayList<InputDescriptor>();
            outputDescriptors = new ArrayList<OutputDescriptor>();
        }

        public B version(String version) {
            this.version = version;
            return self();
        }

        public B storeSupported(boolean storeSupported) {
            this.storeSupported = storeSupported;
            return self();
        }

        public B statusSupported(boolean statusSupported) {
            this.statusSupported = statusSupported;
            return self();
        }

        public B addInputDescriptor(InputDescriptor.Builder inputDescriptorBuilder) {
            return addInputDescriptor(inputDescriptorBuilder.build());
        }

        public B addInputDescriptor(InputDescriptor inputDescriptor) {
            this.inputDescriptors.add(inputDescriptor);
            return self();
        }

        public B addInputDescriptors(List<? extends InputDescriptor> inputDescriptors) {
            this.inputDescriptors.addAll(inputDescriptors);
            return self();
        }

        public B addOutputDescriptor(OutputDescriptor.Builder outputDescriptorBuilder) {
            return addOutputDescriptor(outputDescriptorBuilder.build());
        }

        public B addOutputDescriptor(OutputDescriptor outputDescriptor) {
            this.outputDescriptors.add(outputDescriptor);
            return self();
        }

        public B addOutputDescriptors(List<? extends OutputDescriptor> outputDescriptors) {
            this.outputDescriptors.addAll(outputDescriptors);
            return self();
        }
        
        public AlgorithmDescriptor build() {
            return new AlgorithmDescriptor(this);
        }

    }
    
}
