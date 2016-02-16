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
import org.apache.fop.apps.*;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

public class ApacheFopTool extends AbstractLibrary {

	private FopFactory fopFactory = null;
	private File data_directory = null;

	@Override
	public void load(File data_directory) throws IOException {
		this.data_directory = data_directory;
		this.fopFactory = FopFactory.newInstance(new File(".").toURI());
	}

	public void generatePdf(StreamSource source, File outputFile)
			throws IOException, FOPException, TransformerException {

		OutputStream out = null;

		try {
			FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
			// configure foUserAgent as desired

			// Setup output stream.  Note: Using BufferedOutputStream
			// for performance reasons (helpful with FileOutputStreams).
			out = new FileOutputStream(outputFile);
			out = new BufferedOutputStream(out);

			// Construct fop with desired output format
			Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

			// Setup JAXP using identity transformer
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer(); // identity transformer

			// Resulting SAX events (the generated FO) must be piped through to FOP
			Result res = new SAXResult(fop.getDefaultHandler());

			// Start XSLT transformation and FOP processing
			transformer.transform(source, res);

		} finally {
			IOUtils.close(out);
		}
	}

	public void xmlFileToPdf(File input, File output) throws TransformerException, IOException, FOPException {
		generatePdf(new StreamSource(input), output);
	}

	public void xmlStringToPdf(String input, File output) throws TransformerException, IOException, FOPException {
		StringReader reader = new StringReader(input);
		try {
			generatePdf(new StreamSource(reader), output);
		} finally {
			IOUtils.close(reader);
		}
	}

	public void xmlFileToPdf(String input, String output) throws IOException, FOPException, TransformerException {
		xmlFileToPdf(new File(data_directory, input), new File(data_directory, output));
	}

}
