/*****************************************************************
Copyright © 2007 52°North Initiative for Geospatial Open Source Software GmbH

 Author: foerster
 

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
package org.n52.wps.server.database;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;

import org.apache.commons.io.IOUtils;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.RetrieveResultServlet;
import org.n52.wps.server.WebProcessingService;
import org.n52.wps.server.response.Response;

/*
 * @author foerster
 *
 */
public class FlatFileDatabase implements IDatabase {

	String baseDir = null;
	private static IDatabase db;
	
	/**
	 * File pattern: WPS base directory/Databases/FlatFile/{id}
	 *
	 */
	private FlatFileDatabase() {
		baseDir = WebProcessingService.BASE_DIR + File.separator + "Databases" + File.separator + "FlatFile";
		File f = new File(baseDir);
		f.mkdirs();
	}
	
	public static IDatabase getInstance() {
		if(db == null) {
			db = new FlatFileDatabase();
		}
		return db;
	}
	
	/* (non-Javadoc)
	 * @see org.n52.wps.server.database.IDatabase#generateRetrieveResultURL(java.lang.String)
	 */
	public String generateRetrieveResultURL(String id) {
		return "http://" + 
		WPSConfig.getInstance().getWPSConfig().getServer().getHostname() + ":" + 
		WPSConfig.getInstance().getWPSConfig().getServer().getHostport() + "/" + 
		WebProcessingService.WEBAPP_PATH + "/" + RetrieveResultServlet.SERVLET_PATH + "?id=" + id;
	}

	/* (non-Javadoc)
	 * @see org.n52.wps.server.database.IDatabase#getConnection()
	 */
	public Connection getConnection() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.n52.wps.server.database.IDatabase#getConnectionURL()
	 */
	public String getConnectionURL() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.n52.wps.server.database.IDatabase#getDatabaseName()
	 */
	public String getDatabaseName() {
		// TODO Auto-generated method stub
		return "FlatFileDatabase";
	}

	/* (non-Javadoc)
	 * @see org.n52.wps.server.database.IDatabase#insertResponse(org.n52.wps.server.response.Response)
	 */
	public String insertResponse(Response response) {
		// TODO Auto-generated method stub
		return this.storeResponse(response);
	}

	/* (non-Javadoc)
	 * @see org.n52.wps.server.database.IDatabase#lookupResponse(java.lang.String)
	 */
	public InputStream lookupResponse(String request_id) {
		// TODO Auto-generated method stub
		File f = new File(baseDir + File.separator + request_id);
		try {
			if(f.exists()){
			
				return new FileInputStream(f);
			}
		}
		catch(IOException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.n52.wps.server.database.IDatabase#shutdown()
	 */
	public void shutdown() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.n52.wps.server.database.IDatabase#storeComplexValue(java.lang.String, java.io.ByteArrayOutputStream, java.lang.String)
	 */
	public String storeComplexValue(String id, ByteArrayOutputStream stream,
			String type) {
		// TODO enhance for multiple ProcessResults
		byte[] bytes = stream.toByteArray();
		try {
			File f = new File(baseDir+File.separator+id);
			f.createNewFile();
			FileOutputStream fos = new FileOutputStream(f);
			IOUtils.write(bytes, fos);
		}
		catch(IOException e) {
			throw new RuntimeException(e);
		}
		return generateRetrieveResultURL(id);
	}

	/* (non-Javadoc)
	 * @see org.n52.wps.server.database.IDatabase#storeResponse(org.n52.wps.server.response.Response)
	 */
	public String storeResponse(Response response) {
		
		
		File f = new File(baseDir + File.separator + response.getUniqueId());
		try {
			FileOutputStream os = new FileOutputStream(f);
			response.save(os);
		}
		catch(ExceptionReport e) {
			throw new RuntimeException(e);
		}
		catch(FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		return generateRetrieveResultURL(Long.toString(response.getUniqueId()));
	}

	/* (non-Javadoc)
	 * @see org.n52.wps.server.database.IDatabase#updateResponse(org.n52.wps.server.response.Response)
	 */
	public void updateResponse(Response response) {
		this.storeResponse(response);

	}

}
