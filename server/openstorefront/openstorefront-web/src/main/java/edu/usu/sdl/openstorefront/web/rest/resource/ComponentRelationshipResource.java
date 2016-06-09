/*
 * Copyright 2016 Space Dynamics Laboratory - Utah State University Research Foundation.
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
import edu.usu.sdl.openstorefront.core.entity.ComponentRelationship;
import edu.usu.sdl.openstorefront.core.view.ComponentRelationshipView;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 *
 * @author dshurtleff
 */
@Path("v1/resource/componentrelationship")
@APIDescription("Provides access to relationships acrossed components")
public class ComponentRelationshipResource
	extends BaseResource
{
	
	@GET
	@APIDescription("Gets active, target approved and owner approved relationships")
	@Produces(
	{
		MediaType.APPLICATION_JSON
	})
	@DataType(ComponentRelationshipView.class)
	public Response getRelationships(			
	){
		ComponentRelationship componentRelationship = new ComponentRelationship();
		componentRelationship.setActiveStatus(ComponentRelationship.ACTIVE_STATUS);
		
		List<ComponentRelationship> relationships = componentRelationship.findByExample();
		
		List<ComponentRelationshipView> views = ComponentRelationshipView.toViewList(relationships);
		views = views.stream()
				.filter(r -> r.getOwnerApproved() && r.getTargetApproved())
				.collect(Collectors.toList());
				
		GenericEntity<List<ComponentRelationshipView>> entity = new GenericEntity<List<ComponentRelationshipView>>(views)
		{
		};
		return sendSingleEntityResponse(entity);				
	}
	
}
