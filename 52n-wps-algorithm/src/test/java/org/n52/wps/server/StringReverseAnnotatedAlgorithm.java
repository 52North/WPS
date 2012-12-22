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
