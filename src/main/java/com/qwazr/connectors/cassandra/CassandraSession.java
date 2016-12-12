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
 */
package com.qwazr.connectors.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.exceptions.DriverException;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.qwazr.utils.LockUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;

public class CassandraSession implements Closeable {

	private static final Logger logger = LoggerFactory.getLogger(CassandraSession.class);

	private final LockUtils.ReadWriteLock rwl = new LockUtils.ReadWriteLock();

	private volatile long lastUse;

	private final Cluster cluster;
	private final String keySpace;
	private Session session;

	public CassandraSession(Cluster cluster) {
		this(cluster, null);
	}

	public CassandraSession(Cluster cluster, String keySpace) {
		this.cluster = cluster;
		this.keySpace = keySpace;
		session = null;
		lastUse = System.currentTimeMillis();
	}

	@Override
	public void finalize() throws Throwable {
		closeNoLock();
		super.finalize();
	}

	private void closeNoLock() {
		if (session != null) {
			if (!session.isClosed())
				IOUtils.closeQuietly(session);
			session = null;
		}
	}

	@Override
	public void close() {
		rwl.write(this::closeNoLock);
	}

	public boolean isClosed() {
		return rwl.read(() -> session == null || session.isClosed());
	}

	private Session checkSession() {

		Session s = rwl.read(() -> {
			lastUse = System.currentTimeMillis();
			return session != null && !session.isClosed() ? session : null;
		});
		if (s != null)
			return s;

		try {
			return rwl.writeEx(() -> {
				if (session != null && !session.isClosed())
					return session;
				if (cluster == null || cluster.isClosed())
					throw new DriverException("The cluster is closed");
				if (logger.isDebugEnabled())
					logger.debug("Create session " + keySpace == null ? StringUtils.EMPTY : keySpace);
				session = keySpace == null ? cluster.connect() : cluster.connect(keySpace);
				return session;
			});
		} catch (LockUtils.InsideLockException e) {
			if (e.exception instanceof RuntimeException)
				throw (RuntimeException) e.exception;
			throw e;
		}
	}

	private SimpleStatement getStatement(final String cql, final Integer fetchSize, final Object... values) {
		SimpleStatement statement =
				values != null && values.length > 0 ? new SimpleStatement(cql, values) : new SimpleStatement(cql);
		if (fetchSize != null)
			statement.setFetchSize(fetchSize);
		return statement;
	}

	private ResultSet executeStatement(Session session, SimpleStatement statement) {
		try {
			return session.execute(statement);
		} catch (NoHostAvailableException e1) {
			if (cluster == null || !cluster.isClosed())
				throw e1;
			try {
				return session.execute(statement);
			} catch (DriverException e2) {
				logger.warn(e2.getMessage(), e2);
				throw e1;
			}
		}

	}

	public ResultSet executeWithFetchSize(String cql, int fetchSize, Object... values) {
		if (logger.isDebugEnabled())
			logger.debug("Execute " + cql);
		Session session = checkSession();
		SimpleStatement statement = getStatement(cql, fetchSize, values);
		return executeStatement(session, statement);
	}

	public ResultSet execute(String cql, Object... values) {
		if (logger.isDebugEnabled())
			logger.debug("Execute " + cql);
		Session session = checkSession();
		SimpleStatement statement = getStatement(cql, null, values);
		return executeStatement(session, statement);
	}

	long getLastUse() {
		return lastUse;
	}

}
