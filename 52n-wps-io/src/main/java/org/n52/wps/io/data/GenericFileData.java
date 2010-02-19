/***************************************************************
Copyright © 2009 52°North Initiative for Geospatial Open Source Software GmbH

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

package org.n52.wps.io.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;
import org.geotools.data.DataStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.n52.wps.io.IOUtils;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;

public class GenericFileData {
	
	private static Logger LOGGER = Logger.getLogger(GenericFileData.class);
	
	public final InputStream dataStream;
	public final String fileExtension; 
	public final String mimeType;
	
	public GenericFileData (InputStream stream, String mimeType){
		this.dataStream = stream;
		this.mimeType = mimeType;
		this.fileExtension = GenericFileDataConstants.mimeTypeFileTypeLUT().get(mimeType);
	}
	
	public GenericFileData (File primaryFile, String mimeType) throws IOException{
		this.mimeType = mimeType;
		this.fileExtension = GenericFileDataConstants.mimeTypeFileTypeLUT().get(mimeType);
		
		InputStream is = null;
		
		if (GenericFileDataConstants.getIncludeFilesByMimeType(mimeType) != null){
			
			String baseFile = primaryFile.getName(); 
			baseFile = baseFile.substring(0, baseFile.lastIndexOf("."));
			File directory = new File(primaryFile.getParent());
			String[] extensions = GenericFileDataConstants.getIncludeFilesByMimeType(mimeType);
			
			File[] allFiles = new File[extensions.length + 1];
			
			for (int i = 0; i < extensions.length; i++)
				allFiles[i] = new File(directory, baseFile + "." + extensions[i]);
			
			allFiles[extensions.length] = primaryFile;
			
			is = new FileInputStream(IOUtils.zip(allFiles));
		}
		else {
			is = new FileInputStream(primaryFile);
		}
		
		this.dataStream = is;
		
	}
	
	public String writeData (File workspaceDir){
		
		String fileName = null;
		if (GenericFileDataConstants.getIncludeFilesByMimeType(this.mimeType) != null){
			try {
				fileName = this.unzipData(this.dataStream, this.fileExtension, workspaceDir);
			} catch (IOException e) {
				LOGGER.error("Could not unzip the archive to " + workspaceDir);
				e.printStackTrace();
			}
		}
		else {
			try {
				fileName = this.justWriteData(this.dataStream, this.fileExtension, workspaceDir);
			} catch (IOException e) {
				LOGGER.error("Could not write the input to " + workspaceDir);
				e.printStackTrace();
			}
		}
		
		return fileName;
	}
	
	
	private String unzipData (InputStream is, String extension, File writeDirectory) throws IOException {
		int bufferLength = 2048;
		byte buffer[] = new byte[bufferLength];
		String baseFileName = new Long (System.currentTimeMillis()).toString();
		
		ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(is));
		ZipEntry entry;
		
		String returnFile = null;
		
		while ((entry = zipInputStream.getNextEntry()) != null) {
			
			
			String currentExtension = entry.getName();
			int beginIndex = currentExtension.lastIndexOf(".") + 1;
			currentExtension = currentExtension.substring(beginIndex);
			
			String fileName = baseFileName + "." + currentExtension;
			File currentFile = new File(writeDirectory, fileName);
			currentFile.createNewFile();
			FileOutputStream fos = new FileOutputStream(currentFile);
			BufferedOutputStream bos = new BufferedOutputStream(fos, bufferLength);
			
			int cnt;
			while ((cnt = zipInputStream.read(buffer, 0, bufferLength)) != -1) {
				bos.write(buffer, 0, cnt);
			}
			
			bos.flush();
			bos.close();
			
			if (currentExtension.equalsIgnoreCase(extension)) {
				returnFile = currentFile.getAbsolutePath();
			}
			
			System.gc();
		}
		zipInputStream.close();
		return returnFile;
	}
	
	private String justWriteData (InputStream is, String extension, File writeDirectory) throws IOException {
		
		int bufferLength = 2048;
		byte buffer[] = new byte[bufferLength];
		String fileName = null;
		String baseFileName = new Long (System.currentTimeMillis()).toString();
		
		fileName = baseFileName + "." + extension;
		File currentFile = new File(writeDirectory, fileName);
		currentFile.createNewFile();
		
		//alter FileName for return
		fileName = currentFile.getAbsolutePath();
		
		FileOutputStream fos = new FileOutputStream(currentFile);
		BufferedOutputStream bos = new BufferedOutputStream(fos, bufferLength);
		
		int cnt;
		while ((cnt = is.read(buffer, 0, bufferLength)) != -1) {
			bos.write(buffer, 0, cnt);
		}
		
		bos.flush();
		bos.close();
		
		System.gc();
		
		return fileName;
	}
	
	public GTVectorDataBinding getAsGTVectorDataBinding(){
		String dirName = "tmp" + System.currentTimeMillis();
		File tempDir = null;
		
		if(new File(dirName).mkdir()){
			tempDir = new File(dirName);
		}
		
		LOGGER.info("Writing temp data to: " + tempDir);
		String fileName = writeData(tempDir);
		LOGGER.info("Temp file is: " + fileName);
		File shpFile = new File(fileName);
		
		try {
			DataStore store = new ShapefileDataStore(shpFile.toURI().toURL());
			FeatureCollection features = store.getFeatureSource(store.getTypeNames()[0]).getFeatures();
			System.gc();
			tempDir.delete();
			return new GTVectorDataBinding(features);
		} catch (MalformedURLException e) {
			LOGGER.error("Something went wrong while creating data store.");
			e.printStackTrace();
			throw new RuntimeException("Something went wrong while creating data store.", e);
		} catch (IOException e) {
			LOGGER.error("Something went wrong while converting shapefile to FeatureCollection");
			e.printStackTrace();
			throw new RuntimeException("Something went wrong while converting shapefile to FeatureCollection", e);
		}
	}
	
	private GTRasterDataBinding getAsGTRasterDataBinding(){
		
		//not implemented
		return null;
	}
	
	
	
	
}
