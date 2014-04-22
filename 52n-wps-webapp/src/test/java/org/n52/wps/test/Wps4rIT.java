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

package org.n52.wps.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.xml.sax.SAXException;

/**
 * 
 * To run this integration tests there has to be RServe running on the localhost and the R repository must be
 * enabled in the WPS config.
 * 
 * To start RServe:
 * 
 * <ul>
 * <li>start <code>R</code></li>
 * <li>in the R console, load the library Rserve: <code>library(Rserve)</code></li>
 * <li>in the R console, start Rserve: <code>Rserve()</code></li>
 * <li>you should see and output "... Ok, ready to answer queries."</li>
 * </ul>
 * 
 * To enable the R process repository:
 * 
 * <ul>
 * <li>open your WPSConfiguration file, normally located in /config/wps_config.xml</li>
 * <li>Find the "LocalRAlgorithmRepository"</li>
 * <li>set the attribute "active" to <code>true</code></li>
 * <li>(restart your WPS server)</li>
 * </ul>
 */
public class Wps4rIT {

    private static String wpsUrl;

    @BeforeClass
    public static void beforeClass() {
        wpsUrl = AllTestsIT.getURL();

        // Seems not to work but it would be nice if it does...
        // URL resource = WPS4RTester.class
        // .getResource("/R/wps_config.xml");
        // WPSConfig.forceInitialization(new File(resource.getFile()).getAbsolutePath());

        String host = System.getProperty("test.rserve.host", "127.0.0.1");
        int port = Integer.parseInt(System.getProperty("test.rserve.port", "6311"));
        String user = System.getProperty("test.rserve.user", null);
        String password = System.getProperty("test.rserve.pwd", null);
        try {
            RConnection c = getNewConnection(host, port, user, password);
            c.close();
        }
        catch (RserveException e1) {
            Assume.assumeNoException(e1);
        }
    }

    @AfterClass
    public static void afterClass() {
        // WPSConfig.forceInitialization("src/main/webapp/config/wps_config.xml");
    }

    private static RConnection getNewConnection(String host, int port, String user, String password) throws RserveException {
        RConnection con = new RConnection(host, port);
        if (con != null && con.needLogin())
            con.login(user, password);

        return con;
    }

    @Test
    public void sessionInfoRetrievedFromWPSWebsite() throws MalformedURLException {
        String temp = wpsUrl.substring(0, wpsUrl.lastIndexOf("/"));
        URL urlSessionInfo = new URL(temp + "/R/sessioninfo.jsp");
        try {
            String response = GetClient.sendRequest(urlSessionInfo.toExternalForm());
            assertThat(response, containsString("R ")); // "R version" fails if using unstable R!
            assertThat(response, containsString("Platform:"));
            assertThat(response, containsString("attached base packages:"));
        }
        catch (IOException e) {
            String message = "Cannot retrieve the R session info from WPS.";
            e.printStackTrace();
            throw new AssertionError(message);
        }
    }

    @Test
    public void resourcesAreLoadedAndRead() throws IOException,
            ParserConfigurationException,
            SAXException,
            XmlException {
        URL resource = Wps4rIT.class.getResource("/R/ExecuteTestResources.xml");
        XmlObject xmlPayload = XmlObject.Factory.parse(resource);

        String payload = xmlPayload.toString();
        String response = PostClient.sendRequest(wpsUrl, payload);

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, not(containsString("ExceptionReport")));
        assertThat(response, containsString("This is a dummy txt-file"));
        assertThat(response, containsString("480"));
    }

    @Test
    public void responseContainsVersionSection() throws IOException,
            ParserConfigurationException,
            SAXException,
            XmlException {
        URL resource = Wps4rIT.class.getResource("/R/ExecuteTestResources.xml");
        XmlObject xmlPayload = XmlObject.Factory.parse(resource);

        String payload = xmlPayload.toString();
        String response = PostClient.sendRequest(wpsUrl, payload);

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, not(containsString("ExceptionReport")));
        assertThat(response, containsString("R version "));
    }

    @Test
    public void responseContainsWarningsSection() throws IOException,
            ParserConfigurationException,
            SAXException,
            XmlException {
        URL resource = Wps4rIT.class.getResource("/R/ExecuteTestWarnings.xml");
        XmlObject xmlPayload = XmlObject.Factory.parse(resource);

        String payload = xmlPayload.toString();
        String response = PostClient.sendRequest(wpsUrl, payload);

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, containsString("warnings"));
    }

    @Test
    public void responseContainsWarningsContent() throws IOException,
            ParserConfigurationException,
            SAXException,
            XmlException {

        URL resource = Wps4rIT.class.getResource("/R/ExecuteTestWarnings.xml");
        XmlObject xmlPayload = XmlObject.Factory.parse(resource);

        String payload = xmlPayload.toString();
        String response = PostClient.sendRequest(wpsUrl, payload);

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, containsString("This is the LAST warning."));
    }

    @Test
    public void decribeProcess() throws IOException, ParserConfigurationException, SAXException {
        String identifier = "org.n52.wps.server.r.test.resources";
        String response = GetClient.sendRequest(wpsUrl, "Service=WPS&Request=DescribeProcess&Version=1.0.0&Identifier="
                + identifier);

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, not(containsString("ExceptionReport")));
        assertThat(response, containsString(identifier));
        assertThat(response, containsString("<ows:Identifier>" + identifier + "</ows:Identifier>"));
    }

    @Test
    public void capabilitiesContainProcess() throws IOException, ParserConfigurationException, SAXException {
        String response = GetClient.sendRequest(wpsUrl, "Service=WPS&Request=GetCapabilities");

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, not(containsString("ExceptionReport")));
        assertThat(response, containsString("org.n52.wps.server.r.test.resources"));
        assertThat(response, containsString("org.n52.wps.server.r.test.calculator"));
        assertThat(response, containsString("org.n52.wps.server.r.test.image"));
    }

    @Test
    public void responseTypeIsImage() throws IOException, XmlException {
        URL resource = Wps4rIT.class.getResource("/R/ExecuteTestImage.xml");
        XmlObject xmlPayload = XmlObject.Factory.parse(resource);
        String payload = xmlPayload.toString();
        payload = payload.replace("@@@size@@@", "420");

        String response = PostClient.sendRequest(wpsUrl, payload);
        assertThat(response.split("\n", 1)[0], containsString("PNG"));
        assertThat(response, response, not(containsString("ExceptionReport")));
    }

    @Test
    public void calculatorWorksCorrectly() throws IOException, ParserConfigurationException, SAXException, XmlException {
        URL resource = Wps4rIT.class.getResource("/R/ExecuteTestCalculator.xml");
        XmlObject xmlPayload = XmlObject.Factory.parse(resource);
        String payload = xmlPayload.toString();

        Random rand = new Random();
        int a = rand.nextInt(100);
        payload = payload.replace("@@@a@@@", Integer.toString(a));
        int b = rand.nextInt(100);
        payload = payload.replace("@@@b@@@", Integer.toString(b));
        int op = rand.nextInt(3);
        String[] ops = new String[] {"+", "-", "*"};
        String opString = ops[op];
        payload = payload.replace("@@@op@@@", opString);
        int result = Integer.MIN_VALUE;
        if (opString.equals("+"))
            result = a + b;
        else if (opString.equals("-"))
            result = a - b;
        else if (opString.equals("*"))
            result = a * b;

        String response = PostClient.sendRequest(wpsUrl, payload);

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        String expected = "dataType=\"xs:double\">" + Integer.toString(result) + ".0";
        assertThat(response, containsString(expected));
    }

    @Test
    public void wpsOffAnnotationWorks() throws XmlException, IOException {
        URL resource = Wps4rIT.class.getResource("/R/ExecuteTestWpsOff.xml");
        XmlObject xmlPayload = XmlObject.Factory.parse(resource);
        String payload = xmlPayload.toString();
        String response = PostClient.sendRequest(wpsUrl, payload);

        String expected = "<wps:LiteralData dataType=\"xs:integer\">42</wps:LiteralData>";
        assertThat("Returned value is sum of provided ones, not sum of values defined in deactivated code.",
                   response,
                   containsString(expected));
    }

    @Test
    public void defaultValuesAreLoaded() throws XmlException, IOException {
        URL resource = Wps4rIT.class.getResource("/R/ExecuteTestDefaults.xml");
        XmlObject xmlPayload = XmlObject.Factory.parse(resource);
        String payload = xmlPayload.toString();
        String response = PostClient.sendRequest(wpsUrl, payload);

        String expected = "<wps:LiteralData dataType=\"xs:integer\">42</wps:LiteralData>";
        assertThat("Returned value is sum of defaults, not sum of values defined in deactivated code.",
                   response,
                   containsString(expected));
    }

    @Test
    public void exceptionsOnIllegalInputs() throws XmlException, IOException {
        String[] illegalCommands = new String[] {
        // "\\x0022;", // FIXME Rserve can be crashed with this
        // "\u0071\u0075\u0069\u0074\u0028\u0029",
        // "<-", "a<-q", // result in WPS parsing error
        "="};

        URL resource = Wps4rIT.class.getResource("/R/ExecuteTestInjection.xml");
        XmlObject xmlPayload = XmlObject.Factory.parse(resource);

        for (String cmd : illegalCommands) {
            String payload = xmlPayload.toString();
            payload = payload.replace("@@@cmd@@@", cmd);

            String response = PostClient.sendRequest(wpsUrl, payload);

            String expected = "illegal input";
            assertThat("Response is an exception", response, containsString("ExceptionReport"));
            assertThat("Response contains the keyphrase '" + expected + "'", response, containsString(expected));
            assertThat("Response contains the illegal input", response, containsString(cmd));
        }
    }

    @Test
    public void syntaxErrorOnIllegalInputs() throws XmlException, IOException {
        String[] illegalCommands = new String[] {"\"\";quit(\"no\");", "setwd('/root/')", "setwd(\"c:/\")"};

        URL resource = Wps4rIT.class.getResource("/R/ExecuteTestInjection.xml");
        XmlObject xmlPayload = XmlObject.Factory.parse(resource);

        for (String cmd : illegalCommands) {
            String payload = xmlPayload.toString();
            payload = payload.replace("@@@cmd@@@", cmd);

            String response = PostClient.sendRequest(wpsUrl, payload);

            assertThat("Response is an exception", response, containsString("ExceptionReport"));
            String expected = "eval failed";
            assertThat("Response contains '" + expected + "'", response, containsString(expected));
        }
    }

    @Test
    public void replacementsOnIllegalInputs() throws XmlException, IOException {
        String[] illegalCommands = new String[] {"unlink(getwd())", "q();", "quit()", "%lt;-",
                                                 // "system('format hardisk')",
                                                 "quit(\\\"no\\\");inputVariable;"};

        URL resource = Wps4rIT.class.getResource("/R/ExecuteTestInjection.xml");
        XmlObject xmlPayload = XmlObject.Factory.parse(resource);

        for (String cmd : illegalCommands) {
            String payload = xmlPayload.toString();
            payload = payload.replace("@@@cmd@@@", cmd);

            String response = PostClient.sendRequest(wpsUrl, payload);

            assertThat("Response is not an exception", response, not(containsString("ExceptionReport")));
            // assertThat("Response contains an echo of '" + cmd + "'", response, containsString(cmd));
        }
    }

}
