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
package com.qwazr.library.test;

import com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException;
import com.qwazr.connectors.MybatisConnector;
import com.qwazr.library.LibraryManager;
import com.qwazr.library.annotations.Library;
import com.qwazr.utils.IOUtils;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;

public class MybatisTest extends AbstractLibraryTest {

	@Library("mybatis_default")
	private MybatisConnector mybatis_default;

	@Library("mybatis_file")
	private MybatisConnector mybatis_file;

	@Before
	public void before() throws IOException {
		super.before();
		LibraryManager.inject(this);
	}

	protected void checkSession(SqlSessionFactory sessionFactory) {
		Assert.assertNotNull(sessionFactory);
		final SqlSession session = sessionFactory.openSession();
		try {
			final Connection connection = session.getConnection();
		} catch (PersistenceException e) {
			// It is okay to not be able to establish a mysql connection for this test
			// Any other exception is an error
			if (e.getCause() instanceof MySQLNonTransientConnectionException)
				return;
			throw e;
		} finally {
			IOUtils.close(session);
		}
	}

	protected void checkSessionFactory(MybatisConnector mybatis) {
		Assert.assertNotNull(mybatis);
		checkSession(mybatis.getSqlSessionFactory());
		IOUtils.CloseableContext context = new IOUtils.CloseableList();
		checkSession(mybatis.getSqlSessionFactory(context));
		IOUtils.close(context);
	}

	@Test
	public void mybatis_default() throws IOException {
		checkSessionFactory(mybatis_default);
	}

	@Test
	public void mybatis_file() throws IOException {
		checkSessionFactory(mybatis_file);
	}
}
