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
