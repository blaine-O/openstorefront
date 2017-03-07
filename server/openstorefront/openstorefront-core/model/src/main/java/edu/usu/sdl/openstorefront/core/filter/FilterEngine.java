/*
 * Copyright 2017 Space Dynamics Laboratory - Utah State University Research Foundation.
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
package edu.usu.sdl.openstorefront.core.filter;

import edu.usu.sdl.openstorefront.core.api.ServiceProxyFactory;
import edu.usu.sdl.openstorefront.core.entity.BaseComponent;
import edu.usu.sdl.openstorefront.core.entity.Component;
import edu.usu.sdl.openstorefront.core.entity.StandardEntity;
import edu.usu.sdl.openstorefront.security.SecurityUtil;
import edu.usu.sdl.openstorefront.security.UserContext;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This apply filtering rules (based on current user) to the data
 *
 * @author dshurtleff
 */
public class FilterEngine
{

	private static final Logger LOG = Logger.getLogger(FilterEngine.class.getName());

	public static final String FIELD_DATA_SOURCE = "dataSource";
	public static final String FIELD_DATA_SENSITIVITY = "dataSensitivity";

	/**
	 * remove the records if user has data restrictions
	 *
	 * Doesn't check parent component for base components
	 *
	 * @param <T>
	 * @param dataItems
	 * @return
	 */
	public static <T> List<T> filter(List<T> dataItems)
	{
		return filter(dataItems, false);
	}

	/**
	 * Remove the records if user has data restrictions
	 *
	 * @param <T>
	 * @param dataItems
	 * @param checkParentComponent
	 * @return
	 */
	public static <T> List<T> filter(List<T> dataItems, boolean checkParentComponent)
	{
		if (isFilterable(dataItems)) {
			dataItems = dataItems.stream()
					.filter(data -> filter(data, checkParentComponent) != null)
					.collect(Collectors.toList());
		}
		return dataItems;
	}

	/**
	 * Remove the record if user has data restrictions Doesn't check parent
	 * component for base components
	 *
	 * @param <T>
	 * @param data
	 * @return
	 */
	public static <T> T filter(T data)
	{
		return filter(data, false);
	}

	/**
	 * Remove the record if user has data restrictions
	 *
	 * @param <T>
	 * @param data
	 * @param checkParentComponent (if true, Parent component must not be
	 * restricted)
	 * @return null if filter or the data if it should be kept
	 */
	public static <T> T filter(T data, boolean checkParentComponent)
	{
		T returnValue = null;
		if (data == null) {
			return data;
		}
		if (isFilterable(data)) {

			UserContext userContext = SecurityUtil.getUserContext();

			Set<String> acceptedDataSources = userContext.dataSources();
			Set<String> acceptedDataSensitivity = userContext.dataSensitivity();

			if (data instanceof Component) {

				boolean keepSource = false;
				Component component = (Component) data;
				if (component.getDataSource() == null && userContext.allowUnspecifiedDataSources()) {
					keepSource = true;
				} else if (acceptedDataSources.contains(component.getDataSource())) {
					keepSource = true;
				}

				if (keepSource) {
					if (component.getDataSensitivity() == null && userContext.allowUnspecifiedDataSensitivty()) {
						returnValue = data;
					} else if (acceptedDataSensitivity.contains(component.getDataSensitivity())) {
						returnValue = data;
					}
				}

			} else if (data instanceof BaseComponent) {

				boolean keepData = false;
				if (checkParentComponent) {
					//if base component - check component data restrictions
					BaseComponent baseComponent = (BaseComponent) data;

					ComponentSensitivityModel componentSensitivityModel = ServiceProxyFactory.getServiceProxy().getComponentService().getComponentSensitivity(baseComponent.getComponentId());
					if (componentSensitivityModel != null) {
						boolean keepSource = false;
						if ((componentSensitivityModel.getDataSource() == null && userContext.allowUnspecifiedDataSources())) {
							keepSource = true;
						} else if (acceptedDataSources.contains(componentSensitivityModel.getDataSource())) {
							keepSource = true;
						}

						if (keepSource) {
							if (componentSensitivityModel.getDataSensitivity() == null && userContext.allowUnspecifiedDataSensitivty()) {
								keepData = true;
							} else if (acceptedDataSensitivity.contains(componentSensitivityModel.getDataSensitivity())) {
								keepData = true;
							}
						}
					} else {
						LOG.log(Level.WARNING, MessageFormat.format("Unable to find base component - during filtering. Filtering data out. Component Id: {0}", baseComponent.getComponentId()));
					}

				} else {
					keepData = true;
				}

				if (keepData) {
					StandardEntity standardEntity = (StandardEntity) data;
					if (standardEntity.getDataSensitivity() == null && userContext.allowUnspecifiedDataSensitivty()) {
						returnValue = data;
					} else if (acceptedDataSensitivity.contains(standardEntity.getDataSensitivity())) {
						returnValue = data;
					}
				}
			} else {

				StandardEntity standardEntity = (StandardEntity) data;
				if (standardEntity.getDataSensitivity() == null && userContext.allowUnspecifiedDataSensitivty()) {
					returnValue = data;
				} else if (acceptedDataSensitivity.contains(standardEntity.getDataSensitivity())) {
					returnValue = data;
				}
			}

		}
		return returnValue;
	}

	/**
	 * Data is filterable if the following conditions are met: _Not Null _User
	 * is Logged in _Is a Standard Entity
	 *
	 * @param <T>
	 * @param data (It's assume the list only contains one data type)
	 * @return true if data can be Filtered
	 */
	public static <T> boolean isFilterable(List<T> data)
	{
		boolean filterable = false;

		if (data != null
				&& !data.isEmpty()
				&& SecurityUtil.isLoggedIn()
				&& data.get(0) instanceof StandardEntity) {
			filterable = true;
		}

		return filterable;
	}

	/**
	 * Data is filterable if the following conditions are met: _Not Null _User
	 * is Logged in _Is a Standard Entity
	 *
	 * @param <T>
	 * @param data
	 * @return true if data can be Filtered
	 */
	public static <T> boolean isFilterable(T data)
	{
		boolean filterable = false;

		if (data != null
				&& SecurityUtil.isLoggedIn()
				&& data instanceof StandardEntity) {
			filterable = true;
		}

		return filterable;
	}

	/**
	 * Constructs a where restriction for a query Includes datasource
	 * restriction
	 *
	 * @return
	 */
	public static String queryComponentRestriction()
	{
		StringBuilder query = new StringBuilder();
		query.append(queryStandardRestriction());

		UserContext userContext = SecurityUtil.getUserContext();
		if (userContext != null) {

			if (query.length() > 0) {
				query.append(" and ");
			}

			query.append("(");

			if (userContext.allowUnspecifiedDataSources()) {
				query.append(" " + FIELD_DATA_SOURCE + " IS NULL ");
			} else {
				query.append(" " + FIELD_DATA_SOURCE + " IS NOT NULL ");
			}

			Set<String> datasources = userContext.dataSources();
			if (!datasources.isEmpty()) {
				if (userContext.allowUnspecifiedDataSources()) {
					query.append(" OR ");
				} else {
					query.append(" AND ");
				}

				query.append(FIELD_DATA_SOURCE + " IN [");

				List<String> dataSourceList = new ArrayList<>();
				datasources.forEach((dataSource) -> {
					dataSourceList.add("'" + dataSource + "'");
				});
				query.append(String.join(",", dataSourceList));
				query.append("]");
			}
			query.append(")");
		}

		return query.toString();
	}

	/**
	 * Constructs a where restriction for a query
	 *
	 * @return
	 */
	public static String queryStandardRestriction()
	{
		StringBuilder query = new StringBuilder();

		UserContext userContext = SecurityUtil.getUserContext();
		if (userContext != null) {
			query.append("(");

			if (userContext.allowUnspecifiedDataSensitivty()) {
				query.append(" " + FIELD_DATA_SENSITIVITY + " IS NULL ");
			} else {
				query.append(" " + FIELD_DATA_SENSITIVITY + " IS NOT NULL ");
			}

			Set<String> dataSensitivity = userContext.dataSensitivity();
			if (!dataSensitivity.isEmpty()) {
				if (userContext.allowUnspecifiedDataSensitivty()) {
					query.append(" OR ");
				} else {
					query.append(" AND ");
				}

				query.append(FIELD_DATA_SENSITIVITY + " IN [");

				List<String> dataSensitivityList = new ArrayList<>();
				dataSensitivity.forEach((dsCode) -> {
					dataSensitivityList.add("'" + dsCode + "'");
				});
				query.append(String.join(",", dataSensitivityList));
				query.append("]");
			}
			query.append(")");
		}

		return query.toString();
	}

}
