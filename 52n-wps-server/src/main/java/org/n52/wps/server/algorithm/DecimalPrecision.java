package org.n52.wps.server.algorithm;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.SAXParser;

import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.server.AlgorithmParameterException;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;

public class DecimalPrecision extends AbstractAlgorithm {
	private static Logger LOGGER = Logger.getLogger(DecimalPrecision.class);
	private static String SYNONYM = "DecimalPrecision"; // The other name besides the class name incl. package for the process.
	private static String RESPONSE_IDENTIFIER = "result"; // This should be standardized!
	private String errors = "";
	
	/** 
	 * Constructor. Initialization of parameters.
	 */
	public DecimalPrecision() {
		super(SYNONYM);
	}
	
	/** 
	 * This getter, even if the super class has one, is needed by the algorithm repository.
	 */
	public String getName() {
		return DecimalPrecision.SYNONYM;
	}
	
	/**
	 * Getter for errors.
	 */
	public String getErrors() {
		return errors;
	}

	/**
	 * The actual process method.
	 */
	public Map run(Map inputData, Map parameters) {
		int decimals = 0;
		
		// Note that I put the decimal coutn here as optional.
		if(parameters.containsKey("Precision")) {
			decimals = Integer.parseInt((String) parameters.get("Precision"));
		}
		
		if(!inputData.containsKey("Data")) {
			throw new AlgorithmParameterException("Input data was not given to the process.");
		}
		
		// Create the parser we'll use.  The parser implementation is a 
        // Xerces class, but we use it only through the SAX XMLReader API.
        XMLReader parser = new SAXParser();
        // Create a object of StringBuffer class.
        StringBuffer outputDocumentBuffer = new StringBuffer();
        
        try {
	        // Specify that we don't want validation.  This is the SAX2
	        // API for requesting parser features. Non-validation is
	        // actually the default, so this line isn't really necessary.
	        parser.setFeature("http://xml.org/sax/features/validation", false);
	        // Ensure namespace processing is on. This also is a default value.
	        parser.setFeature("http://xml.org/sax/features/namespaces", true);
	        // Instantiate this class to provide handlers for the parser and
	        // to run parser.
	        SimpleGML3DecimalPrecisionParser handler = new 
	        		SimpleGML3DecimalPrecisionParser(outputDocumentBuffer, decimals);
	        // Tell the parser about the handlers.
	        parser.setContentHandler(handler);
	        parser.setErrorHandler(handler);
	        // Comments are also parsed.
	        parser.setProperty(
	                "http://xml.org/sax/properties/lexical-handler",
	                 handler);
	        // Create an input source that describes the file to parse.
	        InputSource input = setXMLProlog((String) inputData.get("Data"));
	        
	        // Filter could not be used, because of the bounded by tags need parent knowledge.
	        // NamespaceFilter filter = new NamespaceFilter(outputDocumentBuffer, allowedURIs);
	        // filter.setParent(parser);
	        // filter.setContentHandler(handler);
	        // filter.setErrorHandler(handler);
	        // filter.parse(input); // filter calls super(parent) -> parser.parse(input);  
	        
	        // Actual parsing and transforming the coordinates.
	        parser.parse(input);
        } catch(SAXNotRecognizedException sax_nr_ex) {
        	LOGGER.error(sax_nr_ex.getMessage());
        	throw new RuntimeException(sax_nr_ex);
        } catch(SAXNotSupportedException sax_ns_ex) {
        	LOGGER.error(sax_ns_ex.getMessage());
        	throw new RuntimeException(sax_ns_ex);
        } catch(SAXParseException sax_parse_ex) {
        	// Document was not well-formed.
        	LOGGER.error(sax_parse_ex.getMessage());
        	throw new RuntimeException(sax_parse_ex);
        } catch(IOException io_ex) {
        	LOGGER.error(io_ex.getMessage());
        	throw new RuntimeException(io_ex);
        } catch(SAXException sax_ex) {
        	LOGGER.error(sax_ex.getMessage());
        	throw new RuntimeException(sax_ex);
        }
        
		HashMap<String,Object> resultHash = new HashMap<String,Object>();
		resultHash.put(RESPONSE_IDENTIFIER, outputDocumentBuffer.toString());
		return resultHash;
	}
	
	private InputSource setXMLProlog(String input) {
		String prolog = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
		
		// Check if the string already has a prolog.
		if(input.substring(0,6).compareTo("<?xml ") == 0) {
			prolog = "";
		}
		return new InputSource(new StringReader(prolog + input));
	}
}

class SimpleGML3DecimalPrecisionParser extends DefaultHandler2 {
	private static Logger LOGGER = Logger.getLogger(SimpleGML3DecimalPrecisionParser.class);
	private int decimals = 0;
	StringBuffer outputDocumentBuffer = null;
	String data = "";
	boolean coordinateData = false;
	// Variables needed to put namespaces into the return file to the correct place.
	private String namespaces="";
	private boolean isFirstSet = false;

	/** Constructor of parser.
	 */
    public SimpleGML3DecimalPrecisionParser(
    		StringBuffer outputDocumentBuffer, int decimals) 
    		throws SAXException {
    	super();
    	this.outputDocumentBuffer = outputDocumentBuffer;
    	this.decimals = decimals;
    }

    /**
     * Called at the beginning of parsing.
     */ 
    public void startDocument() {
    }
    
	/**
	 * Handling comments.
	 */
	public void comment(char[] buffer, int start, int length)
            throws SAXException  {
		// No additional white spaces needed.
		// this.outputDocumentBuffer.append("<!--" + new String(buffer, start, length) + "-->");
	}

	public void startCDATA() {}
	
	public void endCDATA() {}
	
	public void startDTD(String arg1, String arg2, String arg3) {}
	
	public void endDTD() {}
	
	public void startEntity(String arg) {}
	
	public void endEntity(String arg) {} 
	
    /** Setting the allowed prefixes, according to the prefix mappings,
	 * based on the allowed URIs.
	 * 
	 * @note The prefix mappings are not forwarded to the super class!
	 * @todo Should mappings be flushed?
	 */
	public void startPrefixMapping(String prefix, String uri) 
    	throws SAXException {
		
		// URI's that are declared in the WPS request might have no prefixes.
		if(prefix!=null && prefix.length()!=0)
			this.namespaces += " xmlns:" + prefix + "=\"" + uri + "\"";
	}
	
    /** 
     * When the parser encounters plain text (not XML elements), it calls
     * this method.
     * 
     * @param buffer An character array found by the parser.
     * @param start The index of the first character.
     * @param length Number of character calculated from the beginning point.
     * @note This method may be called multiple times, even with no
     * intervening elements. This is why the values gotten through this 
     * method have to be stored.
     */
    public void characters(char[] buffer, int start, int length) 
    		throws SAXException {
    	if(this.coordinateData)
    		this.data += new String(buffer, start, length);
    	else
    		this.outputDocumentBuffer.append(new String(buffer, start, length));
    }

    /**
     * Beginning of each new element.
     * 
     * @param namespaceURI The namespace URI. Can have zero length. For example "http://www.opengis.net/gml".
     * @param localName The unqualified name of the element. For example "PointPropertyType".
     * @param qname Qualified name of the element.  This is the 
     * namespace prefix compined with the local name of the element.
     * For example "gml:PointPropertyType".
     * @param attributes An Attributes object that contains all attributes for this element.
     */
    public void startElement(String namespaceURI, String localName,
            String qname, Attributes attributes) throws SAXException {
    	if (!"".equals(qname)) {
    		String[] qualifiedNameParts = qname.split(":");
    	    if(qualifiedNameParts.length != 2) {
    	    	LOGGER.error("The qualified name did not have a single colon separating the prefix and namespace URI.");
    	    	throw new SAXException("The qualified name did not have a single colon separating the prefix and namespace URI.");
    	    }
    	    if(qualifiedNameParts[1].compareTo("pos")==0 ||
    	    		qualifiedNameParts[1].compareTo("posList")==0)
    	    	this.coordinateData = true;
    	    this.outputDocumentBuffer.append("<"+qname);
    	} else if(localName != "") {
    		if(localName.compareTo("pos")==0 ||
    				localName.compareTo("posList")==0)
    			this.coordinateData = true;
    		this.outputDocumentBuffer.append("<"+localName);
    	} else {
    		LOGGER.error("No element name was found.");
	    	throw new SAXException("No element name was found.");
    	}
    	
    	// Adding namespace declaration, if some exists.
    	if(!this.namespaces.equals("") && !isFirstSet) {
    		this.outputDocumentBuffer.append(this.namespaces);
    		this.isFirstSet=true;
    	}
    	
    	// Handling attributes.
    	if(attributes!=null) {
    		for (int i = 0; i < attributes.getLength(); i++) {
				String attributeName = "";
				if(!attributes.getQName(i).equals("")) {
					attributeName = attributes.getQName(i);
				} else {
					attributeName = attributes.getLocalName(i);
				}
                if ("".equals(attributeName)) { 
                	attributeName = attributes.getQName(i);
                }
                if(attributeName.compareTo("") !=0 ) {
                	this.outputDocumentBuffer.append(" " + attributeName + "=");
                	this.outputDocumentBuffer.append("\""  + attributes.getValue(i) + "\"");
                }
            }
    	}
    	this.outputDocumentBuffer.append(">");
    }

    /** End of element encountered.
     * 
     * @param namespaceURI The namespace URI. Can have zero length.
     * @param localName The unqualified name of the element. 
     * @param qname Qualified name of the element.  This is the 
     * namespace prefix compined with the local name of the element.
     */
    public void endElement(String namespaceURI, String localName, String qname)
    		throws SAXException, SAXParseException {
    	if(this.coordinateData)
    		this.processAttributes(this.data);
    	if (!"".equals(qname)) {
    	    this.outputDocumentBuffer.append("</"+qname+">");
    	} else if(localName != "") {
    		this.outputDocumentBuffer.append("</"+localName+">");
    	} else {
    		LOGGER.error("No element name was found.");
	    	throw new SAXException("No element name was found.");
    	}
    }

    /** Processing of the attributes data. 
     * 
     * @param attributesData A string of attributes.
     * @return Returns the modified attributes data
     * @note The method does not add at the end of the returned String a '>' character.
     * The data should also be complete, not just part of the attributes.
     */
    private void processAttributes(String attributesData) 
    		throws SAXException, AlgorithmParameterException {
    	String[] coordinates = attributesData.split(" "); // Space is used to separate ordinates (values).
    	for(int i=0; i<coordinates.length; i++) {
    		this.outputDocumentBuffer.append(
    				roundToStr(Double.parseDouble(coordinates[i]), this.decimals));
    		if(i+1 != coordinates.length)
    			this.outputDocumentBuffer.append(",");
    	}
    	this.data = "";
    	this.coordinateData = false;
    }
    
    /**
     * Called at the end of parsing.
     */ 
    public void endDocument() {
    	
    }

    /**
     * Issue a warning.
     */
    public void warning(SAXParseException exception) throws SAXException  {
    	LOGGER.error("WARNING: line " + exception.getLineNumber() + ": "+
                           exception.getMessage());
    	throw(exception);
    }

    /** 
     * Report a parsing error.
     */
    public void error(SAXParseException exception) throws SAXException  {
    	LOGGER.error("ERROR: line " + exception.getLineNumber() + ": " +
                           exception.getMessage());
    	throw(exception);
    }

    /** 
     * Report a non-recoverable error and exit
     */
    public void fatalError(SAXParseException exception) throws SAXException {
    	LOGGER.error("FATAL: line " + exception.getLineNumber() + ": " +
                           exception.getMessage());
        throw(exception);
    }

    /** Rounding of doubles to the desired precision.
	 * 
	 * @param d Number to round.
	 * @param decimalPlace Place of decimal. Has to be non-negative.
	 * @return Returns rounded number as strings.
	 * @note Zeros are added to the end of the returned string
	 * if there are else not enough decimals.
	 */
    private static String roundToStr(double d, int decimalPlace)
    		throws RuntimeException {
    	String number = ""+round(d, decimalPlace);
    	String[] decimals = number.split("\\.");
    	
    	if(decimalPlace == 0) {
    		// Returning just a integer.
    		return decimals[0];
    	}
    	if(decimals.length != 1 && decimals.length != 2)
    		throw new RuntimeException("A number was not valid!");
    	int decimalCount = 0;
    	if(decimals.length != 1 ) {
    		decimalCount = decimals[1].length();
    		for(int i=0; i<decimalPlace-decimalCount; i++)  {
        		decimals[1] += "0";
        	}
    		return decimals[0] + "." + decimals[1]; 
    	} else {
    		String decimalStr = "";
    		for(int i=0; i<decimalPlace-decimalCount; i++)  {
    			decimalStr += "0";
            }
    		return decimals[0] + "." + decimalStr; 
    	}
    }
    
	/** Rounding of doubles to the desired precision.
	 * 
	 * @param d Number to round.
	 * @param decimalPlace Place of decimal. Has to be non-negative.
	 * @return Returns rounded number.
	 */
	private static double round(double d, int decimalPlace){
	    // see the Javadoc about why we use a String in the constructor
	    // http://java.sun.com/j2se/1.5.0/docs/api/java/math/BigDecimal.html#BigDecimal(double)
		if(decimalPlace >= 0) {	
			BigDecimal bd = new BigDecimal(Double.toString(d));
	    	bd = bd.setScale(decimalPlace,BigDecimal.ROUND_HALF_UP);
	    	return bd.doubleValue();
		}
		return d;
	}
}
