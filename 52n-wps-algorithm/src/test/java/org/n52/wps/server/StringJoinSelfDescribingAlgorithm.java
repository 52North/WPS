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

import com.google.common.base.Joiner;
import java.util.ArrayList;
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
public class StringJoinSelfDescribingAlgorithm extends AbstractDescriptorAlgorithm  {

    public final static String INPUT_STRINGS = "INPUT_STRINGS";
    public final static String INPUT_DELIMITER = "INPUT_DELIMITER";
    public final static String OUTPUT_STRING = "OUTPUT_STRING";
    
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

    private static AlgorithmDescriptor DESCRIPTOR;
    protected synchronized static AlgorithmDescriptor getAlgorithmDescriptorStatic() {
        if (DESCRIPTOR == null) {
            DESCRIPTOR =
                // passing in a class to the AlgorithmDescriptor.builder will set
                // set the identity to the the fully qualified class name.  If this
                // is not desired use the String constructor.
                AlgorithmDescriptor.builder(StringJoinSelfDescribingAlgorithm.class).
                    version("0.0.1").  // default is "1.0.0"
                    title("String Join Algorithm (Self Describing)"). // identifier is used if title is not set
                    abstrakt("This is an example algorithm implementation described using a chained builder that joins strings using the specified delimiter."). // default is null (not output)
                    statusSupported(false). // default is true
                    storeSupported(false). // default is true
                    addInputDescriptor(
                        LiteralDataInputDescriptor.stringBuilder(INPUT_STRINGS).
                            title("Input Strings").      // identifier is used if title is not set
                            abstrakt("The strings you want joined.").// defaults to null (not output)
                            minOccurs(2).  // defaults to 1
                            maxOccurs(32)).
                    addInputDescriptor(
                        LiteralDataInputDescriptor.stringBuilder(INPUT_DELIMITER).
                            title("Delimiter").      // identifier is used if title is not set
                            abstrakt("The value to use when joining strings"). // defaults to null (not output)
                            allowedValues(Delimiter.class)).
                    addOutputDescriptor(
                        LiteralDataOutputDescriptor.stringBuilder(OUTPUT_STRING).
                            title("Output String").         // identifier is used if title is not set
                            abstrakt("The strings joined with the delimiter")).  // defaults to null (not output).
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
        // unwrap input strings and error check
        List<IData> inputBoundStringList = inputMap.get(INPUT_STRINGS);
        if (inputBoundStringList == null || inputBoundStringList.size() < 2) {
            addError("Invalid parameter count for" + INPUT_STRINGS);
            return null;
        }
        List<String> inputStringList = new ArrayList<String>();
        for (IData boundString : inputBoundStringList) {
            if (boundString == null || !(boundString instanceof LiteralStringBinding)) {
                addError("unexpected binding ecountered unbinding " + INPUT_STRINGS + " parameter list");
                return null;
            }
            String inputString = ((LiteralStringBinding)boundString).getPayload();
            if (inputString == null || inputString.length() == 0) {
                addError("invalid value encounterd in " + INPUT_STRINGS + " parameter list");
            }
            inputStringList.add(inputString);
        }
        
        // unwrap input delimiter and error check
        List<IData> inputBoundDelimiterList = inputMap.get(INPUT_DELIMITER);
        if (inputBoundDelimiterList == null || inputBoundDelimiterList.size() != 1) {
            addError("Invalid parameter count for" + INPUT_DELIMITER);
            return null;
        }
        
        IData inputBoundDelimiterData = inputBoundDelimiterList.get(0);
        if (inputBoundDelimiterData == null || !(inputBoundDelimiterData instanceof LiteralStringBinding)) {
            addError("Something wierd happened with the request parser!");
            return null;
        }
        String inputDelimiterString = ((LiteralStringBinding)inputBoundDelimiterData).getPayload();
        Delimiter inputDelimiter = null;
        try {
            inputDelimiter = Delimiter.valueOf(inputDelimiterString);
        } catch (IllegalArgumentException e) {
            addError("invalid value encounterd for " + INPUT_DELIMITER + " parameter");
            return null;
        }
        
        // do work
        String outputString = Joiner.on(inputDelimiter.value).join(inputStringList);
        
        // wrap output
        Map<String, IData> outputMap = new HashMap<String, IData>();
        outputMap.put(OUTPUT_STRING, new LiteralStringBinding(outputString));

        return outputMap;
    }

}
