/**
 * Copyright 2015-2016 Emmanuel Keller / QWAZR
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

import com.qwazr.library.AbstractLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.util.Map;

public abstract class AbstractXmlFactoryTool extends AbstractLibrary {

	private static final Logger logger = LoggerFactory.getLogger(AbstractXmlFactoryTool.class);

	public final Boolean namespace_aware = null;
	public final Boolean expand_entity_references = null;
	public final Boolean validating = null;
	public final Map<String, Boolean> features = null;
	public final Boolean coalescing = null;
	public final Boolean x_include_aware = null;

	private DocumentBuilderFactory documentBuilderFactory = null;

	@Override
	public void load(final File parentDir) {
		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		if (namespace_aware != null)
			documentBuilderFactory.setNamespaceAware(namespace_aware);
		if (expand_entity_references != null)
			documentBuilderFactory.setExpandEntityReferences(expand_entity_references);
		if (validating != null)
			documentBuilderFactory.setValidating(validating);
		if (features != null) {
			features.forEach((key, value) -> {
				try {
					documentBuilderFactory.setFeature(key, value);
				} catch (ParserConfigurationException e) {
					throw new RuntimeException("Cannot set the feature: " + key, e);
				}
			});
		}
		if (coalescing != null)
			documentBuilderFactory.setCoalescing(coalescing);
		if (x_include_aware != null)
			documentBuilderFactory.setXIncludeAware(x_include_aware);
	}

	final protected DocumentBuilder getNewDocumentBuilder() throws ParserConfigurationException {
		synchronized (documentBuilderFactory) {
			final DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			builder.setErrorHandler(ToolErrorHandler.INSTANCE);
			return builder;
		}
	}

	private static class ToolErrorHandler implements ErrorHandler {

		private static final ToolErrorHandler INSTANCE = new ToolErrorHandler();

		@Override
		final public void warning(SAXParseException exception) throws SAXException {
			logger.warn(exception.getMessage(), exception);
		}

		@Override
		final public void error(SAXParseException exception) throws SAXException {
			logger.error(exception.getMessage(), exception);
		}

		@Override
		final public void fatalError(SAXParseException exception) throws SAXException {
			throw exception;
		}
	}
}
