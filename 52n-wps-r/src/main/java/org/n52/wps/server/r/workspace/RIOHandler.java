
package org.n52.wps.server.r.workspace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.n52.wps.io.IOUtils;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.GenericFileDataConstants;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.ILiteralData;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
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

    @SuppressWarnings("unchecked")
    protected static List<Class< ? extends AbstractLiteralDataBinding>> easyLiterals = Arrays.asList(LiteralByteBinding.class,
                                                                                                     LiteralDoubleBinding.class,
                                                                                                     LiteralFloatBinding.class,
                                                                                                     LiteralIntBinding.class,
                                                                                                     LiteralLongBinding.class,
                                                                                                     LiteralShortBinding.class,
                                                                                                     LiteralStringBinding.class);

    private static Logger log = LoggerFactory.getLogger(RIOHandler.class);

    public RIOHandler() {
        log.debug("NEW {}", this);
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
        dataType = RAnnotation.getDataClass(rClass);

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
     */
    public String parseInput(List<IData> input, RConnection connection) throws IOException,
            RserveException,
            REXPMismatchException {

        String result = null;
        // building an R - vector of input entries containing more than one
        // value:
        if (input.size() > 1) {
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
        Class< ? extends IData> iclass = ivalue.getClass();
        if (ivalue instanceof ILiteralData)
            return parseLiteralInput(iclass, ivalue.getPayload());

        if (ivalue instanceof GenericFileDataBinding) {
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

    public String parseLiteralInput(Class< ? extends IData> iClass, Object defaultValue) {
        String result;
        if (defaultValue == null) {
            log.warn("Default value for is null for {} - setting it to 'NA' in R.", iClass);
            return "NA";
        }
        else
            result = "\"" + defaultValue.toString() + "\"";

        if (easyLiterals.contains(iClass)) {
            result = defaultValue.toString();
        }
        else if (iClass.equals(LiteralBooleanBinding.class)) {
            boolean b = Boolean.parseBoolean(defaultValue.toString());
            if (b)
                result = "TRUE";
            else
                result = "FALSE";
        }
        else if (iClass.equals(LiteralStringBinding.class)) {
            result = "\"" + defaultValue.toString() + "\"";
        }
        else {
            String message = "An unsuported IData class occured for input: " + iClass
                    + "it will be interpreted as character value within R";
            log.warn(message);
        }

        return result;
    }

    public IData parseOutput(RConnection connection,
                             String result_id,
                             REXP result,
                             Collection<RAnnotation> annotations,
                             boolean wpsWorkDirIsRWorkDir,
                             String wpsWorkDir) throws IOException,
            REXPMismatchException,
            RserveException,
            RAnnotationException,
            ExceptionReport {
        log.debug("parsing Output with id {} from result {}", result_id, result);

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

        if (iClass.equals(GenericFileDataBinding.class)) {
            log.debug("Creating output with GenericFileDataBinding for file {}", filename);
            String mimeType = "application/unknown";

            File resultFile = new File(filename);
            log.debug("Loading file " + resultFile.getAbsolutePath());

            if ( !resultFile.isAbsolute())
                // relative path names are relative to R work directory
                resultFile = new File(connection.eval("getwd()").asString(), resultFile.getName());

            if (resultFile.exists())
                log.debug("Found file at {}", resultFile);
            else
                log.warn("Result file does not exists at {}", resultFile);

            // Transfer file from R workdir to WPS workdir
            File outputFile = null;
            if (wpsWorkDirIsRWorkDir)
                outputFile = resultFile;
            else
                outputFile = streamFromRserveToWPS(connection, resultFile.getAbsolutePath(), wpsWorkDir);

            if ( !outputFile.exists())
                throw new IOException("Output file does not exists: " + resultFile.getAbsolutePath());

            String rType = currentAnnotation.getStringValue(RAttribute.TYPE);
            mimeType = RDataTypeRegistry.getInstance().getType(rType).getProcessKey();
            GenericFileData out = new GenericFileData(outputFile, mimeType);

            return new GenericFileDataBinding(out);
        }
        else if (iClass.equals(GTVectorDataBinding.class)) {
            String mimeType = "application/unknown";

            RTypeDefinition dataType = currentAnnotation.getRDataType();
            File outputFile;

            if (dataType.equals(RDataType.SHAPE) || dataType.equals(RDataType.SHAPE_ZIP2) && !wpsWorkDirIsRWorkDir) {
                String zip = "";
                REXP ev = connection.eval("zipShp(\"" + filename + "\")");

                // filname = baseName (+ suffix)
                String baseName = null;

                if (filename.endsWith(".shp"))
                    baseName = filename.substring(0, filename.length() - ".shp".length());
                else
                    baseName = filename;

                // zip all -- stream --> unzip all or stream each file?
                if ( !ev.isNull()) {
                    zip = ev.asString();
                    File zipfile = streamFromRserveToWPS(connection, zip, wpsWorkDir);
                    outputFile = IOUtils.unzip(zipfile, "shp").get(0);
                }
                else {
                    log.info("R call to zip() does not work, streaming of shapefile without zipping");
                    String[] dir = connection.eval("dir()").asStrings();
                    for (String f : dir) {
                        if (f.startsWith(baseName) && !f.equals(filename))
                            streamFromRserveToWPS(connection, f, wpsWorkDir);
                    }

                    outputFile = streamFromRserveToWPS(connection, filename, wpsWorkDir);
                }
            }
            else {
                if (wpsWorkDirIsRWorkDir) {
                    outputFile = new File(filename);
                    if ( !outputFile.isAbsolute())
                        // relative path names are alway relative to R work directory
                        outputFile = new File(connection.eval("getwd()").asString(), outputFile.getName());
                }
                else
                    outputFile = streamFromRserveToWPS(connection, filename, wpsWorkDir);
            }

            String rType = currentAnnotation.getStringValue(RAttribute.TYPE);
            mimeType = RDataTypeRegistry.getInstance().getType(rType).getProcessKey();

            GenericFileData gfd = new GenericFileData(outputFile, mimeType);
            GTVectorDataBinding gtvec = gfd.getAsGTVectorDataBinding();
            return gtvec;
        }
        else if (iClass.equals(GTRasterDataBinding.class)) {
            String mimeType = "application/unknown";

            File tempfile = streamFromRserveToWPS(connection, filename, wpsWorkDir);

            String rType = currentAnnotation.getStringValue(RAttribute.TYPE);
            mimeType = RDataTypeRegistry.getInstance().getType(rType).getProcessKey();

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

        // TODO: Might be a risky solution in terms of unknown constructors:
        for (Class< ? > literal : easyLiterals) {
            if (iClass.equals(literal)) {
                Constructor<IData> cons = null;
                try {
                    cons = (Constructor<IData>) iClass.getConstructors()[0];
                    Constructor< ? > param = cons.getParameterTypes()[0].getConstructor(String.class);
                    if (literal.equals(LiteralIntBinding.class)) {
                        // try to force conversion from R-datatype to integer
                        // (important for the R-data type "numeric"):
                        return cons.newInstance(param.newInstance("" + result.asInteger()));
                    }
                    return cons.newInstance(param.newInstance(result.asString()));

                }
                catch (Exception e) {
                    String message = "Error for parsing String to IData for " + result_id + " and class " + iClass
                            + "\n" + e.getMessage();
                    log.error(message, e);
                    throw new RuntimeException(message);
                }
            }
        }

        String message = "R_Proccess: Unsuported Output Data Class declared for id " + result_id + ":" + iClass;
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
