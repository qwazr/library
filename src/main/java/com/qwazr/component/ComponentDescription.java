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

import com.qwazr.component.annotations.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public abstract class ComponentDescription {

	private final String name;
	private final String description;

	protected ComponentDescription(final String name, final Component annotation) {
		this.name = name;
		description = annotation == null ? null : annotation.value();
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return getName();
	}

	public String getDescription() {
		return description;
	}

	static class LinkClass extends ComponentDescription {

		private final List<LinkConstructor> constructors;
		private final List<LinkMethod> methods;

		public LinkClass(Class<? extends ComponentInterface> componentClass) {
			super(componentClass.getName(), componentClass.getAnnotation(Component.class));
			constructors = new ArrayList<>();
			for (Constructor constructor : componentClass.getDeclaredConstructors())
				constructors.add(new LinkConstructor(constructor));
			methods = new ArrayList<>();
			for (Method method : componentClass.getDeclaredMethods())
				methods.add(new LinkMethod(method));
		}

		public List<LinkConstructor> getConstructors() {
			return constructors;
		}

		public List<LinkMethod> getMethods() {
			return methods;
		}
	}

	static class LinkConstructor extends ComponentDescription {

		private final List<LinkParameter> parameters;

		LinkConstructor(Constructor<?> constructor) {
			super(constructor.getName(), constructor.getAnnotation(Component.class));
			parameters = getParameters(constructor);
		}

		public List<LinkParameter> getParameters() {
			return parameters;
		}
	}

	static class LinkMethod extends ComponentDescription {

		private final List<LinkParameter> parameters;

		LinkMethod(Method method) {
			super(method.getName(), method.getAnnotation(Component.class));
			parameters = getParameters(method);
		}

		public List<LinkParameter> getParameters() {
			return parameters;
		}

		@Override
		public String toString() {
			return super.toString() + parameters;
		}
	}

	List<LinkParameter> getParameters(Executable executable) {
		final List<LinkParameter> parameters = new ArrayList<>();
		for (Parameter parameter : executable.getParameters())
			parameters.add(new LinkParameter(parameter));
		return parameters;
	}

	static class LinkParameter extends ComponentDescription {

		LinkParameter(Parameter parameter) {
			super(parameter.getName(), parameter.getAnnotation(Component.class));
		}
	}
}
