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

package org.n52.wps.io.datahandler.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import org.n52.wps.io.data.binding.complex.PlainStringBinding;

/**
 * @author Bastian Schaeffer; Matthias Mueller, TU Dresden
 *
 */
public class WCPSQueryParser extends AbstractParser{
	
	public WCPSQueryParser(){
		super();
		supportedIDataTypes.add(PlainStringBinding.class);
	}

	@Override
	public PlainStringBinding parse(InputStream stream, String mimeType, String schema) {
		BufferedReader br;
		StringWriter sw;
		try {
			br = new BufferedReader(new InputStreamReader(stream,"UTF-8"));
		
		    sw=new StringWriter();
		    int k;
		    while((k=br.read())!=-1){
		    	sw.write(k);
		    }
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unsupported Encoding");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	    PlainStringBinding result = new PlainStringBinding(sw.toString());
	    return result;
	}

}
