package org.n52.wps.matlab.description;

import org.n52.wps.matlab.transform.LiteralType;

/**
 *
 * @author Christian Autermann
 */
public interface MatlabLiteralTyped
        extends MatlabTyped {

    LiteralType getLiteralType();

}
