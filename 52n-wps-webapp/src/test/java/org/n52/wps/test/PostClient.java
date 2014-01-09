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

package org.n52.wps.test;

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

        // Send data
        URL url = new URL(targetURL);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");

        // URLConnection conn = url.openConnection();

        conn.setDoOutput(true);

        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

        wr.write(payloadP);
        wr.flush();

        // Get the response
        StringBuffer response = new StringBuffer();
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ( (line = rd.readLine()) != null) {
            response = response.append(line + "\n");
        }
        wr.close();
        rd.close();

        String responseString = response.toString();
        return responseString;
    }

    public static InputStream sendRequestForInputStream(String targetURL, String payload) throws IOException {
        // Construct data

        // Send data
        URL url = new URL(targetURL);

        URLConnection conn = url.openConnection();

        conn.setDoOutput(true);

        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

        wr.write(payload);
        wr.flush();

        return conn.getInputStream();

    }
}
