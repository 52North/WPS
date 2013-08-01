package org.n52.wps.server.request;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.StatusType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlValidationError;
import org.junit.Before;
import org.junit.Test;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.database.DatabaseFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author bpross-52n
 */
public class ExecuteRequestTest {

    private DocumentBuilderFactory fac;

	@Before
    public void setUp(){
		System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
	
		fac = DocumentBuilderFactory.newInstance();
		fac.setNamespaceAware(true);
    }
	
	@Test
    public void testUpdateStatusError() throws ExceptionReport, XmlException, IOException, SAXException, ParserConfigurationException {

		FileInputStream fis = new FileInputStream(new File("src/test/resources/LRDTCCorruptInputResponseDocStatusTrue.xml"));
		
		// parse the InputStream to create a Document
		Document doc = fac.newDocumentBuilder().parse(fis);
    	
    	ExecuteRequest request = new ExecuteRequest(doc);
    	
    	String exceptionText = "TestError";
    	
    	request.updateStatusError(exceptionText);
    	
    	File response = DatabaseFactory.getDatabase().lookupResponseAsFile(request.getUniqueId().toString());
    	
    	ExecuteResponseDocument responseDoc = ExecuteResponseDocument.Factory.parse(response);
    	
    	StatusType statusType = responseDoc.getExecuteResponse().getStatus();
    	
    	assertTrue(validateExecuteResponse(responseDoc));    	
    	assertTrue(statusType.isSetProcessFailed());
    	assertTrue(statusType.getProcessFailed().getExceptionReport().getExceptionArray(0).getExceptionTextArray(0).equals(exceptionText));
    		    	
    }
    
    private boolean validateExecuteResponse(ExecuteResponseDocument responseDoc) {
        XmlOptions xmlOptions = new XmlOptions();
        List<XmlValidationError> xmlValidationErrorList = new ArrayList<XmlValidationError>();
            xmlOptions.setErrorListener(xmlValidationErrorList);
        boolean valid = responseDoc.validate(xmlOptions);
        if (!valid) {
            System.err.println("Error validating process description for " + getClass().getCanonicalName());
            for (XmlValidationError xmlValidationError : xmlValidationErrorList) {
                System.err.println("\tMessage: " +  xmlValidationError.getMessage());
                System.err.println("\tLocation of invalid XML: " +
                     xmlValidationError.getCursorLocation().xmlText());
            }
        }
        return valid;
    }
}
