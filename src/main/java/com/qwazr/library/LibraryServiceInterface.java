/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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
 */
package com.qwazr.library;

import com.qwazr.server.ServiceInterface;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.Map;

@Path("/" + LibraryServiceInterface.SERVICE_NAME)
@RolesAllowed(LibraryServiceInterface.SERVICE_NAME)
public interface LibraryServiceInterface extends ServiceInterface {

	String SERVICE_NAME = "library";

	@GET
	@Path("/")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	Map<String, String> getLibraries();

	@GET
	@Path("/{library_name}")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	LibraryInterface getLibrary(@PathParam("library_name") String library_name);

	void inject(final Object object);
}