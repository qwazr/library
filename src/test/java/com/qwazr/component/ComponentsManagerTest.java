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
		Assert.assertEquals(2, methods.size());
		final List<ComponentDescription.LinkParameter> parameters = methods.get(1).getParameters();
		Assert.assertNotNull(parameters);
		Assert.assertEquals(1, parameters.size());
		Assert.assertNotNull(parameters.get(0).getName());
		Assert.assertEquals("The text to print", parameters.get(0).getDescription());
	}
}
