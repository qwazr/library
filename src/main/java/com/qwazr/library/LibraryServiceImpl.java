/**
 * Copyright 2015-2016 Emmanuel Keller / QWAZR
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

import com.qwazr.utils.server.ServerException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class LibraryServiceImpl implements LibraryServiceInterface {

	public Map<String, String> list() {
		Map<String, String> libraries = new LinkedHashMap<>();
		LibraryManager.getInstance().forEach((name, library) -> libraries.put(name, library.getClass().getName()));
		return libraries;
	}

	public AbstractLibrary get(String libraryName) {
		try {
			return LibraryManager.getInstance().get(libraryName);
		} catch (IOException e) {
			throw ServerException.getJsonException(e);
		}
	}

}
