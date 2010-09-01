/***************************************************************
Copyright © 2009 52°North Initiative for Geospatial Open Source Software GmbH

 Author: Matthias Mueller, TU Dresden

 Contact: Andreas Wytzisk, 
 52°North Initiative for Geospatial Open Source SoftwareGmbH, 
 Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 
 info@52north.org

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; even without the implied WARRANTY OF
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt). If not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA 02111-1307, USA or visit the Free
 Software Foundation’s web page, http://www.fsf.org.

 ***************************************************************/

package org.n52.wps.server.legacy;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralFloatBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;

import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;

public abstract class AbstractLegacyProcessDelegator implements ILegacyProcessDelegator{
	
	private String processID;
	protected List<String> errors;
	private ProcessDescriptionType processDescription;
	
	// to be altered in implementing classes
	protected URI[] supportedBackends = null;
	protected URI[] supportedContainers = null;
	protected File templateWorkspace;
	protected File workspaceDir;
	private LegacyProcessDescription legacyDescription;
	
	private static Logger LOGGER = Logger.getLogger(AbstractLegacyProcessDelegator.class);
	
	public final boolean initialize(String processID, LegacyProcessDescription legacyDescription, ProcessDescriptionType processDescription, File templateWorkspace) {
		
		this.errors = new ArrayList<String>();
		this.processID = processID;
		this.processDescription = processDescription;
		this.legacyDescription = legacyDescription;
		this.templateWorkspace = templateWorkspace;
		boolean success = false;
		
		// brief checks
		if (processDescription != null && processID != null && legacyDescription != null && templateWorkspace != null){
			success = true;
			LOGGER.info("Initialized " + processID + " with template workspace " + templateWorkspace);
		}
		else {
			success = false;
		}
		
		return success;
		
	}

	public final ProcessDescriptionType getDescription() {
		return processDescription;
	}

	public final List<String> getErrors() {
		return errors;
	}
	
	
	public final Class getInputDataType(String id) {
		InputDescriptionType[] inputs = this.getDescription().getDataInputs().getInputArray();
		
		for(InputDescriptionType input : inputs){
			
			//Literal Input
			if(input.isSetLiteralData()){
				String datatype = input.getLiteralData().getDataType().getStringValue();
				if(datatype.contains("tring")){
					return LiteralStringBinding.class;
				}
				if(datatype.contains("oolean")){
					return LiteralBooleanBinding.class;
				}
				if(datatype.contains("loat")){
					return LiteralFloatBinding.class;
				}
				if(datatype.contains("nt")){
					return LiteralIntBinding.class;
				}
				if(datatype.contains("ouble")){
					return LiteralDoubleBinding.class;
				}
			}
			
			//Complex Output
			if(input.isSetComplexData()){
				return GenericFileDataBinding.class;
			}
		}
		
		return null;
	}

	public final Class getOutputDataType(String id) {
		OutputDescriptionType[] outputs = this.getDescription().getProcessOutputs().getOutputArray();
		
		for(OutputDescriptionType output : outputs){
			
			//Literal Output
			if(output.isSetLiteralOutput()){
				String datatype = output.getLiteralOutput().getDataType().getStringValue();
				if(datatype.contains("tring")){
					return LiteralStringBinding.class;
				}
				if(datatype.contains("oolean")){
					return LiteralBooleanBinding.class;
				}
				if(datatype.contains("loat")){
					return LiteralFloatBinding.class;
				}
				if(datatype.contains("ouble")){
					return LiteralDoubleBinding.class;
				}
				if(datatype.contains("nt")){
					return LiteralIntBinding.class;
				}
			}
			
			//Complex Output
			if(output.isSetComplexOutput()){
				return GenericFileDataBinding.class;
			}
		}
		return null;
	}
		
	
	public final String getWellKnownName() {
		return processID;
	}

	
	public final boolean processDescriptionIsValid() {
		return this.getDescription().validate();
	}

	protected final String loadSingleDataItem(IData dataItem){
		
		Object payload = dataItem.getPayload();
		String value = null;
		
		//File
		if (payload instanceof GenericFileData){
			GenericFileData gfd = (GenericFileData)payload;
			value = gfd.writeData(workspaceDir);	
		}
		
		//String
		if (payload instanceof String)
			value = (String) payload;
		
		//Float
		if (payload instanceof Float)
			value = ((Float)payload).toString();

		//Integer
		if (payload instanceof Integer)
			value = ((Integer)payload).toString();
		
		//Double
		if (payload instanceof Double)
			value = ((Double)payload).toString();
		
		return value;
	}
	

	public final URI[] getSupportedBackends() {
		return supportedBackends;
	}

	public final URI[] getSupportedContainers() {
		return supportedContainers;
	}
	
	
	public final boolean isSupportedBackend(URI backend) {
		
		boolean supported = false;
		
		for(URI currentBackend : supportedBackends){
			if (currentBackend.equals(backend)){
				supported = true;
				break;
			}
		}
		
		return supported;
	}

	public final boolean isSupportedContainer(URI container) {

		boolean supported = false;
		
		for(URI currentContainer : this.supportedContainers){
			if (currentContainer.equals(container)){
				supported = true;
				break;
			}
		}
		return supported;
	}
	
	public final LegacyProcessDescription getLegacyDescription(){
		return this.legacyDescription;
	}

	
}
