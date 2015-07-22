package com.github.autermann.wps.matlab.description;

import com.github.autermann.wps.matlab.transform.LiteralType;

/**
 *
 * @author Christian Autermann
 */
public interface MatlabLiteralTyped
        extends MatlabTyped {

    LiteralType getLiteralType();

}
