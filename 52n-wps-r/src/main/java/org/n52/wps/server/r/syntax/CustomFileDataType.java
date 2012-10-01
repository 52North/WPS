package org.n52.wps.server.r.syntax;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;

public class CustomFileDataType implements RTypeDefinition{

	String key;
	String processKey;
	String encoding;
	String schema;
	
	@Override
	public String getKey() {
		return null;
	}

	@Override
	public String getProcessKey() {
		return null;
	}

	@Override
	public boolean isComplex() {
		return false;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setProcessKey(String processKey) {
		this.processKey = processKey;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void setSchema(String schema) {
		schema = this.schema;
	}

	@Override
	public String getEncoding() {
		return "base64";
	}

	@Override
	public String getSchema() {
		return schema;
	}

	@Override
	public Class<? extends IData> getIDataClass() {
		
		return GenericFileDataBinding.class;
	}

}
