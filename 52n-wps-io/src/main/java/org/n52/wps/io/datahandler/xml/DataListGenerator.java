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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import org.n52.wps.io.IStreamableGenerator;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.DataListDataBinding;
import org.n52.wps.io.datahandler.binary.LargeBufferStream;
import org.w3c.dom.Node;

public class DataListGenerator extends AbstractXMLGenerator implements IStreamableGenerator {
	
	
	public DataListGenerator()
	{
		super();
	}
	
	public DataListGenerator(boolean pReadWPSConfig)
	{
		super(pReadWPSConfig);
	}

	@Override
	public OutputStream generate(IData coll) {
		// TODO Auto-generated method stub
		LargeBufferStream baos = new LargeBufferStream();
		this.writeToStream(coll, baos);
		return baos;
	}

	@Override
	public Class[] getSupportedInternalInputDataType() {
		Class[] supportedClasses = {DataListDataBinding.class};
		return supportedClasses;
		
	}

	@Override
	public void writeToStream(IData data, OutputStream os) {
		OutputStreamWriter w = new OutputStreamWriter(os);
		//write(data, w);	
		//TODO
	}

	@Override
	public Node generateXML(IData coll, String schema) {
		// TODO Auto-generated method stub
		return ((DataListDataBinding)coll).getPayload().getDomNode();
		
	}

}
