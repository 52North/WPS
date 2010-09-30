package org.n52.wps.server.r;

import org.apache.log4j.Logger;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;


public class Idw2 extends AbstractRProcess {
	private static Logger LOGGER = Logger.getLogger(Idw2.class);
	
	
	
	public Idw2() {
		super();
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