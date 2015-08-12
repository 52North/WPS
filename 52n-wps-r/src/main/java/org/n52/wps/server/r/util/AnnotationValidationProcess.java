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

package org.n52.wps.server.r.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.io.data.binding.complex.PlainStringBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;
import org.n52.wps.server.r.R_Config;
import org.n52.wps.server.r.data.RDataTypeRegistry;
import org.n52.wps.server.r.metadata.RAnnotationParser;
import org.n52.wps.server.r.syntax.RAnnotation;
import org.n52.wps.server.r.syntax.RAnnotationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Algorithm(version = "1.0.0", identifier = "org.n52.wps.server.algorithm.r.AnnotationValidation", title = "R Annotation Validation", statusSupported = false, storeSupported = false, abstrakt = "Validate the annotations of a WPS4R script without deploying it")
public class AnnotationValidationProcess extends AbstractAnnotatedAlgorithm {

    private static final String RESULT_OK = "OK";

    private static final String RESULT_ERROR = "ERROR";

    private static final String VALIDATION_IDENTIFIER = "id.during.validation";

    private static Logger LOGGER = LoggerFactory.getLogger(AnnotationValidationProcess.class);

    private String script;

    private String validationResult;

    private String annotationsString = null;

    private static RAnnotationParser parser = new RAnnotationParser(new RDataTypeRegistry(), new SimpleR_Config());

    private static class SimpleR_Config extends R_Config {
        //
    }

    public AnnotationValidationProcess() {
        LOGGER.debug("NEW {}", this);
    }

    @ComplexDataInput(identifier = "script", title = "annotated R script (as CDATA)", abstrakt = "An annotated R script to be validated for use within WPS4R. IMPORTANT: Wrap script in CDATA elements.", binding = PlainStringBinding.class)
    public void setScriptToValidate(Object script) {
        this.script = (String) script;
    }

    @LiteralDataOutput(identifier = "validationResultString", title = "Validation output as text", binding = LiteralStringBinding.class)
    public String returnValidationResult() {
        return this.validationResult;
    }

    @LiteralDataOutput(identifier = "validationResultBool", title = "Validation output as boolean", binding = LiteralBooleanBinding.class)
    public boolean returnValidationResultBool() {
        return this.validationResult.contains(RESULT_OK);
    }

    @LiteralDataOutput(identifier = "annotations", title = "A string representation of the Java objects of the parsed annotations", binding = LiteralStringBinding.class)
    public String returnAnnotationsString() {
        return this.annotationsString;
    }

    @Execute
    public void validateScript() {
        StringBuilder validation = new StringBuilder();
        boolean valid = false;

        try (InputStream inputStream = IOUtils.toInputStream(script);) {
            List<RAnnotation> annotations = parser.parseAnnotationsfromScript(inputStream);
            LOGGER.debug("Parsed {} annotations", annotations.size());
            this.annotationsString = Arrays.toString(annotations.toArray());
        }
        catch (RAnnotationException | IOException e) {
            validation.append(RESULT_ERROR);
            validation.append("\nCould not parse annotations: ");
            validation.append(e.getMessage());
            validation.append("\n");
            validation.append(Arrays.toString(e.getStackTrace()));
        }

        try (InputStream inputStream = IOUtils.toInputStream(script);) {
            valid = parser.validateScript(inputStream, VALIDATION_IDENTIFIER);
            LOGGER.debug("Valid script: {}", valid);
        }
        catch (RAnnotationException | IOException e) {
            validation.append(RESULT_ERROR);
            validation.append("\nCould not validate script: ");
            validation.append(e.getMessage());
            validation.append("\n");
            validation.append(Arrays.toString(e.getStackTrace()));
        }

        if ( !valid) {
            try (InputStream inputStream = IOUtils.toInputStream(script);) {
                Collection<Object> errors = parser.validateScriptWithErrors(inputStream, VALIDATION_IDENTIFIER);
                LOGGER.debug("Found {} errors.", errors.size());
                for (Object object : errors) {
                    validation.append("\n").append(object.toString()).append("\n");
                }
            }
            catch (RAnnotationException | IOException e) {
                validation.append(RESULT_ERROR);
                validation.append("\nCould not validate script: ");
                validation.append(e.getMessage());
                validation.append("\n");
                validation.append(Arrays.toString(e.getStackTrace()));
            }
        }

        if (this.annotationsString != null && valid)
            validation.append("\n").append(RESULT_OK).append("\n");

        this.validationResult = validation.toString();
    }

}
