/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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
 */
package com.qwazr.library.test;

import com.qwazr.library.LibraryManager;
import com.qwazr.library.LibraryServiceInterface;
import com.qwazr.server.ApplicationBuilder;
import com.qwazr.server.BaseServer;
import com.qwazr.server.GenericServer;
import com.qwazr.server.RestApplication;
import com.qwazr.server.configuration.ServerConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

class TestServer implements BaseServer {

	final GenericServer server;
	final LibraryManager libraryManager;
	final File dataDirectory;
	final LibraryServiceInterface localService;

	TestServer() throws IOException {
		dataDirectory = Files.createTempDirectory("library-test").toFile();
		final ServerConfiguration configuration =
				ServerConfiguration.of().data(dataDirectory).etcDirectory(new File("src/test/resources/etc")).build();
		final GenericServer.Builder builder = GenericServer.of(configuration, null);
		final ApplicationBuilder webServices = ApplicationBuilder.of("/*").classes(RestApplication.JSON_CLASSES);
		libraryManager =
				new LibraryManager(configuration.dataDirectory, configuration.getEtcFiles()).registerIdentityManager(
						builder).registerContextAttribute(builder).registerWebService(webServices);
		localService = libraryManager.getService();
		libraryManager.getInstancesSupplier().registerInstance(LibraryServiceInterface.class, localService);
		builder.getWebServiceContext().jaxrs(webServices);
		server = builder.build();
	}

	@Override
	public GenericServer getServer() {
		return server;
	}
}
