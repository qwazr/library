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
package com.qwazr.library;

import com.qwazr.utils.IOUtils;
import com.qwazr.utils.LockUtils;
import com.qwazr.utils.ReadOnlyMap;
import com.qwazr.utils.file.TrackedDirectory;
import com.qwazr.utils.file.TrackedInterface;
import com.qwazr.utils.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class LibraryManagerImpl extends ReadOnlyMap<String, AbstractLibrary>
		implements LibraryManager, TrackedInterface.FileChangeConsumer, Closeable {

	private static final Logger logger = LoggerFactory.getLogger(LibraryManagerImpl.class);

	static volatile LibraryManagerImpl INSTANCE = null;

	static synchronized void load(File dataDirectory) throws IOException {
		if (INSTANCE != null)
			throw new IOException("Already loaded");
		INSTANCE = new LibraryManagerImpl(dataDirectory);
	}

	private final File rootDirectory;
	private final File etcDirectory;

	private final TrackedDirectory directoryTracker;

	private final LockUtils.ReadWriteLock mapLock = new LockUtils.ReadWriteLock();
	private final Map<File, Map<String, AbstractLibrary>> libraryFileMap;

	private LibraryManagerImpl(File dataDirectory) throws IOException {
		this.rootDirectory = dataDirectory;
		etcDirectory = new File(rootDirectory, "etc");
		libraryFileMap = new HashMap<>();
		directoryTracker = new TrackedDirectory(this, etcDirectory);
	}

	public void load() throws IOException {
		directoryTracker.check();
	}

	@Override
	public void close() {
		mapLock.w.lock();
		try {
			libraryFileMap.clear();
			IOUtils.close(this.values());
			setMap(Collections.emptyMap());
		} finally {
			mapLock.w.unlock();
		}
	}

	public AbstractLibrary get(String name) throws IOException {
		directoryTracker.check();
		return super.get(name);
	}

	public LibraryServiceInterface getRemoteClient(String address, Integer msTimeOut) throws URISyntaxException {
		return new LibraryServiceClient(address, msTimeOut);
	}

	@Override
	public void accept(TrackedInterface.ChangeReason changeReason, File jsonFile) {
		switch (changeReason) {
		case UPDATED:
			loadLibrarySet(jsonFile);
		case DELETED:
			unloadLibrarySet(jsonFile);
		}
	}

	private void loadLibrarySet(File jsonFile) {
		if (logger.isInfoEnabled())
			logger.info("Load library configuration file: " + jsonFile.getAbsolutePath());
		try {
			final LibraryConfiguration configuration;
			configuration = JsonMapper.MAPPER.readValue(jsonFile, LibraryConfiguration.class);

			if (configuration == null || configuration.library == null)
				return;

			mapLock.w.lock();
			try {
				libraryFileMap.put(jsonFile, buildAndLoad(configuration.library));
				buildGlobalMap();
			} finally {
				mapLock.w.unlock();
			}

		} catch (IOException e) {
			if (logger.isErrorEnabled())
				logger.error(e.getMessage(), e);
			return;
		}
	}

	private void unloadLibrarySet(File jsonFile) {
		if (logger.isInfoEnabled())
			logger.info("Unload library configuration file: " + jsonFile.getAbsolutePath());
		final Map<String, AbstractLibrary> map;
		mapLock.w.lock();
		try {
			map = libraryFileMap.remove(jsonFile);
			if (map == null)
				return;
			buildGlobalMap();
		} finally {
			mapLock.w.unlock();
		}
		IOUtils.close(map.values());
	}

	private void buildGlobalMap() {
		final Map<String, AbstractLibrary> libraries = new HashMap<>();
		libraryFileMap.forEach((file, libraryMap) -> libraries.putAll(libraryMap));
		setMap(libraries);
	}

	private Map<String, AbstractLibrary> buildAndLoad(List<AbstractLibrary> libraries) throws IOException {
		final Map<String, AbstractLibrary> map = new HashMap<>();
		try {
			for (AbstractLibrary library : libraries) {
				library.load(rootDirectory);
				map.put(library.name, library);
			}
			return map;
		} catch (IOException e) {
			IOUtils.close(map.values());
			throw e;
		}
	}

}
