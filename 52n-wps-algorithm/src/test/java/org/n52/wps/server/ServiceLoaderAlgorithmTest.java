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

import net.opengis.wps.x100.ProcessDescriptionType;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;

import static org.hamcrest.CoreMatchers.*;

public class ServiceLoaderAlgorithmTest {

	private ServiceLoaderAlgorithmRepository repo;

	@Before
	public void init() {
		this.repo = new ServiceLoaderAlgorithmRepository();
		Assert.assertNotNull(this.repo);
	}

	@Test
	public void shouldFindAlgorithms() {
		String identified = "dummy-test-identifier";
		Assert.assertThat(repo.containsAlgorithm(identified), is(true));
		Assert.assertThat(repo.getAlgorithm(identified),
				is(notNullValue()));
		Assert.assertThat(repo.getAlgorithm(DummyAnnotatedAlgorithm.class
				.getCanonicalName()), is(notNullValue()));
	}

	@Test
	public void shouldNotFindAlgorithm() {
		String identified = "not-in-there";
		Assert.assertThat(repo.containsAlgorithm(identified), is(not(true)));
		Assert.assertThat(repo.getAlgorithm(identified), is(nullValue()));
	}
	
	@Test
	public void shouldFindTwoRegisteredAlgorithms() {
		Assert.assertThat(this.repo.getAlgorithmNames().size(), is(2));
	}
	
	@Test
	public void shouldResolveProcessDescription() {
		ProcessDescriptionType description = repo.getProcessDescription(DummyAnnotatedAlgorithm.class.getCanonicalName());
		Assert.assertThat(description, is(notNullValue()));
		Assert.assertThat(description, is(instanceOf(ProcessDescriptionType.class)));
	}

	@Algorithm(version = "0.1")
	public static class DummyAnnotatedAlgorithm extends
			AbstractAnnotatedAlgorithm {

		private String output;

		@LiteralDataInput(identifier = "input")
		public String input;

		@LiteralDataOutput(identifier = "output")
		public String getOutput() {
			return this.output;
		}

		@Execute
		public void myRunMethodFollowingNoSyntaxNoArgumentsAllowed() {
			this.output = "works like a charm.";
		}

	}

	@Algorithm(version = "0.1", identifier = "dummy-test-identifier")
	public static class DummyIdentifiedAnnotatedAlgorithm extends
			DummyAnnotatedAlgorithm {

		private String output;

		@LiteralDataInput(identifier = "input")
		public String input;

		@LiteralDataOutput(identifier = "output")
		public String getOutput() {
			return this.output;
		}

		@Execute
		public void myRunMethodFollowingNoSyntaxNoArgumentsAllowed() {
			this.output = "works like a charm.";
		}

	}
}
