/***************************************************************
Copyright © 2011 52°North Initiative for Geospatial Open Source Software GmbH

 Author: victorzinho; Matthias Mueller, TU Dresden

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

package org.n52.wps.io.datahandler.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.n52.wps.io.IOUtils;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;

/**
 * Generator to create a zipped shapefile by using GDMS drivers:
 * {@link GeotoolsFeatureCollectionDriver} and {@link ShapefileDriver}
 * 
 * @author victorzinho
 */
public class GTBinZippedSHPGenerator extends AbstractGenerator {
	
	public GTBinZippedSHPGenerator(){
		super();
		supportedIDataTypes.add(GTVectorDataBinding.class);	
	}
	
	@Override
	public InputStream generateStream(IData data, String mimeType, String schema) throws IOException {
		
//		// check for correct request before returning the stream
//		if (!(this.isSupportedGenerate(data.getSupportedClass(), mimeType, schema))){
//			throw new IOException("I don't support the incoming datatype");
//		}
		GTBinDirectorySHPGenerator directoryShp = new GTBinDirectorySHPGenerator(); 
		InputStream stream = new FileInputStream(createZippedShapefile(
				directoryShp.writeFeatureCollectionToDirectory(data)));
		
		return stream;
	}

	private File createZippedShapefile(File shapeDirectory) throws IOException {
		if (shapeDirectory != null && shapeDirectory.isDirectory()) {
			File[] files = shapeDirectory.listFiles();
			return IOUtils.zip(files);
		}

		return null;
	}
	

	
}
