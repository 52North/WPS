/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. It is compliant 
to the WPS version 0.4.0 (OGC 05-007r4). 

 Copyright (C) 2008 by con terra GmbH

 Authors: 
	Bastian Schaeffer, Institute for geoinformatics, University of Muenster, Germany


 Contact: Albert Remke, con terra GmbH, Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 52n@conterra.de

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt); if not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA or visit the web page of the Free
 Software Foundation, http://www.fsf.org.

 ***************************************************************/

package org.n52.wps.io.datahandler.binary;

import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.JAI;

import org.apache.log4j.Logger;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;

public class GeotiffParser  extends AbstractBinaryParser{
	private static String SUPPORTED_FORMAT = "image/tiff";
	private static Logger LOGGER = Logger.getLogger(GeotiffParser.class);
	
	
	public IData parse(InputStream input) {
		
		String fileName = "tempfile"+System.currentTimeMillis();
		File tempFile = new File(fileName);
		try {
			FileOutputStream outputStream = new FileOutputStream(tempFile);
			byte buf[]=new byte[4096];
			int len;
			while((len=input.read(buf))>0){
				outputStream.write(buf,0,len);
			}
			outputStream.close();
			input.close();
		} catch (FileNotFoundException e) {
			System.gc();
			tempFile.delete();
			LOGGER.error(e);
			throw new RuntimeException(e);
		} catch (IOException e1) {
			System.gc();
			tempFile.delete();
			LOGGER.error(e1);
			throw new RuntimeException(e1);
		}
		
		
		Hints hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
		GeoTiffReader reader;
		try {
			reader = new GeoTiffReader(tempFile, hints);
			GridCoverage2D coverage = (GridCoverage2D) reader.read(null);
			
			System.gc();
			tempFile.delete();
			//read(coverage);
			return new GTRasterDataBinding(coverage);
		} catch (DataSourceException e) {
			System.gc();
			tempFile.delete();
			LOGGER.error(e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			System.gc();
			tempFile.delete();
			LOGGER.error(e);
			throw new RuntimeException(e);
		}
	
		
	}
	
	public static void read(GridCoverage2D coverage){
		BigInteger nullValues = new BigInteger("0");
		BigInteger nonNullValues = new BigInteger("0");
		System.out.println(coverage.getCoordinateReferenceSystem());
		RenderedImage readImage = coverage.getRenderedImage();
		BigInteger one = new BigInteger("1");
		
		
		for(int i = 0; i<readImage.getNumXTiles(); i++){
			Raster raster;
			for(int j = 0; j<readImage.getNumYTiles();j++){
				
				raster = readImage.getTile(i, j);
				
				//System.out.println("Tile = " + i + " , "+ j);
		
				for(int x = raster.getMinX();x<raster.getMinX()+raster.getWidth();x++){
					for(int y = raster.getMinY(); y<raster.getMinY()+raster.getHeight();y++){
						
						int value = raster.getSample(x, y, 0);
						if(value != 0){
							System.out.println(value);
							nonNullValues = nonNullValues.add(one);
						}else{
							nullValues = nullValues.add(one);
						}
						
					}
				}
			}
		}
		System.out.println("0 values = " + nullValues);
		System.out.println("Non 0 values = "+ nonNullValues);
		
		
	}

	public static void main(String[] args){
		JAI.getDefaultInstance().getTileCache().setMemoryCapacity(256*1024*1024L);
		Hints hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
		GeoTiffReader reader;
		try {
			File file = new File("C:\\devel\\Programme\\Apache Software Foundation\\Tomcat 5.5\\webapps\\data\\9829CATD1.tif");
			FileInputStream stream = new FileInputStream(file);
			ImageInputStream s = ImageIO.createImageInputStream(stream);
			/*reader = new GeoTiffReader(stream, hints);
			GridCoverage2D coverage = (GridCoverage2D) reader.read(null);*/
			
		} catch (DataSourceException e) {
			LOGGER.error(e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			LOGGER.error(e);
			throw new RuntimeException(e);
		}
	}
	
	public String[] getSupportedFormats() {
		String[] supportedFormats = {SUPPORTED_FORMAT};
		return supportedFormats;
	}

	public boolean isSupportedEncoding(String encoding) {
		return true;
	}
	
	public boolean isSupportedFormat(String format) {
		if(format.equals(SUPPORTED_FORMAT)) {
			return true;
		}
		return false;
	}
	
	public Class[] getSupportedInternalOutputDataType() {
		Class[] supportedClasses = {GTRasterDataBinding.class};
		return supportedClasses;
	
	}

}
