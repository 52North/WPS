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
import static org.n52.sos.util.builder.ObservablePropertyBuilder.aObservableProperty;
import static org.n52.sos.util.builder.ObservationConstellationBuilder.anObservationConstellation;
import static org.n52.sos.util.builder.ProcedureDescriptionBuilder.aSensorMLProcedureDescription;
import static org.n52.sos.util.builder.QuantityObservationValueBuilder.aQuantityValue;
import static org.n52.sos.util.builder.QuantityValueBuilder.aQuantitiy;
import static org.n52.sos.util.builder.SamplingFeatureBuilder.aSamplingFeature;

import java.io.IOException;
import java.io.InputStream;

import org.joda.time.DateTime;
import org.junit.Test;
import org.n52.iceland.ogc.gml.time.TimeInstant;
import org.n52.iceland.ogc.om.OmConstants;
import org.n52.sos.ogc.om.OmObservation;
import org.n52.sos.ogc.om.features.SfConstants;
import org.n52.sos.ogc.om.features.samplingFeatures.SamplingFeature;
import org.n52.wps.io.data.binding.complex.OMObservationBinding;
import org.n52.wps.io.datahandler.generator.OMGenerator;
import org.n52.wps.io.datahandler.parser.OMParser;

import com.vividsolutions.jts.geom.Point;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * 
 * @since 4.0.0
 *
 */
public class OMGeneratorTest extends AbstractTestCase<OMGenerator> {
	
	private static final String SCHEMA = "http://www.opengis.net/om/2.0";
	private static final String MIME_TYPE = "application/om+xml; version=2.0";
	
	@Test
	public void shouldReturnNullIfInputIsWrong() throws IOException {
		InputStream result = dataHandler.generateStream(null, null, null);
		assertThat(result, is(nullValue()));
		
		result = dataHandler.generateStream(null, "test", null);
		assertThat(result, is(nullValue()));
		
		result = dataHandler.generateStream(null, MIME_TYPE, null);
		assertThat(result, is(nullValue()));
		
		result = dataHandler.generateStream(null, MIME_TYPE, "test");
		assertThat(result, is(nullValue()));
		
		result = dataHandler.generateStream(null, MIME_TYPE, SCHEMA);
		assertThat(result, is(nullValue()));
		
		result = dataHandler.generateStream(new OMObservationBinding(null), MIME_TYPE, SCHEMA);
		assertThat(result, is(nullValue()));
	}
	
	@Test
	public void shouldEncodeSingleOMObservationToXML() throws IOException {
		final String description = "test description for this observation";
		final String procedureId = "http://www.52north.org/test/procedure/9";
		final String identifier = "http://www.52north.org/test/observation/9";
		final TimeInstant time = new TimeInstant(new DateTime("2012-07-31T17:45:15.000+00:00"));
		final double value = 0.28;
		final String unit = "test_unit_9_3";
		final String type = OmConstants.OBS_TYPE_MEASUREMENT;
		final String property = "http://www.52north.org/test/observableProperty/9_3";
		final String offering = "test-offering";
		final String foiName = "52North";
		final String foiIdentifier = "http://www.52north.org/test/featureOfInterest/9";
		final int srid = 4326;
		final double x = 51.935101100104916;
		final double y = 7.651968812254194;
		OmObservation observation = new OmObservation();
		observation.setDescription(description);
		observation.setIdentifier(identifier);
		observation.setResultTime(time);
		observation.setObservationConstellation(anObservationConstellation()
				.addOffering(offering)
				.setProcedure(aSensorMLProcedureDescription()
						.setIdentifier(procedureId)
						.build())
				.setObservationType(type)
				.setFeature(aSamplingFeature()
						.setIdentifier(foiIdentifier)
						.setName(foiName)
						.setFeatureType(SfConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_POINT)
						.setGeometry(y, x, srid)
						.build())
				.setObservableProperty(aObservableProperty()
						.setIdentifier(property)
						.build())
				.build()
				);
		observation.setValue(aQuantityValue()
				.setValue(aQuantitiy()
						.setValue(value)
						.setUnit(unit)
						.build())
				.setPhenomenonTime(time.getValue().getMillis())
				.build());
		OMObservationBinding dataToEncode = new OMObservationBinding(observation);
		InputStream generatedStream = dataHandler.generateStream(dataToEncode, MIME_TYPE, SCHEMA);
		assertThat(generatedStream, is(not(nullValue())));
		assertThat(generatedStream, instanceOf(InputStream.class));
		
		OMObservationBinding parsedObservationBinding = new OMParser().parse(generatedStream, MIME_TYPE, SCHEMA);
		assertThat(parsedObservationBinding, is(not(nullValue())));
		OmObservation parsedObservation = parsedObservationBinding.getPayload();
		assertThat(parsedObservation.getDescription(), is(description));
		assertThat(parsedObservation.getIdentifier(), is(identifier));
		assertThat(parsedObservation.getPhenomenonTime(), is(time));
		assertThat(parsedObservation.getValue().getValue().getValue(), is(new Double(value)));
		assertThat(parsedObservation.getValue().getValue().getUnit(), is(unit));
		assertThat(parsedObservation.getResultTime().isReferenced(), is(true));
		assertThat(parsedObservation.getResultTime().getReference(), is("phenomenonTime"));
		assertThat(parsedObservation.getObservationConstellation().getObservationType(), is(type));
		assertThat(parsedObservation.getObservationConstellation().getProcedure().getIdentifier(), is(procedureId));
		assertThat(parsedObservation.getObservationConstellation().getObservableProperty().getIdentifier(), is(property));
		assertThat(parsedObservation.getObservationConstellation().getFeatureOfInterest().getIdentifier(), is(foiIdentifier));
		assertThat(parsedObservation.getObservationConstellation().getFeatureOfInterest().getName().get(0).getValue(), is(foiName));
		assertThat(parsedObservation.getObservationConstellation().getFeatureOfInterest(), is(instanceOf(SamplingFeature.class)));
		final SamplingFeature feature = (SamplingFeature) parsedObservation.getObservationConstellation().getFeatureOfInterest();
		assertThat(feature.getGeometry().getSRID(), is(srid));
		assertThat(feature.getGeometry(), is(instanceOf(Point.class)));
		final Point point = (Point) feature.getGeometry();
		assertThat(point.getX(), is(x));
		assertThat(point.getY(), is(y));
	}

	@Override
	protected void initializeDataHandler() {
		dataHandler = new OMGenerator();
	}
	
}
