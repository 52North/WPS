/**
 * ﻿Copyright (C) 2006
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
