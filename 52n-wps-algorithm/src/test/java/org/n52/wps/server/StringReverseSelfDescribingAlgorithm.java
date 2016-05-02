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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.n52.wps.io.data.IData;
import org.n52.wps.algorithm.descriptor.AlgorithmDescriptor;
import org.n52.wps.algorithm.descriptor.LiteralDataInputDescriptor;
import org.n52.wps.algorithm.descriptor.LiteralDataOutputDescriptor;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;

/**
 *
 * @author tkunicki
 */
public class StringReverseSelfDescribingAlgorithm extends AbstractDescriptorAlgorithm {

    public final static String INPUT_STRING = "INPUT_STRING";
    public final static String OUTPUT_STRING = "OUTPUT_STRING";

    private static AlgorithmDescriptor DESCRIPTOR;
    protected synchronized static AlgorithmDescriptor getAlgorithmDescriptorStatic() {
        if (DESCRIPTOR == null) {
            DESCRIPTOR =
                // passing in a class to the AlgorithmDescriptor.builder will set
                // set the identity to the the fully qualified class name.  If this
                // is not desired use the String constructor.
                AlgorithmDescriptor.builder(StringReverseSelfDescribingAlgorithm.class).
                    version("0.0.1").  // default is "1.0.0"
                    title("String Reverse Algorithm (Self Describing)"). // identifier is used if title is not set
                    abstrakt("This is an example algorithm implementation described using a chained builder that reverses a string."). // default is null (not output)
                    statusSupported(false). // default is true
                    storeSupported(false). // default is true
                    addInputDescriptor(
                        LiteralDataInputDescriptor.stringBuilder(INPUT_STRING).
                            title("Input String").      // identifier is used if title is not set
                            abstrakt("The input string you want reversed.").// defaults to null (not output)
                            minOccurs(1).  // defaults to 1
                            maxOccurs(1)). // defaults to 1
                    addOutputDescriptor(
                        LiteralDataOutputDescriptor.stringBuilder(OUTPUT_STRING).
                            title("Output String").         // identifier is used if title is not set
                            abstrakt("The reverse of the input string.")).  // defaults to null (not output).
                    build();
        }
        return DESCRIPTOR;
    }

    @Override
    public AlgorithmDescriptor createAlgorithmDescriptor() {
        return getAlgorithmDescriptorStatic();
    }

    @Override
    public Map<String, IData> run(Map<String, List<IData>> inputMap) {
        // unwrap input(s)
        List<IData> inputDataList = inputMap.get(INPUT_STRING);
        if (inputDataList == null || inputDataList.isEmpty()) {
            addError("Missing input string!");
            return null;
        }
        IData inputData = inputDataList.get(0);
        if (inputData == null || !(inputData instanceof LiteralStringBinding)) {
            addError("Something wierd happened with the request parser!");
            return null;
        }
        String inputString = ((LiteralStringBinding)inputData).getPayload();

        // do work
        String outputString = (new StringBuffer(inputString)).reverse().toString();

        // wrap output
        Map<String, IData> outputMap = new HashMap<String, IData>();
        outputMap.put(OUTPUT_STRING, new LiteralStringBinding(outputString));

        return outputMap;
    }

}
