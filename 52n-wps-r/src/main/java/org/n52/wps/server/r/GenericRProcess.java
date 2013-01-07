/**
 * ﻿Copyright (C) 2010
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */

package org.n52.wps.server.r;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.log4j.Logger;
import org.n52.wps.io.IOUtils;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.GenericFileDataConstants;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.ILiteralData;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
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
import org.n52.wps.server.AbstractObservableAlgorithm;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.r.data.RDataType;
import org.n52.wps.server.r.data.RDataTypeRegistry;
import org.n52.wps.server.r.data.RTypeDefinition;
import org.n52.wps.server.r.syntax.RAnnotation;
import org.n52.wps.server.r.syntax.RAnnotationException;
import org.n52.wps.server.r.syntax.RAnnotationType;
import org.n52.wps.server.r.syntax.RAttribute;
import org.n52.wps.server.r.syntax.RegExp;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RFileInputStream;
import org.rosuda.REngine.Rserve.RFileOutputStream;
import org.rosuda.REngine.Rserve.RserveException;

public class GenericRProcess extends AbstractObservableAlgorithm {

    private static Logger LOGGER = Logger.getLogger(GenericRProcess.class);

    // private variables holding process information - initialization in
    // constructor
    private List<RAnnotation> annotations;

    /**
     * Indicates the current workdirectory on the WPS. This is NOT to confuse with the R/Rserve workdirectory
     * used inside the run-method.
     */
    private String currentWPSWorkDir;

    private boolean deleteWorkDirectory = false; // TODO create a task that does
                                                 // this regularly, even if

    // disabled here

    public GenericRProcess(String wellKnownName) {
        super(wellKnownName);

        LOGGER.info("NEW " + this.toString());
    }

    private List<String> errors = new ArrayList<String>();

    private File scriptFile = null;

    private boolean debugScript = true;

    public List<String> getErrors() {
        return errors;
    }

    /**
     * This method should be overwritten, in case you want to have a way of initializing.
     * 
     * In detail it looks for a xml descfile, which is located in the same directory as the implementing class
     * and has the same name as the class, but with the extension XML.
     * 
     * @return
     */
    protected ProcessDescriptionType initializeDescription() {
        LOGGER.info("Initializing description for  " + this.toString());

        // Reading process information from script annotations:
        InputStream rScriptStream = null;
        try {
            String wkn = getWellKnownName();
            LOGGER.debug("Loading file for " + wkn);
            R_Config config = R_Config.getInstance();
            this.scriptFile = config.wknToFile(wkn);
            LOGGER.debug("File loaded: " + this.scriptFile.getAbsolutePath());

            if (this.scriptFile == null) {
                LOGGER.warn("Loaded script file is " + this.scriptFile);
                return null; // FIXME throw exception?
            }
            else {
                rScriptStream = new FileInputStream(this.scriptFile);
                annotations = RAnnotationParser.parseAnnotationsfromScript(rScriptStream);

                // submits annotation with process informations to
                // ProcessdescriptionCreator:
                RProcessDescriptionCreator creator = new RProcessDescriptionCreator();
                ProcessDescriptionType doc = creator.createDescribeProcessType(annotations, getWellKnownName());

                LOGGER.debug("Created process description for " + wkn + ":\n" + doc.xmlText());
                return doc;
            }
        }
        catch (RAnnotationException rae) {
            LOGGER.error(rae.getMessage());
            throw new RuntimeException("Annotation error while parsing process description: " + rae.getMessage());
        }
        catch (IOException ioe) {
            LOGGER.error("I/O error while parsing process description: " + ioe.getMessage());
            throw new RuntimeException("I/O error while parsing process description: " + ioe.getMessage());
        }
        catch (ExceptionReport e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException("Error creating process descriptionn.", e);
        }
        finally {
            try {
                if (rScriptStream != null)
                    rScriptStream.close();
            }
            catch (IOException e) {
                LOGGER.error("Error closing script stream.", e);
            }
        }
    }

    public Map<String, IData> run(Map<String, List<IData>> inputData) throws ExceptionReport {
        LOGGER.info("Running " + this.toString());
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("inputData: " + Arrays.toString(inputData.entrySet().toArray()));

        R_Config config = R_Config.getInstance();
        // create WPS4R workdir (will be deleted later)
        // R workdirectory not same as Wps workdirectory. Instead it shall be a
        // subdirectory of the R default workdirectory otherwise Rserve can't be
        // installed on a separate server
        try {
			this.currentWPSWorkDir = config.createTemporaryWPSWorkDirFullPath();
		} catch (IOException e1) {
			throw new ExceptionReport("Error in creating temporary work directory", ExceptionReport.REMOTE_COMPUTATION_ERROR, e1);
		}
        //File file = new File(this.currentWPSWorkDir);
        //file.mkdirs();
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Temp folder for WPS4R: " + currentWPSWorkDir);

        // retrieve R-Script from path:
        InputStream rScriptStream = null;
        File rScriptFile = null;
        try {
            rScriptFile = config.wknToFile(getWellKnownName());
            rScriptStream = new FileInputStream(rScriptFile);
        }
        catch (IOException e) {
            LOGGER.error("Error reading script file.", e);
            throw new ExceptionReport("Could not read script file " + rScriptFile + " for algorithm "
                    + getWellKnownName(), "Input/Output", e);
        }

        // interaction with R from here:
        RConnection rCon = null;
        // REXP result = null;
        HashMap<String, IData> resulthash = new HashMap<String, IData>();
        try {
            String r_basedir = null;
            try {
                // initializes connection and pre-settings
                rCon = config.openRConnection();

                LOGGER.debug("[R] cleaning session.");
                // ensure that session is clean;
                rCon.eval("rm(list = ls())");
                // rCon.eval(".First()"); // Fehler 127

                // No use of this.currentWorkDir here!
                // R is starting from it's default workdirectory which is
                // changed into a random sub-directory and will be deleted after
                // session completed

                // setting the R working directory relative to default R
                // directory
                String randomFolderName = UUID.randomUUID().toString();
                r_basedir = rCon.eval("getwd()").asString();
                LOGGER.debug("Original getwd(): " + r_basedir);
                rCon.eval("wd = paste(getwd(), \"" + randomFolderName + "\" ,sep=\"/\")");
                rCon.eval("dir.create(wd)");
                REXP result = rCon.eval("setwd(wd)");
                LOGGER.debug("[R] Setting working directory: " + r_basedir);

                LOGGER.debug("Old wd: " + result.asString() + " | New wd: " + rCon.eval("getwd()").asString());

                rCon.eval("cat(paste0(\"[GenericRProcess] work dir: \", getwd()), \"\\n\")");

                loadRUtilityScripts(rCon);

                // Searching for missing inputs to apply standard values:
                List<RAnnotation> inNotations = RAnnotation.filterAnnotations(annotations, RAnnotationType.INPUT);
                LOGGER.debug("inNonations: " + Arrays.toString(inNotations.toArray()));

                // -------------------------------
                // Input value initialization:
                // -------------------------------
                HashMap<String, String> inputValues = new HashMap<String, String>();
                Iterator<Map.Entry<String, List<IData>>> iterator = inputData.entrySet().iterator();

                // parses input values to R-compatible literals and streams
                // input files to workspace
                LOGGER.debug("Parsing input values.");
                while (iterator.hasNext()) {
                    Map.Entry<String, List<IData>> entry = iterator.next();
                    inputValues.put(entry.getKey(), parseInput(entry.getValue(), rCon));
                    RAnnotation current = RAnnotation.filterAnnotations(inNotations,
                                                                        RAttribute.IDENTIFIER,
                                                                        entry.getKey()).get(0);
                    inNotations.remove(current);
                }
                LOGGER.debug("Input: " + Arrays.toString(inNotations.toArray()));

                // parses default values to R-compatible literals:
                for (RAnnotation rAnnotation : inNotations) {
                    String id = rAnnotation.getStringValue(RAttribute.IDENTIFIER);
                    String value = rAnnotation.getStringValue(RAttribute.DEFAULT_VALUE);
                    Class< ? extends IData> iClass = getInputDataType(id);
                    // solution should be suitable for most literal input
                    // values:
                    inputValues.put(id, parseLiteralInput(iClass, value));
                }
                LOGGER.debug("Assigns: " + Arrays.toString(inputValues.entrySet().toArray()));

                // delete help variables and utility functions from workspace:
                LOGGER.debug("[R] clearing utility functions.");
                rCon.eval("rm(list = ls())");

                // assign values to the (clean) workspace:
                LOGGER.debug("[R] assign values.");
                Iterator<Map.Entry<String, String>> inputValuesIterator = inputValues.entrySet().iterator();

                /*
                 * create workspace metadata - TODO this should be done using global variables or an
                 * environment...
                 */
                assignRWPSSessionVariables(rCon);

                while (inputValuesIterator.hasNext()) {
                    Map.Entry<String, String> entry = inputValuesIterator.next();
                    // use eval, not assign (assign only parses strings)
                    String statement = entry.getKey() + " <- " + entry.getValue();
                    LOGGER.debug("[R] " + statement);
                    rCon.eval(statement);
                }

                // save an image that may help debugging R scripts
                if (LOGGER.isDebugEnabled()) {
                    rCon.eval("save.image(file=\"debug.RData\")");
                    LOGGER.debug("Saved image to debug.RData");
                }

                // -------------------------------
                // R script execution:
                // -------------------------------
                boolean success = false;
                success = executeScript(rScriptStream, rCon);

                if ( !success) {
                    rCon.close();
                    String message = "Failure while executing R script. See logs for details";
                    LOGGER.error(message);
                }

                // retrieving result (REXP - Regular Expression Datatype)
                List<RAnnotation> outNotations = RAnnotation.filterAnnotations(annotations, RAnnotationType.OUTPUT);
                for (RAnnotation rAnnotation : outNotations) {
                    String result_id = rAnnotation.getStringValue(RAttribute.IDENTIFIER);
                    result = rCon.eval(result_id);
                    // TODO: change ParseOutput
                    // TODO depending on the generated outputs,
                    // deleteWorkDirectory must be set!
                    resulthash.put(result_id, parseOutput(result_id, result, rCon));
                }

                String sessionInfo = config.getSessionInfo(rCon);
                resulthash.put("sessionInfo", new LiteralStringBinding(sessionInfo));

            }
            catch (IOException e) {
                String message = "Attempt to read R Script file failed:\n" + e.getClass() + " - "
                        + e.getLocalizedMessage() + "\n" + e.getCause();
                LOGGER.error(message, e);
                throw new ExceptionReport(message, e.getClass().getName(), e);
            }
            catch (RAnnotationException e) {
                String message = "R script cannot be executed due to invalid annotations.";
                LOGGER.error(message, e);
                throw new ExceptionReport(message, e.getClass().getName(), e);

            }
            finally {
                if (rCon == null || !rCon.isConnected()) {
                    rCon = config.openRConnection();
                }

                // deletes R workdirectory:
                if (r_basedir != null) {
                    String currentwd = rCon.eval("getwd()").asString();
                    rCon.eval("setwd(\"" + r_basedir + "\")");
                    // should be true usually, if not, workdirectory has been
                    // changed unexpectedly (prob. inside script)
                    if (currentwd != r_basedir)
                        rCon.eval("unlink(\"" + currentwd + "\", recursive=TRUE)");
                    else
                        LOGGER.warn("Unexpected R workdirectory at end of R session, check the R sript for unwanted workdirectory changes");
                }

                LOGGER.debug("[R] cleaning up and closing stream.");
                rCon.eval("rm(list = ls())");
                rCon.close();
                try {
                    rScriptStream.close();
                }
                catch (IOException e) {
                    LOGGER.warn("Connection to R script cannot be closed for process " + getWellKnownName());
                }
            }

        }
        catch (RserveException e) {
            String message = "An R Connection Error occured:\n" + e.getClass() + " - " + e.getLocalizedMessage() + "\n"
                    + e.getCause();
            LOGGER.error(message, e);
            throw new ExceptionReport("Error with the R connection", "R", "R_Connection", e);
        }
        catch (REXPMismatchException e) {
            String message = "An R Parsing Error occoured:\n" + e.getMessage() + e.getClass() + " - "
                    + e.getLocalizedMessage() + "\n" + e.getCause();
            LOGGER.error(message, e);
            throw new ExceptionReport(message, "R", "R_Connection", e);
        }

        // try to delete current local workdir - folder
        if (deleteWorkDirectory) {
            File workdir = new File(this.currentWPSWorkDir);
            boolean deleted = deleteRecursive(workdir);
            if ( !deleted)
                LOGGER.warn("Failed to delete temporary WPS Workdirectory: " + workdir.getAbsolutePath());
        }

        LOGGER.debug("RESULT: " + Arrays.toString(resulthash.entrySet().toArray()));
        return resulthash;
    }

    private void assignRWPSSessionVariables(RConnection rCon) throws RserveException, RAnnotationException {
        R_Config config = R_Config.getInstance();

        // assign link to resource folder to an R variable
        String cmd = RWPSSessionVariables.WPS_SERVER_NAME + " <- TRUE";
        rCon.eval(cmd);
        LOGGER.debug("[R] " + cmd);

        rCon.assign(RWPSSessionVariables.RESOURCE_URL_NAME, config.getResourceDirURL());
        // should have the same result as rCon.eval(resourceUrl <- "lala");
        LOGGER.debug("[R] assigned resource directory to variable \"" + RWPSSessionVariables.RESOURCE_URL_NAME + ":\" "
                + config.getResourceDirURL());

        List<RAnnotation> resAnnotList = RAnnotation.filterAnnotations(annotations, RAnnotationType.RESOURCE);
        String wpsScriptResources = null;
        if (resAnnotList.size() == 1)
            wpsScriptResources = resAnnotList.get(0).getStringValue(RAttribute.NAMED_LIST);
        else
            wpsScriptResources = "list()";
        // evaluations of commands given by strings require "eval"-method
        rCon.eval(RWPSSessionVariables.R_SESSION_SCRIPT_RESOURCES + " = " + wpsScriptResources);

        LOGGER.debug("[R] assigned recource urls to variable \"" + RWPSSessionVariables.R_SESSION_SCRIPT_RESOURCES
                + ":\" " + wpsScriptResources);

        String processDescription = R_Config.getInstance().getUrlPathUpToWebapp()
                + "/WebProcessingService?Request=DescribeProcess&identifier=" + this.getWellKnownName();

        rCon.assign(RWPSSessionVariables.PROCESS_DESCRIPTION, processDescription);
        LOGGER.debug("[R] assigned process description to variable \"" + RWPSSessionVariables.PROCESS_DESCRIPTION
                + ":\" " + processDescription);
    }

    /**
     * @param rCon
     * @throws RserveException
     * @throws IOException
     * @throws FileNotFoundException
     * @throws RAnnotationException
     */
    private void loadRUtilityScripts(RConnection rCon) throws RserveException,
            IOException,
            FileNotFoundException,
            RAnnotationException {
        LOGGER.debug("[R] loading utility scripts.");
        R_Config config = R_Config.getInstance();
        File[] utils = new File(config.UTILS_DIR_FULL).listFiles(new R_Config.RFileExtensionFilter());
        for (File file : utils) {
            executeScript(new FileInputStream(file), rCon);
        }
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
    private String parseInput(List<IData> input, RConnection rCon) throws IOException,
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
                result += parseInput(input.subList(i, i + 1), rCon);
                result += ", ";
            }
            // parsing last element separately to vecor:
            result += parseInput(input.subList(input.size() - 1, input.size()), rCon);
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
            result = streamFromWPSToRserve(rCon, is, ext);
            return result;
        }

        if (ivalue instanceof GTRasterDataBinding) {
            GeotiffGenerator tiffGen = new GeotiffGenerator();
            InputStream is = tiffGen.generateStream(ivalue, GenericFileDataConstants.MIME_TYPE_GEOTIFF, "base64");
            // String ext = value.getFileExtension();
            result = streamFromWPSToRserve(rCon, is, "tiff");
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
            result = streamFromWPSToRserve(rCon, is, ext);
            return result;
        }

        // if nothing was supported:
        String message = "An unsuported IData Class occured for input: " + input.get(0).getClass();
        LOGGER.error(message);
        throw new RuntimeException(message);

    }

    /**
     * Streams a file from WPS to Rserve workdirectory
     * 
     * @param rCon
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
    private String streamFromWPSToRserve(RConnection rCon, InputStream is, String ext) throws IOException,
            REXPMismatchException,
            RserveException {
        String result;
        String randomname = UUID.randomUUID().toString();
        String inputFileName = randomname;

        RFileOutputStream rfos = rCon.createFile(inputFileName);

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
        result = rCon.eval("unzipRename(" + "\"" + inputFileName + "\", " + "\"" + randomname + "\", " + "\"" + ext
                + "\")").asString();
        result = "\"" + result + "\"";
        return result;
    }

    @SuppressWarnings("unchecked")
    private String parseLiteralInput(Class< ? extends IData> iClass, Object value) {
        String result = null;

        List<Class< ? extends ILiteralData>> easyLiterals = Arrays.asList(LiteralByteBinding.class,
                                                                          LiteralDoubleBinding.class,
                                                                          LiteralFloatBinding.class,
                                                                          LiteralIntBinding.class,
                                                                          LiteralLongBinding.class,
                                                                          LiteralShortBinding.class);

        if (easyLiterals.contains(iClass)) {
            result = "" + value;
        }
        else if (iClass.equals(LiteralBooleanBinding.class)) {
            if ((Boolean) value)
                result = "TRUE";
            else
                result = "FALSE";
        }
        else {
            if ( !iClass.equals(LiteralStringBinding.class)) {
                String message = "An unsuported IData class occured for input: " + iClass
                        + "it will be interpreted as character value within R";
                LOGGER.warn(message);
            }

            result = "\"" + value + "\"";
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private IData parseOutput(String result_id, REXP result, RConnection rCon) throws IOException,
            REXPMismatchException,
            RserveException,
            RAnnotationException {
        Class< ? extends IData> iClass = getOutputDataType(result_id);

        if (iClass.equals(GenericFileDataBinding.class)) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Creating output with GenericFileDataBinding");
            String mimeType = "application/unknown";

            // extract filename from R
            File dest = new File(result.asString());

            String filename;
            if (dest.isAbsolute())
                filename = dest.getAbsolutePath();
            else
                filename = dest.getName();

            File tempfile = streamFromRserveToWPS(rCon, filename);

            // extract mimetype from annotations (TODO: might have to be
            // simplified somewhen)
            List<RAnnotation> list = RAnnotation.filterAnnotations(annotations,
                                                                   RAnnotationType.OUTPUT,
                                                                   RAttribute.IDENTIFIER,
                                                                   result_id);

            RAnnotation anot = list.get(0);
            String rType = anot.getStringValue(RAttribute.TYPE);
            mimeType = RDataTypeRegistry.getInstance().getType(rType).getProcessKey();
            GenericFileData out = new GenericFileData(tempfile, mimeType);

            return new GenericFileDataBinding(out);
        }
        else if (iClass.equals(GTVectorDataBinding.class)) {

            String mimeType = "application/unknown";

            // extract filename from R
            String filename = new File(result.asString()).getName();

            RAnnotation out = RAnnotation.filterAnnotations(annotations, RAnnotationType.OUTPUT).get(0);
            RTypeDefinition dataType = out.getRDataType();
            File tempfile;

            if (dataType.equals(RDataType.SHAPE) || dataType.equals(RDataType.SHAPE_ZIP2)) {
                loadRUtilityScripts(rCon);
                String zip = "";
                REXP ev = rCon.eval("zipShp(\"" + filename + "\")");

                // filname = baseName (+ suffix)
                String baseName = null;

                if (filename.endsWith(".shp"))
                    baseName = filename.substring(0, filename.length() - ".shp".length());
                else
                    baseName = filename;

                // zip all -- stream --> unzip all or stream each file?
                if ( !ev.isNull()) {
                    zip = ev.asString();
                    File zipfile = streamFromRserveToWPS(rCon, zip);
                    tempfile = IOUtils.unzip(zipfile, "shp").get(0);
                }
                else {
                    LOGGER.info("R call to zip() does not work, streaming of shapefile without zipping");
                    String[] dir = rCon.eval("dir()").asStrings();
                    for (String f : dir) {
                        if (f.startsWith(baseName) && !f.equals(filename))
                            streamFromRserveToWPS(rCon, f);
                    }

                    tempfile = streamFromRserveToWPS(rCon, filename);
                }
            }
            else {
                // All (single) files which are not Shapefiles
                tempfile = streamFromRserveToWPS(rCon, filename);
            }
            // extract mimetype from annotations (TODO: might have to be
            // simplified somewhen)
            List<RAnnotation> list = RAnnotation.filterAnnotations(annotations,
                                                                   RAnnotationType.OUTPUT,
                                                                   RAttribute.IDENTIFIER,
                                                                   result_id);

            RAnnotation anot = list.get(0);
            String rType = anot.getStringValue(RAttribute.TYPE);
            mimeType = RDataTypeRegistry.getInstance().getType(rType).getProcessKey();

            GenericFileData gfd = new GenericFileData(tempfile, mimeType);
            GTVectorDataBinding gtvec = gfd.getAsGTVectorDataBinding();
            return gtvec;
        }
        else if (iClass.equals(GTRasterDataBinding.class)) {
            String mimeType = "application/unknown";

            // extract filename from R
            String filename = new File(result.asString()).getName();
            File tempfile = streamFromRserveToWPS(rCon, filename);

            // extract mimetype from annotations (TODO: might have to be
            // simplified somewhen)
            List<RAnnotation> list = RAnnotation.filterAnnotations(annotations,
                                                                   RAnnotationType.OUTPUT,
                                                                   RAttribute.IDENTIFIER,
                                                                   result_id);

            RAnnotation anot = list.get(0);
            String rType = anot.getStringValue(RAttribute.TYPE);
            mimeType = RDataTypeRegistry.getInstance().getType(rType).getProcessKey();
            GeotiffParser tiffPar = new GeotiffParser();
            GTRasterDataBinding output = tiffPar.parse(new FileInputStream(tempfile), mimeType, "base64");
            return output;
        }
        else if (iClass.equals(LiteralBooleanBinding.class)) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Creating output with LiteralBooleanBinding");

            int tresult = result.asInteger();
            switch (tresult) {
            case 1:
                return new LiteralBooleanBinding(true);
            case 0:
                return new LiteralBooleanBinding(false);
            }
        }
        else if (iClass.equals(RWorkdirUrlBinding.class)) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Creating output with WorkdirUrlBinding");

            String filename = new File(result.asString()).getName();
            File tempfile = streamFromRserveToWPS(rCon, filename);
            filename = tempfile.getName();

            return new RWorkdirUrlBinding(this.currentWPSWorkDir, filename);
        }

        Class[] easyLiterals = new Class[] {LiteralByteBinding.class,
                                            LiteralDoubleBinding.class,
                                            LiteralFloatBinding.class,
                                            LiteralIntBinding.class,
                                            LiteralLongBinding.class,
                                            LiteralShortBinding.class,
                                            LiteralStringBinding.class};

        // TODO: Might be a risky solution in terms of unknown constructors:
        for (Class< ? > literal : easyLiterals) {
            if (iClass.equals(literal)) {
                Constructor<IData> cons = null;
                try {
                    cons = (Constructor<IData>) iClass.getConstructors()[0];
                    Constructor< ? > param = cons.getParameterTypes()[0].getConstructor(String.class);
                    return cons.newInstance(param.newInstance(result.asString()));

                }
                catch (Exception e) {
                    String message = "Error for parsing String to IData for " + result_id + " and class " + iClass
                            + "\n" + e.getMessage();
                    LOGGER.error(message, e);
                    throw new RuntimeException(message);
                }
            }
        }
        String message = "R_Proccess: Unsuported Output Data Class declared for id " + result_id + ":" + iClass;
        LOGGER.error(message);
        throw new RuntimeException(message);
    }

    /**
     * Streams a File from R workdirectory to a temporal file in the WPS4R workdirectory
     * (R.Config.WORK_DIR/random folder)
     * 
     * @param rCon
     *        an open R connection
     * @param filename
     *        name or path of the file located in the R workdirectory
     * @return Location of a file which has been streamed
     * @throws IOException
     * @throws FileNotFoundException
     */
    private File streamFromRserveToWPS(RConnection rCon, String filename) throws IOException, FileNotFoundException {

        File tempfile = new File(filename);
        if (!tempfile.isAbsolute()) {
            // create File to stream from Rserve to WPS4R
            File destination = new File(currentWPSWorkDir);
            if ( !destination.exists())
                destination.mkdirs();
            tempfile = new File(destination, tempfile.getName());
        }

        // Do streaming Rserve --> WPS tempfile
        RFileInputStream fis = rCon.openFile(filename);
        FileOutputStream fos = new FileOutputStream(tempfile);
        byte[] buffer = new byte[2048];
        int stop = fis.read(buffer);
        while (stop != -1) {
            fos.write(buffer, 0, stop);
            stop = fis.read(buffer);
        }
        fis.close();
        fos.close();
        tempfile.deleteOnExit();
        return tempfile;
    }

    /**
     * 
     * @param script
     *        R input script
     * @param rCon
     *        Connection - should be open usually / otherwise it will be opened and closed separately
     * @return true if read was successful
     * @throws RserveException
     * @throws IOException
     * @throws RAnnotationException
     * @throws RuntimeException
     *         if R reports an error
     */
    private boolean executeScript(InputStream script, RConnection rCon) throws RserveException,
            IOException,
            RAnnotationException {
        LOGGER.debug("Executing script...");
        boolean success = true;

        BufferedReader fr = new BufferedReader(new InputStreamReader(script));
        if ( !fr.ready())
            return false;

        // reading script:
        StringBuilder text = new StringBuilder();
        // surrounds R script with try / catch block in R and an initial digit
        // setting
        text.append("error = try({" + '\n' + "options(digits=12)" + '\n');

        // is set true when wps.off-annotations occur
        // this indicates that parts of the script shall not pass to Rserve
        boolean wpsoff_state = false;

        while (fr.ready()) {
            String line = fr.readLine();

            if (line.contains(RegExp.WPS_OFF)) {
                wpsoff_state = true;
            }
            else if (line.contains(RegExp.WPS_ON)) {
                wpsoff_state = false;
            }
            else if (wpsoff_state)
                line = "# (ignored) " + line;

            if (line.contains(RegExp.WPS_OFF) && line.contains(RegExp.WPS_ON))
                // TODO: check in validation
                throw new RAnnotationException("Invalid R-script: Only one wps.on; / wps.off; expression per line!");

            text.append(line + "\n");

        }
        text.append("})" + '\n' + "hasError = class(error) == \"try-error\" " + '\n'
                + "if(hasError) error_message = as.character(error)" + '\n');

        if (debugScript && LOGGER.isDebugEnabled())
            LOGGER.debug(text);

        rCon.eval(text.toString());

        try {
            // handling internal R errors:
            if (rCon.eval("hasError").asInteger() == 1) {
                String message = "An R-error occured while executing R-script: \n"
                        + rCon.eval("error_message").asString();
                LOGGER.error(message);
                success = false;
                throw new RuntimeException(message);
            }

            // retrieving error from Rserve
        }
        catch (REXPMismatchException e) {
            LOGGER.warn("Error handling during R-script execution failed: " + e.getMessage());
            success = false;
        }

        return success;
    }

    /**
     * Deletes File or Directory completely with its content
     * 
     * @param in
     *        File or directory
     * @return true if all content could be deleted
     */
    private boolean deleteRecursive(File in) {
        boolean success = true;
        if ( !in.exists()) {
            return false;
        }
        if (in.isDirectory()) {
            File[] files = in.listFiles();
            for (File file : files) {
                if (file.isFile()) {
                    success = success && file.delete();
                }
                if (file.isDirectory()) {
                    success = success && deleteRecursive(file);
                }
            }
        }
        if (success) {
            success = success && in.delete();
        }
        return success;
    }

    /**
     * Searches annotations (class attribute) for Inputs / Outputs with a specific referring id
     * 
     * @param ioType
     * @param id
     * @return
     * @throws RAnnotationException
     */
    private Class< ? extends IData> getIODataType(RAnnotationType ioType, String id) throws RAnnotationException {
        Class< ? extends IData> dataType = null;
        List<RAnnotation> ioNotations = RAnnotation.filterAnnotations(annotations, ioType, RAttribute.IDENTIFIER, id);
        if (ioNotations.isEmpty()) {
            LOGGER.error("Missing R-script-annotation of type " + ioType.toString().toLowerCase() + " for id \"" + id
                    + "\" ,datatype - class not found");
            return null;
        }
        if (ioNotations.size() > 1) {
            LOGGER.warn("R-script contains more than one annotation of type " + ioType.toString().toLowerCase()
                    + " for id \"" + id + "\n" + " WPS selects the first one.");
        }

        RAnnotation annotation = ioNotations.get(0);
        String rClass = annotation.getStringValue(RAttribute.TYPE);
        dataType = RAnnotation.getDataClass(rClass);

        if (dataType == null) {
            LOGGER.error("R-script-annotation for " + ioType.toString().toLowerCase() + " id \"" + id
                    + "\" contains unsuported data format identifier \"" + rClass + "\"");
        }
        return dataType;
    }

    public Class< ? extends IData> getInputDataType(String id) {
        try {
            return getIODataType(RAnnotationType.INPUT, id);
        }
        catch (RAnnotationException e) {
            String message = "Data type for id " + id + " could not be retrieved, return null";
            LOGGER.error(message, e);
        }
        return null;
    };

    public Class< ? extends IData> getOutputDataType(String id) {
        try {
            return getIODataType(RAnnotationType.OUTPUT, id);
        }
        catch (RAnnotationException e) {
            String message = "Data type for id " + id + " could not be retrieved, return null";
            LOGGER.error(message, e);
        }
        return null;
    };

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GenericRProcess [script = ");
        sb.append(this.scriptFile);
        if (this.annotations != null) {
            sb.append(", annotations = ");
            sb.append(Arrays.toString(this.annotations.toArray()));
        }
        sb.append("]");
        return sb.toString();
    }

}