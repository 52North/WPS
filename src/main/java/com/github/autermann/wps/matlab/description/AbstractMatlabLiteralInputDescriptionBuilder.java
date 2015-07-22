package com.github.autermann.wps.matlab.description;

import com.github.autermann.wps.commons.description.impl.AbstractLiteralInputDescriptionBuilder;
import com.github.autermann.wps.matlab.transform.LiteralType;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public abstract class AbstractMatlabLiteralInputDescriptionBuilder<T extends MatlabLiteralInputDescription, B extends AbstractMatlabLiteralInputDescriptionBuilder<T, B>>
        extends AbstractLiteralInputDescriptionBuilder<T, B>
        implements MatlabLiteralTypedBuilder<T, B> {

    private LiteralType type;

    LiteralType getType() {
        return type;
    }

    @SuppressWarnings("unchecked")
    @Override
    public B withType(LiteralType type) {
        this.type = type;
        withDataType(type.getXmlType());
        return (B) this;
    }

}
