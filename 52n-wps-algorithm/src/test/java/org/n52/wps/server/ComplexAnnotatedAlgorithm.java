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

import java.net.URI;
import java.util.Date;
import java.util.List;
import org.n52.test.mock.MockBinding;
import org.n52.test.mock.MockEnum;
import org.n52.test.mock.MockComplexObject;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.io.data.binding.literal.LiteralByteBinding;
import org.n52.wps.io.data.binding.literal.LiteralShortBinding;

/**
 *
 * @author tkunicki
 */
@Algorithm(
//    identifier="SampleIdentifier" fully qualified class name is used when this is missing
    version="0.0.1", // default is "1.0.0"
    title="Sample Algorithm Title", // identifier is used if title is not set
    abstrakt="Sample Algortihm Abstract", // default is null (not output)
    storeSupported=false, // default is true
    statusSupported=true) // default is true
public class ComplexAnnotatedAlgorithm extends AbstractAnnotatedAlgorithm {


    // EXAMPLES FOR OUTPUTS

    @LiteralDataInput(
        // binding=LiteralDoubleBinding.class, not needed, can infer type from field type
        identifier=Constants.LITERAL_DOUBLE,
        title="Sample Input Title",
        abstrakt="Sample Input Abstract",
        minOccurs=0,
        maxOccurs=2)
    public List<Double> inputLiteralDouble;
    // public List<? extends Double> inputLiteralDouble; OK, but needs explicit binding
    // public List<? super Double> inputLiteralDouble;   OK, but needs explicit binding
    // public List<?> inputLiteralDouble;                OK, but needs explicit binding
    // public List<? extends Number> inputLiteralDouble; OK, but needs explicit binding
    // public List<? super Number> inputLiteralDouble;   FAIL, [dD]ouble not superclass of Number
    // public [dDouble] inputLiteralDouble;              FAIL, maxOccurs is 2, need List!
    //
    //   not an exhaustive list, point is we can autobind literal types.  For types
    //   where autobinding can't be inferred one can explicitly set binding but parser
    //   will check types to validate assignability between binding payload and field or
    //   method argument.  Methods or fields must be public!

    // primitive field
    @LiteralDataInput(identifier=Constants.LITERAL_FLOAT)
    public float inputLiteralFloat;


    private Long inputLiteralLong;
    // Primitive wrapper as method argument
    @LiteralDataInput(identifier=Constants.LITERAL_LONG)
    public void setInputLiteralLong(Long inputLiteralLong) {
        this.inputLiteralLong = inputLiteralLong;
    }


    private int inputLiteralInt;
    // Primitive as method argument
    @LiteralDataInput(identifier=Constants.LITERAL_INT)
    public void setInputLiteralLong(int inputLiteralInt) {
        this.inputLiteralInt = inputLiteralInt;
    }

    private Number inputLiteralShort;
    // Number as method argument type
    @LiteralDataInput(
        binding=LiteralShortBinding.class,  // REQUIRED since literal type can't be inferred from method parameter type
        identifier=Constants.LITERAL_SHORT)
    public void setInputLiteralLong(Number inputLiteralShort) {
        this.inputLiteralShort = inputLiteralShort;
    }


    // Object as field type
    @LiteralDataInput(
        binding=LiteralByteBinding.class,  // REQUIRED since literal type can't be inferred from field type
        identifier=Constants.LITERAL_BYTE)
    public Object inputLiteralByte;


    private boolean inputeLiteralBoolean;
    // don't care what method name is, only check it's annotation...
    @LiteralDataInput(identifier=Constants.LITERAL_BOOLEAN)
    public void setWithSomeRandomName(boolean inputeLiteralBoolean) {
        this.inputeLiteralBoolean = inputeLiteralBoolean;
    }

    private String inputLiteralString;
    @LiteralDataInput(
        identifier=Constants.LITERAL_STRING,
        defaultValue="Some Default Value", //  annotation parser will validate this is allowedValues if set
        allowedValues= { "Some Default Value", "Not the default Value" } )
    public void setInputLiteralString(String inputLiteralString) {
        this.inputLiteralString = inputLiteralString;
    }

    private MockEnum inputLiteralEnum;
    @LiteralDataInput(
//        binding=LiteralStringBinding.class    Not needed!  Enums are auto-bound to strings!
        identifier=Constants.LITERAL_ENUM,
//        defaultValue=MockEnum.VALUE1,         Argh, Can't do this!
//        defaultValue=MockEnum.VALUE1.name(),  Argh, Can't do this either!, only literals or constants!  So we settle for String constants
        defaultValue="VALUE1",                 //  must be string but annotation parser will validate this is valid constant for MockEnum
//        allowedValues= { ... }                Already set to Enum constants for MockEnum!
        maxOccurs=LiteralDataInput.ENUM_COUNT // special case, set maxOccurs to number of MockEnum constants
        )
    public void setInputEnumType(MockEnum inputLiteralEnum) {
        this.inputLiteralEnum = inputLiteralEnum;
    }

    // then the rest of the inputs...
    private Date inputLiteralDateTime;
    private byte[] inputLiteralBase64Binary;
    private URI inputLiteralAnyURI;
    private MockComplexObject inputComplex;

    @LiteralDataInput(identifier=Constants.LITERAL_DATETIME)
    public void setInputLiteralDateTime(Date inputLiteralDateTime) {
        this.inputLiteralDateTime = inputLiteralDateTime;
    }

    @LiteralDataInput(identifier=Constants.LITERAL_BASE64BINARY)
    public void setInputLiteralDateTime(byte[] inputLiteralBase64Binary) {
        this.inputLiteralBase64Binary = inputLiteralBase64Binary;
    }

    @LiteralDataInput(identifier=Constants.LITERAL_ANYURI)
    public void setInputLiteralDateTime(URI inputLiteralAnyURI) {
        this.inputLiteralAnyURI = inputLiteralAnyURI;
    }

    @ComplexDataInput(
        identifier=Constants.COMPLEX,
        binding=MockBinding.class,      // Binding required for complex types!
        maximumMegaBytes=16)
    public void setInputComplex(MockComplexObject inputComplex) {
        this.inputComplex = inputComplex;
    }


    // EXAMPLES FOR OUTPUTS
    @LiteralDataOutput(
        identifier=Constants.LITERAL_DOUBLE,
        title="Sample Output Title",        // identifier is used if title is not set
        abstrakt="Sample Output Abstract")  // defaults to null (not output)
    public Double outputLiteralDouble;

    @LiteralDataOutput(identifier=Constants.LITERAL_FLOAT)
    public float outputLiteralFloat;


    private Long outputLiteralLong;
    @LiteralDataOutput(identifier=Constants.LITERAL_LONG)
    public Long getOutputLiteralLong() { return outputLiteralLong; }

    private int outputLiteralInt;
    @LiteralDataOutput(identifier=Constants.LITERAL_INT)
    public int getOutputLiteralInt() { return outputLiteralInt; }


    @LiteralDataOutput(
        binding=LiteralShortBinding.class, // REQUIRED, can't infer binding for type Number
        identifier=Constants.LITERAL_SHORT)
    public Number outputLiteralShort;

    // and the rest...

    private byte outputLiteralByte;
    private boolean outputLiteralBoolean;
    private String outputLiteralString;
    private Date outputLiteralDateTime;
    private byte[] outputLiteralBase64Binary;
    private URI outputLiteralAnyURI;
    private MockComplexObject outputComplex;

    @LiteralDataOutput(identifier=Constants.LITERAL_BYTE)
    public byte  getOutputLiteralByte() { return outputLiteralByte; }

    @LiteralDataOutput(identifier=Constants.LITERAL_BOOLEAN)
    public boolean  getOutputLiteralBoolean() { return outputLiteralBoolean; }

    @LiteralDataOutput(identifier=Constants.LITERAL_STRING)
    public String  getOutputLiteralString() { return outputLiteralString; }

    @LiteralDataOutput(identifier=Constants.LITERAL_DATETIME)
    public Date getOutputLiteralDateTime() { return outputLiteralDateTime; }

    @LiteralDataOutput(identifier=Constants.LITERAL_BASE64BINARY)
    public byte[] getOutputLiteralBase64Binary() { return outputLiteralBase64Binary; }

    @LiteralDataOutput(identifier=Constants.LITERAL_ANYURI)
    public URI getOutputLiteralAnyURI() { return outputLiteralAnyURI; }

    @ComplexDataOutput(
        binding=MockBinding.class,  // Binding required for complex types!
        identifier=Constants.COMPLEX)
    public MockComplexObject getComplex() { return outputComplex; }


    @Execute
    public void doStuff() {

        // values already unbound by setting fields or calling methods

        // just access your variables directly...

        // output fields or methods will be called and bound after this method call returns
    }

}
