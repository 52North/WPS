/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.server;

import java.io.IOException;

import net.opengis.wps.x100.CapabilitiesDocument;
import net.opengis.wps.x100.ProcessBriefType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.xmlbeans.XmlException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.n52.test.mock.MockUtil;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.webapp.common.AbstractITClass;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class CapabilitiesGetProcessDescriptionExceptionTest extends AbstractITClass {
	
	public static final String IDENTIFIER = "CatchMeIfYouCan";	
	public static boolean algorithmTriedToInstantiate;

	@Before
    public void setUp(){
		MockMvcBuilders.webAppContextSetup(this.wac).build();
    }
	
	@Test
	public void shouldIgnoreExceptionousProcess() throws XmlException, IOException {
		MockUtil.getMockConfig();
		CapabilitiesDocument caps = CapabilitiesConfiguration.getInstance(CapabilitiesDocument.Factory.newInstance());
		
		Assert.assertTrue("Erroneous algorithm was never instantiated!", algorithmTriedToInstantiate);
		
		boolean found = false;
		for (ProcessBriefType pbt : caps.getCapabilities().getProcessOfferings().getProcessArray()) {
			if (IDENTIFIER.equals(pbt.getIdentifier().getStringValue())) {
				found = true;
			}
		}
		
		Assert.assertFalse("Algo found but was not expected!", found);
	}

	@Algorithm(version = "0.1", identifier = CapabilitiesGetProcessDescriptionExceptionTest.IDENTIFIER)
	public static class InstantiationExceptionAlgorithm extends AbstractAnnotatedAlgorithm {
		
		public InstantiationExceptionAlgorithm() {
			CapabilitiesGetProcessDescriptionExceptionTest.algorithmTriedToInstantiate = true;
		}
		
		private String output;

		@LiteralDataInput(identifier = "input")
		public String input;

		@LiteralDataOutput(identifier = "output")
		public String getOutput() {
			return this.output;
		}

		@Execute
		public void thisMethodsWillNeverEverByCalled() {
			this.output = "w0000t";
		}
		
		@Override
		public synchronized ProcessDescriptionType getDescription() {
			throw new RuntimeException("Gotcha!");
		}

		
	}
}
