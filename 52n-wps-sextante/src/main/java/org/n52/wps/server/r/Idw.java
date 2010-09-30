package org.n52.wps.server.r;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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


public class Idw extends AbstractObservableAlgorithm {
	private static Logger LOGGER = Logger.getLogger(Idw.class);
	
	
	
	public Idw() {
		super();
	}


	private List<String> errors = new ArrayList<String>();
	public List<String> getErrors() {
		return errors;
	}
	
	public Map<String, IData> run(Map<String, List<IData>> inputData){
		/*String[] keys = new String[]{"raster","points","attributename"};
		String[] optkeys = new String[]{"nmax","maxdist"};
		
		List<IData> dataList = null;
		
		for(String key: keys){
			if(inputData==null || !inputData.containsKey(key)){
				throw new RuntimeException("Error while allocating input parameters");
			}
			
			if(dataList == null || dataList.size() != 1){
				throw new RuntimeException("Error while allocating input parameters");
			}
			
		}
		//receive point feature input
		if(inputData==null || !inputData.containsKey("points")){
			throw new RuntimeException("Error while allocating input parameters");
		}
		
		dataList = inputData.get("points");
		if(dataList == null || dataList.size() != 1){
			throw new RuntimeException("Error while allocating input parameters");
		}
		IData firstInputData = dataList.get(0);
				
		GenericFileData pointData4R = ((GenericFileDataBinding) firstInputData).getPayload();
		
		//receive raster input
		if(inputData==null || !inputData.containsKey("raster")){
			throw new RuntimeException("Error while allocating input parameters");
		}
		
		dataList = inputData.get("raster");
		if(dataList == null || dataList.size() != 1){
			throw new RuntimeException("Error while allocating input parameters");
		}
	    firstInputData = dataList.get(0);
				
		GenericFileData rasterData4R = ((GenericFileDataBinding) firstInputData).getPayload();
		
		//receive point feature input
		if( !inputData.containsKey("maxdist")){
			throw new RuntimeException("Error while allocating input parameters");
		}
		List<IData> attributeNameDataList = inputData.get("maxdist");
		if(attributeNameDataList == null || attributeNameDataList.size() != 1){
			throw new RuntimeException("Error while allocating input parameters");
		}
		String maxdist = ((LiteralStringBinding) attributeNameDataList.get(0)).getPayload();
		*/
		GenericFileData resultData=null;
		try {
			resultData = runRIdw(inputData);
		} catch (RserveException e) {
			e.printStackTrace();
			String message = "An R Connection Error occoured:\n"+e.getClass()+" - "+e.getLocalizedMessage()+"\n"+e.getCause();
			LOGGER.error(message);
			throw new RuntimeException(message);
		}
		HashMap<String,IData> resulthash = new HashMap<String,IData>();
		
		resulthash.put("output", new GenericFileDataBinding(resultData));
		
		return resulthash;
	}
	
	
		

//	private GenericFileData runRIdw(GenericFileData pointData4R, GenericFileData rasterData4R,	String maxdist) {
	private GenericFileData runRIdw(Map<String, List<IData>> inputData) throws RserveException {	
		GenericFileData result = null;
		
		//retrieve rSkript from path:
		String className = this.getClass().getName().replace(".", "/");
		InputStream rSkript = this.getClass().getResourceAsStream("/" + className + "Skript.xml");

		//set work directory and retrieve path for R:
		String rname = UUID.randomUUID().toString();
		String workPathRoot = WebProcessingService.BASE_DIR + "\\WEB-INF\\RWorkDir";
		workPathRoot = workPathRoot.replace("\\", "/");
		String workPath=workPathRoot+"/"+rname;
		File workDir = new File(workPath);
		workDir.mkdir();
		
		//writes input file to work directory and retrieves path for R:
//		String inputFileName=new File(pointData4R.writeData(workDir)).getName();
		
		//interaction with R from here:
		RConnection rCon = null;
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
			//rCon.eval("pointfile = \""+inputFileName+"\"");
			//rCon.eval("attribute = \""+maxdist+"\"");			

			rCon.eval("save.image(file=\"debug.RData\")");
			
			//try to read skript:
			boolean success = readSkriptLBL(rSkript, rCon);

			//changes directory to get work directory deletable:
			rCon.eval("setwd(\""+workPathRoot+"\")");
			if(!success){
				rCon.close();
				String message = "Error while reading R Skript\nSee previous Logs for Details";
				LOGGER.error(message);
				throw new RuntimeException(message);
			}
			String resPath = rCon.eval("output").asString();
			result = new GenericFileData(new File(resPath), GenericFileDataConstants.MIME_TYPE_HDF);
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
		}finally{
			if(rCon!=null && rCon.isConnected()){
				//changes directory to get work directory deletable:
				rCon.eval("setwd(\""+workPathRoot+"\")");
				rCon.close();
			}
			if(!deleteRecursive(workDir)){
				LOGGER.error("R work directory "+workDir.getName()+" could not be removed completly.");
			}
		}
		return result;
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
		List<Class<? extends IData>> literals= Arrays.asList(
				LiteralDoubleBinding.class,
				LiteralFloatBinding.class,
				LiteralIntBinding.class,
				LiteralShortBinding.class,
				LiteralByteBinding.class);

		if(literals.contains(input.get(0).getClass())){
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
				
		//retrieving internal R error if occured:
		try {
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

	public Class getInputDataType(String id) {
			if(id.equalsIgnoreCase("raster")){
			return GenericFileDataBinding.class;
		}else
			if(id.equalsIgnoreCase("points")){
			return GenericFileDataBinding.class;
		}else
			if(id.equalsIgnoreCase("attributename")){
			return LiteralStringBinding.class;
		}else
			if(id.equalsIgnoreCase("nmax")){
			return LiteralIntBinding.class;
		}else
			if(id.equalsIgnoreCase("maxdist")){
			return LiteralDoubleBinding.class;
		}
		
		throw new RuntimeException("Could not find datatype for id " + id);	
	}

	public Class getOutputDataType(String id) {
		return GenericFileDataBinding.class;
	}

		
}