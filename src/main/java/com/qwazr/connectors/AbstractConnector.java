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
package com.qwazr.connectors;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")

@JsonSubTypes({ @Type(value = CassandraConnector.class), @Type(value = DatabaseConnector.class),
		@Type(value = EmailConnector.class), @Type(value = FtpConnector.class), @Type(value = HdfsConnector.class),
		@Type(value = LdapConnector.class), @Type(value = MongoDbConnector.class),
		@Type(value = MybatisConnector.class), @Type(value = TableRealmConnector.class) })

public abstract class AbstractConnector implements Closeable {

	public final String name = null;

	public abstract void load(File data_directory) throws IOException;

	public void close() {
	}

}
