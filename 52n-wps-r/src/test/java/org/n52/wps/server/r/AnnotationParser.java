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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.n52.wps.server.r.metadata.RAnnotationParser;
import org.n52.wps.server.r.syntax.RAnnotation;
import org.n52.wps.server.r.syntax.RAnnotationException;
import org.n52.wps.server.r.syntax.RAnnotationType;
import org.n52.wps.server.r.syntax.RAttribute;
import org.n52.wps.server.r.syntax.ResourceAnnotation;

public class AnnotationParser {

    private List<RAnnotation> annotations;

    @Before
    public void loadAnnotations() throws IOException, RAnnotationException {
        File scriptFile = Util.loadFile("/uniform.R");

        // GenericRProcess process = new GenericRProcess("R_andom");
        FileInputStream fis = new FileInputStream(scriptFile);
        RAnnotationParser parser = new RAnnotationParser();
        this.annotations = parser.parseAnnotationsfromScript(fis);
        fis.close();
    }

    @Test
    public void description() throws RAnnotationException {
        for (RAnnotation rAnnotation : this.annotations) {
            if (rAnnotation.getType().equals(RAnnotationType.DESCRIPTION)) {
                Assert.assertEquals("42", rAnnotation.getStringValue(RAttribute.VERSION));
                Assert.assertEquals("Random number generator", rAnnotation.getStringValue(RAttribute.TITLE));
                Assert.assertEquals("MC++", rAnnotation.getStringValue(RAttribute.AUTHOR));
                Assert.assertEquals("Generates random numbers with uniform distribution",
                                    rAnnotation.getStringValue(RAttribute.ABSTRACT));
                Assert.assertEquals("R_andom", rAnnotation.getStringValue(RAttribute.IDENTIFIER));
            }
            else if (rAnnotation.getType().equals(RAnnotationType.OUTPUT)) {
                // output, text, Random number list,
                Assert.assertEquals("output", rAnnotation.getStringValue(RAttribute.IDENTIFIER));
                Assert.assertEquals("text", rAnnotation.getStringValue(RAttribute.TYPE));
                Assert.assertEquals("Random number list", rAnnotation.getStringValue(RAttribute.TITLE));
                Assert.assertEquals("Text file with list of n random numbers in one column",
                                    rAnnotation.getStringValue(RAttribute.ABSTRACT));
            }
            else if (rAnnotation.getType().equals(RAnnotationType.INPUT)) {
                String identifier = rAnnotation.getStringValue(RAttribute.IDENTIFIER);
                if ("n".equals(identifier)) {
                    Assert.assertEquals("integer", rAnnotation.getStringValue(RAttribute.TYPE));
                    Assert.assertEquals("amount of random numbers", rAnnotation.getStringValue(RAttribute.TITLE));
                    Assert.assertEquals("100", rAnnotation.getStringValue(RAttribute.DEFAULT_VALUE));
                    Assert.assertEquals("0", rAnnotation.getStringValue(RAttribute.MIN_OCCURS));
                }
            }
        }
    }

    @Test
    public void resource() {
        for (RAnnotation rAnnotation : this.annotations) {
            if (rAnnotation.getType().equals(RAnnotationType.RESOURCE)) {
                ResourceAnnotation resourceAnnotation = (ResourceAnnotation) rAnnotation;
                String value = resourceAnnotation.getResources().get(0).getResourceValue();
                Assert.assertEquals("test.file.txt", value);
            }
        }

    }

}
