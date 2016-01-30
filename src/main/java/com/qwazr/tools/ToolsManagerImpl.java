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
package com.qwazr.tools;

import com.qwazr.utils.IOUtils;
import com.qwazr.utils.ReadOnlyMap;
import com.qwazr.utils.TrackedFile;
import com.qwazr.utils.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ToolsManagerImpl extends ReadOnlyMap<String, AbstractTool>
				implements ToolsManager, TrackedFile.FileEventReceiver {

	private static final Logger logger = LoggerFactory.getLogger(ToolsManagerImpl.class);

	private static volatile ToolsManagerImpl INSTANCE = null;

	public static void load(File dataDirectory) throws IOException {
		if (INSTANCE != null)
			throw new IOException("Already loaded");
		INSTANCE = new ToolsManagerImpl(dataDirectory);
	}

	final public static ToolsManager getInstance() {
		return INSTANCE;
	}

	private final File rootDirectory;
	private final File toolsFile;
	private final TrackedFile trackedFile;

	private ToolsManagerImpl(File dataDirectory) throws IOException {
		this.rootDirectory = dataDirectory;
		toolsFile = new File(rootDirectory, "tools.json");
		trackedFile = new TrackedFile(this, toolsFile);
		trackedFile.check();
	}

	public synchronized void load() throws IOException {
		final Map<String, AbstractTool> toolMap = new HashMap<>();
		logger.info("Loading tools configuration file: " + toolsFile.getAbsolutePath());
		ToolsConfiguration configuration = JsonMapper.MAPPER.readValue(toolsFile, ToolsConfiguration.class);
		if (configuration.tools != null) {
			for (AbstractTool tool : configuration.tools) {
				logger.info("Loading tool: " + tool.name);
				tool.load(rootDirectory);
				toolMap.put(tool.name, tool);
			}
		}
		reload(toolMap);
	}

	public synchronized void unload() {
		reload(Collections.emptyMap());
	}

	private void reload(final Map<String, AbstractTool> newMap) {
		Map<String, AbstractTool> oldMap = setMap(newMap);
		if (oldMap != null)
			IOUtils.close(oldMap.values());
	}

	public AbstractTool get(String name) throws IOException {
		trackedFile.check();
		return super.get(name);
	}

	public ToolsServiceInterface getRemoteClient(String address, Integer msTimeOut) throws URISyntaxException {
		return new ToolsServiceClient(address, msTimeOut);
	}
	
}
