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

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class ComponentsManagerTest {

	@Test
	public void componentsManagerTest() throws IOException, ClassNotFoundException {
		final ComponentsManager componentsManager = new ComponentsManager().registerServices();
		final ComponentDescription.LinkClass linkClass =
				componentsManager.getComponents().get(HelloWorld.class.getName());
		Assert.assertNotNull(linkClass);
		final List<ComponentDescription.LinkConstructor> constructors = linkClass.getConstructors();
		Assert.assertNotNull(constructors);
		final List<ComponentDescription.LinkMethod> methods = linkClass.getMethods();
		Assert.assertNotNull(methods);
		Assert.assertEquals(methods.toString(), 2, methods.size(), 1);
		for (ComponentDescription.LinkMethod method : methods) {
			final List<ComponentDescription.LinkParameter> parameters = method.getParameters();
			Assert.assertNotNull(parameters);
			for (ComponentDescription.LinkParameter parameter : parameters) {
				Assert.assertNotNull(parameters.get(0).getName());
				Assert.assertNotNull(parameters.get(0).getDescription());
			}
		}
	}
}
