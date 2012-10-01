package org.n52.wps.server.r.syntax;

/**
 * attributes used in Annotations
 */
public enum RAttribute {
	INPUT_START("wps.in", null, true), 
	OUTPUT_START("wps.out", null, true), 
	DESCRIPTION_START("wps.des", null, true),
	IDENTIFIER("id", null, true), 
	TYPE("type", null, true), 
	TITLE("title", IDENTIFIER, false), 
	ABSTRACT("abstract", null, false), 
	MIN_OCCURS("minOccurs", 1, true), 
	MAX_OCCURS("maxOccurs", 1, true), 
	DEFAULT_VALUE("value", null, false), 
	METADATA("meta", null, false), 
	MIMETYPE("mimetype", null, false), 
	SCHEMA("schema", null, false), 
	ENCODING("encoding", null, false), 
	AUTHOR("author", null, false),
	//A sequence of values:
	SEQUENCE("seq", null, true);

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
	 * @return true if attribute has to occur in Process description, if so,
	 *         there has to be a standard value or a value in R Annotion given
	 */
	public boolean isMandatory() {
		return mandatory;
	}

	private boolean mandatory;
	
	@Override
	public String toString() {
		return getKey();
	}
}