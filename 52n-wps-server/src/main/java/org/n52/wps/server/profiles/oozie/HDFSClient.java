/*
 This file is a modification of the free code given on the link below:
  http://linuxjunkies.wordpress.com/

 2011
 */
package org.n52.wps.server.profiles.oozie;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PrivilegedExceptionAction;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.hdfs.protocol.DatanodeInfo;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.log4j.Logger;

public class HDFSClient {

	// HDFS System configuration files
	private Logger log = Logger.getLogger(getClass());
	private String hdfsURL;
	private String user;
	private Configuration configuration;

	/**
	 * If needed, creates the home directory for the user because Oozie expects
	 * this directory to exist.
	 * 
	 * @param userId
	 *            user id for which the home directory is checked (and created).
	 * @throws Exception
	 */
	public void createHomeDirectoryIfNeeded(final String userId)
			throws Exception {

		try {
			final FileSystem fs = FileSystem.get(this.getConfiguration());
			UserGroupInformation ugi = UserGroupInformation
					.createRemoteUser(this.getUser());
			log.debug("Creating home directory of user "+userId+" using privilege of user "+this.getUser());
			ugi.doAs(new PrivilegedExceptionAction<Void>() {

				public Void run() throws Exception {

					org.apache.hadoop.fs.Path homeDir = new org.apache.hadoop.fs.Path(
							"/user/" + userId);
					if (!fs.exists(homeDir)) {
						fs.mkdirs(homeDir);
						fs.setOwner(homeDir, userId, userId);
					}

					return null;

				}
			});
		} catch (Exception ex) {
			log.error(
					"Error occured while trying to create the Oozie home directory: "
							+ ex.getMessage(), ex);
			throw new Exception(
					"Error occured while trying to create the Oozie home directory: "
							+ ex.getMessage(), null);
		}

	}

	public static void main(String[] args) throws IOException,
			InterruptedException {
		HDFSClient client = new HDFSClient("hdfs://quickstart.cloudera:8020",
				"hdfs");
		String deployDirectory = "/user/christophe/deploy";

		// Deployment directory (definition)
		String archiveDir = deployDirectory + File.separator + "test3"
				+ File.separator;
		// Creation of deployment directory
		client.mkdir(archiveDir);

		try {
			client.createHomeDirectoryIfNeeded("pouet");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Read the zip archive

	}

	public HDFSClient(String url, String hdfsAdmin) {
		this.setHdfsURL(url);
		this.setUser(hdfsAdmin);
		System.setProperty("HADOOP_USER_NAME", "hdfs");
		Configuration conf = new Configuration();
		//conf.set("user.name", hdfsAdmin);
		//conf.set("hadoop.job.ugi", "hdfs");
		conf.set("fs.defaultFS", url);
		conf.set("fs.trash.interval", "1");
		conf.set("io.file.buffer.size", "65536");
		conf.set("dfs.namenode.servicerpc-address", "192.168.56.101:8022");
		conf.set("dfs.https.address", "192.168.56.101:50470");
		conf.set("dfs.https.port", "50470");
		conf.set("dfs.namenode.http-address", "192.168.56.101:50070");
		conf.set("dfs.blocksize", "134217728");
		conf.set("dfs.replication", "3");
		conf.set("dfs.client.use.datanode.hostname", "true");

		this.setConfiguration(conf);
	}

	public static void printUsage() {
		System.out
				.println("Usage: hdfsclient add" + "<local_path> <hdfs_path>");
		System.out.println("Usage: hdfsclient read" + "<hdfs_path>");
		System.out.println("Usage: hdfsclient delete" + "<hdfs_path>");
		System.out.println("Usage: hdfsclient mkdir" + "<hdfs_path>");
		System.out.println("Usage: hdfsclient copyfromlocal"
				+ "<local_path> <hdfs_path>");
		System.out.println("Usage: hdfsclient copytolocal"
				+ " <hdfs_path> <local_path> ");
		System.out
				.println("Usage: hdfsclient modificationtime" + "<hdfs_path>");
		System.out.println("Usage: hdfsclient getblocklocations"
				+ "<hdfs_path>");
		System.out.println("Usage: hdfsclient gethostnames");
	}

	public boolean ifExists(Path source) throws IOException {

		FileSystem hdfs = FileSystem.get(this.getConfiguration());
		boolean isExists = hdfs.exists(source);
		return isExists;
	}

	public void getHostnames() throws IOException {
		FileSystem fs = FileSystem.get(this.getConfiguration());
		DistributedFileSystem hdfs = (DistributedFileSystem) fs;
		DatanodeInfo[] dataNodeStats = hdfs.getDataNodeStats();

		String[] names = new String[dataNodeStats.length];
		for (int i = 0; i < dataNodeStats.length; i++) {
			names[i] = dataNodeStats[i].getHostName();
			System.out.println((dataNodeStats[i].getHostName()));
		}
	}

	public void getBlockLocations(String source) throws IOException {

		FileSystem fileSystem = FileSystem.get(this.getConfiguration());
		Path srcPath = new Path(source);

		// Check if the file already exists
		if (!(ifExists(srcPath))) {
			System.out.println("No such destination " + srcPath);
			return;
		}
		// Get the filename out of the file path
		String filename = source.substring(source.lastIndexOf('/') + 1,
				source.length());

		FileStatus fileStatus = fileSystem.getFileStatus(srcPath);

		BlockLocation[] blkLocations = fileSystem.getFileBlockLocations(
				fileStatus, 0, fileStatus.getLen());
		int blkCount = blkLocations.length;

		System.out.println("File :" + filename + "stored at:");
		for (int i = 0; i < blkCount; i++) {
			String[] hosts = blkLocations[i].getHosts();
			System.out.format("Host %d: %s %n", i, hosts);
		}

	}

	public void getModificationTime(String source) throws IOException {

		FileSystem fileSystem = FileSystem.get(this.getConfiguration());
		Path srcPath = new Path(source);

		// Check if the file already exists
		if (!(fileSystem.exists(srcPath))) {
			System.out.println("No such destination " + srcPath);
			return;
		}
		// Get the filename out of the file path
		String filename = source.substring(source.lastIndexOf('/') + 1,
				source.length());

		FileStatus fileStatus = fileSystem.getFileStatus(srcPath);
		long modificationTime = fileStatus.getModificationTime();

		System.out.format("File %s; Modification time : %0.2f %n", filename,
				modificationTime);

	}

	public Configuration getConfiguration() {
		return this.configuration;
	}

	public void writeFile(final InputStream in, final String dest)
			throws IOException, InterruptedException {
		final Path path = new Path(dest);
		log.debug("Writing file on HDFS by using privilege of user "+this.getUser());
		UserGroupInformation ugi = UserGroupInformation.createRemoteUser(this
				.getUser());
		final FileSystem fileSystem = FileSystem.get(this.getConfiguration());
		/*
		 * // Create the destination path including the filename. if
		 * (dest.charAt(dest.length() - 1) != '/') { dest = dest + "/" +
		 * filename; } else { dest = dest + filename; }
		 * 
		 * // Check if the file already exists
		 * 
		 * if (fileSystem.exists(path)) { System.out.println("File " + dest +
		 * " already exists"); return; } // if parent directory does not exist
		 * if (path.getParent() != null && !fileSystem.exists(path.getParent()))
		 * { log.debug("MKDIR in writeFile "+path.getParent());
		 * fileSystem.mkdirs(path.getParent()); }
		 */
		// Create a new file and write data to it.
		ugi.doAs(new PrivilegedExceptionAction<Void>() {
			public Void run() throws Exception {
				FSDataOutputStream out;
				log.debug("create file system file:" + path);
				out = fileSystem.create(path);
				byte[] b = new byte[1024];
				int numBytes = 0;
				while ((numBytes = in.read(b)) > 0) {
					out.write(b, 0, numBytes);
				}

				// Close all the file descripters
				// in.close();
				out.close();
				log.debug("closing fs");
				fileSystem.close();
				return null;
			}
		});
	}

	public void copyFromLocal(String source, String dest) throws IOException {

		FileSystem fileSystem = FileSystem.get(this.getConfiguration());
		Path srcPath = new Path(source);

		Path dstPath = new Path(dest);
		// Check if the file already exists
		if (!(fileSystem.exists(dstPath))) {
			System.out.println("No such destination " + dstPath);
			return;
		}

		// Get the filename out of the file path
		String filename = source.substring(source.lastIndexOf('/') + 1,
				source.length());

		try {
			fileSystem.copyFromLocalFile(srcPath, dstPath);
			System.out.println("File " + filename + "copied to " + dest);
		} catch (Exception e) {
			System.err.println("Exception caught! :" + e);
			System.exit(1);
		} finally {
			fileSystem.close();
		}
	}

	public void copyToLocal(String source, String dest) throws IOException {

		FileSystem fileSystem = FileSystem.get(this.getConfiguration());
		Path srcPath = new Path(source);

		Path dstPath = new Path(dest);
		// Check if the file already exists
		if (!(fileSystem.exists(srcPath))) {
			System.out.println("No such destination " + srcPath);
			return;
		}

		// Get the filename out of the file path
		String filename = source.substring(source.lastIndexOf('/') + 1,
				source.length());

		try {
			fileSystem.copyToLocalFile(srcPath, dstPath);
			System.out.println("File " + filename + "copied to " + dest);
		} catch (Exception e) {
			System.err.println("Exception caught! :" + e);
			System.exit(1);
		} finally {
			fileSystem.close();
		}
	}

	public void renameFile(String fromthis, String tothis) throws IOException {

		FileSystem fileSystem = FileSystem.get(this.getConfiguration());
		Path fromPath = new Path(fromthis);
		Path toPath = new Path(tothis);

		if (!(fileSystem.exists(fromPath))) {
			System.out.println("No such destination " + fromPath);
			return;
		}

		if (fileSystem.exists(toPath)) {
			System.out.println("Already exists! " + toPath);
			return;
		}

		try {
			boolean isRenamed = fileSystem.rename(fromPath, toPath);
			if (isRenamed) {
				System.out.println("Renamed from " + fromthis + "to " + tothis);
			}
		} catch (Exception e) {
			System.out.println("Exception :" + e);
			System.exit(1);
		} finally {
			fileSystem.close();
		}

	}

	public void addFile(String source, String dest) throws IOException {

		FileSystem fileSystem = FileSystem.get(this.getConfiguration());

		// Get the filename out of the file path
		String filename = source.substring(source.lastIndexOf('/') + 1,
				source.length());

		// Create the destination path including the filename.
		if (dest.charAt(dest.length() - 1) != '/') {
			dest = dest + "/" + filename;
		} else {
			dest = dest + filename;
		}

		// Check if the file already exists
		Path path = new Path(dest);
		if (fileSystem.exists(path)) {
			System.out.println("File " + dest + " already exists");
			return;
		}

		// Create a new file and write data to it.
		FSDataOutputStream out = fileSystem.create(path);
		InputStream in = new BufferedInputStream(new FileInputStream(new File(
				source)));

		byte[] b = new byte[1024];
		int numBytes = 0;
		while ((numBytes = in.read(b)) > 0) {
			out.write(b, 0, numBytes);
		}

		// Close all the file descripters
		in.close();
		out.close();
		fileSystem.close();
	}

	public void readFile(String file) throws IOException {

		FileSystem fileSystem = FileSystem.get(this.getConfiguration());

		Path path = new Path(file);
		if (!fileSystem.exists(path)) {
			System.out.println("File " + file + " does not exists");
			return;
		}

		FSDataInputStream in = fileSystem.open(path);

		String filename = file.substring(file.lastIndexOf('/') + 1,
				file.length());

		OutputStream out = new BufferedOutputStream(new FileOutputStream(
				new File(filename)));

		byte[] b = new byte[1024];
		int numBytes = 0;
		while ((numBytes = in.read(b)) > 0) {
			out.write(b, 0, numBytes);
		}

		in.close();
		out.close();
		fileSystem.close();
	}

	public void deleteFile(final String file) throws IOException,
			InterruptedException {
		log.debug("Delete file using the privilege of user "+this.getUser());
		final FileSystem fileSystem = FileSystem.get(this.getConfiguration());
		final Path path = new Path(file);
		UserGroupInformation ugi = UserGroupInformation.createRemoteUser(this
				.getUser());
		ugi.doAs(new PrivilegedExceptionAction<Void>() {
			public Void run() throws Exception {

				if (!fileSystem.exists(path)) {
					System.out.println("File " + file + " does not exists");
					return null;
				}
				fileSystem.delete(new Path(file), true);
				fileSystem.close();
				return null;
			}
		});
	}

	/**
	 * Configured user (admin) creates a directory
	 * 
	 * @param dir
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void mkdir(final String dir) throws IOException,
			InterruptedException {
		log.debug("MKDIR " + dir);
		UserGroupInformation ugi = UserGroupInformation.createRemoteUser(this
				.getUser());

		final FileSystem fileSystem = FileSystem.get(this.getConfiguration());

		ugi.doAs(new PrivilegedExceptionAction<Void>() {

			public Void run() throws Exception {

				Path path = new Path(dir);
				if (fileSystem.exists(path)) {
					System.out.println("Dir " + dir + " already exists!");
					return null;
				}
				fileSystem.mkdirs(path);
				fileSystem.close();
				return null;
			}
		});

	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getHdfsURL() {
		return hdfsURL;
	}

	public void setHdfsURL(String hdfsURL) {
		this.hdfsURL = hdfsURL;
	}

}
