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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.n52.wps.server.r.data.R_Resource;

// TODO resources should behave more similar to regular annotations, so that i can access the resources using the getObjectValue(...)
public class ResourceAnnotation extends RAnnotation {

    private List<R_Resource> resources = new ArrayList<R_Resource>();

    public ResourceAnnotation(HashMap<RAttribute, Object> attributeHash, List<R_Resource> resources) throws IOException,
            RAnnotationException {
        super(RAnnotationType.RESOURCE, attributeHash);
        this.resources.addAll(resources);
    }
    
    @Override
    public Object getObjectValue(RAttribute attr) throws RAnnotationException {
    	if(attr.equals(RAttribute.NAMED_LIST)) {
    		return getResources();
    	}else if(attr.equals(RAttribute.NAMED_LIST_R_SYNTAX)){
    		
    		StringBuilder namedList = new StringBuilder();
			namedList.append("list(");
			boolean startloop = true;
			// have to process the resources to get full URLs to the files
			for (R_Resource resource: this.resources) {
				if(startloop){
					startloop = false;
				}else{
					namedList.append(", ");
				}
				String fullResourceURL = resource.getFullResourceURL()
						.toExternalForm();

				String resourceName = resource.getResourceValue();

				if (fullResourceURL != null) {
					namedList.append("\""+resourceName +"\""+ " = " + "\""
							+ fullResourceURL + "\"");
					}
				else
					namedList.append("\""+resourceName +"\""+ " = " + "\""
							+ resourceName + "\"");
					
			}
			namedList.append(")");
			
    		return namedList.toString();
    	}
    	else throw new RAnnotationException("Attribe not defined for this annotation.");
    }

    public List<R_Resource> getResources() {
        if (this.resources == null)
            this.resources = new ArrayList<R_Resource>();

        return this.resources;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ResourceAnnotation [resources=");
        if (this.resources != null)
            builder.append(Arrays.toString(this.resources.toArray()));
        else
            builder.append("<null>");
        builder.append("]");
        return builder.toString();
    }

}
