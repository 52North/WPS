INSERT INTO users VALUES(DEFAULT, 'wps', 'f0e272b0741e2a259fc10111a65445608294fdbf894a11ca1652bbfdef121ffbc7aecfa0b475a743', 'ROLE_ADMIN');
INSERT INTO ALGORITHMENTRY VALUES('org.n52.wps.server.algorithm.JTSConvexHullAlgorithm','org.n52.wps.server.modules.LocalAlgorithmRepositoryCM',TRUE);
INSERT INTO ALGORITHMENTRY VALUES('org.n52.wps.server.algorithm.test.DummyTestClass','org.n52.wps.server.modules.LocalAlgorithmRepositoryCM',TRUE);
INSERT INTO ALGORITHMENTRY VALUES('org.n52.wps.server.algorithm.test.LongRunningDummyTestClass','org.n52.wps.server.modules.LocalAlgorithmRepositoryCM',TRUE);
INSERT INTO ALGORITHMENTRY VALUES('org.n52.wps.server.algorithm.test.MultipleComplexInAndOutputsDummyTestClass','org.n52.wps.server.modules.LocalAlgorithmRepositoryCM',TRUE);
INSERT INTO ALGORITHMENTRY VALUES('org.n52.wps.server.algorithm.test.MultiReferenceInputAlgorithm','org.n52.wps.server.modules.LocalAlgorithmRepositoryCM',TRUE);
INSERT INTO ALGORITHMENTRY VALUES('org.n52.wps.server.algorithm.test.MultiReferenceBinaryInputAlgorithm','org.n52.wps.server.modules.LocalAlgorithmRepositoryCM',TRUE);
INSERT INTO ALGORITHMENTRY VALUES('org.n52.wps.server.algorithm.test.EchoProcess','org.n52.wps.server.modules.LocalAlgorithmRepositoryCM',TRUE);

INSERT INTO CONFIGURATIONMODULE VALUES('org.n52.wps.io.modules.generator.GeoserverWMSGeneratorCM', FALSE);
INSERT INTO CONFIGURATIONMODULE VALUES('org.n52.wps.io.modules.generator.GeoserverWFSGeneratorCM', FALSE);
INSERT INTO CONFIGURATIONMODULE VALUES('org.n52.wps.io.modules.generator.GeoserverWCSGeneratorCM', FALSE);
INSERT INTO CONFIGURATIONMODULE VALUES('org.n52.wps.io.modules.generator.MapserverWMSGeneratorCM', FALSE);

INSERT INTO FORMATENTRY VALUES('application/x-zipped-shp', '', '', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/img', '', '', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('image/tiff', '', '', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/geotiff', '', '', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/dbase', '', '', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/remap', '', '', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-erdas-hfa', '', '', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-netcdf', '', '', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/dgn', '', '', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('image/jpeg', '', '', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('image/png', '', '', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-geotiff', '', '', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/hdf4-eos', '', '', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('text/plain', '', '', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/rData', '', '', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/rData+Spatial', '', '', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/rData+SpatialPoints', '', '', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/rData+SpatialPolygons', '', '', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/rData', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/rData+Spatial', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/rData+SpatialPoints', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/rData+SpatialPolygons', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-zipped-shp', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/img', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('image/tiff', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/geotiff', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/dbase', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/remap', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-erdas-hfa', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-netcdf', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/dgn', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('image/jpeg', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('image/png', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-geotiff', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/hdf4-eos', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('text/plain', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileParserCM',TRUE);

INSERT INTO FORMATENTRY VALUES('application/x-zipped-shp', '', '', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/img', '', '', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('image/tiff', '', '', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/geotiff', '', '', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/dbase', '', '', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/remap', '', '', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-erdas-hfa', '', '', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-netcdf', '', '', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/dgn', '', '', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('image/jpeg', '', '', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('image/png', '', '', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-geotiff', '', '', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/hdf4-eos', '', '', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('text/plain', '', '', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/rData', '', '', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/rData+Spatial', '', '', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/rData+SpatialPoints', '', '', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/rData+SpatialPolygons', '', '', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/rData', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/rData+Spatial', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/rData+SpatialPoints', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/rData+SpatialPolygons', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-zipped-shp', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/img', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('image/tiff', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/geotiff', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/dbase', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/remap', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-erdas-hfa', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-netcdf', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/dgn', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('image/jpeg', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('image/png', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-geotiff', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/hdf4-eos', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('text/plain', '', 'base64', 'org.n52.wps.io.modules.parser.GenericFileDataWithGTParserCM',TRUE);

INSERT INTO FORMATENTRY VALUES('application/x-zipped-shp', '', '', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/img', '', '', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('image/tiff', '', '', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/geotiff', '', '', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/dbase', '', '', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/remap', '', '', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-erdas-hfa', '', '', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-netcdf', '', '', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/dgn', '', '', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('image/jpeg', '', '', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('image/png', '', '', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-geotiff', '', '', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/hdf4-eos', '', '', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('text/plain', '', '', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/rData', '', '', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/rData+Spatial', '', '', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/rData+SpatialPoints', '', '', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/rData+SpatialPolygons', '', '', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/rData', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/rData+Spatial', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/rData+SpatialPoints', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/rData+SpatialPolygons', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-zipped-shp', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/img', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('image/tiff', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/geotiff', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/dbase', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/remap', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-erdas-hfa', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-netcdf', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/dgn', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('image/jpeg', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('image/png', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-geotiff', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/hdf4-eos', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('text/plain', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileGeneratorCM',TRUE);

INSERT INTO FORMATENTRY VALUES('application/x-zipped-shp', '', '', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/img', '', '', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('image/tiff', '', '', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/geotiff', '', '', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/dbase', '', '', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/remap', '', '', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-erdas-hfa', '', '', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-netcdf', '', '', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/dgn', '', '', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('image/jpeg', '', '', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('image/png', '', '', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-geotiff', '', '', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/hdf4-eos', '', '', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('text/plain', '', '', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/rData', '', '', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/rData+Spatial', '', '', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/rData+SpatialPoints', '', '', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/rData+SpatialPolygons', '', '', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/rData', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/rData+Spatial', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/rData+SpatialPoints', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/rData+SpatialPolygons', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-zipped-shp', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/img', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('image/tiff', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/geotiff', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/dbase', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/remap', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-erdas-hfa', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-netcdf', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/dgn', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('image/jpeg', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('image/png', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-geotiff', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/hdf4-eos', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('text/plain', '', 'base64', 'org.n52.wps.io.modules.generator.GenericFileDataWithGTGeneratorCM',TRUE);

INSERT INTO FORMATENTRY VALUES('application/wkt', '', '', 'org.n52.wps.io.modules.parser.WKTParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/wkt', '', '', 'org.n52.wps.io.modules.generator.WKTGeneratorCM',TRUE);

INSERT INTO FORMATENTRY VALUES('application/vnd.geo+json', '', '', 'org.n52.wps.io.modules.parser.GeoJSONParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('application/vnd.geo+json', '', '', 'org.n52.wps.io.modules.generator.GeoJSONGeneratorCM',TRUE);

INSERT INTO FORMATENTRY VALUES('text/plain', 'http://schemas.opengis.net/wcps/1.0/wcpsAll.xsd', '', 'org.n52.wps.io.modules.parser.WCPSQueryParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('text/plain', 'http://schemas.opengis.net/wcps/1.0/wcpsAll.xsd', '', 'org.n52.wps.io.modules.generator.WCPSGeneratorCM',TRUE);

INSERT INTO FORMATENTRY VALUES('text/xml', '', '', 'org.n52.wps.io.modules.parser.GenericXMLDataParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/2.1.2', 'http://schemas.opengis.net/gml/2.1.2/feature.xsd', '', 'org.n52.wps.io.modules.parser.GenericXMLDataParserCM',TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', '', '', 'org.n52.wps.io.modules.generator.GenericXMLDataGeneratorCM',TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/2.1.2', 'http://schemas.opengis.net/gml/2.1.2/feature.xsd', '', 'org.n52.wps.io.modules.generator.GenericXMLDataGeneratorCM',TRUE);

INSERT INTO FORMATENTRY VALUES('application/img', '', 'base64', 'org.n52.wps.io.modules.parser.GenericRasterFileParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('image/tiff', '', 'base64', 'org.n52.wps.io.modules.parser.GenericRasterFileParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('image/geotiff', '', 'base64', 'org.n52.wps.io.modules.parser.GenericRasterFileParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('application/geotiff', '', 'base64', 'org.n52.wps.io.modules.parser.GenericRasterFileParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('application/dbase', '', 'base64', 'org.n52.wps.io.modules.parser.GenericRasterFileParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('application/remap', '', 'base64', 'org.n52.wps.io.modules.parser.GenericRasterFileParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-erdas-hfa', '', 'base64', 'org.n52.wps.io.modules.parser.GenericRasterFileParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-netcdf', '', 'base64', 'org.n52.wps.io.modules.parser.GenericRasterFileParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('application/dgn', '', 'base64', 'org.n52.wps.io.modules.parser.GenericRasterFileParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('image/jpeg', '', 'base64', 'org.n52.wps.io.modules.parser.GenericRasterFileParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('image/png', '', 'base64', 'org.n52.wps.io.modules.parser.GenericRasterFileParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-geotiff', '', 'base64', 'org.n52.wps.io.modules.parser.GenericRasterFileParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('application/hdf4-eos', '', 'base64', 'org.n52.wps.io.modules.parser.GenericRasterFileParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/plain', '', 'base64', 'org.n52.wps.io.modules.parser.GenericRasterFileParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('application/img', '', '', 'org.n52.wps.io.modules.parser.GenericRasterFileParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('image/tiff', '', '', 'org.n52.wps.io.modules.parser.GenericRasterFileParserCM', TRUE); 
INSERT INTO FORMATENTRY VALUES('image/geotiff', '', '', 'org.n52.wps.io.modules.parser.GenericRasterFileParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('application/geotiff', '', '', 'org.n52.wps.io.modules.parser.GenericRasterFileParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('application/dbase', '', '', 'org.n52.wps.io.modules.parser.GenericRasterFileParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('application/remap', '', '', 'org.n52.wps.io.modules.parser.GenericRasterFileParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-erdas-hfa', '', '', 'org.n52.wps.io.modules.parser.GenericRasterFileParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-netcdf', '', '', 'org.n52.wps.io.modules.parser.GenericRasterFileParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('application/dgn', '', '', 'org.n52.wps.io.modules.parser.GenericRasterFileParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('image/jpeg', '', '', 'org.n52.wps.io.modules.parser.GenericRasterFileParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('image/png', '', '', 'org.n52.wps.io.modules.parser.GenericRasterFileParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-geotiff', '', '', 'org.n52.wps.io.modules.parser.GenericRasterFileParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('application/hdf4-eos', '', '', 'org.n52.wps.io.modules.parser.GenericRasterFileParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/plain', '', '', 'org.n52.wps.io.modules.parser.GenericRasterFileParserCM', TRUE);

INSERT INTO FORMATENTRY VALUES('image/tiff', '', 'base64', 'org.n52.wps.io.modules.parser.GeotiffParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('image/geotiff', '', 'base64', 'org.n52.wps.io.modules.parser.GeotiffParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('image/tiff', '', '', 'org.n52.wps.io.modules.parser.GeotiffParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('image/geotiff', '', '', 'org.n52.wps.io.modules.parser.GeotiffParserCM', TRUE);

INSERT INTO FORMATENTRY VALUES('image/x-zipped-tiff', '', 'base64', 'org.n52.wps.io.modules.parser.GeotiffZippedParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('image/x-zipped-geotiff', '', 'base64', 'org.n52.wps.io.modules.parser.GeotiffZippedParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('image/x-zipped-tiff', '', '', 'org.n52.wps.io.modules.parser.GeotiffZippedParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('image/x-zipped-geotiff', '', '', 'org.n52.wps.io.modules.parser.GeotiffZippedParserCM', TRUE);

INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/3.2.1/base/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML3BasicParser4FilesCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/3.1.1/base/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML3BasicParser4FilesCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/3.1.0/base/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML3BasicParser4FilesCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/3.0.1/base/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML3BasicParser4FilesCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/3.0.0/base/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML3BasicParser4FilesCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/3.0.0', 'http://schemas.opengis.net/gml/3.0.0/base/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML3BasicParser4FilesCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/3.0.1', 'http://schemas.opengis.net/gml/3.0.1/base/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML3BasicParser4FilesCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/3.1.0', 'http://schemas.opengis.net/gml/3.1.0/base/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML3BasicParser4FilesCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/3.1.1', 'http://schemas.opengis.net/gml/3.1.1/base/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML3BasicParser4FilesCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/3.2.1', 'http://schemas.opengis.net/gml/3.2.1/base/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML3BasicParser4FilesCM', TRUE);

INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/2.1.2.1/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML2BasicParser4FilesCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/2.1.2/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML2BasicParser4FilesCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/2.1.1/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML2BasicParser4FilesCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/2.0.0/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML2BasicParser4FilesCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/2.0.0', 'http://schemas.opengis.net/gml/2.0.0/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML2BasicParser4FilesCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/2.1.1', 'http://schemas.opengis.net/gml/2.1.1/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML2BasicParser4FilesCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/2.1.2', 'http://schemas.opengis.net/gml/2.1.2/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML2BasicParser4FilesCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/2.1.2.1', 'http://schemas.opengis.net/gml/2.1.2.1/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML2BasicParser4FilesCM', TRUE);

INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/3.1.1/base/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML3BasicParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/3.1.0/base/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML3BasicParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/3.0.1/base/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML3BasicParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/3.0.0/base/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML3BasicParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/3.0.0', 'http://schemas.opengis.net/gml/3.0.0/base/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML3BasicParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/3.0.1', 'http://schemas.opengis.net/gml/3.0.1/base/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML3BasicParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/3.1.0', 'http://schemas.opengis.net/gml/3.1.0/base/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML3BasicParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/3.1.1', 'http://schemas.opengis.net/gml/3.1.1/base/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML3BasicParserCM', TRUE);

INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/3.2.1/base/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML32BasicParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/3.2.1', 'http://schemas.opengis.net/gml/3.2.1/base/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML32BasicParserCM', TRUE);

INSERT INTO FORMATENTRY VALUES('text/xml', 'http://www.opengeospatial.org/gmlpacket.xsd', '', 'org.n52.wps.io.modules.parser.SimpleGMLParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://geoserver.itc.nl:8080/wps/schemas/gml/2.1.2/gmlpacket.xsd', '', 'org.n52.wps.io.modules.parser.SimpleGMLParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/2.1.2/gmlpacket.xsd', '', 'org.n52.wps.io.modules.parser.SimpleGMLParserCM', TRUE);

INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/2.1.2.1/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML2BasicParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/2.1.2/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML2BasicParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/2.1.1/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML2BasicParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/2.0.0/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML2BasicParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/2.0.0', 'http://schemas.opengis.net/gml/2.0.0/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML2BasicParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/2.1.1', 'http://schemas.opengis.net/gml/2.1.1/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML2BasicParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/2.1.2', 'http://schemas.opengis.net/gml/2.1.2/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML2BasicParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/2.1.2.1', 'http://schemas.opengis.net/gml/2.1.2.1/feature.xsd', '', 'org.n52.wps.io.modules.parser.GML2BasicParserCM', TRUE);

INSERT INTO FORMATENTRY VALUES('application/image-ascii-grass', '', '', 'org.n52.wps.io.modules.parser.AsciiGrassParserCM', TRUE);

INSERT INTO FORMATENTRY VALUES('application/x-zipped-shp', '', 'base64', 'org.n52.wps.io.modules.parser.GTBinZippedSHPParserCM', TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-zipped-shp', '', '', 'org.n52.wps.io.modules.parser.GTBinZippedSHPParserCM', TRUE);

INSERT INTO FORMATENTRY VALUES('application/vnd.google-earth.kml+xml', 'http://schemas.opengis.net/kml/2.2.0/ogckml22.xsd', '', 'org.n52.wps.io.modules.parser.GRASSKMLParserCM', TRUE);

INSERT INTO FORMATENTRY VALUES('application/vnd.google-earth.kml+xml', 'http://schemas.opengis.net/kml/2.2.0/ogckml22.xsd', '', 'org.n52.wps.io.modules.parser.KMLParserCM', TRUE);

INSERT INTO FORMATENTRY VALUES('application/x-zipped-wkt', '', 'Base64', 'org.n52.wps.io.modules.parser.GTBinZippedWKT64ParserCM', TRUE);


INSERT INTO FORMATENTRY VALUES('application/x-jsongeometry', '', 'UTF-8', 'org.n52.wps.io.modules.generator.JSONGeometryGeneratorCM', TRUE);

INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/3.2.1/base/feature.xsd', '', 'org.n52.wps.io.modules.generator.GML3BasicGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/3.1.1/base/feature.xsd', '', 'org.n52.wps.io.modules.generator.GML3BasicGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/3.1.0/base/feature.xsd', '', 'org.n52.wps.io.modules.generator.GML3BasicGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/3.0.1/base/feature.xsd', '', 'org.n52.wps.io.modules.generator.GML3BasicGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/3.0.0/base/feature.xsd', '', 'org.n52.wps.io.modules.generator.GML3BasicGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/3.0.0', 'http://schemas.opengis.net/gml/3.0.0/base/feature.xsd', '', 'org.n52.wps.io.modules.generator.GML3BasicGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/3.0.1', 'http://schemas.opengis.net/gml/3.0.1/base/feature.xsd', '', 'org.n52.wps.io.modules.generator.GML3BasicGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/3.1.0', 'http://schemas.opengis.net/gml/3.1.0/base/feature.xsd', '', 'org.n52.wps.io.modules.generator.GML3BasicGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/3.1.1', 'http://schemas.opengis.net/gml/3.1.1/base/feature.xsd', '', 'org.n52.wps.io.modules.generator.GML3BasicGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/3.2.1', 'http://schemas.opengis.net/gml/3.2.1/base/feature.xsd', '', 'org.n52.wps.io.modules.generator.GML3BasicGeneratorCM', TRUE);

INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/2.1.2.1/feature.xsd', '', 'org.n52.wps.io.modules.generator.GML2BasicGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/2.1.2/feature.xsd', '', 'org.n52.wps.io.modules.generator.GML2BasicGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/2.1.1/feature.xsd', '', 'org.n52.wps.io.modules.generator.GML2BasicGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/2.0.0/feature.xsd', '', 'org.n52.wps.io.modules.generator.GML2BasicGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/2.0.0', 'http://schemas.opengis.net/gml/2.0.0/feature.xsd', '', 'org.n52.wps.io.modules.generator.GML2BasicGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/2.1.1', 'http://schemas.opengis.net/gml/2.1.1/feature.xsd', '', 'org.n52.wps.io.modules.generator.GML2BasicGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/2.1.2', 'http://schemas.opengis.net/gml/2.1.2/feature.xsd', '', 'org.n52.wps.io.modules.generator.GML2BasicGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/2.1.2.1', 'http://schemas.opengis.net/gml/2.1.2.1/feature.xsd', '', 'org.n52.wps.io.modules.generator.GML2BasicGeneratorCM', TRUE);

INSERT INTO FORMATENTRY VALUES('text/xml', 'http://www.opengeospatial.org/gmlpacket.xsd', '', 'org.n52.wps.io.modules.generator.SimpleGMLGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://geoserver.itc.nl:8080/wps/schemas/gml/2.1.2/gmlpacket.xsd', '', 'org.n52.wps.io.modules.generator.SimpleGMLGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/2.1.2/gmlpacket.xsd', '', 'org.n52.wps.io.modules.generator.SimpleGMLGeneratorCM', TRUE);

INSERT INTO FORMATENTRY VALUES('application/image-ascii-grass', '', '', 'org.n52.wps.io.modules.generator.AsciiGrassGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('application/image-ascii-grass', '', 'base64', 'org.n52.wps.io.modules.generator.AsciiGrassGeneratorCM', TRUE);

INSERT INTO FORMATENTRY VALUES('image/tiff', '', 'base64', 'org.n52.wps.io.modules.generator.GeotiffGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('image/geotiff', '', 'base64', 'org.n52.wps.io.modules.generator.GeotiffGeneratorCM', TRUE);

INSERT INTO FORMATENTRY VALUES('image/tiff', '', '', 'org.n52.wps.io.modules.generator.GeotiffGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('image/geotiff', '', '', 'org.n52.wps.io.modules.generator.GeotiffGeneratorCM', TRUE);

INSERT INTO FORMATENTRY VALUES('application/x-zipped-shp', '', 'base64', 'org.n52.wps.io.modules.generator.GTBinZippedSHPGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', '', 'base64', 'org.n52.wps.io.modules.generator.GTBinZippedSHPGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('application/x-zipped-shp', '', '', 'org.n52.wps.io.modules.generator.GTBinZippedSHPGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', '', '', 'org.n52.wps.io.modules.generator.GTBinZippedSHPGeneratorCM', TRUE);

INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/3.1.1/base/gml.xsd', '', 'org.n52.wps.io.modules.generator.GRASSXMLGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/2.1.2.1/feature.xsd', '', 'org.n52.wps.io.modules.generator.GRASSXMLGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/2.1.2/feature.xsd', '', 'org.n52.wps.io.modules.generator.GRASSXMLGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/2.1.1/feature.xsd', '', 'org.n52.wps.io.modules.generator.GRASSXMLGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/2.0.0/feature.xsd', '', 'org.n52.wps.io.modules.generator.GRASSXMLGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/2.0.0', 'http://schemas.opengis.net/gml/2.0.0/feature.xsd', '', 'org.n52.wps.io.modules.generator.GRASSXMLGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/2.1.1', 'http://schemas.opengis.net/gml/2.1.1/feature.xsd', '', 'org.n52.wps.io.modules.generator.GRASSXMLGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/2.1.2', 'http://schemas.opengis.net/gml/2.1.2/feature.xsd', '', 'org.n52.wps.io.modules.generator.GRASSXMLGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/2.1.2.1', 'http://schemas.opengis.net/gml/2.1.2.1/feature.xsd', '', 'org.n52.wps.io.modules.generator.GRASSXMLGeneratorCM', TRUE);
INSERT INTO FORMATENTRY VALUES('application/vnd.google-earth.kml+xml', 'http://schemas.opengis.net/kml/2.2.0/ogckml22.xsd', '', 'org.n52.wps.io.modules.generator.GRASSXMLGeneratorCM', TRUE);

INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/3.2.1/base/feature.xsd', '', 'org.n52.wps.io.modules.generator.ProxyGMLGenerator4FilesCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/3.1.1/base/feature.xsd', '', 'org.n52.wps.io.modules.generator.ProxyGMLGenerator4FilesCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/3.1.0/base/feature.xsd', '', 'org.n52.wps.io.modules.generator.ProxyGMLGenerator4FilesCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/3.0.1/base/feature.xsd', '', 'org.n52.wps.io.modules.generator.ProxyGMLGenerator4FilesCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/3.0.0/base/feature.xsd', '', 'org.n52.wps.io.modules.generator.ProxyGMLGenerator4FilesCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://www.opengeospatial.org/gmlpacket.xsd', '', 'org.n52.wps.io.modules.generator.ProxyGMLGenerator4FilesCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://geoserver.itc.nl:8080/wps/schemas/gml/2.1.2/gmlpacket.xsd', '', 'org.n52.wps.io.modules.generator.ProxyGMLGenerator4FilesCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml', 'http://schemas.opengis.net/gml/2.1.2/gmlpacket.xsd', '', 'org.n52.wps.io.modules.generator.ProxyGMLGenerator4FilesCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/3.0.0', 'http://schemas.opengis.net/gml/3.0.0/base/feature.xsd', '', 'org.n52.wps.io.modules.generator.ProxyGMLGenerator4FilesCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/3.0.1', 'http://schemas.opengis.net/gml/3.0.1/base/feature.xsd', '', 'org.n52.wps.io.modules.generator.ProxyGMLGenerator4FilesCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/3.1.0', 'http://schemas.opengis.net/gml/3.1.0/base/feature.xsd', '', 'org.n52.wps.io.modules.generator.ProxyGMLGenerator4FilesCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/3.1.1', 'http://schemas.opengis.net/gml/3.1.1/base/feature.xsd', '', 'org.n52.wps.io.modules.generator.ProxyGMLGenerator4FilesCM', TRUE);
INSERT INTO FORMATENTRY VALUES('text/xml; subtype=gml/3.2.1', 'http://schemas.opengis.net/gml/3.2.1/base/feature.xsd', '', 'org.n52.wps.io.modules.generator.ProxyGMLGenerator4FilesCM', TRUE);

INSERT INTO FORMATENTRY VALUES('application/WMS', '', '', 'org.n52.wps.io.modules.generator.GeoserverWMSGeneratorCM', TRUE);

INSERT INTO FORMATENTRY VALUES('application/WFS', '', '', 'org.n52.wps.io.modules.generator.GeoserverWFSGeneratorCM', TRUE);

INSERT INTO FORMATENTRY VALUES('application/WCS', '', '', 'org.n52.wps.io.modules.generator.GeoserverWCSGeneratorCM', TRUE);

INSERT INTO FORMATENTRY VALUES('application/WMS', '', '', 'org.n52.wps.io.modules.generator.MapserverWMSGeneratorCM', TRUE);

INSERT INTO FORMATENTRY VALUES('application/vnd.google-earth.kml+xml', 'http://schemas.opengis.net/kml/2.2.0/ogckml22.xsd', '', 'org.n52.wps.io.modules.generator.KMLGeneratorCM', TRUE);

DELETE FROM CONFIGURATIONMODULE WHERE MODULE_CLASS_NAME='org.n52.wps.server.database.PostgresDatabaseConfigurationModule';
INSERT INTO CONFIGURATIONMODULE VALUES('org.n52.wps.server.database.PostgresDatabaseConfigurationModule',FALSE);
INSERT INTO CONFIGURATIONMODULE VALUES('org.n52.wps.webapp.entities.Server',TRUE);
INSERT INTO CONFIGURATIONENTRY VALUES('hostport','org.n52.wps.webapp.entities.Server','8080');