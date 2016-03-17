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
import edu.usu.sdl.openstorefront.core.annotation.ConsumeField;
import edu.usu.sdl.openstorefront.core.annotation.FK;
import edu.usu.sdl.openstorefront.core.annotation.PK;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author dshurtleff
 */
public class UserSavedSearch
		extends StandardEntity
{
	
	@PK(generated = true)
	@NotNull
	private String userSearchId;

	@NotNull
	@Size(min = 1, max = OpenStorefrontConstant.FIELD_SIZE_USERNAME)	
	@ConsumeField
	private String searchName;
	
	@NotNull
	@Size(min = 1, max = OpenStorefrontConstant.FIELD_SIZE_1MB)	
	@ConsumeField
	private String searchRequest;
	
	@NotNull
	@Size(min = 1, max = OpenStorefrontConstant.FIELD_SIZE_USERNAME)
	@ConsumeField
	@FK(UserProfile.class)
	private String username;	
	
	public UserSavedSearch()
	{
	}

	@Override
	public void updateFields(StandardEntity entity)
	{
		super.updateFields(entity);
		
		UserSavedSearch userSaveSearch = (UserSavedSearch) entity;
		this.setSearchName(userSaveSearch.getSearchName());
		this.setSearchRequest(userSaveSearch.getSearchRequest());
		this.setUsername(userSaveSearch.getUsername());
		
	}

	public String getUserSearchId()
	{
		return userSearchId;
	}

	public void setUserSearchId(String userSearchId)
	{
		this.userSearchId = userSearchId;
	}

	public String getSearchName()
	{
		return searchName;
	}

	public void setSearchName(String searchName)
	{
		this.searchName = searchName;
	}

	public String getSearchRequest()
	{
		return searchRequest;
	}

	public void setSearchRequest(String searchRequest)
	{
		this.searchRequest = searchRequest;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}
	
	
	
	
}
