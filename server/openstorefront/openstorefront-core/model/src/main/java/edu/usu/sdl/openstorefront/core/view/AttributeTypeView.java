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
package edu.usu.sdl.openstorefront.core.view;

import edu.usu.sdl.openstorefront.common.util.Convert;
import edu.usu.sdl.openstorefront.core.annotation.DataType;
import edu.usu.sdl.openstorefront.core.entity.AttributeType;
import edu.usu.sdl.openstorefront.core.entity.ComponentTypeRestriction;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;

public class AttributeTypeView
		extends StandardEntityView
{

	@NotNull
	private String attributeType;

	@NotNull
	private String description;

	@NotNull
	private boolean visibleFlg;

	@NotNull
	private boolean requiredFlg;

	@NotNull
	private boolean architectureFlg;

	@NotNull
	private boolean importantFlg;

	@NotNull
	private boolean allowMultipleFlg;

	@NotNull
	private boolean hideOnSubmission;

	private String defaultAttributeCode;
	private String detailedDescription;

	@NotNull
	private String activeStatus;

	@DataType(AttributeCodeView.class)
	private List<AttributeCodeView> codes = new ArrayList<>();

	@DataType(ComponentTypeRestriction.class)
	private List<ComponentTypeRestriction> requiredRestrictions = new ArrayList<>();

	@DataType(ComponentTypeRestriction.class)
	private List<ComponentTypeRestriction> associatedComponentTypes = new ArrayList<>();

	public AttributeTypeView()
	{
	}

	public static AttributeTypeView toView(AttributeType attributeType)
	{
		AttributeTypeView attributeTypeView = new AttributeTypeView();
		attributeTypeView.setAttributeType(attributeType.getAttributeType());
		attributeTypeView.setAllowMultipleFlg(Convert.toBoolean(attributeType.getAllowMultipleFlg()));
		attributeTypeView.setArchitectureFlg(Convert.toBoolean(attributeType.getArchitectureFlg()));
		attributeTypeView.setDescription(attributeType.getDescription());
		attributeTypeView.setImportantFlg(Convert.toBoolean(attributeType.getImportantFlg()));
		attributeTypeView.setRequiredFlg(Convert.toBoolean(attributeType.getRequiredFlg()));
		attributeTypeView.setVisibleFlg(Convert.toBoolean(attributeType.getVisibleFlg()));
		attributeTypeView.setDetailedDescription(attributeType.getDetailedDescription());
		attributeTypeView.setHideOnSubmission(Convert.toBoolean(attributeType.getHideOnSubmission()));
		attributeTypeView.setDefaultAttributeCode(attributeType.getDefaultAttributeCode());
		attributeTypeView.setActiveStatus(attributeType.getActiveStatus());
		attributeTypeView.setRequiredRestrictions(attributeType.getRequiredRestrictions());
		attributeTypeView.setAssociatedComponentTypes(attributeType.getAssociatedComponentTypes());
		attributeTypeView.toStandardView(attributeType);

		return attributeTypeView;

	}

	public String getAttributeType()
	{
		return attributeType;
	}

	public void setAttributeType(String attributeType)
	{
		this.attributeType = attributeType;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public List<AttributeCodeView> getCodes()
	{
		return codes;
	}

	public void setCodes(List<AttributeCodeView> codes)
	{
		this.codes = codes;
	}

	public boolean getVisibleFlg()
	{
		return visibleFlg;
	}

	public void setVisibleFlg(boolean visibleFlg)
	{
		this.visibleFlg = visibleFlg;
	}

	public boolean getRequiredFlg()
	{
		return requiredFlg;
	}

	public void setRequiredFlg(boolean requiredFlg)
	{
		this.requiredFlg = requiredFlg;
	}

	public boolean getArchitectureFlg()
	{
		return architectureFlg;
	}

	public void setArchitectureFlg(boolean architectureFlg)
	{
		this.architectureFlg = architectureFlg;
	}

	public boolean getImportantFlg()
	{
		return importantFlg;
	}

	public void setImportantFlg(boolean importantFlg)
	{
		this.importantFlg = importantFlg;
	}

	public boolean getAllowMultipleFlg()
	{
		return allowMultipleFlg;
	}

	public boolean isAllowMultipleFlg()
	{
		return allowMultipleFlg;
	}

	public void setAllowMultipleFlg(boolean allowMultipleFlg)
	{
		this.allowMultipleFlg = allowMultipleFlg;
	}

	public String getActiveStatus()
	{
		return activeStatus;
	}

	public void setActiveStatus(String activeStatus)
	{
		this.activeStatus = activeStatus;
	}

	public boolean getHideOnSubmission()
	{
		return hideOnSubmission;
	}

	public void setHideOnSubmission(boolean hideOnSubmission)
	{
		this.hideOnSubmission = hideOnSubmission;
	}

	public String getDefaultAttributeCode()
	{
		return defaultAttributeCode;
	}

	public void setDefaultAttributeCode(String defaultAttributeCode)
	{
		this.defaultAttributeCode = defaultAttributeCode;
	}

	public String getDetailedDescription()
	{
		return detailedDescription;
	}

	public void setDetailedDescription(String detailedDescription)
	{
		this.detailedDescription = detailedDescription;
	}

	public List<ComponentTypeRestriction> getRequiredRestrictions()
	{
		return requiredRestrictions;
	}

	public void setRequiredRestrictions(List<ComponentTypeRestriction> requiredRestrictions)
	{
		this.requiredRestrictions = requiredRestrictions;
	}

	public List<ComponentTypeRestriction> getAssociatedComponentTypes()
	{
		return associatedComponentTypes;
	}

	public void setAssociatedComponentTypes(List<ComponentTypeRestriction> associatedComponentTypes)
	{
		this.associatedComponentTypes = associatedComponentTypes;
	}

}
