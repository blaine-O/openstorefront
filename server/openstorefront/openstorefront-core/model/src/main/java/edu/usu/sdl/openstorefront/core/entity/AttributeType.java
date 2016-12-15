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
package edu.usu.sdl.openstorefront.core.entity;

import edu.usu.sdl.openstorefront.common.util.Convert;
import edu.usu.sdl.openstorefront.common.util.OpenStorefrontConstant;
import edu.usu.sdl.openstorefront.core.annotation.APIDescription;
import edu.usu.sdl.openstorefront.core.annotation.ConsumeField;
import edu.usu.sdl.openstorefront.core.annotation.DataType;
import edu.usu.sdl.openstorefront.core.annotation.DefaultFieldValue;
import edu.usu.sdl.openstorefront.core.annotation.PK;
import edu.usu.sdl.openstorefront.validation.BasicHTMLSanitizer;
import edu.usu.sdl.openstorefront.validation.CleanKeySanitizer;
import edu.usu.sdl.openstorefront.validation.RuleResult;
import edu.usu.sdl.openstorefront.validation.Sanitize;
import edu.usu.sdl.openstorefront.validation.TextSanitizer;
import edu.usu.sdl.openstorefront.validation.ValidationResult;
import java.util.List;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.StringUtils;

@APIDescription("Allows for grouping the metadata into categories")
public class AttributeType
		extends StandardEntity<AttributeType>
{

	@PK
	@NotNull
	@Size(min = 1, max = OpenStorefrontConstant.FIELD_SIZE_CODE)
	@Sanitize(CleanKeySanitizer.class)
	@ConsumeField
	private String attributeType;

	@NotNull
	@Size(min = 1, max = OpenStorefrontConstant.FIELD_SIZE_DESCRIPTION)
	@Sanitize(TextSanitizer.class)
	@ConsumeField
	private String description;

	@Size(min = 0, max = OpenStorefrontConstant.FIELD_SIZE_DESCRIPTION)
	@Sanitize(BasicHTMLSanitizer.class)
	@ConsumeField
	private String detailedDescription;

	@NotNull
	@ConsumeField
	@APIDescription("True to show in filters")
	private Boolean visibleFlg;

	@NotNull
	@ConsumeField
	@APIDescription("A component is required to have this attribute")
	private Boolean requiredFlg;

	@DataType(ComponentTypeRestriction.class)
	@ConsumeField
	@OneToMany(orphanRemoval = true)
	private List<ComponentTypeRestriction> requiredRestrictions;

	@DataType(ComponentTypeRestriction.class)
	@ConsumeField
	@APIDescription("The component/entry types for which this attribute is available")
	@OneToMany(orphanRemoval = true)
	private List<ComponentTypeRestriction> associatedComponentTypes;

	@NotNull
	@ConsumeField
	@APIDescription("This attribute type represents an architechture and has special handling")
	private Boolean architectureFlg;

	@NotNull
	@ConsumeField
	@APIDescription("This has special handling")
	private Boolean importantFlg;

	@NotNull
	@ConsumeField
	@APIDescription("Allow multiple value codes per component")
	private Boolean allowMultipleFlg;

	@NotNull
	@ConsumeField
	@APIDescription("Hides attribute on the submission form")
	@DefaultFieldValue("false")
	private Boolean hideOnSubmission;

	@NotNull
	@ConsumeField
	@APIDescription("Allow user-generated attribute codes")
	@DefaultFieldValue("false")
	private Boolean allowUserGeneratedCodes;

	@ConsumeField
	@APIDescription("Default attribute code")
	private String defaultAttributeCode;

	public static final String TYPE = "TYPE";
	public static final String DI2ELEVEL = "DI2ELEVEL";

	public AttributeType()
	{
	}

	@Override
	public void updateFields(StandardEntity entity)
	{
		super.updateFields(entity);

		AttributeType attributeTypeUpdate = (AttributeType) entity;
		this.setAllowMultipleFlg(attributeTypeUpdate.getAllowMultipleFlg());
		this.setArchitectureFlg(attributeTypeUpdate.getArchitectureFlg());
		this.setDescription(attributeTypeUpdate.getDescription());
		this.setImportantFlg(attributeTypeUpdate.getImportantFlg());
		this.setRequiredFlg(attributeTypeUpdate.getRequiredFlg());
		this.setVisibleFlg(attributeTypeUpdate.getVisibleFlg());
		this.setDetailedDescription(attributeTypeUpdate.getDetailedDescription());
		this.setHideOnSubmission(attributeTypeUpdate.getHideOnSubmission());
		this.setAllowUserGeneratedCodes(attributeTypeUpdate.getAllowUserGeneratedCodes());
		this.setDefaultAttributeCode(attributeTypeUpdate.getDefaultAttributeCode());
		this.setRequiredRestrictions(attributeTypeUpdate.getRequiredRestrictions());
		this.setAssociatedComponentTypes(attributeTypeUpdate.getAssociatedComponentTypes());

	}

	public ValidationResult customValidation()
	{
		ValidationResult validationResult = new ValidationResult();

		if (Convert.toBoolean(getHideOnSubmission())) {
			if (Convert.toBoolean(getRequiredFlg())) {
				if (StringUtils.isBlank(getDefaultAttributeCode())) {
					RuleResult result = new RuleResult();
					result.setFieldName("hideOnSubmission");
					result.setEntityClassName(getClass().getSimpleName());
					result.setValidationRule("Hide requires Default Code");
					result.setMessage("Default Code is requried when hide on submission and the attribute is required.");
					validationResult.getRuleResults().add(result);
				}
			}
		}
		return validationResult;
	}

	@Override
	public int hashCode()
	{
		int hash = 0;
		hash += (attributeType != null ? attributeType.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object)
	{
		if (!(object instanceof AttributeType)) {
			return false;
		}
		AttributeType other = (AttributeType) object;
		if (this.attributeType == null && other.attributeType == null) {
			return super.equals(object);
		}
		if ((this.attributeType == null && other.attributeType != null) || (this.attributeType != null && !this.attributeType.equals(other.attributeType))) {
			return false;
		}
		return true;
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

	public Boolean getVisibleFlg()
	{
		return visibleFlg;
	}

	public void setVisibleFlg(Boolean visibleFlg)
	{
		this.visibleFlg = visibleFlg;
	}

	public Boolean getRequiredFlg()
	{
		return requiredFlg;
	}

	public void setRequiredFlg(Boolean requiredFlg)
	{
		this.requiredFlg = requiredFlg;
	}

	public Boolean getArchitectureFlg()
	{
		return architectureFlg;
	}

	public void setArchitectureFlg(Boolean architectureFlg)
	{
		this.architectureFlg = architectureFlg;
	}

	public Boolean getImportantFlg()
	{
		return importantFlg;
	}

	public void setImportantFlg(Boolean importantFlg)
	{
		this.importantFlg = importantFlg;
	}

	public Boolean getAllowMultipleFlg()
	{
		return allowMultipleFlg;
	}

	public void setAllowMultipleFlg(Boolean allowMultipleFlg)
	{
		this.allowMultipleFlg = allowMultipleFlg;
	}

	public Boolean getHideOnSubmission()
	{
		return hideOnSubmission;
	}

	public void setHideOnSubmission(Boolean hideOnSubmission)
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

	public Boolean getAllowUserGeneratedCodes()
	{
		return allowUserGeneratedCodes;
	}

	public void setAllowUserGeneratedCodes(Boolean allowUserGeneratedCodes)
	{
		this.allowUserGeneratedCodes = allowUserGeneratedCodes;
	}

}
