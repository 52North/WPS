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
package org.n52.wps.server;

import com.google.common.base.Joiner;
import java.util.List;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.algorithm.annotation.Execute;

/**
 *
 * @author tkunicki
 */
@Algorithm(
    version="0.0.1",
    title="String Join Algorithm (Annotated)",
    abstrakt="This is an example algorithm implementation described using annotations that joins strings using the specified delimiter.",
    statusSupported=false,
    storeSupported=false)
public class StringJoinAnnotatedAlgorithm extends AbstractAnnotatedAlgorithm {

    public enum Delimiter {
        SPACE(' '),
        TAB('\t'),
        PIPE('|'),
        COMMA(','),
        SEMI_COLON(';'),
        COLON(':');
        public final char value;
        Delimiter(char value) {
            this.value = value;
        }
    }
    
    private List<String> inputStrings;
    private Delimiter inputDelimiter;
    private String outputString;
    
    @LiteralDataInput(
        identifier="INPUT_STRINGS",
        title="Input Strings",
        abstrakt="The strings you want joined.",
        minOccurs=2,
        maxOccurs=32)
    public void setInputString(List<String> inputStrings) {
        this.inputStrings = inputStrings;
    }
    
    @LiteralDataInput(
        identifier="INPUT_DELIMITER",
        title="Delimiter",
        abstrakt="The value to use when joining strings")
    public void setInputDelimiter(Delimiter inputDelimiter) {
        this.inputDelimiter = inputDelimiter;
    }


    @LiteralDataOutput(
        identifier="OUTPUT_STRING",
        title="Output String",
        abstrakt="The strings joined with the delimiter")
    public String getOutputString() {
        return outputString;
    }
    
    @LiteralDataInput(identifier="yourMom")
    public String yourMom;

    @Execute
    public void reverse() {
        // don't need to do any parameter bounds checking that is specified
        // as part of the DescribeProcess, it's already done before this
        // method is called...
        outputString = Joiner.on(inputDelimiter.value).join(inputStrings);
    }

}
