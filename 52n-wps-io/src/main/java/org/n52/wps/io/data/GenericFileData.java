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
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.IOUtils;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.binary.GeotiffGenerator;
import org.opengis.feature.IllegalAttributeException;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.PropertyType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

public class GenericFileData {

	private static Logger LOGGER = Logger.getLogger(GenericFileData.class);

	public final InputStream dataStream;
	public final String fileExtension;
	public final String mimeType;
	private File primaryFile;

	public GenericFileData(InputStream stream, String mimeType) {
		this.dataStream = stream;
		this.mimeType = mimeType;
		this.fileExtension = GenericFileDataConstants.mimeTypeFileTypeLUT()
				.get(mimeType);
	}

	public GenericFileData(FeatureCollection featureCollection)
			throws IOException {
		this(getShpFile(featureCollection), IOHandler.MIME_TYPE_ZIPPED_SHP);

	}

	public GenericFileData(File primaryTempFile, String mimeType)
			throws IOException {
		primaryFile = primaryTempFile;
		this.mimeType = mimeType;
		this.fileExtension = GenericFileDataConstants.mimeTypeFileTypeLUT()
				.get(mimeType);

		InputStream is = null;

		if (GenericFileDataConstants.getIncludeFilesByMimeType(mimeType) != null) {

			String baseFile = primaryFile.getName();
			baseFile = baseFile.substring(0, baseFile.lastIndexOf("."));
			File temp = new File(primaryFile.getAbsolutePath());
			File directory = new File(temp.getParent());
			String[] extensions = GenericFileDataConstants
					.getIncludeFilesByMimeType(mimeType);

			File[] allFiles = new File[extensions.length + 1];

			for (int i = 0; i < extensions.length; i++)
				allFiles[i] = new File(directory, baseFile + "."
						+ extensions[i]);

			allFiles[extensions.length] = primaryFile;

			is = new FileInputStream(IOUtils.zip(allFiles));
		} else {
			is = new FileInputStream(primaryFile);
		}

		this.dataStream = is;

	}

	public GenericFileData(GridCoverage2D payload, String mimeType) {
		GeotiffGenerator generator = new GeotiffGenerator();
		primaryFile = generator.generateFile(new GTRasterDataBinding(payload),
				mimeType);
		dataStream = null;
		fileExtension = "tiff";
		this.mimeType = mimeType;
	}

	public static File getShpFile(FeatureCollection collection)
			throws IOException, IllegalAttributeException {
		SimpleFeatureType type = null;
		SimpleFeatureBuilder build = null;
		FeatureIterator iterator = collection.features();
		FeatureCollection modifiedFeatureCollection = null;
		Transaction transaction = new DefaultTransaction("create");
		FeatureStore<SimpleFeatureType, SimpleFeature> store = null;
		String uuid = UUID.randomUUID().toString();
		String tmpDirPath = System.getProperty("java.io.tmpdir");
		File shp = new File(tmpDirPath + File.separator + "Shape_" + uuid + ".shp");
		while (iterator.hasNext()) {
			SimpleFeature sf = (SimpleFeature) iterator.next();
			// create SimpleFeatureType
			if (type == null) {
				SimpleFeatureType inType = (SimpleFeatureType) collection
						.getSchema();
				SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
				builder.setName(inType.getName());
				builder.setNamespaceURI(inType.getName().getNamespaceURI());

				if (collection.getSchema().getCoordinateReferenceSystem() == null) {
					builder.setCRS(DefaultGeographicCRS.WGS84);
				} else {
					builder.setCRS(collection.getSchema()
							.getCoordinateReferenceSystem());
				}

				builder.setDefaultGeometry(sf.getDefaultGeometryProperty()
						.getName().getLocalPart());

				for (Property prop : sf.getProperties()) {
					if (isSupportedShapefileType(prop.getType())
							&& (prop.getValue() != null)) {
						builder.add(prop.getName().getLocalPart(), prop
								.getType().getBinding());
					}
				}

				type = builder.buildFeatureType();

				ShapefileDataStore dataStore = new ShapefileDataStore(shp
						.toURI().toURL());
				dataStore.createSchema(type);
				dataStore.forceSchemaCRS(type.getCoordinateReferenceSystem());

				String typeName = dataStore.getTypeNames()[0];
				store = (FeatureStore<SimpleFeatureType, SimpleFeature>) dataStore
						.getFeatureSource(typeName);

				store.setTransaction(transaction);

				build = new SimpleFeatureBuilder(type);
				modifiedFeatureCollection = new DefaultFeatureCollection("fc",
						type);
			}
			for (AttributeType attributeType : type.getTypes()) {
				build.add(sf.getProperty(attributeType.getName()).getValue());
				// System.out.println("value: "+attributeType.getName()+" : "+sf.getProperty(attributeType.getName()).getValue());
			}

			modifiedFeatureCollection.add(build.buildFeature(sf.getIdentifier()
					.getID()));
		}

		try {
			store.addFeatures(modifiedFeatureCollection);
			transaction.commit();
		} catch (Exception e1) {
			e1.printStackTrace();
			transaction.rollback();
		} finally {
			transaction.close();
		}

		String path = shp.getAbsolutePath();
		String baseName = path.substring(0, path.length() - ".shp".length());
		File shx = new File(baseName + ".shx");
		File dbf = new File(baseName + ".dbf");
		File prj = new File(baseName + ".prj");

		return shp;
	}

	private static boolean isSupportedShapefileType(PropertyType type) {
		String supported[] = { "String", "Integer", "Double", "Boolean",
				"Date", "LineString", "MultiLineString", "Polygon",
				"MultiPolygon", "Point", "MultiPoint" };
		for (String iter : supported) {
			if (type.getBinding().getSimpleName().equalsIgnoreCase(iter)) {
				return true;
			}
		}
		return false;
	}

	public String writeData(File workspaceDir) {

		String fileName = null;
		if (GenericFileDataConstants.getIncludeFilesByMimeType(this.mimeType) != null) {
			try {
				fileName = this.unzipData(this.dataStream, this.fileExtension,
						workspaceDir);
			} catch (IOException e) {
				LOGGER.error("Could not unzip the archive to " + workspaceDir);
				e.printStackTrace();
			}
		} else {
			try {
				fileName = this.justWriteData(this.dataStream,
						this.fileExtension, workspaceDir);
			} catch (IOException e) {
				LOGGER.error("Could not write the input to " + workspaceDir);
				e.printStackTrace();
			}
		}

		return fileName;
	}

	private String unzipData(InputStream is, String extension,
			File writeDirectory) throws IOException {
		int bufferLength = 2048;
		byte buffer[] = new byte[bufferLength];
		String baseFileName = new Long(System.currentTimeMillis()).toString();

		ZipInputStream zipInputStream = new ZipInputStream(
				new BufferedInputStream(is));
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
			BufferedOutputStream bos = new BufferedOutputStream(fos,
					bufferLength);

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

	private String justWriteData(InputStream is, String extension,
			File writeDirectory) throws IOException {

		int bufferLength = 2048;
		byte buffer[] = new byte[bufferLength];
		String fileName = null;
		String baseFileName = new Long(System.currentTimeMillis()).toString();

		fileName = baseFileName + "." + extension;
		File currentFile = new File(writeDirectory, fileName);
		currentFile.createNewFile();

		// alter FileName for return
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

	public GTVectorDataBinding getAsGTVectorDataBinding() {
		String tmpDirPath = System.getProperty("java.io.tmpdir");
		String dirName = tmpDirPath + File.separator + "tmp" + System.currentTimeMillis();
		File tempDir = null;

		if (new File(dirName).mkdir()) {
			tempDir = new File(dirName);
		}

		LOGGER.info("Writing temp data to: " + tempDir);
		String fileName = writeData(tempDir);
		LOGGER.info("Temp file is: " + fileName);
		File shpFile = new File(fileName);

		try {
			DataStore store = new ShapefileDataStore(shpFile.toURI().toURL());
			FeatureCollection features = store.getFeatureSource(
					store.getTypeNames()[0]).getFeatures();
			System.gc();
			tempDir.delete();
			return new GTVectorDataBinding(features);
		} catch (MalformedURLException e) {
			LOGGER.error("Something went wrong while creating data store.");
			e.printStackTrace();
			throw new RuntimeException(
					"Something went wrong while creating data store.", e);
		} catch (IOException e) {
			LOGGER.error("Something went wrong while converting shapefile to FeatureCollection");
			e.printStackTrace();
			throw new RuntimeException(
					"Something went wrong while converting shapefile to FeatureCollection",
					e);
		}
	}

	private GTRasterDataBinding getAsGTRasterDataBinding() {

		// not implemented
		return null;
	}

	public File getBaseFile() {
		return primaryFile;
	}

}
