package org.n52.wps.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class IOUtils {
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
		File zip = File.createTempFile("zip", ".zip");

		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));

		byte[] buffer = new byte[4096];
		for (File file : files) {
			if (!file.exists()) {
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
	public static File unzip(File file, String extension) throws IOException {
		int bufferLength = 2048;
		byte buffer[] = new byte[bufferLength];

		File ret = null;
		ZipInputStream zipInputStream = new ZipInputStream(
				new BufferedInputStream(new FileInputStream(file)));
		ZipEntry entry;
		File tempDir = File.createTempFile("unzipped", "", new File(System
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
				ret = entryFile;
			}
		}

		zipInputStream.close();

		deleteResources(file);

		return ret;
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
