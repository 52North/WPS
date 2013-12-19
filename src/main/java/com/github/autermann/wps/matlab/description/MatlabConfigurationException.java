package com.github.autermann.wps.matlab.description;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class MatlabConfigurationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public MatlabConfigurationException(String message, Object... param) {
        super(String.format(message, param));
    }

    public MatlabConfigurationException(Throwable cause, String message,
                                        Object... param) {
        super(String.format(message, param), cause);
    }

    public MatlabConfigurationException(Throwable cause) {
        super(cause);
    }

}
