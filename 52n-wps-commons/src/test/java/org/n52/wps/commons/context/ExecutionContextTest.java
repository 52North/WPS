/**
 * ﻿Copyright (C) 2006 - 2015 52°North Initiative for Geospatial Open Source
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wps.commons.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import net.opengis.wps.x100.OutputDefinitionType;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author tkunicki
 */
public class ExecutionContextTest {
    
    public ExecutionContextTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        //
    }
    
    @AfterClass
    public static void tearDownClass() {
        //
    }
    
    @Before
    public void setUp() {
        //
    }
    
    @After
    public void tearDown() {
        //
    }

    @Test 
    public void testConstructor() {
        
        ExecutionContext ec;
        
        ec = new ExecutionContext((OutputDefinitionType)null);
        assertNotNull(ec.getOutputs());
        assertEquals(0, ec.getOutputs().size());
        
        ec = new ExecutionContext(Arrays.asList(new OutputDefinitionType[0]));
        assertNotNull(ec.getOutputs());
        assertEquals(0, ec.getOutputs().size());
        
        ec = new ExecutionContext(Arrays.asList(new OutputDefinitionType[1]));
        assertNotNull(ec.getOutputs());
        assertEquals(1, ec.getOutputs().size());
        
        ec = new ExecutionContext((List<OutputDefinitionType>)null);
        assertNotNull(ec.getOutputs());
        assertEquals(0, ec.getOutputs().size());
        
        ec = new ExecutionContext(OutputDefinitionType.Factory.newInstance());
        assertNotNull(ec.getOutputs());
        assertEquals(1, ec.getOutputs().size());
        
        ec = new ExecutionContext();
        assertNotNull(ec.getOutputs());
        assertEquals(0, ec.getOutputs().size());
    }
    

}
