/**
 * ﻿Copyright (C) 2007 - 2016 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.server.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.n52.wps.server.RetrieveResultServlet;

public class UUIDTest {

	RetrieveResultServlet resultServlet;
	
	@Before
	public void setup(){
		resultServlet = new RetrieveResultServlet();
	}
	
	@Test
	public void testUUIDValidationValidStatusID(){
		
		String id1 = "615c4b53-13a6-4228-9bd2-5bc4a0b09e95";
				
		assertTrue(resultServlet.isIDValid(id1));
	}
	
	@Test
	public void testUUIDValidationValidResultID(){
		
		String id2 = "615c4b53-13a6-4228-9bd2-5bc4a0b09e95result.49b52cb6-5fe6-4812-8faf-ac7bf338ee4d";
		
		assertTrue(resultServlet.isIDValid(id2));
	}
	
	@Test
	public void testUUIDValidationInvalidStatusID(){
		
		String id3 = "1143eb2c-b93e-4340-8769-1437b621bd5e%3Cscript%3Ealert%28%27oh%20noes%27%29%3B%3C/script%3E";

		assertFalse((resultServlet.isIDValid(id3)));
	}
	
	
}
