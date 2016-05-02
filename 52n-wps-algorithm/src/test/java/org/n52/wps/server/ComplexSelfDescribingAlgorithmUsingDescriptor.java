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

import org.n52.test.mock.MockBinding;
import java.util.List;
import java.util.Map;
import org.n52.test.mock.MockEnum;
import org.n52.wps.io.data.IData;
import org.n52.wps.algorithm.descriptor.AlgorithmDescriptor;
import org.n52.wps.algorithm.descriptor.ComplexDataInputDescriptor;
import org.n52.wps.algorithm.descriptor.ComplexDataOutputDescriptor;
import org.n52.wps.algorithm.descriptor.LiteralDataInputDescriptor;
import org.n52.wps.algorithm.descriptor.LiteralDataOutputDescriptor;

/**
 *
 * @author tkunicki
 */
public class ComplexSelfDescribingAlgorithmUsingDescriptor extends AbstractDescriptorAlgorithm {

    // NOTE: AbstractSelfDescribingAlgorithm.initializeDescription(), is called
    // in the constructor of AbstractAlgorithm.  This creates a situation where
    // initializeDescription() and getAlgorithmDescriptor() can be called before
    // the implementing classes have their constructors called...

    // The descriptors do sanity checking and will throw Exceptions with illegal
    // arguments or  state (i.e. null identifier or maxOccurs < minOccurs).  If
    // you decide to instantiate the DESCRIPTOR using a static constructor be
    // sure to wrap it in a try/catch with logging otherwise class load will fail,
    // this is a difficult state to debug


    private static AlgorithmDescriptor DESCRIPTOR;
    protected synchronized static AlgorithmDescriptor getAlgorithmDescriptorStatic() {
        if (DESCRIPTOR == null) {
            DESCRIPTOR =
                // Adding a lot of fields in order to provide example of AlgorithmDescriptor
                // chaining with the different literal and complex types.
                // most process descriptions would be much simpler...
                // Show use of chaining enabled by use of Builder pattern...
                AlgorithmDescriptor.builder(ComplexSelfDescribingAlgorithmUsingDescriptor.class).
                    version("0.0.1").  // default is "1.0.0"
                    title("Sample Algorithm Title"). // identifier is used if title is not set
                    abstrakt("Sample Algortihm Abstract"). // default is null (not output)
                    statusSupported(false). // default is true
                    storeSupported(false). // default is true
                    addInputDescriptor(
                        LiteralDataInputDescriptor.doubleBuilder(Constants.LITERAL_DOUBLE).
                            title("Sample Input Title").      // identifier is used if title is not set
                            abstrakt("Sample Input Abstract").// defaults to null (not output)
                            minOccurs(0).                   // defaults to 1
                            maxOccurs(2)).                  // defaults to 1
                    addInputDescriptor(
                        LiteralDataInputDescriptor.floatBuilder(Constants.LITERAL_FLOAT)).
                    addInputDescriptor(
                        LiteralDataInputDescriptor.longBuilder(Constants.LITERAL_LONG)).
                    addInputDescriptor(
                        LiteralDataInputDescriptor.intBuilder(Constants.LITERAL_INT)).
                    addInputDescriptor(
                        LiteralDataInputDescriptor.shortBuilder(Constants.LITERAL_SHORT)).
                    addInputDescriptor(
                        LiteralDataInputDescriptor.byteBuilder(Constants.LITERAL_BYTE)).
                    addInputDescriptor(
                        LiteralDataInputDescriptor.booleanBuilder(Constants.LITERAL_BOOLEAN)).
                    addInputDescriptor(
                        LiteralDataInputDescriptor.stringBuilder(Constants.LITERAL_STRING).
                            defaultValue("Some Default Value"). // can set a default value.
                            allowedValues(new String[] {"Some Default Value", "Not the default Value"})). // can set allowed values.
                    addInputDescriptor(
                        LiteralDataInputDescriptor.stringBuilder(Constants.LITERAL_ENUM).
                            defaultValue(MockEnum.VALUE1.name()).// you can pass in an enum to set allowed values
                            allowedValues(MockEnum.class).       // you can pass in an enum to set allowed values
                            maxOccurs(MockEnum.class)).          // you can pass in an enum to set maxOccurs
                    addInputDescriptor(
                        LiteralDataInputDescriptor.dateTimeBuilder(Constants.LITERAL_DATETIME)).
                    addInputDescriptor(
                        LiteralDataInputDescriptor.base64BinaryBuilder(Constants.LITERAL_BASE64BINARY)).
                    addInputDescriptor(
                        LiteralDataInputDescriptor.anyURIBuilder(Constants.LITERAL_ANYURI)).
                    addInputDescriptor(
                        ComplexDataInputDescriptor.builder(Constants.COMPLEX, MockBinding.class).
                            maximumMegaBytes(16)).  // can set maximumMegaBytes for ComplexInput types
                    addOutputDescriptor(
                        LiteralDataOutputDescriptor.doubleBuilder(Constants.LITERAL_DOUBLE).
                            title("Sample Output Title").         // identifier is used if title is not set
                            abstrakt("Sample Output Abstract")).  // defaults to null (not output)
                    addOutputDescriptor(
                        LiteralDataOutputDescriptor.floatBuilder(Constants.LITERAL_FLOAT)).
                    addOutputDescriptor(
                        LiteralDataOutputDescriptor.longBuilder(Constants.LITERAL_LONG)).
                    addOutputDescriptor(
                        LiteralDataOutputDescriptor.intBuilder(Constants.LITERAL_INT)).
                    addOutputDescriptor(
                        LiteralDataOutputDescriptor.shortBuilder(Constants.LITERAL_SHORT)).
                    addOutputDescriptor(
                        LiteralDataOutputDescriptor.byteBuilder(Constants.LITERAL_BYTE)).
                    addOutputDescriptor(
                        LiteralDataOutputDescriptor.booleanBuilder(Constants.LITERAL_BOOLEAN)).
                    addOutputDescriptor(
                        LiteralDataOutputDescriptor.stringBuilder(Constants.LITERAL_STRING)).
                    addOutputDescriptor(
                        LiteralDataOutputDescriptor.dateTimeBuilder(Constants.LITERAL_DATETIME)).
                    addOutputDescriptor(
                        LiteralDataOutputDescriptor.base64BinaryBuilder(Constants.LITERAL_BASE64BINARY)).
                    addOutputDescriptor(
                        LiteralDataOutputDescriptor.anyURIBuilder(Constants.LITERAL_ANYURI)).
                    addOutputDescriptor(
                        ComplexDataOutputDescriptor.builder(Constants.COMPLEX, MockBinding.class)).
                    build();
        }
        return DESCRIPTOR;
    }

    // Ideally this would be an abstract method in AbstractSelfDescribingAlgorithm,
    // this would break backwards compatibility with the Old API as it would
    // force migration to the new AlgorithmDescriptor based API.
    @Override
    public AlgorithmDescriptor createAlgorithmDescriptor() {
        // read note in static constructor...
        return getAlgorithmDescriptorStatic();
    }

    @Override
    public Map<String, IData> run(Map<String, List<IData>> inputData) {
        // unbind and do stuff...
        return null;
    }

}
