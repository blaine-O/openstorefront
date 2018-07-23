/*
 * Copyright 2018 Space Dynamics Laboratory - Utah State University Research Foundation.
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
package edu.usu.sdl.openstorefront.service;

import edu.usu.sdl.openstorefront.core.api.WorkPlanService;
import edu.usu.sdl.openstorefront.core.entity.WorkPlan;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author cyearsley
 */
public class WorkPlanServiceImpl
		extends ServiceProxy
		implements WorkPlanService
{
	@Override
	public List<WorkPlan> getWorkPlans()
	{
		// TODO: query ALL workplans (use a view)
		return Arrays.asList();
	}
	
	@Override
	public WorkPlan getWorkPlan(String id)
	{
		// TODO: get single workplan (use a view)
		return new WorkPlan();
	}
	
	@Override
	public WorkPlan createWorkPlan(WorkPlan workPlan)
	{
		// TODO: create workplan
		return new WorkPlan();
	}
	
	@Override
	public WorkPlan updateWorkPlan(String workPlanId, WorkPlan newWorkPlan)
	{
		// TODO: update workplan
		return new WorkPlan();
	}
	
	@Override
	public void deleteWorkPlan(String workPlanId)
	{
		// TODO: delete said workplan
	}
}
