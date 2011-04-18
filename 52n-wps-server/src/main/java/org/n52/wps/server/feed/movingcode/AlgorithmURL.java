/***************************************************************
Copyright � 2011 52�North Initiative for Geospatial Open Source Software GmbH

 Author: Matthias Mueller, TU Dresden
 
 Contact: Andreas Wytzisk, 
 52�North Initiative for Geospatial Open Source SoftwareGmbH, 
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
 Software Foundation�s web page, http://www.fsf.org.

 ***************************************************************/

package org.n52.wps.server.feed.movingcode;

import java.net.URI;

public class AlgorithmURL {
	
	private final URI uri;
	private static final String SCHEME = "algorithm";
	
	public AlgorithmURL (String str){
		uri = URI.create(str);
	}
	
	public boolean isValid(){
		return uri.getScheme().equalsIgnoreCase(SCHEME);
	}
	
	public String getPublicPath(){
		return uri.getPath();
	}
	
	public String getPrivatePath(){
		return uri.getQuery();
	}
	
}
