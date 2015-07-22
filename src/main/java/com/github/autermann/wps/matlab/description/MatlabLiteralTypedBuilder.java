package com.github.autermann.wps.matlab.description;

import com.github.autermann.wps.matlab.transform.LiteralType;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public interface MatlabLiteralTypedBuilder<T extends MatlabLiteralTyped, B extends MatlabLiteralTypedBuilder<T, B>> {

    @SuppressWarnings("unchecked")
    B withType(LiteralType type);

}
