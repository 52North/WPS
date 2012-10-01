
package org.n52.wps.server.r.syntax;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.n52.wps.io.data.IData;
import org.n52.wps.server.r.RAnnotationException;

/**
 * Defines Syntax and Semantics for Annotations in R Skripts
 * 
 * Syntax in (raw) BNF: <RAnnotation> ::= <StartKey> <AttributeSequence> <EndKey> <StartKey> <Attributequence>
 * ::= <RAnnotationTypeInstance>.getStartKey() <RAnnotationTypeInstance>.getAttributeSequence() <EndKey> ::=
 * RSeparator.ANNOTATION_END.getKey() <AttributeSequence> ::= {<RAttributeInstance>.getKey()
 * ATRIBUTE_VALUE_SEPARATOR} <Attributevalue> {ATTRIBUTE_SEPARATOR <RAttributeSequence>}
 * 
 * @author Matthias Hinz
 */
public class RAnnotation {

    private RAnnotationType type;
    private HashMap<RAttribute, String> attributeHash;
    static Logger LOGGER = Logger.getLogger(RAnnotation.class);
    //public static String WPS_OFF_START = "wps.off:";
    //public static String WPS_OFF_END = "wps.off.end;";

    /**
     * 
     * @param type
     * @param attributeHash
     * @throws IOException
     *         if AttributHash is not valid for any cause
     * @throws RAnnotationException
     */
    public RAnnotation(RAnnotationType type, HashMap<RAttribute, String> attributeHash) throws IOException,
            RAnnotationException {
        super();
        this.type = type;
        type.validDescription(attributeHash);
        this.attributeHash = attributeHash;

        LOGGER.debug("NEW " + toString());
    }

    public RAnnotationType getType() {
        return type;
    }

    // public HashMap<RAttribute, String> getAttributeHash() {
    // return attributeHash;
    // }

    public String getAttribute(RAttribute attr) {
        String out = attributeHash.get(attr);
        Object def = attr.getDefValue();
        if (out == null && attr.getDefValue() != null)
            if (def.getClass() == RAttribute.class)
                return getAttribute((RAttribute) def);
            else
                return "" + attr.getDefValue();
        else if (attr == RAttribute.ENCODING)
            return getRDataType().encoding;
        if (attr == RAttribute.SCHEMA)
            return getRDataType().schema;
        return out;
    }

    public static List<RAnnotation> filterAnnotations(List<RAnnotation> annotations,
                                                      RAnnotationType type,
                                                      RAttribute attribute,
                                                      String value) {
        LinkedList<RAnnotation> out = new LinkedList<RAnnotation>();
        for (RAnnotation annotation : annotations) {
            // type filter:
            if (type == null || annotation.getType() == type) {
                // attribute - value filter:
                if (attribute == null || value == null || annotation.getAttribute(attribute).equalsIgnoreCase(value)) {
                    out.add(annotation);
                }
            }
        }
        return out;
    }

    public static List<RAnnotation> filterAnnotations(List<RAnnotation> annotations, RAttribute attribute, String value) {
        return filterAnnotations(annotations, null, attribute, value);
    }

    public static List<RAnnotation> filterAnnotations(List<RAnnotation> annotations, RAnnotationType type) {
        return filterAnnotations(annotations, type, null, null);
    }

    /**
     * 
     * @param rClass
     *        - value referring to RAttribute.TYPE
     * @return null or supported IData class for rClass - string
     * @throws RAnnotationException
     */
    public static Class< ? extends IData> getDataClass(String rClass) throws RAnnotationException {
        RTypeDefinition rType = RDataType.getType(rClass);
        return rType.getIDataClass();
    }

    public Class< ? extends IData> getDataClass() throws RAnnotationException {
        String rClass = getAttribute(RAttribute.TYPE);
        return getDataClass(rClass);
    }

    /**
     * Checks if the type - argument of an annotation refers to complex data
     * 
     * @return
     * @throws RAnnotationException
     */
    public static boolean isComplex(String rClass) throws RAnnotationException {
        return RDataType.getType(rClass).isComplex();

    }

    public RDataType getRDataType() {
        return RDataType.getType(getAttribute(RAttribute.TYPE));
    }

    /**
     * @return true, if the type attribute of an Annotation refers to a complex data type
     * @throws RAnnotationException
     */
    public boolean isComplex() throws RAnnotationException {
        return isComplex(this.getAttribute(RAttribute.TYPE));
    }

    /**
     * 
     * @return null or supported ProcessdescriptionType
     * @throws RAnnotationException
     */
    public String getProcessDescriptionType() throws RAnnotationException {
        String type = getAttribute(RAttribute.TYPE);
        RTypeDefinition rdt = RDataType.getType(type);
        if (rdt != null)
            return rdt.getProcessKey();
        else
            return null;

    }

    static HashMap<String, RDataType> rDataTypeKeys = new HashMap<String, RDataType>();

    @Override
    public String toString() {
        return "RAnnotation [" + this.type + "][" + Arrays.toString(this.attributeHash.entrySet().toArray()) + "]";
    }
}
