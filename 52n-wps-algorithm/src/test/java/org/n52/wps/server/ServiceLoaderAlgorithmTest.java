/**
 * ﻿Copyright (C) 2007 - 2016 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
