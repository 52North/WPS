/***************************************************************
Copyright © 2009 52∞North Initiative for Geospatial Open Source Software GmbH

 Author: Benjamin Proﬂ, 52∞North

 Contact: Andreas Wytzisk, 
 52∞North Initiative for Geospatial Open Source SoftwareGmbH, 
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
 Software Foundationís web page, http://www.fsf.org.

 ***************************************************************/
package org.n52.wps.io.datahandler.xml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.IStreamableParser;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;

public class GRASSKMLParser extends AbstractXMLParser implements IStreamableParser{

	@Override
	public GenericFileDataBinding parse(InputStream input, String mimeType) {
		GenericFileDataBinding data = new GenericFileDataBinding(
				new GenericFileData(input, mimeType));
		return data;
	}

	@Override
	public Class<?>[] getSupportedInternalOutputDataType() {
		Class<?>[] supportedClasses = {GenericFileDataBinding.class};
		return supportedClasses;
	}

	@Override
	public GenericFileDataBinding parseXML(String gml) {

		try {			
			InputStream in = null;
			
			if (!gml.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")) {

				String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

				xml = xml.concat(gml);
				in = new ByteArrayInputStream(xml.getBytes());
			}else{
				in = new ByteArrayInputStream(gml.getBytes());
			}

			GenericFileDataBinding data = new GenericFileDataBinding(
					new GenericFileData(in, "text/xml"));

			return data;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
		
	}

	@Override
	public GenericFileDataBinding parseXML(InputStream stream) {
		GenericFileDataBinding data = new GenericFileDataBinding(
				new GenericFileData(stream, "text/xml"));

		return data;
	}

	@Override
	public GenericFileDataBinding parseXML(URI uri) {
		try{
			URL url = uri.toURL();
			URLConnection connection = url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(false);
			InputStream stream = connection.getInputStream();
			GenericFileDataBinding data = new GenericFileDataBinding(new GenericFileData(stream, "text/xml"));
			return data;
		}
		catch(Exception e) {
			throw new IllegalArgumentException("Error while parsing URI", e);
		}
	}

}
