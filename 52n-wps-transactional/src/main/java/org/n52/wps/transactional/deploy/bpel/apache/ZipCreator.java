/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.n52.wps.transactional.deploy.bpel.apache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import org.w3c.dom.Node;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

/**
 *
 * @author hoffmannn
 */
public class ZipCreator {
    
    public static final int BUF_SIZE = 8192;

  public static final int STATUS_OK          = 0;
  public static final int STATUS_OUT_FAIL    = 1; // No output stream.
  public static final int STATUS_ZIP_FAIL    = 2; // No zipped file
  public static final int STATUS_IN_FAIL     = 4; // No input stream.
  public static final int STATUS_UNZIP_FAIL  = 5; // No decompressed zip file

  private static String fMessages [] = {
    "Operation succeeded",
    "Failed to create output stream",
    "Failed to create zipped file",
    "Failed to open input stream",
    "Failed to decompress zip file"
  };

  /** Return a brief message for each status number. **/
  public static String getStatusMessage (int msg_number) {
    return fMessages [msg_number];
  }
  
    public static int makeZIP(String processID, Node suitcase, Node workflow, Node clientWSDL, Map<Integer, Node> wsdlList, String zipPath) throws IOException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException{
    
    
    FileOutputStream zip_output = new FileOutputStream(zipPath);

    ZipOutputStream zip_out_stream = new ZipOutputStream(zip_output);
   
    try {
      // Use the file name for the ZipEntry name.
      ZipEntry zip_entry = new ZipEntry ("bpel.xml");
      zip_out_stream.putNextEntry (zip_entry);
      ByteArrayOutputStream outputStream = createOutputStreamFromNode(suitcase);
      zip_out_stream.write(outputStream.toByteArray());
      zip_out_stream.closeEntry();
      
      outputStream = createOutputStreamFromNode(workflow);
      zip_out_stream.putNextEntry (new ZipEntry ("workflow.bpel"));
      zip_out_stream.write(outputStream.toByteArray());
      zip_out_stream.closeEntry();
      
      outputStream = createOutputStreamFromNode(clientWSDL);
      zip_out_stream.putNextEntry (new ZipEntry ("client.wsdl"));
      zip_out_stream.write(outputStream.toByteArray());
      zip_out_stream.closeEntry();
      
      for (Integer index : wsdlList.keySet()){
            outputStream = createOutputStreamFromNode(wsdlList.get(index));
            zip_out_stream.putNextEntry (new ZipEntry ("wps"+index+".wsdl"));
            zip_out_stream.write(outputStream.toByteArray());
            zip_out_stream.closeEntry();
      }
    }
    catch (IOException e) {
      return STATUS_ZIP_FAIL;
    }
    // Close up the output file
    try {
        zip_out_stream.flush();
        zip_out_stream.close ();
    }
    catch (IOException e) {}

    zip_output.flush();
    zip_output.close();
    
    return STATUS_OK;

  } // zipFile


    
    private static ByteArrayOutputStream createOutputStreamFromNode(Node node) throws ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException {
		//System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.sun.org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = factory.newDocumentBuilder();
		Document tempDocument = documentBuilder.newDocument();
		Node importedNode = tempDocument.importNode(node, true);
		tempDocument.appendChild(importedNode);
        // Prepare the DOM document for writing
        Source source = new DOMSource(tempDocument);

        // Prepare the output file
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Result result = new StreamResult(outputStream);

        // Write the DOM document to the file
        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.transform(source, result);
        return outputStream;
	}
    
}
