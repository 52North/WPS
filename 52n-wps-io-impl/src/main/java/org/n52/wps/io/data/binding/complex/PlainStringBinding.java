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
package org.n52.wps.io.data.binding.complex;

import java.io.IOException;

import org.n52.wps.io.data.IComplexData;

public class PlainStringBinding implements IComplexData {

    private static final long serialVersionUID = -2102127552780241607L;

    protected transient String payload;

    public PlainStringBinding(String string) {
        payload = string;
    }

    public String getPayload() {
        return payload;
    }

	public Class<?> getSupportedClass() {
        return String.class;
    }

    private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException {
        oos.writeObject(payload);
    }

    private synchronized void readObject(java.io.ObjectInputStream oos) throws IOException, ClassNotFoundException {
        payload = (String) oos.readObject();
    }
    
    @Override
    public void dispose() {
        //
    }

}
