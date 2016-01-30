/**
 * Copyright 2014-2016 Emmanuel Keller / QWAZR
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package com.qwazr.connectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.qwazr.library.AbstractLibrary;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class HdfsConnector extends AbstractLibrary {

	private static final Logger logger = LoggerFactory.getLogger(HdfsConnector.class);

	public final String config_path = null;

	public final List<String> config_files = null;

	@JsonIgnore
	private FileSystem fileSystem;

	@JsonIgnore
	private Configuration configuration;

	@Override
	public void load(File data_directory) {
		configuration = new Configuration();

		try {
			if (config_files != null) {
				for (String configFile : config_files) {
					File file = new File(config_path, configFile);
					if (!file.exists())
						throw new IOException("Configuration file not found: " + file.getAbsolutePath());
					configuration.addResource(new Path(config_path, configFile));
				}
			}
			configuration.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
			configuration.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
			logger.info("*** HDFS configuration ***: " + configuration.toString());
			fileSystem = FileSystem.get(configuration);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() {
		if (fileSystem != null) {
			IOUtils.close(fileSystem);
			fileSystem = null;
		}
	}

	private void checkFileSystem() throws IOException {
		if (fileSystem == null)
			throw new IOException("No filesystem available");
	}

	public void write(Path path, String content) throws IOException {
		checkFileSystem();
		if (content == null || content.length() == 0)
			throw new IOException("No content");
		logger.info("Writing text: " + path);
		FSDataOutputStream out = fileSystem.create(path, true);
		try {
			out.writeUTF(content);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	public void write(String pathString, String content) throws IOException {
		write(new Path(pathString), content);
	}

	public void write(Path path, InputStream in) throws IOException {
		checkFileSystem();
		if (in == null)
			throw new IOException("No input stream");
		logger.info("Writing stream: " + path);
		FSDataOutputStream out = fileSystem.create(path, true);
		try {
			IOUtils.copy(in, out);
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
	}

	public void write(String pathString, InputStream in) throws IOException {
		write(new Path(pathString), in);
	}

	public String readUTF(Path path) throws IOException {
		checkFileSystem();
		if (logger.isInfoEnabled())
			logger.info("readUTF: " + path);
		FSDataInputStream in = fileSystem.open(path);
		try {
			return in.readUTF();
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	public String readUTF(String pathString) throws IllegalArgumentException, IOException {
		return readUTF(new Path(pathString));
	}

	public File readAsFile(Path path, File localFile) throws IOException {
		checkFileSystem();
		if (logger.isInfoEnabled())
			logger.info("readAsFile: " + path);
		FSDataInputStream in = fileSystem.open(path);
		try {
			IOUtils.copy(in, localFile);
			return localFile;
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	public File readAsFile(String pathString, File localFile) throws IllegalArgumentException, IOException {
		return readAsFile(new Path(pathString), localFile);
	}

	public File readAsTempFile(Path path, String fileSuffix) throws IOException {
		File localFile = File
				.createTempFile("qwazr-hdfs-connector", fileSuffix == null ? StringUtils.EMPTY : fileSuffix);
		return readAsFile(path, localFile);
	}

	public File readAsTempFile(String pathString, String fileSuffix) throws IOException {
		return readAsTempFile(new Path(pathString), fileSuffix);
	}

	public boolean exists(Path path) throws IOException {
		checkFileSystem();
		if (logger.isInfoEnabled())
			logger.info("Check path exist: " + path.toString());
		return fileSystem.exists(path);
	}

	public boolean exists(String pathString) throws IOException {
		return exists(new Path(pathString));
	}

	public boolean mkdir(Path path) throws IOException {
		checkFileSystem();
		if (logger.isInfoEnabled())
			logger.info("Create dir: " + path.toString());
		return fileSystem.mkdirs(path);
	}

	public boolean mkdir(String pathString) throws IOException {
		return fileSystem.mkdirs(new Path(pathString));
	}

	public FileStatus[] listStatus(Path path) throws IOException {
		checkFileSystem();
		if (logger.isInfoEnabled())
			logger.info("List dir: " + path.toString());
		return fileSystem.listStatus(path);
	}

	public FileStatus[] listStatus(String pathString) throws IOException {
		checkFileSystem();
		return listStatus(new Path(pathString));
	}

	public boolean delete(Path path, boolean recursive) throws IOException {
		checkFileSystem();
		if (logger.isInfoEnabled())
			logger.info("Delete path: " + path.toString());
		return fileSystem.delete(path, recursive);
	}

	public boolean delete(String pathString, boolean recursive) throws IOException {
		return delete(new Path(pathString), recursive);
	}

}
