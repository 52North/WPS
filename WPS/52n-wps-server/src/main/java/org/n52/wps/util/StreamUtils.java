package org.n52.wps.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.n52.wps.io.datahandler.binary.LargeBufferStream;

public class StreamUtils {

	public static InputStream convertOutputStreamToInputStream(LargeBufferStream outputStream){
		File tempFile = new File("tempFile"+System.currentTimeMillis());
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
			outputStream.close();
			outputStream.writeTo(outputStream);
			InputStream inputStream = new FileInputStream(tempFile);
			System.gc();
			tempFile.delete();
			return inputStream;
		} catch (FileNotFoundException e) {
			System.gc();
			tempFile.delete();
			throw new RuntimeException("Could not convert streams");
		} catch (IOException e) {
			System.gc();
			tempFile.delete();
			throw new RuntimeException("Could not convert streams");
		}
		
		
		
		
	}
	
}
