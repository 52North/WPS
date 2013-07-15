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
package org.n52.wps.io.datahandler.generator.mapserver;

import edu.umn.gis.mapscript.colorObj;

/**
 * This is a BindingClass for Mapscript ColorStyles.
 * 
 * @author Jacob Mendt
 * 
 * @TODO Offer various different styles (For example getBlack() getGrey() ...)
 */
public class MSColorStyles {

	/**
	 * Returns an mapscript color object for the default color red.
	 * 
	 * @return colorObj Mapscript color object red
	 */
	public static colorObj getDefaultColor() {
		return new colorObj(255, 128, 128, 0);
	}

	/**
	 * Returns an mapscript color object for the default outline color grey.
	 * 
	 * @return colorObj Mapscript color object grey
	 */
	public static colorObj getDefaultOutlineColor() {
		return new colorObj(96, 96, 96, 0);
	}
}
