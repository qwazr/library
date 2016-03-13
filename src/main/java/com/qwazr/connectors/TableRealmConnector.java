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

import com.qwazr.database.TableServiceImpl;
import com.qwazr.database.model.ColumnDefinition;
import com.qwazr.library.AbstractLibrary;
import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.idm.PasswordCredential;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.File;
import java.security.Principal;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

public class TableRealmConnector extends AbstractLibrary implements IdentityManager {

	private static final Logger logger = LoggerFactory.getLogger(TableRealmConnector.class);

	public final String table_name = null;
	public final String login_column = null;
	public final String password_column = null;
	public final String roles_column = null;

	private TableServiceImpl tableService = null;
	private Set<String> columns = null;

	@Override
	public void load(File parentDir) {
		tableService = new TableServiceImpl();
		Set<String> tables = tableService.list(null, true);
		if (!tables.contains(table_name)) {
			tableService.createTable(table_name);
			tableService.addColumn(table_name, login_column,
					new ColumnDefinition(ColumnDefinition.Type.STRING, ColumnDefinition.Mode.INDEXED));
			tableService.addColumn(table_name, password_column,
					new ColumnDefinition(ColumnDefinition.Type.STRING, ColumnDefinition.Mode.STORED));
			tableService.addColumn(table_name, roles_column,
					new ColumnDefinition(ColumnDefinition.Type.STRING, ColumnDefinition.Mode.STORED));
		}
		columns = new HashSet<String>();
		columns.add(password_column);
		columns.add(roles_column);
	}

	@Override
	public Account verify(Account account) {
		return account;
	}

	@Override
	public Account verify(String id, Credential credential) {

		// This realm only support one type of credential
		if (!(credential instanceof PasswordCredential))
			throw new RuntimeException("Unsupported credential type: " + credential.getClass().getName());

		PasswordCredential passwordCredential = (PasswordCredential) credential;

		// We request the database
		final LinkedHashMap<String, Object> row;
		try {
			row = tableService.getRow(table_name, id, columns);
			if (row == null)
				return null;
		} catch (WebApplicationException e) {
			if (e.getResponse().getStatusInfo().getFamily() == Response.Status.Family.CLIENT_ERROR)
				return authenticationFailure("Unknown user: " + id);
			throw e;
		}

		Object password = row.get(password_column);
		if (password == null)
			return null;
		if (password instanceof String[]) {
			String[] passwordArray = (String[]) password;
			if (passwordArray.length == 0)
				return null;
			password = passwordArray[0];
		}

		// The password is stored hashed
		final String passwd = new String(passwordCredential.getPassword());
		String digest = DigestUtils.sha256Hex(passwd);
		if (!digest.equals(password))
			return authenticationFailure("Wrong password: " + id + " " + digest + '/' + passwd + '/' + password);

		//We retrieve the roles
		Object object = row.get(roles_column);
		LinkedHashSet<String> roles = new LinkedHashSet<String>();
		if (object instanceof String[]) {
			for (Object o : (String[]) object)
				roles.add(o.toString());
		} else
			roles.add(object.toString());

		return new Account() {
			@Override
			public Principal getPrincipal() {
				return new Principal() {
					@Override
					public String getName() {
						return id;
					}
				};
			}

			@Override
			public Set<String> getRoles() {
				return roles;
			}
		};
	}

	private Account authenticationFailure(String msg) {
		logger.warn(msg);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			if (logger.isWarnEnabled())
				logger.warn(e.getMessage(), e);
		}
		return null;
	}

	@Override
	public Account verify(Credential credential) {
		return null;
	}
}
