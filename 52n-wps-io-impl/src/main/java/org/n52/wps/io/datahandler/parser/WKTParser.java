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
package org.n52.wps.io.datahandler.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.wps.io.data.binding.complex.JTSGeometryBinding;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * This class parses String representations out of JTS Geometries.
 * @author Benjamin Pross
 *
 */
public class WKTParser extends AbstractParser {

	private static Logger LOGGER = LoggerFactory.getLogger(WKTParser.class);
	
	public WKTParser() {
		super();
		supportedIDataTypes.add(JTSGeometryBinding.class);
	}
	
	@Override
	public JTSGeometryBinding parse(InputStream input, String mimeType, String schema) {
		
		try {
			Geometry g = new WKTReader().read(new InputStreamReader(input));	
			
			return new JTSGeometryBinding(g);
			
		} catch (ParseException e) {
			LOGGER.error(e.getMessage(), e);
		}finally{
			try {
				input.close();
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		
		return null;
	}

}
