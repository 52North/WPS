package org.n52.wps.server.algorithm.raster;

import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.server.AbstractSelfDescribingAlgorithm;

/**
 * Normalized Difference Water Index algorithm
 * @author gcarrillo
 *
 */
public class ndwi extends AbstractSelfDescribingAlgorithm{
	
	public Class getInputDataType(String id) {
		return GTRasterDataBinding.class;
		
	}

	public Class getOutputDataType(String id) {
		return GTRasterDataBinding.class;
	}

	@Override
	public List<String> getInputIdentifiers() {
		List<String> identifierList =  new ArrayList<String>();
		/* 
		 * NIR:   Near-InfraRed
		 * SWIR:  Short-wave InfraRed
		 * */
		identifierList.add("NIR");
		identifierList.add("SWIR");
		return identifierList;
	}

	@Override
	public List<String> getOutputIdentifiers() {
		List<String> identifierList =  new ArrayList<String>();
		identifierList.add("NDWI");
		return identifierList;
	}


	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
				
		if(inputData==null || !inputData.containsKey("NIR")){
			throw new RuntimeException("Error while allocating input parameters");
		}
		
		List<IData> dataList1 = inputData.get("NIR");
		if(dataList1 == null || dataList1.size() != 1){
			throw new RuntimeException("Error while allocating input parameters");
		}
		IData dataset1 = dataList1.get(0);
		GridCoverage2D gridCoverage1 = (GridCoverage2D) dataset1.getPayload();
		RenderedImage image1 = gridCoverage1.getRenderedImage();
		
		if(inputData==null || !inputData.containsKey("SWIR")){
			throw new RuntimeException("Error while allocating input parameters");
		}
		
		List<IData> dataList2 = inputData.get("SWIR");
		if(dataList2 == null || dataList2.size() != 1){
			throw new RuntimeException("Error while allocating input parameters");
		}
		IData dataset2 = dataList2.get(0);
		GridCoverage2D gridCoverage2 = (GridCoverage2D) dataset2.getPayload();
		RenderedImage image2 = gridCoverage2.getRenderedImage();
		  
		// NDWI = (NIR - SWIR) / (NIR + SWIR)
		RenderedOp numerator = JAI.create("subtract", image2, image1);
		RenderedOp denominator= JAI.create("add", image1, image2);
		double[] constants = new double[1];
		constants[0] = 0.01; // Avoid division by zero error
		RenderedOp adjustedDenominator = JAI.create("addconst", denominator, constants);
		RenderedOp ndwi = JAI.create("divide", numerator, adjustedDenominator);
		
		GridCoverageFactory gcf = new GridCoverageFactory();
        GridCoverage2D output =  gcf.create("NDWI", ndwi, gridCoverage1.getEnvelope());
        
		HashMap<String,IData> resulthash = new HashMap<String,IData>();
		resulthash.put("NDWI", new GTRasterDataBinding(output));
		
		return resulthash;
	
	}
		
}
