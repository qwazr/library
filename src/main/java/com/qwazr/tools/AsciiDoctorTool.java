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
import org.asciidoctor.*;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class AsciiDoctorTool extends AbstractLibrary {

	//Options
	public final Boolean to_file = null;
	public final Boolean in_place = null;
	public final String backend = null;
	public final String base_dir = null;
	public final Boolean compact = null;
	public final String destination_dir = null;
	public final String doctype = null;
	public final SafeMode safe = null;
	public final Boolean header_footer = null;
	public final Boolean parse_header_only = null;
	public final Boolean mk_dirs = null;
	public final String eruby = null;
	public final Boolean template_cache = null;
	public final String[] template_dirs = null;
	public final String template_engine = null;

	public final List<String> required_libraries = null;

	//Attributes
	public final HashMap<String, Object> attributes = null;

	@JsonIgnore
	private Asciidoctor asciidoctor = null;

	@JsonIgnore
	private Options options = null;

	@Override
	public void load(File dataDir) {
		asciidoctor = Asciidoctor.Factory.create();
		if (required_libraries != null)
			for (String library : required_libraries)
				asciidoctor.requireLibrary(library);

		options = new Options();
		if (to_file != null)
			options.setToFile(to_file);
		if (in_place != null)
			options.setInPlace(in_place);
		if (backend != null)
			options.setBackend(backend);
		if (base_dir != null)
			options.setBaseDir(base_dir);
		if (compact != null)
			options.setCompact(compact);
		if (destination_dir != null)
			options.setDestinationDir(destination_dir);
		if (doctype != null)
			options.setDocType(doctype);
		if (safe != null)
			options.setSafe(safe);
		if (header_footer != null)
			options.setHeaderFooter(header_footer);
		if (parse_header_only != null)
			options.setParseHeaderOnly(parse_header_only);
		if (mk_dirs != null)
			options.setMkDirs(mk_dirs);
		if (eruby != null)
			options.setEruby(eruby);
		if (template_cache != null)
			options.setTemplateCache(template_cache);
		if (template_dirs != null)
			options.setTemplateDirs(template_dirs);
		if (template_engine != null)
			options.setTemplateEngine(template_engine);
		if (attributes != null)
			options.setAttributes(attributes);
	}

	@Override
	public void close() {
		if (asciidoctor != null) {
			asciidoctor.shutdown();
			asciidoctor = null;
		}
	}

	public String convertFile(File file) {
		return asciidoctor.convertFile(file, options);
	}

	public String[] convertFiles(Collection<File> files) {
		return asciidoctor.convertFiles(files, options);
	}

	public String[] convertDirectory(String baseDir) {
		AsciiDocDirectoryWalker directoryWalker = new AsciiDocDirectoryWalker(baseDir);
		return asciidoctor.convertDirectory(directoryWalker, options);
	}

	public String[] convertDirectory(File baseDir) throws IOException {
		return convertDirectory(baseDir.getCanonicalPath());
	}
}
