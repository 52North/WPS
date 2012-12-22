package org.n52.wps.commons;

import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import javanet.staxutils.IndentingXMLStreamWriter;
import javanet.staxutils.XMLStreamUtils;
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
import org.w3c.dom.Node;

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
    
    public static void copyXML(
            InputStream input,
            OutputStream output,
            boolean indent) throws IOException
    {
        try {
            copyXML(
                xmlInputFactory.createXMLStreamReader(input, "UTF-8"),
                xmlOutputFactory.createXMLStreamWriter(output, "UTF-8"),
                indent);
        } catch (XMLStreamException e) {
            throw new IOException("Error copying XML", e);
        }
    }
    
    public static void copyXML(
            Source input,
            OutputStream output,
            boolean indent) throws IOException
    {
        try {
            copyXML(
                xmlInputFactory.createXMLStreamReader(input),
                xmlOutputFactory.createXMLStreamWriter(output, "UTF-8"),
                indent);
        } catch (XMLStreamException e) {
            throw new IOException("Error copying XML", e);
        }

    }
    
    private static void copyXML(
            XMLStreamReader xmlStreamReader,
            XMLStreamWriter xmlStreamWriter,
            boolean indent) throws XMLStreamException
    {
        try {
            xmlStreamReader = new XMLUtil.WhiteSpaceRemovingDelegate(xmlStreamReader);
            if (indent) {
                xmlStreamWriter = new IndentingXMLStreamWriter(xmlStreamWriter);
            }
            XMLStreamUtils.copy(xmlStreamReader, xmlStreamWriter);
        } finally {
            if (xmlStreamReader != null ) {
                try { xmlStreamReader.close(); } catch (XMLStreamException e) { /* ignore */ }
            }
            if (xmlStreamWriter != null ) {
                try { xmlStreamWriter.close(); } catch (XMLStreamException e) { /* ignore */ }
            }
        }
    }
        
    public static class WhiteSpaceRemovingDelegate extends StreamReaderDelegate {
        WhiteSpaceRemovingDelegate(XMLStreamReader reader) {
            super(reader);
        }
        @Override public int next() throws XMLStreamException {
            int eventType;
            do {
                eventType = super.next();
            } while ( 
                (eventType == XMLStreamConstants.CHARACTERS && isWhiteSpace()) ||
                (eventType == XMLStreamConstants.CDATA && isWhiteSpace()) ||
                 eventType == XMLStreamConstants.SPACE);
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
}
