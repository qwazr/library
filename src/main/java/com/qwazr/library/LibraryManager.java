/**
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
 **/
package com.qwazr.library;

import com.qwazr.classloader.ClassLoaderManager;
import com.qwazr.database.TableServiceInterface;
import com.qwazr.library.annotations.Library;
import com.qwazr.server.GenericServer;
import com.qwazr.utils.AnnotationsUtils;
import com.qwazr.utils.ClassLoaderUtils;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.LockUtils;
import com.qwazr.utils.ReadOnlyMap;
import com.qwazr.utils.json.JsonMapper;
import io.undertow.security.idm.IdentityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class LibraryManager extends ReadOnlyMap<String, LibraryInterface>
		implements Map<String, LibraryInterface>, GenericServer.IdentityManagerProvider, ClassLoaderUtils.ClassFactory,
		Closeable {

	private static final Logger LOGGER = LoggerFactory.getLogger(LibraryManager.class);

	private final File dataDirectory;
	private final ClassLoaderManager classLoaderManager;
	private final LibraryServiceInterface service;
	private final TableServiceInterface tableService;

	private final LockUtils.ReadWriteLock mapLock = new LockUtils.ReadWriteLock();
	private final Map<File, Map<String, LibraryInterface>> libraryFileMap;

	public LibraryManager(final ClassLoaderManager classLoaderManager, final TableServiceInterface tableService,
			final File dataDirectory, final Collection<File> etcFiles) throws IOException {
		this.classLoaderManager = classLoaderManager;
		this.dataDirectory = dataDirectory;
		this.service = new LibraryServiceImpl(this);
		this.tableService = tableService;
		this.libraryFileMap = new HashMap<>();

		if (classLoaderManager != null)
			classLoaderManager.register(this);

		if (etcFiles != null)
			etcFiles.forEach(this::loadLibrarySet);
	}

	public LibraryManager registerWebService(final GenericServer.Builder builder) {
		registerContextAttribute(builder);
		builder.webService(LibraryServiceImpl.class);
		return this;
	}

	public LibraryManager registerIdentityManager(final GenericServer.Builder builder) {
		builder.identityManagerProvider(this);
		return this;
	}

	public LibraryManager registerContextAttribute(final GenericServer.Builder builder) {
		builder.contextAttribute(this);
		return this;
	}

	final public LibraryServiceInterface getService() {
		return service;
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

	final public File getDataDirectory() {
		return dataDirectory;
	}

	final public ClassLoaderManager getClassLoaderManager() {
		return classLoaderManager;
	}

	final public TableServiceInterface getTableService() {
		return tableService;
	}

	public Map<String, String> getLibraries() {
		final Map<String, String> map = new LinkedHashMap<>();
		this.forEach((name, library) -> map.put(name, library.getClass().getName()));
		return map;
	}

	/**
	 * Create a new object using a public empty constructor and inject the library objects in the annotated properties
	 *
	 * @param clazz
	 * @param <T>
	 * @return
	 * @throws ReflectiveOperationException
	 */
	final public <T> T newInstance(final Class<T> clazz) throws ReflectiveOperationException {
		final T instance = clazz.newInstance();
		inject(instance);
		return instance;
	}

	/**
	 * Inject the library objects in the annotated properties
	 *
	 * @param object
	 */
	final public void inject(final Object object) {
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
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		});
	}

	/**
	 * Return the LibraryManager instance from the ServletContext
	 *
	 * @param context
	 * @return
	 */
	static public LibraryManager getInstance(final ServletContext context) {
		Objects.requireNonNull(context, "Cannot find a Library instance, the context is null");
		return GenericServer.getContextAttribute(context, LibraryManager.class);
	}

	/**
	 * Inject the library object in the annotated property of the given object.
	 * <p>
	 * The LibraryManager instance is extracted from the ServletContext
	 *
	 * @param context
	 * @param object
	 */
	static public void inject(final ServletContext context, final Object object) {
		if (object == null)
			return;
		final LibraryManager libraryManager = getInstance(context);
		Objects.requireNonNull(libraryManager, "No library manager found in this context");
		libraryManager.inject(object);
	}

	private void loadLibrarySet(final File jsonFile) {
		try {
			final LibraryConfiguration configuration =
					JsonMapper.MAPPER.readValue(jsonFile, LibraryConfiguration.class);

			if (configuration == null || configuration.library == null) {
				unloadLibrarySet(jsonFile);
				return;
			}

			if (LOGGER.isInfoEnabled())
				LOGGER.info("Load library configuration file: " + jsonFile.getAbsolutePath());

			mapLock.writeEx(() -> {
				configuration.library.values().forEach((library) -> {
					try {
						library.load(this);
						library.load();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				libraryFileMap.put(jsonFile, configuration.library);
				buildGlobalMap();
			});

		} catch (IOException e) {
			if (LOGGER.isErrorEnabled())
				LOGGER.error(e.getMessage(), e);
		}
	}

	private void unloadLibrarySet(File jsonFile) {
		mapLock.write(() -> {
			final Map<String, LibraryInterface> map = libraryFileMap.remove(jsonFile);
			if (map == null)
				return;
			if (LOGGER.isInfoEnabled())
				LOGGER.info("Unload library configuration file: " + jsonFile.getAbsolutePath());
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
	public IdentityManager getIdentityManager(final String realm) throws IOException {
		final LibraryInterface library = get(realm);
		if (library == null)
			throw new IOException("No realm connector with this name: " + realm);
		if (!(library instanceof IdentityManager))
			throw new IOException("This is a not a realm connector: " + realm);
		return (IdentityManager) library;
	}

}
