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

import edu.usu.sdl.openstorefront.common.exception.AttachedReferencesException;
import edu.usu.sdl.openstorefront.common.util.ReflectionUtil;
import edu.usu.sdl.openstorefront.core.annotation.APIDescription;
import edu.usu.sdl.openstorefront.core.annotation.DataType;
import edu.usu.sdl.openstorefront.core.api.query.GenerateStatementOption;
import edu.usu.sdl.openstorefront.core.api.query.QueryByExample;
import edu.usu.sdl.openstorefront.core.api.query.SpecialOperatorModel;
import edu.usu.sdl.openstorefront.core.entity.Organization;
import edu.usu.sdl.openstorefront.core.model.OrgReference;
import edu.usu.sdl.openstorefront.core.view.FilterQueryParams;
import edu.usu.sdl.openstorefront.core.view.LookupModel;
import edu.usu.sdl.openstorefront.core.view.OrganizationView;
import edu.usu.sdl.openstorefront.core.view.OrganizationWrapper;
import edu.usu.sdl.openstorefront.doc.security.RequireAdmin;
import edu.usu.sdl.openstorefront.validation.ValidationModel;
import edu.usu.sdl.openstorefront.validation.ValidationResult;
import edu.usu.sdl.openstorefront.validation.ValidationUtil;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import net.sourceforge.stripes.util.bean.BeanUtil;

/**
 *
 * @author dshurtleff
 */
@Path("v1/resource/organizations")
@APIDescription("Organization information")
public class OrganizationResource
		extends BaseResource
{

	@GET
	@APIDescription("Gets organization records.")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(OrganizationView.class)
	public Response getOrganizations(@BeanParam FilterQueryParams filterQueryParams)
	{
		ValidationResult validationResult = filterQueryParams.validate();
		if (!validationResult.valid()) {
			return sendSingleEntityResponse(validationResult.toRestError());
		}

		Organization organizationExample = new Organization();
		organizationExample.setActiveStatus(filterQueryParams.getStatus());

		Organization organizationStartExample = new Organization();
		organizationStartExample.setUpdateDts(filterQueryParams.getStart());

		Organization organizationEndExample = new Organization();
		organizationEndExample.setUpdateDts(filterQueryParams.getEnd());

		QueryByExample queryByExample = new QueryByExample(organizationExample);

		SpecialOperatorModel specialOperatorModel = new SpecialOperatorModel();
		specialOperatorModel.setExample(organizationStartExample);
		specialOperatorModel.getGenerateStatementOption().setOperation(GenerateStatementOption.OPERATION_GREATER_THAN);
		queryByExample.getExtraWhereCauses().add(specialOperatorModel);

		specialOperatorModel = new SpecialOperatorModel();
		specialOperatorModel.setExample(organizationEndExample);
		specialOperatorModel.getGenerateStatementOption().setOperation(GenerateStatementOption.OPERATION_LESS_THAN_EQUAL);
		specialOperatorModel.getGenerateStatementOption().setParameterSuffix(GenerateStatementOption.PARAMETER_SUFFIX_END_RANGE);
		queryByExample.getExtraWhereCauses().add(specialOperatorModel);

		queryByExample.setMaxResults(filterQueryParams.getMax());
		queryByExample.setFirstResult(filterQueryParams.getOffset());
		queryByExample.setSortDirection(filterQueryParams.getSortOrder());

		Organization organizationSortExample = new Organization();
		Field sortField = ReflectionUtil.getField(organizationSortExample, filterQueryParams.getSortField());
		if (sortField != null) {
			BeanUtil.setPropertyValue(sortField.getName(), organizationSortExample, QueryByExample.getFlagForType(sortField.getType()));
			queryByExample.setOrderBy(organizationSortExample);
		}

		List<Organization> organizations = service.getPersistenceService().queryByExample(Organization.class, queryByExample);

		OrganizationWrapper organizationWrapper = new OrganizationWrapper();
		organizationWrapper.getData().addAll(OrganizationView.toView(organizations));
		organizationWrapper.setTotalNumber(service.getPersistenceService().countByExample(queryByExample));

		return sendSingleEntityResponse(organizationWrapper);
	}

	@GET
	@APIDescription("Gets an organization record. ")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(Organization.class)
	@Path("/{id}")
	public Response getOrganization(
			@PathParam("id") String organizationId
	)
	{
		Organization organizationExample = new Organization();
		organizationExample.setOrganizationId(organizationId);
		return sendSingleEntityResponse(organizationExample.find());
	}
	
	@GET
	@APIDescription("Gets an organization record by name. ")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(Organization.class)
	@Path("/name/{name}")
	public Response getOrganizationByName(
			@PathParam("name") String name
	)
	{
		Organization organizationExample = new Organization();
		organizationExample.setName(name);
		return sendSingleEntityResponse(organizationExample.find());
	}	

	@GET
	@APIDescription("Gets an organization references. ")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(OrgReference.class)
	@Path("/{id}/references")
	public List<OrgReference> getReferences(
			@PathParam("id") String organizationId,
			@QueryParam("activeOnly") boolean activeOnly,
			@QueryParam("approvedOnly") boolean approvedOnly
	)
	{
		return service.getOrganizationService().findReferences(organizationId, activeOnly, approvedOnly);
	}

	@GET
	@APIDescription("Gets references that do not have organization . ")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(OrgReference.class)
	@Path("/references")
	public List<OrgReference> getReferencesNoOrg(
			@QueryParam("activeOnly") boolean activeOnly,
			@QueryParam("approvedOnly") boolean approvedOnly
	)
	{
		return service.getOrganizationService().findReferences(null, activeOnly, approvedOnly);
	}
	
	
	@GET
	@APIDescription("Get a list of active organizations for selection list.")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(LookupModel.class)
	@Path("/lookup")
	public Response getLookupList()
	{
		List<LookupModel> lookupModels = new ArrayList<>();
		Organization organizationExample = new Organization();
		organizationExample.setActiveStatus(Organization.ACTIVE_STATUS);
		List<Organization> organizations = organizationExample.findByExample();
		organizations.forEach(organization -> {
			LookupModel lookupModel = new LookupModel();
			lookupModel.setCode(organization.getOrganizationId());
			lookupModel.setDescription(organization.getName());
			lookupModels.add(lookupModel);
		});

		GenericEntity<List<LookupModel>> entity = new GenericEntity<List<LookupModel>>(lookupModels)
		{
		};
		return sendSingleEntityResponse(entity);
	}

	@POST
	@APIDescription("Gets an organization references by organization name.")
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes({MediaType.TEXT_PLAIN})
	@DataType(OrgReference.class)
	@Path("/referencesByName")
	public List<OrgReference> getReferencesByName(
			@QueryParam("activeOnly") boolean activeOnly,
			@QueryParam("approvedOnly") boolean approvedOnly,
			String name)
	{
		return service.getOrganizationService().findReferences(name, activeOnly, approvedOnly);
	}

	@POST
	@RequireAdmin
	@APIDescription("Creates an organization")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response createOrganization(Organization organization)
	{
		return handleSaveOrganization(organization, true);
	}

	@PUT
	@RequireAdmin
	@APIDescription("Updates an organization")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/{id}")
	public Response createOrganization(
			@PathParam("id") String organizationId,
			Organization organization)
	{
		Organization existing = new Organization();
		existing.setOrganizationId(organizationId);
		existing = existing.find();
		if (existing == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		organization.setOrganizationId(organizationId);
		return handleSaveOrganization(organization, false);
	}

	private Response handleSaveOrganization(Organization organization, boolean post)
	{
		ValidationModel validationModel = new ValidationModel(organization);
		validationModel.setConsumeFieldsOnly(true);
		ValidationResult validationResult = ValidationUtil.validate(validationModel);
		if (validationResult.valid()) {
			service.getOrganizationService().saveOrganization(organization);
		} else {
			return Response.ok(validationResult.toRestError()).build();
		}
		if (post) {
			return Response.created(URI.create("v1/resource/organization/" + organization.getOrganizationId())).entity(organization).build();
		} else {
			return Response.ok(organization).build();
		}
	}

	@POST
	@RequireAdmin
	@APIDescription("Merges one organization with another")
	@Consumes({MediaType.APPLICATION_JSON})
	@Path("/{targetId}/merge/{mergeId}")
	public Response merge(
			@PathParam("targetId") String targetId,
			@PathParam("mergeId") String mergeId
	)
	{
		Response response = Response.status(Status.NOT_FOUND).build();

		Organization target = new Organization();
		target.setOrganizationId(targetId);
		target = target.find();

		Organization merge = new Organization();
		merge.setOrganizationId(mergeId);
		merge = merge.find();
		if (target != null && merge != null) {
			service.getOrganizationService().mergeOrganizations(targetId, mergeId);
			response = Response.ok(target).build();
		}

		return response;
	}

	@POST
	@RequireAdmin
	@APIDescription("Extract organizations from the data")
	@Path("/extract")
	public Response extractFromData()
	{
		Response response = Response.ok().build();
		service.getOrganizationService().extractOrganizations();
		return response;
	}

	@DELETE
	@RequireAdmin
	@APIDescription("Deletes an organization")
	@Path("/{id}")
	public void deleteReport(
			@PathParam("id") String organizationid) throws AttachedReferencesException
	{
		service.getOrganizationService().removeOrganization(organizationid);
	}

}
