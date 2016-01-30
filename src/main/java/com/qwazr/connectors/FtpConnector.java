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
import com.qwazr.library.AbstractPasswordLibrary;
import com.qwazr.utils.IOUtils;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.commons.net.ftp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class FtpConnector extends AbstractPasswordLibrary {

	public final String hostname = null;
	public final String username = null;
	public final Boolean ssl = null;
	public final Integer connect_time_out = null;
	public final Integer data_timeout = null;
	public final Integer keep_alive_timeout = null;
	public final Integer control_keep_alive_timeout = null;

	private static final Logger logger = LoggerFactory.getLogger(FtpConnector.class);

	@Override
	public void load(File parentDir) {
	}

	@JsonIgnore
	public FTPSession getNewSession(IOUtils.CloseableContext context) {
		FTPSession ftpSession = new FTPSession();
		if (context != null)
			context.add(ftpSession);
		return ftpSession;
	}

	public class FTPSession implements Closeable {

		private final FTPClient ftp;

		private FTPSession() {
			ftp = ssl != null && ssl ? new FTPSClient() : new FTPClient();
		}

		public FTPClient connect() throws IOException {
			if (ftp.isConnected())
				return ftp;
			if (keep_alive_timeout != null)
				ftp.setControlKeepAliveTimeout(keep_alive_timeout);
			if (control_keep_alive_timeout != null)
				ftp.setControlKeepAliveReplyTimeout(control_keep_alive_timeout);
			if (data_timeout != null)
				ftp.setDataTimeout(data_timeout);
			if (connect_time_out != null)
				ftp.setConnectTimeout(connect_time_out);
			ftp.connect(hostname);
			int reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply))
				throw new IOException("FTP server returned an error: " + reply);
			if (!ftp.login(username, password))
				throw new IOException("FTP login failed: " + ftp.getReplyCode());
			return ftp;
		}

		/**
		 * Download the file if any
		 *
		 * @param remote the name of the file
		 * @param file   the destination file
		 * @throws IOException
		 */
		public void retrieve(String remote, File file, Boolean binary) throws IOException {
			if (binary != null) {
				if (binary) {
					if (!ftp.setFileType(FTP.BINARY_FILE_TYPE))
						throw new IOException("FTP cannot be set to binary mode");
				} else {
					if (!ftp.setFileType(FTP.ASCII_FILE_TYPE))
						throw new IOException("FTP cannot be set to ASCII mode");
				}
			}
			InputStream is = ftp.retrieveFileStream(remote);
			if (is == null)
				throw new FileNotFoundException("FTP file not found: " + hostname + "/" + remote);
			try {
				IOUtils.copy(is, file);
			} finally {
				IOUtils.closeQuietly(is);
			}
			ftp.completePendingCommand();
		}

		public void retrieve(FTPFile remote, File file, Boolean binary) throws IOException {
			retrieve(remote.getName(), file, binary);
		}

		public void retrieve(FTPFile remote, String local_path, Boolean binary) throws IOException {
			retrieve(remote.getName(), new File(local_path), binary);
		}

		public void retrieve(String remote, String local_path, Boolean binary) throws IOException {
			retrieve(remote, new File(local_path), binary);
		}

		public void sync_files(ScriptObjectMirror browser, String remote_path, File localDirectory,
				Boolean downloadOnlyIfNotExists, Boolean binary) throws IOException {

			final boolean file_method = browser != null ? browser.hasMember("file") : false;
			final boolean dir_method = browser != null ? browser.hasMember("directory") : false;
			if (!ftp.changeWorkingDirectory(remote_path))
				throw new IOException("Remote working directory change failed: " + hostname + "/" + remote_path);
			if (!localDirectory.exists())
				throw new FileNotFoundException("The destination directory does not exist: " + localDirectory);
			if (!localDirectory.isDirectory())
				throw new IOException("The destination path is not a directory: " + localDirectory);
			FTPFile[] remoteFiles = ftp.listFiles();
			if (remoteFiles == null)
				return;
			final LinkedHashMap<FTPFile, File> remoteDirs = new LinkedHashMap<>();
			for (FTPFile remoteFile : remoteFiles) {
				if (remoteFile == null)
					continue;
				final String remoteName = remoteFile.getName();
				if (".".equals(remoteName))
					continue;
				if ("..".endsWith(remoteName))
					continue;
				if (remoteFile.isDirectory()) {
					if (dir_method)
						if (Boolean.FALSE.equals(browser.callMember("directory", remote_path + '/' + remoteName)))
							continue;
					File localDir = new File(localDirectory, remoteName);
					if (!localDir.exists())
						localDir.mkdir();
					remoteDirs.put(remoteFile, localDir);
					continue;
				}
				if (!remoteFile.isFile())
					continue;
				File localFile = new File(localDirectory, remoteName);
				if (file_method)
					if (Boolean.FALSE
							.equals(browser.callMember("file", remote_path + '/' + remoteName, localFile.exists())))
						continue;
				if (downloadOnlyIfNotExists != null && downloadOnlyIfNotExists && localFile.exists())
					continue;
				if (logger.isInfoEnabled())
					logger.info("FTP download: " + hostname + '/' + remote_path + '/' + remoteName);
				retrieve(remoteFile, localFile, binary);
			}
			for (Map.Entry<FTPFile, File> entry : remoteDirs.entrySet())
				sync_files(browser, remote_path + '/' + entry.getKey().getName(), entry.getValue(),
						downloadOnlyIfNotExists, binary);
		}

		public void sync_files(ScriptObjectMirror browser, String remote_path, String local_path,
				Boolean downloadOnlyIfNotExists, Boolean binary) throws IOException {
			sync_files(browser, remote_path, new File(local_path), downloadOnlyIfNotExists, binary);
		}

		public void logout() throws IOException {
			ftp.logout();
		}

		@Override
		public void close() throws IOException {
			if (!ftp.isConnected())
				return;
			try {
				ftp.disconnect();
			} catch (IOException e) {
				logger.warn(e.getMessage(), e);
			}
		}
	}

}
