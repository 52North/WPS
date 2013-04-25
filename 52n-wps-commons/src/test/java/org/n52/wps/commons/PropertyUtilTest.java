package org.n52.wps.commons;

import com.google.common.base.Joiner;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.jmock.Expectations;
import static org.jmock.Expectations.returnValue;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.joda.time.Period;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.n52.wps.PropertyDocument.Property;

/**
 *
 * @author tkunicki
 */
public class PropertyUtilTest {
    
    static final String KEY_TEST_ROOT = "my.test.root";
    
    static final String KEY_TEST_BOOLEAN = "sample.boolean";
    static final String KEY_TEST_LONG = "sample.long";
    static final String KEY_TEST_DOUBLE = "sample.double";
    static final String KEY_TEST_STRING = "sample.string";
    static final String KEY_TEST_PERIOD = "sample.period";
    
    static final boolean DEFAULT_BOOLEAN = false;
    static final long   DEFAULT_LONG = 1l;
    static final double DEFAULT_DOUBLE = 1d;
    static final String DEFAULT_STRING = "default";
    static final String DEFAULT_PERIOD = "P7D";
    static final long   DEFAULT_PERIOD_MS = Period.days(7).toStandardDuration().getMillis();
    
    static final boolean CONFIG_BOOLEAN = true;
    static final boolean CONFIG_LONG = true;
    static final boolean CONFIG_DOUBLE = true;
    static final boolean CONFIG_STRING = true;
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    Property[] properties;
    
    public PropertyUtilTest() {

    }
    
    @Before
    public void setUp() {
        properties = new Property[5];
        properties[0] = context.mock(Property.class, "p1");
        properties[1] = context.mock(Property.class, "p2");
        properties[2] = context.mock(Property.class, "p3");
        properties[3] = context.mock(Property.class, "p4");
        properties[4] = context.mock(Property.class, "p5");
    }
    
    @Test
    public void testExtractDefaults_NoSystemProperties_NoConfigProperties() {
        PropertyUtil cu = new PropertyUtil(new Property[0]);
        
        System.out.println("Verify default values are passed through in absense of system or config properties");
        assertThat("extractBoolean", cu.extractBoolean(KEY_TEST_BOOLEAN, DEFAULT_BOOLEAN), equalTo(DEFAULT_BOOLEAN));
        assertThat("extractLong", cu.extractLong(KEY_TEST_LONG, DEFAULT_LONG), equalTo(DEFAULT_LONG));
        assertThat("extractDouble", cu.extractDouble(KEY_TEST_DOUBLE, DEFAULT_DOUBLE), equalTo(DEFAULT_DOUBLE));
        assertThat("extractString", cu.extractString(KEY_TEST_STRING, DEFAULT_STRING), equalTo(DEFAULT_STRING));
        assertThat("extractPeriodAsMillis", cu.extractPeriodAsMillis(KEY_TEST_PERIOD, DEFAULT_PERIOD_MS), equalTo(DEFAULT_PERIOD_MS));
        
    }
    
    @Test
    public void testExtractDefaults_NoSystemProperties_NoConfigProperties_SystemPropertyRootSet() {
        PropertyUtil cu = new PropertyUtil(new Property[0], KEY_TEST_ROOT);
        
        System.out.println("Verify default values are passed through in absense of system or config properties even with system properties root set");
        assertThat("extractBoolean", cu.extractBoolean(KEY_TEST_BOOLEAN, DEFAULT_BOOLEAN), equalTo(DEFAULT_BOOLEAN));
        assertThat("extractLong", cu.extractLong(KEY_TEST_LONG, DEFAULT_LONG), equalTo(DEFAULT_LONG));
        assertThat("extractDouble", cu.extractDouble(KEY_TEST_DOUBLE, DEFAULT_DOUBLE), equalTo(DEFAULT_DOUBLE));
        assertThat("extractString", cu.extractString(KEY_TEST_STRING, DEFAULT_STRING), equalTo(DEFAULT_STRING));
        assertThat("extractPeriodAsMillis", cu.extractPeriodAsMillis(KEY_TEST_PERIOD, DEFAULT_PERIOD_MS), equalTo(DEFAULT_PERIOD_MS));
    }
    
    @Test
    public void testExtractValid_SystemProperties() {
        PropertyUtil cu = new PropertyUtil(new Property[0], KEY_TEST_ROOT);
        
        setValidSystemProperties();
        try {
            System.out.println("Verify system property values passed through");
            assertThat("extractBoolean", cu.extractBoolean(KEY_TEST_BOOLEAN, DEFAULT_BOOLEAN), equalTo(true));
            assertThat("extractLong", cu.extractLong(KEY_TEST_LONG, DEFAULT_LONG), equalTo(3l));
            assertThat("extractDouble", cu.extractDouble(KEY_TEST_DOUBLE, DEFAULT_DOUBLE), equalTo(3d));
            assertThat("extractString", cu.extractString(KEY_TEST_STRING, DEFAULT_STRING), equalTo("system"));
            assertThat("extractPeriodAsMillis", cu.extractPeriodAsMillis(KEY_TEST_PERIOD, DEFAULT_PERIOD_MS), equalTo(Period.days(3).toStandardDuration().getMillis()));
        } finally {
            clearSystemProperties();
        }
    }
    
    @Test
    public void testExtractValid_SystemProperties_ConfigProperties_SystemPreferredOverConfig() {
        PropertyUtil cu = new PropertyUtil(getValidMockProperties(true), KEY_TEST_ROOT);
        
        setValidSystemProperties();
        try {
            System.out.println("Verify system property values preferred over config property values");
            assertThat("extractBoolean", cu.extractBoolean(KEY_TEST_BOOLEAN, DEFAULT_BOOLEAN), equalTo(true));
            assertThat("extractLong", cu.extractLong(KEY_TEST_LONG, DEFAULT_LONG), equalTo(3l));
            assertThat("extractDouble", cu.extractDouble(KEY_TEST_DOUBLE, DEFAULT_DOUBLE), equalTo(3d));
            assertThat("extractString", cu.extractString(KEY_TEST_STRING, DEFAULT_STRING), equalTo("system"));
            assertThat("extractPeriodAsMillis", cu.extractPeriodAsMillis(KEY_TEST_PERIOD, DEFAULT_PERIOD_MS), equalTo(Period.days(3).toStandardDuration().getMillis()));
        } finally {
            clearSystemProperties();
        }
    }
    
    @Test
    public void testExtractValid_PropertiesInactive_NoSystemProperties() {
        
        PropertyUtil cu = new PropertyUtil(getValidMockProperties(false));
        
        assertThat("extractBoolean", cu.extractBoolean(KEY_TEST_BOOLEAN, DEFAULT_BOOLEAN), equalTo(DEFAULT_BOOLEAN));
        assertThat("extractLong", cu.extractLong(KEY_TEST_LONG, DEFAULT_LONG), equalTo(DEFAULT_LONG));
        assertThat("extractDouble", cu.extractDouble(KEY_TEST_DOUBLE, DEFAULT_DOUBLE), equalTo(DEFAULT_DOUBLE));
        assertThat("extractString", cu.extractString(KEY_TEST_STRING, DEFAULT_STRING), equalTo(DEFAULT_STRING));
        assertThat("extractPeriodAsMillis", cu.extractPeriodAsMillis(KEY_TEST_PERIOD, DEFAULT_PERIOD_MS), equalTo(DEFAULT_PERIOD_MS));
        
    }
    
    private Property[] getValidMockProperties(final boolean active) {
        
        context.checking(new Expectations() {{
            allowing (properties[0]).getName(); will(returnValue(KEY_TEST_BOOLEAN));
            allowing (properties[0]).getActive(); will(returnValue(active));
            allowing (properties[0]).getStringValue(); will(returnValue("true"));
            
            allowing (properties[1]).getName(); will(returnValue(KEY_TEST_LONG));
            allowing (properties[1]).getActive(); will(returnValue(active));
            allowing (properties[1]).getStringValue(); will(returnValue(Long.toString(2l)));
            
            allowing (properties[2]).getName(); will(returnValue(KEY_TEST_DOUBLE));
            allowing (properties[2]).getActive(); will(returnValue(active));
            allowing (properties[2]).getStringValue(); will(returnValue(Double.toString(2d)));
            
            allowing (properties[3]).getName(); will(returnValue(KEY_TEST_STRING));
            allowing (properties[3]).getActive(); will(returnValue(active));
            allowing (properties[3]).getStringValue(); will(returnValue("config"));
            
            allowing (properties[4]).getName(); will(returnValue(KEY_TEST_PERIOD));
            allowing (properties[4]).getActive(); will(returnValue(active));
            allowing (properties[4]).getStringValue(); will(returnValue("P2D"));
        }});
        
        return properties;
    }
    
    private void setValidSystemProperties() {
        Joiner dot = Joiner.on('.');
        System.setProperty(dot.join(KEY_TEST_ROOT, KEY_TEST_BOOLEAN), "true");
        System.setProperty(dot.join(KEY_TEST_ROOT, KEY_TEST_LONG), Long.toString(3l));
        System.setProperty(dot.join(KEY_TEST_ROOT, KEY_TEST_DOUBLE), Double.toString(3d));
        System.setProperty(dot.join(KEY_TEST_ROOT, KEY_TEST_STRING), "system");
        System.setProperty(dot.join(KEY_TEST_ROOT, KEY_TEST_PERIOD), "P3D");
    }
    
    private void clearSystemProperties() {
        Joiner dot = Joiner.on('.');
        System.clearProperty(dot.join(KEY_TEST_ROOT, KEY_TEST_BOOLEAN));
        System.clearProperty(dot.join(KEY_TEST_ROOT, KEY_TEST_LONG));
        System.clearProperty(dot.join(KEY_TEST_ROOT, KEY_TEST_DOUBLE));
        System.clearProperty(dot.join(KEY_TEST_ROOT, KEY_TEST_STRING));
        System.clearProperty(dot.join(KEY_TEST_ROOT, KEY_TEST_PERIOD));
    }
    
}