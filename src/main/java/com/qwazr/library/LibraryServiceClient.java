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

import com.fasterxml.jackson.core.type.TypeReference;
import com.qwazr.utils.json.client.JsonClientAbstract;
import org.apache.http.client.fluent.Request;

import java.net.URISyntaxException;
import java.util.Map;

public class LibraryServiceClient extends JsonClientAbstract implements LibraryServiceInterface {

	private final static String TOOLS_PREFIX = "/tools/";

	LibraryServiceClient(String url, Integer msTimeOut) throws URISyntaxException {
		super(url, msTimeOut);
	}

	public final static TypeReference<Map<String, String>> MapStringStringTypeRef = new TypeReference<Map<String, String>>() {
	};

	public Map<String, String> list() {
		UBuilder uriBuilder = new UBuilder(TOOLS_PREFIX);
		Request request = Request.Get(uriBuilder.build());
		return commonServiceRequest(request, null, null, MapStringStringTypeRef, 200);
	}

	public final static TypeReference<Map<String, Object>> MapStringObjectTypeRef = new TypeReference<Map<String, Object>>() {
	};

	public Map<String, Object> get(String toolName) {
		UBuilder uriBuilder = new UBuilder(TOOLS_PREFIX, toolName);
		Request request = Request.Get(uriBuilder.build());
		return commonServiceRequest(request, null, null, MapStringObjectTypeRef, 200);
	}

}
