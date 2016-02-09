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
package com.qwazr.library.test;

import com.qwazr.library.LibraryManager;
import com.qwazr.utils.file.TrackedDirectory;
import org.apache.commons.io.filefilter.FileFileFilter;

import java.io.File;
import java.io.IOException;

public abstract class AbstractLibraryTest {

	protected void before() throws IOException {
		final LibraryManager libraryManager = LibraryManager.getInstance();
		if (libraryManager != null)
			return;
		TrackedDirectory etcTracker = new TrackedDirectory(new File("src/test/resources/etc"), FileFileFilter.FILE);
		LibraryManager.load(new File("src/test/resources"), etcTracker);
		etcTracker.check();
	}

}
