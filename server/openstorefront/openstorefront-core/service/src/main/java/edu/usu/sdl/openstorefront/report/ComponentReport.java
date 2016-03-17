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
package edu.usu.sdl.openstorefront.report;

import edu.usu.sdl.openstorefront.common.util.OpenStorefrontConstant;
import edu.usu.sdl.openstorefront.common.util.TimeUtil;
import edu.usu.sdl.openstorefront.core.api.query.QueryByExample;
import edu.usu.sdl.openstorefront.core.entity.ApprovalStatus;
import edu.usu.sdl.openstorefront.core.entity.Component;
import edu.usu.sdl.openstorefront.core.entity.ComponentQuestion;
import edu.usu.sdl.openstorefront.core.entity.ComponentQuestionResponse;
import edu.usu.sdl.openstorefront.core.entity.ComponentReview;
import edu.usu.sdl.openstorefront.core.entity.ComponentTag;
import edu.usu.sdl.openstorefront.core.entity.ComponentTracking;
import edu.usu.sdl.openstorefront.core.entity.Report;
import edu.usu.sdl.openstorefront.core.entity.TrackEventCode;
import edu.usu.sdl.openstorefront.report.generator.CSVGenerator;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author dshurtleff
 */
public class ComponentReport
		extends BaseReport
{

	private List<Component> components;

	public ComponentReport(Report report)
	{
		super(report);
	}

	@Override
	protected void gatherData()
	{
		Component componentExample = new Component();
		componentExample.setActiveStatus(Component.ACTIVE_STATUS);
		componentExample.setApprovalState(ApprovalStatus.APPROVED);
		components = service.getPersistenceService().queryByExample(Component.class, componentExample);
		if (!report.dataIdSet().isEmpty()) {
			components = components.stream().filter(c -> report.dataIdSet().contains(c.getComponentId())).collect(Collectors.toList());
		}
	}

	@Override
	protected void writeReport()
	{
		CSVGenerator cvsGenerator = (CSVGenerator) generator;

		//write header
		cvsGenerator.addLine("Component Report", sdf.format(TimeUtil.currentDate()));
		cvsGenerator.addLine(
				"Name",
				"Organization",
				//"Security Classification",
				"Last Activity Date",
				"Approval Status",
				"Approval Date",
				"Approval User",
				"Active Status",
				"Create Date",
				"Create User",
				"Last Viewed",
				"Views",
				"Resources Clicked",
				"Active Reviews",
				"Tags",
				"Active Questions",
				"Active Question Responses"
		);

		//write Body
		for (Component component : components) {

			ComponentTracking componentTrackingExample = new ComponentTracking();
			componentTrackingExample.setActiveStatus(ComponentTracking.ACTIVE_STATUS);
			componentTrackingExample.setComponentId(component.getComponentId());
			componentTrackingExample.setTrackEventTypeCode(TrackEventCode.VIEW);
			long views = service.getPersistenceService().countByExample(componentTrackingExample);

			componentTrackingExample.setTrackEventTypeCode(TrackEventCode.EXTERNAL_LINK_CLICK);
			long resourcesClicked = service.getPersistenceService().countByExample(componentTrackingExample);

			ComponentReview componentReviewExample = new ComponentReview();
			componentReviewExample.setComponentId(component.getComponentId());
			componentReviewExample.setActiveStatus(ComponentReview.ACTIVE_STATUS);
			long reviews = service.getPersistenceService().countByExample(componentReviewExample);

			ComponentTag componentTagExample = new ComponentTag();
			componentTagExample.setComponentId(component.getComponentId());
			componentTagExample.setActiveStatus(ComponentReview.ACTIVE_STATUS);
			long tags = service.getPersistenceService().countByExample(componentTagExample);

			ComponentQuestion componentQuestionExample = new ComponentQuestion();
			componentQuestionExample.setComponentId(component.getComponentId());
			componentQuestionExample.setActiveStatus(ComponentReview.ACTIVE_STATUS);
			long questions = service.getPersistenceService().countByExample(componentQuestionExample);

			ComponentQuestionResponse componentQuestionResponseExample = new ComponentQuestionResponse();
			componentQuestionResponseExample.setComponentId(component.getComponentId());
			componentQuestionResponseExample.setActiveStatus(ComponentReview.ACTIVE_STATUS);
			long questionResponse = service.getPersistenceService().countByExample(componentQuestionResponseExample);

			componentTrackingExample = new ComponentTracking();
			componentTrackingExample.setActiveStatus(ComponentTracking.ACTIVE_STATUS);
			componentTrackingExample.setComponentId(component.getComponentId());
			componentTrackingExample.setTrackEventTypeCode(TrackEventCode.VIEW);

			ComponentTracking componentTrackingOrderExample = new ComponentTracking();
			componentTrackingOrderExample.setEventDts(QueryByExample.DATE_FLAG);

			QueryByExample queryByExample = new QueryByExample(componentTrackingExample);
			queryByExample.setMaxResults(1);
			queryByExample.setOrderBy(componentTrackingOrderExample);
			queryByExample.setSortDirection(OpenStorefrontConstant.SORT_ASCENDING);

			ComponentTracking componentTracking = service.getPersistenceService().queryOneByExample(ComponentTracking.class, queryByExample);
			String lastViewed = "";
			if (componentTracking != null) {
				lastViewed = sdf.format(componentTracking.getEventDts());
			}

			cvsGenerator.addLine(
					component.getName(),
					component.getOrganization(),
					//component.getSecurityMarkingType() == null ? "" : "(" + component.getSecurityMarkingType() + ") - " + TranslateUtil.translate(SecurityMarkingType.class, component.getSecurityMarkingType()),
					sdf.format(component.getLastActivityDts()),
					component.getApprovalState(),
					component.getApprovedDts() == null ? "" : sdf.format(component.getApprovedDts()),
					component.getApprovedUser(),
					component.getActiveStatus(),
					sdf.format(component.getCreateDts()),
					component.getCreateUser(),
					lastViewed,
					views,
					resourcesClicked,
					reviews,
					tags,
					questions,
					questionResponse
			);

		}

	}

}
