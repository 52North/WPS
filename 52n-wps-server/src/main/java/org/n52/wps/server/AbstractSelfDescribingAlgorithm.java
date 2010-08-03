package org.n52.wps.server;

import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

import org.n52.wps.io.GeneratorFactory;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.IParser;
import org.n52.wps.io.ParserFactory;
import org.n52.wps.io.data.IComplexData;
import org.n52.wps.io.data.ILiteralData;
import org.n52.wps.server.oberserpattern.IObserver;
import org.n52.wps.server.oberserpattern.ISubject;


public abstract class AbstractSelfDescribingAlgorithm extends AbstractAlgorithm implements ISubject{

	protected ProcessDescriptionType initializeDescription() {
		ProcessDescriptionsDocument document = ProcessDescriptionsDocument.Factory.newInstance();
		ProcessDescriptions processDescriptions = document.addNewProcessDescriptions();
		ProcessDescriptionType processDescription = processDescriptions.addNewProcessDescription();
		
		processDescription.setStatusSupported(true);
		processDescription.setStoreSupported(true);
		processDescription.setProcessVersion("1.0.0");
		
		//1. Identifer
		processDescription.addNewIdentifier().setStringValue(this.getClass().getName());
		processDescription.addNewTitle().setStringValue(this.getClass().getCanonicalName());
		//2. Inputs
		DataInputs dataInputs = processDescription.addNewDataInputs();
		List<String> identifiers = this.getInputIdentifiers();
		for(String identifier : identifiers){
			InputDescriptionType dataInput = dataInputs.addNewInput();
			dataInput.setMinOccurs(getMinOccurs(identifier));
			dataInput.setMaxOccurs(getMaxOccurs(identifier));
			dataInput.addNewIdentifier().setStringValue(identifier);
			dataInput.addNewTitle().setStringValue(identifier);
			
			Class inputDataTypeClass = this.getInputDataType(identifier);
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
						literalData.addNewDataType().setReference("xs:"+inputClassType.toLowerCase());
					}
					literalData.addNewAnyValue();				
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
		List<String> literaloutputIdentifiers = this.getOutputIdentifiers();
		for(String identifier : literaloutputIdentifiers){
			OutputDescriptionType dataOutput = dataOutputs.addNewOutput();
			SupportedComplexDataType complexData = dataOutput.addNewComplexOutput();
			ComplexDataCombinationType defaultInputFormat = complexData.addNewDefault();
			ComplexDataCombinationsType supportedtInputFormat = complexData.addNewSupported();
			dataOutput.addNewIdentifier().setStringValue(identifier);
			dataOutput.addNewTitle().setStringValue(identifier);
			dataOutput.addNewAbstract().setStringValue(identifier);
			
			Class outputDataTypeClass = this.getOutputDataType(identifier);
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
	
	public BigInteger getMinOccurs(String identifier){
		return new BigInteger("1");
	}
	public BigInteger getMaxOccurs(String identifier){
		return new BigInteger("1");
	}
	
	public abstract List<String> getInputIdentifiers();
	public abstract List<String> getOutputIdentifiers();
	

	
	private List observers = new ArrayList();

	private Object state = null;

	public Object getState() {
	  return state;
	}

	public void update(Object state) {
	   this.state = state;
	   notifyObservers();
	}

	 public void addObserver(IObserver o) {
	   observers.add(o);
	 }

	 public void removeObserver(IObserver o) {
	   observers.remove(o);
	 }

	 public void notifyObservers() {
	   Iterator i = observers.iterator();
	   while (i.hasNext()) {
	     IObserver o = (IObserver) i.next();
	     o.update(this);
	   }
	 }
}
