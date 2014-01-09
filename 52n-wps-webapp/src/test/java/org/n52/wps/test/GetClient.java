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
import java.net.URL;
import java.net.URLConnection;

public class GetClient {

    public static String sendRequest(String targetURL) throws IOException {
        return sendRequest(targetURL, null);
    }

    public static String sendRequest(String targetURL, String payload) throws IOException {
        // Construct data
        // Send data
        URL url = null;
        if (payload == null || payload.equalsIgnoreCase("")) {
            url = new URL(targetURL);
        }
        else {
            String payloadClean = payload.replace("?", "");
            url = new URL(targetURL + "?" + payloadClean);
        }

        URLConnection conn = url.openConnection();

        conn.setDoOutput(true);

        // Get the response
        StringBuffer response = new StringBuffer();
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ( (line = rd.readLine()) != null) {
            response = response.append(line + "\n");
        }

        rd.close();

        return response.toString();
    }

    public static InputStream sendRequestForInputStream(String targetURL, String payload) throws IOException {
        // Construct data

        // Send data
        String payloadClean = payload.replace("?", "");
        URL url = new URL(targetURL + "?" + payloadClean);

        return url.openStream();
    }
}
