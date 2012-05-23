
package org.n52.wps.server.r;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.n52.wps.io.data.GenericFileDataConstants;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;

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
    private static Logger LOGGER = Logger.getLogger(RAnnotation.class);

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
        RDataType rType = RDataType.getType(rClass);
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
        RDataType rdt = RDataType.getType(type);
        if (rdt != null)
            return rdt.getProcessKey();
        else
            return null;

    }

    private static HashMap<String, RDataType> rDataTypeKeys = new HashMap<String, RDataType>();

    /**
     * Data types which are supported by scripts Note that every IData class must be parsed from an to are to
     * be handled successful --> GenericRProcess TODO: restructure dependent classes & methods for new
     * attributes
     */
    public enum RDataType {
        // literal data:
        STRING("string", "xs:string", LiteralStringBinding.class), CHARACTER("character", "xs:string",
                LiteralStringBinding.class), INTEGER("integer", "xs:integer", LiteralIntBinding.class), DOUBLE(
                "double", "xs:double", LiteralDoubleBinding.class), BOOLEAN("boolean", "xs:boolean",
                LiteralBooleanBinding.class), URL("text/url", "xs:string", RWorkdirUrlBinding.class),

        // geodata:
        DBASE("dbf", GenericFileDataConstants.MIME_TYPE_DBASE, GenericFileDataBinding.class, true, null, "base64"), DGN(
                "dgn", GenericFileDataConstants.MIME_TYPE_DGN, GenericFileDataBinding.class, true, null, "base64"), GEOTIFF(
                "geotiff", GenericFileDataConstants.MIME_TYPE_GEOTIFF, GenericFileDataBinding.class, true, null,
                "base64"), GEOTIFF2("geotiff_image", GenericFileDataConstants.MIME_TYPE_IMAGE_GEOTIFF,
                GTRasterDataBinding.class, true, null, "base64"), GEOTIFF_X("geotiff_x",
                GenericFileDataConstants.MIME_TYPE_X_GEOTIFF, GenericFileDataBinding.class, true, null, "base64"), IMG(
                "img", GenericFileDataConstants.MIME_TYPE_HDF, GenericFileDataBinding.class, true, null, "base64"), IMG2(
                "img_x", GenericFileDataConstants.MIME_TYPE_X_ERDAS_HFA, GenericFileDataBinding.class, true, null,
                "base64"), NETCDF("netcdf", GenericFileDataConstants.MIME_TYPE_NETCDF, GenericFileDataBinding.class,
                true, null, "base64"), NETCDF_X("netcdf_x", GenericFileDataConstants.MIME_TYPE_X_NETCDF,
                GenericFileDataBinding.class, true, null, "base64"), REMAP("remap",
                GenericFileDataConstants.MIME_TYPE_REMAPFILE, GenericFileDataBinding.class, true, null, "base64"), SHAPE(
                "shp", GenericFileDataConstants.MIME_TYPE_SHP, GTVectorDataBinding.class, true, null, "base64"),
        // SHAPE_ZIP("shp_zip",GenericFileDataConstants.MIME_TYPE_ZIPPED_SHP, GenericFileDataBinding.class,
        // true),
        SHAPE_ZIP2("shp_x", GenericFileDataConstants.MIME_TYPE_ZIPPED_SHP, GTVectorDataBinding.class, true, null,
                "base64"), KML("kml", GenericFileDataConstants.MIME_TYPE_KML, GenericFileDataBinding.class, true, null,
                "UTF-8"),

        // graphical data
        GIF("gif", GenericFileDataConstants.MIME_TYPE_IMAGE_GIF, GenericFileDataBinding.class, true, null, "base64"), JPEG(
                "jpeg", GenericFileDataConstants.MIME_TYPE_IMAGE_JPEG, GenericFileDataBinding.class, true, null,
                "base64"), JPEG2("jpg", GenericFileDataConstants.MIME_TYPE_IMAGE_JPEG, GenericFileDataBinding.class,
                true, null, "base64"), PNG("png", GenericFileDataConstants.MIME_TYPE_IMAGE_PNG,
                GenericFileDataBinding.class, true, null, "base64"), TIFF("tiff",
                GenericFileDataConstants.MIME_TYPE_TIFF, GenericFileDataBinding.class, true, null, "base64"),

        // file data and xml:
        TEXT_PLAIN("text", GenericFileDataConstants.MIME_TYPE_PLAIN_TEXT, GenericFileDataBinding.class, true), TEXT_XML(
                "xml", GenericFileDataConstants.MIME_TYPE_TEXT_XML, GenericFileDataBinding.class, true),

        FILE("file", "application/unknown", GenericFileDataBinding.class);
        // TEXT_XML2("text_xml", GenericFileDataConstants.MIME_TYPE_TEXT_XML,
        // GenericFileDataBinding.class,true);

        private String key;
        private String processKey;
        private Class< ? extends IData> iDataClass;
        private boolean isComplex;
        private String schema;
        private String encoding = "UTF-8";

        private RDataType(String key,
                          String processKey,
                          Class< ? extends IData> iDataClass,
                          boolean isComplex,
                          String schema,
                          String encoding) {
            this.key = key;
            this.processKey = processKey;
            this.iDataClass = iDataClass;
            this.isComplex = isComplex;
            this.schema = schema;
            this.encoding = encoding;
            setKey(key);
            setKey(processKey);
        }

        private RDataType(String key, String processKey, Class< ? extends IData> iDataClass, boolean isComplex) {
            this.key = key;
            this.processKey = processKey;
            this.iDataClass = iDataClass;
            this.isComplex = isComplex;
            setKey(key);
            setKey(processKey);
        }

        private RDataType(String key, String processKey, Class< ? extends IData> iDataClass) {
            this.key = key;
            this.processKey = processKey;
            this.iDataClass = iDataClass;
            this.isComplex = false;
            setKey(key);
            setKey(processKey);
        }

        private void setKey(String key) {
            if ( !rDataTypeKeys.containsKey(key))
                rDataTypeKeys.put(key, this);
            else
                LOGGER.warn("Doubled definition of data type-key for notation: " + key + "\n"
                        + "only the first definition will be used for this key.");

            // put process key, i.e. mimetype or xml-notation for literal type, as alternative key into
            // hashmap:
            if ( !rDataTypeKeys.containsKey(this.getProcessKey()))
                rDataTypeKeys.put(this.getProcessKey(), this);
            else
                LOGGER.warn("Doubled definition of data type-key for notation: "
                        + this.getProcessKey()
                        + "\n"
                        + "only the first definition will be used for this key.+"
                        + "(That might be the usual case if more than one annotation type key refer to one WPS-mimetype with different data handlers)");
        }

        public String getKey() {
            return key;
        }

        public String getProcessKey() {
            return processKey;
        }

        public boolean isComplex() {
            return isComplex;
        }

        /**
         * 
         * @return encoding, null if not available
         */
        public String getEncoding() {
            return encoding;
        }

        /**
         * 
         * @return encoding, UTF-8 by default
         */
        public String getSchema() {
            return schema;
        }

        /**
         * This method is important for parsers to request the meaning of a specific key
         * 
         * @param key
         *        process keys and self defined short keys are recognized as dataType keys
         * @return
         * @throws RAnnotationException
         */
        public static RDataType getType(String key) throws RAnnotationException {
            RDataType out = rDataTypeKeys.get(key);
            if (out == null)
                throw new RAnnotationException("Invalid datatype key for R script annotations: " + key);
            else
                return out;
        }

        public Class< ? extends IData> getIDataClass() {
            return iDataClass;
        }
    }

    /**
     * Describes each annotation type considering attributes, their order and behavior
     * 
     */
    public enum RAnnotationType {
        INPUT(Arrays.asList(RAttribute.INPUT_START,
                            RAttribute.IDENTIFIER,
                            RAttribute.TYPE,
                            RAttribute.TITLE,
                            RAttribute.ABSTRACT,
                            // RAttribute.METADATA,
                            RAttribute.DEFAULT_VALUE,
                            RAttribute.MIN_OCCURS,
                            RAttribute.MAX_OCCURS)), OUTPUT(Arrays.asList(RAttribute.OUTPUT_START,
                                                                          RAttribute.IDENTIFIER,
                                                                          RAttribute.TYPE,
                                                                          RAttribute.TITLE,
                                                                          RAttribute.ABSTRACT
        // RAttribute.METADATA
        )), DESCRIPTION(Arrays.asList(RAttribute.DESCRIPTION_START,
        // TODO: Meaning of identifier???? -- doesn't have a meaning yet..
                                      RAttribute.IDENTIFIER,
                                      RAttribute.TITLE,
                                      RAttribute.ABSTRACT,
                                      RAttribute.AUTHOR));

        private HashMap<String, RAttribute> attributeLut = new HashMap<String, RAttribute>();
        private HashSet<RAttribute> mandatory = new HashSet<RAttribute>();
        private RAttribute startKey;
        private List<RAttribute> attributeSequence;

        private RAnnotationType(List<RAttribute> attributeSequence) {
            this.startKey = attributeSequence.get(0);
            this.attributeSequence = attributeSequence;

            for (RAttribute attribute : attributeSequence) {
                attributeLut.put(attribute.getKey(), attribute);
                if (attribute.isMandatory()) {
                    mandatory.add(attribute);
                }
            }
        }

        public RAttribute getStartKey() {
            return startKey;
        }

        public RAttribute getAttribute(String key) throws RAnnotationException {
            key = key.toLowerCase();
            if (attributeLut.containsKey(key))
                return attributeLut.get(key);
            else
                throw new RAnnotationException("Annotation for " + this + " (" + this.startKey
                        + " ...) contains no key named: " + key + ".");
        }

        public Iterable<RAttribute> getAttributeSequence() {
            return attributeSequence;

        }

        /**
         * Checks if Annotation content is valid for process description and adds attributes / standard values
         * if necessary
         * 
         * @param key
         *        / value pairs given in the annotation from RSkript
         * @return key / value pairs ready for process description
         * @throws IOException
         */
        public void validDescription(HashMap<RAttribute, String> keyValues) throws RAnnotationException {
            // check minOccurs Attribute and default value:
            try {
                if (keyValues.containsKey(RAttribute.MIN_OCCURS)) {
                    Integer min = Integer.parseInt(keyValues.get(RAttribute.MIN_OCCURS));
                    if (keyValues.containsKey(RAttribute.DEFAULT_VALUE) && !min.equals(0))
                        throw new RAnnotationException("");
                }
            }
            catch (NumberFormatException e) {
                throw new RAnnotationException("Syntax Error in Annotation " + this + " (" + this.startKey + " ...), "
                        + "unable to parse Integer value from attribute " + RAttribute.MIN_OCCURS.getKey()
                        + e.getMessage());
            }

            if (keyValues.containsKey(RAttribute.DEFAULT_VALUE) && !keyValues.containsKey(RAttribute.MIN_OCCURS)) {
                keyValues.put(RAttribute.MIN_OCCURS, "0");
            }

            // check maxOccurs Attribute:
            try {
                if (keyValues.containsKey(RAttribute.MAX_OCCURS)) {
                    Integer.parseInt(keyValues.get(RAttribute.MAX_OCCURS));
                }
            }
            catch (NumberFormatException e) {
                throw new RAnnotationException("Syntax Error in Annotation " + this + " (" + this.startKey + " ...), "
                        + "unable to parse Integer value from attribute " + RAttribute.MAX_OCCURS.getKey());
            }
        }

    }

    /**
     * Separators used in Annotations
     * 
     */
    public enum RSeperator {
        STARTKEY_SEPARATOR(":"), ATTRIBUTE_SEPARATOR(","), ATTRIBUTE_VALUE_SEPARATOR("="), ANNOTATION_END(";");

        private String key;

        private RSeperator(String key) {
            this.key = key.toLowerCase();
        }

        public String getKey() {
            return key;
        }

    }

    /**
     * attributes used in Annotations
     */
    public enum RAttribute {
        // "group" maybe added to identify one in/output in different formats:
        // GROUP("group", String.class, null, false),
        // IO_FUNCTION_CALL("func", String.class, null, false),
        INPUT_START("wps.in", null, true), OUTPUT_START("wps.out", null, true), DESCRIPTION_START("wps.des", null, true), IDENTIFIER(
                "id", null, true), TYPE("type", null, true), TITLE("title", IDENTIFIER, false), ABSTRACT("abstract",
                null, false), MIN_OCCURS("minOccurs", 1, true), MAX_OCCURS("maxOccurs", 1, true), DEFAULT_VALUE(
                "value", null, false), METADATA("meta", null, false), MIMETYPE("mimetype", null, false), SCHEMA(
                "schema", null, false), ENCODING("encoding", null, false), AUTHOR("author", null, false);

        private String key;
        private Object defValue;

        private RAttribute(String key, Object defValue, boolean mandatory) {
            this.key = key.toLowerCase();
            this.defValue = defValue;
            this.mandatory = mandatory;
        }

        public String getKey() {
            return key;
        }

        public Object getDefValue() {
            return defValue;
        }

        /**
         * @return true if attribute has to occur in Process description, if so, there has to be a standard
         *         value or a value in R Annotion given
         */
        public boolean isMandatory() {
            return mandatory;
        }

        private boolean mandatory;
    }

    @Override
    public String toString() {
        return "RAnnotation [" + this.type + "][" + Arrays.toString(this.attributeHash.entrySet().toArray()) + "]";
    }
}
