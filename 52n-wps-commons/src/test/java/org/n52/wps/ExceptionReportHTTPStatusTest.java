/**
 * ﻿Copyright (C) 2006 - 2016 52°North Initiative for Geospatial Open Source
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
package org.n52.wps;

import static org.junit.Assert.assertTrue;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.n52.wps.server.ExceptionReport;

/**
 * Class for testing the correct HTTP Status code for different WPS error codes. 
 * 
 * @author Benjamin Pross
 *
 */
public class ExceptionReportHTTPStatusTest {

	ExceptionReport exceptionReport;
	
	@Test
	public void testMissingParameterValueHTTTPStatusErrorCode(){
		exceptionReport = new ExceptionReport("Test error", ExceptionReport.MISSING_PARAMETER_VALUE);
		assertTrue(exceptionReport.getHTTPStatusCode() == HttpServletResponse.SC_BAD_REQUEST);
	}
	
	@Test
	public void testInvalidParameterValueHTTTPStatusErrorCode(){
		exceptionReport = new ExceptionReport("Test error", ExceptionReport.INVALID_PARAMETER_VALUE);
		assertTrue(exceptionReport.getHTTPStatusCode() == HttpServletResponse.SC_BAD_REQUEST);		
	}
	
	@Test
	public void testOperationNotSupportedHTTTPStatusErrorCode(){
		exceptionReport = new ExceptionReport("Test error", ExceptionReport.OPERATION_NOT_SUPPORTED);
		assertTrue(exceptionReport.getHTTPStatusCode() == HttpServletResponse.SC_NOT_IMPLEMENTED);			
	}
	
	@Test
	public void testVersionNegotiationFailedHTTTPStatusErrorCode(){
		exceptionReport = new ExceptionReport("Test error", ExceptionReport.VERSION_NEGOTIATION_FAILED);
		assertTrue(exceptionReport.getHTTPStatusCode() == HttpServletResponse.SC_BAD_REQUEST);			
	}
	
	@Test
	public void testInvalidUpdateSequenceHTTTPStatusErrorCode(){
		exceptionReport = new ExceptionReport("Test error", ExceptionReport.INVALID_UPDATE_SEQUENCE);
		assertTrue(exceptionReport.getHTTPStatusCode() == HttpServletResponse.SC_BAD_REQUEST);		
	}
	
	@Test
	public void testNoApplicableCodeHTTTPStatusErrorCode(){
		exceptionReport = new ExceptionReport("Test error", ExceptionReport.NO_APPLICABLE_CODE);
		assertTrue(exceptionReport.getHTTPStatusCode() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR);		
	}
	
	@Test
	public void testDefaultHTTTPStatusErrorCode(){
		exceptionReport = new ExceptionReport("Test error", "");
		assertTrue(exceptionReport.getHTTPStatusCode() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR);		
	}
}
