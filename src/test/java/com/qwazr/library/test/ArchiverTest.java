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

import com.qwazr.library.LibraryManager;
import com.qwazr.library.annotations.Library;
import com.qwazr.tools.ArchiverTool;
import com.qwazr.utils.IOUtils;
import org.apache.commons.compress.compressors.CompressorException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ArchiverTest extends AbstractLibraryTest {

	@Library("archiver")
	private ArchiverTool archiver;

	@Before
	public void before() throws IOException {
		super.before();
		LibraryManager.inject(this);
	}

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
}
