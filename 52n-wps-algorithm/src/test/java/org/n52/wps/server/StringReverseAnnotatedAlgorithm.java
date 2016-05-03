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
    title="String Reverse Algorithm (Annotated)",
    abstrakt="This is an example algorithm implementation described using annotations that reverses a string.",
    statusSupported=false,
    storeSupported=false)
public class StringReverseAnnotatedAlgorithm extends AbstractAnnotatedAlgorithm {

    private String inputString;
    private String outputString;
    
    @LiteralDataInput(
        identifier="INPUT_STRING",
        title="Input String",
        abstrakt="The input string you want reversed.")
    public void setInputString(String inputString) {
        this.inputString = inputString;
    }


    @LiteralDataOutput(
        identifier="OUTPUT_STRING",
        title="Output String",
        abstrakt="The reverse of the input string.")
    public String getOutputString() {
        return outputString;
    }

    @Execute
    public void reverse() {
        outputString = (new StringBuffer(inputString)).reverse().toString();
    }

}
