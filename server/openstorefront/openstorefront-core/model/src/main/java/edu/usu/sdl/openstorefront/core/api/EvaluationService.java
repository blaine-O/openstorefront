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
package edu.usu.sdl.openstorefront.core.api;

import edu.usu.sdl.openstorefront.core.entity.Evaluation;
import edu.usu.sdl.openstorefront.core.model.ChecklistAll;
import edu.usu.sdl.openstorefront.core.model.EvaluationAll;

/**
 *
 * @author dshurtleff
 */
public interface EvaluationService
		extends AsyncService
{

	/**
	 * This complete save of the checklist. It assume all responses and
	 * recommendation are included as it remove the existing and adds the new
	 * ones.
	 *
	 * @param checklistAll
	 * @return
	 */
	@ServiceInterceptor(TransactionInterceptor.class)
	public String saveCheckListAll(ChecklistAll checklistAll);

	/**
	 * Find a complete checklist by id
	 *
	 * @param checklistId
	 * @return null if not found
	 */
	public ChecklistAll getChecklistAll(String checklistId);

	/**
	 * Save the full evaluation
	 *
	 * @param evaluationAll
	 * @return evaluation Id
	 */
	@ServiceInterceptor(TransactionInterceptor.class)
	public String saveEvaluationAll(EvaluationAll evaluationAll);

	/**
	 * Get a complete evaluation model
	 *
	 * @param evaluationId
	 * @return null if not found
	 */
	public EvaluationAll getEvaluation(String evaluationId);

	/**
	 * Finds the latest evaluation based on create data
	 *
	 * @param componentId
	 * @return null if no evaluation
	 */
	public EvaluationAll getLatestEvaluation(String componentId);

	/**
	 * Create an evaluation base on the template
	 *
	 * @param evaluation
	 * @return
	 */
	public Evaluation createEvaluationFromTemplate(Evaluation evaluation);

	/**
	 * Make sure the evaluation component change request existing
	 * if it doesn't create one.
	 * 
	 * @param evaluation 
	 */
	public void checkEvaluationComponent(String evaluationId);
	
	/**
	 * Deletes an evaluation and all child data
	 *
	 * @param evaluationId
	 */
	@ServiceInterceptor(TransactionInterceptor.class)
	public void deleteEvaluation(String evaluationId);

	/**
	 * Publishes the evaluation to make is public Note: it still needs be active
	 * (which is a separate process)
	 *
	 * @param evaluationId
	 */
	@ServiceInterceptor(TransactionInterceptor.class)
	public void publishEvaluation(String evaluationId);

	/**
	 * Un-publish a evaluation to make it only visible to evaluators Note:
	 * active controls an addition layer of visibility
	 *
	 * @param evaluationId
	 */
	@ServiceInterceptor(TransactionInterceptor.class)
	public void unpublishEvaluation(String evaluationId);
	
	/**
	 * Creates a full copy of an evaluation with a different change request
	 * @param evaluationId
	 * @return copy's evaluation Id
	 */
	public String copyEvaluation(String evaluationId);	

}