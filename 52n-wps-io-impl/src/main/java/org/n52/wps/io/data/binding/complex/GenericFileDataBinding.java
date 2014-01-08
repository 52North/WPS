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

package org.n52.wps.io.data.binding.complex;

import org.apache.commons.io.FileUtils;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.IComplexData;


/**
 * @author Matthias Mueller, TU Dresden
 *
 */
public class GenericFileDataBinding implements IComplexData {
	/**
	 * 
	 */
	private static final long serialVersionUID = 625383192227478620L;
	protected GenericFileData payload; 
	
	public GenericFileDataBinding(GenericFileData fileData){
		this.payload = fileData;
	}
	
	public GenericFileData getPayload() {
		return payload;
	}

	public Class getSupportedClass() {
		return GenericFileData.class;
	}
    
    @Override
	public void dispose(){
                //FIXME (MH) The command bellow is flawed because getBaseFile(...) *writes* files from an inputstream into the wps temp directory. 
                   // If the given input stream is closed, the method throws *RuntimeExceptions* that let the process crash.
		//FileUtils.deleteQuietly(payload.getBaseFile(false));
	}
}
