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

import com.qwazr.library.AbstractLibrary;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import java.io.File;

public class FileCrawlerTool extends AbstractLibrary {

	@Override
	public void load(File parentDir) {
	}

	public void browse(String path, int max_depth, ScriptObjectMirror browser) {
		new Browser(browser, max_depth, new File(path));
	}

	private static class Browser {

		private final boolean file_method;
		private final boolean dir_method;
		private final int max_depth;

		private Browser(ScriptObjectMirror browser, int max_depth, File rootFile) {
			this.max_depth = max_depth;
			file_method = browser.hasMember("file");
			dir_method = browser.hasMember("directory");
			if (!rootFile.exists())
				return;
			browse(browser, rootFile, 0);
		}

		private boolean browse(ScriptObjectMirror browser, File file, int depth) {
			if (file.isFile())
				return browseFile(browser, file, depth);
			else if (file.isDirectory() && depth < max_depth)
				return browseDir(browser, file, depth + 1);
			return true;
		}

		private boolean browseFile(ScriptObjectMirror browser, File file, int depth) {
			if (!file_method)
				return false;
			return !Boolean.FALSE.equals(browser.callMember("file", file, depth));
		}

		private boolean browseDir(ScriptObjectMirror browser, File dir, int depth) {
			if (dir_method)
				if (Boolean.FALSE.equals(browser.callMember("directory", dir, depth)))
					return false;
			File[] files = dir.listFiles();
			for (File file : files)
				if (!browse(browser, file, depth))
					return false;
			return true;
		}
	}

}
