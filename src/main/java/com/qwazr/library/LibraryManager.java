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

	static void inject(Object object) {
		if (object == null)
			return;
		LibraryManager manager = getInstance();
		if (manager == null)
			return;
		inject(manager, object, object.getClass());
	}

	static void inject(LibraryManager manager, Object object, Class<?> clazz) {
		if (clazz == null || clazz.isPrimitive())
			return;
		inject(manager, object, clazz.getDeclaredFields());
		Class<?> nextClazz = clazz.getSuperclass();
		if (nextClazz == clazz)
			return;
		inject(manager, object, nextClazz);
	}

	static void inject(LibraryManager manager, Object object, Field[] fields) {
		if (fields == null)
			return;
		try {
			for (Field field : fields) {
				Library library = field.getAnnotation(Library.class);
				if (library == null)
					continue;
				AbstractLibrary libraryItem = manager.getLibrary(library.value());
				if (libraryItem == null)
					continue;
				field.setAccessible(true);
				field.set(object, libraryItem);
			}
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

}
