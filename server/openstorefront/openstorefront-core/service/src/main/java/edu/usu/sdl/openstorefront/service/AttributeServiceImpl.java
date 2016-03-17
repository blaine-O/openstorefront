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
package edu.usu.sdl.openstorefront.service;

import edu.usu.sdl.openstorefront.common.exception.OpenStorefrontRuntimeException;
import edu.usu.sdl.openstorefront.common.util.OpenStorefrontConstant;
import edu.usu.sdl.openstorefront.common.util.ReflectionUtil;
import edu.usu.sdl.openstorefront.common.util.TimeUtil;
import edu.usu.sdl.openstorefront.core.api.AttributeService;
import edu.usu.sdl.openstorefront.core.api.query.QueryByExample;
import edu.usu.sdl.openstorefront.core.api.query.QueryType;
import edu.usu.sdl.openstorefront.core.entity.AttributeCode;
import edu.usu.sdl.openstorefront.core.entity.AttributeCodePk;
import edu.usu.sdl.openstorefront.core.entity.AttributeType;
import edu.usu.sdl.openstorefront.core.entity.AttributeXRefMap;
import edu.usu.sdl.openstorefront.core.entity.AttributeXRefType;
import edu.usu.sdl.openstorefront.core.entity.Component;
import edu.usu.sdl.openstorefront.core.entity.ComponentAttribute;
import edu.usu.sdl.openstorefront.core.entity.ComponentAttributePk;
import edu.usu.sdl.openstorefront.core.entity.FileHistoryOption;
import edu.usu.sdl.openstorefront.core.entity.LookupEntity;
import edu.usu.sdl.openstorefront.core.entity.ReportOption;
import edu.usu.sdl.openstorefront.core.entity.ScheduledReport;
import edu.usu.sdl.openstorefront.core.entity.TopicSearchItem;
import edu.usu.sdl.openstorefront.core.model.Architecture;
import edu.usu.sdl.openstorefront.core.model.AttributeAll;
import edu.usu.sdl.openstorefront.core.model.AttributeXrefModel;
import edu.usu.sdl.openstorefront.core.model.BulkComponentAttributeChange;
import edu.usu.sdl.openstorefront.core.sort.ArchitectureComparator;
import edu.usu.sdl.openstorefront.core.util.EntityUtil;
import edu.usu.sdl.openstorefront.core.view.AttributeCodeView;
import edu.usu.sdl.openstorefront.core.view.AttributeCodeWrapper;
import edu.usu.sdl.openstorefront.core.view.AttributeTypeWrapper;
import edu.usu.sdl.openstorefront.core.view.AttributeXRefView;
import edu.usu.sdl.openstorefront.core.view.FilterQueryParams;
import edu.usu.sdl.openstorefront.security.SecurityUtil;
import edu.usu.sdl.openstorefront.service.api.AttributeServicePrivate;
import edu.usu.sdl.openstorefront.service.manager.OSFCacheManager;
import edu.usu.sdl.openstorefront.validation.ValidationModel;
import edu.usu.sdl.openstorefront.validation.ValidationResult;
import edu.usu.sdl.openstorefront.validation.ValidationUtil;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.sf.ehcache.Element;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Handles Attribute information
 */
public class AttributeServiceImpl
		extends ServiceProxy
		implements AttributeService, AttributeServicePrivate
{

	private static final Logger log = Logger.getLogger(AttributeServiceImpl.class.getName());

	@Override
	public List<AttributeType> getRequiredAttributes()
	{
		AttributeType example = new AttributeType();
		example.setActiveStatus(AttributeType.ACTIVE_STATUS);
		example.setRequiredFlg(Boolean.TRUE);
		List<AttributeType> required = persistenceService.queryByExample(AttributeType.class, new QueryByExample(example));
		return required;
	}

	@Override
	public List<AttributeCode> getAllAttributeCodes(String activeStatus)
	{
		List<AttributeCode> attributeCodes;
		Element element = OSFCacheManager.getAttributeCodeAllCache().get(OSFCacheManager.ALLCODE_KEY);
		if (element != null) {
			attributeCodes = (List<AttributeCode>) element.getObjectValue();
		} else {
			attributeCodes = persistenceService.queryByExample(AttributeCode.class, new AttributeCode());
			element = new Element(OSFCacheManager.ALLCODE_KEY, attributeCodes);
			OSFCacheManager.getAttributeCodeAllCache().put(element);
		}
		if (StringUtils.isNotBlank(activeStatus)) {
			attributeCodes = attributeCodes.stream().filter(code -> code.getActiveStatus().equals(activeStatus)).collect(Collectors.toList());
		}
		return attributeCodes;
	}

	@Override
	public List<AttributeCode> findCodesForType(String type)
	{
		return findCodesForType(type, false);
	}

	@Override
	public List<AttributeCode> findCodesForType(String type, boolean all)
	{
		List<AttributeCode> attributeCodes;
		if (all) {
			AttributeCode attributeCodeExample = new AttributeCode();
			AttributeCodePk attributeCodePk = new AttributeCodePk();
			attributeCodePk.setAttributeType(type);
			attributeCodeExample.setAttributeCodePk(attributeCodePk);
			attributeCodes = persistenceService.queryByExample(AttributeCode.class, new QueryByExample(attributeCodeExample));
		} else {
			Element element;
			element = OSFCacheManager.getAttributeCache().get(type);
			if (element != null) {
				attributeCodes = (List<AttributeCode>) element.getObjectValue();
			} else {

				AttributeCode attributeCodeExample = new AttributeCode();
				AttributeCodePk attributeCodePk = new AttributeCodePk();
				attributeCodePk.setAttributeType(type);
				attributeCodeExample.setAttributeCodePk(attributeCodePk);
				attributeCodeExample.setActiveStatus(AttributeCode.ACTIVE_STATUS);

				attributeCodes = persistenceService.queryByExample(AttributeCode.class, new QueryByExample(attributeCodeExample));
				element = new Element(type, attributeCodes);
				OSFCacheManager.getAttributeCache().put(element);
			}
		}
		return attributeCodes;
	}

	@Override
	public void saveAttributeType(AttributeType attributeType)
	{
		saveAttributeType(attributeType, true);
	}

	@Override
	public void saveAttributeType(AttributeType attributeType, boolean updateIndexes)
	{
		getAttributeServicePrivate().performSaveAttributeType(attributeType);

		if (updateIndexes) {
			ComponentAttributePk componentAttributePk = new ComponentAttributePk();
			componentAttributePk.setAttributeType(attributeType.getAttributeType());
			ComponentAttribute componentAttribute = new ComponentAttribute();
			componentAttribute.setComponentAttributePk(componentAttributePk);
			List<ComponentAttribute> componentAttributes = getPersistenceService().queryByExample(ComponentAttribute.class, componentAttribute);

			List<Component> components = new ArrayList<>();
			componentAttributes.stream().forEach((attr) -> {
				components.add(persistenceService.findById(Component.class, attr.getComponentAttributePk().getComponentId()));
			});
			getSearchService().indexComponents(components);
		}
	}

	@Override
	public void performSaveAttributeType(AttributeType attributeType)
	{
		AttributeType existing = persistenceService.findById(AttributeType.class, attributeType.getAttributeType());

		ValidationResult validationResult = attributeType.customValidation();
		if (validationResult.valid() == false) {
			throw new OpenStorefrontRuntimeException(validationResult.toString());
		}

		if (existing != null) {
			//remove to inactivate
			existing.updateFields(attributeType);
			persistenceService.persist(existing);
		} else {
			attributeType.populateBaseCreateFields();
			persistenceService.persist(attributeType);
		}
		cleanCaches(attributeType.getAttributeType());
	}

	private void cleanCaches(String attributeType)
	{
		OSFCacheManager.getAttributeCache().remove(attributeType);
		OSFCacheManager.getAttributeTypeCache().remove(attributeType);
		OSFCacheManager.getAttributeCodeAllCache().removeAll();
	}

	@Override
	public void saveAttributeCode(AttributeCode attributeCode)
	{
		saveAttributeCode(attributeCode, true);
	}

	@Override
	public void saveAttributeCode(AttributeCode attributeCode, boolean updateIndexes)
	{
		getAttributeServicePrivate().performSaveAttributeCode(attributeCode);

		if (updateIndexes) {
			ComponentAttributePk pk = new ComponentAttributePk();
			pk.setAttributeType(attributeCode.getAttributeCodePk().getAttributeType());
			pk.setAttributeCode(attributeCode.getAttributeCodePk().getAttributeCode());
			ComponentAttribute example = new ComponentAttribute();
			example.setComponentAttributePk(pk);

			List<ComponentAttribute> componentAttributes = getPersistenceService().queryByExample(ComponentAttribute.class, new QueryByExample(example));

			List<Component> components = new ArrayList<>();
			componentAttributes.stream().forEach((attr) -> {
				components.add(persistenceService.findById(Component.class, attr.getComponentAttributePk().getComponentId()));
			});
			getSearchService().indexComponents(components);
		}
	}

	@Override
	public void performSaveAttributeCode(AttributeCode attributeCode)
	{
		AttributeCode existing = persistenceService.findById(AttributeCode.class, attributeCode.getAttributeCodePk());
		if (existing != null) {
			existing.updateFields(attributeCode);
			persistenceService.persist(existing);
		} else {
			attributeCode.populateBaseCreateFields();
			persistenceService.persist(attributeCode);
		}
		cleanCaches(attributeCode.getAttributeCodePk().getAttributeType());
	}

	@Override
	public void removeAttributeType(String type)
	{
		Objects.requireNonNull(type, "Type is required.");

		AttributeType attributeType = persistenceService.findById(AttributeType.class, type);
		if (attributeType != null) {
			attributeType.setActiveStatus(AttributeCode.INACTIVE_STATUS);
			attributeType.setUpdateDts(TimeUtil.currentDate());
			attributeType.setUpdateUser(SecurityUtil.getCurrentUserName());
			persistenceService.persist(attributeType);

			BulkComponentAttributeChange bulkComponentAttributeChange = new BulkComponentAttributeChange();
			bulkComponentAttributeChange.setAttributes(getComponentAttributes(type, null));
			bulkComponentAttributeChange.setOpertionType(BulkComponentAttributeChange.OpertionType.INACTIVE);

			//Stay in the same transaction
			(new ComponentServiceImpl(persistenceService)).bulkComponentAttributeChange(bulkComponentAttributeChange);

			cleanCaches(type);
		}
	}

	private List<ComponentAttribute> getComponentAttributes(String type, String code)
	{
		ComponentAttribute componentAttributeExample = new ComponentAttribute();
		ComponentAttributePk componentAttributePk = new ComponentAttributePk();
		componentAttributePk.setAttributeType(type);
		componentAttributePk.setAttributeCode(code);
		componentAttributeExample.setComponentAttributePk(componentAttributePk);
		QueryByExample queryByExample = new QueryByExample(componentAttributeExample);
		queryByExample.setReturnNonProxied(false);
		return persistenceService.queryByExample(ComponentAttribute.class, queryByExample);
	}

	@Override
	public void removeAttributeCode(AttributeCodePk attributeCodePk)
	{
		Objects.requireNonNull(attributeCodePk, "AttributeCodePk is required.");

		AttributeCode attributeCode = persistenceService.findById(AttributeCode.class, attributeCodePk);
		if (attributeCode != null) {
			attributeCode.setActiveStatus(AttributeCode.INACTIVE_STATUS);
			attributeCode.setUpdateDts(TimeUtil.currentDate());
			attributeCode.setUpdateUser(SecurityUtil.getCurrentUserName());
			persistenceService.persist(attributeCode);

			BulkComponentAttributeChange bulkComponentAttributeChange = new BulkComponentAttributeChange();
			bulkComponentAttributeChange.setAttributes(getComponentAttributes(attributeCodePk.getAttributeType(), attributeCodePk.getAttributeCode()));
			bulkComponentAttributeChange.setOpertionType(BulkComponentAttributeChange.OpertionType.INACTIVE);
			(new ComponentServiceImpl(persistenceService)).bulkComponentAttributeChange(bulkComponentAttributeChange);

			cleanCaches(attributeCodePk.getAttributeType());
		}
	}

	@Override
	public void cascadeDeleteAttributeType(String type)
	{
		Objects.requireNonNull(type, "Attribute type is required.");

		AttributeType attributeType = persistenceService.findById(AttributeType.class, type);
		if (attributeType != null) {
			AttributeCode attributeCodeExample = new AttributeCode();
			AttributeCodePk attributeCodePk = new AttributeCodePk();
			attributeCodePk.setAttributeType(type);
			attributeCodeExample.setAttributeCodePk(attributeCodePk);
			persistenceService.deleteByExample(attributeCodeExample);

			deleteAttributeXrefType(type);

			TopicSearchItem topicSearchItemExample = new TopicSearchItem();
			topicSearchItemExample.setAttributeType(type);
			persistenceService.deleteByExample(topicSearchItemExample);

			ScheduledReport scheduledReport = new ScheduledReport();
			ReportOption reportOption = new ReportOption();
			reportOption.setCategory(type);
			scheduledReport.setReportOption(reportOption);
			persistenceService.deleteByExample(scheduledReport);

			persistenceService.delete(attributeType);

			BulkComponentAttributeChange bulkComponentAttributeChange = new BulkComponentAttributeChange();
			bulkComponentAttributeChange.setAttributes(getComponentAttributes(type, null));
			bulkComponentAttributeChange.setOpertionType(BulkComponentAttributeChange.OpertionType.DELETE);
			(new ComponentServiceImpl(persistenceService)).bulkComponentAttributeChange(bulkComponentAttributeChange);

			cleanCaches(type);
		}
	}

	@Override
	public void cascadeDeleteAttributeCode(AttributeCodePk attributeCodePk)
	{
		Objects.requireNonNull(attributeCodePk, "AttributeCodePk is required.");

		AttributeCode attributeCode = persistenceService.findById(AttributeCode.class, attributeCodePk);
		if (attributeCode != null) {

			AttributeXRefMap example = new AttributeXRefMap();
			example.setAttributeType(attributeCodePk.getAttributeType());
			example.setLocalCode(attributeCodePk.getAttributeCode());
			persistenceService.deleteByExample(example);

			persistenceService.delete(attributeCode);

			BulkComponentAttributeChange bulkComponentAttributeChange = new BulkComponentAttributeChange();
			bulkComponentAttributeChange.setAttributes(getComponentAttributes(attributeCodePk.getAttributeType(), attributeCodePk.getAttributeCode()));
			bulkComponentAttributeChange.setOpertionType(BulkComponentAttributeChange.OpertionType.DELETE);
			(new ComponentServiceImpl(persistenceService)).bulkComponentAttributeChange(bulkComponentAttributeChange);

			cleanCaches(attributeCodePk.getAttributeType());
		}
	}

	@Override
	public void activateAttributeCode(AttributeCodePk attributeCodePk)
	{
		Objects.requireNonNull(attributeCodePk, "AttributeCodePk is required.");

		AttributeCode attributeCode = persistenceService.findById(AttributeCode.class, attributeCodePk);
		if (attributeCode != null) {
			attributeCode.setActiveStatus(AttributeCode.ACTIVE_STATUS);
			attributeCode.setUpdateDts(TimeUtil.currentDate());
			attributeCode.setUpdateUser(SecurityUtil.getCurrentUserName());
			persistenceService.persist(attributeCode);

			BulkComponentAttributeChange bulkComponentAttributeChange = new BulkComponentAttributeChange();
			bulkComponentAttributeChange.setAttributes(getComponentAttributes(attributeCodePk.getAttributeType(), attributeCodePk.getAttributeCode()));
			bulkComponentAttributeChange.setOpertionType(BulkComponentAttributeChange.OpertionType.ACTIVATE);
			(new ComponentServiceImpl(persistenceService)).bulkComponentAttributeChange(bulkComponentAttributeChange);

			cleanCaches(attributeCodePk.getAttributeType());
		}
	}

	@Override
	public void syncAttribute(Map<AttributeType, List<AttributeCode>> attributeMap)
	{
		AttributeType attributeTypeExample = new AttributeType();
		List<AttributeType> attributeTypes = persistenceService.queryByExample(AttributeType.class, new QueryByExample(attributeTypeExample));
		Map<String, AttributeType> existingAttributeMap = new HashMap<>();
		attributeTypes.stream().forEach((attributeType) -> {
			existingAttributeMap.put(attributeType.getAttributeType(), attributeType);
		});

		attributeMap.keySet().stream().forEach(attributeType -> {

			try {

				ValidationModel validationModel = new ValidationModel(attributeType);
				validationModel.setConsumeFieldsOnly(true);
				ValidationResult validationResult = ValidationUtil.validate(validationModel);
				if (validationResult.valid()) {
					attributeType.setAttributeType(attributeType.getAttributeType().replace(ReflectionUtil.COMPOSITE_KEY_SEPERATOR, ReflectionUtil.COMPOSITE_KEY_REPLACER));

					AttributeType existing = existingAttributeMap.get(attributeType.getAttributeType());
					if (existing != null) {
						existing.setDescription(attributeType.getDescription());
						existing.setAllowMultipleFlg(attributeType.getAllowMultipleFlg());
						existing.setArchitectureFlg(attributeType.getArchitectureFlg());
						existing.setImportantFlg(attributeType.getImportantFlg());
						existing.setRequiredFlg(attributeType.getRequiredFlg());
						existing.setVisibleFlg(attributeType.getVisibleFlg());
						existing.setDetailedDescription(attributeType.getDetailedDescription());
						existing.setHideOnSubmission(attributeType.getHideOnSubmission());
						existing.setDefaultAttributeCode(attributeType.getDefaultAttributeCode());
						existing.setActiveStatus(AttributeType.ACTIVE_STATUS);
						existing.setCreateUser(OpenStorefrontConstant.SYSTEM_ADMIN_USER);
						existing.setUpdateUser(OpenStorefrontConstant.SYSTEM_ADMIN_USER);
						saveAttributeType(existing, false);
					} else {
						attributeType.setActiveStatus(AttributeType.ACTIVE_STATUS);
						attributeType.setCreateUser(OpenStorefrontConstant.SYSTEM_ADMIN_USER);
						attributeType.setUpdateUser(OpenStorefrontConstant.SYSTEM_ADMIN_USER);
						saveAttributeType(attributeType, false);
					}

					List<AttributeCode> existingAttributeCodes = findCodesForType(attributeType.getAttributeType());
					Map<String, AttributeCode> existingCodeMap = new HashMap<>();
					for (AttributeCode attributeCode : existingAttributeCodes) {
						existingCodeMap.put(attributeCode.getAttributeCodePk().toKey(), attributeCode);
					}

					Set<String> newCodeSet = new HashSet<>();
					List<AttributeCode> attributeCodes = attributeMap.get(attributeType);
					for (AttributeCode attributeCode : attributeCodes) {
						try {
							ValidationModel validationModelCode = new ValidationModel(attributeCode);
							validationModelCode.setConsumeFieldsOnly(true);
							ValidationResult validationResultCode = ValidationUtil.validate(validationModelCode);
							if (validationResultCode.valid()) {
								attributeCode.getAttributeCodePk().setAttributeCode(attributeCode.getAttributeCodePk().getAttributeCode().replace(ReflectionUtil.COMPOSITE_KEY_SEPERATOR, ReflectionUtil.COMPOSITE_KEY_REPLACER));

								AttributeCode existingCode = existingCodeMap.get(attributeCode.getAttributeCodePk().toKey());
								if (existingCode != null) {
									if (EntityUtil.isObjectsDifferent(existingCode, attributeCode, true)) {
										existingCode.setDescription(attributeCode.getDescription());
										existingCode.setDetailUrl(attributeCode.getDetailUrl());
										existingCode.setLabel(attributeCode.getLabel());
										existingCode.setArchitectureCode(attributeCode.getArchitectureCode());
										existingCode.setBadgeUrl(attributeCode.getBadgeUrl());
										existingCode.setGroupCode(attributeCode.getGroupCode());
										existingCode.setSortOrder(attributeCode.getSortOrder());
										existingCode.setHighlightStyle(attributeCode.getHighlightStyle());
										existingCode.setActiveStatus(AttributeCode.ACTIVE_STATUS);
										existingCode.setCreateUser(OpenStorefrontConstant.SYSTEM_ADMIN_USER);
										existingCode.setUpdateUser(OpenStorefrontConstant.SYSTEM_ADMIN_USER);
										saveAttributeCode(existingCode, false);
									}
								} else {
									attributeCode.setActiveStatus(AttributeCode.ACTIVE_STATUS);
									attributeCode.setCreateUser(OpenStorefrontConstant.SYSTEM_ADMIN_USER);
									attributeCode.setUpdateUser(OpenStorefrontConstant.SYSTEM_ADMIN_USER);
									saveAttributeCode(attributeCode, false);
								}
								newCodeSet.add(attributeCode.getAttributeCodePk().toKey());
							} else {
								log.log(Level.WARNING, MessageFormat.format("(Data Sync) Unable to Add  Attribute Code:  {0} Validation Issues:\n{1}", new Object[]{attributeCode.getAttributeCodePk().toKey(), validationResult.toString()}));
							}
						} catch (Exception e) {
							log.log(Level.SEVERE, "Unable to save attribute code: " + attributeCode.getAttributeCodePk().toKey(), e);
						}
					}
					//inactive missing codes
					existingAttributeCodes.stream().forEach((attributeCode) -> {
						if (newCodeSet.contains(attributeCode.getAttributeCodePk().toKey()) == false) {
							attributeCode.setActiveStatus(LookupEntity.INACTIVE_STATUS);
							attributeCode.setUpdateUser(OpenStorefrontConstant.SYSTEM_ADMIN_USER);
							removeAttributeCode(attributeCode.getAttributeCodePk());
						}
					});
				} else {
					log.log(Level.WARNING, MessageFormat.format("(Data Sync) Unable to Add Type:  {0} Validation Issues:\n{1}", new Object[]{attributeType.getAttributeType(), validationResult.toString()}));
				}
			} catch (Exception e) {
				log.log(Level.SEVERE, "Unable to save attribute type:" + attributeType.getAttributeType(), e);
			}
		});
		//Clear cache
		OSFCacheManager.getAttributeTypeCache().removeAll();
		OSFCacheManager.getAttributeCache().removeAll();
		OSFCacheManager.getAttributeCodeAllCache().removeAll();

		getSearchService().saveAll();
	}

	@Override
	public AttributeCode findCodeForType(AttributeCodePk pk)
	{
		AttributeCode attributeCode = null;
		List<AttributeCode> attributeCodes = findCodesForType(pk.getAttributeType());
		for (AttributeCode attributeCodeCheck : attributeCodes) {
			if (attributeCodeCheck.getAttributeCodePk().getAttributeCode().equals(pk.getAttributeCode())) {
				attributeCode = attributeCodeCheck;
				break;
			}
		}

		return attributeCode;
	}

	@Override
	public AttributeType findType(String type)
	{
		AttributeType attributeType = null;

		Element element = OSFCacheManager.getAttributeTypeCache().get(type);
		if (element != null) {
			attributeType = (AttributeType) element.getObjectValue();
		} else {
			AttributeType attributeTypeExample = new AttributeType();
			attributeTypeExample.setActiveStatus(AttributeType.ACTIVE_STATUS);
			List<AttributeType> attributeTypes = persistenceService.queryByExample(AttributeType.class, new QueryByExample(attributeTypeExample));
			for (AttributeType attributeTypeCheck : attributeTypes) {
				if (attributeTypeCheck.getAttributeType().equals(type)) {
					attributeType = attributeTypeCheck;
				}
				element = new Element(attributeTypeCheck.getAttributeType(), attributeTypeCheck);
				OSFCacheManager.getAttributeTypeCache().put(element);
			}
		}

		return attributeType;
	}

	@Override
	public Architecture generateArchitecture(String attributeType)
	{
		Architecture architecture = new Architecture();

		AttributeType attributeTypeFull = persistenceService.findById(AttributeType.class, attributeType);
		if (attributeTypeFull != null) {
			if (attributeTypeFull.getArchitectureFlg()) {
				architecture.setName(attributeTypeFull.getDescription());
				architecture.setAttributeType(attributeType);

				String rootCode = "0";
				List<AttributeCode> attributeCodes = findCodesForType(attributeType);
				for (AttributeCode attributeCode : attributeCodes) {
					if (rootCode.equals(attributeCode.adjustedArchitectureCode())) {
						architecture.setAttributeCode(attributeCode.adjustedArchitectureCode());
						architecture.setDescription(attributeCode.getDescription());
					} else {
						String codeTokens[] = attributeCode.adjustedArchitectureCode().split(Pattern.quote("."));
						Architecture rootArchtecture = architecture;
						StringBuilder codeKey = new StringBuilder();
						for (int i = 0; i < codeTokens.length - 1; i++) {
							codeKey.append(codeTokens[i]);

							//put in stubs as needed
							boolean found = false;
							for (Architecture child : rootArchtecture.getChildren()) {
								if (child.getAttributeCode().equals(codeKey.toString())) {
									found = true;
									rootArchtecture = child;
									break;
								}
							}
							if (!found) {
								Architecture newChild = new Architecture();
								newChild.setAttributeCode(codeKey.toString());
								newChild.setAttributeType(attributeType);
								rootArchtecture.getChildren().add(newChild);
								rootArchtecture = newChild;
							}
							codeKey.append(".");
						}
						//now find the correct postion and add/update
						boolean found = false;
						for (Architecture child : rootArchtecture.getChildren()) {
							if (child.getAttributeCode().equals(attributeCode.adjustedArchitectureCode())) {
								child.setName(attributeCode.getLabel());
								child.setDescription(attributeCode.getDescription());
								found = true;
							}
						}
						if (!found) {
							Architecture newChild = new Architecture();
							newChild.setAttributeCode(attributeCode.adjustedArchitectureCode());
							newChild.setOriginalAttributeCode(attributeCode.getAttributeCodePk().getAttributeCode());
							newChild.setArchitectureCode(attributeCode.getArchitectureCode());
							newChild.setSortOrder(attributeCode.getSortOrder());
							newChild.setAttributeType(attributeType);
							newChild.setName(attributeCode.getLabel());
							newChild.setDescription(attributeCode.getDescription());
							rootArchtecture.getChildren().add(newChild);
						}
					}
				}

			} else {
				throw new OpenStorefrontRuntimeException("Attribute Type is not an architecture: " + attributeType, "Make sure type is an architecture.");
			}
		} else {
			throw new OpenStorefrontRuntimeException("Unable to find attribute type: " + attributeType, "Check type code.");
		}
		sortArchitecture(architecture.getChildren());
		return architecture;
	}

	private void sortArchitecture(List<Architecture> architectures)
	{
		if (architectures.isEmpty()) {
			return;
		}

		for (Architecture architecture : architectures) {
			sortArchitecture(architecture.getChildren());
		}
		architectures.sort(new ArchitectureComparator<>());
	}

	@Override
	public List<AttributeXRefType> getAttributeXrefTypes(AttributeXrefModel attributeXrefModel)
	{
		AttributeXRefType xrefAttributeTypeExample = new AttributeXRefType();
		xrefAttributeTypeExample.setActiveStatus(AttributeXRefType.ACTIVE_STATUS);
		xrefAttributeTypeExample.setIntegrationType(attributeXrefModel.getIntegrationType());
		xrefAttributeTypeExample.setProjectType(attributeXrefModel.getProjectKey());
		xrefAttributeTypeExample.setIssueType(attributeXrefModel.getIssueType());
		List<AttributeXRefType> xrefAttributeTypes = persistenceService.queryByExample(AttributeXRefType.class, xrefAttributeTypeExample);
		return xrefAttributeTypes;
	}

	@Override
	public Map<String, Map<String, String>> getAttributeXrefMapFieldMap()
	{
		Map<String, Map<String, String>> attributeCodeMap = new HashMap<>();

		AttributeXRefMap xrefAttributeMapExample = new AttributeXRefMap();
		xrefAttributeMapExample.setActiveStatus(AttributeXRefMap.ACTIVE_STATUS);

		List<AttributeXRefMap> xrefAttributeMaps = persistenceService.queryByExample(AttributeXRefMap.class, xrefAttributeMapExample);
		for (AttributeXRefMap xrefAttributeMap : xrefAttributeMaps) {

			if (attributeCodeMap.containsKey(xrefAttributeMap.getAttributeType())) {
				Map<String, String> codeMap = attributeCodeMap.get(xrefAttributeMap.getAttributeType());
				if (codeMap.containsKey(xrefAttributeMap.getExternalCode())) {

					//should only have one external code if there's a dup we'll only use one.
					//(however, which  code  is used depends on the order that came in.  which is not  determinate)
					//First one we hit wins
					log.log(Level.WARNING, MessageFormat.format("Duplicate external code for attribute type: {0} Code: {1}", new Object[]{xrefAttributeMap.getAttributeType(), xrefAttributeMap.getExternalCode()}));
				} else {
					codeMap.put(xrefAttributeMap.getExternalCode(), xrefAttributeMap.getLocalCode());
				}
			} else {
				Map<String, String> codeMap = new HashMap<>();
				codeMap.put(xrefAttributeMap.getExternalCode(), xrefAttributeMap.getLocalCode());
				attributeCodeMap.put(xrefAttributeMap.getAttributeType(), codeMap);
			}
		}

		return attributeCodeMap;
	}

	@Override
	public void saveAttributeXrefMap(AttributeXRefView attributeXRefView)
	{
		AttributeXRefType type = persistenceService.findById(AttributeXRefType.class, attributeXRefView.getType().getAttributeType());
		if (type != null) {
			type.setAttributeType(attributeXRefView.getType().getAttributeType());
			type.setActiveStatus(attributeXRefView.getType().getActiveStatus());
			type.setFieldId(attributeXRefView.getType().getFieldId());
			type.setFieldName(attributeXRefView.getType().getFieldName());
			type.setIntegrationType(attributeXRefView.getType().getIntegrationType());
			type.setIssueType(attributeXRefView.getType().getIssueType());
			type.setProjectType(attributeXRefView.getType().getProjectType());
			persistenceService.persist(type);
			AttributeXRefMap mapTemp = new AttributeXRefMap();
			mapTemp.setAttributeType(type.getAttributeType());
			List<AttributeXRefMap> tempMaps = persistenceService.queryByExample(AttributeXRefMap.class, new QueryByExample(mapTemp));
			for (AttributeXRefMap tempMap : tempMaps) {
				mapTemp = persistenceService.findById(AttributeXRefMap.class, tempMap.getXrefId());
				persistenceService.delete(mapTemp);
			}

			for (AttributeXRefMap map : attributeXRefView.getMap()) {
				AttributeXRefMap temp = persistenceService.queryOneByExample(AttributeXRefMap.class, map);
				if (temp != null) {
					temp.setActiveStatus(map.getActiveStatus());
					temp.setAttributeType(map.getAttributeType());
					temp.setExternalCode(map.getExternalCode());
					temp.setLocalCode(map.getLocalCode());
					persistenceService.persist(temp);
				} else {
					map.setActiveStatus(AttributeXRefMap.ACTIVE_STATUS);
					map.setXrefId(persistenceService.generateId());
					persistenceService.persist(map);
				}
			}
		} else {
			attributeXRefView.getType().setActiveStatus(AttributeXRefType.ACTIVE_STATUS);
			persistenceService.persist(attributeXRefView.getType());
			for (AttributeXRefMap map : attributeXRefView.getMap()) {
				map.setActiveStatus(AttributeXRefMap.ACTIVE_STATUS);
				map.setXrefId(persistenceService.generateId());
				persistenceService.persist(map);
			}
		}

	}

	@Override
	public void deleteAttributeXrefType(String attributeType)
	{
		AttributeXRefMap example = new AttributeXRefMap();
		example.setAttributeType(attributeType);
		persistenceService.deleteByExample(example);

		AttributeXRefType attributeXRefType = persistenceService.findById(AttributeXRefType.class, attributeType);
		if (attributeXRefType != null) {
			persistenceService.delete(attributeXRefType);
		}
	}

	@Override
	public void activateAttributeType(String type)
	{
		AttributeType attributeType = persistenceService.findById(AttributeType.class, type);

		if (attributeType != null) {
			attributeType.setActiveStatus(AttributeType.ACTIVE_STATUS);
			attributeType.setUpdateDts(TimeUtil.currentDate());
			attributeType.setUpdateUser(SecurityUtil.getCurrentUserName());
			persistenceService.persist(attributeType);

			BulkComponentAttributeChange bulkComponentAttributeChange = new BulkComponentAttributeChange();
			bulkComponentAttributeChange.setAttributes(getComponentAttributes(type, null));
			bulkComponentAttributeChange.setOpertionType(BulkComponentAttributeChange.OpertionType.ACTIVATE);
			(new ComponentServiceImpl(persistenceService)).bulkComponentAttributeChange(bulkComponentAttributeChange);

			cleanCaches(type);
		} else {
			throw new OpenStorefrontRuntimeException("Unable to save sort order.  Attribute Type: " + type, "Check data");
		}
	}

	@Override
	public void saveAttributeCodeSortOrder(AttributeCodePk attributeCodePk, Integer sortOrder)
	{
		Objects.requireNonNull(attributeCodePk, "Attribute Code PK is required");
		AttributeCode code = persistenceService.findById(AttributeCode.class, attributeCodePk);
		if (code != null) {
			code.setSortOrder(sortOrder);
			persistenceService.persist(code);

			cleanCaches(attributeCodePk.getAttributeType());
		} else {
			throw new OpenStorefrontRuntimeException("Unable to save sort order.  Attribute code: " + attributeCodePk.toString(), "Check data");
		}
	}

	@Override
	public AttributeTypeWrapper getFilteredTypes(FilterQueryParams filter)
	{
		AttributeTypeWrapper result = new AttributeTypeWrapper();

		AttributeType attributeExample = new AttributeType();
		if (filter.getAll() == null || filter.getAll() == false) {
			attributeExample.setActiveStatus(filter.getStatus());
		}

		QueryByExample queryByExample = new QueryByExample(attributeExample);

		queryByExample.setMaxResults(filter.getMax());
		queryByExample.setFirstResult(filter.getOffset());
		queryByExample.setSortDirection(filter.getSortOrder());

		AttributeType attributeOrderExample = new AttributeType();
		Field sortField = ReflectionUtil.getField(attributeOrderExample, filter.getSortField());
		if (sortField != null) {
			try {
				BeanUtils.setProperty(attributeOrderExample, sortField.getName(), QueryByExample.getFlagForType(sortField.getType()));
			} catch (IllegalAccessException | InvocationTargetException ex) {
				log.log(Level.WARNING, "Unable to set sort field", ex);
			}
			queryByExample.setOrderBy(attributeOrderExample);
		}

		List<AttributeType> attributes = persistenceService.queryByExample(AttributeType.class, queryByExample);

		result.setData(attributes);

		queryByExample.setQueryType(QueryType.COUNT);
		result.setTotalNumber(persistenceService.countByExample(queryByExample));
		return result;
	}

	@Override
	public AttributeCodeWrapper getFilteredCodes(FilterQueryParams filter, String type)
	{
		AttributeCodeWrapper result = new AttributeCodeWrapper();
		AttributeCodePk pk = new AttributeCodePk();
		pk.setAttributeType(type);
		AttributeCode attributeExample = new AttributeCode();
		attributeExample.setAttributeCodePk(pk);
		if (filter.getAll() == null || filter.getAll() == false) {
			attributeExample.setActiveStatus(filter.getStatus());
		}

		QueryByExample queryByExample = new QueryByExample(attributeExample);

		queryByExample.setMaxResults(filter.getMax());
		queryByExample.setFirstResult(filter.getOffset());
		queryByExample.setSortDirection(filter.getSortOrder());

		AttributeCode attributeOrderExample = new AttributeCode();
		if (filter.getSortField().equals("code")) {
			AttributeCodePk newPk = new AttributeCodePk();
			newPk.setAttributeCode(QueryByExample.STRING_FLAG);
			attributeOrderExample.setAttributeCodePk(newPk);
			queryByExample.setOrderBy(attributeOrderExample);
		} else {

			Field sortField = ReflectionUtil.getField(attributeOrderExample, filter.getSortField());
			if (sortField != null) {
				try {
					BeanUtils.setProperty(attributeOrderExample, sortField.getName(), QueryByExample.getFlagForType(sortField.getType()));
				} catch (IllegalAccessException | InvocationTargetException ex) {
					log.log(Level.WARNING, "Unable to set sort field", ex);
				}
				queryByExample.setOrderBy(attributeOrderExample);
			}
		}

		List<AttributeCode> attributes = persistenceService.queryByExample(AttributeCode.class, queryByExample);
		List<AttributeCodeView> views = AttributeCodeView.toViews(attributes);

		result.setData(views);

		queryByExample.setQueryType(QueryType.COUNT);
		result.setTotalNumber(persistenceService.countByExample(queryByExample));
		return result;
	}

	@Override
	public void changeAttributeCode(AttributeCodePk attributeCodePk, String newCode)
	{

		String query = "Update " + ComponentAttributePk.class.getSimpleName() + " set attributeCode = :attributeCodeParamReplace where attributeCode = :oldCodeParam and attributeType = :attributeTypeParam";
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("attributeCodeParamReplace", newCode);
		parameters.put("oldCodeParam", attributeCodePk.getAttributeCode());
		parameters.put("attributeTypeParam", attributeCodePk.getAttributeType());

		persistenceService.runDbCommand(query, parameters);

		query = "Update " + AttributeCodePk.class.getSimpleName() + " set attributeCode = :attributeCodeParamReplace where attributeCode = :oldCodeParam and attributeType = :attributeTypeParam";
		parameters = new HashMap<>();
		parameters.put("attributeCodeParamReplace", newCode);
		parameters.put("oldCodeParam", attributeCodePk.getAttributeCode());
		parameters.put("attributeTypeParam", attributeCodePk.getAttributeType());

		persistenceService.runDbCommand(query, parameters);

		cleanCaches(attributeCodePk.getAttributeType());
	}

	public void importAttributes(List<AttributeAll> attributes, FileHistoryOption options)
	{
		attributes.forEach(attribute
				-> {
			AttributeType existing = findType(attribute.getAttributeType().toString());
			if (existing != null) {
				attribute.getAttributeType().setAttributeType(existing.getAttributeType());
			}
			saveAttributeType(attribute.getAttributeType());
			attribute.getAttributeCodes().forEach(code -> {
				saveAttributeCode(code);
			});
		});
	}

}
