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
package org.n52.wps.server.handler;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Map;

import org.n52.wps.io.data.IData;

import net.opengis.wps.x100.InputType;

/**
 * For some algorithms it is needed to intercept
 * the DataInputs before being processed. An algorithm
 * should provide implementations of these through this
 * interface and the corresponding annotation.
 * 
 * @author matthes rieke
 *
 */
public interface DataInputInterceptors {
	

	/**
	 * @return a map where input identifiers are keys
	 */
	public Map<String, InterceptorInstance> getInterceptors();
	
	
	public static interface InterceptorInstance {
		
		/**
		 * applies the actual interception
		 * @param input the input as provided in the Execute request
		 * 
		 * @return true if processed, this triggers a skip of parsing within the InputHandler 
		 */
		public List<IData> applyInterception(InputType input);
		
	}
	
	/**
	 * Decorate your Algorithm implementation with this
	 * annotation. the value must be the fully qualified
	 * class name of the {@link DataInputInterceptors} implementation.
	 * 
	 * @author matthes rieke
	 *
	 */
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface DataInputInterceptorImplementations {
		
		String value();
		
	}

}
