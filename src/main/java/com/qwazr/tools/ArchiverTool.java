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
package com.qwazr.tools;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.qwazr.library.AbstractLibrary;
import com.qwazr.utils.CharsetUtils;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.json.JsonMapper;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ArchiverTool extends AbstractLibrary {

	private static final Logger logger = LoggerFactory.getLogger(ArchiverTool.class);

	private volatile CompressorStreamFactory factory = null;

	public enum CodecType {

		deflate(CompressorStreamFactory.DEFLATE),

		gzip(CompressorStreamFactory.GZIP),

		bzip2(CompressorStreamFactory.BZIP2),

		z(CompressorStreamFactory.Z);

		private final String codecName;

		CodecType(String codecName) {
			this.codecName = codecName;
		}
	}

	public final CodecType codec;

	public ArchiverTool() {
		codec = null;
	}

	@Override
	public void load(File parentDir) {
		factory = new CompressorStreamFactory();
	}

	private InputStream getCompressorNewInputStream(InputStream input) throws IOException, CompressorException {
		if (codec == null)
			return factory.createCompressorInputStream(input);
		else
			return factory.createCompressorInputStream(codec.codecName, input);
	}

	/**
	 * Return a reader for the given file
	 *
	 * @param source  the source file
	 * @param context an optional autoclosing context
	 * @return a new reader
	 * @throws IOException
	 * @throws CompressorException
	 */
	@JsonIgnore
	public InputStreamReader getCompressorReader(File source, IOUtils.CloseableContext context)
			throws IOException, CompressorException {
		InputStream input = getCompressorNewInputStream(new BufferedInputStream(new FileInputStream(source)));
		InputStreamReader reader = new InputStreamReader(input);
		if (context != null)
			context.add(reader);
		return reader;
	}

	public void decompress(File source, File destFile) throws IOException, CompressorException {
		if (destFile.exists())
			throw new IOException("The file already exists: " + destFile.getPath());
		InputStream input = getCompressorNewInputStream(new BufferedInputStream(new FileInputStream(source)));
		try {
			IOUtils.copy(input, destFile);
		} catch (IOException e) {
			throw new IOException("Unable to decompress the file: " + source.getPath(), e);
		} finally {
			IOUtils.closeQuietly(input);
		}
	}

	/**
	 * Decompress the file as a String
	 *
	 * @param sourceFile the file to uncompress
	 * @return a string with the uncompressed content
	 * @throws IOException         related to I/O errors
	 * @throws CompressorException if any compression error occurs
	 */
	public String decompressString(File sourceFile) throws IOException, CompressorException {
		InputStream input = getCompressorNewInputStream(new BufferedInputStream(new FileInputStream(sourceFile)));
		try {
			return IOUtils.toString(input);
		} finally {
			IOUtils.closeQuietly(input);
		}
	}

	/**
	 * Decompress a JSON structure
	 *
	 * @param sourceFile
	 * @return the decompressed object
	 * @throws IOException         related to I/O errors
	 * @throws CompressorException if any compression error occurs
	 */
	public Object decompressJson(File sourceFile) throws IOException, CompressorException {
		InputStream input = getCompressorNewInputStream(new BufferedInputStream(new FileInputStream(sourceFile)));
		try {
			return JsonMapper.MAPPER.readValue(input, Object.class);
		} finally {
			IOUtils.closeQuietly(input);
		}
	}

	public void decompress_dir(File sourceDir, String sourceExtension, File destDir, String destExtension)
			throws IOException, CompressorException {
		if (!sourceDir.exists())
			throw new FileNotFoundException("The source directory does not exist: " + sourceDir.getPath());
		if (!destDir.exists())
			throw new FileNotFoundException("The destination directory does not exist: " + destDir.getPath());
		File[] sourceFiles = sourceDir.listFiles();
		if (sourceFiles == null)
			return;
		for (File sourceFile : sourceFiles) {
			if (!sourceFile.isFile())
				continue;
			String ext = FilenameUtils.getExtension(sourceFile.getName());
			if (!sourceExtension.equals(ext))
				continue;
			String newName = FilenameUtils.getBaseName(sourceFile.getName()) + '.' + destExtension;
			File destFile = new File(destDir, newName);
			if (destFile.exists())
				continue;
			decompress(sourceFile, destFile);
		}
	}

	public void decompress_dir(String sourcePath, String sourceExtension, String destPath, String destExtension)
			throws IOException, CompressorException {
		decompress_dir(new File(sourcePath), sourceExtension, new File(destPath), destExtension);
	}

	public void extract(File sourceFile, File destDir) throws IOException, ArchiveException {
		final InputStream is = new BufferedInputStream(new FileInputStream(sourceFile));
		try {
			ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream(is);
			try {
				ArchiveEntry entry;
				while ((entry = in.getNextEntry()) != null) {
					if (!in.canReadEntryData(entry))
						continue;
					if (entry.isDirectory()) {
						new File(destDir, entry.getName()).mkdir();
						continue;
					}
					if (entry instanceof ZipArchiveEntry)
						if (((ZipArchiveEntry) entry).isUnixSymlink())
							continue;
					File destFile = new File(destDir, entry.getName());
					File parentDir = destFile.getParentFile();
					if (!parentDir.exists())
						parentDir.mkdirs();
					IOUtils.copy(in, destFile);
				}
			} catch (IOException e) {
				throw new IOException("Unable to extract the archive: " + sourceFile.getPath(), e);
			} finally {
				IOUtils.closeQuietly(in);
			}
		} catch (ArchiveException e) {
			throw new ArchiveException("Unable to extract the archive: " + sourceFile.getPath(), e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	public void extract_dir(File sourceDir, String sourceExtension, File destDir, Boolean logErrorAndContinue)
			throws IOException, ArchiveException {
		if (logErrorAndContinue == null)
			logErrorAndContinue = false;
		if (!sourceDir.exists())
			throw new FileNotFoundException("The source directory does not exist: " + sourceDir.getPath());
		if (!destDir.exists())
			throw new FileNotFoundException("The destination directory does not exist: " + destDir.getPath());
		File[] sourceFiles = sourceDir.listFiles();
		if (sourceFiles == null)
			return;
		for (File sourceFile : sourceFiles) {
			if (!sourceFile.isFile())
				continue;
			String ext = FilenameUtils.getExtension(sourceFile.getName());
			if (!sourceExtension.equals(ext))
				continue;
			try {
				extract(sourceFile, destDir);
			} catch (IOException | ArchiveException e) {
				if (logErrorAndContinue)
					logger.error(e.getMessage(), e);
				else
					throw e;
			}
		}
	}

	public void extract_dir(String sourcePath, String sourceExtension, String destPath, Boolean logErrorAndContinue)
			throws IOException, ArchiveException {
		extract_dir(new File(sourcePath), sourceExtension, new File(destPath), logErrorAndContinue);
	}

	private CompressorOutputStream getCompressor(OutputStream input) throws CompressorException {
		return factory.createCompressorOutputStream(codec.codecName, input);
	}

	/**
	 * Compress a stream an write the compressed content in a file
	 *
	 * @param input    the stream to compress
	 * @param destFile the compressed file
	 * @throws CompressorException if any compression error occurs
	 * @throws IOException         if any I/O error occurs
	 */
	public void compress(InputStream input, File destFile) throws IOException, CompressorException {
		OutputStream output = getCompressor(new BufferedOutputStream(new FileOutputStream(destFile)));
		try {
			IOUtils.copy(input, output);
		} finally {
			IOUtils.closeQuietly(output);
		}
	}

	/**
	 * Compress an array of byte and write it to a file
	 *
	 * @param bytes    the bytes to compress
	 * @param destFile the compressed file
	 * @throws CompressorException if any compression error occurs
	 * @throws IOException         related to I/O errors
	 */
	public void compress(byte[] bytes, File destFile) throws CompressorException, IOException {
		InputStream input = new ByteArrayInputStream(bytes);
		try {
			compress(input, destFile);
		} finally {
			IOUtils.closeQuietly(input);
		}
	}

	/**
	 * Compress an UTF-8 string and write it to a file
	 *
	 * @param content  the text to compress
	 * @param destFile the compressed file
	 * @throws CompressorException if any compression error occurs
	 * @throws IOException         related to I/O errors
	 */
	public void compress(String content, File destFile) throws CompressorException, IOException {
		compress(CharsetUtils.encodeUtf8(content), destFile);
	}

	/**
	 * Compress the content of a file to a new file
	 *
	 * @param sourceFile the file to compress
	 * @param destFile   the compressed file
	 * @throws CompressorException if any compression error occurs
	 * @throws IOException         related to I/O errors
	 */
	public void compress(File sourceFile, File destFile) throws CompressorException, IOException {
		InputStream input = new BufferedInputStream(new FileInputStream(sourceFile));
		try {
			compress(input, destFile);
		} finally {
			IOUtils.closeQuietly(input);
		}
	}

	public void createZipArchive(Map<String, Object> sourcePaths, File zipFile) throws IOException {
		FileOutputStream fos = new FileOutputStream(zipFile);
		try {
			ZipOutputStream zos = new ZipOutputStream(fos);
			try {
				for (Map.Entry<String, Object> entry : sourcePaths.entrySet())
					addToZipFile(entry.getKey(), entry.getValue().toString(), zos);
			} finally {
				IOUtils.closeQuietly(zos);
			}
		} finally {
			IOUtils.closeQuietly(fos);
		}
	}

	public void createZipArchive(Map<String, Object> sourcePaths, String zipFilePath) throws IOException {
		createZipArchive(sourcePaths, new File(zipFilePath));
	}

	public void addToZipFile(String entryName, String filePath, ZipOutputStream zos) throws IOException {

		File srcFile = new File(filePath);
		if (!srcFile.exists())
			throw new FileNotFoundException("The file does not exists: " + srcFile.getPath());
		FileInputStream fis = new FileInputStream(srcFile);
		try {
			ZipEntry zipEntry = new ZipEntry(entryName);
			zos.putNextEntry(zipEntry);
			IOUtils.copy(fis, zos);
			zos.closeEntry();
		} finally {
			IOUtils.closeQuietly(fis);
		}
	}

	public static ContentType APPLICATION_ZIP = ContentType.create("application/zip");

	@JsonIgnore
	public static ContentType getApplicationZipContentType() {
		return APPLICATION_ZIP;
	}
}
