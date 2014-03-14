/**
 * ﻿Copyright (C) 2006 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *       • Apache License, version 2.0
 *       • Apache Software License, version 1.0
 *       • GNU Lesser General Public License, version 3
 *       • Mozilla Public License, versions 1.0, 1.1 and 2.0
 *       • Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
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
    static final long    DEFAULT_LONG = 1l;
    static final double  DEFAULT_DOUBLE = 1d;
    static final String  DEFAULT_STRING = "default";
    static final String  DEFAULT_PERIOD = "P1D";
    static final long    DEFAULT_PERIOD_MS = Period.days(1).toStandardDuration().getMillis();
    
    static final boolean CONFIG_BOOLEAN = true;
    static final long    CONFIG_LONG = 2l;
    static final double  CONFIG_DOUBLE = 2d;
    static final String  CONFIG_STRING = "config";
    static final String  CONFIG_PERIOD = "P2D";
    static final long    CONFIG_PERIOD_MS = Period.days(2).toStandardDuration().getMillis();
    
    static final boolean SYSTEM_BOOLEAN = true;
    static final long    SYSTEM_LONG = 3l;
    static final double  SYSTEM_DOUBLE = 3d;
    static final String  SYSTEM_STRING = "system";
    static final String  SYSTEM_PERIOD = "P3D";
    static final long    SYSTEM_PERIOD_MS = Period.days(3).toStandardDuration().getMillis();
    
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
            System.out.println("Verify system property values are used if present");
            assertThat("extractBoolean", cu.extractBoolean(KEY_TEST_BOOLEAN, DEFAULT_BOOLEAN), equalTo(SYSTEM_BOOLEAN));
            assertThat("extractLong", cu.extractLong(KEY_TEST_LONG, DEFAULT_LONG), equalTo(SYSTEM_LONG));
            assertThat("extractDouble", cu.extractDouble(KEY_TEST_DOUBLE, DEFAULT_DOUBLE), equalTo(SYSTEM_DOUBLE));
            assertThat("extractString", cu.extractString(KEY_TEST_STRING, DEFAULT_STRING), equalTo(SYSTEM_STRING));
            assertThat("extractPeriodAsMillis", cu.extractPeriodAsMillis(KEY_TEST_PERIOD, DEFAULT_PERIOD_MS), equalTo(SYSTEM_PERIOD_MS));
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
            assertThat("extractBoolean", cu.extractBoolean(KEY_TEST_BOOLEAN, DEFAULT_BOOLEAN), equalTo(SYSTEM_BOOLEAN));
            assertThat("extractLong", cu.extractLong(KEY_TEST_LONG, DEFAULT_LONG), equalTo(SYSTEM_LONG));
            assertThat("extractDouble", cu.extractDouble(KEY_TEST_DOUBLE, DEFAULT_DOUBLE), equalTo(SYSTEM_DOUBLE));
            assertThat("extractString", cu.extractString(KEY_TEST_STRING, DEFAULT_STRING), equalTo(SYSTEM_STRING));
            assertThat("extractPeriodAsMillis", cu.extractPeriodAsMillis(KEY_TEST_PERIOD, DEFAULT_PERIOD_MS), equalTo(SYSTEM_PERIOD_MS));
        } finally {
            clearSystemProperties();
        }
    }
    
    @Test
    public void testExtractValid_ConfigProperties_NoSystemProperties() {
        
        PropertyUtil cu = new PropertyUtil(getValidMockProperties(true));
        
        System.out.println("Verify config property values are used if system aren't present");
        assertThat("extractBoolean", cu.extractBoolean(KEY_TEST_BOOLEAN, DEFAULT_BOOLEAN), equalTo(CONFIG_BOOLEAN));
        assertThat("extractLong", cu.extractLong(KEY_TEST_LONG, DEFAULT_LONG), equalTo(CONFIG_LONG));
        assertThat("extractDouble", cu.extractDouble(KEY_TEST_DOUBLE, DEFAULT_DOUBLE), equalTo(CONFIG_DOUBLE));
        assertThat("extractString", cu.extractString(KEY_TEST_STRING, DEFAULT_STRING), equalTo(CONFIG_STRING));
        assertThat("extractPeriodAsMillis", cu.extractPeriodAsMillis(KEY_TEST_PERIOD, DEFAULT_PERIOD_MS), equalTo(CONFIG_PERIOD_MS));
        
    }
    
    @Test
    public void testExtractValid_ConfigPropertiesInactive_NoSystemProperties() {
        
        PropertyUtil cu = new PropertyUtil(getValidMockProperties(false));
        
        System.out.println("Verify config property values ignored when inactive (defaults are used in absense of system properties)");
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
            allowing (properties[0]).getStringValue(); will(returnValue(Boolean.toString(CONFIG_BOOLEAN)));
            
            allowing (properties[1]).getName(); will(returnValue(KEY_TEST_LONG));
            allowing (properties[1]).getActive(); will(returnValue(active));
            allowing (properties[1]).getStringValue(); will(returnValue(Long.toString(CONFIG_LONG)));
            
            allowing (properties[2]).getName(); will(returnValue(KEY_TEST_DOUBLE));
            allowing (properties[2]).getActive(); will(returnValue(active));
            allowing (properties[2]).getStringValue(); will(returnValue(Double.toString(CONFIG_DOUBLE)));
            
            allowing (properties[3]).getName(); will(returnValue(KEY_TEST_STRING));
            allowing (properties[3]).getActive(); will(returnValue(active));
            allowing (properties[3]).getStringValue(); will(returnValue(CONFIG_STRING));
            
            allowing (properties[4]).getName(); will(returnValue(KEY_TEST_PERIOD));
            allowing (properties[4]).getActive(); will(returnValue(active));
            allowing (properties[4]).getStringValue(); will(returnValue(CONFIG_PERIOD));
        }});
        
        return properties;
    }
    
    private void setValidSystemProperties() {
        Joiner dot = Joiner.on('.');
        System.setProperty(dot.join(KEY_TEST_ROOT, KEY_TEST_BOOLEAN), Boolean.toString(SYSTEM_BOOLEAN));
        System.setProperty(dot.join(KEY_TEST_ROOT, KEY_TEST_LONG), Long.toString(SYSTEM_LONG));
        System.setProperty(dot.join(KEY_TEST_ROOT, KEY_TEST_DOUBLE), Double.toString(SYSTEM_DOUBLE));
        System.setProperty(dot.join(KEY_TEST_ROOT, KEY_TEST_STRING), SYSTEM_STRING);
        System.setProperty(dot.join(KEY_TEST_ROOT, KEY_TEST_PERIOD), SYSTEM_PERIOD);
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