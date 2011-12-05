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
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.ILiteralData;
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
import org.n52.wps.server.AbstractObservableAlgorithm;
import org.n52.wps.server.r.RAnnotation.RAnnotationType;
import org.n52.wps.server.r.RAnnotation.RAttribute;
import org.n52.wps.server.r.RAnnotation.RDataType;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RFileInputStream;
import org.rosuda.REngine.Rserve.RFileOutputStream;
import org.rosuda.REngine.Rserve.RserveException;


public class GenericRProcess extends AbstractObservableAlgorithm {
	private static Logger LOGGER = Logger.getLogger(GenericRProcess.class);
	
	//private variables holding process information - initialization in constructor
	private List<RAnnotation> annotations;
	private String WORK_DIR = R_Config.WORK_DIR +"/"+ UUID.randomUUID();
	
	public GenericRProcess(String wellKnownName) {
		super(wellKnownName);
	}


	private List<String> errors = new ArrayList<String>();
	public List<String> getErrors() {
		return errors;
	}
	
	/** 
	 * This method should be overwritten, in case you want to have a way of initializing.
	 * 
	 * In detail it looks for a xml descfile, which is located in the same directory as the implementing class and has the same
	 * name as the class, but with the extension XML.
	 * @return
	 */
	protected ProcessDescriptionType initializeDescription() {
		// Reading Process information from skript annotations:
		InputStream rSkriptStream = null;
		try {
			File rSkriptFile = R_Config.wknToFile(getWellKnownName());
			rSkriptStream = new FileInputStream(rSkriptFile);
			annotations = RAnnotationParser.parseAnnotationsfromSkript(rSkriptStream);
		} catch (IOException e1) {
			LOGGER.error(e1.getMessage());
			throw new RuntimeException("I/O error while parsing process description: "+e1.getMessage());
		}finally{
			try {
				rSkriptStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//	submits annotation with process informations to ProcessdescriptionCreator:
		RProcessDescriptionCreator creator = new RProcessDescriptionCreator();
		ProcessDescriptionType doc = creator.createDescribeProcessType(annotations, getWellKnownName());
		return doc;
	}
	
	public Map<String, IData> run(Map<String, List<IData>> inputData){
		//create WPS4R workdir (will be deleted later)
		new File(WORK_DIR).mkdirs();
		
		//retrieve rSkript from path:
		
		//String processName = getWellKnownName().replace(".", "/");
		//InputStream rSkript = this.getClass().getResourceAsStream("/" + processName + "Skript.xml");
		InputStream rSkriptStream = null;
		try {
			File rSkriptFile = R_Config.wknToFile(getWellKnownName());
			rSkriptStream = new FileInputStream(rSkriptFile);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		//interaction with R from here:
		RConnection rCon = null;
		//REXP result = null;
		HashMap<String,IData> resulthash = new HashMap<String,IData>();
		try{
			String r_basedir = null;
			try {				
				//initializes connection and pre-settings
				rCon = R_Config.openRConnection();
				
				//ensure that session is clean;
				rCon.eval("rm(list = ls())");
				rCon.eval(".First()");
				
				String wd = UUID.randomUUID().toString();
				r_basedir = rCon.eval("getwd()").asString();
				rCon.eval("wd = paste(getwd(), \""+wd+"\" ,sep=\"/\")");
				rCon.eval("dir.create(wd)");
				rCon.eval("setwd(wd)");
		
				loadRUtilityScripts(rCon);
				
				//Searching for missing inputs to apply standard values:
				List<RAnnotation> inNotations = RAnnotation.filterAnnotations(annotations, RAnnotationType.INPUT);
				
				//-------------------------------
				//Input value initialization:
				//-------------------------------
				
				HashMap<String, String> assign = new HashMap<String, String>();
				Iterator<Map.Entry<String, List<IData>>> iterator = inputData.entrySet().iterator();
				
				//parses input values to R-compatible literals and streams input files to workspace
				for (; iterator.hasNext();) {
					Map.Entry<String, List<IData>> entry = iterator.next();
					assign.put(entry.getKey(), parseInput(entry.getValue(), rCon) );	
					RAnnotation current = RAnnotation.filterAnnotations(inNotations, RAttribute.IDENTIFIER, entry.getKey()).get(0);
					inNotations.remove(current);
				}

				//parses default values to R-compatible literals:
				for (RAnnotation rAnnotation : inNotations) {
					String id = rAnnotation.getAttribute(RAttribute.IDENTIFIER);
					String value = rAnnotation.getAttribute(RAttribute.DEFAULT_VALUE);
					Class<? extends IData> iClass = getInputDataType(id);
					//solution should be suitable for most literal input values:
					assign.put(id, parseLiteralInput(iClass, value));
				}
				
				//delete help variables and utility functions from workspace:
				rCon.eval("rm(list = ls())");
				
				//assign values to the (clean) workspace:
				Iterator<Map.Entry<String, String>> iterator2 = assign.entrySet().iterator();
				for( ;iterator2.hasNext();) {
					Map.Entry<String, String> entry = iterator2.next();
					//use eval, not assign (assign only parses strings)
					rCon.eval(entry.getKey()+" = "+entry.getValue());
				}
				
				//Save an image may help debugging R scripts TODO: remove if not necessary...
				//rCon.eval("save.image(file=\"debug.RData\")");
				
				
				//-------------------------------
				//R script Execution:
				//-------------------------------
				boolean success = readSkript(rSkriptStream, rCon);
				if(!success){
					rCon.close();
					String message = "Error while reading R Skript\nSee previous Logs for Details";
					LOGGER.error(message);
					throw new RuntimeException(message);
				}
				 
				//retrieving result (REXP - Regular Expression Datatype)
				List<RAnnotation> outNotations = RAnnotation.filterAnnotations(annotations, RAnnotationType.OUTPUT);
				for (RAnnotation rAnnotation : outNotations) {
				     String result_id = rAnnotation.getAttribute(RAttribute.IDENTIFIER);
				     REXP result = rCon.eval(result_id);
				     //TODO: change ParseOutput
					 resulthash.put(result_id, parseOutput(result_id, result, rCon));    
				} 
			    
				
			
			}catch (IOException e) {
				e.printStackTrace();
				String message = "Attempt to read R Skript file failed:\n"+e.getClass()+" - "+e.getLocalizedMessage()+"\n"+e.getCause();
				LOGGER.error(message);
			throw new RuntimeException(message);
			} finally{
				if(rCon ==null || !rCon.isConnected()){
					rCon = R_Config.openRConnection();
				}
					
				 //deletes workdirectory:
				if(r_basedir != null){
					String wd =rCon.eval("getwd()").asString();
					rCon.eval("setwd(\""+r_basedir+"\")");
					if(wd != r_basedir)
						rCon.eval("unlink(\""+wd+"\", recursive=TRUE)");
				}
				rCon.eval("rm(list = ls())");
				rCon.close();
				try {
					rSkriptStream.close();
				} catch (IOException e) {
					LOGGER.warn("Connection to R script cannot be closed for process "+getWellKnownName());
					e.printStackTrace();
				}
			}
			
		} catch (RserveException e) {
			e.printStackTrace();
			String message = "An R Connection Error occoured:\n"+e.getClass()+" - "+e.getLocalizedMessage()+"\n"+e.getCause();
			LOGGER.error(message);
			throw new RuntimeException(message);
		} catch (REXPMismatchException e) {
			e.printStackTrace();
			String message = "An R Parsing Error occoured:\n"+e.getMessage()+e.getClass()+" - "+e.getLocalizedMessage()+"\n"+e.getCause();
			LOGGER.error(message);	
			throw new RuntimeException(message);
		}	
			
		deleteRecursive(new File(WORK_DIR));
		
		return resulthash;
	}

	/**
	 * @param rCon
	 * @throws RserveException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private void loadRUtilityScripts(RConnection rCon) throws RserveException,
			IOException, FileNotFoundException {
		File[] utils = new File(R_Config.UTILS_DIR).listFiles(new R_Config.ScriptFilter());
		for (File file : utils) {
			readSkript(new FileInputStream(file), rCon);
		}
	}
	
	/**
	 * parses iData values to string representations which can be evaluated by Rserve,
	 * complex data will be preprocessed and handled here, 
	 * uses parseLiteralInput for parsing literal Data
	 * @param input input value as databinding
	 * @param Rconnection (open)
	 * @return String which could be evaluated by RConnection.eval(String)
	 * @throws IOException 
	 * @throws RserveException 
	 * @throws REXPMismatchException 
	 */
	private String parseInput(List<IData> input, RConnection rCon) throws IOException, RserveException, REXPMismatchException{
		String result = null;
		//building an R - vector of input entries containing more than one value:
		if(input.size()>1){
			result ="c(";
			//parsing elements 1..n-1 to vector:
			for (int i = 0; i<input.size()-1;i++) {
				if(input.get(i).equals(null))continue;
				result+=parseInput(input.subList(i, i+1), rCon);
				result+=", ";
			}
			//parsing last element separately to vecor:
			result+=parseInput(input.subList(input.size()-1, input.size()), rCon);
			result +=")";
		}
		
		IData ivalue = input.get(0);
		Class<? extends IData> iclass = ivalue.getClass();
		if(ivalue instanceof ILiteralData)
			return parseLiteralInput(iclass, ivalue.getPayload());
		
		if(ivalue instanceof GenericFileDataBinding){
				GenericFileData value = (GenericFileData) ivalue.getPayload();
				
				InputStream is = value.getDataStream();
				String ext = value.getFileExtension();
				result = streamFromWPSToRserve(rCon, is, ext);
				return result;
		}
		
		if(ivalue instanceof GTVectorDataBinding){
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
			
		
		//if nothing was supported:
		String message = "An unsuported IData Class occured for input: "+input.get(0).getClass();
		LOGGER.error(message);
		throw new RuntimeException(message);
		
	}

	/**
	 * Streams a file from WPS to Rserve workdirectory
	 * @param rCon active RConnecion
	 * @param is inputstream of the inputfile
	 * @param ext basefile extension
	 * @return
	 * @throws IOException
	 * @throws REXPMismatchException
	 * @throws RserveException
	 */
	private String streamFromWPSToRserve(RConnection rCon, InputStream is,
			String ext) throws IOException, REXPMismatchException,
			RserveException {
		String result;
		String randomname = UUID.randomUUID().toString();
		String inputFileName = randomname;
		
		RFileOutputStream rfos = rCon.createFile(inputFileName);
		
		int blen = Math.min(2048, is.available());
		byte[] buffer = new byte[blen];
		int stop = is.read(buffer);
		
		while(stop != -1){
			rfos.write(buffer, 0, stop);
			stop = is.read(buffer);
		}
		rfos.close();
		is.close();
		//R unzips archive files and renames files with unique 
		//random names
		result = rCon.eval(
				"unzipRename(" 
				+ "\""+inputFileName+"\", "
				+ "\""+randomname+"\", "
				+ "\""+ext+"\")"
				).asString();
		result = "\""+result+ "\"";
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private String parseLiteralInput(Class<? extends IData> iClass, Object value){
		String result = null;
		
		List<Class<? extends ILiteralData>> easyLiterals= Arrays.asList(
				LiteralByteBinding.class,
				LiteralDoubleBinding.class,
				LiteralFloatBinding.class,
				LiteralIntBinding.class,
				LiteralLongBinding.class,
				LiteralShortBinding.class
				);

		if(easyLiterals.contains(iClass))
		{
			result = "" + value;
		}else 
			if(iClass.equals(LiteralBooleanBinding.class))
		{
				if((Boolean) value) 
					 result = "TRUE";
				else result = "FALSE";
		}else{ 
			if(!iClass.equals(LiteralStringBinding.class))
			{				
				String message = "An unsuported IData class occured for input: "+iClass
								+"it will be interpreted as character value within R";
				LOGGER.warn(message);
			}
			
			result = "\"" + value+"\"";
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private IData parseOutput(String result_id, REXP result, RConnection rCon) throws IOException, REXPMismatchException, RserveException{
		Class<? extends IData> iClass = getOutputDataType(result_id);
		
		if(iClass.equals(GenericFileDataBinding.class)){
		
			String mimeType = "application/unknown";
			
			//extract filename from R
			String filename = new File(result.asString()).getName();	
			File tempfile = streamFromRserveToWPS(rCon, filename);
			
			//extract mimetype from annotations (TODO: might have to be simplified somewhen)
			List<RAnnotation> list = RAnnotation.filterAnnotations(
										 annotations,
										 RAnnotationType.OUTPUT,
										 RAttribute.IDENTIFIER, 
										 result_id
										 );
			
			RAnnotation anot = list.get(0);
			String rType = anot.getAttribute(RAttribute.TYPE);
			mimeType = RDataType.getType(rType).getProcessKey();

			return new GenericFileDataBinding(
					new GenericFileData(tempfile, mimeType));
		}
		
		if(iClass.equals(GTVectorDataBinding.class)){
			
			String mimeType = "application/unknown";
			
			//extract filename from R
			String filename = new File(result.asString()).getName();	
			
			RAnnotation out = RAnnotation.filterAnnotations(annotations,RAnnotationType.OUTPUT).get(0);
			RDataType dataType = out.getRDataType();
			File tempfile;
			
			if(dataType.equals(RDataType.SHAPE)||
					dataType.equals(RDataType.SHAPE_ZIP2)){
				loadRUtilityScripts(rCon);
				String zip = "";
				REXP ev = rCon.eval("zipShp(\""+filename+"\")");
				
				//filname --> baseName + suffix
				String baseName = filename;
				String suffix = ".shp";
				if(filename.endsWith(".shp"))
					baseName = filename.substring(0, filename.length() - ".shp".length());
				else
					suffix = null;
				
				// zip all -- stream --> unzip all or stream each file?
				if(!ev.isNull()){
					zip = ev.asString();
					streamFromRserveToWPS(rCon, zip);
					IOUtils.unzipAll(new File(zip));
					tempfile = new File(filename);
				}else{
					LOGGER.info("R call to zip() does not work, streaming of shapefile without zipping");
					String[] dir = rCon.eval("dir()").asStrings();
					for (String f : dir) {
						if(f.startsWith(baseName)&& !f.equals(filename))
							streamFromRserveToWPS(rCon, f);
					}
					
					tempfile = streamFromRserveToWPS(rCon, filename);
				}
			}else{
				//All (single) files which are not Shapefiles
				tempfile = streamFromRserveToWPS(rCon, filename);
			}
			//extract mimetype from annotations (TODO: might have to be simplified somewhen)
			List<RAnnotation> list = RAnnotation.filterAnnotations(
										 annotations,
										 RAnnotationType.OUTPUT,
										 RAttribute.IDENTIFIER, 
										 result_id
										 );
			
			RAnnotation anot = list.get(0);
			String rType = anot.getAttribute(RAttribute.TYPE);
			mimeType = RDataType.getType(rType).getProcessKey();

			GenericFileData gfd =	new GenericFileData(tempfile, mimeType);
			GTVectorDataBinding gtvec = gfd.getAsGTVectorDataBinding();
			return gtvec;
		}
		
		if(iClass.equals(LiteralBooleanBinding.class)){
			int tresult = result.asInteger();
			switch(tresult){
			case 1: return new LiteralBooleanBinding(true);
			case 0:	return new LiteralBooleanBinding(false);};
		};
		
		Class[] easyLiterals = new Class[]{
			LiteralByteBinding.class,
			LiteralDoubleBinding.class,
			LiteralFloatBinding.class,
			LiteralIntBinding.class,
			LiteralLongBinding.class,
			LiteralShortBinding.class,
			LiteralStringBinding.class
		};

		//TODO: Might be a risky solution in terms of unknown constructors:
		for (Class literal : easyLiterals) {
			if(iClass.equals(literal)){
				Constructor<IData> cons = null;
			try {
				cons = (Constructor<IData>) iClass.getConstructors()[0];
				Constructor param = cons.getParameterTypes()[0].getConstructor(String.class);
				return cons.newInstance(param.newInstance(result.asString()));

			 } catch (Exception e) {
				 	String message = "Error for parsing String to IData for "+ result_id+" and class "+iClass + "\n"+e.getMessage();
				 	LOGGER.error(message);
					e.printStackTrace();
					throw new RuntimeException(message);
			 	}
			}
		}
		String message = "R_Proccess: Unsuported Output Data Class declared for id "+ result_id+":"+iClass;
		LOGGER.error(message);
		throw new RuntimeException(message);
	}

	/**
	 * Streams a File from R workdirectory to a temporal file in the WPS4R workdirectory (R.Config.WORK_DIR/random folder)
	 * @param rCon an open R connection
	 * @param filename name or path of the file located in the R workdirectory
	 * @return Location of a file which has been streamed
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private File streamFromRserveToWPS(RConnection rCon, String filename)
			throws IOException, FileNotFoundException {

		//create File to stream from Rserve to WPS4R
		File destination = new File(WORK_DIR);
		if(!destination.exists()) destination.mkdirs();
		File tempfile = new File(destination, filename);
		
		
		//Do streaming Rserve --> WPS tempfile
		RFileInputStream fis = rCon.openFile(filename);
		FileOutputStream fos = new FileOutputStream(tempfile);
		byte[] buffer = new byte[2048];
		int stop = fis.read(buffer);
		while(stop!=-1){
			fos.write(buffer, 0, stop);
			stop = fis.read(buffer);
		}
		fis.close();
		fos.close();
		return tempfile;
	}
	
	
	/**
	 * 
	 * @param skript R input skript
	 * @param rCon Connection - should be open usually / 
	 * otherwise it will be opende and closed separately
	 * @return true if read was successfull
	 * @throws RserveException
	 * @throws IOException 
	 */
	private boolean readSkript(InputStream skript, RConnection rCon) throws RserveException, IOException{
		boolean success = true;
		
		BufferedReader fr = new BufferedReader(new InputStreamReader(skript));
		//reading skript:
		StringBuilder text = new StringBuilder();
		//surrounds R skript with try / catch block in R and an initial digit setting
		text.append(
				"error = try({"      
+ '\n'+		    "options(digits=12)" 
+ '\n'
		);
		while(fr.ready()){
			String line = fr.readLine();
			if(line==null)break;
			text.append(line+"\n");
		}
		text.append(
				"})"
+'\n'+			"hasError = class(error) == \"try-error\" "
+'\n'+			"if(hasError) error_message = as.character(error)"
+'\n'	
		);
		rCon.eval(text.toString());
		try {		
			//retrieving internal R error if occured:
			if(rCon.eval("hasError").asInteger() == 1){
				String message = "Internal R error occured while reading skript: "
						+rCon.eval("error_message").asString();
				LOGGER.error(message);
				success = false;
			}

		//retrieving error from Rserve
		} catch (REXPMismatchException e) {
			 LOGGER.error("Error handling during R-script execution failed: "+e.getMessage());
			 success=false;
		}
		
		return success;
	}
	

	/**
	 * Deletes File or Directory completely with its content
	 * @param in File or directory
	 * @return true if all content could be deleted
	 */
	private boolean deleteRecursive(File in){
		boolean success = true;
		if (!in.exists()){ 
			return false;
		}
		if(in.isDirectory()){
			File[] files = in.listFiles();
			for (File file:files) {
				if(file.isFile()){
					success=success && file.delete();
				}
				if(file.isDirectory()){
					success=success && deleteRecursive(file);
				}
			}
		}
		if(success){
			success=success && in.delete();
		}
		return success;
	}

	/**
	 * Searches annotations (class attribute) for Inputs / Outputs with a specific referring id
	 * @param ioType
	 * @param id
	 * @return
	 */
	private Class<? extends IData> getIODataType(RAnnotationType ioType, String id){
		Class<? extends IData> dataType = null;
		List<RAnnotation> ioNotations = RAnnotation.filterAnnotations(annotations, ioType, RAttribute.IDENTIFIER, id);
		if(ioNotations.isEmpty()){
			LOGGER.error("Missing R-skript-annotation of type "+ioType.toString().toLowerCase()+" for id \""+id +"\" ,datatype - class not found");	
			return null;
		}
		if(ioNotations.size()>1){
			LOGGER.warn("R-skript contains more than one annotation of type "+ioType.toString().toLowerCase()+" for id \""+id +"\n"
						+" WPS selects the first one.");
		}
		RAnnotation annotation = ioNotations.get(0);
		String rClass = annotation.getAttribute(RAttribute.TYPE);
		dataType = RAnnotation.getDataClass(rClass);
		
		if(dataType== null){
			LOGGER.error("R-skript-annotation for "+ioType.toString().toLowerCase()+" id \""+id
					+ "\" contains unsuported data format identifier \""+ rClass+"\"");
			
		}
		return dataType;
	}
	
	
	public Class<? extends IData> getInputDataType(String id){
		return getIODataType(RAnnotationType.INPUT, id);
	};
	
	
	public Class<? extends IData> getOutputDataType(String id){
		return getIODataType(RAnnotationType.OUTPUT, id);
	};
	

		
}