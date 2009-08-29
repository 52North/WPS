package org.n52.wps.io.data.binding.complex;

import java.io.File;

import org.n52.wps.io.data.IData;

public class FileDataBinding implements IData {
	private File file;

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
}
