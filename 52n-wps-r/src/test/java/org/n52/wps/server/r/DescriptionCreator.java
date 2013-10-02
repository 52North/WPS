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
import java.net.URL;
import java.util.List;

import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionsDocument;

import org.apache.xmlbeans.XmlException;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.r.metadata.RAnnotationParser;
import org.n52.wps.server.r.metadata.RProcessDescriptionCreator;
import org.n52.wps.server.r.syntax.RAnnotation;
import org.n52.wps.server.r.syntax.RAnnotationException;
import org.n52.wps.server.r.syntax.RAnnotationType;
import org.n52.wps.server.r.syntax.RAttribute;
import org.w3c.dom.Document;

public class DescriptionCreator {

    private List<RAnnotation> annotations;

    @Before
    public void loadAnnotations() throws IOException, RAnnotationException
    {
        File scriptFile = Util.loadFile("/uniform.R");

        // GenericRProcess process = new GenericRProcess("R_andom");
        FileInputStream fis = new FileInputStream(scriptFile);
        RAnnotationParser parser = new RAnnotationParser();
        this.annotations = parser.parseAnnotationsfromScript(fis);
        fis.close();
    }

    @Test
    public void uniform() throws ExceptionReport, RAnnotationException, IOException, XmlException
    {
        File descriptionFile = Util.loadFile("/uniform.xml");

        // GenericRProcess process = new GenericRProcess("R_andom");
        FileInputStream fis = new FileInputStream(descriptionFile);
        RProcessDescriptionCreator creator = new RProcessDescriptionCreator();
        ProcessDescriptionType testType = creator.createDescribeProcessType(this.annotations, "R_andom", new URL("http://my.url/myScript.R"), new URL("http://my.url/sessioninfo.jsp"));
        ProcessDescriptionsDocument testDoc = ProcessDescriptionsDocument.Factory.newInstance();
        testDoc.addNewProcessDescriptions().addNewProcessDescription().set(testType);
        // System.out.println(testDoc.xmlText());

        ProcessDescriptionsDocument control = ProcessDescriptionsDocument.Factory.parse(descriptionFile);

        // test process description manually
        String abstractString = null;
        String identifierString = null;
        String titleString = null;
        for (RAnnotation anno : this.annotations) {
            if (anno.getType().equals(RAnnotationType.DESCRIPTION)) {
                abstractString = anno.getStringValue(RAttribute.ABSTRACT);
                identifierString = anno.getStringValue(RAttribute.IDENTIFIER);
                titleString = anno.getStringValue(RAttribute.TITLE);
            }
        }

        Assert.assertEquals(testType.getAbstract().getStringValue(), abstractString);
        testType.getIdentifier().getStringValue().equals(identifierString);
        testType.getTitle().getStringValue().equals(titleString);

        // test full document > some namespace issues! FIXME
        // Document controlDocument = (Document) control.getDomNode();
        // Document testDocument = (Document) testDoc.getDomNode();
        // XMLAssert.assertXMLEqual("Comparing process descriptions for uniform.",
        // controlDocument,
        // testDocument);

        fis.close();
    }

}
