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
import com.jamesmurty.utils.XMLBuilder2;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.XmlMapper;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.api.scripting.ScriptUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;

public class XMLTool extends AbstractXmlFactoryTool {

	@JsonIgnore
	private final SAXParserFactory saxParserFactory;

	public XMLTool() {
		saxParserFactory = SAXParserFactory.newInstance();
		if (namespace_aware != null)
			saxParserFactory.setNamespaceAware(namespace_aware);
		if (x_include_aware != null)
			saxParserFactory.setXIncludeAware(x_include_aware);
	}

	/**
	 * @param root the name of the root element
	 * @return an new XML builder instance
	 * {@link XMLBuilder2}
	 */
	public XMLBuilder2 create(String root) {
		return XMLBuilder2.create(root);
	}

	/**
	 * Save the XML to the file described by the given path
	 *
	 * @param builder an XML builder
	 * @param path    the destination path
	 * @throws IOException if any I/O error occurs
	 */
	public void saveTo(XMLBuilder2 builder, String path) throws IOException {
		FileWriter writer = new FileWriter(path);
		try {
			builder.toWriter(true, writer, null);
		} finally {
			writer.close();
		}
	}

	/**
	 * Parse an XML stream and call the JS functions
	 *
	 * @param jsObject any Javascript receiving the events
	 * @param input    the stream
	 * @throws ParserConfigurationException if any XML error occurs
	 * @throws SAXException                 if any XML error occurs
	 * @throws IOException                  if any I/O error occurs
	 */
	public void parseStream(ScriptObjectMirror jsObject, InputStream input)
			throws ParserConfigurationException, SAXException, IOException {
		DefaultHandler defaultHandler = (DefaultHandler) ScriptUtils.convert(jsObject, DefaultHandler.class);
		SAXParser saxParser = saxParserFactory.newSAXParser();
		saxParser.parse(input, defaultHandler);
	}

	/**
	 * Parse an XML file
	 *
	 * @param jsObject the Javascript object receiving the events
	 * @param path     the path to the XML file to read
	 * @throws IOException                  if any I/O error occurs
	 * @throws SAXException                 if any XML error occurs
	 * @throws ParserConfigurationException if any XML error occurs
	 */
	public void parseFile(ScriptObjectMirror jsObject, String path)
			throws IOException, SAXException, ParserConfigurationException {
		InputStream in = new BufferedInputStream(new FileInputStream(path));
		try {
			parseStream(jsObject, in);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	/**
	 * Parse an XML string and build a DOM object
	 *
	 * @param xmlString the XML as String
	 * @return a DOM document
	 * @throws IOException                  if any I/O error occurs
	 * @throws SAXException                 if any XML error occurs
	 * @throws ParserConfigurationException if any XML error occurs
	 */
	public Document domParseString(String xmlString) throws IOException, SAXException, ParserConfigurationException {
		final InputSource input = new InputSource();
		input.setCharacterStream(new StringReader(xmlString));
		return getNewDocumentBuilder().parse(input);
	}

	/**
	 * Parse an XML file and build a DOM object
	 *
	 * @param file the file to read
	 * @return a new DOM document
	 * @throws ParserConfigurationException if any XML error occurs
	 * @throws IOException                  if any I/O error occurs
	 * @throws SAXException                 if any XML error occurs
	 */
	public Document domParseFile(String file) throws ParserConfigurationException, IOException, SAXException {
		return getNewDocumentBuilder().parse(file);
	}

	/**
	 * Parse an XML stream and build a DOM object
	 *
	 * @param input the stream to read
	 * @return a new DOM document
	 * @throws ParserConfigurationException if any XML error occurs
	 * @throws IOException                  if any I/O error occurs
	 * @throws SAXException                 if any XML error occurs
	 */
	public Document domParseStream(InputStream input) throws ParserConfigurationException, IOException, SAXException {
		return getNewDocumentBuilder().parse(input);
	}

	/**
	 * Parse an XML document using its URL and build a DOM object
	 *
	 * @param url
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public Document domParseURL(String url) throws IOException, SAXException, ParserConfigurationException {
		return getNewDocumentBuilder().parse(url);
	}

	/**
	 * Generate an XML string from an Object using JAXB
	 *
	 * @param object the object to serialize
	 * @return an XML string which represent the object
	 * @throws IOException if any serialisation error occurs
	 */
	public String printXML(Object object) throws IOException {
		StringWriter sw = new StringWriter();
		toXML(object, sw);
		return sw.toString();
	}

	/**
	 * Write an XML representation of an object
	 *
	 * @param object the object to serialize
	 * @param writer the destination writer
	 * @throws IOException if any serialisation error occurs
	 */
	public void toXML(Object object, Writer writer) throws IOException {
		//JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
		//Marshaller marshaller = jaxbContext.createMarshaller();
		//StringWriter sw = new StringWriter();
		//marshaller.marshal(object, writer);
		XmlMapper.MAPPER.writeValue(writer, object);
	}

}