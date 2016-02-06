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
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class FreeMarkerTool extends AbstractLibrary {

	public final String output_encoding;
	public final String default_encoding;
	public final String default_content_type;

	public final Boolean use_classloader;

	@JsonIgnore
	protected Configuration cfg = null;

	@JsonIgnore
	protected File parentDir = null;

	private final static String DEFAULT_CHARSET = "UTF-8";
	private final static String DEFAULT_CONTENT_TYPE = "text/html";

	public FreeMarkerTool() {
		output_encoding = null;
		default_encoding = null;
		default_content_type = null;
		use_classloader = null;
	}

	@Override
	public void load(File parentDir) {
		this.parentDir = parentDir;
		cfg = new Configuration(Configuration.VERSION_2_3_23);
		cfg.setTemplateLoader(
				(use_classloader != null && use_classloader) ? new ResourceTemplateLoader() : new FileTemplateLoader());
		cfg.setOutputEncoding(output_encoding == null ? DEFAULT_CHARSET : output_encoding);
		cfg.setDefaultEncoding(default_encoding == null ? DEFAULT_CHARSET : default_encoding);
		cfg.setLocalizedLookup(false);
		cfg.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
	}

	@Override
	public void close() {
		cfg.clearTemplateCache();
	}

	public void template(String templatePath, Map<String, Object> dataModel, HttpServletResponse response)
			throws TemplateException, IOException {
		if (response.getContentType() == null)
			response.setContentType(default_content_type == null ? DEFAULT_CONTENT_TYPE : default_content_type);
		response.setCharacterEncoding(DEFAULT_CHARSET);
		Template template = cfg.getTemplate(templatePath);
		template.process(dataModel, response.getWriter());
	}

	public void template(String templatePath, HttpServletRequest request, HttpServletResponse response)
			throws IOException, TemplateException {
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("request", request);
		variables.put("session", request.getSession());
		Enumeration<String> attrNames = request.getAttributeNames();
		if (attrNames != null) {
			while (attrNames.hasMoreElements()) {
				String attrName = attrNames.nextElement();
				variables.put(attrName, request.getAttribute(attrName));
			}
		}
		template(templatePath, variables, response);
	}

	public String template(String templatePath, Map<String, Object> dataModel) throws TemplateException, IOException {
		Template template = cfg.getTemplate(templatePath);
		StringWriter stringWriter = new StringWriter();
		try {
			template.process(dataModel, stringWriter);
			return stringWriter.toString();
		} finally {
			IOUtils.close(stringWriter);
		}
	}

	private class FileTemplateLoader implements TemplateLoader {

		@Override
		public Object findTemplateSource(String path) throws IOException {
			File file = new File(parentDir, path);
			return file.exists() && file.isFile() ? file : null;
		}

		@Override
		@JsonIgnore
		public long getLastModified(Object templateSource) {
			return ((File) templateSource).lastModified();
		}

		@Override
		@JsonIgnore
		public Reader getReader(Object templateSource, String encoding) throws IOException {
			return new FileReader((File) templateSource);
		}

		@Override
		public void closeTemplateSource(Object templateSource) throws IOException {
			if (templateSource instanceof Closeable)
				IOUtils.closeQuietly((Closeable) templateSource);
		}

	}

	private static class ResourceTemplateLoader implements TemplateLoader {

		@Override
		public Object findTemplateSource(String path) throws IOException {
			return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
		}

		@Override
		@JsonIgnore
		public long getLastModified(Object templateSource) {
			return Thread.currentThread().getContextClassLoader().hashCode();
		}

		@Override
		@JsonIgnore
		public Reader getReader(Object templateSource, String encoding) throws IOException {
			return new InputStreamReader((InputStream) templateSource);
		}

		@Override
		public void closeTemplateSource(Object templateSource) throws IOException {
			if (templateSource instanceof Closeable)
				IOUtils.close((Closeable) templateSource);
		}

	}
}
