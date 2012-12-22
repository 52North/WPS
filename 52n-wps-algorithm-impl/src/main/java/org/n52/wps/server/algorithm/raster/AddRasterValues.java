package org.n52.wps.server.algorithm.raster;

import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.TiledImage;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.feature.FeatureCollection;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.server.AbstractSelfDescribingAlgorithm;
import org.opengis.coverage.grid.GridCoverage;

public class AddRasterValues extends AbstractSelfDescribingAlgorithm{


	public Class getInputDataType(String id) {
		return GTRasterDataBinding.class;
		
	
	}

	public Class getOutputDataType(String id) {
		return GTRasterDataBinding.class;
	}

	@Override
	public List<String> getInputIdentifiers() {
		List<String> identifierList =  new ArrayList<String>();
		identifierList.add("dataset1");
		identifierList.add("dataset2");
		return identifierList;
	}

	@Override
	public List<String> getOutputIdentifiers() {
		List<String> identifierList =  new ArrayList<String>();
		identifierList.add("result");
		return identifierList;
	}


	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		if(inputData==null || !inputData.containsKey("dataset1")){
			throw new RuntimeException("Error while allocating input parameters");
		}
		
		List<IData> dataList1 = inputData.get("dataset1");
		if(dataList1 == null || dataList1.size() != 1){
			throw new RuntimeException("Error while allocating input parameters");
		}
		IData dataset1 = dataList1.get(0);
		GridCoverage2D gridCoverage1 = (GridCoverage2D) dataset1.getPayload();
		RenderedImage image1 = gridCoverage1.getRenderedImage();
		
		if(inputData==null || !inputData.containsKey("dataset2")){
			throw new RuntimeException("Error while allocating input parameters");
		}
		
		List<IData> dataList2 = inputData.get("dataset2");
		if(dataList2 == null || dataList2.size() != 1){
			throw new RuntimeException("Error while allocating input parameters");
		}
		IData dataset2 = dataList2.get(0);
		GridCoverage2D gridCoverage2 = (GridCoverage2D) dataset2.getPayload();
		RenderedImage image2 = gridCoverage2.getRenderedImage();
		
		RenderedOp resultImage = JAI.create("add",image1, image2);
		
		GridCoverageFactory gcf = new GridCoverageFactory();
        GridCoverage2D output =  gcf.create("result", resultImage, gridCoverage1.getEnvelope());

       
		HashMap<String,IData> resulthash = new HashMap<String,IData>();
		resulthash.put("result", new GTRasterDataBinding(output));
		return resulthash;
	
	}
	
		
		
	
}
