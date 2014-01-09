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
package org.n52.wps.algorithm.descriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import junit.framework.TestCase;
import org.n52.test.mock.MockBinding;

/**
 *
 * @author tkunicki
 */
public class AlgorithmDescriptorTest extends TestCase {
    
    private LiteralDataOutputDescriptor.Builder MOCK_OUPUT1_BUILDER;
    
    private List<InputDescriptor.Builder<?,?>> MOCK_INPUT_BUILDERS;
    private List<OutputDescriptor.Builder<?,?>> MOCK_OUTPUT_BUILDERS;
    
    public AlgorithmDescriptorTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() {
        MOCK_INPUT_BUILDERS = new ArrayList<InputDescriptor.Builder<?,?>>();
        MOCK_INPUT_BUILDERS.add(LiteralDataInputDescriptor.booleanBuilder("mock_input1"));
        MOCK_INPUT_BUILDERS.add(LiteralDataInputDescriptor.booleanBuilder("mock_input2"));
        MOCK_INPUT_BUILDERS.add(ComplexDataInputDescriptor.builder("mock_input3", MockBinding.class));
        MOCK_INPUT_BUILDERS.add(ComplexDataInputDescriptor.builder("mock_input4", MockBinding.class));
        MOCK_INPUT_BUILDERS = Collections.unmodifiableList(MOCK_INPUT_BUILDERS);
        
        MOCK_OUPUT1_BUILDER = LiteralDataOutputDescriptor.booleanBuilder("mock_output1");
        
        MOCK_OUTPUT_BUILDERS = new ArrayList<OutputDescriptor.Builder<?,?>>();
//        MOCK_OUTPUT_BUILDERS.add(LiteralDataOutputDescriptor.booleanBuilder("mock_output1"));
        MOCK_OUTPUT_BUILDERS.add(LiteralDataOutputDescriptor.booleanBuilder("mock_output2"));
        MOCK_OUTPUT_BUILDERS.add(ComplexDataOutputDescriptor.builder("mock_output3", MockBinding.class));
        MOCK_OUTPUT_BUILDERS.add(ComplexDataOutputDescriptor.builder("mock_output4", MockBinding.class));
        MOCK_OUTPUT_BUILDERS = Collections.unmodifiableList(MOCK_OUTPUT_BUILDERS);
        
    }
    
    public void testStaticBuilder_String() {
        
        AlgorithmDescriptor descriptor = null;
        
        // Test fail-early, exception should be thrown if identifier is 'null';
        boolean thrown = false;
        try {
            AlgorithmDescriptor.builder((String)null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
        
        // test that static builder with String parameter
        descriptor = AlgorithmDescriptor.builder("mock_identifier").
                addOutputDescriptor(MOCK_OUPUT1_BUILDER).  // require one output
                build();
        assertEquals("mock_identifier", descriptor.getIdentifier());
        assertTrue(descriptor.hasTitle());
        assertEquals("mock_identifier", descriptor.getTitle());
        
    }

    public void testStaticBuilder_Class() {
        
        AlgorithmDescriptor descriptor = null;
        
        // Test fail-early, exception should be thrown if idnetifier is 'null';
        boolean thrown = false;
        try {
            AlgorithmDescriptor.builder((Class)null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            thrown = true;
        }
        assertTrue(thrown);
        
        // test that static builder with a valid class parameter
         descriptor = AlgorithmDescriptor.builder(getClass()).
                addOutputDescriptor(MOCK_OUPUT1_BUILDER). // require one output
                build();
        assertEquals(getClass().getCanonicalName(), descriptor.getIdentifier());
        assertTrue(descriptor.hasTitle());
        assertEquals(getClass().getCanonicalName(), descriptor.getTitle());
    }
    


    public void testVersion() {
        AlgorithmDescriptor descriptor = null;
        
        // test default is "1.0.0"
        descriptor = createMinimumCompliantBuilder().build();
        assertEquals("1.0.0", descriptor.getVersion());
        
        // test we can change
        descriptor = createMinimumCompliantBuilder().version("X.Y.Z").build();
        assertEquals("X.Y.Z", descriptor.getVersion());
    }

    public void testStoreSupported() {
        AlgorithmDescriptor descriptor = null;
        
        // test default is true
        descriptor = createMinimumCompliantBuilder().build();
        assertTrue(descriptor.getStoreSupported());
 
        // test we can set to true (explicitly)
        descriptor = createMinimumCompliantBuilder().storeSupported(true).build();
        assertTrue(descriptor.getStoreSupported());
        
        // test we can set to false
        descriptor = createMinimumCompliantBuilder().storeSupported(false).build();
        assertFalse(descriptor.getStoreSupported());
    }

    public void testStatusSupported() {
        AlgorithmDescriptor descriptor = null;
        
        // test default is true
        descriptor = createMinimumCompliantBuilder().build();
        assertTrue(descriptor.getStatusSupported());
 
        // test we can set to true (explicitly)
        descriptor = createMinimumCompliantBuilder().statusSupported(true).build();
        assertTrue(descriptor.getStatusSupported());
        
        // test we can set to false
        descriptor = createMinimumCompliantBuilder().statusSupported(false).build();
        assertFalse(descriptor.getStatusSupported());
    }

    public void testInputDescriptorHandling() {
        AlgorithmDescriptor descriptor = null;
        
        // test default is true
        descriptor = createMinimumCompliantBuilder().build();
        assertNotNull(descriptor.getInputDescriptors());
        assertEquals(0, descriptor.getInputDescriptors().size());
        assertNotNull(descriptor.getInputIdentifiers());
        assertEquals(0, descriptor.getInputIdentifiers().size());
        
        
        // test addInputDescriptor(InputDescriptor.Builder<?,?>) interface.
        AlgorithmDescriptor.Builder<?> builder = createMinimumCompliantBuilder();
        for (InputDescriptor.Builder inputBuilder : MOCK_INPUT_BUILDERS) {
            builder.addInputDescriptor(inputBuilder);
        }
        validateInputDescriptors(builder.build());
        
        // test addInputDescriptor(InputDescriptor<?>) interface.
        builder = createMinimumCompliantBuilder();
        for (InputDescriptor.Builder inputBuilder : MOCK_INPUT_BUILDERS) {
            builder.addInputDescriptor(inputBuilder.build());
        }
        validateInputDescriptors(builder.build());
        
        // test addInputDescriptor(InputDescriptor<?>) interface.
        builder = createMinimumCompliantBuilder();
        List<InputDescriptor> inputDescriptorList = new ArrayList<InputDescriptor>(MOCK_INPUT_BUILDERS.size());
        for (InputDescriptor.Builder inputBuilder : MOCK_INPUT_BUILDERS) {
            inputDescriptorList.add(inputBuilder.build());
        }
        builder.addInputDescriptors(inputDescriptorList);
        validateInputDescriptors(builder.build());
    }

    public void testOutputDescriptorHanding() {
        AlgorithmDescriptor descriptor = null;
        
        // Test fail-early, exception should be thrown if idnetifier is 'null';
        boolean thrown = false;
        try {
            AlgorithmDescriptor.builder("mock_identifier").build();
            fail("Expected IllegalArgumentException");
        } catch (IllegalStateException e) {
            thrown = true;
        }
        assertTrue(thrown);
        
        // test assumption that createMinimumCompliantBuilder() returns us 1 output
        descriptor = createMinimumCompliantBuilder().build();
        assertNotNull(descriptor.getOutputDescriptors());
        assertEquals(1, descriptor.getOutputDescriptors().size());
        assertNotNull(descriptor.getOutputIdentifiers());
        assertEquals(1, descriptor.getOutputIdentifiers().size());
        
        // test addOutputDescriptor(OutputDescriptor.Builder<?,?>) interface.
        AlgorithmDescriptor.Builder<?> builder = createMinimumCompliantBuilder();
        for (OutputDescriptor.Builder outputBuilder : MOCK_OUTPUT_BUILDERS) {
            builder.addOutputDescriptor(outputBuilder);
        }
        validateOutputDescriptors(builder.build());
        
        // test addOutputDescriptor(OutputDescriptor<?>) interface.
        builder = createMinimumCompliantBuilder();
        for (OutputDescriptor.Builder outputBuilder : MOCK_OUTPUT_BUILDERS) {
            builder.addOutputDescriptor(outputBuilder.build());
        }
        validateOutputDescriptors(builder.build());
        
        // test addOutputDescriptor(OutputDescriptor<?>) interface.
        builder = createMinimumCompliantBuilder();
        List<OutputDescriptor> outputDescriptorList = new ArrayList<OutputDescriptor>(MOCK_OUTPUT_BUILDERS.size());
        for (OutputDescriptor.Builder outputBuilder : MOCK_OUTPUT_BUILDERS) {
            outputDescriptorList.add(outputBuilder.build());
        }
        builder.addOutputDescriptors(outputDescriptorList);
        validateOutputDescriptors(builder.build());
    }
    
    private AlgorithmDescriptor.Builder<?> createMinimumCompliantBuilder() {
        return AlgorithmDescriptor.builder("mock_identifier").
                addOutputDescriptor(MOCK_OUPUT1_BUILDER);
    }
    
    private void validateInputDescriptors(AlgorithmDescriptor algorithmDescriptor) {
        assertNotNull(algorithmDescriptor.getInputDescriptors());
        
        // Test Collection<InputDescriptor> getInputDescriptors()
        Collection<InputDescriptor> collection = algorithmDescriptor.getInputDescriptors();
        // correct size?
        assertEquals(4, collection.size());
        // input order preserved?
        Iterator<InputDescriptor> iterator = collection.iterator();
        InputDescriptor inputDescriptor = iterator.next();
        assertNotNull(inputDescriptor);
        assertEquals("mock_input1", inputDescriptor.getIdentifier());
        inputDescriptor = iterator.next();
        assertNotNull(inputDescriptor);
        assertEquals("mock_input2", inputDescriptor.getIdentifier());
        inputDescriptor = iterator.next();
        assertNotNull(inputDescriptor);
        assertEquals("mock_input3", inputDescriptor.getIdentifier());
        inputDescriptor = iterator.next();
        assertNotNull(inputDescriptor);
        assertEquals("mock_input4", inputDescriptor.getIdentifier());
        assertFalse(iterator.hasNext());
        
        // Test InputDescriptor getInputDescriptor(String)
        // Can we access by indentifier?
        assertNotNull(algorithmDescriptor.getInputDescriptor("mock_input1"));
        assertNotNull(algorithmDescriptor.getInputDescriptor("mock_input2"));
        assertNotNull(algorithmDescriptor.getInputDescriptor("mock_input3"));
        assertNotNull(algorithmDescriptor.getInputDescriptor("mock_input4"));
        // Are we getting the correct decriptors returned by identifier?      
        assertEquals(algorithmDescriptor.getInputDescriptor("mock_input1").getIdentifier(), "mock_input1");
        assertEquals(algorithmDescriptor.getInputDescriptor("mock_input2").getIdentifier(), "mock_input2");
        assertEquals(algorithmDescriptor.getInputDescriptor("mock_input3").getIdentifier(), "mock_input3");
        assertEquals(algorithmDescriptor.getInputDescriptor("mock_input4").getIdentifier(), "mock_input4");
        
        // Test List<String> getInputIdentifiers();
        List<String> inputIdentifierList = algorithmDescriptor.getInputIdentifiers();
        // Size ok?
        assertEquals(4, inputIdentifierList.size());
        // Order preserved?
        assertEquals("mock_input1", inputIdentifierList.get(0));
        assertEquals("mock_input2", inputIdentifierList.get(1));
        assertEquals("mock_input3", inputIdentifierList.get(2));
        assertEquals("mock_input4", inputIdentifierList.get(3));
    }
    
    private void validateOutputDescriptors(AlgorithmDescriptor algorithmDescriptor) {
        assertNotNull(algorithmDescriptor.getOutputDescriptors());
        
        // Test Collection<OutputDescriptor> getOutputDescriptors()
        Collection<OutputDescriptor> collection = algorithmDescriptor.getOutputDescriptors();
        // correct size?
        assertEquals(4, collection.size());
        // output order preserved?
        Iterator<OutputDescriptor> iterator = collection.iterator();
        OutputDescriptor outputDescriptor = iterator.next();
        assertNotNull(outputDescriptor);
        assertEquals("mock_output1", outputDescriptor.getIdentifier());
        outputDescriptor = iterator.next();
        assertNotNull(outputDescriptor);
        assertEquals("mock_output2", outputDescriptor.getIdentifier());
        outputDescriptor = iterator.next();
        assertNotNull(outputDescriptor);
        assertEquals("mock_output3", outputDescriptor.getIdentifier());
        outputDescriptor = iterator.next();
        assertNotNull(outputDescriptor);
        assertEquals("mock_output4", outputDescriptor.getIdentifier());
        assertFalse(iterator.hasNext());
        
        // Test OutputDescriptor getOutputDescriptor(String)
        // Can we access by indentifier?
        assertNotNull(algorithmDescriptor.getOutputDescriptor("mock_output1"));
        assertNotNull(algorithmDescriptor.getOutputDescriptor("mock_output2"));
        assertNotNull(algorithmDescriptor.getOutputDescriptor("mock_output3"));
        assertNotNull(algorithmDescriptor.getOutputDescriptor("mock_output4"));
        // Are we getting the correct decriptors returned by identifier?      
        assertEquals(algorithmDescriptor.getOutputDescriptor("mock_output1").getIdentifier(), "mock_output1");
        assertEquals(algorithmDescriptor.getOutputDescriptor("mock_output2").getIdentifier(), "mock_output2");
        assertEquals(algorithmDescriptor.getOutputDescriptor("mock_output3").getIdentifier(), "mock_output3");
        assertEquals(algorithmDescriptor.getOutputDescriptor("mock_output4").getIdentifier(), "mock_output4");
        
        // Test List<String> getOutputIdentifiers();
        List<String> outputIdentifierList = algorithmDescriptor.getOutputIdentifiers();
        // Size ok?
        assertEquals(4, outputIdentifierList.size());
        // Order preserved?
        assertEquals("mock_output1", outputIdentifierList.get(0));
        assertEquals("mock_output2", outputIdentifierList.get(1));
        assertEquals("mock_output3", outputIdentifierList.get(2));
        assertEquals("mock_output4", outputIdentifierList.get(3));
    }


}
