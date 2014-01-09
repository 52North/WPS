/**
 * ﻿Copyright (C) 2007
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
