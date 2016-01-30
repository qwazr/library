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
package com.qwazr.library;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.qwazr.connectors.*;
import com.qwazr.tools.*;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")

@JsonSubTypes({ @Type(value = CassandraConnector.class), @Type(value = DatabaseConnector.class),
		@Type(value = EmailConnector.class), @Type(value = FtpConnector.class), @Type(value = HdfsConnector.class),
		@Type(value = LdapConnector.class), @Type(value = MongoDbConnector.class),
		@Type(value = MybatisConnector.class), @Type(value = TableRealmConnector.class),
		@Type(value = ArchiverTool.class), @Type(value = CSVTool.class), @Type(value = FileCrawlerTool.class),
		@Type(value = FreeMarkerTool.class), @Type(value = MarkdownTool.class), @Type(value = ProcessTool.class),
		@Type(value = PropertiesTool.class), @Type(value = Rrd4jTool.class), @Type(value = XMLTool.class),
		@Type(value = XPathTool.class) })
public abstract class AbstractLibrary implements Closeable {

	public final String name = null;

	public abstract void load(File data_directory) throws IOException;

	public void close() {
	}

}
