/**
 * ﻿Copyright (C) 2010
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
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
