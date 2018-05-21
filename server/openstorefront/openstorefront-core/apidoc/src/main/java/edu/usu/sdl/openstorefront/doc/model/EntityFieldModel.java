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
package edu.usu.sdl.openstorefront.doc.model;

import edu.usu.sdl.openstorefront.core.annotation.DataType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author dshurtleff
 */
public class EntityFieldModel
{

	private String name;
	private String description;
	private String type;
	private String genericType;
	private String originClass;
	private boolean embeddedType;
	private boolean primaryKey;

	@DataType(EntityConstraintModel.class)
	private List<EntityConstraintModel> constraints = new ArrayList<>();

	public EntityFieldModel()
	{
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public boolean getPrimaryKey()
	{
		return primaryKey;
	}

	public void setPrimaryKey(boolean primaryKey)
	{
		this.primaryKey = primaryKey;
	}

	public List<EntityConstraintModel> getConstraints()
	{
		return constraints;
	}

	public void setConstraints(List<EntityConstraintModel> constraints)
	{
		this.constraints = constraints;
	}

	public String getOriginClass()
	{
		return originClass;
	}

	public void setOriginClass(String originClass)
	{
		this.originClass = originClass;
	}

	public boolean getEmbeddedType()
	{
		return embeddedType;
	}

	public void setEmbeddedType(boolean embeddedType)
	{
		this.embeddedType = embeddedType;
	}

	public String getGenericType()
	{
		return genericType;
	}

	public void setGenericType(String genericType)
	{
		this.genericType = genericType;
	}

}
