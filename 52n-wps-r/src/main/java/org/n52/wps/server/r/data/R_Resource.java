/**
 * Copyright (C) 2010-2015 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.server.r.data;


/**
 * 
 * POJO for a file system resource use by R scripts.
 * 
 * @author Matthias Hinz, Daniel Nüst
 *
 */
public class R_Resource {

    private String resourceValue;

    private String processId;

    private boolean isPublic = true;

    public R_Resource(final String scriptId, final String resourceValue, boolean isPublic) {
        this.processId = scriptId;
        this.resourceValue = resourceValue;
        this.isPublic = isPublic;
    }

    public String getResourceValue() {
        return this.resourceValue;
    }

    public String getProcessId() {
        return processId;
    }

    public boolean isPublic() {
        return isPublic;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("R_Resource [resourceValue=").append(resourceValue).append(", scriptId=").append(processId).append(", isPublic=").append(isPublic).append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (isPublic ? 1231 : 1237);
        result = prime * result + ( (resourceValue == null) ? 0 : resourceValue.hashCode());
        result = prime * result + ( (processId == null) ? 0 : processId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        R_Resource other = (R_Resource) obj;
        if (isPublic != other.isPublic)
            return false;
        if (resourceValue == null) {
            if (other.resourceValue != null)
                return false;
        }
        else if ( !resourceValue.equals(other.resourceValue))
            return false;
        if (processId == null) {
            if (other.processId != null)
                return false;
        }
        else if ( !processId.equals(other.processId))
            return false;
        return true;
    }

}
