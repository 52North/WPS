/*
 * Copyright (C) 2007-2017 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.io.datahandler.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import org.n52.wps.io.data.binding.complex.PlainStringBinding;
import org.n52.wps.io.datahandler.parser.AbstractParser;

/**
 * @author Bastian Schaeffer; Matthias Mueller, TU Dresden
 *
 */
public class WCPSQueryParser extends AbstractParser{

    public WCPSQueryParser(){
        super();
        supportedIDataTypes.add(PlainStringBinding.class);
    }

    @Override
    public PlainStringBinding parse(InputStream stream, String mimeType, String schema) {
        BufferedReader br;
        StringWriter sw;
        try {
            br = new BufferedReader(new InputStreamReader(stream,"UTF-8"));

            sw=new StringWriter();
            int k;
            while((k=br.read())!=-1){
                sw.write(k);
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported Encoding");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        PlainStringBinding result = new PlainStringBinding(sw.toString());
        return result;
    }

}
