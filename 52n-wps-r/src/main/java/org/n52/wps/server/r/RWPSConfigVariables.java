/**
 * ﻿Copyright (C) 2010
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */

package org.n52.wps.server.r;

public enum RWPSConfigVariables {

    SCRIPT_DIR, RESOURCE_DIR, ALGORITHM, ENABLE_BATCH_START, RSERVE_HOST, RSERVE_PORT, RSERVE_USER, RSERVE_PASSWORD;

    public String toString() {
        switch (this) {
        case SCRIPT_DIR:
            return "Script_Dir";
        case RESOURCE_DIR:
            return "Resource_Dir";
        case ALGORITHM:
            return "Algorithm";
        case ENABLE_BATCH_START:
            return "Enable_Batch_Start";
        case RSERVE_HOST:
            return "Rserve_Host";
        case RSERVE_PORT:
            return "Rserve_Port";
        case RSERVE_USER:
            return "Rserve_User";
        case RSERVE_PASSWORD:
            return "Rserve_Password";
        default:
            return "NO STRING REPRESENTATION DEFINED FOR ENUM CONSTANT!";
        }

    };

}
