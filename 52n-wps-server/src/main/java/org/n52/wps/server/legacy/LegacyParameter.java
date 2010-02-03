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

public class LegacyParameter {
	
	public final String mimeType;
	public final String schema;
	
	public final String literalDataType;
	public final String defaultCRS;
	
	public final String prefixString;
	public final String suffixString;
	public final String separatorString;
	public final String wpsInputID;
	public final String wpsOutputID;
	public final String pameterID;
	
	public final boolean isComplex;
	public final boolean isLiteral;
	public final boolean isCRS;
	
	public final boolean isInput;
	public final boolean isOutput;
	
	public LegacyParameter (String wpsInputID, String wpsOutputID, String gpParameterID,
			String wpsComplexDataSchema, String wpsMimeType,
			String wpsLiteralDataType, String wpsDefaultCRS,
			String prefixString, String suffixString, String separatorString){
		
		this.prefixString = null2empty(prefixString);
		this.suffixString = null2empty(suffixString);
		this.separatorString = null2empty(separatorString);
		
		this.wpsInputID = null2empty(wpsInputID);
		this.wpsOutputID = null2empty(wpsOutputID);
		this.pameterID = null2empty(gpParameterID); 
		
		
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
	
	public static final String null2empty(String str){
		if (str == null) str = "";
		return str;
	}
	
}
