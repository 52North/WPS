package com.github.autermann.wps.matlab.description;

import com.google.common.base.Strings;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class AbstractMatlabDescription {

    private String id;
    private String title;
    private String abstrakt;

    public String getId() {
        return id;
    }

    public boolean hasId() {
        return !Strings.isNullOrEmpty(getId());
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public boolean hasTitle() {
        return !Strings.isNullOrEmpty(getTitle());
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAbstract() {
        return abstrakt;
    }

    public boolean hasAbstract() {
        return !Strings.isNullOrEmpty(getAbstract());
    }

    public void setAbstract(String abstrakt) {
        this.abstrakt = abstrakt;
    }
}
