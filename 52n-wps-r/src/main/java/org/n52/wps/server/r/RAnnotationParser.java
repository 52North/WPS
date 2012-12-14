/**
 * ﻿Copyright (C) 2010
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

package org.n52.wps.server.r;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.r.syntax.RAnnotation;
import org.n52.wps.server.r.syntax.RAnnotationException;
import org.n52.wps.server.r.syntax.RAnnotationType;
import org.n52.wps.server.r.syntax.RAttribute;
import org.n52.wps.server.r.syntax.RSeperator;

public class RAnnotationParser {

    private static Logger LOGGER = Logger.getLogger(RAnnotationParser.class);
    
    /**
     * 
     * @param script
     * @throws RAnnotationException if script is invalid
     * @throws IOException
     * @throws ExceptionReport 
     */
    public static void validateScript(InputStream script, String wkn) throws RAnnotationException, IOException, ExceptionReport{
    	//TODO: improve this method to something more useful
    	
    	//try to parse annotations:
    	List<RAnnotation> annotations = parseAnnotationsfromScript(script);
    	//try to create process description:
    	RProcessDescriptionCreator descriptionCreator = new RProcessDescriptionCreator();
    	
    	//TODO: WPS.des and WPS.res should only occur once or not.
    	try {
			descriptionCreator.createDescribeProcessType(annotations, wkn);
		} catch (ExceptionReport e) {
			String message ="Script validation failed when testing process description creator.";
			LOGGER.error(message);
			throw e;
		}catch(RAnnotationException e){
			String message ="Script validation failed when testing process description creator.";
			LOGGER.error(message);
			throw e;
		}
    }

    // TODO: Improve process script validation
    public static List<RAnnotation> parseAnnotationsfromScript(InputStream inputScript) throws IOException,
            RAnnotationException {
        LOGGER.debug("Starting to parse annotations from script " + inputScript);

        BufferedReader lineReader = new BufferedReader(new InputStreamReader(inputScript));
        int lineCounter = 0;
        boolean isCurrentlyParsingAnnotation = false;
        StringBuilder annotationString = null;
        RAnnotationType annotationType = null;
        ArrayList<RAnnotation> annotations = new ArrayList<RAnnotation>();

        while (lineReader.ready()) {
            String line = lineReader.readLine();
            lineCounter++;
            
            if (line.contains("#")) { // is a comment
                if ( !line.startsWith("##")) { // is a double comment, do not use!
                    line = line.split("#", 2)[1];
                    if ( !isCurrentlyParsingAnnotation)
                        // searches for startKey - expressions in a line
                        for (RAnnotationType anot : RAnnotationType.values()) {
                            String startKey = anot.getStartKey().getKey();
                            if (line.contains(startKey)) {
                                if (LOGGER.isDebugEnabled())
                                    LOGGER.debug("Parsing annotation " + startKey);

                                // start parsing an annotation, which might spread several lines
                                line = line.split(RSeperator.STARTKEY_SEPARATOR.getKey(), 2)[1];
                                annotationString = new StringBuilder();
                                annotationType = anot;
                                isCurrentlyParsingAnnotation = true;

                                break;
                            }
                        }
                    try {
                        if (isCurrentlyParsingAnnotation) {
                            String endKey = RSeperator.ANNOTATION_END.getKey();
                            if (line.contains(endKey)) {
                                line = line.split(endKey, 2)[0];
                                isCurrentlyParsingAnnotation = false;
                                // last line for multiline annotation
                            }

                            annotationString.append(line);
                            if ( !isCurrentlyParsingAnnotation) {
                                HashMap<RAttribute, Object> attrHash = hashAttributes(annotationType,
                                                                                      annotationString.toString());
                                RAnnotation newAnnotation = new RAnnotation(annotationType, attrHash);
                                annotations.add(newAnnotation);

                                LOGGER.debug("Done parsing annotation " + annotationString.toString() + " >>> " + newAnnotation);
                            }
                        }
                    }
                    catch (RAnnotationException e) {
                        LOGGER.error("Invalid R script with wrong annotation in Line " + lineCounter
                                + "\n" + e.getMessage());
                    }
                }
            }
        }

        LOGGER.debug("Finished to parse annotations from script " + inputScript);
        return annotations;
    }

    private static HashMap<RAttribute, Object> hashAttributes(RAnnotationType anotType, String attributeString) throws IOException,
            RAnnotationException {
    	
    	if(anotType.equals(RAnnotationType.RESOURCE)){
    		return hashResourceAnnotation(attributeString);
    	}
        HashMap<RAttribute, Object> attrHash = new HashMap<RAttribute, Object>();
        StringTokenizer attrValueTokenizer = new StringTokenizer(attributeString,
                                                                 RSeperator.ATTRIBUTE_SEPARATOR.getKey());
        boolean iterableOrder = true;
        // iterates over the attribute sequence of an Annotation
        Iterator<RAttribute> attrKeyIterator = anotType.getAttributeSequence().iterator();

        // Important for sequential order: start attribute contains no value,
        // iteration starts from the second key
        attrKeyIterator.next();

        while (attrValueTokenizer.hasMoreElements()) {
            String attrValue = attrValueTokenizer.nextToken();
            if (attrValue.contains(RSeperator.ATTRIBUTE_VALUE_SEPARATOR.getKey())) {
                iterableOrder = false;

                // in the following case, the annotation contains no sequential order and
                // lacks an explicit attribute declaration --> Annotation cannot be interpreted
                // e.g. value1, value2, attribute9 = value9, value4 --> parser error for "value4"
            }
            else if ( !iterableOrder) {
                throw new RAnnotationException("Annotation contains no valid order: " + "\""
                        + anotType.getStartKey().getKey() + " " + attributeString + "\"");
            }

            // Valid annotations:
            // 1) Annotation with a sequential attribute order:
            // wps.in: name,description,0,1;
            // 2) Annotation with a partially sequential attribute order:
            // wps.in: name,description, maxOccurs = 1;
            // 3) Annotations without sequential order:
            // wps.des: abstract = example process, title = Example1;
            if (iterableOrder) {
                attrHash.put(attrKeyIterator.next(), attrValue.trim());

            }
            else {
                String[] keyValue = attrValue.split(RSeperator.ATTRIBUTE_VALUE_SEPARATOR.getKey());
                RAttribute attribute = anotType.getAttribute(keyValue[0].trim());
                String value = keyValue[1].trim();
                attrHash.put(attribute, value);
            }
        }
        return attrHash;
    }

	private static HashMap<RAttribute, Object> hashResourceAnnotation(
			String attributeString) {
		HashMap<RAttribute, Object> attributeHash = new HashMap<RAttribute, Object>();
        StringTokenizer attrValueTokenizer = new StringTokenizer(attributeString,
                RSeperator.ATTRIBUTE_SEPARATOR.getKey());
        String namedList = "list(";
        while (attrValueTokenizer.hasMoreElements()) {
        	String resourcefile = attrValueTokenizer.nextToken();
        	resourcefile = resourcefile.trim();
        	namedList +="\""+ resourcefile +"\" = "+"\""+R_Config.getInstance().getResourceDirURL()+"/"+resourcefile+"\"";
        	if(attrValueTokenizer.hasMoreElements()){
        		namedList+=", ";
        	}	
        }
        namedList+=")";
        attributeHash.put(RAttribute.NAMED_LIST, namedList);
		return attributeHash;
	}

    // Main method for tests:
    /*
     * public static void main(String[] args){ try { parseAnnotationsfromSkript(new FileInputStream("Idw.R"));
     * 
     * } catch (FileNotFoundException e) { System.out.println(e.getLocalizedMessage()); e.printStackTrace(); }
     * catch (IOException e) { System.out.println(e.getLocalizedMessage()); //e.printStackTrace(); }
     * 
     * 
     * try { parseAnnotationsfromSkript(new FileInputStream("Idw_falsch1.R"));
     * 
     * } catch (FileNotFoundException e) { System.out.println(e.getLocalizedMessage()); e.printStackTrace(); }
     * catch (IOException e) { System.out.println(e.getLocalizedMessage()); //e.printStackTrace(); }
     * 
     * try { parseAnnotationsfromSkript(new FileInputStream("Idw_falsch2.R"));
     * 
     * } catch (FileNotFoundException e) { System.out.println(e.getLocalizedMessage()); e.printStackTrace(); }
     * catch (IOException e) { System.out.println(e.getLocalizedMessage()); //e.printStackTrace(); }
     * 
     * 
     * try { parseAnnotationsfromSkript(new FileInputStream("Idw_falsch3.R"));
     * 
     * } catch (FileNotFoundException e) { System.out.println(e.getLocalizedMessage()); e.printStackTrace(); }
     * catch (IOException e) { System.out.println(e.getLocalizedMessage()); //e.printStackTrace(); }
     * 
     * try { parseAnnotationsfromSkript(new FileInputStream("Idw_falsch4.R"));
     * 
     * } catch (FileNotFoundException e) { System.out.println(e.getLocalizedMessage()); e.printStackTrace(); }
     * catch (IOException e) { System.out.println(e.getLocalizedMessage()); //e.printStackTrace(); } }
     */
}
