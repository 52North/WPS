package org.n52.wps.io.datahandler.binary;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.axis.encoding.Base64;
import org.apache.commons.io.IOUtils;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.referencing.CRS;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.SchemaRepository;

import org.n52.wps.io.IStreamableGenerator;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.xml.AbstractXMLGenerator;
import org.n52.wps.io.datahandler.xml.GTHelper;
import org.opengis.feature.Feature;
import org.opengis.feature.IllegalAttributeException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Generator to create a zipped shapefile by using GDMS drivers:
 * {@link GeotoolsFeatureCollectionDriver} and {@link ShapefileDriver}
 * 
 * @author victorzinho
 */
public class GTBinZippedSHPGenerator extends AbstractXMLGenerator implements
		IStreamableGenerator {
	/**
	 * @throws IllegalArgumentException
	 *             If the <b>data</b> paramater is not a
	 *             {@link GTVectorDataBinding}
	 * @throws RuntimeException
	 * @see org.n52.wps.io.datahandler.xml.AbstractXMLGenerator#generateXML(org.n52.wps.io.data.IData,
	 *      java.lang.String)
	 */
	@Override
	public Node generateXML(IData data, String schema)
			throws IllegalArgumentException, RuntimeException {
		if (!(data instanceof GTVectorDataBinding)) {
			throw new IllegalArgumentException("Unsupported IData type: "
					+ data.getClass() + ". Expecting: "
					+ GTVectorDataBinding.class);

		}

		try {
			// Create the FeatureCollection data source
			GTVectorDataBinding binding = (GTVectorDataBinding) data;
			FeatureCollection collection = binding.getPayload();

			// Create the text node and return it
			File file = toBaseZippedSHP(collection);
			InputStream is = new FileInputStream(file);
			if (file.length() > Integer.MAX_VALUE) {
				throw new IOException("File is too large to process");
			}
			byte[] bytes = new byte[(int) file.length()];
			is.read(bytes);
			is.close();
			file.delete();
			
			String encoded = Base64.encode(bytes);
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			Document document = factory.newDocumentBuilder().newDocument();
			Text text = document.createTextNode(encoded);
			return text;
		} catch (IOException e) {
			throw new RuntimeException("An error has occurred while zipping "
					+ "and encoding the results", e);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("An error has occurred while "
					+ "generating the document builder for the response", e);
		} catch (IllegalAttributeException e) {
			throw new RuntimeException("An error has occurred while "
					+ "transforming the results into a shapefile", e);
		}
	}

	/**
	 * @throws IllegalArgumentException
	 *             If the <b>data</b> parameter is not a
	 *             {@link GTVectorDataBinding}
	 * @throws RuntimeException
	 *             If an error occurs while copying the underlying file into the
	 *             given {@link OutputStream}
	 * @see org.n52.wps.io.IStreamableGenerator#writeToStream(org.n52.wps.io.data.IData,
	 *      java.io.OutputStream)
	 */
	@Override
	public void writeToStream(IData data, OutputStream os)
			throws IllegalArgumentException, RuntimeException {
		try {
			if (!(data instanceof GTVectorDataBinding)) {
				throw new IllegalArgumentException("Unsupported IData type: "
						+ data.getClass() + ". Expecting: "
						+ GTVectorDataBinding.class);
			}

			GTVectorDataBinding binding = (GTVectorDataBinding) data;
			FeatureCollection originalCollection = binding.getPayload();

			FeatureCollection collection = createCorrectFeatureCollection(originalCollection);
			
			File zippedShpFile =toBaseZippedSHP(collection);
			InputStream inutStream = new FileInputStream(zippedShpFile);
			try {
				IOUtils.copy(inutStream, os);
				inutStream.close();
			
				System.gc();
			} catch (Exception e) {
				
				throw new RuntimeException(e);
			}
		} catch (IOException e) {
			throw new RuntimeException("An error has occurred while "
					+ "transforming the results into a shapefile", e);
		} catch (IllegalAttributeException e) {
			throw new RuntimeException("An error has occurred while "
					+ "transforming the results into a shapefile", e);
		}
	}
	
	private FeatureCollection createCorrectFeatureCollection(
			FeatureCollection fc) {
		
		FeatureCollection resultFeatureCollection = DefaultFeatureCollections.newCollection();
		SimpleFeatureType featureType = null;
		FeatureIterator iterator = fc.features();
		String uuid = UUID.randomUUID().toString();
		int i = 0;
		while(iterator.hasNext()){
			SimpleFeature feature = (SimpleFeature) iterator.next();
		
			if(i==0){
				featureType = GTHelper.createFeatureType(feature.getProperties(), (Geometry)feature.getDefaultGeometry(), uuid, feature.getFeatureType().getCoordinateReferenceSystem());
				QName qname = GTHelper.createGML3SchemaForFeatureType(featureType);
				SchemaRepository.registerSchemaLocation(qname.getNamespaceURI(), qname.getLocalPart());
			}
			Feature resultFeature = GTHelper.createFeature("ID"+i, (Geometry)feature.getDefaultGeometry(), featureType, feature.getProperties());
		
			resultFeatureCollection.add(resultFeature);
			i++;
		}
		return resultFeatureCollection;
		
	}
	

	@Override
	public OutputStream generate(IData coll) {
		LargeBufferStream stream = new LargeBufferStream();
		this.writeToStream(coll, stream);
		return stream;
	}

	@Override
	public Class<?>[] getSupportedInternalInputDataType() {
		return new Class<?>[] { GTVectorDataBinding.class };
	}

	@Override
	public String[] getSupportedSchemas() {
		return new String[] {};
	}

		

	@Override
	public boolean isSupportedSchema(String schema) {
		return schema == null;
	}

	/**
	 * Transforms the given {@link FeatureCollection} into a zipped SHP file
	 * (.shp, .shx, .dbf, .prj) and returs its Base64 encoding
	 * 
	 * @param collection
	 *            the collection to transform
	 * @return the base64 of the zipped shapefile
	 * @throws IOException
	 *             If an error occurs while creating the SHP file or encoding
	 *             the shapefile
	 * @throws IllegalAttributeException
	 *             If an error occurs while writing the features into the the
	 *             shapefile
	 */
	private File toBaseZippedSHP(FeatureCollection collection)
			throws IOException, IllegalAttributeException {
		File shp = File.createTempFile("shp", ".shp");
		DataStoreFactorySpi dataStoreFactory = new ShapefileDataStoreFactory();
		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("url", shp.toURI().toURL());
		params.put("create spatial index", Boolean.TRUE);

		ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory
				.createNewDataStore(params);

		newDataStore.createSchema((SimpleFeatureType) collection.getSchema());
		if(collection.getSchema().getCoordinateReferenceSystem()==null){
			try {
				newDataStore.forceSchemaCRS(CRS.decode("4326"));
			} catch (NoSuchAuthorityCodeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FactoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			newDataStore.forceSchemaCRS(collection.getSchema()
				.getCoordinateReferenceSystem());
		}

		Transaction transaction = new DefaultTransaction("create");

		String typeName = newDataStore.getTypeNames()[0];
		FeatureStore<SimpleFeatureType, SimpleFeature> featureStore = (FeatureStore<SimpleFeatureType, SimpleFeature>) newDataStore
				.getFeatureSource(typeName);
		featureStore.setTransaction(transaction);
		try {
			featureStore.addFeatures(collection);
			transaction.commit();
		} catch (Exception problem) {
			transaction.rollback();
		} finally {
			transaction.close();
		}

		// Zip the shapefile
		String path = shp.getAbsolutePath();
		String baseName = path.substring(0, path.length() - ".shp".length());
		File shx = new File(baseName + ".shx");
		File dbf = new File(baseName + ".dbf");
		File prj = new File(baseName + ".prj");
		File zipped =org.n52.wps.io.IOUtils.zip(shp, shx, dbf, prj);

		
		
		return zipped;
	}
}
