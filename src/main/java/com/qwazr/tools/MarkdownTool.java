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
import org.apache.commons.io.FileUtils;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MarkdownTool extends AbstractLibrary {

	public List<ExtensionEnum> extensions;

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

	@JsonIgnore
	private PegDownProcessor pegDownProcessor = null;

	@Override
	public void load(File dataDir) {
		int extensionsValue = 0;
		if (extensions != null)
			for (ExtensionEnum extensionEnum : extensions)
				extensionsValue |= extensionEnum.ext;
		pegDownProcessor = new PegDownProcessor(extensionsValue);
	}

	public String toHtml(String input) {
		return pegDownProcessor.markdownToHtml(input);
	}

	public String toHtml(File file) throws IOException {
		synchronized (pegDownProcessor) {
			return pegDownProcessor.markdownToHtml(FileUtils.readFileToString(file, "UTF-8"));
		}
	}

	public String toHtml(File file, String encoding) throws IOException {
		synchronized (pegDownProcessor) {
			return pegDownProcessor.markdownToHtml(FileUtils.readFileToString(file, encoding));
		}
	}
}
