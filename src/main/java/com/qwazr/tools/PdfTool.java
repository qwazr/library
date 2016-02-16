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
package com.qwazr.tools;

import com.qwazr.library.AbstractLibrary;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.StringUtils;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.PDFOperator;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class PdfTool extends AbstractLibrary {

	private File data_directory = null;

	@Override
	public void load(File data_directory) throws IOException {
		this.data_directory = data_directory;
	}

	public void replace(String input, String output, Map<String, Object> replacements)
			throws IOException, COSVisitorException {
		replace(new File(data_directory, input), new File(data_directory, output), replacements);
	}

	public void replace(File input, File output, Map<String, Object> replacements)
			throws IOException, COSVisitorException {
		// the document
		PDDocument doc = null;
		try {
			doc = PDDocument.load(input);
			List pages = doc.getDocumentCatalog().getAllPages();
			for (int i = 0; i < pages.size(); i++) {
				PDPage page = (PDPage) pages.get(i);
				PDStream contents = page.getContents();
				PDFStreamParser parser = new PDFStreamParser(contents.getStream());
				parser.parse();
				List tokens = parser.getTokens();
				for (int j = 0; j < tokens.size(); j++) {
					Object next = tokens.get(j);
					if (next instanceof PDFOperator) {
						PDFOperator op = (PDFOperator) next;
						//Tj and TJ are the two operators that display
						//strings in a PDF
						if (op.getOperation().equals("Tj")) {
							//Tj takes one operator and that is the string
							//to display so lets update that operator
							COSString previous = (COSString) tokens.get(j - 1);
							String string = previous.getString();
							string = StringUtils.replaceEach(string, replacements);
							previous.reset();
							previous.append(string.getBytes("ISO-8859-1"));
						} else if (op.getOperation().equals("TJ")) {
							COSArray previous = (COSArray) tokens.get(j - 1);
							for (int k = 0; k < previous.size(); k++) {
								Object arrElement = previous.getObject(k);
								if (arrElement instanceof COSString) {
									COSString cosString = (COSString) arrElement;
									String string = cosString.getString();
									string = StringUtils.replaceEach(string, replacements);
									cosString.reset();
									cosString.append(string.getBytes("ISO-8859-1"));
								}
							}
						}
					}
				}
				//now that the tokens are updated we will replace the
				//page content stream.
				PDStream updatedStream = new PDStream(doc);
				OutputStream out = updatedStream.createOutputStream();
				ContentStreamWriter tokenWriter = new ContentStreamWriter(out);
				tokenWriter.writeTokens(tokens);
				page.setContents(updatedStream);
			}
			doc.save(output);
		} finally {
			IOUtils.close(doc);
		}
	}
}
