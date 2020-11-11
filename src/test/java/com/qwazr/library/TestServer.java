/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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
package com.qwazr.library;

import com.qwazr.server.ApplicationBuilder;
import com.qwazr.server.BaseServer;
import com.qwazr.server.GenericServer;
import com.qwazr.server.GenericServerBuilder;
import com.qwazr.server.RestApplication;
import com.qwazr.server.configuration.ServerConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

class TestServer implements BaseServer {

    final GenericServer server;
    final LibraryManager libraryManager;
    final LibraryServiceInterface libraryService;
    final Path dataDirectory;

    TestServer() throws IOException {
        dataDirectory = Files.createTempDirectory("library-test");
        final ServerConfiguration configuration =
                ServerConfiguration.of().data(dataDirectory).build();
        final GenericServerBuilder builder = GenericServer.of(configuration, null);
        final ApplicationBuilder webServices = ApplicationBuilder.of("/*").classes(RestApplication.JSON_CLASSES);
        libraryManager = new LibraryManager(configuration.dataDirectory, List.of(Paths.get("src/test/resources/etc/library.json")));
        webServices.singletons(libraryService = libraryManager.getService());
        libraryManager.getInstancesSupplier().registerInstance(LibraryServiceInterface.class, libraryService);
        builder.getWebServiceContext().jaxrs(webServices);
        builder.shutdownListener(server -> libraryManager.close());
        server = builder.build();
    }

    @Override
    public GenericServer getServer() {
        return server;
    }
}
