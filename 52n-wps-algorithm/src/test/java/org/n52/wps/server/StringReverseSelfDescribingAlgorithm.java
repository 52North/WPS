/**
 * ﻿Copyright (C) 2007
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
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
