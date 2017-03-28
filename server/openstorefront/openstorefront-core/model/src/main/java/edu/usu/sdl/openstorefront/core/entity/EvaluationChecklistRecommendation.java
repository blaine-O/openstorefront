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
import edu.usu.sdl.openstorefront.core.annotation.PK;
import edu.usu.sdl.openstorefront.core.annotation.ValidValueType;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author dshurtleff
 */
@APIDescription("Evalution recommendation")
public class EvaluationChecklistRecommendation
		extends StandardEntity<EvaluationChecklistRecommendation>
		implements LoggableModel<EvaluationChecklistRecommendation>
{

	@PK(generated = true)
	@NotNull
	private String recommendationId;

	@NotNull
	@FK(EvaluationChecklist.class)
	private String checklistId;

	@ConsumeField
	@NotNull
	@Size(min = 1, max = OpenStorefrontConstant.FIELD_SIZE_32K)
	private String recommendation;

	@ConsumeField
	@Size(min = 0, max = OpenStorefrontConstant.FIELD_SIZE_32K)
	private String reason;

	@NotNull
	@ConsumeField
	@ValidValueType(value = {}, lookupClass = RecommendationType.class)
	@FK(RecommendationType.class)
	private String recommendationType;

	public EvaluationChecklistRecommendation()
	{
	}

	@Override
	public <T extends StandardEntity> void updateFields(T entity)
	{
		super.updateFields(entity);

		EvaluationChecklistRecommendation evaluationChecklistRecommendation = (EvaluationChecklistRecommendation) entity;
		setReason(evaluationChecklistRecommendation.getReason());
		setRecommendation(evaluationChecklistRecommendation.getRecommendation());
		setRecommendationType(evaluationChecklistRecommendation.getRecommendationType());

	}
	
	@Override
	public List<ChangeLog> findChanges(EvaluationChecklistRecommendation updated)
	{
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}	

	public String getRecommendationId()
	{
		return recommendationId;
	}

	public void setRecommendationId(String recommendationId)
	{
		this.recommendationId = recommendationId;
	}

	public String getChecklistId()
	{
		return checklistId;
	}

	public void setChecklistId(String checklistId)
	{
		this.checklistId = checklistId;
	}

	public String getRecommendation()
	{
		return recommendation;
	}

	public void setRecommendation(String recommendation)
	{
		this.recommendation = recommendation;
	}

	public String getReason()
	{
		return reason;
	}

	public void setReason(String reason)
	{
		this.reason = reason;
	}

	public String getRecommendationType()
	{
		return recommendationType;
	}

	public void setRecommendationType(String recommendationType)
	{
		this.recommendationType = recommendationType;
	}

}
