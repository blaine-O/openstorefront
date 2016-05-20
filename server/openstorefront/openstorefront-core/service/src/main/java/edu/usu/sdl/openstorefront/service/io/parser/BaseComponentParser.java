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

import edu.usu.sdl.openstorefront.common.util.OpenStorefrontConstant;
import edu.usu.sdl.openstorefront.core.entity.ApprovalStatus;
import edu.usu.sdl.openstorefront.core.entity.AttributeCode;
import edu.usu.sdl.openstorefront.core.entity.AttributeCodePk;
import edu.usu.sdl.openstorefront.core.entity.AttributeType;
import edu.usu.sdl.openstorefront.core.entity.Component;
import edu.usu.sdl.openstorefront.core.entity.ComponentAttribute;
import edu.usu.sdl.openstorefront.core.entity.ComponentAttributePk;
import edu.usu.sdl.openstorefront.core.entity.ComponentType;
import edu.usu.sdl.openstorefront.core.entity.FileHistoryOption;
import edu.usu.sdl.openstorefront.core.entity.ModificationType;
import edu.usu.sdl.openstorefront.core.entity.SecurityMarkingType;
import edu.usu.sdl.openstorefront.core.model.ComponentAll;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
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
		ComponentAll componentAll = new ComponentAll();
		Component component = new Component();
		component.setApprovalState(ApprovalStatus.PENDING);
		component.setComponentType(ComponentType.COMPONENT);
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
	
	protected String getSecurityMarking(String inputMarking)
	{
		//try to match if not add marking
		inputMarking = inputMarking.trim().toUpperCase();
		SecurityMarkingType securityMarking = service.getLookupService().getLookupEnity(SecurityMarkingType.class, inputMarking);
		if (securityMarking == null)
		{
			//check description
			securityMarking = service.getLookupService().getLookupEnityByDesc(SecurityMarkingType.class, inputMarking);
			if (securityMarking == null)
			{
				securityMarking = new SecurityMarkingType();
				securityMarking.setCode(StringUtils.left(inputMarking, OpenStorefrontConstant.FIELD_SIZE_CODE));				
				securityMarking.setDescription(inputMarking);
				service.getLookupService().saveLookupValue(securityMarking);
				log.log(Level.INFO, MessageFormat.format("Added missing security marking: {0}", inputMarking));
			}
		}
		return securityMarking.getCode();
	}
	
	
}
