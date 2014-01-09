/**
 * ﻿Copyright (C) 2007
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

package org.n52.wps.server.feed.movingcode;

import java.io.File;

import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.OutputDescriptionType;

import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralFloatBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;

/**
 * @author Matthias Mueller, TU Dresden
 *
 */
public class MovingCodeUtils {
	
	public static Class getInputDataType(MovingCodeObject mco, String id) {
		InputDescriptionType[] inputs = mco.getProcessDescription().getDataInputs().getInputArray();
		
		for(InputDescriptionType input : inputs){
			
			if (input.getIdentifier().getStringValue().equalsIgnoreCase(id)){
				
				//Literal Input
				if(input.isSetLiteralData()){
					String datatype = input.getLiteralData().getDataType().getStringValue();
					if(datatype.equalsIgnoreCase("string")){
						return LiteralStringBinding.class;
					}
					if(datatype.equalsIgnoreCase("boolean")){
						return LiteralBooleanBinding.class;
					}
					if(datatype.equalsIgnoreCase("float")){
						return LiteralFloatBinding.class;
					}
					if(datatype.equalsIgnoreCase("double")){
						return LiteralDoubleBinding.class;
					}
					if(datatype.equalsIgnoreCase("int")){
						return LiteralIntBinding.class;
					}
					if(datatype.equalsIgnoreCase("integer")){
						return LiteralIntBinding.class;
					}
				}
				
				//Complex Output
				if(input.isSetComplexData()){
					return GenericFileDataBinding.class;
				}
			}
		}
		
		return null;
	}
	
	public static Class getOutputDataType(MovingCodeObject mco, String id) {
		OutputDescriptionType[] outputs = mco.getProcessDescription().getProcessOutputs().getOutputArray();
		
		for(OutputDescriptionType output : outputs){
			
			if (output.getIdentifier().getStringValue().equalsIgnoreCase(id)){
				
				//Literal Output
				if(output.isSetLiteralOutput()){
					String datatype = output.getLiteralOutput().getDataType().getStringValue();
					if(datatype.equalsIgnoreCase("string")){
						return LiteralStringBinding.class;
					}
					if(datatype.equalsIgnoreCase("boolean")){
						return LiteralBooleanBinding.class;
					}
					if(datatype.equalsIgnoreCase("float")){
						return LiteralFloatBinding.class;
					}
					if(datatype.equalsIgnoreCase("double")){
						return LiteralDoubleBinding.class;
					}
					if(datatype.equalsIgnoreCase("int")){
						return LiteralIntBinding.class;
					}
					if(datatype.equalsIgnoreCase("integer")){
						return LiteralIntBinding.class;
					}
				}
				
				//Complex Output
				if(output.isSetComplexOutput()){
					return GenericFileDataBinding.class;
				}
			}
		}
		return null;
	}
	
	
	public static String loadSingleDataItem(IData dataItem, File workspaceDir){
		
		Object payload = dataItem.getPayload();
		String fileName = null;
		
		//File
		if (payload instanceof GenericFileData){
			GenericFileData gfd = (GenericFileData)payload;
			fileName = gfd.writeData(workspaceDir);	
		}
		
		//String
		if (payload instanceof String)
			fileName = (String) payload;
		
		//Float
		if (payload instanceof Float)
			fileName = ((Float)payload).toString();

		//Integer
		if (payload instanceof Integer)
			fileName = ((Integer)payload).toString();
		
		//Double
		if (payload instanceof Double)
			fileName = ((Double)payload).toString();
		
		return fileName;
	}
	
	
}
