package org.n52.wps.server.r.syntax;

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