/**
 * ﻿Copyright (C) 2006 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.wps.commons;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import javanet.staxutils.IndentingXMLStreamWriter;
import javanet.staxutils.XMLStreamUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.util.StreamReaderDelegate;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Node;

import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;

/**
 * 
 * @author tkunicki
 */
public class XMLUtil {

    private final static XMLOutputFactory xmlOutputFactory;
    private final static XMLInputFactory xmlInputFactory;

    static {
        xmlInputFactory = new WstxInputFactory();
        xmlOutputFactory = new WstxOutputFactory();
    }

    public static XMLInputFactory getInputFactory() {
        return xmlInputFactory;
    }

    public static XMLOutputFactory getOutputFactory() {
        return xmlOutputFactory;
    }

    public static void copyXML(InputStream input, OutputStream output, boolean indent) throws IOException {
        try {
            copyXML(xmlInputFactory.createXMLStreamReader(input, "UTF-8"),
                    xmlOutputFactory.createXMLStreamWriter(output, "UTF-8"),
                    indent);
        }
        catch (XMLStreamException e) {
            throw new IOException("Error copying XML", e);
        }
    }

    public static void copyXML(Source input, OutputStream output, boolean indent) throws IOException {
        try {
            copyXML(xmlInputFactory.createXMLStreamReader(input),
                    xmlOutputFactory.createXMLStreamWriter(output, "UTF-8"),
                    indent);
        }
        catch (XMLStreamException e) {
            throw new IOException("Error copying XML", e);
        }

    }

    private static void copyXML(XMLStreamReader xmlStreamReader, XMLStreamWriter xmlStreamWriter, boolean indent) throws XMLStreamException {
        try {
            WhiteSpaceRemovingDelegate xmlStreamReader2 = new XMLUtil.WhiteSpaceRemovingDelegate(xmlStreamReader);
            XMLStreamWriter xmlStreamWriter2 = xmlStreamWriter;
            if (indent) {
                xmlStreamWriter2 = new IndentingXMLStreamWriter(xmlStreamWriter);
            }
            XMLStreamUtils.copy(xmlStreamReader2, xmlStreamWriter2);
        }
        finally {
            if (xmlStreamReader != null) {
                try {
                    xmlStreamReader.close();
                }
                catch (XMLStreamException e) { /* ignore */
                }
            }
            if (xmlStreamWriter != null) {
                try {
                    xmlStreamWriter.close();
                }
                catch (XMLStreamException e) { /* ignore */
                }
            }
        }
    }

    public static class WhiteSpaceRemovingDelegate extends StreamReaderDelegate {
        WhiteSpaceRemovingDelegate(XMLStreamReader reader) {
            super(reader);
        }

        @Override
        public int next() throws XMLStreamException {
            int eventType;
            do {
                eventType = super.next();
            } while ( (eventType == XMLStreamConstants.CHARACTERS && isWhiteSpace())
                    || (eventType == XMLStreamConstants.CDATA && isWhiteSpace())
                    || eventType == XMLStreamConstants.SPACE);
            return eventType;
        }
    }

    public static String nodeToString(Node node) throws TransformerFactoryConfigurationError, TransformerException {
        StringWriter stringWriter = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new DOMSource(node), new StreamResult(stringWriter));

        return stringWriter.toString();
    }
    
    public static XmlObject qualifySubstitutionGroup(XmlObject xobj, QName newInstance, SchemaType newType) {
        XmlObject substitute = null;
        if (newType != null) {
           substitute = xobj.substitute(newInstance, newType);
            if (substitute != null && substitute.schemaType() == newType &&
                  substitute.getDomNode().getLocalName().equals(newInstance.getLocalPart())) {
               return substitute;
            }
        }
        
         XmlCursor cursor = xobj.newCursor();
         cursor.setName(newInstance);
         QName qName = new QName("http://www.w3.org/2001/XMLSchema-instance", "type");
         cursor.removeAttribute(qName);
         cursor.dispose();
         
         return null;
     }
}
