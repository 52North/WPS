package org.n52.wps.io.datahandler.binary;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.WKTReader2;
import org.geotools.referencing.CRS;
import org.n52.wps.io.IOUtils;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.xml.GTHelper;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

public class GTBinZippedWKT64Parser extends AbstractBinaryParser {
	

	public GTBinZippedWKT64Parser() {
		super();
	}

	/**
	 * @throws RuntimeException
	 *             if an error occurs while writing the stream to disk or
	 *             unzipping the written file
	 * @see org.n52.wps.io.IParser#parse(java.io.InputStream)
	 */
	@Override
	public IData parse(InputStream input, String mimeType) throws RuntimeException {
		try {
			File zipped = IOUtils.writeBase64ToFile(input, "zip");
			List<File> wktFiles = IOUtils.unzip(zipped, "wkt");

			if (wktFiles == null) {
				throw new RuntimeException(
						"Cannot find a shapefile inside the zipped file.");
			}

			//set namespace namespace
			List<Geometry> geometries = new ArrayList<Geometry>();
		
			//read wkt file
			//please not that only 1 geometry is returned. If multiple geometries are included, perhaps use the read(String wktstring) method
			for(int i = 0; i<wktFiles.size();i++){
				File wktFile = wktFiles.get(i);
				Reader fileReader = new FileReader(wktFile);
				
				WKTReader2 wktReader = new WKTReader2();
				com.vividsolutions.jts.geom.Geometry geometry = wktReader.read(fileReader);
				geometries.add(geometry);
			}

			CoordinateReferenceSystem coordinateReferenceSystem = CRS.decode("EPSG:4326");			
			FeatureCollection inpuitFeatureCollection = createFeatureCollection(geometries, coordinateReferenceSystem);
		
			zipped.delete();
			zipped.delete();
			for(int i = 0; i<wktFiles.size();i++){
				File wktFile = wktFiles.get(i);
				wktFile.delete();
			}
			System.gc();
			return new GTVectorDataBinding(inpuitFeatureCollection);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"An error has occurred while accessing provided data", e);
		} catch (ParseException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"An error has occurred while accessing provided data", e);
		
		} catch (NoSuchAuthorityCodeException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"An error has occurred while accessing provided data", e);
		
		} catch (FactoryException e) {
				e.printStackTrace();
				throw new RuntimeException(
						"An error has occurred while accessing provided data", e);
			
		}
	}
	
	private FeatureCollection createFeatureCollection(List<com.vividsolutions.jts.geom.Geometry> geometries, CoordinateReferenceSystem coordinateReferenceSystem){
		
		FeatureCollection collection = FeatureCollections.newCollection();	
		SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
		if(coordinateReferenceSystem==null){
			try {
				coordinateReferenceSystem = CRS.decode("EPSG:4326");
			} catch (NoSuchAuthorityCodeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FactoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			typeBuilder.setCRS(coordinateReferenceSystem);
		}
	
		String namespace = "http://www.opengis.net/gml";
		typeBuilder.setNamespaceURI(namespace);
		Name nameType = new NameImpl(namespace, "Feature");
		typeBuilder.setName(nameType);
		typeBuilder.add("GEOMETRY", geometries.get(0).getClass());
	
		SimpleFeatureType featureType = typeBuilder.buildFeatureType();
	
		for(int i = 0; i<geometries.size();i++){
				Feature feature = GTHelper.createFeature(""+i, geometries.get(i), featureType, new ArrayList());
				collection.add(feature);
		}
		return collection;
	}

	@Override
	public Class[] getSupportedInternalOutputDataType() {
		Class[] classes = new Class[1];
		classes[0] = GTVectorDataBinding.class;
		return classes;
	}

}
