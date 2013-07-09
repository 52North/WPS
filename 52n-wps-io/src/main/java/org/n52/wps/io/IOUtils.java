package org.n52.wps.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.codec.binary.Base64InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import static org.apache.commons.io.IOUtils.*;

public class IOUtils {
	/**
	 * Reads the given input stream as a string and decodes that base64 string
	 * into a file with the specified extension
	 * 
	 * @param input
	 *            the stream with the base64 string
	 * @param extension
	 *            the extension of the result file (without the '.' at the
	 *            beginning)
	 * @return the decoded base64 file written to disk
	 * @throws IOException
	 *             if an error occurs while writing the contents to disk
	 */
	
	private static Logger LOGGER = LoggerFactory.getLogger(IOUtils.class);
	
	public static File writeBase64ToFile(InputStream input, String extension)
			throws IOException {

        File file = File.createTempFile(
                "file" + UUID.randomUUID(),
                "." + extension,
                new File(System.getProperty("java.io.tmpdir")));
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            copyLarge(new Base64InputStream(input), outputStream);
        } finally {
            closeQuietly(outputStream);
        }

		return file;
	}
	
	public static File writeStreamToFile(InputStream inputStream, String extension)
	throws IOException {
		File file = File.createTempFile("file" + UUID.randomUUID(), "." + extension);
		return writeStreamToFile(inputStream, extension, file);
	}

	public static File writeStreamToFile(InputStream inputStream,
			String extension, File file) throws IOException {
		FileOutputStream output = new FileOutputStream(file);
		
		byte buf[]=new byte[1024];
		int len;
		while((len=inputStream.read(buf))>0){
			output.write(buf,0,len);
		}
		output.close();
		inputStream.close();

		return file;		
	}

	public static File writeBase64XMLToFile(InputStream stream, String extension)
			throws SAXException, IOException, ParserConfigurationException,
			DOMException, TransformerException {
        
		// ToDo:  look at StAX to stream XML parsing instead of in memory DOM
		Document document = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder().parse(stream);
		String binaryContent = XPathAPI.selectSingleNode(
				document.getFirstChild(), "text()").getTextContent();

		InputStream byteStream = null;
        try {
            byteStream = new ByteArrayInputStream(binaryContent.getBytes());
            return writeBase64ToFile(byteStream, extension);
        } finally {
            closeQuietly(byteStream);
        }
	}

	/**
	 * Zip the files. Returns a zipped file and delete the specified files
	 * 
	 * @param files
	 *            files to zipped
	 * @return the zipped file
	 * @throws IOException
	 *             if the zipping process fails.
	 */
	public static File zip(File... files) throws IOException {
		File zip = File.createTempFile("zip" + UUID.randomUUID(), ".zip");

		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));

		byte[] buffer = new byte[4096];
		for (File file : files) {
			if (!file.exists()) {
				LOGGER.debug("Could not zip " + file.getAbsolutePath());
				continue;
			}

			out.putNextEntry(new ZipEntry(file.getName()));
			FileInputStream in = new FileInputStream(file);

			int len;
			while ((len = in.read(buffer)) > 0) {
				out.write(buffer, 0, len);
			}

			out.closeEntry();
			in.close();
		}

		deleteResources(files);

		out.close();

		return zip;
	}

	/**
	 * Unzip the file. Returns the unzipped file with the specified extension
	 * and deletes the zipped file
	 * 
	 * @param file
	 *            the file to unzip
	 * @param extension
	 *            the extension to search in the content files
	 * @return the file with the specified extension
	 * @throws IOException
	 *             if the unzipping process fails
	 */
	public static List<File> unzip(File file, String extension) throws IOException {
		return unzip(file, extension, null);
	}
	
	public static List<File> unzip(File file, String extension, File directory) throws IOException {
		int bufferLength = 2048;
		byte buffer[] = new byte[bufferLength];
		List<File> foundFiles = new ArrayList<File>();
		ZipInputStream zipInputStream = new ZipInputStream(
				new BufferedInputStream(new FileInputStream(file)));
		ZipEntry entry;
		File tempDir = directory;
		if (tempDir == null || !directory.isDirectory()) {
			tempDir = File.createTempFile("unzipped" + UUID.randomUUID(), "", new File(System
					.getProperty("java.io.tmpdir")));
			tempDir.delete();
			tempDir.mkdir();
		}
		while ((entry = zipInputStream.getNextEntry()) != null) {
			int count;
			File entryFile = new File(tempDir, entry.getName());
			entryFile.createNewFile();
			FileOutputStream fos = new FileOutputStream(entryFile);
			BufferedOutputStream dest = new BufferedOutputStream(fos,
					bufferLength);
			while ((count = zipInputStream.read(buffer, 0, bufferLength)) != -1) {
				dest.write(buffer, 0, count);
			}
			dest.flush();
			dest.close();

			if (entry.getName().endsWith("." + extension)) {
				foundFiles.add(entryFile);
				
			}
		}

		zipInputStream.close();

		deleteResources(file);

		return foundFiles;
	}
	
	public static List<File> unzipAll(File file) throws IOException {
		int bufferLength = 2048;
		byte buffer[] = new byte[bufferLength];
		List<File> foundFiles = new ArrayList<File>();
		ZipInputStream zipInputStream = new ZipInputStream(
				new BufferedInputStream(new FileInputStream(file)));
		ZipEntry entry;
		File tempDir = File.createTempFile("unzipped" + UUID.randomUUID(), "", new File(System
				.getProperty("java.io.tmpdir")));
		tempDir.delete();
		tempDir.mkdir();
		while ((entry = zipInputStream.getNextEntry()) != null) {
			int count;
			File entryFile = new File(tempDir, entry.getName());
			entryFile.createNewFile();
			FileOutputStream fos = new FileOutputStream(entryFile);
			BufferedOutputStream dest = new BufferedOutputStream(fos,
					bufferLength);
			while ((count = zipInputStream.read(buffer, 0, bufferLength)) != -1) {
				dest.write(buffer, 0, count);
			}
			dest.flush();
			dest.close();

			foundFiles.add(entryFile);
			
		}

		zipInputStream.close();

		deleteResources(file);

		return foundFiles;
	}

	/**
	 * Delete the given files and all the files with the same name but different
	 * extension. If some file is <code>null</code> just doesn't process it and
	 * continue to the next element of the array
	 * 
	 * @param files
	 *            the files to delete
	 */
	public static void deleteResources(File... files) {
		for (File file : files) {
			if (file != null) {
				if (file.getAbsolutePath().startsWith(
						System.getProperty("java.io.tmpdir"))) {
					delete(file);
					File parent = file.getAbsoluteFile().getParentFile();
					if (parent != null
							&& !(parent.getAbsolutePath().equals(System
									.getProperty("java.io.tmpdir")))) {
						parent.deleteOnExit();
					}
				}
			}
		}
	}

	/**
	 * Delete the given files and all the files with the same name but different
	 * extension. If some file is <code>null</code> just doesn't process it and
	 * continue to the next element of the array
	 * 
	 * @param files
	 *            the files to delete
	 */
	private static void delete(File... files) {
		for (File file : files) {
			if (file != null) {
				final String baseName = file.getName().substring(0,
						file.getName().lastIndexOf("."));
				File[] list = file.getAbsoluteFile().getParentFile().listFiles(
						new FileFilter() {
							@Override
							public boolean accept(File pathname) {
								return pathname.getName().startsWith(baseName);
							}
						});
				for (File f : list) {
					f.deleteOnExit();
				}

				file.deleteOnExit();
			}
		}
	}
}
