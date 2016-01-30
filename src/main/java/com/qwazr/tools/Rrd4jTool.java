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
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.SubstitutedVariables;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

public class Rrd4jTool extends AbstractTool {

	private static final Logger logger = LoggerFactory.getLogger(Rrd4jTool.class);

	public final String path = null;
	public final Long startTime = null;
	public final Long step = null;
	public final Integer version = null;
	public final RrdArchive[] archives = null;
	public final RrdDataSource[] datasources = null;
	public final String backendFactory = null;

	private String resolvedPath = null;

	@Override
	public void load(File parentDir) throws IOException {
		Objects.requireNonNull(path, "The path property is required");
		resolvedPath = SubstitutedVariables.getEnvironmentVariables().substitute(path);
	}

	protected RrdDef createDef() {
		final RrdDef rrdDef;
		if (step != null) {
			if (startTime != null) {
				if (version != null)
					rrdDef = new RrdDef(resolvedPath, startTime, step, version);
				else
					rrdDef = new RrdDef(resolvedPath, startTime, step);
			} else
				rrdDef = new RrdDef(resolvedPath, step);
		} else
			rrdDef = new RrdDef(resolvedPath);
		if (archives != null)
			for (RrdArchive archive : archives)
				rrdDef.addArchive(archive.getDef());
		if (datasources != null)
			for (RrdDataSource datasource : datasources)
				rrdDef.addDatasource(datasource.getDef());
		rrdDef.addDatasource();
		return rrdDef;
	}

	/**
	 * @param closeableContext
	 * @return a new RrdDb instance
	 * @throws IOException
	 * @see RrdDb
	 */
	@JsonIgnore
	public RrdDb getDb(IOUtils.CloseableContext closeableContext, boolean readOnly) throws IOException {
		Objects.requireNonNull(closeableContext, "Requires a closeable parameter");
		RrdDatabase rrdDatabase = null;
		try {
			rrdDatabase = new RrdDatabase(resolvedPath, backendFactory, readOnly);
		} catch (FileNotFoundException e) {
			if (logger.isInfoEnabled())
				logger.info("RRD database not found. Create a new one: " + resolvedPath);
			rrdDatabase = new RrdDatabase(createDef(), backendFactory);
		}
		closeableContext.add(rrdDatabase);
		return rrdDatabase.rrdDb;
	}

	/**
	 * @param closeableContext
	 * @return a new RrdDb instance
	 * @throws IOException
	 */
	public RrdDb getDb(IOUtils.CloseableContext closeableContext) throws IOException {
		return getDb(closeableContext, false);
	}

	public RrdDb getDb(IOUtils.CloseableContext closeableContext, String rrdPath) throws IOException {
		return new RrdDatabase(rrdPath, backendFactory, true).rrdDb;
	}

	@JsonIgnore
	public Sample createSample(RrdDb rrdDb, Long time) throws IOException {
		final Sample sample;
		if (time != null)
			sample = rrdDb.createSample(time);
		else
			sample = rrdDb.createSample();
		return sample;
	}

	/**
	 * Parses at-style time specification and returns the corresponding timestamp. For example:<p>
	 * <pre>
	 * long t = Util.getTimestamp("now-1d");
	 * </pre>
	 *
	 * @param atStyleTimeSpec at-style time specification. For the complete explanation of the syntax
	 *                        allowed see RRDTool's <code>rrdfetch</code> man page.<p>
	 * @return timestamp in seconds since epoch.
	 */
	public long getTimestamp(String atStyleTimeSpec) {
		return Util.getTimestamp(atStyleTimeSpec);
	}

	public static class RrdArchive {

		public final ConsolFun consolFun = null;
		public final Double xff = null;
		public final Integer steps = null;
		public final Integer rows = null;

		@JsonIgnore
		private ArcDef getDef() {
			Objects.requireNonNull(consolFun, "The consolFun property is required");
			Objects.requireNonNull(xff, "The xff property is required");
			Objects.requireNonNull(steps, "The steps property is required");
			Objects.requireNonNull(rows, "The rows property is required");
			return new ArcDef(consolFun, xff, steps, rows);
		}
	}

	public static class RrdDataSource {

		public final String dsName = null;
		public final DsType dsType = null;
		public final Long heartbeat = null;
		public final Double minValue = null;
		public final Double maxValue = null;

		@JsonIgnore
		private DsDef getDef() {
			Objects.requireNonNull(dsName, "The dsName property is required");
			Objects.requireNonNull(dsType, "The dsType property is required");
			return new DsDef(dsName, dsType, heartbeat == null ? 600 : heartbeat,
					minValue == null ? Double.NaN : minValue, maxValue == null ? Double.NaN : maxValue);
		}
	}

	private static class RrdDatabase implements Closeable {

		private final RrdDb rrdDb;

		private RrdDatabase(String path, String backendFactory, boolean readOnly) throws IOException {
			if (backendFactory != null) {
				rrdDb = new RrdDb(path, readOnly, RrdBackendFactory.getFactory(backendFactory));
			} else {
				rrdDb = new RrdDb(path, readOnly);
			}
		}

		private RrdDatabase(RrdDef def, String backendFactory) throws IOException {
			if (backendFactory != null)
				rrdDb = new RrdDb(def, RrdBackendFactory.getFactory(backendFactory));
			else
				rrdDb = new RrdDb(def);
		}

		@Override
		public void close() throws IOException {
			if (rrdDb == null)
				return;
			if (rrdDb.isClosed())
				return;
			try {
				rrdDb.close();
			} catch (IOException e) {
				if (logger.isWarnEnabled())
					logger.warn(e.getMessage(), e);
			}
		}
	}

}