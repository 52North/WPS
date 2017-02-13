package be.vito.ese.wps.io;

import java.io.OutputStream;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.binary.LargeBufferStream;
import org.n52.wps.io.datahandler.xml.AbstractXMLGenerator;
import org.w3c.dom.Node;

public class TimeSeriesGenerator extends AbstractXMLGenerator{

	@Override
	public OutputStream generate(IData coll){

		LargeBufferStream lbos = new LargeBufferStream();
		// this.writeToStream(coll, lbos);
		return lbos;

	}

	@Override
	public Class<?>[] getSupportedInternalInputDataType(){
		
		Class<?> [] supportedClasses = {TimeSeriesDataBinding.class};		
		return supportedClasses;

	}

	@Override
	public Node generateXML(IData coll, String schema){
		return ((TimeSeriesDataBinding)coll).getPayload().getDomNode();
	}

}
