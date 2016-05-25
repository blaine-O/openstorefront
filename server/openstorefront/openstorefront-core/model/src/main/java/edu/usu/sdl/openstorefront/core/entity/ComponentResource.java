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
package edu.usu.sdl.openstorefront.core.entity;

import edu.usu.sdl.openstorefront.common.manager.FileSystemManager;
import edu.usu.sdl.openstorefront.common.util.OpenStorefrontConstant;
import edu.usu.sdl.openstorefront.common.util.ReflectionUtil;
import edu.usu.sdl.openstorefront.core.annotation.APIDescription;
import edu.usu.sdl.openstorefront.core.annotation.ConsumeField;
import edu.usu.sdl.openstorefront.core.annotation.FK;
import edu.usu.sdl.openstorefront.core.annotation.PK;
import edu.usu.sdl.openstorefront.core.annotation.ValidValueType;
import edu.usu.sdl.openstorefront.validation.HTMLSanitizer;
import edu.usu.sdl.openstorefront.validation.LinkSanitizer;
import edu.usu.sdl.openstorefront.validation.Sanitize;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jlaw
 */
@APIDescription("Resource for a component")
public class ComponentResource
		extends BaseComponent
{

	@PK(generated = true)
	@NotNull
	private String resourceId;

	@Size(min = 0, max = OpenStorefrontConstant.FIELD_SIZE_GENERAL_TEXT)
	@APIDescription("For a local resource")
	private String fileName;

	@Size(min = 0, max = OpenStorefrontConstant.FIELD_SIZE_GENERAL_TEXT)
	@APIDescription("For a local resource")
	private String originalName;

	@Size(min = 0, max = OpenStorefrontConstant.FIELD_SIZE_GENERAL_TEXT)
	@APIDescription("For a local resource")
	private String mimeType;

	@NotNull
	@ConsumeField
	@Size(min = 1, max = OpenStorefrontConstant.FIELD_SIZE_CODE)
	@ValidValueType(value = {}, lookupClass = ResourceType.class)
	@FK(ResourceType.class)
	private String resourceType;

	@ConsumeField
	@Size(min = 0, max = OpenStorefrontConstant.FIELD_SIZE_URL)
	@Sanitize(LinkSanitizer.class)
	@APIDescription("For an external resource")
	private String link;

	@ConsumeField
	@Size(min = 0, max = OpenStorefrontConstant.FIELD_SIZE_16K)
	@Sanitize(HTMLSanitizer.class)
	private String description;

	@ConsumeField
	@APIDescription("This is used to indentify if a resource requires a login or CAC")
	private Boolean restricted;
	
	public ComponentResource()
	{
	}

	@Override
	public String uniqueKey()
	{
		return StringUtils.isNotBlank(getLink()) ? getLink() : getOriginalName();
	}

	@Override
	protected void customKeyClear()
	{
		setResourceId(null);
	}

	@Override
	public void updateFields(StandardEntity entity)
	{
		super.updateFields(entity);

		ComponentResource resource = (ComponentResource) entity;

		if (StringUtils.isNotBlank(resource.getLink())) {
			this.setFileName(null);
			this.setOriginalName(null);
			this.setMimeType(null);
		} else {
			this.setFileName(resource.getFileName());
			this.setOriginalName(resource.getOriginalName());
			this.setMimeType(resource.getMimeType());
		}
		this.setDescription(resource.getDescription());
		this.setLink(resource.getLink());
		this.setResourceType(resource.getResourceType());
		this.setRestricted(resource.getRestricted());

		
	}
	

	@Override
	public int customCompareTo(BaseComponent o)
	{
		ComponentResource componentResource = ((ComponentResource) o);
		int value = ReflectionUtil.compareObjects(getFileName(), componentResource.getFileName());
		if (value == 0) {
			value = ReflectionUtil.compareObjects(getMimeType(), componentResource.getMimeType());
		}
		return value;
	}

	/**
	 * Get the path to the resource on disk. Note: this may be ran from a proxy
	 * so don't use variable directly
	 *
	 * @return Resource or null if this doesn't represent a disk resource
	 */
	public Path pathToResource()
	{
		Path path = null;
		if (StringUtils.isNotBlank(getFileName())) {
			File resourceDir = FileSystemManager.getDir(FileSystemManager.RESOURCE_DIR);
			path = Paths.get(resourceDir.getPath() + "/" + getFileName());
		}
		return path;
	}

	public String getResourceId()
	{
		return resourceId;
	}

	public void setResourceId(String resourceId)
	{
		this.resourceId = resourceId;
	}

	public String getLink()
	{
		return link;
	}

	public void setLink(String link)
	{
		this.link = link;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getResourceType()
	{
		return resourceType;
	}

	public void setResourceType(String resourceType)
	{
		this.resourceType = resourceType;
	}

	public Boolean getRestricted()
	{
		return restricted;
	}

	public void setRestricted(Boolean restricted)
	{
		this.restricted = restricted;
	}

	public String getFileName()
	{
		return fileName;
	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	public String getOriginalName()
	{
		return originalName;
	}

	public void setOriginalName(String originalName)
	{
		this.originalName = originalName;
	}

	public String getMimeType()
	{
		return mimeType;
	}

	public void setMimeType(String mimeType)
	{
		this.mimeType = mimeType;
	}

}
