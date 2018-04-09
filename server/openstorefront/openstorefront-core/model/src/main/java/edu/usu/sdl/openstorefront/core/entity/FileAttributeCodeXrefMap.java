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

import edu.usu.sdl.openstorefront.common.util.OpenStorefrontConstant;
import edu.usu.sdl.openstorefront.core.annotation.APIDescription;
import edu.usu.sdl.openstorefront.core.annotation.ConsumeField;
import edu.usu.sdl.openstorefront.core.annotation.FK;
import java.io.Serializable;
import javax.persistence.Embeddable;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author dshurtleff
 */
@APIDescription("Used to store code mapping between external attributes and internal")
@Embeddable
public class FileAttributeCodeXrefMap
		implements Serializable
{

	@NotNull
	@ConsumeField
	@Size(min = 1, max = OpenStorefrontConstant.FIELD_SIZE_CODE)
	@FK(AttributeCode.class)
	private String attributeCode;

	@NotNull
	@ConsumeField
	@Size(min = 1, max = OpenStorefrontConstant.FIELD_SIZE_GENERAL_TEXT)
	private String externalCode;

	@Version
	private String storageVersion;

	@SuppressWarnings({"squid:S2637", "squid:S1186"})
	public FileAttributeCodeXrefMap()
	{
	}

	public String getAttributeCode()
	{
		return attributeCode;
	}

	public void setAttributeCode(String attributeCode)
	{
		this.attributeCode = attributeCode;
	}

	public String getExternalCode()
	{
		return externalCode;
	}

	public void setExternalCode(String externalCode)
	{
		this.externalCode = externalCode;
	}

	public String getStorageVersion()
	{
		return storageVersion;
	}

	public void setStorageVersion(String storageVersion)
	{
		this.storageVersion = storageVersion;
	}

}
