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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;

import org.junit.Test;
import org.n52.sos.ogc.om.OmObservation;
import org.n52.wps.io.data.binding.complex.OMObservationBinding;
import org.n52.wps.io.datahandler.parser.OMParser;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * 
 * @since 4.0.0
 *
 */
public class OMParserTest extends AbstractTestCase<OMParser> {
	
	private static final String SCHEMA = "http://www.opengis.net/om/2.0";
	private static final String MIME_TYPE = "application/om+xml; version=2.0";
	
	@Test
	public void shouldReturnNullIfInputIsWrong() {
		OMObservationBinding result = dataHandler.parse(null, null, null);
		assertThat(result, is(nullValue()));
		
		result = dataHandler.parse(null, "test", null);
		assertThat(result, is(nullValue()));
		
		result = dataHandler.parse(null, MIME_TYPE, null);
		assertThat(result, is(nullValue()));
		
		result = dataHandler.parse(null, MIME_TYPE, "test");
		assertThat(result, is(nullValue()));
		
		result = dataHandler.parse(null, MIME_TYPE, SCHEMA);
		assertThat(result, is(nullValue()));
		
		result = dataHandler.parse(new ByteArrayInputStream("test".getBytes()), MIME_TYPE, SCHEMA);
		assertThat(result, is(nullValue()));
	}
	
	@Test
	public void shouldDecodeSingleOMObservationXML() {
		ByteArrayInputStream stream = new ByteArrayInputStream(
			("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
			"<om:OM_Observation gml:id=\"o1\"" +
			"    xmlns:sml=\"http://www.opengis.net/sensorML/1.0.1\" " +
			"	xmlns:gml=\"http://www.opengis.net/gml/3.2\"" +
			"	xmlns:xlink=\"http://www.w3.org/1999/xlink\" " +
			"	xmlns:om=\"http://www.opengis.net/om/2.0\"" +
			"	xmlns:sams=\"http://www.opengis.net/samplingSpatial/2.0\"" +
			"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
			"	xsi:schemaLocation=\"http://www.opengis.net/sos/2.0 http://schemas.opengis.net/sos/2.0/sos.xsd          "
			+ "http://www.opengis.net/samplingSpatial/2.0 http://schemas.opengis.net/samplingSpatial/2.0/spatialSamplingFeature.xsd\"" +
			"	xmlns:sf=\"http://www.opengis.net/sampling/2.0\">" + 
			"	<gml:description>test description for this observation</gml:description>" + 
			"	<gml:identifier codeSpace=\"\">http://www.52north.org/test/observation/9</gml:identifier>" + 
			"	<om:type xlink:href=\"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\"/>" + 
			"	<om:phenomenonTime>" + 
			"		<gml:TimeInstant gml:id=\"phenomenonTime\">" + 
			"			<gml:timePosition>2012-07-31T17:45:15.000+00:00</gml:timePosition>" + 
			"		</gml:TimeInstant>" + 
			"	</om:phenomenonTime>" + 
			"	<om:resultTime xlink:href=\"#phenomenonTime\"/>" + 
			"	<om:procedure xlink:href=\"http://www.52north.org/test/procedure/9\"/>" + 
			"	<om:observedProperty xlink:href=\"http://www.52north.org/test/observableProperty/9_3\"/>" + 
			"	<om:featureOfInterest>" + 
			"		<sams:SF_SpatialSamplingFeature gml:id=\"ssf_test_feature_9\"> " + 
			"			<gml:identifier codeSpace=\"\">http://www.52north.org/test/featureOfInterest/9</gml:identifier> " + 
			"			<gml:name>52North</gml:name>" + 
			"			<sf:type xlink:href=\"http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingPoint\"/> " + 
			"			<sf:sampledFeature xlink:href=\"http://www.52north.org/test/featureOfInterest/1\"/> " + 
			"			<sams:shape> " + 
			"				<gml:Point gml:id=\"test_feature_9\"> " + 
			"					<gml:pos srsName=\"http://www.opengis.net/def/crs/EPSG/0/4326\">51.935101100104916 7.651968812254194</gml:pos> " + 
			"				</gml:Point> " + 
			"			</sams:shape> " + 
			"		</sams:SF_SpatialSamplingFeature>" + 
			"	</om:featureOfInterest>" + 
			"	<om:result xsi:type=\"gml:MeasureType\" uom=\"test_unit_9_3\">0.28</om:result>" + 
			"</om:OM_Observation>")
				.getBytes());
		OMObservationBinding observationBinding = dataHandler.parse(stream, MIME_TYPE, SCHEMA);
		assertThat(observationBinding, is(not(nullValue())));
		assertThat(observationBinding.getPayload(), instanceOf(OmObservation.class));
	}

	@Override
	protected void initializeDataHandler() {
		dataHandler = new OMParser();
	}
	
}
