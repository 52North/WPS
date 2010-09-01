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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.datahandler.binary.LargeBufferStream;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.RetrieveResultServlet;
import org.n52.wps.server.WebProcessingService;
import org.n52.wps.server.request.ExecuteRequest;
import org.n52.wps.server.request.Request;
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
		String mimeType= getMimeTypeForStoreResponse(request_id);
		String[] splittedMimeType= mimeType.split("/");
		String usedMimeType = null; 
		if(splittedMimeType.length==2){
			usedMimeType = splittedMimeType[1];
			if(usedMimeType.equalsIgnoreCase("tiff")){
				usedMimeType = "tif";
			}
		}
		File f = new File(baseDir);
		File[] allFiles = f.listFiles();
		try {
			for(File tempFile : allFiles){
				String fileName = tempFile.getName();
				if(fileName.equalsIgnoreCase(request_id)){
						return new FileInputStream(tempFile);
				}
				if(fileName.startsWith(request_id) && fileName.endsWith(usedMimeType)){
						return new FileInputStream(tempFile);
				}
			}
			return new FileInputStream(baseDir + File.separator + request_id);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Could not find requested file in FlatfileDatabase");
		}
		
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
	public String storeComplexValue(String id, LargeBufferStream stream, String type, String mimeType) {
		// TODO enhance for multiple ProcessResults
				
		LargeBufferStream bufferedBytes = ((LargeBufferStream)stream);
		String uuid = UUID.randomUUID().toString();
		String usedMimeType = mimeType;
		try {
			String[] splittedMimeType= mimeType.split("/");
			if(splittedMimeType.length==2){
				usedMimeType = splittedMimeType[1];
				if(usedMimeType.equalsIgnoreCase("TIFF")){
					usedMimeType = "tif";
				}
			}
			
		
			File f = new File(baseDir+File.separator+id+"result-"+uuid);
			f.createNewFile();
			FileOutputStream fos = new FileOutputStream(f);
			bufferedBytes.close();
			bufferedBytes.writeTo(fos);
			bufferedBytes.destroy();
			fos.close();
			
		//	IOUtils.write(bytes, fos);
			File f_mime = new File(baseDir+File.separator+id+"result-"+uuid+"_mimeType");
			FileOutputStream fos_mime = new FileOutputStream(f_mime);
			IOUtils.write(mimeType, fos_mime);
			fos_mime.close();
			
		}
		catch(IOException e) {
			throw new RuntimeException(e);
		}
	
		return generateRetrieveResultURL(id+"result-"+uuid);
				
	}

	/* (non-Javadoc)
	 * @see org.n52.wps.server.database.IDatabase#storeResponse(org.n52.wps.server.response.Response)
	 */
	public String storeResponse(Response response) {
		Request request = response.getRequest();
		if(!(request instanceof ExecuteRequest)){
			throw new RuntimeException("Could not store response in Flatfile Database. Response id = " + response.getUniqueId());
		}
		ExecuteRequest executeRequest = (ExecuteRequest) request;
		String mimeType = executeRequest.getExecuteResponseBuilder().getMimeType();
		String usedMimeType = mimeType;
		String[] splittedMimeType= mimeType.split("/");
		if(splittedMimeType.length==2){
			usedMimeType = splittedMimeType[1];
			if(usedMimeType.equalsIgnoreCase("TIFF")){
				usedMimeType = "tif";
			}
		}
		
		File f = new File(baseDir+File.separator+response.getUniqueId()+"result."+usedMimeType);
		try {
			FileOutputStream os = new FileOutputStream(f);
			response.save(os);
			os.close();
				
			File f_mime = new File(baseDir+File.separator+response.getUniqueId()+"_mimeType");
			FileOutputStream fos_mime = new FileOutputStream(f_mime);
			IOUtils.write(mimeType, fos_mime);
			fos_mime.close();
		}catch(ExceptionReport e) {
			throw new RuntimeException(e);
		}
		catch(FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return generateRetrieveResultURL(Long.toString(response.getUniqueId()));
	}

	/* (non-Javadoc)
	 * @see org.n52.wps.server.database.IDatabase#updateResponse(org.n52.wps.server.response.Response)
	 */
	public void updateResponse(Response response) {
		
		this.storeResponse(response);

	}

	public String getMimeTypeForStoreResponse(String id) {
		File f_mime = new File(baseDir+File.separator+id+"_mimeType");
		try {
			if(f_mime.exists()){
			
				InputStream stream = new FileInputStream(f_mime);
				String mimeType = "";
				int c;
				while (0 < (c = stream.read())) {
					mimeType=mimeType + (char) c;
				} 
				stream.close();
				return mimeType;
				
			}
		}catch(Exception e){
			
		}
		return null;
	}

	public boolean deleteStoredResponse(String id) {
	/*	System.gc();
		File f = new File(baseDir+File.separator+id);
		File f1 = new File(baseDir+File.separator+id+"_mimeType");
		boolean success = false;
		try {
			if(f.exists()){
				success = f.delete();
				if(!success){
					return false;
				}
			}
			if(f1.exists()){
				success = f1.delete();
				if(!success){
					return false;
				}
			}
		}catch(Exception e){
			return false;
		}*/
		return true;
	}

	public File lookupResponseAsFile(String id) {
		File f = new File(baseDir);
		File[] allFiles = f.listFiles();
		for(File tempFile : allFiles){
			String fileName = tempFile.getName();
			if(fileName.equalsIgnoreCase(id)){
				return tempFile;
			}
			String[] splittedName = fileName.split("result");
			if(splittedName.length==2){
				if(splittedName[0].startsWith(id) || (splittedName[0]+"result").startsWith(id)){
					return tempFile;
				}
			}
		}
		return new File(baseDir + File.separator + id);
	}

	

}
