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
package org.n52.wps.server.r.syntax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.n52.wps.server.r.data.R_Resource;

public class ResourceAnnotation extends RAnnotation {

	private List<R_Resource> resources;
	
	public ResourceAnnotation(HashMap<RAttribute, Object> attributeHash, List<R_Resource> resources) throws IOException,
			RAnnotationException {
		super(RAnnotationType.RESOURCE, attributeHash);
		this.resources = resources;
	}

	public List<R_Resource> getResources() {
		if(this.resources == null)
			this.resources = new ArrayList<R_Resource>();
			
		return this.resources;
	}

 
   void setResources(List<R_Resource> resources) {
		this.resources = resources;
	}

}
