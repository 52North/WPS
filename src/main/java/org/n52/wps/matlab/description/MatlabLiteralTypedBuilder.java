package org.n52.wps.matlab.description;

import org.n52.wps.matlab.transform.LiteralType;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public interface MatlabLiteralTypedBuilder<T extends MatlabLiteralTyped, B extends MatlabLiteralTypedBuilder<T, B>> {

    @SuppressWarnings("unchecked")
    B withType(LiteralType type);

}
