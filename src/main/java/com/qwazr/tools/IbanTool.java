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
import org.iban4j.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class IbanTool extends AbstractLibrary {

	public final Map<String, String> error_messages = null;

	@Override
	public void load(File data_directory) throws IOException {

	}

	public String check_error_compact(String iban) {
		try {
			IbanUtil.validate(iban);
			return null;
		} catch (IbanFormatException | InvalidCheckDigitException | UnsupportedCountryException e) {
			return e.getMessage();
		}
	}

	public String check_error_default(String iban) {
		try {
			IbanUtil.validate(iban, IbanFormat.Default);
			return null;
		} catch (IbanFormatException | InvalidCheckDigitException | UnsupportedCountryException e) {
			return e.getMessage();
		}
	}
}
