/**
 * Copyright 2016-2017 Emmanuel Keller / QWAZR
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
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

public class ServerTest {

	@Library("custom")
	private CustomLibrary custom;

	private static TestServer server;

	@BeforeClass
	public static void beforeClass() throws IOException {
		server = new TestServer();
	}

	@AfterClass
	public static void afterClass() throws IOException {
		if (server != null) {
			server.stop();
			server = null;
		}
	}

	@Before
	public void before() {
		server.libraryManager.inject(this);
	}

	@Test
	public void check() {
		Assert.assertNotNull(custom);
		Assert.assertTrue(custom.isLoaded());
		Assert.assertEquals(Integer.valueOf(12), custom.myParam);
	}

	@Test
	public void list() {
		Assert.assertTrue(server.localService.getLibraries().containsKey("custom"));
	}

	@Test
	public void get() {
		Assert.assertNotNull(server.localService.getLibrary("custom"));
	}
}
