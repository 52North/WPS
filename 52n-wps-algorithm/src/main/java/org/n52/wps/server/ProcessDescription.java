/*
 * Copyright (C) 2007-2017 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.wps.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import net.opengis.ows.x11.RangeType;
import net.opengis.ows.x20.BoundingBoxType;
import net.opengis.ows.x20.ValueType;
import net.opengis.wps.x100.CRSsType;
import net.opengis.wps.x100.ComplexDataCombinationType;
import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.LiteralInputType;
import net.opengis.wps.x100.LiteralOutputType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.SupportedCRSsType;
import net.opengis.wps.x100.SupportedCRSsType.Default;
import net.opengis.wps.x100.SupportedComplexDataInputType;
import net.opengis.wps.x100.SupportedComplexDataType;
import net.opengis.wps.x20.BoundingBoxDataDocument;
import net.opengis.wps.x20.BoundingBoxDataDocument.BoundingBoxData;
import net.opengis.wps.x20.ComplexDataDocument;
import net.opengis.wps.x20.ComplexDataType;
import net.opengis.wps.x20.DataDescriptionType;
import net.opengis.wps.x20.FormatDocument;
import net.opengis.wps.x20.FormatDocument.Format;
import net.opengis.wps.x20.LiteralDataDocument;
import net.opengis.wps.x20.LiteralDataDomainType;
import net.opengis.wps.x20.LiteralDataType;
import net.opengis.wps.x20.ProcessOfferingDocument.ProcessOffering;
import net.opengis.wps.x20.SupportedCRSDocument.SupportedCRS;

import org.apache.xmlbeans.XmlObject;

import org.n52.wps.commons.WPSConfig;
import org.n52.wps.commons.XMLUtil;

import com.google.common.base.Strings;

/**
 * TODO: javadoc
 *
 * @author Benjamin Pross
 *
 */
public class ProcessDescription {

    private Map<String, XmlObject> versionDescriptionTypeMap = new HashMap<String, XmlObject>();

    public XmlObject getProcessDescriptionType(String version) {

        XmlObject result = versionDescriptionTypeMap.get(version);

        if(result == null && version.equals(WPSConfig.VERSION_200)){
            ProcessDescriptionType processDescriptionV100 = (ProcessDescriptionType) versionDescriptionTypeMap.get(WPSConfig.VERSION_100);
            if(processDescriptionV100 != null){
                result = createProcessDescriptionV200fromV100(processDescriptionV100);
            }
        }
        return result;
    }

    public void addProcessDescriptionForVersion(XmlObject processDescription, String version){
        versionDescriptionTypeMap.put(version, processDescription);
    }

    public static ProcessOffering createProcessDescriptionV200fromV100(ProcessDescriptionType processDescriptionV100){

        ProcessOffering processOffering = ProcessOffering.Factory.newInstance();

        net.opengis.wps.x20.ProcessDescriptionType processDescription = processOffering.addNewProcess();

            processOffering.setProcessVersion(processDescriptionV100.getProcessVersion());

            //TODO check options
            List<String> jobControlOptions = new ArrayList<>();

            jobControlOptions.add(WPSConfig.JOB_CONTROL_OPTION_SYNC_EXECUTE);

            if(processDescriptionV100.getStatusSupported()){
                jobControlOptions.add(WPSConfig.JOB_CONTROL_OPTION_ASYNC_EXECUTE);
            }

            processOffering.setJobControlOptions(jobControlOptions);

            List<String> outputTransmissionModes = new ArrayList<>();

            outputTransmissionModes.add(WPSConfig.OUTPUT_TRANSMISSION_VALUE);
            outputTransmissionModes.add(WPSConfig.OUTPUT_TRANSMISSION_REFERENCE);

            processOffering.setOutputTransmission(outputTransmissionModes);
            // 1. Identifier
            processDescription.addNewIdentifier().setStringValue(processDescriptionV100.getIdentifier().getStringValue());
            processDescription.addNewTitle().setStringValue(processDescriptionV100.getTitle() != null ?
                    processDescriptionV100.getTitle().getStringValue() :
                        processDescriptionV100.getIdentifier().getStringValue());
            if (processDescriptionV100.getAbstract() != null) {
                processDescription.addNewAbstract().setStringValue(processDescriptionV100.getAbstract().getStringValue());
            }

            InputDescriptionType[] inputDescriptionTypes = new InputDescriptionType[]{};

            if(processDescriptionV100.getDataInputs() != null){
                inputDescriptionTypes = processDescriptionV100.getDataInputs().getInputArray();
            }

            for (InputDescriptionType inputDescriptionType : inputDescriptionTypes) {

                net.opengis.wps.x20.InputDescriptionType dataInput = processDescription.addNewInput();
                dataInput.setMinOccurs(inputDescriptionType.getMinOccurs());
                dataInput.setMaxOccurs(inputDescriptionType.getMaxOccurs());

                dataInput.addNewIdentifier().setStringValue(inputDescriptionType.getIdentifier().getStringValue());
                dataInput.addNewTitle().setStringValue( inputDescriptionType.getTitle() != null ?
                        inputDescriptionType.getTitle().getStringValue() :
                            inputDescriptionType.getIdentifier().getStringValue());
                if (inputDescriptionType.getAbstract() != null) {
                    dataInput.addNewAbstract().setStringValue(inputDescriptionType.getAbstract().getStringValue());
                }

                if (inputDescriptionType.getLiteralData() != null) {
                    LiteralInputType literalInputType = inputDescriptionType.getLiteralData();

                    LiteralDataType literalData = LiteralDataType.Factory.newInstance();

                    addFormatAndSubstitute(literalData, "text/plain", null, null, true);

                    addFormatAndSubstitute(literalData, "text/xml", null, null);

                    LiteralDataDomainType literalDataDomainType = literalData.addNewLiteralDataDomain();

                    String dataType = literalInputType.getDataType().getStringValue();

                    if(dataType == null || dataType.equals("")){
                        dataType = literalInputType.getDataType().getReference();
                    }

                    literalDataDomainType.addNewDataType().setReference(dataType);

                    if (literalInputType.getDefaultValue() != null) {

                        ValueType defaultValue = ValueType.Factory.newInstance();

                        defaultValue.setStringValue(literalInputType.getDefaultValue());

                        literalDataDomainType.setDefaultValue(defaultValue);
                    }
                    if (literalInputType.getAllowedValues() != null) {
                        net.opengis.ows.x20.AllowedValuesDocument.AllowedValues allowed = literalDataDomainType.addNewAllowedValues();
                        for (net.opengis.ows.x11.ValueType allowedValue : literalInputType.getAllowedValues().getValueArray()) {
                            allowed.addNewValue().setStringValue(allowedValue.getStringValue());
                        }
                        for (RangeType range : literalInputType.getAllowedValues().getRangeArray()) {
                            net.opengis.ows.x20.RangeType newRange = allowed.addNewRange();
                            String minimumValue = range.getMinimumValue() != null ? range.getMinimumValue().getStringValue() : "";

                            if(minimumValue != null && !minimumValue.equals("")){
                                newRange.addNewMinimumValue().setStringValue(minimumValue);
                            }
                            String maximumValue = range.getMaximumValue() != null ? range.getMaximumValue().getStringValue() : "";

                            if(maximumValue != null && !maximumValue.equals("")){
                                newRange.addNewMaximumValue().setStringValue(maximumValue);
                            }
                            String spacing = range.getSpacing() != null ? range.getSpacing().getStringValue() : "";

                            if(spacing != null && !spacing.equals("")){
                                newRange.addNewSpacing().setStringValue(spacing);
                            }
                        }
                    } else {
                        literalDataDomainType.addNewAnyValue();
                    }

                    dataInput.setDataDescription(literalData);

                    QName literalDataDocumentName = LiteralDataDocument.type.getDocumentElementName();

                    literalDataDocumentName = new QName(literalDataDocumentName.getNamespaceURI(), literalDataDocumentName.getLocalPart(), "wps");

                    XMLUtil.qualifySubstitutionGroup(dataInput.getDataDescription(), literalDataDocumentName, null);

                } else if (inputDescriptionType.getComplexData() != null) {

                    ComplexDataType complexDataType = ComplexDataType.Factory.newInstance();

                    transformComplexDataFromV100ToV200(complexDataType, inputDescriptionType.getComplexData());

                    dataInput.setDataDescription(complexDataType);

                    QName complexDataDocumentName = ComplexDataDocument.type.getDocumentElementName();

                    complexDataDocumentName = new QName(complexDataDocumentName.getNamespaceURI(), complexDataDocumentName.getLocalPart(), "wps");

                    XMLUtil.qualifySubstitutionGroup(dataInput.getDataDescription(), complexDataDocumentName, null);
                }else if(inputDescriptionType.getBoundingBoxData() != null){

                    BoundingBoxData boundingBoxData = BoundingBoxData.Factory.newInstance();

                    transformBBoxDataFromV100ToV200(boundingBoxData, inputDescriptionType.getBoundingBoxData());

                    dataInput.setDataDescription(boundingBoxData);

                    XMLUtil.qualifySubstitutionGroup(dataInput.getDataDescription(), BoundingBoxDataDocument.type.getDocumentElementName(), null);
                }

        }

            // 3. Outputs
            OutputDescriptionType[] outputDescriptions = processDescriptionV100.getProcessOutputs().getOutputArray();

            for (OutputDescriptionType outputDescription : outputDescriptions) {

                net.opengis.wps.x20.OutputDescriptionType dataOutput = processDescription.addNewOutput();
                dataOutput.addNewIdentifier().setStringValue(outputDescription.getIdentifier().getStringValue());
                dataOutput.addNewTitle().setStringValue( outputDescription.getTitle() != null ?
                        outputDescription.getTitle().getStringValue() :
                            outputDescription.getIdentifier().getStringValue());
                if (outputDescription.getAbstract() != null) {
                    dataOutput.addNewAbstract().setStringValue(outputDescription.getAbstract().getStringValue());
                }

                if (outputDescription.getLiteralOutput() != null) {
                    LiteralOutputType literalOutputType = outputDescription.getLiteralOutput();

                    LiteralDataType literalData = LiteralDataType.Factory.newInstance();

                    net.opengis.wps.x20.FormatDocument.Format defaultFormat =  literalData.addNewFormat();

                    defaultFormat.setDefault(true);

                    defaultFormat.setMimeType("text/plain");

                    net.opengis.wps.x20.FormatDocument.Format textXMLFormat =  literalData.addNewFormat();

                    textXMLFormat.setMimeType("text/xml");

                    LiteralDataDomainType literalDataDomainType = literalData.addNewLiteralDataDomain();

                    literalDataDomainType.addNewDataType().setReference(literalOutputType.getDataType().getStringValue());

                    literalDataDomainType.addNewAnyValue();

                    dataOutput.setDataDescription(literalData);

                    QName literalDataDocumentName = LiteralDataDocument.type.getDocumentElementName();

                    literalDataDocumentName = new QName(literalDataDocumentName.getNamespaceURI(), literalDataDocumentName.getLocalPart(), "wps");

                    XMLUtil.qualifySubstitutionGroup(dataOutput.getDataDescription(), literalDataDocumentName, null);

                } else if (outputDescription.getComplexOutput() != null) {

                    ComplexDataType complexDataType = ComplexDataType.Factory.newInstance();

                    transformComplexDataFromV100ToV200(complexDataType, outputDescription.getComplexOutput());

                    dataOutput.setDataDescription(complexDataType);

                    QName complexDataDocumentName = ComplexDataDocument.type.getDocumentElementName();

                    complexDataDocumentName = new QName(complexDataDocumentName.getNamespaceURI(), complexDataDocumentName.getLocalPart(), "wps");

                    XMLUtil.qualifySubstitutionGroup(dataOutput.getDataDescription(), complexDataDocumentName, null);

                } else if(outputDescription.getBoundingBoxOutput() != null){

                    BoundingBoxData boundingBoxData = BoundingBoxData.Factory.newInstance();

                    transformBBoxDataFromV100ToV200(boundingBoxData, outputDescription.getBoundingBoxOutput());

                    dataOutput.setDataDescription(boundingBoxData);

                    XMLUtil.qualifySubstitutionGroup(dataOutput.getDataDescription(), BoundingBoxDataDocument.type.getDocumentElementName(), null);
                }
            }

        return processOffering;
    }

    private static void transformBBoxDataFromV100ToV200(
                BoundingBoxData boundingBoxData,
                SupportedCRSsType supportedCRSsType){

        Format defaultFormat = boundingBoxData.addNewFormat();

        defaultFormat.setMimeType("text/xml");

        defaultFormat.setDefault(true);

        Default defaultCRS = supportedCRSsType.getDefault();

        if(defaultCRS != null){

            SupportedCRS wps20DefaultCRS = boundingBoxData.addNewSupportedCRS();

            wps20DefaultCRS.setDefault(true);

            wps20DefaultCRS.setStringValue(defaultCRS.getCRS());
        }

        CRSsType supportedCRSType = supportedCRSsType.getSupported();

        for (String supportedCRSString : supportedCRSType.getCRSArray()) {

                SupportedCRS wps20DefaultCRS = boundingBoxData.addNewSupportedCRS();

                wps20DefaultCRS.setStringValue(supportedCRSString);
            }

    }

    private static void transformComplexDataFromV100ToV200(
            ComplexDataType complexDataType,
            XmlObject complexData) {

        ComplexDataCombinationType defaultFormat = ComplexDataCombinationType.Factory.newInstance();

        if(complexData instanceof SupportedComplexDataType){
            defaultFormat = ((SupportedComplexDataType)complexData).getDefault();
        }else if(complexData instanceof SupportedComplexDataInputType){
            defaultFormat = ((SupportedComplexDataInputType)complexData).getDefault();
        }

        String defaultMimeType = defaultFormat.getFormat().getMimeType();
        String defaultEncoding = defaultFormat.getFormat().getEncoding();
        String defaultSchema = defaultFormat.getFormat().getSchema();

        addFormatAndSubstitute(complexDataType, defaultMimeType, defaultEncoding, defaultSchema, true);

        ComplexDataDescriptionType[] supportedFormats = new ComplexDataDescriptionType[0];

        if(complexData instanceof SupportedComplexDataType){
            supportedFormats = ((SupportedComplexDataType)complexData).getSupported().getFormatArray();
        }else if(complexData instanceof SupportedComplexDataInputType){
            supportedFormats = ((SupportedComplexDataInputType)complexData).getSupported().getFormatArray();
        }

        for (ComplexDataDescriptionType complexDataDescriptionType : supportedFormats) {

            String mimeType = complexDataDescriptionType.getMimeType();
            String encoding = complexDataDescriptionType.getEncoding();
            String schema = complexDataDescriptionType.getSchema();

            //prevent duplicate format
            if(!((encoding == null ? encoding == defaultEncoding : encoding.equals(defaultEncoding)) &&
                    (mimeType == null ? mimeType == defaultMimeType : mimeType.equals(defaultMimeType))&&
                    (schema == null ? schema == defaultSchema : schema.equals(defaultSchema)))){
                addFormatAndSubstitute(complexDataType, mimeType, encoding, schema);
            }
        }
    }

           private static void addFormatAndSubstitute(DataDescriptionType dataDescriptionType,
                    String mimeType,
                    String encoding,
                    String schema){
               addFormatAndSubstitute(dataDescriptionType, mimeType, encoding, schema, false);
            }

    private static void addFormatAndSubstitute(DataDescriptionType dataDescriptionType,
                String mimeType,
                String encoding,
                String schema, boolean defaulFormat){

            Format supportedFormat = dataDescriptionType.addNewFormat();

            if(defaulFormat){
                supportedFormat.setDefault(defaulFormat);
            }

            describeDataDescriptionFormat200(supportedFormat, mimeType, encoding, schema);

//            QName formatDocumentName = FormatDocument.type.getDocumentElementName();
//
//            formatDocumentName = new QName(formatDocumentName.getNamespaceURI(), formatDocumentName.getLocalPart(), "wps");
//
//            XMLUtil.qualifySubstitutionGroup(dataDescriptionType, formatDocumentName, null);
    }

    private static void describeDataDescriptionFormat200(Format supportedFormatType,
            String format,
            String encoding,
            String schema) {
        if ( !Strings.isNullOrEmpty(format)) {
            supportedFormatType.setMimeType(format);
        }
        if ( !Strings.isNullOrEmpty(encoding)) {
            supportedFormatType.setEncoding(encoding);
        }
        if ( !Strings.isNullOrEmpty(schema)) {
            supportedFormatType.setSchema(schema);
        }
    }


}
