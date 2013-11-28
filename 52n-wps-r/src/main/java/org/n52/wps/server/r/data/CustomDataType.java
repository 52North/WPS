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

package org.n52.wps.server.r.data;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;

public class CustomDataType implements RTypeDefinition {

    String key;

    String processKey;

    String encoding;

    String schema;

    boolean isComplex;

    public void setComplex(boolean isComplex)
    {
        this.isComplex = isComplex;
    }

    @Override
    public String getKey()
    {
        return this.key;
    }

    @Override
    public String getProcessKey()
    {
        return this.processKey;
    }

    @Override
    public boolean isComplex()
    {
        return this.isComplex;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public void setProcessKey(String processKey)
    {
        this.processKey = processKey;
    }

    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    public void setSchema(String schema)
    {
        this.schema = schema;
    }

    @Override
    public String getEncoding()
    {
        return "base64";
    }

    @Override
    public String getSchema()
    {
        return this.schema;
    }

    @Override
    public Class<? extends IData> getIDataClass()
    {

        return GenericFileDataBinding.class;
    }

    public String toString()
    {
        return this.key + " - " + this.processKey + " - " + this.encoding + " - " + this.schema + " - " + this.isComplex;
    }

}
