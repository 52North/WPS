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

package org.n52.wps.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.n52.wps.server.database.DatabaseFactory;
import org.n52.wps.server.database.IDatabase;
import org.n52.wps.commons.MIMEUtil;
import org.n52.wps.commons.XMLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetrieveResultServlet extends HttpServlet {

    private final static Logger LOGGER = LoggerFactory.getLogger(RetrieveResultServlet.class);
    private static final long serialVersionUID = -268198171054599696L;
    // This is required for URL generation for response documents.
    public final static String SERVLET_PATH = "RetrieveResultServlet";
    // in future parameterize
    private final boolean indentXML = false;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // id of result to retrieve.
        String id = request.getParameter("id");

        // return result as attachment (instructs browser to offer user "Save" dialog)
        String attachment = request.getParameter("attachment");

        if (StringUtils.isEmpty(id)) {
            errorResponse("id parameter missing", response);
        } else {

            IDatabase db = DatabaseFactory.getDatabase();
            String mimeType = db.getMimeTypeForStoreResponse(id);
            long contentLength = db.getContentLengthForStoreResponse(id);

            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = db.lookupResponse(id);

                if (inputStream == null) {
                    errorResponse("id " + id + " is unknown to server", response);
                } else if (mimeType == null) {
                    errorResponse("Unable to determine mime-type for id " + id, response);
                } else {
                    String suffix = MIMEUtil.getSuffixFromMIMEType(mimeType).toLowerCase();

                    // if attachment parameter unset, default to false for mime-type of 'xml' and true for everything else.
                    boolean useAttachment = (StringUtils.isEmpty(attachment) && !"xml".equals(suffix)) || Boolean.parseBoolean(attachment);
                    if (useAttachment) {
                        String attachmentName = (new StringBuilder(id)).append('.').append(suffix).toString();
                        response.addHeader("Content-Disposition", "attachment; filename=\"" + attachmentName + "\"");
                    }

                    response.setContentType(mimeType);

                    if ("xml".equals(suffix)) {

                        // NOTE:  We don't set "Content-Length" header, xml may be modified

                        // need these to work around aggressive IE 8 caching.
                        response.addHeader("Cache-Control", "no-cache, no-store");
                        response.addHeader("Pragma", "no-cache");
                        response.addHeader("Expires", "-1");

                        try {
                            outputStream = response.getOutputStream();
                        } catch (IOException e) {
                            throw new IOException("Error obtaining output stream for response", e);
                        }
                        copyResponseAsXML(inputStream, outputStream, useAttachment || indentXML, id);
                    } else {

                        if (contentLength > -1) {
                            // Can't use response.setContentLength(...) as it accepts an int (max of 2^31 - 1) ?!
                            // response.setContentLength(contentLength);
                            response.setHeader("Content-Length", Long.toString(contentLength));
                        } else {
                            LOGGER.warn("Content-Length unknown for response to id {}", id);
                        }

                        try {
                            outputStream = response.getOutputStream();
                        } catch (IOException e) {
                            throw new IOException("Error obtaining output stream for response", e);
                        }
                        copyResponseStream(inputStream, outputStream, id, contentLength);
                    }
                }
            } catch (Exception e) {
                logException(e);
            } finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }
        }
    }

    protected void errorResponse(String error, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        PrintWriter writer = response.getWriter();
        writer.write("<html><title>Error</title><body>" + error + "</body></html>");
        writer.flush();
        LOGGER.warn("Error processing response: " + error);
    }

    protected void copyResponseStream(
            InputStream inputStream,
            OutputStream outputStream,
            String id,
            long contentLength) throws IOException {
        long contentWritten = 0;
        try {
            byte[] buffer = new byte[8192];
            int bufferRead;
            while ((bufferRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bufferRead);
                contentWritten += bufferRead;
            }
        } catch (IOException e) {
            String exceptionMessage = contentLength > -1
                    ? String.format("Error writing response to output stream for id %s, %d of %d bytes written", id, contentWritten, contentLength)
                    : String.format("Error writing response to output stream for id %s, %d bytes written", id, contentWritten);
            throw new IOException(exceptionMessage, e);
        }
        LOGGER.info("{} bytes written in response to id {}", contentWritten, id);
    }

    protected void copyResponseAsXML(
            InputStream inputStream,
            OutputStream outputStream,
            boolean indent,
            String id) throws IOException {
        try {
            XMLUtil.copyXML(inputStream, outputStream, indent);
        } catch (IOException e) {
            throw new IOException("Error writing XML response for id " + id, e);
        }
    }

    private void logException(Exception exception) {
        StringBuilder errorBuilder = new StringBuilder(exception.getMessage());
        Throwable cause = getRootCause(exception);
        if (cause != exception) {
            errorBuilder.append(", exception message: ").append(cause.getMessage());
        }
        LOGGER.error(errorBuilder.toString());
    }

    public static Throwable getRootCause(Throwable t) {
        return t.getCause() == null ? t : getRootCause(t.getCause());
    }
}