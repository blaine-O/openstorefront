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
package edu.usu.sdl.openstorefront.service.search;

import edu.usu.sdl.openstorefront.common.exception.OpenStorefrontRuntimeException;
import edu.usu.sdl.openstorefront.common.util.ReflectionUtil;
import edu.usu.sdl.openstorefront.core.api.query.GenerateStatementOption;
import edu.usu.sdl.openstorefront.core.api.query.GenerateStatementOptionBuilder;
import edu.usu.sdl.openstorefront.core.api.query.QueryByExample;
import edu.usu.sdl.openstorefront.core.entity.ComponentAttribute;
import edu.usu.sdl.openstorefront.core.entity.ComponentAttributePk;
import edu.usu.sdl.openstorefront.core.model.search.SearchElement;
import edu.usu.sdl.openstorefront.core.model.search.SearchOperation;
import edu.usu.sdl.openstorefront.validation.ValidationResult;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author dshurtleff
 */
public class AttributeSearchHandler
		extends BaseSearchHandler
{

	public AttributeSearchHandler(List<SearchElement> searchElements)
	{
		super(searchElements);
	}

	@Override
	protected ValidationResult internalValidate()
	{
		ValidationResult validationResult = new ValidationResult();

		for (SearchElement searchElement : searchElements) {
			if (StringUtils.isBlank(searchElement.getKeyField())) {
				validationResult.getRuleResults().add(getRuleResult("keyField", "Required"));
			}
		}

		return validationResult;
	}

	@Override
	public List<String> processSearch()
	{
		List<String> foundIds = new ArrayList<>();
		SearchOperation.MergeCondition mergeCondition = SearchOperation.MergeCondition.OR;
		for (SearchElement searchElement : searchElements) {

			ComponentAttribute componentAttribute = new ComponentAttribute();
			ComponentAttributePk componentAttributePk = new ComponentAttributePk();
			componentAttributePk.setAttributeType(searchElement.getKeyField());
			componentAttribute.setComponentAttributePk(componentAttributePk);
			componentAttribute.setActiveStatus(ComponentAttribute.ACTIVE_STATUS);
			
			QueryByExample<ComponentAttribute> queryByExample = new QueryByExample(componentAttribute);

			if (StringUtils.isNotBlank(searchElement.getField())) {
				Field field = ReflectionUtil.getField(new ComponentAttribute(), searchElement.getField());
				field.setAccessible(true);

				Class type = field.getType();
				if (type.getSimpleName().equals(String.class.getSimpleName())) {
					String likeValue = null;
					try {
						switch (searchElement.getStringOperation()) {
							case EQUALS:
								String value = searchElement.getValue();
								if (searchElement.getCaseInsensitive()) {									
									queryByExample.getFieldOptions().put(field.getName(), 
											new GenerateStatementOptionBuilder().setMethod(GenerateStatementOption.METHOD_LOWER_CASE).build());									
									value = value.toLowerCase();
								}
								field.set(componentAttribute, value);
								break;
							default:
								likeValue = searchElement.getStringOperation().toQueryString(searchElement.getValue());
								break;
						}

						if (likeValue != null) {
							ComponentAttribute componentAttributeLike = new ComponentAttribute();
							if (searchElement.getCaseInsensitive()) {
								likeValue = likeValue.toLowerCase();
								queryByExample.getLikeExampleOption().setMethod(GenerateStatementOption.METHOD_LOWER_CASE);
							}
							field.set(componentAttributeLike, likeValue);
							queryByExample.setLikeExample(componentAttributeLike);
						}
					} catch (SecurityException | IllegalArgumentException | IllegalAccessException | OpenStorefrontRuntimeException e) {
						throw new OpenStorefrontRuntimeException("Unable to handle search request", e);
					}
				} else {
					throw new OpenStorefrontRuntimeException("Type: " + type.getSimpleName() + " is not support in this query handler", "Add support");
				}
			}

			if (StringUtils.isNotBlank(searchElement.getKeyValue())) {
				String likeValue = null;
				switch (searchElement.getStringOperation()) {
					case EQUALS:
						componentAttributePk.setAttributeCode(searchElement.getKeyValue());
						break;
					default:
						likeValue = searchElement.getStringOperation().toQueryString(searchElement.getKeyValue());
						break;
				}

				if (likeValue != null) {
					ComponentAttribute componentAttributeLike = new ComponentAttribute();
					ComponentAttributePk componentAttributePkLike = new ComponentAttributePk();
					componentAttributePkLike.setAttributeCode(likeValue);
					componentAttributeLike.setComponentAttributePk(componentAttributePkLike);
					queryByExample.setLikeExample(componentAttributeLike);
				}
			}

			List<ComponentAttribute> attributes = serviceProxy.getPersistenceService().queryByExample(ComponentAttribute.class, queryByExample);
			List<String> results = new ArrayList<>();
			for (ComponentAttribute attribute : attributes) {
				results.add(attribute.getComponentId());
			}
			foundIds = mergeCondition.apply(foundIds, results);
			mergeCondition = searchElement.getMergeCondition();
		}
		return foundIds;
	}

}
