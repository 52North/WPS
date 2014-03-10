/**
 * ﻿Copyright (C) 2009 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *       • Apache License, version 2.0
 *       • Apache Software License, version 1.0
 *       • GNU Lesser General Public License, version 3
 *       • Mozilla Public License, versions 1.0, 1.1 and 2.0
 *       • Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.wps.ags;

/**
 * @author Matthias Mueller, TU Dresden
 *
 */
public class ToolParameter {
	
	protected final String mimeType;
	protected final String schema;
	
	protected final String literalDataType;
	protected final String defaultCRS;
	
	protected final String prefixString;
	protected final String suffixString;
	protected final String separatorString;
	protected final String wpsInputID;
	protected final String wpsOutputID;
	protected final String pameterID;
	
	protected final boolean isComplex;
	protected final boolean isLiteral;
	protected final boolean isCRS;
	protected final boolean isOptional;
	
	protected final boolean isInput;
	protected final boolean isOutput;
	
	protected ToolParameter (String wpsInputID, String wpsOutputID, String gpParameterID,
			String wpsComplexDataSchema, String wpsMimeType,
			String wpsLiteralDataType, String wpsDefaultCRS,
			String prefixString, String suffixString, String separatorString, boolean isOptional){
		
		this.prefixString = null2empty(prefixString);
		this.suffixString = null2empty(suffixString);
		this.separatorString = null2empty(separatorString);
		
		this.wpsInputID = null2empty(wpsInputID);
		this.wpsOutputID = null2empty(wpsOutputID);
		this.pameterID = null2empty(gpParameterID); 
		
		this.isOptional = isOptional; 
		
		
		this.mimeType = null2empty(wpsMimeType);
		this.schema = null2empty(wpsComplexDataSchema);
		
		this.literalDataType = null2empty(wpsLiteralDataType);
		this.defaultCRS = null2empty(wpsDefaultCRS);
		
		if (!this.mimeType.isEmpty()) this.isComplex = true;
		else this.isComplex = false;
		
		if (!this.literalDataType.isEmpty()) this.isLiteral = true;
		else this.isLiteral = false;
		
		if (!this.defaultCRS.isEmpty()) this.isCRS = true;
		else this.isCRS = false;
		
		
		if (!this.wpsInputID.isEmpty()) this.isInput = true;
		else this.isInput = false;
		
		if (!this.wpsOutputID.isEmpty()) this.isOutput = true;
		else this.isOutput = false;
		
	}
	
	private static final String null2empty(String str){
		if (str == null) str = "";
		return str;
	}
	
}
