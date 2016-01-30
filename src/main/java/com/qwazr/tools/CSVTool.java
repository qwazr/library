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
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class CSVTool extends AbstractLibrary {

	public enum Format {

		DEFAULT(CSVFormat.DEFAULT),

		EXCEL(CSVFormat.EXCEL),

		MYSQL(CSVFormat.MYSQL),

		RFC4180(CSVFormat.RFC4180),

		TDF(CSVFormat.TDF);

		private final CSVFormat csvFormat;

		Format(CSVFormat csvFormat) {
			this.csvFormat = csvFormat;
		}
	}

	public final Format format = Format.DEFAULT;

	@Override
	public void load(File parentDir) {
	}

	@JsonIgnore
	public CSVPrinter getNewPrinter(Appendable appendable, IOUtils.CloseableContext closeable) throws IOException {
		return getNewPrinter(format.csvFormat, appendable, closeable);
	}

	@JsonIgnore
	public CSVPrinter getNewPrinter(CSVFormat format, Appendable appendable, IOUtils.CloseableContext closeable)
			throws IOException {
		CSVPrinter printer = new CSVPrinter(appendable, format);
		if (closeable != null)
			closeable.add(printer);
		return printer;
	}

	@JsonIgnore
	public CSVParser getNewParser(CSVFormat format, File file, IOUtils.CloseableContext closeable) throws IOException {
		FileReader fileReader = new FileReader(file);
		if (closeable != null)
			closeable.add(fileReader);
		return getNewParser(format, fileReader, closeable);
	}

	@JsonIgnore
	public CSVParser getNewParser(Reader reader, IOUtils.CloseableContext closeable) throws IOException {
		return getNewParser(format.csvFormat, reader, closeable);
	}

	@JsonIgnore
	public CSVParser getNewParser(CSVFormat format, Reader reader, IOUtils.CloseableContext closeable)
			throws IOException {
		CSVParser parser = new CSVParser(reader, format);
		if (closeable != null)
			closeable.add(parser);
		return parser;
	}

}