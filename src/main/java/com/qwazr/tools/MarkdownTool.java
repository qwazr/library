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
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.StringUtils;
import org.apache.commons.io.FileUtils;
import org.pegdown.Extensions;
import org.pegdown.LinkRenderer;
import org.pegdown.ParsingTimeoutException;
import org.pegdown.PegDownProcessor;
import org.pegdown.ToHtmlSerializer;
import org.pegdown.VerbatimSerializer;
import org.pegdown.ast.RootNode;
import org.pegdown.ast.TableNode;
import org.pegdown.plugins.ToHtmlSerializerPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class MarkdownTool extends AbstractLibrary {

	final public List<ExtensionEnum> extensions = null;

	final public Long max_parsing_time = null;

	final public String html_serializer_class = null;

	/**
	 * @see org.pegdown.Extensions
	 */
	public enum ExtensionEnum {

		none(Extensions.NONE),
		smarts(Extensions.SMARTS),
		quotes(Extensions.QUOTES),
		smartypants(Extensions.SMARTYPANTS),
		abbrevations(Extensions.ABBREVIATIONS),
		hardwraps(Extensions.HARDWRAPS),
		autolinks(Extensions.AUTOLINKS),
		tables(Extensions.TABLES),
		definitions(Extensions.DEFINITIONS),
		fenced_code_blocks(Extensions.FENCED_CODE_BLOCKS),
		wikilinks(Extensions.WIKILINKS),
		strikethrough(Extensions.STRIKETHROUGH),
		anchorlinks(Extensions.ANCHORLINKS),
		all(Extensions.ALL),
		suppress_html_blocks(Extensions.SUPPRESS_HTML_BLOCKS),
		suppress_inline_html(Extensions.SUPPRESS_INLINE_HTML),
		suppress_all_html(Extensions.SUPPRESS_ALL_HTML),
		atxheaderspace(Extensions.ATXHEADERSPACE),
		forcelistitempara(Extensions.FORCELISTITEMPARA),
		relaxedhrules(Extensions.RELAXEDHRULES),
		tasklistitems(Extensions.TASKLISTITEMS),
		extanchorlinks(Extensions.EXTANCHORLINKS),
		all_optionals(Extensions.ALL_OPTIONALS),
		all_with_optionals(Extensions.ALL_WITH_OPTIONALS);

		private final int ext;

		ExtensionEnum(int ext) {
			this.ext = ext;
		}
	}

	private final static String DEFAULT_CHARSET = "UTF-8";

	@JsonIgnore
	private volatile MarkdownProcessor markdownProcessor;

	@Override
	public void load() throws ClassNotFoundException, NoSuchMethodException {
		int extensionsValue = 0;
		if (extensions != null)
			for (ExtensionEnum extensionEnum : extensions)
				extensionsValue |= extensionEnum.ext;
		final Class<? extends ToHtmlSerializer> htmlSerializerClass = StringUtils.isEmpty(html_serializer_class) ?
				null :
				libraryManager.getClassLoaderManager().findClass(html_serializer_class);
		markdownProcessor = new MarkdownProcessor(htmlSerializerClass, extensionsValue, max_parsing_time);
	}

	public String toHtml(String input) {
		return markdownProcessor.markdownToHtml(input);
	}

	public String fileToHtml(final String path, final String encoding) throws IOException {
		return toHtml(Paths.get(path).toFile(), encoding);
	}

	public String fileToHtml(final String path) throws IOException {
		return toHtml(Paths.get(path).toFile(), DEFAULT_CHARSET);
	}

	public String resourceToHtml(final String resourceName, final String encoding) throws IOException {
		try (final InputStream input = libraryManager.getClassLoaderManager().getResourceAsStream(resourceName)) {
			return markdownProcessor.markdownToHtml(IOUtils.toString(input, encoding));
		}
	}

	public String resourceToHtml(final String res) throws IOException {
		return resourceToHtml(res, DEFAULT_CHARSET);
	}

	public String toHtml(final File file) throws IOException {
		return markdownProcessor.markdownToHtml(FileUtils.readFileToString(file, DEFAULT_CHARSET));
	}

	public String toHtml(final File file, final String encoding) throws IOException {
		return markdownProcessor.markdownToHtml(FileUtils.readFileToString(file, encoding));
	}

	private class MarkdownProcessor extends PegDownProcessor {

		private final Constructor<? extends ToHtmlSerializer> htmlSerializerConstructor;

		private MarkdownProcessor(final Class<? extends ToHtmlSerializer> htmlSerializerClass,
				final int extensionsValue, final Long maxParsingTime) throws NoSuchMethodException {
			super(extensionsValue, maxParsingTime == null ? PegDownProcessor.DEFAULT_MAX_PARSING_TIME : maxParsingTime);
			this.htmlSerializerConstructor = htmlSerializerClass == null ?
					null :
					htmlSerializerClass.getConstructor(LinkRenderer.class, Map.class, List.class);
		}

		@Override
		public synchronized String markdownToHtml(char[] markdownSource, LinkRenderer linkRenderer,
				Map<String, VerbatimSerializer> verbatimSerializerMap, List<ToHtmlSerializerPlugin> plugins) {
			if (htmlSerializerConstructor == null)
				return super.markdownToHtml(markdownSource, linkRenderer, verbatimSerializerMap, plugins);
			// Synchronized because PegDownProcessor is not thread safe
			try {
				RootNode astRoot = parseMarkdown(markdownSource);
				return htmlSerializerConstructor.newInstance(linkRenderer, verbatimSerializerMap, plugins)
						.toHtml(astRoot);
			} catch (ParsingTimeoutException e) {
				return null;
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static class BootstrapHtmlSerializer extends ToHtmlSerializer {

		public BootstrapHtmlSerializer(final LinkRenderer linkRenderer,
				final Map<String, VerbatimSerializer> verbatimSerializers, final List<ToHtmlSerializerPlugin> plugins) {
			super(linkRenderer, verbatimSerializers, plugins);
		}

		@Override
		public void visit(TableNode node) {
			currentTableNode = node;
			printer.print("<table");
			printAttribute("class", "table");
			printer.print('>').indent(+2);
			visitChildren(node);
			printer.indent(-2).println().print('<').print('/').print("table").print('>');
			currentTableNode = null;
		}
	}
}