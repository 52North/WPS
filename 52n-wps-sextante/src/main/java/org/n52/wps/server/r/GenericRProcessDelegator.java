package org.n52.wps.server.r;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.log4j.Logger;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.IAlgorithm;


public class GenericRProcessDelegator implements IAlgorithm, SextanteConstants {

	private static Logger LOGGER = Logger.getLogger(GenericRProcessDelegator.class);

	private String processID;
	private ProcessDescriptionType processDescription;
	private List<String> errors;
//	private GeoAlgorithm sextanteProcess;


	public GenericRProcessDelegator(String processID, ProcessDescriptionType processDescriptionType) {
		this.processID = processID.replace("Sextante_","");;
		errors = new ArrayList<String>();
		this.processDescription = processDescriptionType;
		//sextanteProcess = Sextante.getAlgorithmFromCommandLineName(processID);

	}

	public ProcessDescriptionType getDescription() {
		return processDescription;
	}

	public List<String> getErrors() {
		return errors;
	}

	public String getWellKnownName() {
		return processID;
	}

	public boolean processDescriptionIsValid() {
		return processDescription.validate();
	}

	public Map<String, IData> run(Map<String, List<IData>> inputData) {

		Map<String, IData> resultMap = new HashMap<String, IData>();
		



		return resultMap;
	}


	
	public Class getInputDataType(String id) {

		return null;

	}

	public Class getOutputDataType(String id) {
		/*OutputDescriptionType[] outputs = processDescription.getProcessOutputs().getOutputArray();

		for(OutputDescriptionType output : outputs){

			if(!output.getIdentifier().getStringValue().equals(id)){
				continue;
			}
			if(output.isSetLiteralOutput()){
				String datatype = output.getLiteralOutput().getDataType().getStringValue();
				if(datatype.contains("tring")){
					return LiteralStringBinding.class;
				}
				if(datatype.contains("ollean")){
					return LiteralBooleanBinding.class;
				}
				if(datatype.contains("loat") || datatype.contains("ouble")){
					return LiteralDoubleBinding.class;
				}
				if(datatype.contains("nt")){
					return LiteralIntBinding.class;
				}
			}
			if(output.isSetComplexOutput()){
				String mimeType = output.getComplexOutput().getDefault().getFormat().getMimeType();
				if(mimeType.contains("xml") || (mimeType.contains("XML"))){
					return GTVectorDataBinding.class;
				}else{
					return GTRasterDataBinding.class;
				}
			}
		}*/
		return null;
	}








}

