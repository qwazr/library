/**
 * Copyright 2016 Emmanuel Keller / QWAZR
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
package com.qwazr.library.test;

import com.qwazr.library.annotations.Library;
import com.qwazr.tools.ArchiverTool;
import com.qwazr.utils.IOUtils;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.compressors.CompressorException;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ArchiverTest extends AbstractLibraryTest {

	@Library("archiver")
	private ArchiverTool archiver;

	@Library("gzip_archiver")
	private ArchiverTool gzipArchiver;

	private final static String TEST_STRING = "TEST_COMPRESSION";

	@Test
	public void compressDecompress() throws CompressorException, IOException {
		Assert.assertNotNull(archiver);
		File zipFile = Files.createTempFile("archiverToolTest", ".zip").toFile();
		archiver.compress(TEST_STRING, zipFile);
		Assert.assertEquals(TEST_STRING, archiver.decompressString(zipFile));
		File clearFile = Files.createTempFile("archiverToolTest", ".txt").toFile();
		archiver.decompress(zipFile, clearFile);
		Assert.assertEquals(TEST_STRING, IOUtils.readFileAsString(clearFile));
	}

	@Test
	public void extractDir() throws IOException, ArchiveException, CompressorException {
		File destDir = Files.createTempDirectory("archiverToolTest").toFile();

		Assert.assertNotNull(gzipArchiver);
		gzipArchiver.decompress_dir("src/test/resources/com/qwazr/library/test/archiver", "gz",
				destDir.getAbsolutePath());
		Assert.assertTrue(new File(destDir, "test1.tar").exists());
		Assert.assertTrue(new File(destDir, "test2.tar").exists());

		Assert.assertNotNull(archiver);
		archiver.extract_dir(destDir.getPath(), "tar", destDir.getAbsolutePath(), false);
		Assert.assertTrue(new File(destDir, "test1").exists());
		Assert.assertTrue(new File(destDir, "test2").exists());
	}
}
