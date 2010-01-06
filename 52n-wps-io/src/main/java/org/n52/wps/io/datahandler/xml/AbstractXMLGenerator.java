/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. It is compliant 
to the WPS version 0.4.0 (OGC 05-007r4). 

 Copyright (C) 2006 by con terra GmbH

 Authors: 
	Theodor Foerster, ITC, Enschede, the Netherlands
	Carsten Priess, Institute for geoinformatics, University of
Muenster, Germany


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


import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.data.IData;
import org.w3c.dom.Node;

/**
 * This class and its extending subclasses shall provide functionality to
 * create XML encoded String.
 * @author foerster
 *
 */

public abstract class AbstractXMLGenerator implements IGenerator { 
	protected Property[] properties;
	
	public AbstractXMLGenerator() {
		 properties = WPSConfig.getInstance().getPropertiesForGeneratorClass(this.getClass().getName());
	}
	
	public AbstractXMLGenerator(boolean pReadWPSConfig) {
		if (pReadWPSConfig)
		{
			properties = WPSConfig.getInstance().getPropertiesForGeneratorClass(this.getClass().getName());
		}
		else
		{
			properties = new Property[0];
		}
	}
	
	public abstract Node generateXML(IData coll, String schema);
	
	
	public boolean isSupportedFormat(String format) {
		for(String f : getSupportedFormats()) {
			if (f.equalsIgnoreCase(format)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean supportsSchemas() {
		return true;
	}
	
	public String[] getSupportedFormats() {
		return new String[]{"text/xml"};
	}
}
