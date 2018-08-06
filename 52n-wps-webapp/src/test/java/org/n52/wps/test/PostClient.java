/*
 * Copyright (C) 2007-2018 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.test;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Joiner;

public class PostClient {

    public String buildRequest(String value) throws UnsupportedEncodingException {

        String data = URLEncoder.encode("operation", "UTF-8") + "=" + URLEncoder.encode("process", "UTF-8");
        data += "&" + URLEncoder.encode("payload", "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8");

        return data;
    }

    public static String sendRequest(String targetURL, String payload) throws IOException {
        // Construct data
        String payloadP = URLEncoder.encode(payload, "UTF-8");

        payloadP = "request=" + payloadP;

        InputStream in = sendRequestForInputStream(targetURL, payloadP);

        // Get the response
        BufferedReader rd = new BufferedReader(new InputStreamReader(in));
        List<String> lines= new LinkedList<String>();
        String line;
        while ( (line = rd.readLine()) != null) {
            lines.add(line);
        }
        rd.close();
        return Joiner.on('\n').join(lines);
    }

    public static InputStream sendRequestForInputStream(String targetURL, String payload) throws IOException {
        // Send data
        URL url = new URL(targetURL);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setDoOutput(true);

        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

        wr.write(payload);
        wr.close();

        if (conn.getResponseCode() >= 400) {
            return conn.getErrorStream();
        } else {
             return conn.getInputStream();
        }
    }

    public static void checkForExceptionReport(String targetURL, String payload, int expectedHTTPStatusCode, String... expectedExceptionParameters) throws IOException{
        // Send data
        URL url = new URL(targetURL);

        URLConnection conn = url.openConnection();

        conn.setDoOutput(true);

        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

        wr.write(payload);
        wr.close();

        try {
            conn.getInputStream();
        } catch (IOException e) {
            /*
             * expected, ignore
             */
        }

        InputStream error = ((HttpURLConnection) conn).getErrorStream();

        String exceptionReport = "";

        int data = error.read();
        while (data != -1) {
            exceptionReport = exceptionReport + (char)data;
            data = error.read();
        }
        error.close();
        assertTrue(((HttpURLConnection) conn).getResponseCode() == expectedHTTPStatusCode);

        for (String expectedExceptionParameter : expectedExceptionParameters) {

            assertTrue(exceptionReport.contains(expectedExceptionParameter));

        }
    }
}
