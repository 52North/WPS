package org.n52.wps.io.data.binding.complex;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

import org.n52.wps.io.data.IComplexData;

public class FileDataBinding implements IComplexData {
	protected File file;

	public FileDataBinding(File file) {
		this.file = file;
	}

	@Override
	public File getPayload() {
		return file;
	}

	@Override
	public Class<?> getSupportedClass() {
		return File.class;
	}
	
	private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException
	{
		throw new RuntimeException("Serialization of 'FileDataBinding' data type not implemented yet.");
	}
	
	private synchronized void readObject(java.io.ObjectInputStream oos) throws IOException, ClassNotFoundException
	{
		throw new RuntimeException("Deserialization of 'FileDataBinding' data type not implemented yet.");
	}
	
	@Override
    public void dispose() {
        FileUtils.deleteQuietly(file);
    }
}
