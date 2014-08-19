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
package org.n52.wps.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.ServerDocument.Server;
import org.n52.wps.commons.WPSConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tkunicki
 */
public class ResponseURLFilter implements Filter {

    private final static Logger LOGGER = LoggerFactory.getLogger(ResponseURLFilter.class);
    
    public final static String PROP_responseURLFilterEnabled = "responseURLFilterEnabled";

    private String configURLString;
    private boolean enabled;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        
        Server server = WPSConfig.getInstance().getWPSConfig().getServer();
        
        // Build URL from WPS configuration.  This is the
        // hardcoded URL that we expect to see in reponses and would like to
        // replace with the URL from the HTTP request.
        configURLString =  "http://" +
            server.getHostname() + ":" +
            server.getHostport() + "/" +
            server.getWebappPath();
        
        // Is filtering enabled in WPS configuration?
        Property[] serverProperties = server.getPropertyArray();
        for (Property serverProperty : serverProperties) {
            if (/* serverProperty.getActive() && */ PROP_responseURLFilterEnabled.equals(serverProperty.getName())) {
                enabled = Boolean.parseBoolean(serverProperty.getStringValue());
            }
        }
        
        if (enabled) {
            LOGGER.info("Response URL filtering enabled using base URL of {}", configURLString);
        } else {
            LOGGER.info("Response URL filtering disabled.");
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest requestHTTP = (request instanceof HttpServletRequest) ?
                (HttpServletRequest) request : null;
        HttpServletResponse responseHTTP = (response instanceof HttpServletResponse) ?
                (HttpServletResponse) response : null;
        
        if (enabled && requestHTTP != null && responseHTTP != null) {
            
            String requestURLString = extractRequestURLString(requestHTTP);
           
            // extract servlet path from request URL
            String baseURLString = requestURLString.replaceAll("/[^/]*$", "");
            
            LOGGER.info("Wrapping response for URL filtering");
            chain.doFilter(request, new BaseURLFilterHttpServletResponse(
                    responseHTTP, configURLString, baseURLString));
        } else {
            LOGGER.warn("Unable to to wrap response for URL filtering");
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        // nothing to do yet
    }

    protected static String extractRequestURLString(HttpServletRequest request) {
        return request.getRequestURL().toString();
    }

    private static class BaseURLFilterHttpServletResponse extends HttpServletResponseWrapper {

        private final String configURLString;
        public final String requestURLString;

        public BaseURLFilterHttpServletResponse(HttpServletResponse response, String configURLString, String requestURLString) {
            super(response);
            this.configURLString = configURLString;
            this.requestURLString = requestURLString;
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            String contentType = getResponse().getContentType();
            if (contentType == null || contentType.startsWith("text/xml") || contentType.startsWith("application/xml")) {
                LOGGER.info("Content-type: {}, response URL filtering enabled for response to {}", contentType, requestURLString);
                return new ServletOutputStreamWrapper(
                        getResponse().getOutputStream(),
                        configURLString,
                        requestURLString);
            } else {
                LOGGER.info("Content-type: {}, response URL filtering disabled for response to {}", contentType, requestURLString);
                return getResponse().getOutputStream();
            }
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return new PrintWriter(getOutputStream()); 
        }
    }

    private static class ServletOutputStreamWrapper extends ServletOutputStream {

        private final ServletOutputStream outputStream;

        private ByteBuffer find;
        private ByteBuffer replace;
        private boolean match;

        public ServletOutputStreamWrapper(ServletOutputStream outputStream, String find, String replace) {
            this.outputStream = outputStream;
            this.find = ByteBuffer.wrap(find.getBytes());
            this.replace = ByteBuffer.wrap(replace.getBytes());
        }

        @Override
        public void write(int i) throws IOException {
            byte b = (byte)(i & 0xff);
            if (match) {
                if(find.get() == b) {
                    if (!find.hasRemaining()) {
                        // COMPLETE MATCH
                        // 1) write out replacement buffer
                        // 2) unset 'match' flag
                        outputStream.write(replace.array());
                        match = false;
                    } // else { /* POTENTIAL MATCH ongoing, writes deferred */ } 
                } else {
                    // FAILED MATCH
                    // 1) write out portion of 'find' buffer that matched
                    // 2) write out the current byte that caused mismatch
                    // 3) unset 'match' flag
                    outputStream.write(find.array(), 0, find.position() - 1);
                    outputStream.write(b);
                    match = false;
                }
            } else {
                if (b == find.get(0)) {
                    // POTENTIAL MATCH started, write deferred
                    // - set 'match' flag to true for next write call
                    // - position 'find' buffer at next byte for next check
                    match = true;
                    find.position(1);
                } else {
                    // NO MATCH, just pass byte through to underlying outputstream
                    outputStream.write(b);
                }
            }
        }

        @Override
        public void write(byte[] bytes) throws IOException {
            write(bytes, 0, bytes.length);
        }

        @Override
        public void write(byte[] b, int o, int l) throws IOException {
            for (int i = 0; i < l; ++i) { write(b[o + i]); }
        }

        @Override
        public void close() throws IOException {
            if (match) {
                // FAILED MATCH, complete deferred writes
                outputStream.write(find.array(), 0, find.position());
                match = false;
            }
            super.close();
            outputStream.close();
        }

        @Override
        public void flush() throws IOException {
            super.flush();
            outputStream.flush();
        }

		@Override
		public boolean isReady() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void setWriteListener(WriteListener arg0) {
			// TODO Auto-generated method stub
			
		}
    }
}
