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

import com.qwazr.library.annotations.Library;
import com.qwazr.utils.file.TrackedDirectory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

public interface LibraryManager extends Map<String, AbstractLibrary> {

	static void load(File dataDirectory, TrackedDirectory etcTracker) throws IOException {
		LibraryManagerImpl.load(dataDirectory, etcTracker);
	}

	static LibraryManager getInstance() {
		return LibraryManagerImpl.INSTANCE;
	}

	<T extends AbstractLibrary> T getLibrary(String name);

	Map<String, String> getLibraries();

	static void inject(Object object) throws IllegalAccessException {
		if (object == null)
			return;
		LibraryManager manager = getInstance();
		if (manager == null)
			return;
		Field[] fields = object.getClass().getFields();
		if (fields == null)
			return;
		for (Field field : fields) {
			Library library = field.getAnnotation(Library.class);
			if (library == null)
				continue;
			AbstractLibrary libraryItem = manager.getLibrary(library.value());
			if (libraryItem == null)
				continue;
			field.set(object, libraryItem);
		}
	}

}
