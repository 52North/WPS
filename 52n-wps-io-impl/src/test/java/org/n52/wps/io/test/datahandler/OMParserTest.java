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
package org.n52.wps.io.test.datahandler;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;

import org.junit.Test;
import org.n52.wps.io.data.binding.complex.OMObservationBinding;
import org.n52.wps.io.datahandler.parser.OMParser;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 */
public class OMParserTest extends AbstractTestCase<OMParser> {
	
	@Test
	public void shouldReturnNullIfInputIsWrong() {
		OMObservationBinding result = dataHandler.parse(null, null, null);
		assertThat(result, is(nullValue()));
		
		result = dataHandler.parse(null, "test", null);
		assertThat(result, is(nullValue()));
		
		result = dataHandler.parse(null, "application/om+xml; version=2.0", null);
		assertThat(result, is(nullValue()));
		
		result = dataHandler.parse(null, "application/om+xml; version=2.0", "test");
		assertThat(result, is(nullValue()));
		
		result = dataHandler.parse(null, "application/om+xml; version=2.0", "http://www.opengis.net/om/2.0");
		assertThat(result, is(nullValue()));
		
		result = dataHandler.parse(new ByteArrayInputStream("test".getBytes()), "application/om+xml; version=2.0", "http://www.opengis.net/om/2.0");
		assertThat(result, is(nullValue()));
	}

	@Override
	protected void initializeDataHandler() {
		dataHandler = new OMParser();
	}

}
