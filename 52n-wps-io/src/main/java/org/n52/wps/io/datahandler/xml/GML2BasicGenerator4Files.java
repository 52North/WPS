package org.n52.wps.io.datahandler.xml;

import java.io.OutputStream;
import java.io.Writer;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.w3c.dom.Node;

public class GML2BasicGenerator4Files extends GML2BasicGenerator{
	
	public void write(IData outputData, Writer writer) {
		if(!(outputData instanceof GenericFileDataBinding)){
			throw new RuntimeException("GML2BasicGenerator4Files does not support incoming datatype");
		}
		GTVectorDataBinding vectorBinding = ((GenericFileDataBinding)outputData).getPayload().getAsGTVectorDataBinding();
		super.write(vectorBinding, writer);
	}
	
	public Node generateXML(IData outputData, String schema) {
		if(!(outputData instanceof GenericFileDataBinding)){
			throw new RuntimeException("GML2BasicGenerator4Files does not support incoming datatype");
		}
		GTVectorDataBinding vectorBinding = ((GenericFileDataBinding)outputData).getPayload().getAsGTVectorDataBinding();
		return super.generateXML(vectorBinding, schema);
	}
	
	public OutputStream generate(IData outputData) {
		if(!(outputData instanceof GenericFileDataBinding)){
			throw new RuntimeException("GML2BasicGenerator4Files does not support incoming datatype");
		}
		GTVectorDataBinding vectorBinding = ((GenericFileDataBinding)outputData).getPayload().getAsGTVectorDataBinding();
		return super.generate(vectorBinding);
	}
	
	public void writeToStream(IData outputData, OutputStream os) {
		if(!(outputData instanceof GenericFileDataBinding)){
			throw new RuntimeException("GML2BasicGenerator4Files does not support incoming datatype");
		}
		GTVectorDataBinding vectorBinding = ((GenericFileDataBinding)outputData).getPayload().getAsGTVectorDataBinding();
		super.writeToStream(vectorBinding, os);
	}
	
	public Class[] getSupportedInternalInputDataType() {
		Class[] supportedClasses = {GenericFileDataBinding.class};
		return supportedClasses;
	}
}
