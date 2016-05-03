/**
 * ﻿Copyright (C) 2007 - 2016 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
