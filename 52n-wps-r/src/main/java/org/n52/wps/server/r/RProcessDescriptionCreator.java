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

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import net.opengis.ows.x11.DomainMetadataType;
import net.opengis.ows.x11.MetadataType;
import net.opengis.wps.x100.ComplexDataCombinationsType;
import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.LiteralInputType;
import net.opengis.wps.x100.LiteralOutputType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType.DataInputs;
import net.opengis.wps.x100.ProcessDescriptionType.ProcessOutputs;
import net.opengis.wps.x100.SupportedComplexDataType;

import org.apache.log4j.Logger;
import org.n52.wps.FormatDocument.Format;
import org.n52.wps.io.GeneratorFactory;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.IParser;
import org.n52.wps.io.ParserFactory;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.r.syntax.RAnnotation;
import org.n52.wps.server.r.syntax.RAnnotationException;
import org.n52.wps.server.r.syntax.RAttribute;

public class RProcessDescriptionCreator {

    private static Logger LOGGER = Logger.getLogger(RProcessDescriptionCreator.class);

    /**
     * Usually called from GenericRProcess (extends AbstractObservableAlgorithm)
     * 
     * @param annotations
     *        contain all process description information
     * @param wkn
     *        Process identifier
     * @return
     * @throws ExceptionReport 
     * @throws RAnnotationException 
     */
    public ProcessDescriptionType createDescribeProcessType(List<RAnnotation> annotations, String wkn) throws ExceptionReport, RAnnotationException {
        ProcessDescriptionType pdt = ProcessDescriptionType.Factory.newInstance();
        pdt.setStatusSupported(true);
        pdt.setStoreSupported(true);

        pdt.addNewIdentifier().setStringValue(wkn);
        pdt.setProcessVersion("1.0.0");
        MetadataType mt = pdt.addNewMetadata();

        mt.setTitle("R Script used for this process");
        //mt.setAbout("The R script which is used for this process");
        String url;
        try {
            url = R_Config.getInstance().getScriptURL(wkn).toString();
            mt.setHref(url);
        }
        catch (MalformedURLException e) {
            LOGGER.error("Could not create URL for script file " + wkn, e);
            mt.setHref("N/A");
        }
        
        //Add URL to resource folder
        mt = pdt.addNewMetadata();
        mt.setTitle("Resource Directory URL");
        url = R_Config.getInstance().getResourceDirURL();
		mt.setHref(url);

        mt = pdt.addNewMetadata();
        mt.setTitle("R Session Info");
       // mt.setAbout("R Console output of sessionInfo() method in R, content is generated dynamically for the current state");
        url = R_Config.getInstance().getSessionInfoURL();
        mt.setHref(url);

        ProcessOutputs outputs = pdt.addNewProcessOutputs();
        DataInputs inputs = pdt.addNewDataInputs();

        // iterates over annotations,
        // The annotation type (RAnnotationType - enumeration) determines
        // next method call
        for (RAnnotation annotation : annotations) {
            switch (annotation.getType()) {
            case INPUT:
                addInput(inputs, annotation);
                break;
            case OUTPUT:
                addOutput(outputs, annotation);
                break;
            case DESCRIPTION:
                addProcessDescription(pdt, annotation);
                break;
			case RESOURCE:
				//TODO: add resources to description
				break;
			default:
				break;
            }
        }

        // Add SessionInfo-Output
        OutputDescriptionType outdes = outputs.addNewOutput();
        outdes.addNewIdentifier().setStringValue("sessionInfo");
        outdes.addNewTitle().setStringValue("Information about the R session which has been used");
        outdes.addNewAbstract().setStringValue("Output of the sessionInfo()-method after R-script execution");
        outdes.addNewLiteralOutput().addNewDataType().setStringValue("xs:string");

        return pdt;
    }

    /**
     * @param pdt
     * @param annotation
     * @throws RAnnotationException 
     */
    private void addProcessDescription(ProcessDescriptionType pdt, RAnnotation annotation) throws RAnnotationException {
        String abstr = annotation.getStringValue(RAttribute.ABSTRACT);
        pdt.addNewAbstract().setStringValue("" + abstr);
        String title = annotation.getStringValue(RAttribute.TITLE);
        pdt.addNewTitle().setStringValue("" + title);
    }

    private void addInput(DataInputs inputs, RAnnotation annotation) throws RAnnotationException {
        InputDescriptionType input = inputs.addNewInput();

        String identifier = annotation.getStringValue(RAttribute.IDENTIFIER);
        input.addNewIdentifier().setStringValue(identifier);

        // title is optional, therefore it could be null
        String title = annotation.getStringValue(RAttribute.TITLE);
        if (title != null)
            input.addNewTitle().setStringValue(title);

        String abstr = annotation.getStringValue(RAttribute.ABSTRACT);
        // abstract is optional, therefore it could be null
        if (abstr != null)
            input.addNewAbstract().setStringValue(abstr);

        String min = annotation.getStringValue(RAttribute.MIN_OCCURS);
        BigInteger minOccurs = BigInteger.valueOf(Long.parseLong(min));
        input.setMinOccurs(minOccurs);

        String max = annotation.getStringValue(RAttribute.MAX_OCCURS);
        BigInteger maxOccurs = BigInteger.valueOf(Long.parseLong(max));
        input.setMaxOccurs(maxOccurs);

        if (annotation.isComplex()) {
            addComplexInput(annotation, input);
        }
        else {
            addLiteralInput(annotation, input);

        }
    }

    /**
     * @param annotation
     * @param input
     * @throws RAnnotationException
     */
    private void addLiteralInput(RAnnotation annotation, InputDescriptionType input) throws RAnnotationException {
        LiteralInputType literalInput = input.addNewLiteralData();
        DomainMetadataType dataType = literalInput.addNewDataType();
        dataType.setReference(annotation.getProcessDescriptionType());
        literalInput.setDataType(dataType);
        literalInput.addNewAnyValue();
        String def = annotation.getStringValue(RAttribute.DEFAULT_VALUE);
        if (def != null) {
            literalInput.setDefaultValue(def);
        }
    }

    /**
     * @param annotation
     * @param input
     * @throws RAnnotationException
     */
    private void addComplexInput(RAnnotation annotation, InputDescriptionType input) throws RAnnotationException {
        SupportedComplexDataType complexInput = input.addNewComplexData();
        ComplexDataDescriptionType cpldata = complexInput.addNewDefault().addNewFormat();
        cpldata.setMimeType(annotation.getProcessDescriptionType());
        String encod = annotation.getStringValue(RAttribute.ENCODING);
        if (encod != null && encod != "base64")
            cpldata.setEncoding(encod);

        Class< ? extends IData> iClass = annotation.getDataClass();
        if (iClass.equals(GenericFileDataBinding.class)) {
        	ComplexDataCombinationsType supported = complexInput.addNewSupported();
            ComplexDataDescriptionType format = supported.addNewFormat();
            format.setMimeType(annotation.getProcessDescriptionType());
            encod = annotation.getStringValue(RAttribute.ENCODING);
            if (encod != null)
                format.setEncoding(encod);
	            if(encod=="base64"){
	            	//set a format entry such that not encoded data is supported as well
	                ComplexDataDescriptionType format2 = supported.addNewFormat();
	                format2.setMimeType(annotation.getProcessDescriptionType());
	            }
        }
        else {
            addSupportedInputFormats(complexInput, iClass);
        }
    }

    private void addOutput(ProcessOutputs outputs, RAnnotation out) throws RAnnotationException {
        OutputDescriptionType output = outputs.addNewOutput();

        String identifier = out.getStringValue(RAttribute.IDENTIFIER);
        output.addNewIdentifier().setStringValue(identifier);

        // title is optional, therefore it could be null
        String title = out.getStringValue(RAttribute.TITLE);
        if (title != null)
            output.addNewTitle().setStringValue(title);

        // is optional, therefore it could be null
        String abstr = out.getStringValue(RAttribute.ABSTRACT);
        if (abstr != null)
            output.addNewAbstract().setStringValue(abstr);

        if (out.isComplex()) {
            addComplexOutput(out, output);
        }
        else {
            addLiteralOutput(out, output);
        }
    }

    /**
     * @param out
     * @param output
     * @throws RAnnotationException
     */
    private void addLiteralOutput(RAnnotation out, OutputDescriptionType output) throws RAnnotationException {
        LiteralOutputType literalOutput = output.addNewLiteralOutput();
        DomainMetadataType dataType = literalOutput.addNewDataType();
        dataType.setReference(out.getProcessDescriptionType());
        literalOutput.setDataType(dataType);
    }

    /**
     * @param out
     * @param output
     * @throws RAnnotationException
     */
    private void addComplexOutput(RAnnotation out, OutputDescriptionType output) throws RAnnotationException {
        SupportedComplexDataType complexOutput = output.addNewComplexOutput();
        ComplexDataDescriptionType complexData = complexOutput.addNewDefault().addNewFormat();
        complexData.setMimeType(out.getProcessDescriptionType());

        String encod = out.getStringValue(RAttribute.ENCODING);
        if (encod != null && encod !="base64"){
        	//base64 shall not be default, but occur in the supported formats
            complexData.setEncoding(encod);
        }
        Class< ? extends IData> iClass = out.getDataClass();

        if (iClass.equals(GenericFileDataBinding.class)) {
 
        	ComplexDataCombinationsType supported = complexOutput.addNewSupported();
            ComplexDataDescriptionType format = supported.addNewFormat();
            format.setMimeType(out.getProcessDescriptionType());
            encod = out.getStringValue(RAttribute.ENCODING);
            	
            if (encod != null){
                format.setEncoding(encod);
                if(encod=="base64"){
                	//set a format entry such that not encoded data is supported as well
                    ComplexDataDescriptionType format2 = supported.addNewFormat();
                    format2.setMimeType(out.getProcessDescriptionType());
                }
            }
        }
        else {
            addSupportedOutputFormats(complexOutput, iClass);
        }

    }
	
	/**
	 * Searches all available datahandlers for supported encodings / schemas / mime-types and adds
	 * them to the supported list of an output
	 * 
	 * @param complex IData class for which data handlers are searched
	 * @param supportedClass
	 */
	private void addSupportedOutputFormats(SupportedComplexDataType complex, Class<? extends IData> supportedClass){	
		// retrieve a list of generators which support the supportedClass-input
		List<IGenerator> generators = GeneratorFactory.getInstance().getAllGenerators();
		List<IGenerator> foundGenerators = new ArrayList<IGenerator>();
		for(IGenerator generator : generators) {
			Class<?>[] supportedClasses = generator.getSupportedDataBindings();
			for(Class<?> clazz : supportedClasses){
				if(clazz.equals(supportedClass)){
					foundGenerators.add(generator);
				}
			}
		}
		
		ComplexDataCombinationsType supported = complex.addNewSupported();
		for(int i = 0; i<foundGenerators.size(); i++){
				IGenerator generator = foundGenerators.get(i);
				Format[] fullFormats = generator.getSupportedFullFormats();
				
				for (Format format : fullFormats) {
					ComplexDataDescriptionType newSupportedFormat = supported.addNewFormat();
					String encoding = format.getEncoding();
					if(encoding != null)
						newSupportedFormat.setEncoding(encoding);
					else
						newSupportedFormat.setEncoding(IOHandler.DEFAULT_ENCODING);
					
					newSupportedFormat.setMimeType(format.getMimetype());
					String schema = format.getSchema();
					if(schema != null)
						newSupportedFormat.setSchema(schema);
				}
				
		}
					
		
	}

	
	
	/**
	 * Searches all available datahandlers for supported encodings / schemas / mime-types and adds
	 * them to the supported list of an output
	 * 
	 * @param complex IData class for which data handlers are searched
	 * @param supportedClass
	 */
	private void addSupportedInputFormats(SupportedComplexDataType complex, Class<? extends IData> supportedClass){	
		// retrieve a list of parsers which support the supportedClass-input
		List<IParser> parsers = ParserFactory.getInstance().getAllParsers();
		List<IParser> foundParsers = new ArrayList<IParser>();
		for(IParser parser : parsers) {
			Class<?>[] supportedClasses = parser.getSupportedDataBindings();
			for(Class<?> clazz : supportedClasses){
				if(clazz.equals(supportedClass)){
					foundParsers.add(parser);
				}
			}
		}
		
		
		//add properties for each parser which is found
		ComplexDataCombinationsType supported = complex.addNewSupported();
		for(int i = 0; i<foundParsers.size(); i++){
				IParser parser = foundParsers.get(i);
				Format[] fullFormats = parser.getSupportedFullFormats();
				for (Format format : fullFormats) {
					ComplexDataDescriptionType newSupportedFormat = supported.addNewFormat();
					String encoding = format.getEncoding();
					if(encoding != null)
						newSupportedFormat.setEncoding(encoding);
					else 
						newSupportedFormat.setEncoding(IOHandler.DEFAULT_ENCODING);
					newSupportedFormat.setMimeType(format.getMimetype());
					String schema = format.getSchema();
					if(schema!=null)
						newSupportedFormat.setSchema(schema);
				}
				
			}
					
		
	}

}
