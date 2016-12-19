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
package com.qwazr.library.test;

import com.qwazr.classloader.ClassLoaderManager;
import com.qwazr.library.LibraryManager;
import com.qwazr.server.BaseServer;
import com.qwazr.server.GenericServer;
import com.qwazr.server.configuration.ServerConfiguration;
import org.junit.Before;

import java.io.File;
import java.io.IOException;

abstract class AbstractLibraryTest {

	private static TestServer INSTANCE;

	@Before
	public void before() {
		if (INSTANCE == null) {
			try {
				INSTANCE = new TestServer();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		INSTANCE.libraryManager.inject(this);
	}

	private static class TestServer implements BaseServer {

		private final GenericServer server;
		private final LibraryManager libraryManager;

		private TestServer() throws IOException {
			final ServerConfiguration configuration = ServerConfiguration.of()
					.data(new File("src/test/resources"))
					.etcDirectory(new File("src/test/resources/etc"))
					.build();
			final GenericServer.Builder builder = GenericServer.of(configuration, null);
			final ClassLoaderManager classLoaderManager =
					new ClassLoaderManager(builder.getConfiguration().dataDirectory, Thread.currentThread());
			libraryManager = new LibraryManager(classLoaderManager, null, builder);
			server = builder.build();
		}

		@Override
		public GenericServer getServer() {
			return server;
		}
	}

}
