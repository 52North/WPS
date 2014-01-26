package com.github.autermann.wps.matlab.description;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;

import net.opengis.ows.x11.AllowedValuesDocument.AllowedValues;
import net.opengis.wps.x100.ComplexDataCombinationType;
import net.opengis.wps.x100.ComplexDataCombinationsType;
import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.LiteralInputType;
import net.opengis.wps.x100.LiteralOutputType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType.DataInputs;
import net.opengis.wps.x100.ProcessDescriptionType.ProcessOutputs;
import net.opengis.wps.x100.ProcessDescriptionsDocument;
import net.opengis.wps.x100.ProcessDescriptionsDocument.ProcessDescriptions;
import net.opengis.wps.x100.SupportedComplexDataInputType;
import net.opengis.wps.x100.SupportedComplexDataType;
import net.opengis.wps.x100.SupportedUOMsType;

import com.github.autermann.matlab.client.MatlabClientConfiguration;
import com.github.autermann.matlab.server.MatlabInstanceConfiguration;
import com.github.autermann.matlab.value.MatlabType;
import com.github.autermann.wps.matlab.YamlConstants;
import com.github.autermann.wps.matlab.transform.LiteralType;
import com.github.autermann.yaml.YamlNode;
import com.google.common.collect.Lists;

public class MatlabDescriptionGenerator {

    private MatlabComplexInputDescription createComplexInput(YamlNode definition) {
        int minOccurs = definition.path(YamlConstants.MIN_OCCURS).asIntValue(1);
        int maxOccurs = definition.path(YamlConstants.MAX_OCCURS).asIntValue(1);
        String id = definition.path(YamlConstants.IDENTIFIER).textValue();
        if (id == null || id.isEmpty()) {
            throw new MatlabConfigurationException("Missing input identifier");
        }
        String abstrakt = definition.path(YamlConstants.ABSTRACT).asTextValue();
        String title = definition.path(YamlConstants.TITLE).asTextValue();
        if (!definition.path(YamlConstants.TYPE).isMap()) {
            throw new MatlabConfigurationException("Missing type for output %s", id);
        }
        String mimeType = definition.path(YamlConstants.TYPE)
                .path(YamlConstants.MIME_TYPE).asTextValue();
        if (mimeType == null || mimeType.isEmpty()) {
            throw new MatlabConfigurationException("Missing mimeType for input %s", id);
        }
        String schema = definition.path(YamlConstants.TYPE)
                .path(YamlConstants.SCHEMA).asTextValue();

        if (maxOccurs < 1 || minOccurs < 0 || maxOccurs < minOccurs) {
            throw new IllegalArgumentException(String
                    .format("Invalid min/max occurs: [%d,%d]", minOccurs, maxOccurs));
        }

        MatlabComplexInputDescription desc = new MatlabComplexInputDescription();
        desc.setId(id);
        desc.setTitle(title);
        desc.setAbstract(abstrakt);
        desc.setMinOccurs(minOccurs);
        desc.setMaxOccurs(maxOccurs);
        desc.setMatlabType(MatlabType.FILE);
        desc.setMimeType(mimeType);
        desc.setSchema(schema);
        return desc;
    }

    private MatlabComplexOutputDescription createComplexOutput(
            YamlNode definition) {
        String id = definition.path(YamlConstants.IDENTIFIER).textValue();
        if (id == null || id.isEmpty()) {
            throw new MatlabConfigurationException("Missing output identifier");
        }
        if (!definition.path(YamlConstants.TYPE).isMap()) {
            throw new MatlabConfigurationException("Missing type for output %s", id);
        }
        String mimeType = definition.path(YamlConstants.TYPE)
                .path(YamlConstants.MIME_TYPE).asTextValue();
        if (mimeType == null || mimeType.isEmpty()) {
            throw new MatlabConfigurationException("Missing mimeType for output %s", id);
        }
        String schema = definition.path(YamlConstants.TYPE)
                .path(YamlConstants.SCHEMA).asTextValue();
        String abstrakt = definition.path(YamlConstants.ABSTRACT).asTextValue();
        String title = definition.path(YamlConstants.TITLE).asTextValue();
        MatlabComplexOutputDescription desc
                = new MatlabComplexOutputDescription();
        desc.setId(id);
        desc.setTitle(title);
        desc.setAbstract(abstrakt);
        desc.setMatlabType(MatlabType.FILE);
        desc.setMimeType(mimeType);
        desc.setSchema(schema);
        return desc;
    }

    private MatlabLiteralInputDescription createLiteralInput(YamlNode definition) {
        String id = definition.path(YamlConstants.IDENTIFIER).textValue();
        if (id == null || id.isEmpty()) {
            throw new MatlabConfigurationException("Missing process input identifier");
        }
        LiteralType type = LiteralType
                .of(definition.path(YamlConstants.TYPE).textValue());
        if (type == null) {
            throw new MatlabConfigurationException("Missing type for input %s", id);
        }
        int minOccurs = definition.path(YamlConstants.MIN_OCCURS).asIntValue(1);
        int maxOccurs = definition.path(YamlConstants.MAX_OCCURS).asIntValue(1);
        String abstrakt = definition.path(YamlConstants.ABSTRACT).asTextValue();
        String title = definition.path(YamlConstants.TITLE).asTextValue();
        String unit = definition.path(YamlConstants.UNIT).asTextValue();

        if (maxOccurs < 1 || minOccurs < 0 || maxOccurs < minOccurs) {
            throw new MatlabConfigurationException("Invalid min/max occurs: [%d,%d] for input %s",
                                                   minOccurs, maxOccurs, id);
        }

        MatlabLiteralInputDescription desc = new MatlabLiteralInputDescription();
        desc.setId(id);
        desc.setTitle(title);
        desc.setAbstract(abstrakt);
        desc.setMinOccurs(minOccurs);
        desc.setMaxOccurs(maxOccurs);
        desc.setType(type);
        desc.setUnit(unit);
        desc.setMatlabType(type.getMatlabType());
        if (definition.path(YamlConstants.VALUES).isSequence()) {
            List<String> allowedValues = Lists.newLinkedList();
            for (YamlNode allowedValue : definition.path(YamlConstants.VALUES)) {
                allowedValues.add(allowedValue.asTextValue());
            }
            desc.setAllowedValues(allowedValues);
        }
        return desc;
    }

    private MatlabLiteralOutputDescription createLiteralOutput(
            YamlNode definition) {
        String id = definition.path(YamlConstants.IDENTIFIER).textValue();
        if (id == null || id.isEmpty()) {
            throw new MatlabConfigurationException("Missing output identifier");
        }
        LiteralType type = LiteralType.of(definition.path(YamlConstants.TYPE)
                .textValue());
        if (type == null) {
            throw new MatlabConfigurationException("Missing type for output %s", id);
        }
        String abstrakt = definition.path(YamlConstants.ABSTRACT).asTextValue();
        String title = definition.path(YamlConstants.TITLE).asTextValue();
        String unit = definition.path(YamlConstants.UNIT).asTextValue();
        MatlabLiteralOutputDescription desc
                = new MatlabLiteralOutputDescription();
        desc.setId(id);
        desc.setTitle(title);
        desc.setAbstract(abstrakt);
        desc.setType(type);
        desc.setUnit(unit);
        desc.setMatlabType(type.getMatlabType());
        return desc;
    }

    private MatlabInputDescripton createInput(YamlNode definition) {
        if (definition.path(YamlConstants.TYPE).isText()) {
            return createLiteralInput(definition);
        } else {
            return createComplexInput(definition);
        }
    }

    private MatlabOutputDescription createOutput(YamlNode definition) {
        if (definition.path(YamlConstants.TYPE).isText()) {
            return createLiteralOutput(definition);
        } else {
            return createComplexOutput(definition);
        }
    }

    public MatlabProcessDescription createProcessDescription(YamlNode definition) {
        String id = definition.path(YamlConstants.IDENTIFIER).asTextValue();
        if (id == null || id.isEmpty()) {
            throw new MatlabConfigurationException("Missing process identifier");
        }
        String title = definition.path(YamlConstants.TITLE).asTextValue();
        String abstrakt = definition.path(YamlConstants.ABSTRACT).asTextValue();
        String version = definition.path(YamlConstants.VERSION).asTextValue();
        String function = definition.path(YamlConstants.FUNCTION).asTextValue();
        if (function == null || function.isEmpty()) {
            throw new MatlabConfigurationException("Missing function name for process %s", id);
        }
        boolean storeSupported = definition.path(YamlConstants.STORE_SUPPPORTED)
                .asBooleanValue(true);
        boolean statusSupported = definition
                .path(YamlConstants.STATUS_SUPPORTED).asBooleanValue(false);

        MatlabProcessDescription desc = new MatlabProcessDescription();
        desc.setFunction(function);
        desc.setId(id);
        desc.setTitle(title);
        desc.setAbstract(abstrakt);
        desc.setVersion(version);
        desc.setStoreSupported(storeSupported);
        desc.setStatusSupported(statusSupported);
        for (YamlNode input : definition.path(YamlConstants.INPUTS)) {
            desc.addInputDescription(createInput(input));
        }
        if (desc.getInputs().isEmpty()) {
            throw new MatlabConfigurationException("Missing input definitions for process %s", id);
        }
        for (YamlNode output : definition.path(YamlConstants.OUTPUTS)) {
            desc.addOutputDescription(createOutput(output));
        }
        if (desc.getOutputs().isEmpty()) {
            throw new MatlabConfigurationException("Missing output definitions for process %s", id);
        }
        desc.setClientConfiguration(createClientConfig(definition
                .path(YamlConstants.CONNECTION)));
        desc.setProcessDescription(createXmlDescription(desc));
        return desc;
    }

    private MatlabClientConfiguration createClientConfig(YamlNode settings) {
        checkArgument(settings != null && settings.exists());
        MatlabClientConfiguration.Builder b = MatlabClientConfiguration
                .builder();
        b.withInstanceConfiguration(MatlabInstanceConfiguration
                        .builder()/*.hidden()*/.build());
        if (settings.isMap()) {
            b.withAddress(settings.path(YamlConstants.HOST).textValue(),
                          settings.path(YamlConstants.PORT).intValue());
        } else if (settings.isText()) {
            String address = settings.textValue();
            if (address.startsWith("ws://") ||
                address.startsWith("wss://")) {
                b.withAddress(URI.create(address));
            } else if (address.startsWith("file://")) {
                try {
                    b.withDirectory(URI.create(address).toURL().getFile());
                } catch (MalformedURLException ex) {
                    throw new MatlabConfigurationException(ex);
                }
            } else if (!address.equalsIgnoreCase("local")) {
                throw new MatlabConfigurationException("Unsupported connection setting");
            }
        } else {
            throw new MatlabConfigurationException("Missing or invalid connection setting");
        }
        return b.build();
    }

private ProcessDescriptionType createXmlDescription(
            MatlabProcessDescription desc) {
        ProcessDescriptionsDocument document = ProcessDescriptionsDocument.Factory.newInstance();
		ProcessDescriptions processDescriptions = document.addNewProcessDescriptions();
		ProcessDescriptionType xbDescription = processDescriptions.addNewProcessDescription();

        xbDescription.setStatusSupported(desc.isStatusSupported());
        xbDescription.setStoreSupported(desc.isStoreSupported());
        xbDescription.setProcessVersion(desc.getVersion());
        xbDescription.addNewIdentifier().setStringValue(desc.getId());
        xbDescription.addNewTitle().setStringValue(desc.hasTitle() ? desc
                .getTitle() : desc.getId());
        if (desc.hasAbstract()) {
            xbDescription.addNewAbstract().setStringValue(desc.getAbstract());
        }
        if (desc.getInputs().size() > 0) {
            DataInputs xbInputs = xbDescription.addNewDataInputs();

            for (MatlabInputDescripton input : desc.getInputs()) {
                InputDescriptionType xbInput = xbInputs.addNewInput();
                xbInput.addNewIdentifier().setStringValue(input.getId());
                xbInput.setMinOccurs(BigInteger.valueOf(input.getMinOccurs()));
                xbInput.setMaxOccurs(BigInteger.valueOf(input.getMaxOccurs()));
                xbInput.addNewTitle().setStringValue(input.hasTitle() ? input
                        .getTitle() : input.getId());
                if (input.hasAbstract()) {
                    xbInput.addNewAbstract().setStringValue(input.getAbstract());
                }
                if (input instanceof MatlabLiteralInputDescription) {
                    MatlabLiteralInputDescription literalInput
                            = (MatlabLiteralInputDescription) input;
                    LiteralInputType xbLiteralInput = xbInput
                            .addNewLiteralData();
                    xbLiteralInput.addNewDataType().setReference(literalInput
                            .getType().getXmlType());
                    if (literalInput.hasDefaultValue()) {
                        xbLiteralInput.setDefaultValue(literalInput
                                .getDefaultValue());
                    }
                    if (literalInput.hasAllowedValues()) {
                        AllowedValues allowed = xbLiteralInput
                                .addNewAllowedValues();
                        for (String allowedValue : literalInput
                                .getAllowedValues()) {
                            allowed.addNewValue().setStringValue(allowedValue);
                        }
                    } else {
                        xbLiteralInput.addNewAnyValue();
                    }
                    if (literalInput.hasUnit()) {
                        SupportedUOMsType unit = SupportedUOMsType.Factory.newInstance();
                        unit.addNewDefault().addNewUOM().setStringValue(literalInput.getUnit());
                        unit.addNewSupported().addNewUOM().setStringValue(literalInput.getUnit());
                        xbLiteralInput.setUOMs(unit);
                    }
                } else if (input instanceof MatlabComplexInputDescription) {
                    MatlabComplexInputDescription complexInput
                            = (MatlabComplexInputDescription) input;
                    SupportedComplexDataInputType xbComplexInput = xbInput
                            .addNewComplexData();

                    ComplexDataCombinationType xbDefault = xbComplexInput
                            .addNewDefault();
                    ComplexDataDescriptionType xbDefaultFormat = xbDefault
                            .addNewFormat();
                    if (complexInput.getSchema() != null) {
                        xbDefaultFormat.setSchema(complexInput.getSchema());
                    }
                    if (complexInput.getEncoding() != null) {
                        xbDefaultFormat.setEncoding(complexInput.getEncoding());
                    }

                    xbDefaultFormat.setMimeType(complexInput.getMimeType());

                    ComplexDataCombinationsType xbSupported = xbComplexInput
                            .addNewSupported();
                    ComplexDataDescriptionType xbSupportedFormat = xbSupported
                            .addNewFormat();
                    if (complexInput.getSchema() != null) {
                        xbSupportedFormat.setSchema(complexInput.getSchema());
                    }
                    if (complexInput.getEncoding() != null) {
                        xbSupportedFormat.setEncoding(complexInput.getEncoding());
                    }
                    xbSupportedFormat.setMimeType(complexInput.getMimeType());
                }
            }
        }
        if (desc.getOutputs().size() > 0) {
            ProcessOutputs xbOutputs = xbDescription.addNewProcessOutputs();
            for (MatlabOutputDescription output : desc.getOutputs()) {
                OutputDescriptionType xbOutput = xbOutputs.addNewOutput();
                xbOutput.addNewIdentifier().setStringValue(output.getId());
                xbOutput.addNewTitle().setStringValue(output.hasTitle() ? output
                        .getTitle() : output.getId());
                if (output.hasAbstract()) {
                    xbOutput.addNewAbstract().setStringValue(output
                            .getAbstract());
                }

                if (output instanceof MatlabLiteralOutputDescription) {
                    MatlabLiteralOutputDescription literalOutput
                            = (MatlabLiteralOutputDescription) output;
                    LiteralOutputType xbLiteralOutput = xbOutput
                            .addNewLiteralOutput();
                    xbLiteralOutput.addNewDataType().setReference(literalOutput
                            .getType().getXmlType());
                    if (literalOutput.hasUnit()) {
                        SupportedUOMsType unit = SupportedUOMsType.Factory.newInstance();
                        unit.addNewDefault().addNewUOM().setStringValue(literalOutput.getUnit());
                        unit.addNewSupported().addNewUOM().setStringValue(literalOutput.getUnit());
                        xbLiteralOutput.setUOMs(unit);
                    }
                } else if (output instanceof MatlabComplexOutputDescription) {
                    MatlabComplexOutputDescription complexOutput
                            = (MatlabComplexOutputDescription) output;
                    SupportedComplexDataType xbComplexOutput = xbOutput
                            .addNewComplexOutput();

                    ComplexDataCombinationType xbDefault = xbComplexOutput
                            .addNewDefault();
                    ComplexDataDescriptionType xbDefaultFormat = xbDefault
                            .addNewFormat();
                    if (complexOutput.getSchema() != null) {
                        xbDefaultFormat.setSchema(complexOutput.getSchema());
                    }
                    if (complexOutput.getEncoding() != null) {
                        xbDefaultFormat.setEncoding(complexOutput.getEncoding());
                    }
                    xbDefaultFormat.setMimeType(complexOutput.getMimeType());

                    ComplexDataCombinationsType xbSupported = xbComplexOutput
                            .addNewSupported();
                    ComplexDataDescriptionType xbSupportedFormat = xbSupported
                            .addNewFormat();
                    if (complexOutput.getSchema() != null) {
                        xbSupportedFormat.setSchema(complexOutput.getSchema());
                    }
                    if (complexOutput.getEncoding() != null) {
                        xbSupportedFormat.setEncoding(complexOutput.getEncoding());
                    }
                    xbSupportedFormat.setMimeType(complexOutput.getMimeType());
                }
            }
        }

        return xbDescription;
    }
}
