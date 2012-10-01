package org.n52.wps.server.r.syntax;

import org.n52.wps.io.data.GenericFileDataConstants;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.r.RWorkdirUrlBinding;

/**
 * Data types which are supported by scripts Note that every IData class must be parsed from an to are to
 * be handled successful --> GenericRProcess TODO: restructure dependent classes & methods for new
 * attributes
 */
public enum RDataType implements RTypeDefinition {
    // literal data:
    STRING("string", "xs:string", LiteralStringBinding.class), CHARACTER("character", "xs:string",
            LiteralStringBinding.class), INTEGER("integer", "xs:integer", LiteralIntBinding.class), DOUBLE(
            "double", "xs:double", LiteralDoubleBinding.class), BOOLEAN("boolean", "xs:boolean",
            LiteralBooleanBinding.class), URL("text/url", "xs:string", RWorkdirUrlBinding.class),

    // geodata:
    DBASE("dbf", GenericFileDataConstants.MIME_TYPE_DBASE, GenericFileDataBinding.class, true, null, "base64"), 
    DGN(
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
    GIF("gif", GenericFileDataConstants.MIME_TYPE_IMAGE_GIF, GenericFileDataBinding.class, true, null, null), 
    
    JPEG(
            "jpeg", GenericFileDataConstants.MIME_TYPE_IMAGE_JPEG, GenericFileDataBinding.class, true, null,
            null), 
            
       JPEG2("jpg", GenericFileDataConstants.MIME_TYPE_IMAGE_JPEG, GenericFileDataBinding.class,
            true, null, null), 
            
       PNG("png", GenericFileDataConstants.MIME_TYPE_IMAGE_PNG,
            GenericFileDataBinding.class, true, null, null), 
            
            
        TIFF("tiff",
            GenericFileDataConstants.MIME_TYPE_TIFF, GenericFileDataBinding.class, true, null, null),

    // file data and xml:
    TEXT_PLAIN("text", GenericFileDataConstants.MIME_TYPE_PLAIN_TEXT, GenericFileDataBinding.class, true), TEXT_XML(
            "xml", GenericFileDataConstants.MIME_TYPE_TEXT_XML, GenericFileDataBinding.class, true),

    FILE("file", "application/unknown", GenericFileDataBinding.class),
    PDF("pdf","application/pdf", GenericFileDataBinding.class, true, null, "base64"),
    STY("sty","application/sty", GenericFileDataBinding.class, true, null, "base64"),
    RNW("rnw","application/rnw", GenericFileDataBinding.class, true, null, "base64");
    // TEXT_XML2("text_xml", GenericFileDataConstants.MIME_TYPE_TEXT_XML,
    // GenericFileDataBinding.class,true);

    private String key;
    private String processKey;
    private Class< ? extends IData> iDataClass;
    private boolean isComplex;
    String schema;
    String encoding = "UTF-8";

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
        if ( !RAnnotation.rDataTypeKeys.containsKey(key))
            RAnnotation.rDataTypeKeys.put(key, this);
        else
            RAnnotation.LOGGER.warn("Doubled definition of data type-key for notation: " + key + "\n"
                    + "only the first definition will be used for this key.");

        // put process key, i.e. mimetype or xml-notation for literal type, as alternative key into
        // hashmap:
        if ( !RAnnotation.rDataTypeKeys.containsKey(this.getProcessKey()))
            RAnnotation.rDataTypeKeys.put(this.getProcessKey(), this);
        else
            RAnnotation.LOGGER.warn("Doubled definition of data type-key for notation: "
                    + this.getProcessKey()
                    + "\n"
                    + "only the first definition will be used for this key.+"
                    + "(That might be the usual case if more than one annotation type key refer to one WPS-mimetype with different data handlers)");
    }

    /* (non-Javadoc)
	 * @see org.n52.wps.server.r.syntax.RTypeDefinition#getKey()
	 */
    @Override
	public String getKey() {
        return key;
    }

    /* (non-Javadoc)
	 * @see org.n52.wps.server.r.syntax.RTypeDefinition#getProcessKey()
	 */
    @Override
	public String getProcessKey() {
        return processKey;
    }

    /* (non-Javadoc)
	 * @see org.n52.wps.server.r.syntax.RTypeDefinition#isComplex()
	 */
    @Override
	public boolean isComplex() {
        return isComplex;
    }

    /* (non-Javadoc)
	 * @see org.n52.wps.server.r.syntax.RTypeDefinition#getEncoding()
	 */
    @Override
	public String getEncoding() {
        return encoding;
    }

    /* (non-Javadoc)
	 * @see org.n52.wps.server.r.syntax.RTypeDefinition#getSchema()
	 */
    @Override
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
        RDataType out = RAnnotation.rDataTypeKeys.get(key);
        if (out == null)
            throw new RAnnotationException("Invalid datatype key for R script annotations: " + key);
        else
            return out;
    }

    /* (non-Javadoc)
	 * @see org.n52.wps.server.r.syntax.RTypeDefinition#getIDataClass()
	 */
    @Override
	public Class< ? extends IData> getIDataClass() {
        return iDataClass;
    }
}