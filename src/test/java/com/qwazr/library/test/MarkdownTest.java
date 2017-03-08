/**
 * Copyright 2016 Emmanuel Keller / QWAZR
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
package com.qwazr.library.test;

import com.qwazr.library.annotations.Library;
import com.qwazr.tools.MarkdownTool;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class MarkdownTest extends AbstractLibraryTest {

	@Library("markdown_extensions")
	private MarkdownTool markdownExtensionsTool;

	@Library("markdown_default")
	private MarkdownTool markdownDefaultTool;

	@Library("markdown_bootstrap")
	private MarkdownTool markdownBootstrapTool;

	@Test
	public void convertString() throws IOException {
		Assert.assertNotNull(markdownExtensionsTool);
		String html = markdownExtensionsTool.toHtml("# Test");
		Assert.assertNotNull(html);
		Assert.assertEquals("<h1 id=\"test\">Test</h1>\n", html);
	}

	final static String TABLE_MD_PATH = "src/test/resources/com/qwazr/library/test/markdown/table.md";

	@Test
	public void customAttributeProvider() throws IOException {
		Assert.assertNotNull(markdownBootstrapTool);
		String html = markdownBootstrapTool.toHtml(new File(TABLE_MD_PATH));
		Assert.assertNotNull(html);
		Assert.assertTrue(html.startsWith("<table class=\"table\">"));
	}

	@Test
	public void converResource() throws IOException {
		Assert.assertNotNull(markdownDefaultTool);
		Assert.assertNotNull(markdownDefaultTool.resourceToHtml("com/qwazr/library/test/markdown/table.md"));
	}

	@Test
	public void convertPath() throws IOException {
		Assert.assertNotNull(markdownDefaultTool);
		Assert.assertNotNull(markdownDefaultTool.toHtml(TABLE_MD_PATH));
	}
}
