package be.vito.ese.wps.io;

import org.n52.wps.io.data.IComplexData;
import org.w3c.dom.Document;

public class GenericXMLDataBinding implements IComplexData{

	private static final long serialVersionUID = -5326483198974935610L;

	private Document document;
	
	public GenericXMLDataBinding(Document document){
		this.document = document;
	}
	
	@Override
	public Document getPayload(){
		return document;
	}

	@Override
	public Class<?> getSupportedClass(){
		return Document.class;
	}

}