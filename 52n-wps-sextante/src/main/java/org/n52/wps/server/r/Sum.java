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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.stream.util.StreamReaderDelegate;

import org.n52.wps.server.*;
import org.apache.log4j.Logger;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractObservableAlgorithm;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;


public class Sum extends AbstractObservableAlgorithm {
	private static Logger LOGGER = Logger.getLogger(Sum.class);
	
	
	
	public Sum() {
		super();
	}


	private List<String> errors = new ArrayList<String>();
	public List<String> getErrors() {
		return errors;
	}
	
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		if(inputData==null || !inputData.containsKey("data")){
			throw new RuntimeException("Error while allocating input parameters");
		}
		
		List<IData> dataList = inputData.get("data");
		if(dataList == null || dataList.size() != 1){
			throw new RuntimeException("Error while allocating input parameters");
		}
		IData firstInputData = dataList.get(0);
				
		GenericFileData inputData4R = ((GenericFileDataBinding) firstInputData).getPayload();
		
		if( !inputData.containsKey("attributename")){
			throw new RuntimeException("Error while allocating input parameters");
		}
		List<IData> attributeNameDataList = inputData.get("attributename");
		if(attributeNameDataList == null || attributeNameDataList.size() != 1){
			throw new RuntimeException("Error while allocating input parameters");
		}
		String attributeName = ((LiteralStringBinding) attributeNameDataList.get(0)).getPayload();
		
		Double resultData = runRSum(inputData4R, attributeName);
		HashMap<String,IData> resulthash = new HashMap<String,IData>();
		resulthash.put("result", new LiteralDoubleBinding(resultData));
		return resulthash;
	}
	
	
		

	private Double runRSum(GenericFileData inputData4R,	String attributeName) {	
		double result = Double.NaN;
		
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
		String inputFileName=new File(inputData4R.writeData(workDir)).getName();
		
		//interaction with R from here:
		RConnection rCon = null;
		try {
			rCon = new RConnection();
			rCon.eval("setwd(\""+workPath+"\")");
			rCon.eval("data = \""+inputFileName+"\"");
			rCon.eval("attribute = \""+attributeName+"\"");
			
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
			result = rCon.eval("sum").asDouble();			
		}catch (RserveException e) {
			e.printStackTrace();
			String message = "An R Connection Error occoured:\n"+e.getClass()+" - "+e.getLocalizedMessage()+"\n"+e.getCause();
			LOGGER.error(message);
			rCon.close();
			throw new RuntimeException(message);
		}catch (IOException e) {
			e.printStackTrace();
			String message = "Attempt to read R Skript file failed:\n"+e.getClass()+" - "+e.getLocalizedMessage()+"\n"+e.getCause();
			LOGGER.error(message);
			rCon.close();
			throw new RuntimeException(message);
		}catch (REXPMismatchException e){
			e.printStackTrace();
			String message = "An R Parsing Error occoured:\n"+e.getMessage()+e.getClass()+" - "+e.getLocalizedMessage()+"\n"+e.getCause();
			LOGGER.error(message);	
			rCon.close();
			throw new RuntimeException(message);
		}finally{
			if(rCon!=null && !rCon.close()){
				rCon.close();
			}
			if(!deleteRecursive(workDir)){
				LOGGER.error("R work directory "+workDir.getName()+" could not be removed completly.");
			}
		}
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
								 +rCon.eval("error_message").asString();
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
		if(id.equalsIgnoreCase("data")){
			return GenericFileDataBinding.class;
		}else if(id.equalsIgnoreCase("attributename")){
				return LiteralStringBinding.class;
		}
		throw new RuntimeException("Could not find datatype for id " + id);	
	}

	public Class getOutputDataType(String id) {
		return LiteralDoubleBinding.class;
	}

		
}