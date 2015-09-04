/**
 * ﻿Copyright (C) 2013 - 2014 52°North Initiative for Geospatial Open Source
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

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.LinkedHashSet;

import org.n52.matlab.connector.MatlabException;
import org.n52.matlab.connector.client.MatlabClient;
import org.n52.matlab.connector.client.MatlabClientConfiguration;
import org.n52.matlab.connector.instance.MatlabInstanceConfiguration;
import org.n52.matlab.connector.instance.MatlabInstancePoolConfiguration;
import org.n52.wps.matlab.YamlConstants;
import org.n52.wps.matlab.transform.LiteralType;

import com.github.autermann.wps.commons.Format;
import com.github.autermann.wps.commons.description.InputOccurence;
import com.github.autermann.wps.commons.description.ProcessInputDescription;
import com.github.autermann.wps.commons.description.ProcessInputDescriptionBuilder;
import com.github.autermann.wps.commons.description.ProcessOutputDescription;
import com.github.autermann.wps.commons.description.ProcessOutputDescriptionBuilder;
import com.github.autermann.wps.commons.description.ows.OwsAllowedValue;
import com.github.autermann.wps.commons.description.ows.OwsAllowedValues;
import com.github.autermann.wps.commons.description.ows.OwsCRS;
import com.github.autermann.yaml.YamlNode;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public class MatlabDescriptionGenerator {
    //FIXME make this configurable
    private static final int LOCAL_INSTANCES = 5;

    private ProcessInputDescription createComplexInput(YamlNode definition) {
        final int maxOccurs;
        if (definition.path(YamlConstants.MAX_OCCURS).isText()) {
            if (definition.path(YamlConstants.MAX_OCCURS).asTextValue().equals("unbounded")) {
                maxOccurs = Integer.MAX_VALUE;
            } else {
                maxOccurs = 1;
            }
        } else {
            maxOccurs = definition.path(YamlConstants.MAX_OCCURS).asIntValue(1);
        }
        int minOccurs = definition.path(YamlConstants.MIN_OCCURS).asIntValue(1);

        String id = definition.path(YamlConstants.IDENTIFIER).textValue();
        if (id == null || id.isEmpty()) {
            throw new MatlabConfigurationException("Missing input identifier");
        }
        String abstrakt = definition.path(YamlConstants.ABSTRACT).asTextValue();
        String title = definition.path(YamlConstants.TITLE).asTextValue();
        YamlNode type = definition.path(YamlConstants.TYPE);
        if (!type.isMap()) {
            throw new MatlabConfigurationException("Missing type for output %s", id);
        }
        if (maxOccurs < 1 || minOccurs < 0 || maxOccurs < minOccurs) {
            throw new IllegalArgumentException(String
                    .format("Invalid min/max occurs: [%d,%d]", minOccurs, maxOccurs));
        }

        String mimeType = type.path(YamlConstants.MIME_TYPE).asTextValue();
        String schema = type.path(YamlConstants.SCHEMA).asTextValue();
        String encoding = type.path(YamlConstants.ENCODING).asTextValue();


        ProcessInputDescriptionBuilder<?,?> desc;

        if (mimeType != null && !mimeType.isEmpty()) {
            desc = MatlabComplexInputDescription.builder()
                            .withSupportedFormat(new Format(mimeType, encoding, schema));

        } else if (type.path(YamlConstants.CRS).exists()) {
            desc = MatlabBoundingBoxInputDescription.builder()
                    .withSupportedCRS(getCRS(type));
        } else {
            throw new MatlabConfigurationException("Missing mimeType or crs for input %s", id);
        }

        desc.withIdentifier(id);
        desc.withTitle(title);
        desc.withAbstract(abstrakt);
        desc.withMaximalOccurence(minOccurs);
        desc.withMaximalOccurence(maxOccurs);

        return desc.build();
    }

    private LinkedHashSet<OwsCRS> getCRS(YamlNode type) {
        LinkedHashSet<OwsCRS> crss = new LinkedHashSet<>(type.path(YamlConstants.CRS).size());
        if (type.path(YamlConstants.CRS).isSequence()) {
            for (YamlNode crs : type.path(YamlConstants.CRS)) {
                crss.add(new OwsCRS(crs.asTextValue()));
            }
        } else {
            crss.add(new OwsCRS(type.path(YamlConstants.CRS).asTextValue()));
        }
        return crss;
    }

    private ProcessOutputDescription createComplexOutput(
            YamlNode definition) {
        String id = definition.path(YamlConstants.IDENTIFIER).textValue();
        if (id == null || id.isEmpty()) {
            throw new MatlabConfigurationException("Missing output identifier");
        }
        YamlNode type = definition.path(YamlConstants.TYPE);
        if (!type.isMap()) {
            throw new MatlabConfigurationException("Missing type for output %s", id);
        }


        String mimeType = type.path(YamlConstants.MIME_TYPE).asTextValue();
        String schema = type.path(YamlConstants.SCHEMA).asTextValue();
        String encoding = type.path(YamlConstants.ENCODING).asTextValue();

        ProcessOutputDescriptionBuilder<?,?> desc;
        if (mimeType != null && !mimeType.isEmpty()) {
            desc = MatlabComplexOutputDescription.builder().withSupportedFormat(new Format(mimeType, encoding, schema));
        } else if (type.path(YamlConstants.CRS).exists()) {
            desc = MatlabBoundingBoxOutputDescription.builder().withSupportedCRS(getCRS(type));
        } else {
            throw new MatlabConfigurationException("Missing mimeType or crs for output %s", id);
        }

        String abstrakt = definition.path(YamlConstants.ABSTRACT).asTextValue();
        String title = definition.path(YamlConstants.TITLE).asTextValue();
        return desc.withIdentifier(id)
                .withTitle(title)
                .withAbstract(abstrakt)
                .build();
    }

    private MatlabLiteralInputDescription createLiteralInput(YamlNode definition) {
        String id = definition.path(YamlConstants.IDENTIFIER).textValue();
        if (id == null || id.isEmpty()) {
            throw new MatlabConfigurationException("Missing process input identifier");
        }
        LiteralType type = LiteralType.of(definition.path(YamlConstants.TYPE).textValue());
        if (type == null) {
            throw new MatlabConfigurationException("Missing type for input %s", id);
        }
        BigInteger minOccurs = definition.path(YamlConstants.MIN_OCCURS).asBigIntegerValue(BigInteger.ONE);
        BigInteger maxOccurs;
        if (definition.path(YamlConstants.MAX_OCCURS).isText()) {
            if (definition.path(YamlConstants.MAX_OCCURS).asTextValue().equals("unbounded")) {
                maxOccurs = BigInteger.valueOf(Long.MAX_VALUE);
            } else {
                maxOccurs = BigInteger.ONE;
            }
        } else {
            maxOccurs = definition.path(YamlConstants.MAX_OCCURS).asBigIntegerValue(BigInteger.ONE);
        }

        InputOccurence minmax;
        try {
            minmax = new InputOccurence(minOccurs, maxOccurs);
        } catch (IllegalArgumentException e) {
            throw new MatlabConfigurationException("Invalid min/max occurs: [%d,%d] for %s", minOccurs, maxOccurs, id);
        }


        String abstrakt = definition.path(YamlConstants.ABSTRACT).asTextValue();
        String title = definition.path(YamlConstants.TITLE).asTextValue();
        String unit = definition.path(YamlConstants.UNIT).asTextValue();

        AbstractMatlabLiteralInputDescriptionBuilder<?,?> builder = MatlabLiteralInputDescription.builder()
                        .withIdentifier(id)
                        .withTitle(title)
                        .withAbstract(abstrakt)
                        .withOccurence(minmax)
                        .withType(type)
                        .withDefaultUOM(unit);
        if (definition.path(YamlConstants.VALUES).isSequence()) {
            OwsAllowedValues owsAllowedValues = new OwsAllowedValues();
            for (YamlNode allowedValue : definition.path(YamlConstants.VALUES)) {
                owsAllowedValues.add(OwsAllowedValue.of(allowedValue.asTextValue()));
            }
            builder.withAllowedValues(owsAllowedValues);
        } else {
            builder.withAllowedValues(OwsAllowedValues.any());
        }
        return builder.build();
    }

    private MatlabLiteralOutputDescription createLiteralOutput(
            YamlNode definition) {
        String id = definition.path(YamlConstants.IDENTIFIER).textValue();
        if (id == null || id.isEmpty()) {
            throw new MatlabConfigurationException("Missing output identifier");
        }
        LiteralType type = LiteralType.of(definition.path(YamlConstants.TYPE)
                .textValue());
        if (type == null) {
            throw new MatlabConfigurationException("Missing type for output %s", id);
        }
        String abstrakt = definition.path(YamlConstants.ABSTRACT).asTextValue();
        String title = definition.path(YamlConstants.TITLE).asTextValue();
        String unit = definition.path(YamlConstants.UNIT).asTextValue();

        return MatlabLiteralOutputDescription.builder()
                .withIdentifier(id)
                .withTitle(title)
                .withAbstract(abstrakt)
                .withType(type)
                .withDataType(type.getXmlType())
                .withDefaultUOM(unit)
                .build();
    }

    private ProcessInputDescription createInput(YamlNode definition) {
        if (definition.path(YamlConstants.TYPE).isText()) {
            return createLiteralInput(definition);
        } else {
            return createComplexInput(definition);
        }
    }

    private ProcessOutputDescription createOutput(YamlNode definition) {
        if (definition.path(YamlConstants.TYPE).isText()) {
            return createLiteralOutput(definition);
        } else {
            return createComplexOutput(definition);
        }
    }

    public MatlabProcessDescription createProcessDescription(YamlNode definition) {
        String id = definition.path(YamlConstants.IDENTIFIER).asTextValue();
        if (id == null || id.isEmpty()) {
            throw new MatlabConfigurationException("Missing process identifier");
        }
        String title = definition.path(YamlConstants.TITLE).asTextValue();
        String abstrakt = definition.path(YamlConstants.ABSTRACT).asTextValue();
        String version = definition.path(YamlConstants.VERSION).asTextValue();
        String function = definition.path(YamlConstants.FUNCTION).asTextValue();
        if (function == null || function.isEmpty()) {
            throw new MatlabConfigurationException("Missing function name for process %s", id);
        }
        boolean storeSupported = definition.path(YamlConstants.STORE_SUPPPORTED)
                .asBooleanValue(true);
        boolean statusSupported = definition
                .path(YamlConstants.STATUS_SUPPORTED).asBooleanValue(false);

        MatlabProcessDescriptionBuilder<?, ?> builder = MatlabProcessDescription.builder()
                        .withFunction(function)
                        .withIdentifier(id)
                        .withTitle(title)
                        .withAbstract(abstrakt)
                        .withVersion(version)
                        .storeSupported(storeSupported)
                        .statusSupported(statusSupported);

        for (YamlNode input : definition.path(YamlConstants.INPUTS)) {
            builder.withInput(createInput(input));
        }

        for (YamlNode output : definition.path(YamlConstants.OUTPUTS)) {
            builder.withOutput(createOutput(output));
        }

        builder.withClientProvider(createClientProvider(definition.path(YamlConstants.CONNECTION)));


        MatlabProcessDescription desc = builder.build();

        if (desc.getInputs().isEmpty()) {
            throw new MatlabConfigurationException("Missing input definitions for process %s", id);
        }
        if (desc.getOutputs().isEmpty()) {
            throw new MatlabConfigurationException("Missing output definitions for process %s", id);
        }

        return desc;
    }

    private Supplier<MatlabClient> createClientProvider(YamlNode settings) {
        checkArgument(settings != null && settings.exists());
        try {
            if (settings.isMap()) {
                return new FactorySupplier(MatlabClientConfiguration.builder()
                        .withAddress(settings.path(YamlConstants.HOST).asTextValue(),
                             settings.path(YamlConstants.PORT).asIntValue())
                        .build());
            } else if (settings.isText()) {
                String address = settings.textValue();
                if (address.startsWith("ws://") || address.startsWith("wss://")) {
                    return new FactorySupplier(MatlabClientConfiguration.builder()
                            .withAddress(URI.create(address))
                            .build());
                } else if (address.startsWith("file://")) {
                    File directory = new File(URI.create(address).toURL().getFile());
                    return Suppliers.memoize(new FactorySupplier(MatlabClientConfiguration.builder()
                            .withInstancePoolConfiguration(MatlabInstancePoolConfiguration
                                    .builder()
                                    .withMaximalNumInstances(LOCAL_INSTANCES)
                                    .withInstanceConfig(MatlabInstanceConfiguration
                                            .builder()
//                                            .hidden()
                                            .withBaseDir(directory).build())
                                    .build())
                            .build()));
                } else if (address.equalsIgnoreCase("local")) {
                    return Suppliers.memoize(new FactorySupplier(MatlabClientConfiguration
                            .builder()
                            .withInstancePoolConfiguration(MatlabInstancePoolConfiguration
                                    .builder()
                                    .withMaximalNumInstances(LOCAL_INSTANCES)
                                    .withInstanceConfig(MatlabInstanceConfiguration
                                            .builder()
//                                            .hidden()
                                            .build())
                                    .build())
                            .build()));
                } else {
                    throw new MatlabConfigurationException("Missing or invalid connection setting");
                }
            } else {
                throw new MatlabConfigurationException("Missing or invalid connection setting");
            }
        } catch (MalformedURLException ex) {
            throw new MatlabConfigurationException(ex);
        }
    }

    private class FactorySupplier implements Supplier<MatlabClient> {
        private final MatlabClientConfiguration config;

        FactorySupplier(MatlabClientConfiguration config) {
            this.config = config;
        }

        @Override
        public MatlabClient get() {
            try {
                return MatlabClient.create(config);
            } catch (MatlabException | IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
