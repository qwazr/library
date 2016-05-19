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
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.DocumentHeader;
import org.asciidoctor.ast.DocumentRuby;
import org.asciidoctor.ast.StructuredDocument;
import org.asciidoctor.converter.JavaConverterRegistry;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.extension.RubyExtensionRegistry;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	/**
	 * Parse the AsciiDoc source input into an Document {@link DocumentRuby} and
	 * render it to the specified backend format.
	 * <p>
	 * Accepts input as File path.
	 * <p>
	 * If the :in_place option is true, and the input is a File, the output is
	 * written to a file adjacent to the input file, having an extension that
	 * corresponds to the backend format. Otherwise, if the :to_file option is
	 * specified, the file is written to that file. If :to_file is not an
	 * absolute path, it is resolved relative to :to_dir, if given, otherwise
	 * the Document#base_dir. If the target directory does not exist, it will
	 * not be created unless the :mkdirs option is set to true. If the file
	 * cannot be written because the target directory does not exist, or because
	 * it falls outside of the Document#base_dir in safe mode, an IOError is
	 * raised.
	 *
	 * @param file an input Asciidoctor file.
	 * @return returns nothing if the rendered output String is written to a
	 * file.
	 */
	public String convertFile(File file) {
		return asciidoctor.convertFile(file, options);
	}

	/**
	 * Parses all files added inside a collection.
	 *
	 * @param files to be rendered.
	 *              a Hash of options to control processing (default: {}).
	 * @return returns an array of 0 positions if the rendered output is written
	 * to a file.
	 */
	public String[] convertFiles(Collection<File> files) {
		return asciidoctor.convertFiles(files, options);
	}

	/**
	 * Parse all AsciiDoc files found in the base directory.
	 *
	 * @param baseDir the directory with all files to be rendered.
	 * @return returns an array of 0 positions if the rendered output is written
	 * to a file.
	 */
	public String[] convertDirectory(String baseDir) {
		final AsciiDocDirectoryWalker directoryWalker = new AsciiDocDirectoryWalker(baseDir);
		return asciidoctor.convertDirectory(directoryWalker, options);
	}

	/**
	 * Parse all AsciiDoc files found in the base directory.
	 *
	 * @param baseDir the directory with all files to be rendered.
	 * @return returns an array of 0 positions if the rendered output is written
	 * to a file.
	 */
	public String[] convertDirectory(File baseDir) throws IOException {
		return convertDirectory(baseDir.getCanonicalPath());
	}

	/**
	 * Parse the AsciiDoc source input into an Document and
	 * render it to the specified backend format.
	 * Accepts input as String object.
	 *
	 * @param content the AsciiDoc source as String.
	 * @return the rendered output String is returned
	 */
	public String convert(String content) {
		return asciidoctor.convert(content, options);
	}

	/**
	 * Parse the AsciiDoc source input into an Document {@link DocumentRuby} and
	 * render it to the specified backend format.
	 * Accepts input as String object.
	 *
	 * @param content the AsciiDoc source as String.
	 * @param options a Hash of options to control processing (default: {}).
	 * @return the rendered output String is returned
	 */
	public String convert(String content, Options options) {
		return asciidoctor.convert(content, options);
	}

	/**
	 * @return a clone of the current options
	 */
	public Options getNewOptions() {
		return new Options(options.map());
	}

	/**
	 * Parse the document read from reader, and rendered result is sent to
	 * writer.
	 *
	 * @param contentReader  where asciidoc content is read.
	 * @param rendererWriter where rendered content is written. Writer is flushed, but not
	 *                       closed.
	 * @throws IOException if an error occurs while writing rendered content, this
	 *                     exception is thrown.
	 */
	public void convert(Reader contentReader, Writer rendererWriter) throws IOException {
		asciidoctor.convert(contentReader, rendererWriter, options);
	}

}
