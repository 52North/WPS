package org.n52.wps.server.sextante;

import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.UnsupportedOutputChannelException;
import es.unex.sextante.geotools.GTOutputFactory;
import es.unex.sextante.geotools.GTVectorLayer;
import es.unex.sextante.geotools.MemoryVectoryLayerFactory;
import es.unex.sextante.geotools.ShapefilePostStrategy;
import es.unex.sextante.outputs.FileOutputChannel;
import es.unex.sextante.outputs.IOutputChannel;

public class N52OutputFactory extends GTOutputFactory{
	
	public N52OutputFactory(){
		super();
	}

	@Override
	public IVectorLayer getNewVectorLayer(String sName,
			  int iShapeType,
			  Class[] types,
			  String[] sFields,
			  IOutputChannel channel,
			  Object crs) throws UnsupportedOutputChannelException {

		if (channel instanceof FileOutputChannel){
			String sFilename = ((FileOutputChannel)channel).getFilename();
			GTVectorLayer vectorLayer = new MemoryVectoryLayerFactory().create(sName, iShapeType, types, sFields, sFilename, crs);
			vectorLayer.setPostProcessStrategy(new NullStrategy());
			return vectorLayer;
		}
		else{
			throw new UnsupportedOutputChannelException();
		}

	}

}
