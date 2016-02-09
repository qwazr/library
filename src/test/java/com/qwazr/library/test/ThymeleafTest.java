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

import com.qwazr.library.LibraryManager;
import com.qwazr.library.annotations.Library;
import com.qwazr.tools.ThymeleafTool;
import freemarker.template.TemplateException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ThymeleafTest extends AbstractLibraryTest {

	private final static String TEMPLATE_RESOURCE = "com/qwazr/library/test/thymeleaf/template";
	private final static String TEST_TEXT = "Hello world!";

	@Library("thymeleaf_classloader")
	private ThymeleafTool thymeleaf_classloader;

	@Library("thymeleaf_files")
	private ThymeleafTool thymeleaf_files;

	@Before
	public void before() throws IOException {
		super.before();
		LibraryManager.inject(this);
	}

	private Map<String, Object> getVariables() {
		Map<String, Object> variables = new HashMap<>();
		variables.put("test", TEST_TEXT);
		return variables;
	}

	@Test
	public void classloaderTemplate() throws IOException, TemplateException {
		Assert.assertNotNull(thymeleaf_classloader);
		String test = thymeleaf_classloader.template(TEMPLATE_RESOURCE, Locale.getDefault(), getVariables());
		Assert.assertNotNull(test);
		Assert.assertTrue(test.contains(TEST_TEXT));
	}

	@Test
	public void fileTemplate() throws IOException, TemplateException {
		Assert.assertNotNull(thymeleaf_files);
		String test = thymeleaf_files.template(TEMPLATE_RESOURCE, Locale.getDefault(), getVariables());
		Assert.assertNotNull(test);
		Assert.assertTrue(test.contains(TEST_TEXT));
	}
}
