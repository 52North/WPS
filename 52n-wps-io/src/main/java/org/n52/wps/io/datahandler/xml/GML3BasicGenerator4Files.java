package org.n52.wps.io.datahandler.xml;

import java.io.OutputStream;
import java.io.Writer;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.w3c.dom.Node;

public class GML3BasicGenerator4Files extends GML3BasicGenerator{

	
	protected void write(IData outputData, Writer writer) {
		if(outputData instanceof GTVectorDataBinding){
			 super.write(outputData, writer);
		}else{
			if (outputData instanceof GenericFileDataBinding) {
				//convert and recall write
				GTVectorDataBinding vectorBinding = ((GenericFileDataBinding)outputData).getPayload().getAsGTVectorDataBinding();
				write(vectorBinding, writer);
			}
			else {
				throw new RuntimeException("GML3BasicGenerator4Files does not support incoming datatype");
			}
		}
	}
	
	
	public Node generateXML(IData outputData, String schema) {
		
		if(outputData instanceof GTVectorDataBinding){
			return super.generateXML(outputData, schema);
		}else{
			if (outputData instanceof GenericFileDataBinding) {
				//convert and recall generateXML
				GTVectorDataBinding vectorBinding = ((GenericFileDataBinding)outputData).getPayload().getAsGTVectorDataBinding();
				return generateXML(vectorBinding, schema);
			}
			else {
				throw new RuntimeException("GML3BasicGenerator4Files does not support incoming datatype");
			}
		}
	}
	
	public OutputStream generate(IData outputData) {
		
		if(outputData instanceof GTVectorDataBinding){
			return super.generate(outputData);
		}else{
			if (outputData instanceof GenericFileDataBinding) {
				//convert and recall generate
				GTVectorDataBinding vectorBinding = ((GenericFileDataBinding)outputData).getPayload().getAsGTVectorDataBinding();
				return generate(vectorBinding);
			}
			else {
				throw new RuntimeException("GML3BasicGenerator4Files does not support incoming datatype");
			}
		}
	}
	
	public void writeToStream(IData outputData, OutputStream os) {
		
		if(outputData instanceof GTVectorDataBinding){
			super.writeToStream(outputData, os);
		}else{
			if (outputData instanceof GenericFileDataBinding) {
				//convert and recall writeToStream
				GTVectorDataBinding vectorBinding = ((GenericFileDataBinding)outputData).getPayload().getAsGTVectorDataBinding();
				writeToStream(vectorBinding, os);
			}
			else {
				throw new RuntimeException("GML3BasicGenerator4Files does not support incoming datatype");
			}
		}
	}
	
	public Class[] getSupportedInternalInputDataType() {
		Class[] supportedClasses = {GenericFileDataBinding.class};
		return supportedClasses;
	}
}
