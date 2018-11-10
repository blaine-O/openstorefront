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
package edu.usu.sdl.openstorefront.service.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.usu.sdl.openstorefront.common.exception.OpenStorefrontRuntimeException;
import edu.usu.sdl.openstorefront.common.manager.Initializable;
import edu.usu.sdl.openstorefront.common.manager.PropertiesManager;
import edu.usu.sdl.openstorefront.common.util.Convert;
import edu.usu.sdl.openstorefront.common.util.OpenStorefrontConstant;
import edu.usu.sdl.openstorefront.common.util.StringProcessor;
import edu.usu.sdl.openstorefront.core.api.SearchService;
import edu.usu.sdl.openstorefront.core.entity.ApprovalStatus;
import edu.usu.sdl.openstorefront.core.entity.Component;
import edu.usu.sdl.openstorefront.core.entity.ComponentAttribute;
import edu.usu.sdl.openstorefront.core.entity.ComponentReview;
import edu.usu.sdl.openstorefront.core.entity.ComponentTag;
import edu.usu.sdl.openstorefront.core.entity.ErrorTypeCode;
import edu.usu.sdl.openstorefront.core.entity.SearchOptions;
import edu.usu.sdl.openstorefront.core.model.search.SearchSuggestion;
import edu.usu.sdl.openstorefront.core.view.ComponentSearchView;
import edu.usu.sdl.openstorefront.core.view.ComponentSearchWrapper;
import edu.usu.sdl.openstorefront.core.view.FilterQueryParams;
import edu.usu.sdl.openstorefront.core.view.SearchQuery;
import edu.usu.sdl.openstorefront.service.ServiceProxy;
import edu.usu.sdl.openstorefront.service.manager.resource.ElasticSearchClient;
import edu.usu.sdl.openstorefront.service.search.IndexSearchResult;
import edu.usu.sdl.openstorefront.service.search.SearchStatTable;
import edu.usu.sdl.openstorefront.service.search.SolrComponentModel;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

/**
 *
 * @author dshurtleff
 */
public class ElasticSearchManager
		extends BaseSearchManager
		implements Initializable, PooledResourceManager<ElasticSearchClient>
{

	private static final Logger LOG = Logger.getLogger(ElasticSearchManager.class.getName());

	private static final String INDEX = "openstorefront";
	private static final String INDEX_TYPE = "component";
	private static final String ELASTICSEARCH_ALL_FIELDS = "_all";
	private static final String DEFAULT_POOL_SIZE = "40";

	private static AtomicBoolean started = new AtomicBoolean(false);
	private static AtomicBoolean indexCreated = new AtomicBoolean(false);

	private BlockingQueue<ElasticSearchClient> clientPool;
	private int maxPoolSize;
	private static ElasticSearchManager singleton = null;
	private PropertiesManager propertiesManager;
	private ServiceProxy service;

	protected ElasticSearchManager(ServiceProxy service, PropertiesManager propertiesManager, BlockingQueue<ElasticSearchClient> clientPool)
	{
		this.clientPool = clientPool;
		this.propertiesManager = propertiesManager;
		this.service = service;
	}

	public static ElasticSearchManager getInstance()
	{
		return getInstance(ServiceProxy.getProxy(), PropertiesManager.getInstance(), null);
	}

	public static ElasticSearchManager getInstance(PropertiesManager propertiesManager)
	{
		if (singleton == null) {
			singleton = new ElasticSearchManager(ServiceProxy.getProxy(), propertiesManager, null);
		}
		return singleton;
	}

	public static ElasticSearchManager getInstance(ServiceProxy service, PropertiesManager propertiesManager, BlockingQueue<ElasticSearchClient> clientPool)
	{
		if (singleton == null) {
			singleton = new ElasticSearchManager(service, propertiesManager, clientPool);
		}
		return singleton;
	}

	private void init()
	{
		String poolSize = propertiesManager.getValue(PropertiesManager.KEY_ELASTIC_SEARCH_POOL, DEFAULT_POOL_SIZE);
		maxPoolSize = Convert.toInteger(poolSize);
		LOG.log(Level.INFO, () -> "Initializing Elasticsearch Pool size: " + maxPoolSize);

		String host = propertiesManager.getValue(PropertiesManager.KEY_ELASTIC_HOST, "localhost");
		Integer port = Convert.toInteger(propertiesManager.getValue(PropertiesManager.KEY_ELASTIC_PORT, "9200"));
		LOG.log(Level.INFO, () -> "Initializing Elasticsearch Connection to server: " + host + " port: " + port);
		clientPool = new ArrayBlockingQueue<>(maxPoolSize, true);

		for (int i = 0; i < maxPoolSize; i++) {
			ElasticSearchClient client = createClient(host, port);
			if (!clientPool.offer(client)) {
				LOG.log(Level.FINER, "Client not added to Pool; Pool was full.");
			}
		}
	}

	protected ElasticSearchClient createClient(String host, Integer port)
	{
		return new ElasticSearchClient(host, port, this);
	}

	/**
	 * Close client when done. This will create a new client for each call.
	 *
	 * @return a new client
	 */
	@Override
	public ElasticSearchClient getClient()
	{
		checkSearchIndexCreation();
		ElasticSearchClient client = justGetClient();
		return client;
	}

	private ElasticSearchClient justGetClient()
	{
		if (!isStarted()) {
			throw new OpenStorefrontRuntimeException("Search Manager is not started", "Restart search manager and try again (system admin)");
		}

		if (clientPool.isEmpty()) {
			throw new OpenStorefrontRuntimeException("No elasticsearch client avaliable for searching", "Check pool size and restart search server (system admin)");
		}

		int waitTimeSeconds = Convert.toInteger(propertiesManager.getValue(PropertiesManager.KEY_ELASTIC_CONNECTION_WAIT_TIME, "60"));
		try {
			ElasticSearchClient client = clientPool.poll(waitTimeSeconds, TimeUnit.SECONDS);
			if (client == null) {
				throw new OpenStorefrontRuntimeException("Unable to retrieve Elasticsearch Connection in time.  No resource available.", "Adjust Elasticsearch pool size appropriate to load or try again", ErrorTypeCode.INTEGRATION);
			}
			return client;
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new OpenStorefrontRuntimeException("Unable to retrieve Elasticsearch Connection - wait interrupted.  No resource available.", "Adjust Elasticsearch pool size appropriate to load. (system admin)", ex, ErrorTypeCode.INTEGRATION);
		}
	}

	private void checkSearchIndexCreation()
	{
		//	Check if index already exists...
		//	As of now (Elasticsearch 5.6.3), the best way to check if an index exists is to try and create it!
		//	... then catch the error if thrown... High/Low-level REST client doesn't support checking the existence indices.
		//		per: https://discuss.elastic.co/t/high-level-rest-client-admin-api/100461
		if (!indexCreated.get()) {
			try (ElasticSearchClient client = justGetClient();) {

				XContentBuilder source = JsonXContent.contentBuilder().startObject().endObject();
				StringEntity entity = new StringEntity(source.string(), ContentType.APPLICATION_JSON);

				//	Perform a request attempting to create an index
				Response response = client
						.getInstance()
						.getLowLevelClient()
						.performRequest("PUT", "/" + INDEX, Collections.emptyMap(), entity);

				LOG.log(Level.INFO, () -> "Search index: " + INDEX + " has been created. " + response.getStatusLine().getStatusCode());
				indexCreated.set(true);

			} catch (ResponseException e) {
				//	Index was already created...
				indexCreated.set(true);
			} catch (IOException e) {
				LOG.log(Level.SEVERE, "Unable to connect to elasticsearch", e);
			}
		}
	}

	@Override
	public void releaseClient(ElasticSearchClient client)
	{
		if (!clientPool.offer(client)) {
			LOG.log(Level.FINER, "Client not added to Pool; Pool was full.");
		}
	}

	@Override
	public int getMaxConnections()
	{
		return maxPoolSize;
	}

	@Override
	public int getAvailableConnections()
	{
		return maxPoolSize - clientPool.remainingCapacity();
	}

	@Override
	public void shutdownPool()
	{
		for (ElasticSearchClient client : clientPool) {
			client.close();
		}
		clientPool.clear();
	}

	private void cleanup()
	{
		if (singleton != null) {
			if (singleton.getAvailableConnections() != singleton.getMaxConnections()) {
				LOG.log(Level.WARNING, () -> singleton.getAvailableConnections() + " elasticsearch connections were in process. ");
			}
			LOG.log(Level.FINE, "Stopping pool.");
			singleton.shutdownPool();
		}
	}

	@Override
	public void initialize()
	{
		init();
		started.set(true);
	}

	@Override
	public void shutdown()
	{
		cleanup();
		started.set(false);
	}

	@Override
	public ComponentSearchWrapper search(SearchQuery searchQuery, FilterQueryParams filter)
	{
		ComponentSearchWrapper componentSearchWrapper = new ComponentSearchWrapper();

		IndexSearchResult indexSearchResult = doIndexSearch(searchQuery.getQuery(), filter);

		updateSearchScore(searchQuery.getQuery(), indexSearchResult.getSearchViews());

		componentSearchWrapper.setData(indexSearchResult.getSearchViews());
		componentSearchWrapper.setResults(indexSearchResult.getSearchViews().size());
		componentSearchWrapper.setTotalNumber(indexSearchResult.getTotalResults());

		return componentSearchWrapper;
	}

	@Override
	public IndexSearchResult doIndexSearch(String query, FilterQueryParams filter)
	{
		return doIndexSearch(query, filter, null);
	}

	@Override
	public IndexSearchResult doIndexSearch(String query, FilterQueryParams filter, String[] addtionalFieldsToReturn)
	{
		IndexSearchResult indexSearchResult = new IndexSearchResult();
		SearchOptions searchOptions = service.getSearchService().getSearchOptions();

		int maxSearchResults = 10000;
		if (filter.getMax() < maxSearchResults) {
			maxSearchResults = filter.getMax();
		}

		if (StringUtils.isBlank(query)) {
			query = "*";
		}

		// Initialize Search Phrases
		// (Searching Different Phrases [Quoted Words])
		List<String> searchPhrases = new ArrayList<>();

		// Convert Query To StringBuilder
		StringBuilder queryString = new StringBuilder(query);

		// Search For Quotes
		while (queryString.indexOf("\"") != -1) {

			// Store Index Of First Quote
			int quoteStartIndex = queryString.indexOf("\"");

			// Store Index Of Next Quote
			int quoteEndIndex = queryString.indexOf("\"", quoteStartIndex + 1);

			// Check If We Failed To Find Next Quote
			if (quoteEndIndex == -1) {

				// Remove First Quote
				queryString.deleteCharAt(quoteStartIndex);
			} else {

				// Create Sub-String Of Quoted Phrase
				String subQueryString = queryString.substring(quoteStartIndex, quoteEndIndex + 1);

				// Remove Sub-String From Query
				queryString.delete(quoteStartIndex, quoteEndIndex + 1);

				// Add Sub-String To Search Phrases
				searchPhrases.add(subQueryString);

				//////////
				// TRIM //
				//////////
				// Replace All Double Spaces
				while (queryString.indexOf("  ") != -1) {

					// Get Index Of Double Space
					int doubleSpaceIndex = queryString.indexOf("  ");

					queryString.replace(doubleSpaceIndex, doubleSpaceIndex + 2, " ");
				}

				// Search For Beginning Whitespace
				if (queryString.length() > 0 && queryString.charAt(0) == ' ') {

					// Remove Whitespace
					queryString.deleteCharAt(0);
				}

				// Search For Ending Whitespace
				if (queryString.length() > 0 && queryString.charAt(queryString.length() - 1) == ' ') {

					// Remove Whitespace
					queryString.deleteCharAt(queryString.length() - 1);
				}
			}
		}

		// Initialize ElasticSearch Query
		BoolQueryBuilder esQuery = QueryBuilders.boolQuery();

		// Check For Remaining Query Items
		if (queryString.length() > 0) {

			String actualQuery;
			String allUpperQuery;
			String allLowerQuery;
			String properCaseQuery;

			// This is done becuase searching doesn't properly deal with the hyphens.
			if (isHyphenatedWithoutSpaces(queryString.toString())) {
				// Create custom queries
				allUpperQuery = createSubstringOfQuery(queryString.toString().toUpperCase());
				allLowerQuery = createSubstringOfQuery(queryString.toString().toLowerCase());
				properCaseQuery = createSubstringOfQuery(toProperCase(queryString.toString()));
				actualQuery = createSubstringOfQuery(queryString.toString());

			} else {
				// Create custom queries
				allUpperQuery = queryString.toString().toUpperCase();
				allLowerQuery = queryString.toString().toLowerCase();
				properCaseQuery = toProperCase(queryString.toString());
				actualQuery = queryString.toString();
			}
			
			if(searchOptions.getCanUseOrganizationsInSearch()){
				// Custom query for entry organization
				esQuery.should(QueryBuilders.wildcardQuery(ComponentSearchView.FIELD_ORGANIZATION, allUpperQuery));
				esQuery.should(QueryBuilders.wildcardQuery(ComponentSearchView.FIELD_ORGANIZATION, allLowerQuery));
				esQuery.should(QueryBuilders.wildcardQuery(ComponentSearchView.FIELD_ORGANIZATION, properCaseQuery));
				esQuery.should(QueryBuilders.wildcardQuery(ComponentSearchView.FIELD_ORGANIZATION, actualQuery));
			} else {
				esQuery.mustNot(QueryBuilders.wildcardQuery(ComponentSearchView.FIELD_ORGANIZATION, allUpperQuery));
				esQuery.mustNot(QueryBuilders.wildcardQuery(ComponentSearchView.FIELD_ORGANIZATION, allLowerQuery));
				esQuery.mustNot(QueryBuilders.wildcardQuery(ComponentSearchView.FIELD_ORGANIZATION, properCaseQuery));
				esQuery.mustNot(QueryBuilders.wildcardQuery(ComponentSearchView.FIELD_ORGANIZATION, actualQuery));
			}
			
			if(searchOptions.getCanUseNameInSearch()){
				// Custom query for entry name
				esQuery.should(QueryBuilders.wildcardQuery(ComponentSearchView.FIELD_NAME, allUpperQuery));
				esQuery.should(QueryBuilders.wildcardQuery(ComponentSearchView.FIELD_NAME, allLowerQuery));
				esQuery.should(QueryBuilders.wildcardQuery(ComponentSearchView.FIELD_NAME, properCaseQuery));
				esQuery.should(QueryBuilders.wildcardQuery(ComponentSearchView.FIELD_NAME, actualQuery));

				esQuery.should(QueryBuilders.matchPhraseQuery(ComponentSearchView.FIELD_NAME, allUpperQuery));
				esQuery.should(QueryBuilders.matchPhraseQuery(ComponentSearchView.FIELD_NAME, allLowerQuery));
				esQuery.should(QueryBuilders.matchPhraseQuery(ComponentSearchView.FIELD_NAME, properCaseQuery));
				esQuery.should(QueryBuilders.matchPhraseQuery(ComponentSearchView.FIELD_NAME, actualQuery));
			} else {
				esQuery.mustNot(QueryBuilders.wildcardQuery(ComponentSearchView.FIELD_NAME, allUpperQuery));
				esQuery.mustNot(QueryBuilders.wildcardQuery(ComponentSearchView.FIELD_NAME, allLowerQuery));
				esQuery.mustNot(QueryBuilders.wildcardQuery(ComponentSearchView.FIELD_NAME, properCaseQuery));
				esQuery.mustNot(QueryBuilders.wildcardQuery(ComponentSearchView.FIELD_NAME, actualQuery));

				esQuery.mustNot(QueryBuilders.matchPhraseQuery(ComponentSearchView.FIELD_NAME, allUpperQuery));
				esQuery.mustNot(QueryBuilders.matchPhraseQuery(ComponentSearchView.FIELD_NAME, allLowerQuery));
				esQuery.mustNot(QueryBuilders.matchPhraseQuery(ComponentSearchView.FIELD_NAME, properCaseQuery));
				esQuery.mustNot(QueryBuilders.matchPhraseQuery(ComponentSearchView.FIELD_NAME, actualQuery));
			}

			if(searchOptions.getCanUseDescriptionInSearch()){
				// Custom query for description
				esQuery.should(QueryBuilders.matchPhraseQuery("description", actualQuery));				
			} else {
				esQuery.mustNot(QueryBuilders.matchPhraseQuery("description", actualQuery));	
			}
		}

		// Loop Through Search Phrases
		for (String phrase : searchPhrases) {
			
			if (searchOptions.getCanUseOrganizationsInSearch()) {
				esQuery.should(QueryBuilders.matchPhraseQuery(ComponentSearchView.FIELD_ORGANIZATION, phrase));
			} else {
				esQuery.mustNot(QueryBuilders.matchPhraseQuery(ComponentSearchView.FIELD_ORGANIZATION, phrase));
			}

			if (searchOptions.getCanUseNameInSearch()) {
				esQuery.should(QueryBuilders.matchPhraseQuery(ComponentSearchView.FIELD_NAME, phrase));
			} else {
				esQuery.mustNot(QueryBuilders.matchPhraseQuery(ComponentSearchView.FIELD_NAME, phrase));
			}

			if (searchOptions.getCanUseDescriptionInSearch()) {
				esQuery.should(QueryBuilders.matchPhraseQuery("description", phrase.toLowerCase()));
			} else {
				esQuery.mustNot(QueryBuilders.matchPhraseQuery("description", phrase.toLowerCase()));
			}
			
		}
		FieldSortBuilder sort = new FieldSortBuilder(filter.getSortField())
				//.unmappedType("String") // currently the only fileds we are searching/sorting on are strings
				.order(OpenStorefrontConstant.SORT_ASCENDING.equals(filter.getSortOrder()) ? SortOrder.ASC : SortOrder.DESC);

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
				.query(esQuery)
				.from(filter.getOffset())
				.size(maxSearchResults)
				.sort(sort);
		SearchRequest searchRequest = new SearchRequest(INDEX)
				.source(searchSourceBuilder);

		try {
			performIndexSearch(searchRequest, indexSearchResult);
		} catch (IOException ex) {
			Logger.getLogger(ElasticSearchManager.class.getName()).log(Level.SEVERE, null, ex);
		} catch (ElasticsearchStatusException e) {

			//	if a status exception occurs, it is likely the fielddata == false for description.
			//		Thus, update the mapping.
			updateMapping();

			//	try to perform the index search one more time...
			try {
				performIndexSearch(searchRequest, indexSearchResult);
			} catch (IOException | ElasticsearchStatusException ex) {
				throw new OpenStorefrontRuntimeException("Unable to perform search!", "check index [" + INDEX + "] mapping", ex);
			}
		}

		return indexSearchResult;
	}

	private void performIndexSearch(SearchRequest searchRequest, IndexSearchResult indexSearchResult) throws IOException, ElasticsearchStatusException
	{
		// perform search
		SearchResponse response;
		try (ElasticSearchClient client = singleton.getClient()) {
			response = client.getInstance().search(searchRequest);
		}

		indexSearchResult.setTotalResults(response.getHits().getTotalHits());
		indexSearchResult.setMaxScore(response.getHits().getMaxScore());

		ObjectMapper objectMapper = StringProcessor.defaultObjectMapper();
		for (SearchHit hit : response.getHits().getHits()) {
			try {
				ComponentSearchView view = objectMapper.readValue(hit.getSourceAsString(), new TypeReference<ComponentSearchView>()
				{
				});
				if (service.getComponentService().checkComponentApproval(view.getComponentId())) {
					view.setSearchScore(hit.getScore());
					indexSearchResult.getSearchViews().add(view);
					indexSearchResult.getResultsList().add(SolrComponentModel.fromComponentSearchView(view));
				} else {
					LOG.log(Level.FINER, MessageFormat.format("Component is no longer approved and active.  Removing index.  {0}", view.getComponentId()));
					indexSearchResult.setTotalResults(indexSearchResult.getTotalResults() - 1);
					deleteById(view.getComponentId());
				}
			} catch (IOException ex) {
				throw new OpenStorefrontRuntimeException("Unable to handle search result", "check index database", ex);
			}
		}
		indexSearchResult.applyDataFilter();
	}

	protected String toProperCase(String query)
	{
		String delimiters = "'-/*";

		StringBuilder searchQuery = new StringBuilder();
		boolean capNext = true;
		for (char c : query.toCharArray()) {

			if (capNext && Character.isLetter(c)) {
				c = Character.toUpperCase(c);
				searchQuery.append(c);
				capNext = (delimiters.indexOf((int) c) >= 0);
			} else {
				searchQuery.append(c);
				capNext = (delimiters.indexOf((int) c) >= 0);
			}
		}
		return searchQuery.toString();
	}

	protected boolean isHyphenatedWithoutSpaces(String query)
	{
		if (query.indexOf('-') >= 0 && query.charAt(0) != '"') {

			for (int i = 1; i < query.length() - 1; i++) {
				if (query.charAt(i) == '-' && query.charAt(i - 1) != ' ' && query.charAt(i + 1) != ' ') {
					return true;
				}
			}
			return false;
		} else {
			return false;
		}
	}

	protected String createSubstringOfQuery(String query)
	{
		int indexOfHyphen = query.indexOf('-');

		return query.substring(0, indexOfHyphen);
	}

	@Override
	public List<SearchSuggestion> searchSuggestions(String query, int maxResult, String componentType)
	{
		List<SearchSuggestion> searchSuggestions = new ArrayList<>();

		FilterQueryParams filter = FilterQueryParams.defaultFilter();
		filter.setSortField(ComponentSearchView.FIELD_NAME);

		//ignore case
		query = "*" + query.toLowerCase() + "*";

		//query everything we can
		String extraFields[] = {
			SolrComponentModel.FIELD_NAME,
			SolrComponentModel.FIELD_ORGANIZATION,
			SolrComponentModel.FIELD_DESCRIPTION
		};
		IndexSearchResult indexSearchResult = doIndexSearch(query, filter, extraFields);

		if (StringUtils.isNotBlank(componentType)) {
			indexSearchResult.setResultsList(
					indexSearchResult.getResultsList()
							.stream()
							.filter((result) -> componentType.equals(result.getComponentType()))
							.collect(Collectors.toList())
			);
			indexSearchResult.setSearchViews(
					indexSearchResult.getSearchViews()
							.stream()
							.filter((result) -> componentType.equals(result.getComponentType()))
							.collect(Collectors.toList())
			);
		}

		if (StringUtils.isBlank(query)) {
			query = "";
		}

		//apply weight to items
		String queryNoWild = query.replace("*", "").toLowerCase();
		for (SolrComponentModel model : indexSearchResult.getResultsList()) {
			int score = 0;

			if (StringUtils.isNotBlank(model.getName())
					&& model.getName().toLowerCase().contains(queryNoWild)) {
				score += 100;
			}

			if (StringUtils.isNotBlank(model.getOrganization())
					&& model.getOrganization().toLowerCase().contains(queryNoWild)) {
				score += 50;
			}

			int count = StringUtils.countMatches(model.getDescription().toLowerCase(), queryNoWild);
			score += count * 5;

			model.setSearchWeight(score);
		}

		//sort
		indexSearchResult.getResultsList().sort((SolrComponentModel o1, SolrComponentModel o2) -> Integer.compare(o2.getSearchWeight(), o1.getSearchWeight()));

		//window
		List<SolrComponentModel> topItems = indexSearchResult.getResultsList().stream().limit(maxResult).collect(Collectors.toList());

		for (SolrComponentModel model : topItems) {

			SearchSuggestion suggestion = new SearchSuggestion();
			suggestion.setName(model.getName());
			suggestion.setComponentId(model.getId());
			suggestion.setQuery("\"" + model.getName() + "\"");

			// Only include approved components.
			if (service.getComponentService().checkComponentApproval(suggestion.getComponentId())) {
				searchSuggestions.add(suggestion);
			}
		}

		return searchSuggestions;
	}

	@Override
	public void index(List<Component> components)
	{
		Objects.requireNonNull(components);

		if (!components.isEmpty()) {
			ObjectMapper objectMapper = StringProcessor.defaultObjectMapper();
			BulkRequest bulkRequest = new BulkRequest();

			//pull attribute and map
			ComponentAttribute componentAttribute = new ComponentAttribute();
			componentAttribute.setActiveStatus(ComponentAttribute.ACTIVE_STATUS);
			List<ComponentAttribute> componentAttributes = componentAttribute.findByExample();
			Map<String, List<ComponentAttribute>> attributeMap = componentAttributes.stream().collect(Collectors.groupingBy(ComponentAttribute::getComponentId));

			//pull reviews and map
			ComponentReview componentReview = new ComponentReview();
			componentReview.setActiveStatus(ComponentReview.ACTIVE_STATUS);
			List<ComponentReview> componentReviews = componentReview.findByExample();
			Map<String, List<ComponentReview>> reviewMap = componentReviews.stream().collect(Collectors.groupingBy(ComponentReview::getComponentId));

			//pull tags and map
			ComponentTag componentTag = new ComponentTag();
			componentTag.setActiveStatus(ComponentReview.ACTIVE_STATUS);
			List<ComponentTag> componentTags = componentTag.findByExample();
			Map<String, List<ComponentTag>> tagMap = componentTags.stream().collect(Collectors.groupingBy(ComponentTag::getComponentId));
			
			SearchStatTable statTable = new SearchStatTable(attributeMap, tagMap);
			statTable.index();

			for (Component component : components) {

				//convert to search result object
				componentAttributes = attributeMap.getOrDefault(component.getComponentId(), new ArrayList<>());
				componentReviews = reviewMap.getOrDefault(component.getComponentId(), new ArrayList<>());
				componentTags = tagMap.getOrDefault(component.getComponentId(), new ArrayList<>());

				ComponentSearchView componentSearchView = ComponentSearchView.toView(component, componentAttributes, componentReviews, componentTags);
				try {
					bulkRequest.add(new IndexRequest(INDEX, INDEX_TYPE, componentSearchView.getComponentId())
							.source(objectMapper.writeValueAsString(componentSearchView), XContentType.JSON));
				} catch (JsonProcessingException ex) {
					LOG.log(Level.SEVERE, null, ex);
				}
			}

			BulkResponse bulkResponse;
			try (ElasticSearchClient client = singleton.getClient()) {
				bulkResponse = client.getInstance().bulk(bulkRequest);

				if (bulkResponse.hasFailures()) {
					bulkResponse.forEach(response -> {
						if (StringUtils.isNotBlank(response.getFailureMessage())) {
							LOG.log(Level.WARNING, MessageFormat.format("A component failed to index: {0}", response.getFailureMessage()));
						}
					});
				} else {
					LOG.log(Level.FINE, "Index components successfully");
				}
			} catch (IOException ex) {
				LOG.log(Level.SEVERE, null, ex);
			}
		}
	}

	@Override
	public void deleteById(String id)
	{
		DeleteRequest deleteRequest = new DeleteRequest(INDEX, INDEX_TYPE, id);
		DeleteResponse response;
		try (ElasticSearchClient client = singleton.getClient()) {
			response = client.getInstance().delete(deleteRequest);
			LOG.log(Level.FINER, MessageFormat.format("Found Record to delete: {0}", response.getId()));
		} catch (IOException ex) {
			LOG.log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void deleteAll()
	{
		//query all (delete in groups)
		int start = 0;
		int max = 1000;
		long total = max;

		int maxRetries = 5;
		int retries = 0;

		while (start < total) {

			//	Create the search request
			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
					.query(QueryBuilders.matchAllQuery())
					.from(start)
					.size(max);
			SearchRequest searchRequest = new SearchRequest(INDEX)
					.types(INDEX_TYPE)
					.source(sourceBuilder);

			SearchResponse response;
			try (ElasticSearchClient client = singleton.getClient()) {
				response = client.getInstance().search(searchRequest);

				SearchHits searchHits = response.getHits();
				BulkRequest bulkRequest = new BulkRequest();
				if (searchHits.getTotalHits() > 0) {
					//bulk delete results
					searchHits.forEach(hit -> {
						bulkRequest.add(new DeleteRequest(INDEX, INDEX_TYPE, hit.getId()));
					});

					//	Process the bulk request (ensure there were no failures)
					BulkResponse bulkResponse = client.getInstance().bulk(bulkRequest);
					if (bulkResponse.hasFailures()) {
						bulkResponse.forEach(br -> {
							if (StringUtils.isNotBlank(br.getFailureMessage())) {
								LOG.log(Level.WARNING, MessageFormat.format("A component failed to delete: {0}", br.getFailureMessage()));
							}
						});
					}
				}
				start += searchHits.getHits().length;
				total = searchHits.getTotalHits();
			} catch (ElasticsearchStatusException ex) {
				LOG.log(Level.WARNING, "Index is not found. Skipping delete.");
				LOG.log(Level.FINER, null, ex);
				indexCreated.set(false);

				retries++;
				if (retries == maxRetries) {
					throw new OpenStorefrontRuntimeException("Unable to correct index/search error on detail. Giving up.", "Check elasticsearch", ex);
				}
			} catch (IOException ex) {
				LOG.log(Level.SEVERE, null, ex);
			}
		}
	}

	@Override
	public void saveAll()
	{
		Component component = new Component();
		component.setActiveStatus(Component.ACTIVE_STATUS);
		component.setApprovalState(ApprovalStatus.APPROVED);
		List<Component> components = component.findByExample();

		index(components);
	}

	@Override
	public void resetIndexer()
	{
		deleteAll();
		saveAll();
		updateMapping();
	}

	@Override
	public boolean isStarted()
	{
		return started.get();
	}

	private void updateMapping()
	{
		// Update description field to use fielddata=true
		//	Here, we must update all types
		try (ElasticSearchClient client = singleton.getClient()) {

			List<String> fieldsToUpdate = Arrays.asList(
					ComponentSearchView.FIELD_NAME,
					ComponentSearchView.FIELD_DESCRIPTION
			);

			for (String field : fieldsToUpdate) {

				XContentBuilder source = JsonXContent
						.contentBuilder()
						.startObject()
						.startObject("properties")
						.startObject(field)
						.field("type", "text")
						.startObject("fields")
						.startObject("keyword")
						.field("type", "keyword")
						.endObject()
						.endObject()
						.field("fielddata", true)
						.endObject()
						.endObject()
						.endObject();

				//	Use low-level REST client to perform re-mapping
				StringEntity entity = new StringEntity(source.string(), ContentType.APPLICATION_JSON);

				//	Perform a PUT request to update the description mapping
				client.getInstance()
						.getLowLevelClient()
						.performRequest("PUT", "/" + INDEX + "/_mapping/" + INDEX_TYPE + "?update_all_types", Collections.emptyMap(), entity);
			}

		} catch (IOException ex) {
			LOG.log(Level.SEVERE, null, ex);
		}
	}

}
