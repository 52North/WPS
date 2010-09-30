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


public class Sum2 extends AbstractRProcess {
	private static Logger LOGGER = Logger.getLogger(Sum2.class);
	
	
	
	public Sum2() {
		super();
	}


	private List<String> errors = new ArrayList<String>();
	public List<String> getErrors() {
		return errors;
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