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
package org.n52.sos.util;
/**
 * ﻿Copyright (C) 2013
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

import org.n52.iceland.ogc.OGCConstants;
import org.n52.iceland.service.ServiceConfiguration;
import org.n52.sos.util.SosHelper.Configuration;

/**
 * Provides means to mock/set the configuration settings required by the O&M
 * parser and generator.
 * 
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 */
public class SosConfiguration {
	
	private static boolean isInit = false;
	
	private SosConfiguration() {}
	
	public static void init(){
		if (!isInit) {
			SosHelper.setConfiguration(new SosConfiguration.ConfigurationSeam());
			ServiceConfiguration.getInstance().setSrsNamePrefixForSosV2(OGCConstants.URL_DEF_CRS_EPSG);
	        ServiceConfiguration.getInstance().setSrsNamePrefixForSosV1(OGCConstants.URN_DEF_CRS_EPSG);
		}
	}
	
	protected static class ConfigurationSeam extends Configuration {

		@Override
		protected String getSrsNamePrefix() {
			return OGCConstants.URN_DEF_CRS_EPSG;
		}

		@Override
		protected String getSrsNamePrefixSosV2() {
			return OGCConstants.URL_DEF_CRS_EPSG;
		}
		
	}

}
