/**
 * Copyright (C) 2013
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
 * 
 */
package org.n52.wps.server.algorithm.test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

@Algorithm(version = "1.1.0", title="for testing multiple inputs by reference")
public class MultiReferenceBinaryInputAlgorithm extends AbstractAnnotatedAlgorithm {

    private static Logger LOGGER = Logger.getLogger(MultiReferenceBinaryInputAlgorithm.class);

    public MultiReferenceBinaryInputAlgorithm() {
        super();
    }
    
    private GenericFileData result;
    private List<GenericFileData> data;
    
    @ComplexDataOutput(identifier = "result", binding = GenericFileDataBinding.class)
    public GenericFileData getResult() {
        return result;
    }

    @ComplexDataInput(identifier = "data", binding = GenericFileDataBinding.class, minOccurs=1, maxOccurs=2)
    public void setData(List<GenericFileData> data) {
        this.data = data;
    }

    @Execute
    public void runBuffer() {
    	
    	GenericFileData gfd = data.get(0);
    	
    	File f = gfd.getBaseFile(false);
    	
    	try {
			result = new GenericFileData(f, gfd.getMimeType());
		} catch (IOException e) {
			LOGGER.error(e);
		}
    }
}