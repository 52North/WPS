/***************************************************************
Copyright © 2010 52°North Initiative for Geospatial Open Source Software GmbH

 Author: Matthias Mueller, TU Dresden

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

package org.n52.wps.server;

import java.util.ArrayList;


public class ProcessIDRegistry {
	
	private static ProcessIDRegistry instance = new ProcessIDRegistry();
	private volatile boolean lock = false;
	private static ArrayList<String> idList = new ArrayList<String>();
	
	private ProcessIDRegistry(){
		//empty private constructor
	}
	
	public static ProcessIDRegistry getInstance(){
		return instance;
	}
	
	public boolean addID(String id){
		while (lock){
			//spin
		}
		try{
			lock = true;
			boolean retval = idList.add(id);
			lock = false;
			return retval;
		}
		finally{
			lock = false;
		}
	}
	
	public synchronized boolean removeID(String id){
		while (lock){
			//spin
		}
		try{
			lock = true;
			boolean retval = idList.remove(id);
			lock = false;
			return retval;
		}
		finally{
			lock = false;
		}
	}
	
	public boolean containsID(String id){
		return idList.contains(id);
	}
	
	public String[] getIDs(){
		return idList.toArray(new String[idList.size()]);
	}
	
	protected void clearRegistry(){
		while (lock){
			//spin
		}
		try{
			lock = true;
			idList.clear();
			lock = false;
		}
		finally{
			lock = false;
		}
	}
}
