package org.n52.wps.server.r;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.stream.util.StreamReaderDelegate;

import org.n52.wps.server.*;
import org.apache.log4j.Logger;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.GenericFileDataConstants;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.literal.*;
import org.n52.wps.server.AbstractObservableAlgorithm;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;


public abstract class CopyOfAbstractRProcess extends AbstractObservableAlgorithm {
	private static Logger LOGGER = Logger.getLogger(CopyOfAbstractRProcess.class);
	
	
	
	public CopyOfAbstractRProcess() {
		super();
	}


	private List<String> errors = new ArrayList<String>();
	public List<String> getErrors() {
		return errors;
	}
	
	public Map<String, IData> run(Map<String, List<IData>> inputData){
		//retrieve rSkript from path:
		String className = this.getClass().getName().replace(".", "/");
		InputStream rSkript = this.getClass().getResourceAsStream("/" + className + "Skript.xml");
		//set work directory and retrieve path for R:
		String rname = UUID.randomUUID().toString();
		String workPathRoot = WebProcessingService.BASE_DIR + "\\WEB-INF\\RWorkDir";
		workPathRoot = workPathRoot.replace("\\", "/");
			
		File wpRoot = new File(workPathRoot);
		if(!wpRoot.exists())wpRoot.mkdir();
		String workPath=workPathRoot+"/"+rname;
		File workDir = new File(workPath);
		workDir.mkdir();
			
		//interaction with R from here:
		RConnection rCon = null;
		String resultString = null;
		try{
			try {				
				//initializes connection and pre-settings
				rCon = new RConnection();
				rCon.eval("setwd(\""+workPath+"\")");
				
				//initializes input variables by iterating over entries:
				Iterator<Map.Entry<String, List<IData>>> iterator = inputData.entrySet().iterator();
				for (; iterator.hasNext();) {
					Map.Entry<String, List<IData>> entry = iterator.next();
					rCon.eval(entry.getKey()+" = "+parseInput(entry.getValue(), workDir));
				}		

//Saving an image may help debugging R skripts:
//rCon.eval("save.image(file=\"debug.RData\")");
				
				//try to read skript:
				boolean success = readSkript(rSkript, rCon);
				
				//changes directory to get work directory deletable:
				rCon.eval("setwd(\""+workPathRoot+"\")");
				if(!success){
					rCon.close();
					String message = "Error while reading R Skript\nSee previous Logs for Details";
					LOGGER.error(message);
					throw new RuntimeException(message);
				}
			
				//retrieving result (literal string or filepath)
			    resultString = rCon.eval("result").asString();
				
			
			}catch (IOException e) {
				e.printStackTrace();
				String message = "Attempt to read R Skript file failed:\n"+e.getClass()+" - "+e.getLocalizedMessage()+"\n"+e.getCause();
				LOGGER.error(message);
				//rCon.close();
			throw new RuntimeException(message);
			}catch (REXPMismatchException e){
				e.printStackTrace();
				String message = "An R Parsing Error occoured:\n"+e.getMessage()+e.getClass()+" - "+e.getLocalizedMessage()+"\n"+e.getCause();
				LOGGER.error(message);	
				//rCon.close();
				throw new RuntimeException(message);

			} finally{
				if(rCon!=null && rCon.isConnected()){
					//changes directory to get work directory deletable:
					rCon.eval("setwd(\""+workPathRoot+"\")");
					rCon.close();
				}
			}
		} catch (RserveException e) {
			e.printStackTrace();
			String message = "An R Connection Error occoured:\n"+e.getClass()+" - "+e.getLocalizedMessage()+"\n"+e.getCause();
			LOGGER.error(message);
			throw new RuntimeException(message);

		}finally{
			if(!deleteRecursive(workDir)){
				LOGGER.error("R work directory "+workDir.getName()+" could not be removed completly.");
			}
		}

		HashMap<String,IData> resulthash = new HashMap<String,IData>();
		
		//resultData = new GenericFileData(new File(resPath), GenericFileDataConstants.MIME_TYPE_HDF);
		try {
			resulthash.put("output", parseOutput(resultString));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return resulthash;
	}
	
	@SuppressWarnings("unchecked")
	private IData parseOutput(String resultString) throws IOException{
		Class<IData> outputDataType = getOutputDataType(null);
		
		if(outputDataType.equals(GenericFileDataBinding.class)){
			String mimeType = "unknown file";
			File primFile = new File(resultString);
			
			//searches for mime - type by file - extension
			Set<Map.Entry<String,String>> entries = GenericFileDataConstants.mimeTypeFileTypeLUT().entrySet();
			for (Map.Entry<String, String> entry : entries) {
				if(primFile.getName().endsWith(entry.getValue())){
					mimeType = entry.getKey();
				}	
			}
			return new GenericFileDataBinding(
					new GenericFileData(primFile, mimeType));
		}
		
		if(outputDataType.equals(LiteralBooleanBinding.class)){
			if(resultString.equalsIgnoreCase("TRUE"))
					return new LiteralBooleanBinding(true);
			if(resultString.equalsIgnoreCase("FALSE"))
				return new LiteralBooleanBinding(false);
		};
		
		Class[] easyLiterals = new Class[]{
		LiteralByteBinding.class,
		LiteralDoubleBinding.class,
		LiteralFloatBinding.class,
		LiteralIntBinding.class,
		LiteralLongBinding.class,
		LiteralShortBinding.class,
		LiteralStringBinding.class};

		for (Class literal : easyLiterals) {
			if(outputDataType.equals(GenericFileDataBinding.class)){
				Constructor<IData> cons = null;
			try {
				cons = outputDataType.getConstructor();
				Class param = cons.getParameterTypes()[0];
				return cons.newInstance(param.cast(resultString));

			    } catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}//getConstructors()[0];
				
			}
		}
		throw new RuntimeException("Unsuported Output Data Type declared: "+outputDataType);
	}
	
	@SuppressWarnings("unchecked")
	private String parseInput(List<IData> input, File workDir){
		String result = null;
		//building an R - vector of inputs for multiple inputs:
		if(input.size()>1){
			result ="c(";
			//parsing elements 1..n-1 to vector:
			for (int i = 0; i<input.size()-1;i++) {
				if(input.get(i).equals(null))continue;
				result+=parseInput(input.subList(i, i+1), workDir);
				result+=", ";
			}
			//parsing last element separately to vecor:
			result+=parseInput(input.subList(input.size()-1, input.size()), workDir);
			result +=")";
		}
		List<Class<? extends IData>> easyLiterals= Arrays.asList(
				LiteralDoubleBinding.class,
				LiteralFloatBinding.class,
				LiteralIntBinding.class,
				LiteralShortBinding.class,
				LiteralByteBinding.class);

		if(easyLiterals.contains(input.get(0).getClass())){
			result = "" + input.get(0).getPayload();
		}else
			if(input.get(0).getClass().equals(LiteralStringBinding.class)){
			result = "\"" + input.get(0).getPayload()+"\""; 
		}else
			if(input.get(0).getClass().equals(LiteralBooleanBinding.class)){
				if((Boolean) input.get(0).getPayload()) 
					 result = "T";
				else result = "F";
		}else
			if(input.get(0).getClass().equals(GenericFileData.class)){
				GenericFileData value = (GenericFileData) input.get(0);
				String inputFileName = new File(value.writeData(workDir)).getName();
				result = "\""+inputFileName+"\"";
		}else
		    if(input.get(0).getClass().equals(GenericFileDataBinding.class)){
				GenericFileData value = (GenericFileData) input.get(0).getPayload();
				String inputFileName = new File(value.writeData(workDir)).getName();
				result = "\""+inputFileName+"\"";
		}else 
			throw new RuntimeException("Unsuported Input Data Type: "+input.get(0).getClass());
		return result;
	}
	
	/**
	 * 
	 * @param skript R input skript
	 * @param rCon Connection - should be open usually / 
	 * otherwise it will be opende and closed separately
	 * @return true if read was successfull
	 * @throws IOException
	 * @throws RserveException
	 */
	private boolean readSkript(InputStream skript, RConnection rCon) throws IOException, RserveException{
		boolean success = true;
		
		//closes and opens connection if connection is not open
		boolean finclose = false;
		if(!rCon.isConnected()){
			rCon = new RConnection();
			finclose = true;
		}
		
		BufferedReader fr = new BufferedReader(new InputStreamReader(skript));
		//reading skript:
		StringBuilder text = new StringBuilder();
		while(fr.ready()){
			String line = fr.readLine();
			if(line==null)break;
			text.append(line+"\n");
		}
		rCon.eval(text.toString());
		try {		
			//retrieving internal R error if occured:
			String[] wspace = rCon.eval("ls()").asStrings();
			if(rCon.eval("hasError").asInteger() == 1){
				String message = "Internal R error occured while reading skript: "
								 +rCon.eval("error-message").asString();
				LOGGER.error(message);
				success = false;
			}
			
		//retrieving error from Rserve
		} catch (REXPMismatchException e) {
			 LOGGER.error(e.getMessage());
			 success=false;
		}
		
		
		if(finclose){
			rCon.close();
		}
		return success;
	}
	
	/**
	 * Reads a script line by line
	 * - not sufficient for commands of multiple lines
	 * - more sufficient for debugging purposes
	 * @param skript R input skript
	 * @param rCon Connection - should be open usually / 
	 * otherwise it will be opende and closed separately
	 * @return true if read was successfull
	 * @throws IOException
	 * @throws RserveException
	 */
	private boolean readSkriptLBL(InputStream skript, RConnection rCon) throws IOException, RserveException{
		boolean success = true;
		
		//closes and opens connection if connection is not open
		boolean finclose = false;
		if(!rCon.isConnected()){
			rCon = new RConnection();
			finclose = true;
		}
		
		BufferedReader fr = new BufferedReader(new InputStreamReader(skript));
		//reading skript:
		for(int i=1;fr.ready();i++){
			String line = fr.readLine();
			if(line==null)break;
			try{
			rCon.eval(line);
			}catch(RserveException e){
				throw new RuntimeException(e.getMessage()+"\nSyntax error in line:"+i+"\n"+line);
			}
		}
		
		
		
		if(finclose){
			rCon.close();
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

	public abstract Class getInputDataType(String id);
	public abstract Class getOutputDataType(String id);

		
}