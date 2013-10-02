
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
