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

import edu.usu.sdl.openstorefront.common.util.OpenStorefrontConstant;
import edu.usu.sdl.openstorefront.core.annotation.APIDescription;
import edu.usu.sdl.openstorefront.core.annotation.ConsumeField;
import edu.usu.sdl.openstorefront.core.annotation.PK;
import edu.usu.sdl.openstorefront.core.annotation.Unique;
import edu.usu.sdl.openstorefront.core.api.ServiceProxyFactory;
import edu.usu.sdl.openstorefront.core.model.FieldChangeModel;
import edu.usu.sdl.openstorefront.validation.ComponentTagUniqueHandler;
import edu.usu.sdl.openstorefront.validation.Sanitize;
import edu.usu.sdl.openstorefront.validation.TextSanitizer;
import java.util.List;
import java.util.Set;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author dshurtleff
 */
@APIDescription("Represents a tag")
public class ComponentTag
		extends BaseComponent<ComponentTag>
		implements LoggableModel<ComponentTag>
{

	public static final String FIELD_TEXT = "text";

	@PK(generated = true)
	@NotNull
	private String tagId;

	@NotNull
	@ConsumeField
	@Size(min = 1, max = OpenStorefrontConstant.FIELD_SIZE_TAG)
	@Sanitize(TextSanitizer.class)
	private String text;

	@SuppressWarnings({"squid:S2637", "squid:S1186"})
	public ComponentTag()
	{
	}

	@Override
	public String uniqueKey()
	{
		return getText();
	}

	@Override
	protected void customKeyClear()
	{
		setTagId(null);
	}

	@Override
	public void updateFields(StandardEntity entity)
	{
		ComponentTag tag = (ComponentTag) entity;
		ServiceProxyFactory.getServiceProxy().getChangeLogService().findUpdateChanges(this, tag);
		super.updateFields(entity);

		this.setText(tag.getText());

	}

	@Override
	public List<FieldChangeModel> findChanges(ComponentTag updated)
	{
		Set<String> excludeFields = excludedChangeFields();
		excludeFields.add("tagId");
		List<FieldChangeModel> changes = FieldChangeModel.allChangedFields(excludeFields, this, updated);
		return changes;
	}

	@Override
	public String addRemoveComment()
	{
		return getText();
	}

	public String getTagId()
	{
		return tagId;
	}

	public void setTagId(String tagId)
	{
		this.tagId = tagId;
	}

	public String getText()
	{
		return text;
	}

	public void setText(String tagText)
	{
		this.text = tagText;
	}

}
