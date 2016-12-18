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
import com.qwazr.utils.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Closeable;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ThymeleafTool extends AbstractLibrary implements Closeable {

	@JsonIgnore
	private volatile TemplateEngine templateEngine = null;

	public final Boolean use_classloader = false;
	public final Boolean cache_enabled = false;
	public final Long cache_ttl_ms = null;
	public final String character_encoding = null;
	public final String template_mode = null;
	public final String prefix = null;
	public final String suffix = null;

	@Override
	public void load() {

		templateEngine = new TemplateEngine();

		final AbstractConfigurableTemplateResolver templateResolver;
		if (use_classloader)
			templateResolver = new ClassLoaderTemplateResolver();
		else {
			templateResolver = new FileTemplateResolver();
			templateResolver.setPrefix(
					StringUtils.ensureSuffix(libraryManager.getDataDirectory().getAbsolutePath(), "/"));
		}
		if (cache_enabled != null)
			templateResolver.setCacheable(cache_enabled);
		if (cache_ttl_ms != null)
			templateResolver.setCacheTTLMs(cache_ttl_ms);
		if (character_encoding != null)
			templateResolver.setCharacterEncoding(character_encoding);
		if (prefix != null)
			templateResolver.setPrefix(prefix);
		if (suffix != null)
			templateResolver.setSuffix(suffix);
		if (template_mode != null)
			templateResolver.setTemplateMode(template_mode);

		templateEngine.setTemplateResolver(templateResolver);
	}

	@Override
	public void close() {
		if (templateEngine != null) {
			templateEngine.clearTemplateCache();
			templateEngine = null;
		}
	}

	private void checkLoaded() {
		Objects.requireNonNull(templateEngine, "The template engine has not been loaded");
	}

	public void template(String templateName, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		checkLoaded();
		WebContext ctx = new WebContext(request, response, request.getServletContext(), request.getLocale());
		templateEngine.process(templateName, ctx, response.getWriter());
	}

	public String template(String templateName, Locale locale, Map<String, Object> variables) {
		checkLoaded();
		Context ctx = new Context(locale, variables);
		return templateEngine.process(templateName, ctx);
	}

}
