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

package org.n52.wps.server.feed.movingcode;

import java.util.LinkedList;

/**
 * @author Matthias Mueller, TU Dresden
 *
 */
public class CommandLineParameter {
	
	private String prefix;
	private String suffix;
	private String separator;
	
	private LinkedList<String> values;
	
	public CommandLineParameter (String prefixString, String suffixString, String separatorString){
		prefix = prefixString;
		suffix = suffixString;
		separator = separatorString;
		
		values = new LinkedList<String>();
		
	}
	
	public void addValue(String value){
		values.add(value);
	}
	
	public String getAsCommandString(){
		String str = prefix;
		
		boolean firstrun = true;
		for (String currentValue : values){
			if (!firstrun){
				str = str + separator + currentValue;
			} else {
				str = str + currentValue;
				firstrun = false;
			}
		}
		
		str = str + suffix;
		return str;
	}
	
	public String getAsPlainString(){
		return values.get(0);
	}
	
	
}
