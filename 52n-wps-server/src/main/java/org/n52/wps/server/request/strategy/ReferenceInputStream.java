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
package org.n52.wps.server.request.strategy;

import java.io.FilterInputStream;
import java.io.InputStream;

/**
 *
 * @author tkunicki
 */
public class ReferenceInputStream extends FilterInputStream {
    
    private final String mimeType;
    private final String encoding;
    
    public ReferenceInputStream(InputStream inputStream, String mimeType, String encoding) {
        super(inputStream);
        this.mimeType = mimeType;
        this.encoding = encoding;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public String getEncoding() {
        return encoding;
    }
}
