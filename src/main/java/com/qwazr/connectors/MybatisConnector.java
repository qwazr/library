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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.qwazr.classloader.ClassLoaderManager;
import com.qwazr.library.AbstractPasswordLibrary;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.IOUtils.CloseableContext;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class MybatisConnector extends AbstractPasswordLibrary {

	private static final Logger logger = LoggerFactory.getLogger(MybatisConnector.class);

	public final String configuration_file = null;

	public final String configuration_resource = null;

	public final String environment = null;

	public final Map<String, String> properties = null;

	private SqlSessionFactory sqlSessionFactory = null;

	private final static String default_configuration = "com/qwazr/connectors/mybatis/default-config.xml";

	@Override
	public void load(File data_directory) throws IOException {

		final File configurationFile;
		if (configuration_file != null) {
			configurationFile = new File(configuration_file);
			if (!configurationFile.exists())
				throw new RuntimeException("The configuration file " + configuration_file + " does not exist");
		} else
			configurationFile = null;
		final Properties props;
		if (properties != null) {
			props = new Properties();
			props.putAll(properties);
		} else
			props = null;

		final SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
		final InputStream inputStream;
		if (configurationFile != null)
			inputStream = new FileInputStream(configurationFile);
		else
			inputStream = Resources.getResourceAsStream(ClassLoaderManager.classLoader,
					configuration_resource != null ? configuration_resource : default_configuration);
		try {
			if (environment != null) {
				if (props != null)
					sqlSessionFactory = builder.build(inputStream, environment, props);
				else
					sqlSessionFactory = builder.build(inputStream, environment);
			} else {
				if (props != null)
					sqlSessionFactory = builder.build(inputStream, props);
				else
					sqlSessionFactory = builder.build(inputStream);
			}
		} finally {
			IOUtils.close(inputStream);
		}
	}

	@JsonIgnore
	public SqlSessionFactory getSqlSessionFactory() {
		return sqlSessionFactory;
	}

	@JsonIgnore
	public SqlSessionFactory getSqlSessionFactory(CloseableContext closeable) {
		Objects.requireNonNull(closeable, "closeable cannot be null");
		return new CloseableSqlSessionFactory(closeable);
	}

	public class CloseableSqlSessionFactory implements SqlSessionFactory {

		private final CloseableContext closeable;

		private CloseableSqlSessionFactory(CloseableContext closeable) {
			this.closeable = closeable;
		}

		@Override
		public SqlSession openSession() {
			return closeable.add(sqlSessionFactory.openSession());
		}

		@Override
		public SqlSession openSession(boolean autoCommit) {
			return closeable.add(sqlSessionFactory.openSession(autoCommit));
		}

		@Override
		public SqlSession openSession(Connection connection) {
			return closeable.add(sqlSessionFactory.openSession(connection));
		}

		@Override
		public SqlSession openSession(TransactionIsolationLevel level) {
			return closeable.add(sqlSessionFactory.openSession(level));
		}

		@Override
		public SqlSession openSession(ExecutorType execType) {
			return closeable.add(sqlSessionFactory.openSession(execType));
		}

		@Override
		public SqlSession openSession(ExecutorType execType, boolean autoCommit) {
			return closeable.add(sqlSessionFactory.openSession(execType, autoCommit));
		}

		@Override
		public SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level) {
			return closeable.add(sqlSessionFactory.openSession(execType, level));
		}

		@Override
		public SqlSession openSession(ExecutorType execType, Connection connection) {
			return closeable.add(sqlSessionFactory.openSession(execType, connection));
		}

		@Override
		public Configuration getConfiguration() {
			return sqlSessionFactory.getConfiguration();
		}
	}
}
