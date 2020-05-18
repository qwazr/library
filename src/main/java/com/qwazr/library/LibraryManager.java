/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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

import com.qwazr.library.annotations.Library;
import com.qwazr.server.GenericServer;
import com.qwazr.utils.AnnotationsUtils;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.ObjectMappers;
import com.qwazr.utils.ReadOnlyMap;
import com.qwazr.utils.concurrent.ReadWriteLock;
import com.qwazr.utils.reflection.InstancesSupplier;
import io.undertow.security.idm.IdentityManager;

import javax.ws.rs.InternalServerErrorException;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LibraryManager extends ReadOnlyMap<String, LibraryInterface>
        implements Map<String, LibraryInterface>, GenericServer.IdentityManagerProvider, Closeable {

    private static final Logger LOGGER = LoggerUtils.getLogger(LibraryManager.class);

    private final Path dataDirectory;
    private final LibraryServiceInterface service;
    private final InstancesSupplier instancesSupplier;

    private final ReadWriteLock mapLock;
    private final Map<Path, Map<String, LibraryInterface>> libraryFileMap;

    public LibraryManager(final Path dataDirectory, final Collection<Path> etcFiles,
                          final InstancesSupplier instancesSupplier) {
        this.dataDirectory = dataDirectory;
        this.service = new LibraryServiceImpl(this);
        this.libraryFileMap = new HashMap<>();
        this.mapLock = ReadWriteLock.stamped();
        this.instancesSupplier = instancesSupplier == null ? InstancesSupplier.withConcurrentMap() : instancesSupplier;
        if (etcFiles != null)
            etcFiles.forEach(this::loadLibrarySet);
    }

    public LibraryManager(final Path dataDirectory, final Collection<Path> etcFiles) {
        this(dataDirectory, etcFiles, null);
    }

    final public LibraryServiceInterface getService() {
        return service;
    }

    final public InstancesSupplier getInstancesSupplier() {
        return instancesSupplier;
    }

    @Override
    public void close() {
        mapLock.write(() -> {
            libraryFileMap.clear();
            IOUtils.closeObjects(this.values());
            setMap(Collections.emptyMap());
        });
    }

    final public <T extends LibraryInterface> T getLibrary(final String name) {
        return (T) super.get(name);
    }

    final public Path getDataDirectory() {
        return dataDirectory;
    }

    public Map<String, String> getLibraries() {
        final Map<String, String> map = new LinkedHashMap<>();
        this.forEach((name, library) -> map.put(name, library.getClass().getName()));
        return map;
    }

    /**
     * Inject the library objects in the annotated properties
     *
     * @param object the class instance to inject
     */
    final void inject(final Object object) {
        if (object == null)
            return;
        AnnotationsUtils.browseFieldsRecursive(object.getClass(), field -> {
            final Library library = field.getAnnotation(Library.class);
            if (library == null)
                return;
            final LibraryInterface libraryItem = getLibrary(library.value());
            if (libraryItem == null)
                return;
            field.setAccessible(true);
            try {
                field.set(object, libraryItem);
            }
            catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void loadLibrarySet(final Path jsonFile) {
        try {
            final LibraryConfiguration configuration =
                    ObjectMappers.JSON.readValue(jsonFile.toFile(), LibraryConfiguration.class);

            if (configuration == null || configuration.library == null) {
                unloadLibrarySet(jsonFile);
                return;
            }

            LOGGER.info(() -> "Load library configuration file: " + jsonFile.toAbsolutePath());

            mapLock.writeEx(() -> {
                configuration.library.values().forEach((library) -> {
                    try {
                        library.load(this);
                        library.load();
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                libraryFileMap.put(jsonFile, configuration.library);
                buildGlobalMap();
            });

        }
        catch (IOException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Cannot load the file: " + jsonFile);
        }
    }

    private void unloadLibrarySet(Path jsonFile) {
        mapLock.write(() -> {
            final Map<String, LibraryInterface> map = libraryFileMap.remove(jsonFile);
            if (map == null)
                return;
            LOGGER.info(() -> "Unload library configuration file: " + jsonFile.toAbsolutePath());
            buildGlobalMap();
            IOUtils.closeObjects(map.values());
        });
    }

    private void buildGlobalMap() {
        final Map<String, LibraryInterface> libraries = new HashMap<>();
        libraryFileMap.forEach((file, libraryMap) -> libraries.putAll(libraryMap));
        setMap(libraries);
    }

    @Override
    public IdentityManager getIdentityManager(final String realm) {
        final LibraryInterface library = get(realm);
        if (library == null)
            return null;
        if (!(library instanceof IdentityManager))
            throw new InternalServerErrorException("This is a not a realm connector: " + realm);
        return (IdentityManager) library;
    }

}
