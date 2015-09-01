/**
 * ﻿Copyright (C) 2010 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *       • Apache License, version 2.0
 *       • Apache Software License, version 1.0
 *       • GNU Lesser General Public License, version 3
 *       • Mozilla Public License, versions 1.0, 1.1 and 2.0
 *       • Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */

package org.n52.wps.server.r.workspace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.n52.wps.io.IOUtils;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.GenericFileDataConstants;
import org.n52.wps.io.data.GenericFileDataWithGT;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.ILiteralData;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataWithGTBinding;
import org.n52.wps.io.data.binding.literal.AbstractLiteralDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralByteBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralFloatBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralLongBinding;
import org.n52.wps.io.data.binding.literal.LiteralShortBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.io.datahandler.generator.GeotiffGenerator;
import org.n52.wps.io.datahandler.parser.GeotiffParser;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.r.data.RDataType;
import org.n52.wps.server.r.data.RDataTypeRegistry;
import org.n52.wps.server.r.data.RTypeDefinition;
import org.n52.wps.server.r.syntax.RAnnotation;
import org.n52.wps.server.r.syntax.RAnnotationException;
import org.n52.wps.server.r.syntax.RAnnotationType;
import org.n52.wps.server.r.syntax.RAttribute;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RFileInputStream;
import org.rosuda.REngine.Rserve.RFileOutputStream;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RIOHandler {

    /**
     * these data bindings do not need any pre-procesing or wrapping when loaded into an R session
     */
    protected static List<Class< ? extends AbstractLiteralDataBinding>> simpleInputLiterals = Arrays.asList(LiteralByteBinding.class,
                                                                                                            LiteralDoubleBinding.class,
                                                                                                            LiteralFloatBinding.class,
                                                                                                            LiteralIntBinding.class,
                                                                                                            LiteralLongBinding.class,
                                                                                                            LiteralShortBinding.class);

    protected static List<Class< ? extends AbstractLiteralDataBinding>> simpleOutputLiterals = Arrays.asList(LiteralByteBinding.class,
                                                                                                             LiteralDoubleBinding.class,
                                                                                                             LiteralFloatBinding.class,
                                                                                                             LiteralIntBinding.class,
                                                                                                             LiteralLongBinding.class,
                                                                                                             LiteralShortBinding.class,
                                                                                                             LiteralStringBinding.class);

    public static interface RInputFilter {

        public abstract String filter(String input) throws ExceptionReport;

    }

    public class StringInputFilter implements RInputFilter {

        public String filter(String input) throws ExceptionReport {
            if (input.contains("=") || input.contains("<-"))
                throw new ExceptionReport("Assignment operators found, not allowed, illegal input '" + input + "'",
                                          ExceptionReport.INVALID_PARAMETER_VALUE);

            return input;
        }
    }

    private static Logger log = LoggerFactory.getLogger(RIOHandler.class);

    private RInputFilter filter;

    private RDataTypeRegistry dataTypeRegistry;

    public RIOHandler(RDataTypeRegistry dataTypeRegistry) {
        this.dataTypeRegistry = dataTypeRegistry;

        log.debug("NEW {}", this);

        this.filter = new StringInputFilter();
    }

    public Class< ? extends IData> getInputDataType(String id, Collection<RAnnotation> annotations) {
        try {
            return getIODataType(RAnnotationType.INPUT, id, annotations);
        }
        catch (RAnnotationException e) {
            String message = "Data type for id " + id + " could not be retrieved, return null";
            log.error(message, e);
        }
        return null;
    }

    /**
     * Searches annotations (class attribute) for Inputs / Outputs with a specific referring id
     * 
     * @param ioType
     * @param id
     * @param annotations
     * @return
     * @throws RAnnotationException
     */
    protected Class< ? extends IData> getIODataType(RAnnotationType ioType,
                                                    String id,
                                                    Collection<RAnnotation> annotations) throws RAnnotationException {
        Class< ? extends IData> dataType = null;
        List<RAnnotation> ioNotations = RAnnotation.filterAnnotations(annotations, ioType, RAttribute.IDENTIFIER, id);
        if (ioNotations.isEmpty()) {
            log.error("Missing R-script-annotation of type " + ioType.toString().toLowerCase() + " for id \"" + id
                    + "\" ,datatype - class not found");
            return null;
        }
        if (ioNotations.size() > 1) {
            log.warn("R-script contains more than one annotation of type " + ioType.toString().toLowerCase()
                    + " for id \"" + id + "\n" + " WPS selects the first one.");
        }

        RAnnotation annotation = ioNotations.get(0);
        String rClass = annotation.getStringValue(RAttribute.TYPE);
        dataType = annotation.getDataClass(rClass);

        if (dataType == null) {
            log.error("R-script-annotation for " + ioType.toString().toLowerCase() + " id \"" + id
                    + "\" contains unsuported data format identifier \"" + rClass + "\"");
        }
        return dataType;
    }

    public Class< ? extends IData> getOutputDataType(String id, Collection<RAnnotation> annotations) {
        if (id.equalsIgnoreCase("sessionInfo") || id.equalsIgnoreCase("warnings"))
            return GenericFileDataBinding.class;

        try {
            return getIODataType(RAnnotationType.OUTPUT, id, annotations);
        }
        catch (RAnnotationException e) {
            String message = "Data type for id " + id + " could not be retrieved, return null";
            log.error(message, e);
        }
        return null;
    }

    /**
     * parses iData values to string representations which can be evaluated by Rserve, complex data will be
     * preprocessed and handled here, uses parseLiteralInput for parsing literal Data
     * 
     * @param input
     *        input value as databinding
     * @param Rconnection
     *        (open)
     * @return String which could be evaluated by RConnection.eval(String)
     * @throws IOException
     * @throws RserveException
     * @throws REXPMismatchException
     * @throws ExceptionReport
     */
    public String parseInput(List<IData> input, RConnection connection) throws IOException,
            RserveException,
            REXPMismatchException,
            RAnnotationException,
            ExceptionReport {

        String result = null;
        // building an R - vector of input entries containing more than one
        // value:
        if (input.size() > 1) {
            log.debug("Parsing input vector of length {}", input.size());

            result = "c(";
            // parsing elements 1..n-1 to vector:
            for (int i = 0; i < input.size() - 1; i++) {
                if (input.get(i).equals(null))
                    continue;
                result += parseInput(input.subList(i, i + 1), connection);
                result += ", ";
            }
            // parsing last element separately to vecor:
            result += parseInput(input.subList(input.size() - 1, input.size()), connection);
            result += ")";
        }

        IData ivalue = input.get(0);
        log.debug("Handling input value {} with payload {}", ivalue, ivalue.getPayload());

        Class< ? extends IData> iclass = ivalue.getClass();
        if (ivalue instanceof ILiteralData)
            return parseLiteralInput(iclass, ivalue.getPayload());

        if (ivalue instanceof GenericFileDataWithGTBinding || ivalue instanceof GenericFileDataBinding) {
            GenericFileData value = (GenericFileData) ivalue.getPayload();

            InputStream is = value.getDataStream();
            String ext = value.getFileExtension();
            result = streamFromWPSToRserve(connection, is, ext);
            is.close();

            return result;
        }

        if (ivalue instanceof GTRasterDataBinding) {
            GeotiffGenerator tiffGen = new GeotiffGenerator();
            InputStream is = tiffGen.generateStream(ivalue, GenericFileDataConstants.MIME_TYPE_GEOTIFF, "base64");
            // String ext = value.getFileExtension();
            result = streamFromWPSToRserve(connection, is, "tiff");
            is.close();

            return result;
        }

        if (ivalue instanceof GTVectorDataBinding) {
            GTVectorDataBinding value = (GTVectorDataBinding) ivalue;
            File shp = value.getPayloadAsShpFile();

            String path = shp.getAbsolutePath();
            String baseName = path.substring(0, path.length() - ".shp".length());
            File shx = new File(baseName + ".shx");
            File dbf = new File(baseName + ".dbf");
            File prj = new File(baseName + ".prj");

            File shpZip = IOUtils.zip(shp, shx, dbf, prj);

            InputStream is = new FileInputStream(shpZip);
            String ext = "shp";
            result = streamFromWPSToRserve(connection, is, ext);

            is.close();

            return result;
        }

        // if nothing was supported:
        String message = "An unsuported IData Class occured for input: " + input.get(0).getClass();
        log.error(message);
        throw new RuntimeException(message);
    }

    public String parseLiteralInput(Class< ? extends IData> iClass, Object value) throws ExceptionReport {
        String result = null;

        if (value == null) {
            log.warn("Value for is null for {} - setting it to 'NA' in R.", iClass);
            result = "NA";
        }
        else {
            String valueString = value.toString();
            valueString = this.filter.filter(valueString);

            if (simpleInputLiterals.contains(iClass)) {
                result = valueString;
            }
            else if (iClass.equals(LiteralBooleanBinding.class)) {
                boolean b = Boolean.parseBoolean(valueString);
                if (b)
                    result = "TRUE";
                else
                    result = "FALSE";
            }
            else if (iClass.equals(LiteralStringBinding.class)) {
                result = "\"" + valueString + "\"";
            }
            else {
                log.warn("An unsuported IData class occured for input {} with value {}. It will be interpreted as character value within R",
                         iClass,
                         valueString);
                result = "\"" + valueString + "\"";
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public IData parseOutput(RConnection connection,
                             String result_id,
                             REXP result,
                             Collection<RAnnotation> annotations,
                             RWorkspace workspace) throws IOException,
            REXPMismatchException,
            RserveException,
            RAnnotationException,
            ExceptionReport {
        log.debug("parsing Output with id {} from result {}", result_id, result);

        boolean wpsWorkDirIsRWorkDir = workspace.isWpsWorkDirIsRWorkDir();
        String wpsWorkDir = workspace.getPath();

        if (result == null) {
            log.error("Result for output parsing is NULL for id {}", result_id);
            throw new ExceptionReport("Result for output parsing is NULL for id " + result_id, result_id);
        }

        Class< ? extends IData> iClass = getOutputDataType(result_id, annotations);
        log.debug("Output data type: {}", iClass.toString());

        // extract mimetype from annotations (TODO: might have to be
        // simplified somewhen)
        List<RAnnotation> list = RAnnotation.filterAnnotations(annotations,
                                                               RAnnotationType.OUTPUT,
                                                               RAttribute.IDENTIFIER,
                                                               result_id);
        if (list.size() > 1)
            log.warn("Filtered for annotation by name but got more than one result! Just using the first one of : {}",
                     Arrays.toString(list.toArray()));

        RAnnotation currentAnnotation = list.get(0);
        log.debug("Current annotation: {}", currentAnnotation);
        // extract filename from R

        String filename = new File(result.asString()).getName();

        if (iClass.equals(GenericFileDataBinding.class) || iClass.equals(GenericFileDataWithGTBinding.class)) {
            log.debug("Creating output with GenericFileDataBinding for file {}", filename);

            File resultFile = new File(filename);
            log.debug("Loading file " + resultFile.getAbsolutePath());

            if ( !resultFile.isAbsolute())
                // relative path names are relative to R work directory
                resultFile = new File(connection.eval("getwd()").asString(), resultFile.getName());

            if (resultFile.exists())
                log.debug("Found file at {}", resultFile);
            else
                log.warn("Result file does not exist at {}", resultFile);

            // Transfer file from R workdir to WPS workdir
            File outputFile = null;
            if (wpsWorkDirIsRWorkDir)
                outputFile = resultFile;
            else
                outputFile = streamFromRserveToWPS(connection, resultFile.getAbsolutePath(), wpsWorkDir);

            if ( !outputFile.exists())
                throw new IOException("Output file does not exists: " + resultFile.getAbsolutePath());

            String rType = currentAnnotation.getStringValue(RAttribute.TYPE);
            String mimeType = dataTypeRegistry.getType(rType).getMimeType();
            GenericFileData out = new GenericFileData(outputFile, mimeType);

            return new GenericFileDataBinding(out);
        }
        else if (iClass.equals(GTVectorDataBinding.class)) {
            RTypeDefinition dataType = currentAnnotation.getRDataType();
            File outputFile;

            if (dataType.equals(RDataType.SHAPE) || dataType.equals(RDataType.SHAPE_ZIP2)) {
                if (wpsWorkDirIsRWorkDir) {
                    // vector data binding needs "main" shapefile
                    String shpFileName = filename;
                    if ( !shpFileName.endsWith(".shp"))
                        shpFileName = filename + ".shp";

                    outputFile = new File(shpFileName);
                    if ( !outputFile.isAbsolute())
                        // relative path names are alway relative to R work directory
                        outputFile = new File(connection.eval("getwd()").asString(), outputFile.getName());
                }
                else {
                    // if it is a shapefile and the r workdir is remote, I need to zip, trnasfer, and unzip it
                    String baseName = null;
                    if (filename.endsWith(".shp"))
                        baseName = filename.substring(0, filename.length() - ".shp".length());
                    else
                        baseName = filename;

                    log.debug("Zipping output '{}' as shapefile with base '{}' with R util function: {}",
                              result_id,
                              baseName);
                    REXP ev = connection.eval("zipShp(\"" + baseName + "\")");

                    if ( !ev.isNull()) {
                        String zipfileName = ev.asString();

                        // stream to WPS4R workdir, then the binding needs the files unzipped and the .shp
                        // file as
                        // the "main" file
                        File zipfile = streamFromRserveToWPS(connection, zipfileName, wpsWorkDir);
                        outputFile = IOUtils.unzip(zipfile, "shp").get(0);
                    }
                    else {
                        log.info("R call to zipShp() did not work, streaming of shapefile without zipping");
                        String[] dir = connection.eval("dir()").asStrings();
                        for (String f : dir) {
                            if (f.startsWith(baseName) && !f.equals(filename))
                                streamFromRserveToWPS(connection, f, wpsWorkDir);
                        }

                        outputFile = streamFromRserveToWPS(connection, filename, wpsWorkDir);
                    }
                }
            }
            else {
                if (wpsWorkDirIsRWorkDir) {
                    outputFile = new File(filename);
                    if ( !outputFile.isAbsolute())
                        // relative path names are always relative to R work directory
                        outputFile = new File(connection.eval("getwd()").asString(), outputFile.getName());
                }
                else
                    outputFile = streamFromRserveToWPS(connection, filename, wpsWorkDir);
            }

            if ( !outputFile.exists())
                throw new ExceptionReport("Output file does not exist: " + outputFile,
                                          ExceptionReport.NO_APPLICABLE_CODE);

            String rType = currentAnnotation.getStringValue(RAttribute.TYPE);
            String mimeType = dataTypeRegistry.getType(rType).getMimeType();

            GenericFileDataWithGT gfd = new GenericFileDataWithGT(outputFile, mimeType);
            GTVectorDataBinding gtvec = gfd.getAsGTVectorDataBinding();
            return gtvec;
        }
        else if (iClass.equals(GTRasterDataBinding.class)) {
            File tempfile = streamFromRserveToWPS(connection, filename, wpsWorkDir);

            String rType = currentAnnotation.getStringValue(RAttribute.TYPE);
            String mimeType = dataTypeRegistry.getType(rType).getMimeType();

            GeotiffParser tiffPar = new GeotiffParser();
            FileInputStream fis = new FileInputStream(tempfile);
            GTRasterDataBinding output = tiffPar.parse(fis, mimeType, "base64");
            fis.close();

            return output;
        }
        else if (iClass.equals(LiteralBooleanBinding.class)) {
            log.debug("Creating output with LiteralBooleanBinding");

            int tresult = result.asInteger();
            switch (tresult) {
            case 1:
                return new LiteralBooleanBinding(true);
            case 0:
                return new LiteralBooleanBinding(false);
            default:
                break;
            }
        }

        for (Class< ? > literal : simpleOutputLiterals) {
            if (iClass.equals(literal)) {
                Constructor<IData> cons = null;
                try {
                    cons = (Constructor<IData>) iClass.getConstructors()[0];
                    Constructor< ? > param = cons.getParameterTypes()[0].getConstructor(String.class);
                    if (literal.equals(LiteralIntBinding.class)) {
                        // try to force conversion from R-datatype to integer
                        // (important for the R-data type "numeric"):
                        String intString = Integer.toString(result.asInteger());
                        return cons.newInstance(param.newInstance(intString));
                    }
                    return cons.newInstance(param.newInstance(result.asString()));
                }
                catch (RuntimeException | InstantiationException | IllegalAccessException | InvocationTargetException
                        | NoSuchMethodException e) {
                    String message = "Error for parsing String to IData for " + result_id + " and class " + iClass
                            + "\n" + e.getMessage();
                    log.error(message, e);
                    throw new RuntimeException(message);
                }
            }
        }

        String message = "R_Proccess: Unsuported Output Data Class declared for id '" + result_id + "':" + iClass;
        log.error(message);

        throw new RuntimeException(message);
    }

    /**
     * Streams a File from R workdirectory to a temporal file in the WPS4R workdirectory
     * (R.Config.WORK_DIR/random folder)
     * 
     * @param filename
     *        name or path of the file located in the R workdirectory
     * @param wpsWorkDir
     * @return Location of a file which has been streamed
     * @throws IOException
     * @throws FileNotFoundException
     */
    private File streamFromRserveToWPS(RConnection connection, String filename, String wpsWorkDir) throws IOException,
            FileNotFoundException {
        File tempfile = new File(filename);
        File destination = new File(wpsWorkDir);
        if ( !destination.exists())
            destination.mkdirs();
        tempfile = new File(destination, tempfile.getName());

        // Do streaming Rserve --> WPS tempfile
        RFileInputStream fis = connection.openFile(filename);
        FileOutputStream fos = new FileOutputStream(tempfile);
        byte[] buffer = new byte[2048];
        int stop = fis.read(buffer);
        while (stop != -1) {
            fos.write(buffer, 0, stop);
            stop = fis.read(buffer);
        }
        fis.close();
        fos.close();
        // tempfile.deleteOnExit();
        return tempfile;
    }

    /**
     * Streams a file from WPS to Rserve workdirectory
     * 
     * @param connection
     *        active RConnecion
     * @param is
     *        inputstream of the inputfile
     * @param ext
     *        basefile extension
     * @return
     * @throws IOException
     * @throws REXPMismatchException
     * @throws RserveException
     */
    private String streamFromWPSToRserve(RConnection connection, InputStream is, String ext) throws IOException,
            REXPMismatchException,
            RserveException {
        String result;
        String randomname = UUID.randomUUID().toString();
        String inputFileName = randomname;

        // List<Class< ? extends AbstractLiteralDataBinding>> easyLiterals =
        // Arrays.asList(LiteralByteBinding.class,
        // LiteralDoubleBinding.class,
        // LiteralFloatBinding.class,
        // LiteralIntBinding.class,
        // LiteralLongBinding.class,
        // LiteralShortBinding.class);

        RFileOutputStream rfos = connection.createFile(inputFileName);

        byte[] buffer = new byte[2048];
        int stop = is.read(buffer);

        while (stop != -1) {
            rfos.write(buffer, 0, stop);
            stop = is.read(buffer);
        }
        rfos.flush();
        rfos.close();
        is.close();
        // R unzips archive files and renames files with unique
        // random names
        // TODO: check whether input is a zip archive or not
        result = connection.eval("unzipRename(" + "\"" + inputFileName + "\", " + "\"" + randomname + "\", " + "\""
                + ext + "\")").asString();
        result = "\"" + result + "\"";
        return result;
    }

}
