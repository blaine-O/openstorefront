/*
 * Copyright 2015 Space Dynamics Laboratory - Utah State University Research Foundation.
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
import edu.usu.sdl.openstorefront.core.entity.GeneralMedia;
import edu.usu.sdl.openstorefront.core.view.FilterQueryParams;
import edu.usu.sdl.openstorefront.core.view.GeneralMediaView;
import edu.usu.sdl.openstorefront.core.view.LookupModel;
import edu.usu.sdl.openstorefront.doc.security.RequireSecurity;
import edu.usu.sdl.openstorefront.validation.ValidationResult;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author dshurtleff
 */
@Path("v1/resource/generalmedia")
@APIDescription("General media is used for articles, badges, etc.  Dynamic Resources. <br> To create a new record POST to Media.action?UploadGeneralMedia&generalMedia.name={name} *Admin Role required")
public class GeneralMediaResource
		extends BaseResource
{

	@GET
	@APIDescription("Gets all general media records.")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(GeneralMediaView.class)
	public Response getGeneralMedia(@BeanParam FilterQueryParams filterQueryParams)
	{
		ValidationResult validationResult = filterQueryParams.validate();
		if (!validationResult.valid()) {
			return sendSingleEntityResponse(validationResult.toRestError());
		}

		GeneralMedia generalMediaExample = new GeneralMedia();
		generalMediaExample.setActiveStatus(filterQueryParams.getStatus());
		List<GeneralMedia> generalMedia = service.getPersistenceService().queryByExample(GeneralMedia.class, generalMediaExample);
		generalMedia = filterQueryParams.filter(generalMedia);
		List<GeneralMediaView> generalMediaViews = GeneralMediaView.toViewList(generalMedia);

		GenericEntity<List<GeneralMediaView>> entity = new GenericEntity<List<GeneralMediaView>>(generalMediaViews)
		{
		};
		return sendSingleEntityResponse(entity);
	}

	@GET
	@APIDescription("Gets all general media records for a lookup list")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(LookupModel.class)
	@Path("/lookup")
	public Response getGeneralMediaLookup()
	{
		GeneralMedia generalMediaExample = new GeneralMedia();
		generalMediaExample.setActiveStatus(GeneralMedia.ACTIVE_STATUS);
		List<GeneralMedia> generalMedia = service.getPersistenceService().queryByExample(GeneralMedia.class, generalMediaExample);
		List<GeneralMediaView> generalMediaViews = GeneralMediaView.toViewList(generalMedia);

		List<LookupModel> lookups = new ArrayList<>();
		generalMediaViews.forEach(media -> {
			LookupModel lookupModel = new LookupModel();
			lookupModel.setCode(media.getMediaLink());
			lookupModel.setDescription(media.getName());
		});

		GenericEntity<List<LookupModel>> entity = new GenericEntity<List<LookupModel>>(lookups)
		{
		};
		return sendSingleEntityResponse(entity);
	}

	@GET
	@APIDescription("Gets a general media record. See Media.action?GeneralMedia&name={name} to get the actual resource")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(GeneralMediaView.class)
	@Path("/{name}")
	public Response getGeneralMedia(
			@PathParam("name") String name)
	{
		GeneralMedia generalMediaExample = new GeneralMedia();
		generalMediaExample.setName(name);
		GeneralMedia generalMedia = service.getPersistenceService().queryOneByExample(GeneralMedia.class, generalMediaExample);
		return sendSingleEntityResponse(generalMedia);
	}

	@GET
	@APIDescription("Check name to see if it is available. Returns true if avaliable")
	@Produces({MediaType.TEXT_PLAIN})
	@Path("/{name}/available")
	public Response checkAvailable(
			@PathParam("name") String name)
	{
		boolean available = true;
		GeneralMedia generalMediaExample = new GeneralMedia();
		generalMediaExample.setName(name);
		GeneralMedia generalMedia = service.getPersistenceService().queryOneByExample(GeneralMedia.class, generalMediaExample);
		if (generalMedia != null) {
			available = false;
		}
		return Response.ok(Boolean.toString(available), MediaType.TEXT_PLAIN).build();
	}

	@DELETE
	@RequireSecurity("ADMIN-MEDIA")
	@APIDescription("Deletes a general media record.")
	@Path("/{name}")
	public void deleteGeneralMedia(
			@PathParam("name") String name)
	{
		service.getSystemService().removeGeneralMedia(name);
	}

}
