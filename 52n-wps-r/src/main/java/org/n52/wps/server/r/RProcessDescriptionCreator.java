package org.n52.wps.server.r;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import net.opengis.ows.x11.DomainMetadataType;
import net.opengis.wps.x100.ComplexDataCombinationsType;
import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.LiteralInputType;
import net.opengis.wps.x100.LiteralOutputType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.SupportedComplexDataInputType;
import net.opengis.wps.x100.SupportedComplexDataType;
import net.opengis.wps.x100.ProcessDescriptionType.DataInputs;
import net.opengis.wps.x100.ProcessDescriptionType.ProcessOutputs;

import org.n52.wps.io.GeneratorFactory;
import org.n52.wps.io.IParser;
import org.n52.wps.io.ParserFactory;
import org.n52.wps.io.data.*;
import org.n52.wps.io.data.binding.complex.*;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.server.r.RAnnotation.RAttribute;



public class RProcessDescriptionCreator{

	/**
	 * Usually called from GenericRProcess (extends AbstractObservableAlgorithm)
	 * 
	 * @param annotations contain all process description information
	 * @param wkn Process identifier
	 * @return
	 */
	public ProcessDescriptionType createDescribeProcessType(List<RAnnotation> annotations, String wkn){

			ProcessDescriptionType pdt = ProcessDescriptionType.Factory.newInstance();
			pdt.setStatusSupported(true);
			pdt.setStoreSupported(true);
			
			pdt.addNewIdentifier().setStringValue(wkn);
			pdt.setProcessVersion("1.0.0");
			
			ProcessOutputs outputs = pdt.addNewProcessOutputs();
			DataInputs inputs = pdt.addNewDataInputs();
			
			//iterates over annotations, 
			//The annotation type (RAnnotationType - enumeration) determines
			//next method call
			for(RAnnotation annotation : annotations){
				switch(annotation.getType()){
					case INPUT:
						addInput(inputs, annotation);
						break;
					case OUTPUT:
						addOutput(outputs, annotation);
						break;
					case DESCRIPTION:
						addProcessDescription(pdt, annotation);
						break;
				}
			}
			return pdt;
	}



	/**
	 * @param pdt
	 * @param annotation
	 */
	private void addProcessDescription(ProcessDescriptionType pdt,
		RAnnotation annotation) {
		String abstr = annotation.getAttribute(RAttribute.ABSTRACT);
		pdt.addNewAbstract().setStringValue(""+abstr);
		String title = annotation.getAttribute(RAttribute.TITLE);
		pdt.addNewTitle().setStringValue(""+title);
	}



	private void addInput(DataInputs inputs, RAnnotation annotation) {
		InputDescriptionType input = inputs.addNewInput();	
		
		String identifier = annotation.getAttribute(RAttribute.IDENTIFIER);
		input.addNewIdentifier().setStringValue(identifier);
		
		//title is optional, therefore it could be null
		String title = annotation.getAttribute(RAttribute.TITLE);
		if(title!=null)
			input.addNewTitle().setStringValue(title);
		
		String abstr = annotation.getAttribute(RAttribute.ABSTRACT);
		//abstract is optional, therefore it could be null
		if(abstr != null)
			input.addNewAbstract().setStringValue(abstr);
		
		String min = annotation.getAttribute(RAttribute.MIN_OCCURS);
		BigInteger minOccurs = BigInteger.valueOf(Long.parseLong(min)); 
		input.setMinOccurs(minOccurs);
		
		String max = annotation.getAttribute(RAttribute.MAX_OCCURS);
		BigInteger maxOccurs = BigInteger.valueOf(Long.parseLong(max)); 
		input.setMaxOccurs(maxOccurs);
		
		if(annotation.isComplex()){
			addComplexInput(annotation, input);
		}else {	
			addLiteralInput(annotation, input);

		}
	}



	/**
	 * @param annotation
	 * @param input
	 * @throws RAnnotationException
	 */
	private void addLiteralInput(RAnnotation annotation,
			InputDescriptionType input) throws RAnnotationException {
		LiteralInputType literalInput = input.addNewLiteralData();
		DomainMetadataType dataType = literalInput.addNewDataType();
		dataType.setReference(annotation.getProcessDescriptionType());
		literalInput.setDataType(dataType);
		literalInput.addNewAnyValue();
		String def = annotation.getAttribute(RAttribute.DEFAULT_VALUE);
		if(def != null){
			literalInput.setDefaultValue(def);
		}
	}

	/**
	 * @param annotation
	 * @param input
	 * @throws RAnnotationException
	 */
	private void addComplexInput(RAnnotation annotation,
		InputDescriptionType input) throws RAnnotationException {
		SupportedComplexDataType complexInput = input.addNewComplexData();
		ComplexDataDescriptionType cpldata = complexInput.addNewDefault().addNewFormat();
		cpldata.setMimeType(annotation.getProcessDescriptionType());
		String encod = annotation.getAttribute(RAttribute.ENCODING);
		if(encod!=null)
			cpldata.setEncoding(encod);
		
		Class<? extends IData> iClass = annotation.getDataClass();
		if(iClass.equals(GenericFileDataBinding.class)){
			ComplexDataDescriptionType format = complexInput.addNewSupported().addNewFormat();
			format.setMimeType(annotation.getProcessDescriptionType());
			encod = annotation.getAttribute(RAttribute.ENCODING);
			if(encod!=null)
				format.setEncoding(encod);
		}else{
			addSupportedFormats(complexInput, iClass);
		}
	}


	private void addOutput(ProcessOutputs outputs, RAnnotation out) {
		OutputDescriptionType output = outputs.addNewOutput();
		
		String identifier = out.getAttribute(RAttribute.IDENTIFIER);
		output.addNewIdentifier().setStringValue(identifier);
		
		//title is optional, therefore it could be null
		String title = out.getAttribute(RAttribute.TITLE);
		if(title !=null)
			output.addNewTitle().setStringValue(title);
		
		// is optional, therefore it could be null
		String abstr = out.getAttribute(RAttribute.ABSTRACT);
		if(abstr != null)
			output.addNewAbstract().setStringValue(abstr);
		
		if(out.isComplex()){
			addComplexOutput(out, output);
		}else{	
			addLiteralOutput(out, output);
		}
	}



	/**
	 * @param out
	 * @param output
	 * @throws RAnnotationException
	 */
	private void addLiteralOutput(RAnnotation out, OutputDescriptionType output)
			throws RAnnotationException {
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
	private void addComplexOutput(RAnnotation out, OutputDescriptionType output)
			throws RAnnotationException {
		SupportedComplexDataType complexOutput = output.addNewComplexOutput();
		ComplexDataDescriptionType complexData = complexOutput.addNewDefault().addNewFormat();
		complexData.setMimeType(out.getProcessDescriptionType());
		
		String encod = out.getAttribute(RAttribute.ENCODING);
		if(encod!=null)
			complexData.setEncoding(encod);
		
		
		Class<? extends IData> iClass = out.getDataClass();
		
		if(iClass.equals(GenericFileDataBinding.class)){
				ComplexDataDescriptionType format = complexOutput.addNewSupported().addNewFormat();
				format.setMimeType(out.getProcessDescriptionType());
				encod = out.getAttribute(RAttribute.ENCODING);
				if(encod!=null)
					format.setEncoding(encod);
		}else{
			addSupportedFormats(complexOutput, iClass);
		}
		
	}
	
	
	/**
	 * Searches all available datahandlers for supported encodings / schemas / mime-types and adds
	 * them to the supported list of an output
	 * 
	 * @param complex IData class for which data handlers are searched
	 * @param supportedClass
	 */
	private void addSupportedFormats(SupportedComplexDataType complex, Class<? extends IData> supportedClass){	
		List<IGenerator> generators = GeneratorFactory.getInstance().getAllGenerators();
		List<IGenerator> foundGenerators = new ArrayList<IGenerator>();
		for(IGenerator generator : generators) {
			Class[] supportedClasses = generator.getSupportedDataBindings();
			for(Class clazz : supportedClasses){
				if(clazz.equals(supportedClass)){
					foundGenerators.add(generator);
				}
			}
		}
		ComplexDataCombinationsType supported = complex.addNewSupported();
		for(int i = 0; i<foundGenerators.size(); i++){
				IGenerator generator = foundGenerators.get(i);
				String[] supportedFormats = generator.getSupportedFormats();
				String[] supportedSchemas = generator.getSupportedSchemas();
				if(supportedSchemas == null){
					supportedSchemas = new String[0];
				}
				String[] supportedEncodings = generator.getSupportedEncodings();
				
				for(int j=0; j<supportedFormats.length;j++){
					for(int k=0; k<supportedEncodings.length;k++){
						String supportedFormat = supportedFormats[j];
						ComplexDataDescriptionType supportedCreatedFormat = supported.addNewFormat();
						supportedCreatedFormat.setMimeType(supportedFormat);
						supportedCreatedFormat.setEncoding(supportedEncodings[k]);
						for(int t = 0; t<supportedSchemas.length;t++){
							if(t==0){
								supportedCreatedFormat.setSchema(supportedSchemas[t]);
							}
							else if(t>0){
								ComplexDataDescriptionType supportedCreatedFormatAdditional = supported.addNewFormat();
								supportedCreatedFormatAdditional.setMimeType(supportedFormat);
								supportedCreatedFormatAdditional.setSchema(supportedSchemas[t]);
								supportedCreatedFormatAdditional.setEncoding(supportedEncodings[k]);
							}
						}
					}
				}
			}
					
		
	}



}


