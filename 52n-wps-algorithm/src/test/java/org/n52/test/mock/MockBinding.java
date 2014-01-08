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
package org.n52.test.mock;

import org.n52.wps.io.data.IComplexData;

/**
 *
 * @author tkunicki
 */
public class MockBinding implements IComplexData {

    private final MockComplexObject payload;

    public MockBinding(MockComplexObject payload) {
        this.payload = payload;
    }

    @Override
    public MockComplexObject getPayload() {
        return payload;
    }

    @Override
    public Class getSupportedClass() {
        return MockComplexObject.class;
    }

    @Override
    public void dispose() {
        
    }
}
