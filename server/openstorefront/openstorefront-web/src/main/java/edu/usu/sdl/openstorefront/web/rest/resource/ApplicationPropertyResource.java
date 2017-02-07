/*
 * Copyright 2014 Space Dynamics Laboratory - Utah State University Research Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.usu.sdl.openstorefront.web.rest.resource;

import edu.usu.sdl.openstorefront.core.annotation.APIDescription;
import edu.usu.sdl.openstorefront.core.annotation.DataType;
import edu.usu.sdl.openstorefront.core.api.query.QueryByExample;
import edu.usu.sdl.openstorefront.core.entity.ApplicationProperty;
import edu.usu.sdl.openstorefront.doc.annotation.RequiredParam;
import edu.usu.sdl.openstorefront.doc.security.RequireSecurity;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author dshurtleff
 */
@Path("v1/resource/applicationproperties")
@APIDescription("System application state properties.")
public class ApplicationPropertyResource
		extends BaseResource
{

	@GET
	@RequireSecurity("ADMIN-SYSTEM-MANAGEMENT")
	@APIDescription("Gets all active properties in the system")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(ApplicationProperty.class)
	public List<ApplicationProperty> getApplicationProperties()
	{
		ApplicationProperty applicationPropertyExample = new ApplicationProperty();
		applicationPropertyExample.setActiveStatus(ApplicationProperty.ACTIVE_STATUS);
		List<ApplicationProperty> applicationProperties = service.getPersistenceService().queryByExample(ApplicationProperty.class, new QueryByExample(applicationPropertyExample));
		return applicationProperties;
	}

	@GET
	@RequireSecurity("ADMIN-SYSTEM-MANAGEMENT")
	@APIDescription("Gets a property in the system")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(ApplicationProperty.class)
	@Path("/{key}")
	public Response getApplicationProperty(
			@PathParam("key")
			@RequiredParam String key)
	{
		ApplicationProperty applicationProperty = service.getSystemService().getProperty(key);
		applicationProperty = service.getPersistenceService().unwrapProxyObject(ApplicationProperty.class, applicationProperty);
		return sendSingleEntityResponse(applicationProperty);
	}

	@PUT
	@RequireSecurity("ADMIN-SYSTEM-MANAGEMENT")
	@APIDescription("Updates a property in the system. NOTE: data may need to be formatted specifically according to the property.")
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes({MediaType.WILDCARD})
	@DataType(ApplicationProperty.class)
	@Path("/{key}")
	public Response updateApplicationProperty(
			@PathParam("key")
			@RequiredParam String key,
			String value)
	{
		Response response = Response.status(Response.Status.NOT_FOUND).build();
		ApplicationProperty applicationProperty = service.getSystemService().getProperty(key);
		if (applicationProperty != null) {
			service.getSystemService().saveProperty(key, value);
			applicationProperty = service.getSystemService().getProperty(key);
			response = Response.ok(applicationProperty).build();
		}
		return response;
	}

}
