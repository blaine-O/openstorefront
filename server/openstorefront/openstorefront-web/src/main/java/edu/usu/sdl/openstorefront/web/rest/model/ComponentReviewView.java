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
package edu.usu.sdl.openstorefront.web.rest.model;

import edu.usu.sdl.openstorefront.doc.DataType;
import edu.usu.sdl.openstorefront.service.ServiceProxy;
import edu.usu.sdl.openstorefront.storage.model.Component;
import edu.usu.sdl.openstorefront.storage.model.ComponentReview;
import edu.usu.sdl.openstorefront.storage.model.ComponentReviewCon;
import edu.usu.sdl.openstorefront.storage.model.ComponentReviewPro;
import edu.usu.sdl.openstorefront.storage.model.ExperienceTimeType;
import edu.usu.sdl.openstorefront.storage.model.UserTypeCode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author dshurtleff
 */
public class ComponentReviewView
{

	private String username;
    private String userType;
    private String comment;
    private int rating;
    private String title;
    private String usedTimeCode;
    private Date lastUsed;    
    private Date updateDate;
    private String organization;
    private boolean recommend;
	private String componentId;
	private String name;
	private Component component;

	
	@DataType(ComponentReviewPro.class)
	private List<ComponentReviewProCon> pros = new ArrayList<>();

	@DataType(ComponentReviewCon.class)
	private List<ComponentReviewProCon> cons = new ArrayList<>();

	public ComponentReviewView()
	{
	}
	
	public static ComponentReviewView toView(ComponentReview review)
	{
		ServiceProxy service = new ServiceProxy();
		ComponentReviewView view = new ComponentReviewView();
		view.setUsername(review.getCreateUser());
		UserTypeCode typeCode = service.getLookupService().getLookupEnity(UserTypeCode.class, review.getUserTypeCode());
		if (typeCode == null) {
			view.setUserType(null);
		} else {
			view.setUserType(typeCode.getDescription());
		}
		view.setComment(review.getComment());
		view.setRating(review.getRating());
		view.setTitle(review.getTitle());
		view.setComponentId(review.getComponentId());
		view.setName(service.getPersistenceService().findById(Component.class, review.getComponentId()).getName());
		ExperienceTimeType timeCode = service.getLookupService().getLookupEnity(ExperienceTimeType.class, review.getUserTimeCode());
		if (timeCode == null){
			view.setUsedTimeCode(null);
		} else {
			view.setUsedTimeCode(timeCode.getDescription());			
		}
		view.setLastUsed(review.getLastUsed());
		view.setUpdateDate(review.getUpdateDts());
		view.setOrganization(review.getOrganization());
		view.setRecommend(review.getRecommend());
		return view;
	}

	public List<ComponentReviewProCon> getPros()
	{
		return pros;
	}

	public void setPros(List<ComponentReviewProCon> pros)
	{
		this.pros = pros;
	}

	public List<ComponentReviewProCon> getCons()
	{
		return cons;
	}

	public void setCons(List<ComponentReviewProCon> cons)
	{
		this.cons = cons;
	}

	/**
	 * @return the username
	 */
	public String getUsername()
	{
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username)
	{
		this.username = username;
	}

	/**
	 * @return the userType
	 */
	public String getUserType()
	{
		return userType;
	}

	/**
	 * @param userType the userType to set
	 */
	public void setUserType(String userType)
	{
		this.userType = userType;
	}

	/**
	 * @return the comment
	 */
	public String getComment()
	{
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment)
	{
		this.comment = comment;
	}

	/**
	 * @return the rating
	 */
	public int getRating()
	{
		return rating;
	}

	/**
	 * @param rating the rating to set
	 */
	public void setRating(int rating)
	{
		this.rating = rating;
	}

	/**
	 * @return the title
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}

	/**
	 * @return the usedTimeCode
	 */
	public String getUsedTimeCode()
	{
		return usedTimeCode;
	}

	/**
	 * @param usedTimeCode the usedTimeCode to set
	 */
	public void setUsedTimeCode(String usedTimeCode)
	{
		this.usedTimeCode = usedTimeCode;
	}

	/**
	 * @return the lastUsed
	 */
	public Date getLastUsed()
	{
		return lastUsed;
	}

	/**
	 * @param lastUsed the lastUsed to set
	 */
	public void setLastUsed(Date lastUsed)
	{
		this.lastUsed = lastUsed;
	}

	/**
	 * @return the updateDate
	 */
	public Date getUpdateDate()
	{
		return updateDate;
	}

	/**
	 * @param updateDate the updateDate to set
	 */
	public void setUpdateDate(Date updateDate)
	{
		this.updateDate = updateDate;
	}

	/**
	 * @return the organization
	 */
	public String getOrganization()
	{
		return organization;
	}

	/**
	 * @param organization the organization to set
	 */
	public void setOrganization(String organization)
	{
		this.organization = organization;
	}

	/**
	 * @return the recommend
	 */
	public boolean isRecommend()
	{
		return recommend;
	}

	/**
	 * @param recommend the recommend to set
	 */
	public void setRecommend(boolean recommend)
	{
		this.recommend = recommend;
	}

	/**
	 * @return the component
	 */
	public Component getComponent()
	{
		return component;
	}

	/**
	 * @param component the component to set
	 */
	public void setComponent(Component component)
	{
		this.component = component;
	}

	/**
	 * @return the componentId
	 */
	public String getComponentId()
	{
		return componentId;
	}

	/**
	 * @param componentId the componentId to set
	 */
	public void setComponentId(String componentId)
	{
		this.componentId = componentId;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

}
