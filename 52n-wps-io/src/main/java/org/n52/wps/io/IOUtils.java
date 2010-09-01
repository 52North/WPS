package org.n52.wps.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.axis.encoding.Base64;
import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

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
	
	private static Logger LOGGER = Logger.getLogger(IOUtils.class);
	
	public static File writeBase64ToFile(InputStream input, String extension)
			throws IOException {
		char[] buffer = new char[4096];
		String encoded = "";
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		while (reader.read(buffer) != -1) {
			encoded += new String(buffer);
		}
		reader.close();

		File file = File.createTempFile("file"+System.currentTimeMillis(), "." + extension, new File(
				System.getProperty("java.io.tmpdir")));
		FileOutputStream output = new FileOutputStream(file);
		Base64.decode(encoded, output);
		output.close();

		return file;
	}

	public static File writeBase64XMLToFile(InputStream stream, String extension)
			throws SAXException, IOException, ParserConfigurationException,
			DOMException, TransformerException {
		// Create XML document
		Document document = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder().parse(stream);
		String binaryContent = XPathAPI.selectSingleNode(
				document.getFirstChild(), "text()").getTextContent();

		// Flush binary data to temporary file
		File file = File.createTempFile("file"+System.currentTimeMillis(), "." + extension, new File(
				System.getProperty("java.io.tmpdir")));
		FileOutputStream output = new FileOutputStream(file);
		Base64.decode(binaryContent, output);
		output.close();

		return file;
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
		File zip = File.createTempFile("zip"+System.currentTimeMillis(), ".zip");

		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));

		byte[] buffer = new byte[4096];
		for (File file : files) {
			if (!file.exists()) {
				LOGGER.debug("Colud not zip " + file.getAbsolutePath());
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
		int bufferLength = 2048;
		byte buffer[] = new byte[bufferLength];
		List<File> foundFiles = new ArrayList<File>();
		ZipInputStream zipInputStream = new ZipInputStream(
				new BufferedInputStream(new FileInputStream(file)));
		ZipEntry entry;
		File tempDir = File.createTempFile("unzipped"+System.currentTimeMillis(), "", new File(System
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

			if (entry.getName().endsWith("." + extension)) {
				foundFiles.add(entryFile);
				
			}
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
						parent.delete();
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
					f.delete();
				}

				file.delete();
			}
		}
	}
}
