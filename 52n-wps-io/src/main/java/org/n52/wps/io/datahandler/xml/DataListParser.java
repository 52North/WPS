/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. It is compliant 
to the WPS version 0.4.0 (OGC 05-007r4). 

 Copyright (C) 2006 by con terra GmbH

 Authors: 
	 Bastian Sch�ffer, IfGI

 Contact: Albert Remke, con terra GmbH, Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 52n@conterra.de

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt); if not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA or visit the web page of the Free
 Software Foundation, http://www.fsf.org.

 Created on: 13.06.2006
 ***************************************************************/
package org.n52.wps.io.datahandler.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.n52.wps.io.IStreamableParser;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.DataListDataBinding;
import xint.esa.ese.wps.format.dataList.DataListDocument;


/**
 * This parser handles xml files compliant to gmlpacket.xsd 
 * @author schaeffer
 *
 */
public class DataListParser extends AbstractXMLParser implements IStreamableParser {
	private static Logger LOGGER = Logger.getLogger(DataListParser.class);

	
	public DataListParser() {
		super();
		LOGGER.info("DataListParser");
		
	}
	
	public DataListParser(boolean pReadWPSConfig) {
		super(pReadWPSConfig);
		LOGGER.info("URLListParser2");
	}

	
		

	@Override
	public DataListDataBinding parseXML(String urllist) {
		LOGGER.info("URLListParser");
		LOGGER.info(urllist);
		DataListDocument urlDom = null;
		try {
			urlDom = DataListDocument.Factory.parse(urllist);
		} catch (XmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new DataListDataBinding(urlDom);
		}
	
	@Override
	public DataListDataBinding parseXML(InputStream stream) {
		DataListDocument urlDom = null;
		try {
			urlDom = DataListDocument.Factory.parse(stream);
		} catch (XmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new DataListDataBinding(urlDom);
		}

	@Override
	public IData parse(InputStream input, String mimeType) {
		LOGGER.info("URLListParser6");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class[] getSupportedInternalOutputDataType() {
		Class[] supportedClasses = {DataListDataBinding.class};
		return supportedClasses;
	}

	@Override
	public Object parseXML(URI uri) {
		// TODO Auto-generated method stub
		return null;
	}
	

	


}
