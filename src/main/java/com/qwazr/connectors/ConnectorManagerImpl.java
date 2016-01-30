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
package com.qwazr.connectors;

import com.qwazr.utils.IOUtils;
import com.qwazr.utils.ReadOnlyMap;
import com.qwazr.utils.TrackedFile;
import com.qwazr.utils.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ConnectorManagerImpl extends ReadOnlyMap<String, AbstractConnector>
				implements ConnectorManager, TrackedFile.FileEventReceiver {

	private static final Logger logger = LoggerFactory.getLogger(ConnectorManagerImpl.class);

	private static volatile ConnectorManagerImpl INSTANCE = null;

	public static void load(File directory) throws IOException {
		if (INSTANCE != null)
			throw new IOException("Already loaded");
		INSTANCE = new ConnectorManagerImpl(directory);
	}

	final public static ConnectorManager getInstance() {
		return INSTANCE;
	}

	private final File rootDirectory;
	private final File connectorsFile;
	private final TrackedFile trackedFile;

	private ConnectorManagerImpl(File rootDirectory) throws IOException {
		this.rootDirectory = rootDirectory;
		connectorsFile = new File(rootDirectory, "connectors.json");
		trackedFile = new TrackedFile(this, connectorsFile);
		trackedFile.check();
	}

	public synchronized void load() throws IOException {
		final Map<String, AbstractConnector> connectorMap = new HashMap<>();
		logger.info("Loading connectors configuration file: " + connectorsFile.getAbsolutePath());
		ConnectorsConfiguration configuration = JsonMapper.MAPPER
						.readValue(connectorsFile, ConnectorsConfiguration.class);
		if (configuration.connectors != null) {
			for (AbstractConnector connector : configuration.connectors) {
				logger.info("Loading connector: " + connector.name);
				connector.load(rootDirectory);
				connectorMap.put(connector.name, connector);
			}
		}
		reload(connectorMap);
	}

	public synchronized void unload() {
		reload(Collections.emptyMap());
	}

	private void reload(final Map<String, AbstractConnector> newMap) {
		Map<String, AbstractConnector> oldMap = setMap(newMap);
		if (oldMap != null)
			IOUtils.close(oldMap.values());
	}

	public AbstractConnector get(String name) throws IOException {
		trackedFile.check();
		return super.get(name);
	}
	
}
