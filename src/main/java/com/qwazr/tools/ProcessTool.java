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
import com.qwazr.utils.ScriptUtils;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProcessTool extends AbstractLibrary {

	final public List<String> commands = null;
	final public Map<String, String> environment_variables = null;
	final public String working_directory = null;

	@Override
	public void load(File parentDir) {
	}

	public Process execute(File workingDirectory, List<String> commandAndArgs, Map<String, String> env, File outputFile,
			File errorFile) throws IOException {

		List<String> commandsAndArgs = new ArrayList<String>();
		if (commandAndArgs != null && !commandAndArgs.isEmpty())
			commandsAndArgs.addAll(commandAndArgs);
		else if (commands != null)
			commandsAndArgs.addAll(commands);

		// create the process builder
		final ProcessBuilder processBuilder = new ProcessBuilder(commandsAndArgs);
		if (environment_variables != null)
			processBuilder.environment().putAll(environment_variables);
		if (env != null)
			processBuilder.environment().putAll(environment_variables);

		// Set the working directory
		final File workingDirectoryFile;
		if (workingDirectory != null)
			workingDirectoryFile = workingDirectory;
		else if (working_directory != null)
			workingDirectoryFile = new File(working_directory);
		else
			workingDirectoryFile = null;
		if (workingDirectoryFile != null) {
			if (!workingDirectoryFile.exists())
				throw new IOException("The path does not exist: " + workingDirectoryFile);
			if (!workingDirectoryFile.isDirectory())
				throw new IOException("The path is not a directory: " + workingDirectoryFile);
			processBuilder.directory(workingDirectoryFile);
		}

		// Set IN/OUT
		if (errorFile != null) {
			if (errorFile == outputFile)
				processBuilder.redirectErrorStream(true);
			else
				processBuilder.redirectError(errorFile);
		}
		if (outputFile != null)
			processBuilder.redirectOutput(outputFile);

		// Execute
		return processBuilder.start();
	}

	/**
	 * Call this command from Javascript to execute a local binary.
	 * <pre>
	 * {@code
	 * {
	 *     "working_directory": "/path/to/directory",
	 *     "environment_variables": {
	 *         "lang": "de",
	 *         "hello": "world"
	 *     },
	 *     "commands": ["convert", "my.jpg", "my.gif"],
	 *     "output_file": "/path/to/out.log",
	 *     "error_file": "/path/to/in.log"
	 * }
	 * }
	 * </pre>
	 *
	 * @param som The Javascript object
	 * @return the launched process
	 * @throws ScriptException if any Javascript error occurs
	 * @throws IOException     if any I/O error occurs
	 */
	public Process execute(ScriptObjectMirror som) throws ScriptException, IOException {

		// Extract the working directoru
		String workingDirectory = (String) som.get("working_directory");
		final File workingDirectoryFile = workingDirectory != null ? new File(workingDirectory) : null;

		// Extracts the arguments
		ScriptObjectMirror jsCommands = (ScriptObjectMirror) som.get("commands");
		final List<String> commands;
		if (jsCommands != null) {
			commands = new ArrayList<String>(jsCommands.size());
			ScriptUtils.fillStringCollection(jsCommands, commands);
		} else
			commands = null;

		// Set the environment variables
		ScriptObjectMirror jsEnv = (ScriptObjectMirror) som.get("environment_variables");
		final Map<String, String> envVars;
		if (jsEnv != null) {
			envVars = new LinkedHashMap<String, String>();
			ScriptUtils.fillStringMap(jsEnv, envVars);
		} else
			envVars = null;

		// Set the output and error files
		String outputPath = (String) som.get("output_file");
		final File outputFile = outputPath != null ? new File(outputPath) : null;
		String errorPath = (String) som.get("error_file");
		final File errorFile = errorPath != null ? new File(errorPath) : null;

		return execute(workingDirectoryFile, commands, envVars, outputFile, errorFile);
	}

}