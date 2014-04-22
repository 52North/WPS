/*
 * Copyright (C) 2013 Christian Autermann
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
