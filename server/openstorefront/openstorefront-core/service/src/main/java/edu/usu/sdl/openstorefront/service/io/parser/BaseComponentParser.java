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
package edu.usu.sdl.openstorefront.service.io.parser;

import edu.usu.sdl.openstorefront.common.exception.OpenStorefrontRuntimeException;
import edu.usu.sdl.openstorefront.common.util.OpenStorefrontConstant;
import edu.usu.sdl.openstorefront.core.entity.ApprovalStatus;
import edu.usu.sdl.openstorefront.core.entity.AttributeCode;
import edu.usu.sdl.openstorefront.core.entity.AttributeCodePk;
import edu.usu.sdl.openstorefront.core.entity.AttributeType;
import edu.usu.sdl.openstorefront.core.entity.Component;
import edu.usu.sdl.openstorefront.core.entity.ComponentAttribute;
import edu.usu.sdl.openstorefront.core.entity.ComponentAttributePk;
import edu.usu.sdl.openstorefront.core.entity.ComponentType;
import edu.usu.sdl.openstorefront.core.entity.ComponentTypeRestriction;
import edu.usu.sdl.openstorefront.core.entity.FileHistoryOption;
import edu.usu.sdl.openstorefront.core.entity.LookupEntity;
import edu.usu.sdl.openstorefront.core.entity.ModificationType;
import edu.usu.sdl.openstorefront.core.model.ComponentAll;
import edu.usu.sdl.openstorefront.validation.CleanKeySanitizer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author dshurtleff
 */
public abstract class BaseComponentParser
		extends AbstractParser
{
	private static final Logger log = Logger.getLogger(BaseComponentParser.class.getName());

	protected static final int MAX_BUCKET_SIZE = 100;
	protected List<ComponentAll> componentsAll = new ArrayList<>();

	protected List<AttributeType> requiredAttributes;

	protected ComponentAll defaultComponentAll()
	{
		return defaultComponentAll(ComponentType.COMPONENT);
	}	
	
	protected ComponentAll defaultComponentAll(String componentType)
	{
		ComponentAll componentAll = new ComponentAll();
		Component component = new Component();
		component.setApprovalState(ApprovalStatus.PENDING);
		component.setComponentType(componentType);		
		component.setDescription(OpenStorefrontConstant.NOT_AVAILABLE);
		component.setCreateUser(fileHistoryAll.getFileHistory().getCreateUser());
		component.setUpdateUser(fileHistoryAll.getFileHistory().getUpdateUser());
		component.setDataSource(fileHistoryAll.getFileHistory().getDataSource());
		updateComponentStandardFields(component);

		componentAll.setComponent(component);

		//required Attributes
		if (requiredAttributes == null) {
			AttributeType attributeTypeRequired = new AttributeType();
			attributeTypeRequired.setRequiredFlg(Boolean.TRUE);

			requiredAttributes = attributeTypeRequired.findByExample();
		}

		for (AttributeType attributeType : requiredAttributes) {
			
			
			boolean attributeRequiredForType = false;
			if (attributeType.getRequiredRestrictions() != null) {
				Set<String> componentTypesInRestriction = new HashSet<>();				
				for (ComponentTypeRestriction restriction : attributeType.getRequiredRestrictions()) {
					componentTypesInRestriction.add(restriction.getComponentType());
				}
				if (componentTypesInRestriction.contains(componentType)) {
					attributeRequiredForType = true;
				}
			} else {
				attributeRequiredForType = true;
			}
			
			if (attributeRequiredForType) {
				if (StringUtils.isNotBlank(attributeType.getDefaultAttributeCode())) {
					AttributeCodePk attributeCodePk = new AttributeCodePk();
					attributeCodePk.setAttributeType(attributeType.getAttributeType());
					attributeCodePk.setAttributeCode(attributeType.getDefaultAttributeCode());
					AttributeCode attributeCode = service.getAttributeService().findCodeForType(attributeCodePk);
					if (attributeCode != null) {
						ComponentAttribute componentAttribute = new ComponentAttribute();
						ComponentAttributePk componentAttributePk = new ComponentAttributePk();
						componentAttributePk.setAttributeCode(attributeCode.getAttributeCodePk().getAttributeCode());
						componentAttributePk.setAttributeType(attributeCode.getAttributeCodePk().getAttributeType());
						componentAttribute.setComponentAttributePk(componentAttributePk);

						componentAll.getAttributes().add(componentAttribute);
					}
				}
			}
		}

		return componentAll;
	}

	protected void updateComponentStandardFields(Component component)
	{
		component.setFileHistoryId(fileHistoryAll.getFileHistory().getFileHistoryId());
		component.setLastModificationType(ModificationType.IMPORT);
	}

	@Override
	protected <T> List<T> getStorageBucket()
	{
		return (List<T>) componentsAll;
	}

	@Override
	protected int getMaxBucketSize()
	{
		return MAX_BUCKET_SIZE;
	}

	@Override
	protected void performStorage()
	{
		if (fileHistoryAll.getFileHistory().getFileHistoryOption() == null) {
			service.getComponentService().importComponents(componentsAll, new FileHistoryOption());
		} else {
			service.getComponentService().importComponents(componentsAll, fileHistoryAll.getFileHistory().getFileHistoryOption());
		}
	}

	/**
	 * Try to match if not will add a new code
	 * @param lookupClass
	 * @param input
	 * @return 
	 */
	protected String getLookup(Class lookupClass, String input)
	{
		if (StringUtils.isNotBlank(input)) {

			input = input.trim();
			CleanKeySanitizer sanitizer = new CleanKeySanitizer();
			String key = sanitizer.santize(input).toString();

			LookupEntity lookup = service.getLookupService().getLookupEnity(lookupClass, key);
			if (lookup == null) {
				//check description
				lookup = service.getLookupService().getLookupEnityByDesc(lookupClass, input);
				if (lookup == null) {
					try {
						lookup = (LookupEntity) lookupClass.newInstance();
						lookup.setCode(StringUtils.left(key, OpenStorefrontConstant.FIELD_SIZE_CODE));
						lookup.setDescription(StringUtils.left(input, OpenStorefrontConstant.FIELD_SIZE_GENERAL_TEXT));
						lookup.setCreateUser(fileHistoryAll.getFileHistory().getCreateUser());
						lookup.setUpdateUser(fileHistoryAll.getFileHistory().getCreateUser());
						
						service.getLookupService().saveLookupValue(lookup);
						log.log(Level.INFO, MessageFormat.format("Added missing lookup: {0} to lookup {1}", input, lookupClass.getSimpleName()));
					} catch (InstantiationException | IllegalAccessException ex) {
						throw new OpenStorefrontRuntimeException("Unable to create a new instance of look up class.", ex);
					}
				}
			}
			return lookup.getCode();
		}
		return input;
	}	
	
	/**
	 * This will attempt to find the attribute but if not found it will add type and code
	 * @param attributeTypeCode
	 * @param attributeCode
	 * @param attributeDescribtion
	 * @return 
	 */
	protected AttributeCode getAttributeCode(String attributeTypeCode, String attributeTypeDescription, String attributeCode, String attributeCodeDescription)
	{	
		if (StringUtils.isBlank(attributeCode) ||
			StringUtils.isBlank(attributeCodeDescription)) 
		{
			return null;
		}
		
		AttributeType attributeType = service.getAttributeService().findType(attributeTypeCode);
		if (attributeType == null) {
			attributeType = new AttributeType();
			attributeType.setAttributeType(attributeTypeCode);
			attributeType.setAllowMultipleFlg(Boolean.TRUE);
			attributeType.setArchitectureFlg(Boolean.FALSE);
			attributeType.setImportantFlg(Boolean.FALSE);
			attributeType.setHideOnSubmission(Boolean.FALSE);
			attributeType.setRequiredFlg(Boolean.FALSE);
			attributeType.setVisibleFlg(Boolean.FALSE);
						
			attributeType.setDescription(attributeTypeDescription);
			attributeType.setCreateUser(fileHistoryAll.getFileHistory().getCreateUser());
			attributeType.setUpdateUser(fileHistoryAll.getFileHistory().getCreateUser());
					
			service.getAttributeService().saveAttributeType(attributeType, false);			
		}
		AttributeCodePk attributeCodePk = new AttributeCodePk();
		attributeCodePk.setAttributeType(attributeType.getAttributeType());
		
		CleanKeySanitizer sanitizer = new CleanKeySanitizer();
		String key = sanitizer.santize(attributeCode).toString();
		attributeCodePk.setAttributeCode(StringUtils.left(key, OpenStorefrontConstant.FIELD_SIZE_CODE));		
		
		AttributeCode attributeCodeFound = service.getAttributeService().findCodeForType(attributeCodePk);
		if (attributeCodeFound == null) {
			attributeCodeFound = new AttributeCode();
			attributeCodeFound.setAttributeCodePk(attributeCodePk);
			attributeCodeFound.setLabel(StringUtils.left(attributeCodeDescription, OpenStorefrontConstant.FIELD_SIZE_GENERAL_TEXT));	
			attributeCodeFound.setCreateUser(fileHistoryAll.getFileHistory().getCreateUser());
			attributeCodeFound.setUpdateUser(fileHistoryAll.getFileHistory().getCreateUser());			
			
			service.getAttributeService().saveAttributeCode(attributeCodeFound, false);
		}
		
		return attributeCodeFound;
	}
	
}
