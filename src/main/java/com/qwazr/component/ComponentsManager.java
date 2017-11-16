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
package com.qwazr.component;

import java.io.IOException;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public class ComponentsManager {

	private final ConcurrentHashMap<String, ComponentDescription.LinkClass> components;

	public ComponentsManager() {
		components = new ConcurrentHashMap();
	}

	public ComponentsManager registerServices() throws IOException, ClassNotFoundException {
		ServiceLoader.load(ComponentInterface.class, Thread.currentThread().getContextClassLoader())
				.forEach(this::register);
		return this;
	}

	final void register(ComponentInterface componentClass) {
		final ComponentDescription.LinkClass linkClass = new ComponentDescription.LinkClass(componentClass.getClass());
		components.put(linkClass.getName(), linkClass);
	}

	final public Map<String, ComponentDescription.LinkClass> getComponents() {
		return components;
	}

}
