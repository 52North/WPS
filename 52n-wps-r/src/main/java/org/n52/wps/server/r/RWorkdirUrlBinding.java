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

import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;

public class RWorkdirUrlBinding extends LiteralStringBinding {

    private static Logger LOGGER = Logger.getLogger(RWorkdirUrlBinding.class);

    /**
     * 
     */
    private static final long serialVersionUID = 6106100279013286421L;
    private URL url;

    public RWorkdirUrlBinding(URL url) {
        super(url.toString());
        this.url = url;
    }

    public RWorkdirUrlBinding(String currentWorkdir, String filename) {
        super(currentWorkdir + "/" + filename);
        try {
            this.url = R_Config.getInstance().getOutputFileURL(currentWorkdir, filename);
        }
        catch (IOException e) {
            LOGGER.error(e);
        }

        LOGGER.info("NEW " + this);
    }

    public Class< ? > getSupportedClass() {
        return URL.class;
    }

    public String getPayload() {
        return this.url.toString();
    }

    @Override
    public String toString() {
        return "RWorkdirUrlBinding [" + this.url.toString() + "]";
    }

}
