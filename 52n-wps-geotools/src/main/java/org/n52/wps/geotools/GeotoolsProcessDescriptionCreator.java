package org.n52.wps.geotools;
/*import java.awt.Dimension;
import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.opengis.ows.x11.DomainMetadataType;
import net.opengis.wps.x100.ComplexDataCombinationType;
import net.opengis.wps.x100.ComplexDataCombinationsType;
import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.LiteralInputType;
import net.opengis.wps.x100.LiteralOutputType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionsDocument;
import net.opengis.wps.x100.SupportedComplexDataInputType;
import net.opengis.wps.x100.SupportedComplexDataType;
import net.opengis.wps.x100.ProcessDescriptionType.DataInputs;
import net.opengis.wps.x100.ProcessDescriptionType.ProcessOutputs;
import net.opengis.wps.x100.ProcessDescriptionsDocument.ProcessDescriptions;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.Parameter;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.Envelope2D;
import org.geotools.process.ProcessFactory;
import org.n52.wps.io.GeneratorFactory;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.IParser;
import org.n52.wps.io.ParserFactory;
import org.n52.wps.io.data.IComplexData;
import org.n52.wps.io.data.ILiteralData;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.opengis.feature.type.Name;
import org.opengis.geometry.Envelope;






public class GeotoolsProcessDescriptionCreator {

	public ProcessDescriptionType createDescribeProcessType(ProcessFactory processFactory){
		
		 Name name = processFactory.getNames().iterator().next();
		 System.out.println(name.getLocalPart());
		 Map<String, Parameter<?>> inputParameterInfo = processFactory.getParameterInfo(name);
		 Map<String, Parameter<?>> outputParameterInfo = processFactory.getResultInfo(name, null);
		 return initializeDescription(name.getLocalPart(), inputParameterInfo, outputParameterInfo);
	
		
	}
	
	protected ProcessDescriptionType initializeDescription(String processName, Map<String, Parameter<?>> inputs, Map<String, Parameter<?>> outputs) {
		ProcessDescriptionsDocument document = ProcessDescriptionsDocument.Factory.newInstance();
		ProcessDescriptions processDescriptions = document.addNewProcessDescriptions();
		ProcessDescriptionType processDescription = processDescriptions.addNewProcessDescription();
		processDescription.setStatusSupported(true);
		processDescription.setStoreSupported(true);
		processDescription.setProcessVersion("1.0.0");
		
		//1. Identifer
		processDescription.addNewIdentifier().setStringValue("gt:"+processName);
		processDescription.addNewTitle().setStringValue("gt:"+processName);
		//2. Inputs
		DataInputs dataInputs = processDescription.addNewDataInputs();
		Set<String> identifiers = inputs.keySet();
		for(String identifier : identifiers){
			Parameter<?> parameter = inputs.get(identifier);
			
			Class inputDataTypeClass = this.getDataTypeForParameter(identifier, parameter);
			if(inputDataTypeClass==null){
				return null;
			}
			//ignore envelope input. set to max extend for input data
			if(inputDataTypeClass.equals(Envelope2D.class) || inputDataTypeClass.equals(Envelope.class) ){
				continue;
			}
			InputDescriptionType dataInput = dataInputs.addNewInput();
		
			
			dataInput.setMinOccurs(new BigInteger(""+parameter.minOccurs));
			dataInput.setMaxOccurs(new BigInteger(""+parameter.maxOccurs));
			dataInput.addNewIdentifier().setStringValue(identifier);
			dataInput.addNewTitle().setStringValue(identifier);
			Class[] interfaces = inputDataTypeClass.getInterfaces();
						
			for(Class implementedInterface : interfaces){
				if(implementedInterface.equals(ILiteralData.class)){
					LiteralInputType literalData = dataInput.addNewLiteralData();
					String inputClassType = "";
					
					Constructor[] constructors = inputDataTypeClass.getConstructors();
					for(Constructor constructor : constructors){
						Class[] parameters = constructor.getParameterTypes();
						if(parameters.length==1){
							inputClassType	= parameters[0].getSimpleName();
						}
					}
					
					if(inputClassType.length()>0){
						DomainMetadataType datatype = literalData.addNewDataType();
						datatype.setReference("xs:"+inputClassType.toLowerCase());
						literalData.addNewAnyValue();		
					}
							
				}else if(implementedInterface.equals(IComplexData.class)){
					SupportedComplexDataInputType complexData = dataInput.addNewComplexData();
					ComplexDataCombinationType defaultInputFormat = complexData.addNewDefault();
					ComplexDataCombinationsType supportedtInputFormat = complexData.addNewSupported();
					List<IParser> parsers = ParserFactory.getInstance().getAllParsers();
					List<IParser> foundParsers = new ArrayList<IParser>();
					for(IParser parser : parsers) {
						Class[] supportedClasses = parser.getSupportedInternalOutputDataType();
						for(Class clazz : supportedClasses){
							if(clazz.equals(inputDataTypeClass)){
								foundParsers.add(parser);
							}
							
						}
					}
					
					for(int i = 0; i<foundParsers.size(); i++){
						IParser parser = foundParsers.get(i);
						String[] supportedFormats = parser.getSupportedFormats();
						String[] supportedSchemas = parser.getSupportedSchemas();
						if(supportedSchemas == null){
							supportedSchemas = new String[0];
						}
						String[] supportedEncodings = parser.getSupportedEncodings();
						
						for(int j=0; j<supportedFormats.length;j++){
							for(int k=0; k<supportedEncodings.length;k++){
								if(j==0 && k==0 && i == 0){
									String supportedFormat = supportedFormats[j];
									ComplexDataDescriptionType defaultFormat = defaultInputFormat.addNewFormat();
									defaultFormat.setMimeType(supportedFormat);
									defaultFormat.setEncoding(supportedEncodings[k]);
									for(int t = 0; t<supportedSchemas.length;t++){
										if(t==0){
											defaultFormat.setSchema(supportedSchemas[t]);
										}else{
											ComplexDataDescriptionType supportedCreatedFormatAdditional = supportedtInputFormat.addNewFormat();
											supportedCreatedFormatAdditional.setEncoding(supportedEncodings[k]);
											supportedCreatedFormatAdditional.setMimeType(supportedFormat);
											supportedCreatedFormatAdditional.setSchema(supportedSchemas[t]);
											
											
										
										}
									}
								}else{
									String supportedFormat = supportedFormats[j];
									ComplexDataDescriptionType supportedCreatedFormat = supportedtInputFormat.addNewFormat();
									supportedCreatedFormat.setMimeType(supportedFormat);
									supportedCreatedFormat.setEncoding(supportedEncodings[k]);
									for(int t = 0; t<supportedSchemas.length;t++){
										if(t==0){
											supportedCreatedFormat.setSchema(supportedSchemas[t]);
										}
										if(t>0){
											ComplexDataDescriptionType supportedCreatedFormatAdditional = supportedtInputFormat.addNewFormat();
											supportedCreatedFormatAdditional.setEncoding(supportedEncodings[k]);
											supportedCreatedFormatAdditional.setMimeType(supportedFormat);
											supportedCreatedFormatAdditional.setSchema(supportedSchemas[t]);
										}
									}
								}
							}
						}
					}
				}		
			}
		}
		
		//3. Outputs
		ProcessOutputs dataOutputs = processDescription.addNewProcessOutputs();
		Set<String> outputIdentifiers = outputs.keySet();
		for(String identifier : outputIdentifiers){
			Parameter<?> parameter = outputs.get(identifier);
			Class outputDataTypeClass = this.getDataTypeForParameter(identifier, parameter);
			if(outputDataTypeClass==null){
				return null;
			}
			if(outputDataTypeClass.equals(Dimension.class) || outputDataTypeClass.equals(Object.class) ){
				continue;
			}
			OutputDescriptionType dataOutput = dataOutputs.addNewOutput();
			
			
			dataOutput.addNewIdentifier().setStringValue(identifier);
			dataOutput.addNewTitle().setStringValue(identifier);
			dataOutput.addNewAbstract().setStringValue(identifier);
			
			if(outputDataTypeClass==null){
				return null;
			}
			Class[] interfaces = outputDataTypeClass.getInterfaces();
			
			for(Class implementedInterface : interfaces){
					
				
				if(implementedInterface.equals(ILiteralData.class)){
					LiteralOutputType literalData = dataOutput.addNewLiteralOutput();
					String outputClassType = "";
					
					Constructor[] constructors = outputDataTypeClass.getConstructors();
					for(Constructor constructor : constructors){
						Class[] parameters = constructor.getParameterTypes();
						if(parameters.length==1){
							outputClassType	= parameters[0].getSimpleName();
						}
					}
					
					if(outputClassType.length()>0){
						literalData.addNewDataType().setReference("xs:"+outputClassType.toLowerCase());
					}
				
					
				}else if(implementedInterface.equals(IComplexData.class)){
					
						SupportedComplexDataType complexData = dataOutput.addNewComplexOutput();
						ComplexDataCombinationType defaultInputFormat = complexData.addNewDefault();
						ComplexDataCombinationsType supportedtOutputFormat = complexData.addNewSupported();
						
						List<IGenerator> generators = GeneratorFactory.getInstance().getAllGenerators();
						List<IGenerator> foundGenerators = new ArrayList<IGenerator>();
						for(IGenerator generator : generators) {
							Class[] supportedClasses = generator.getSupportedInternalInputDataType();
							for(Class clazz : supportedClasses){
								if(clazz.equals(outputDataTypeClass)){
									foundGenerators.add(generator);
								}
								
							}
					}
					
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
								if(j==0 && k==0 && i == 0){
									String supportedFormat = supportedFormats[j];
									ComplexDataDescriptionType defaultFormat = defaultInputFormat.addNewFormat();
									defaultFormat.setMimeType(supportedFormat);
									defaultFormat.setEncoding(supportedEncodings[k]);
									for(int t = 0; t<supportedSchemas.length;t++){
										if(t==0){
											defaultFormat.setSchema(supportedSchemas[t]);
										}else{
												ComplexDataDescriptionType supportedCreatedFormatAdditional = supportedtOutputFormat.addNewFormat();
												supportedCreatedFormatAdditional.setEncoding(supportedEncodings[k]);
												supportedCreatedFormatAdditional.setMimeType(supportedFormat);
												supportedCreatedFormatAdditional.setSchema(supportedSchemas[t]);
												
											
										}
									}
								}else{
									String supportedFormat = supportedFormats[j];
									ComplexDataDescriptionType supportedCreatedFormat = supportedtOutputFormat.addNewFormat();
									supportedCreatedFormat.setMimeType(supportedFormat);
									supportedCreatedFormat.setEncoding(supportedEncodings[k]);
									for(int t = 0; t<supportedSchemas.length;t++){
										if(t==0){
											supportedCreatedFormat.setSchema(supportedSchemas[t]);
										}
										if(t>0){
											ComplexDataDescriptionType supportedCreatedFormatAdditional = supportedtOutputFormat.addNewFormat();
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
			}
		}
		
		return document.getProcessDescriptions().getProcessDescriptionArray(0);
	}

	private Class getDataTypeForParameter(String identifier, Parameter<?> parameter) {
		Class<?> type = parameter.type;
    	if(type.equals(FeatureCollection.class)){
    		return GTVectorDataBinding.class;
    	}
    	if(type.equals(GridCoverage2D.class)){
    		return GTRasterDataBinding.class;
    	}
    	if(type.equals(Double.class)){
    		return LiteralDoubleBinding.class;
    	}
    	if(type.equals(Integer.class)){
    		return LiteralIntBinding.class;
    	}
    	if(type.equals(String.class)){
    		return LiteralStringBinding.class;
    	}
    	if(type.equals(Boolean.class)){
    		return LiteralBooleanBinding.class;
    	}
    	if(type.equals(Envelope2D.class)){
    		return type;
    	}if(type.equals(Envelope.class)){
    		return type;
    	}
    	if(type.equals(Dimension.class)){
    		return type;
    	}if(type.equals(Object.class)){
    		return type;
    	}
    	return null;
	}
}*/
