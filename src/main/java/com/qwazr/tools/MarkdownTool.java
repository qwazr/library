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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.qwazr.library.AbstractLibrary;
import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension;
import org.commonmark.ext.ins.InsExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.AttributeProvider;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class MarkdownTool extends AbstractLibrary {

	final public LinkedHashSet<ExtensionEnum> extensions = null;

	final public String attribute_provider_class = null;

	public enum ExtensionEnum {

		autolinks(AutolinkExtension.create()),
		tables(TablesExtension.create()),
		strikethrough(StrikethroughExtension.create()),
		heading_anchor(HeadingAnchorExtension.create()),
		ins(InsExtension.create()),
		yaml_front_matter(YamlFrontMatterExtension.create());

		private final Extension extension;

		ExtensionEnum(Extension extension) {
			this.extension = extension;
		}
	}

	private final static String DEFAULT_CHARSET = "UTF-8";

	@JsonIgnore
	private volatile Parser parser;

	@JsonIgnore
	private volatile HtmlRenderer renderer;

	@Override
	public void load() throws ClassNotFoundException, NoSuchMethodException {

		final List<Extension> extensionsList = new ArrayList<>(extensions == null ? 0 : extensions.size());
		if (extensions != null)
			for (ExtensionEnum extensionEnum : extensions)
				extensionsList.add(extensionEnum.extension);
		final HtmlRenderer.Builder rendererBuilder = HtmlRenderer.builder();
		final Parser.Builder parserBuilder = Parser.builder();
		if (!extensionsList.isEmpty()) {
			rendererBuilder.extensions(extensionsList);
			parserBuilder.extensions(extensionsList);
		}
		if (attribute_provider_class != null) {
			rendererBuilder.attributeProviderFactory(context -> {
				try {
					return libraryManager.getClassLoaderManager().newInstance(attribute_provider_class);
				} catch (IOException | ReflectiveOperationException e) {
					throw new RuntimeException(e);
				}
			});
		}
		parser = parserBuilder.build();
		renderer = rendererBuilder.build();
	}

	public String toHtml(final String input) {
		return renderer.render(parser.parse(input));
	}

	public String toHtml(final InputStream inputStream, final String encoding) throws IOException {
		return toHtml(new InputStreamReader(inputStream, encoding));
	}

	public String toHtml(final Reader input) throws IOException {
		return renderer.render(parser.parseReader(input));
	}

	public String fileToHtml(final String path, final String encoding) throws IOException {
		return toHtml(Paths.get(path).toFile(), encoding);
	}

	public String fileToHtml(final String path) throws IOException {
		return toHtml(Paths.get(path).toFile(), DEFAULT_CHARSET);
	}

	public String resourceToHtml(final String resourceName, final String encoding) throws IOException {
		try (final InputStream input = libraryManager.getClassLoaderManager().getResourceAsStream(resourceName)) {
			return toHtml(input, encoding);
		}
	}

	public String resourceToHtml(final String res) throws IOException {
		return resourceToHtml(res, DEFAULT_CHARSET);
	}

	public String toHtml(final File file) throws IOException {
		return toHtml(file, DEFAULT_CHARSET);
	}

	public String toHtml(final File file, final String encoding) throws IOException {
		try (final InputStream input = new FileInputStream(file)) {
			return toHtml(input, encoding);
		}
	}

	public static class BoostrapAttributeProvider implements AttributeProvider {

		@Override
		public void setAttributes(final Node node, final String tagName, final Map<String, String> attributes) {
			if ("table".equals(tagName)) {
				attributes.put("class", "table");
			}
		}
	}
}