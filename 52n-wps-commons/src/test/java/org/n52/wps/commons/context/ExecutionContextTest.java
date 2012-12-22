/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wps.commons.context;

import java.util.Arrays;
import java.util.List;
import net.opengis.wps.x100.OutputDefinitionType;
import net.opengis.wps.x100.impl.OutputDefinitionTypeImpl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkunicki
 */
public class ExecutionContextTest {
    
    public ExecutionContextTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
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
