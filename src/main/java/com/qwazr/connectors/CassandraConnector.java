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

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.qwazr.utils.cassandra.CassandraCluster;
import com.qwazr.utils.cassandra.CassandraSession;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class CassandraConnector extends AbstractPasswordConnector {

	public final List<String> hosts = null;

	public final String login = null;

	public final Integer timeout_connect_ms = null;
	public final Integer timeout_read_ms = null;

	public final Integer timeout_pool_ms = null;
	public final Integer pool_connections = null;

	@JsonIgnore
	private CassandraCluster cluster = null;

	@JsonIgnore
	private String keyspace = null;

	@Override
	public void load(File dataDir) {
		cluster = new CassandraCluster(login, password, hosts, timeout_connect_ms, timeout_read_ms, timeout_pool_ms,
				pool_connections);
	}

	@Override
	public void close() {
		if (cluster != null) {
			IOUtils.closeQuietly(cluster);
			cluster = null;
		}
	}

	public ResultSet executeWithFetchSize(String csql, int fetchSize, Object... values) {
		CassandraSession session = cluster.getSession(keyspace);
		return session.executeWithFetchSize(csql, fetchSize, values);
	}

	public ResultSet execute(String csql, Object... values) {
		CassandraSession session = cluster.getSession(keyspace);
		return session.execute(csql, values);
	}

	@JsonIgnore
	public UUID getTimeUUID() {
		return UUIDs.timeBased();
	}

	@JsonIgnore
	public long getTimeFromUUID(UUID uuid) {
		return UUIDs.unixTimestamp(uuid);
	}

}
