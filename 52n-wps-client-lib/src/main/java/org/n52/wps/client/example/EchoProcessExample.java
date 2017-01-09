/*
 * Copyright (C) 2007-2015 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.client.example;

import java.io.IOException;
import java.util.Random;

import javax.xml.transform.TransformerException;

import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.n52.wps.client.ExecuteRequestBuilder;
import org.n52.wps.client.WPSClientException;
import org.n52.wps.client.WPSClientSession;

public class EchoProcessExample {

    private String url = "http://localhost:8080/wps/WebProcessingService";

    private String processID = "org.n52.wps.server.algorithm.test.EchoProcess";

    private Random rand = new Random();

    public static void main(String[] args) {
        EchoProcessExample example = new EchoProcessExample();
        try {
            example.run();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (WPSClientException e) {
            e.printStackTrace();
        }
        catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    private void run() throws IOException, WPSClientException, TransformerException {
        System.out.println("EchoProcess Example");

        // connect session
        WPSClientSession wpsClient = WPSClientSession.getInstance();
        boolean connected = wpsClient.connect(url);
        if ( !connected) {
            System.out.println("Could not connect to WPS.");
            return;
        }

        // take a look at the process description
        ProcessDescriptionType processDescription = wpsClient.getProcessDescription(url, processID);
        System.out.println("Echo process description:\n" + processDescription.xmlText() + "\n");

        // create the request, add literal input
        ExecuteRequestBuilder executeBuilder = new ExecuteRequestBuilder(processDescription);
        String input = "Hello lucky number " + rand.nextInt(42) + "!";
        String parameterIn = "literalInput";
        executeBuilder.addLiteralData(parameterIn, input);
        String parameterOut = "literalOutput";
        executeBuilder.setResponseDocument(parameterOut, null, null, null);

        if ( !executeBuilder.isExecuteValid()) {
            System.out.println("Created execute request is NOT valid.");
        }

        // build and send the request document
        ExecuteDocument executeRequest = executeBuilder.getExecute();
        System.out.println("Sending execute request:\n" + executeRequest.xmlText() + "\n");
        Object response = wpsClient.execute(url, executeRequest);
        System.out.println("Got response:\n" + response.toString() + "\n");

        // compare input and output
        if (response instanceof ExecuteResponseDocument) {
            ExecuteResponseDocument responseDoc = (ExecuteResponseDocument) response;
            XObject data = XPathAPI.eval(responseDoc.getDomNode(), "//wps:LiteralData");
            String output = data.toString();
            if (output.equals(input)) {
                System.out.println("Echo received!");
            }
        }
    }
}
