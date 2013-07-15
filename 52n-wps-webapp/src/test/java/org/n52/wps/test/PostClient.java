
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
