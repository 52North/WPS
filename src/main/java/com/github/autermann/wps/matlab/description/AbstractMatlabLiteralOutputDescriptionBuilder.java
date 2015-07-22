package com.github.autermann.wps.matlab.description;

import com.github.autermann.wps.commons.description.impl.AbstractLiteralOutputDescriptionBuilder;
import com.github.autermann.wps.matlab.transform.LiteralType;
import com.google.common.base.Preconditions;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public abstract class AbstractMatlabLiteralOutputDescriptionBuilder<T extends MatlabLiteralOutputDescription, B extends AbstractMatlabLiteralOutputDescriptionBuilder<T, B>>
        extends AbstractLiteralOutputDescriptionBuilder<T, B>
        implements MatlabLiteralTypedBuilder<T, B> {

    private LiteralType type;

    LiteralType getType() {
        return this.type;
    }

    @SuppressWarnings("unchecked")
    @Override
    public B withType(LiteralType type) {
        this.type = Preconditions.checkNotNull(type);
        return (B) this;
    }

}
