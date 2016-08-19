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
import com.qwazr.utils.AnnotationsUtils;
import com.qwazr.utils.file.TrackedInterface;
import com.qwazr.utils.server.GenericServer;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public interface LibraryManager extends Map<String, LibraryInterface>, GenericServer.IdentityManagerProvider {

	static void load(final File dataDirectory, final TrackedInterface etcTracker) throws IOException {
		LibraryManagerImpl.load(dataDirectory, etcTracker);
	}

	static LibraryManager getInstance() {
		return LibraryManagerImpl.INSTANCE;
	}

	<T extends LibraryInterface> T getLibrary(String name);

	Map<String, String> getLibraries();

	File getDataDirectory();

	static void inject(Object object) {
		if (object == null)
			return;
		final LibraryManager manager = getInstance();
		if (manager == null)
			return;
		AnnotationsUtils.browseFieldsRecursive(object.getClass(), field -> {
			final Library library = field.getAnnotation(Library.class);
			if (library == null)
				return;
			final LibraryInterface libraryItem = manager.getLibrary(library.value());
			if (libraryItem == null)
				return;
			field.setAccessible(true);
			try {
				field.set(object, libraryItem);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		});
	}

}
