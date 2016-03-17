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
package edu.usu.sdl.openstorefront.web.rest.service;

import au.com.bytecode.opencsv.CSVWriter;
import edu.usu.sdl.openstorefront.common.util.TimeUtil;
import edu.usu.sdl.openstorefront.core.annotation.APIDescription;
import edu.usu.sdl.openstorefront.core.annotation.DataType;
import edu.usu.sdl.openstorefront.core.api.model.TaskRequest;
import edu.usu.sdl.openstorefront.core.api.query.QueryByExample;
import edu.usu.sdl.openstorefront.core.api.query.QueryType;
import edu.usu.sdl.openstorefront.core.entity.ApprovalStatus;
import edu.usu.sdl.openstorefront.core.entity.AttributeCodePk;
import edu.usu.sdl.openstorefront.core.entity.Component;
import edu.usu.sdl.openstorefront.core.model.search.AdvanceSearchResult;
import edu.usu.sdl.openstorefront.core.model.search.SearchModel;
import edu.usu.sdl.openstorefront.core.model.search.SearchSuggestion;
import edu.usu.sdl.openstorefront.core.sort.ComponentSearchViewComparator;
import edu.usu.sdl.openstorefront.core.sort.RecentlyAddedViewComparator;
import edu.usu.sdl.openstorefront.core.view.ComponentSearchView;
import edu.usu.sdl.openstorefront.core.view.ComponentSearchWrapper;
import edu.usu.sdl.openstorefront.core.view.FilterQueryParams;
import edu.usu.sdl.openstorefront.core.view.ListingStats;
import edu.usu.sdl.openstorefront.core.view.RecentlyAddedView;
import edu.usu.sdl.openstorefront.core.view.RestErrorModel;
import edu.usu.sdl.openstorefront.core.view.SearchQuery;
import edu.usu.sdl.openstorefront.doc.annotation.RequiredParam;
import edu.usu.sdl.openstorefront.doc.security.RequireAdmin;
import edu.usu.sdl.openstorefront.validation.ValidationResult;
import edu.usu.sdl.openstorefront.web.rest.resource.BaseResource;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Search Service
 *
 * @author dshurtleff
 */
@Path("v1/service/search")
@APIDescription("Provides access to search listing in the application")
public class Search
		extends BaseResource
{

	@GET
	@APIDescription("Searches listing according to parameters.  (Components, Articles)")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(ComponentSearchView.class)
	public Response searchListing(
			@QueryParam("paging") @APIDescription("Returns the data wrapped for paging") boolean paging,
			@BeanParam SearchQuery query,
			@BeanParam FilterQueryParams filterQueryParams)
	{
		ValidationResult validationResult = filterQueryParams.validate();
		if (!validationResult.valid()) {
			return sendSingleEntityResponse(validationResult.toRestError());
		}

		ComponentSearchWrapper results = service.getSearchService().getSearchItems(query, filterQueryParams);
		results.getData().sort(new ComponentSearchViewComparator());
		if (paging) {
			return sendSingleEntityResponse(results);
		} else {
			GenericEntity<List<ComponentSearchView>> entity = new GenericEntity<List<ComponentSearchView>>(results.getData())
			{
			};
			return sendSingleEntityResponse(entity);
		}
	}

	@POST
	@APIDescription("Advance search of listing ")
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_JSON})
	@DataType(ComponentSearchView.class)
	@Path("/advance")
	public Response advanceSearch(
			@QueryParam("paging") @APIDescription("Returns the data wrapped for paging") boolean paging,
			@BeanParam FilterQueryParams filterQueryParams,
			SearchModel searchModel)
	{
		ValidationResult validationResult = filterQueryParams.validate();
		if (!validationResult.valid()) {
			return sendSingleEntityResponse(validationResult.toRestError());
		}

		if (searchModel == null) {
			RestErrorModel restErrorModel = new RestErrorModel();
			restErrorModel.getErrors().put("searchModel", "Search Model must be pass to this call see API documentation");
			return sendSingleEntityResponse(restErrorModel);
		}
		searchModel.setStartOffset(filterQueryParams.getOffset());
		searchModel.setMax(filterQueryParams.getMax());
		searchModel.setSortField(filterQueryParams.getSortField());
		searchModel.setSortDirection(filterQueryParams.getSortOrder());

		AdvanceSearchResult result = service.getSearchService().advanceSearch(searchModel);
		if (result.getValidationResult().valid()) {
			if (paging) {
				ComponentSearchWrapper searchWrapper = new ComponentSearchWrapper();
				searchWrapper.setData(result.getResults());
				searchWrapper.setResults(result.getResults().size());
				searchWrapper.setTotalNumber(result.getTotalNumber());
				searchWrapper.setResultTypeStats(result.getResultTypeStats());				
				return sendSingleEntityResponse(searchWrapper);
			} else {
				GenericEntity<List<ComponentSearchView>> entity = new GenericEntity<List<ComponentSearchView>>(result.getResults())
				{
				};
				return sendSingleEntityResponse(entity);
			}
		} else {
			return sendSingleEntityResponse(result.getValidationResult().toRestError());
		}
	}

	@DELETE
	@RequireAdmin
	@APIDescription("Removes all indexes from Solr")
	@Consumes({MediaType.APPLICATION_JSON})
	@Path("/clearSolr")
	public Response clearSolr()
	{
		service.getSearchService().deleteAll();
		return Response.noContent().build();
	}

	@POST
	@RequireAdmin
	@APIDescription("Removes all indexes from Solr and then re-indexes current components and articles")
	@Path("/resetSolr")
	public Response resetSolr()
	{
		TaskRequest taskRequest = new TaskRequest();
		taskRequest.setAllowMultiple(false);
		taskRequest.setName("Resetting Indexer");
		taskRequest.setDetails("Reindexing components and articles");
		service.getAsyncProxy(service.getSearchService(), taskRequest).resetIndexer();
		return Response.ok().build();
	}

	@GET
	@APIDescription("Searches listing by an archtecture.")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(ComponentSearchView.class)
	@Path("/attribute/{type}/{code}")
	public Response searchListing(
			@PathParam("type")
			@RequiredParam String type,
			@PathParam("code")
			@RequiredParam String code,
			@BeanParam FilterQueryParams filterQueryParams)
	{
		ValidationResult validationResult = filterQueryParams.validate();
		if (!validationResult.valid()) {
			return sendSingleEntityResponse(validationResult.toRestError());
		}

		AttributeCodePk pk = new AttributeCodePk();

		pk.setAttributeCode(code);
		pk.setAttributeType(type);

		List<ComponentSearchView> results = service.getSearchService().architectureSearch(pk, filterQueryParams);
		results.sort(new ComponentSearchViewComparator());
		GenericEntity<List<ComponentSearchView>> entity = new GenericEntity<List<ComponentSearchView>>(results)
		{
		};
		return sendSingleEntityResponse(entity);
	}

	@GET
	@APIDescription("Used to retrieve all possible search results.")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(ComponentSearchView.class)
	@Path("/all")
	public List<ComponentSearchView> getAllForSearch()
	{
		List<ComponentSearchView> results = service.getSearchService().getAll();
		Collections.sort(results, new ComponentSearchViewComparator());
		return results;
	}

	@GET
	@APIDescription("Gets the recently added items")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(RecentlyAddedView.class)
	@Path("/recent")
	public List<RecentlyAddedView> getRecentlyAdded(
			@DefaultValue("5")
			@QueryParam("max") int maxResults)
	{
		List<RecentlyAddedView> recentlyAddedViews = new ArrayList<>();

		//We only need to look at components now (all types)
		List<Component> components = service.getComponentService().findRecentlyAdded(maxResults);

		for (Component component : components) {
			recentlyAddedViews.add(RecentlyAddedView.toView(component));
		}

		recentlyAddedViews.sort(new RecentlyAddedViewComparator<>());
		if (recentlyAddedViews.size() > maxResults) {
			recentlyAddedViews = recentlyAddedViews.subList(0, maxResults);
		}

		return recentlyAddedViews;
	}

	@GET
	@APIDescription("Get Listing Stats")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(ListingStats.class)
	@Path("/stats")
	public Response getListingStats()
	{
		ListingStats listingStats = new ListingStats();

		Component componentExample = new Component();
		componentExample.setActiveStatus(Component.ACTIVE_STATUS);
		componentExample.setApprovalState(ApprovalStatus.APPROVED);
		long numberOfActiveComponents = service.getPersistenceService().countByExample(new QueryByExample(QueryType.COUNT, componentExample));
		listingStats.setNumberOfComponents(numberOfActiveComponents);

		return Response.ok(listingStats).build();
	}

	@POST
	@APIDescription("Export a set entries")
	@Produces({"application/csv"})	
	@Path("/export")
	public Response export(
			@FormParam("multipleIds")					
			@RequiredParam List<String> ids			
	)
	{
		StringWriter writer = new StringWriter();
		CSVWriter cvsWriter = new CSVWriter(writer);
		
		String header[] = {
			"Name", 
			"Organization",
			"Description",
			"Last Updated Dts",
			"Entry Type"
		};
		cvsWriter.writeNext(header);
		
		SimpleDateFormat sdf = TimeUtil.standardDateFormater();
		List<ComponentSearchView> views = service.getComponentService().getSearchComponentList(ids);
		for (ComponentSearchView view : views) {
			String data[] = {
				view.getName(),
				view.getOrganization(),
				view.getDescription(),
				sdf.format(view.getLastActivityDts()),
				view.getComponentTypeDescription()
			};
			cvsWriter.writeNext(data);		
		}
		
		Response.ResponseBuilder response = Response.ok(writer.toString());
		response.header("Content-Type", "application/csv");
		response.header("Content-Disposition", "attachment; filename=\"searchResults.csv\"");
		return response.build();		
	}
	
	@GET
	@APIDescription("Get Search Suggestions")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(SearchSuggestion.class)
	@Path("/suggestions")
	public List<SearchSuggestion> getSearchSuggestions(
		@QueryParam("query")
		@DefaultValue("*") String query,
		@QueryParam("max") 
		@DefaultValue("6") int maxResults	
	)			
	{	
		List<SearchSuggestion> suggestions = service.getSearchService().searchSuggestions(query, maxResults);		
		return suggestions;
	}
	
}
