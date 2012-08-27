package org.n52.wps.server.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.GenericFileDataConstants;
import org.n52.wps.server.RetrieveResultServlet;
import org.n52.wps.server.WebProcessingService;
import org.n52.wps.server.response.Response;

/**
 * This Database is intended to store several files in a folder sharing a mimeType 
 * @author gcarrillo
 *
 */
public class FolderDatabase implements IDatabase {

	protected String baseDir = null;
	private static IDatabase db;
	private String separator = "_";
	
	public FolderDatabase(){
		baseDir = WebProcessingService.BASE_DIR + File.separator + "Databases" + File.separator + "Folder";
		File f = new File(baseDir);
		f.mkdirs();
	}
	
	public static synchronized IDatabase getInstance() {
		if(db == null) {
			db = new FolderDatabase();
		}
		return db;
	}
	
	public void shutdown() {
	}

	public String getDatabaseName() {
		return "FolderDatabase";
	}

	public String insertResponse(Response response) {
		return this.storeResponse(response);
	}

	public void updateResponse(Response response) {
		this.storeResponse(response);
	}

	@Override
	public String storeResponse(Response response) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream lookupResponse(String request_id) {
		// TODO Auto-generated method stub
		return null;
	}

	public String storeComplexValue(String id, InputStream stream, String type,
			String mimeType) {
		String usedMimeType = mimeType;
		try {
			String[] splittedMimeType= mimeType.split("/");
			if(splittedMimeType.length==2){
				usedMimeType = splittedMimeType[1];
				if(usedMimeType.equalsIgnoreCase("TIFF")){
					usedMimeType = "tif";
				}
			}
			
			/* Streaming based WPS */
			if(mimeType.toLowerCase().contains("playlist")){
				mimeType = mimeType.split("//+")[0];
			}
			
			String suffix = GenericFileDataConstants.mimeTypeFileTypeLUT().get(mimeType);
			if(suffix==null){
				suffix = "dat";
			}
			
			String[] splittedId  = id.split(separator);
			String folder = "";
			String file = "";
			if (splittedId.length==2){
				folder = splittedId[0];
				file = splittedId[1]; 
			}
			
			// Folder
			File subfolder = new File(baseDir+File.separator+folder);
			if (!subfolder.exists()){				
				subfolder.mkdirs();	
			}
			
			// File
			File f = new File(baseDir+File.separator+folder+File.separator+file+"result."+suffix);
			f.createNewFile();
			FileOutputStream fos = new FileOutputStream(f);
			IOUtils.copy(stream, fos);
			fos.flush();
			fos.close();
			stream.close();
			
			// mimetype file
			File f_mime = new File(baseDir+File.separator+folder+File.separator+folder+"_mimeTypes");
			if (!f_mime.exists()){
				FileOutputStream fos_mime = new FileOutputStream(f_mime);
				IOUtils.write(mimeType, fos_mime);
				fos_mime.close();
			}
		}
		catch(IOException e) {
			throw new RuntimeException(e);
		}
	
		return generateRetrieveResultURL(id);
	}

	public String generateRetrieveResultURL(String id) {
		return "http://" + 
		WPSConfig.getInstance().getWPSConfig().getServer().getHostname() + ":" + 
		WPSConfig.getInstance().getWPSConfig().getServer().getHostport() + "/" + 
		WebProcessingService.WEBAPP_PATH + "/" + RetrieveResultServlet.SERVLET_PATH + "?id=dir" + id;
	}

	public String getMimeTypeForStoreResponse(String id) {
		String folder = id.split(separator)[0];
		File f_mime = new File(baseDir+File.separator+folder+File.separator+folder+"_mimeTypes");
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
		return true;
	}

	
	public File lookupResponseAsFile(String id) {
		String[] splittedId = id.split(separator);
		
		File f = new File(baseDir+File.separator+splittedId[0]+File.separator+splittedId[1]);
		if (f.exists()){
			return f;
		}
		return new File(baseDir + File.separator + id);
	}

}
