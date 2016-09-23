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
package edu.usu.sdl.openstorefront.service.manager;

import edu.usu.sdl.openstorefront.common.manager.Initializable;
import edu.usu.sdl.openstorefront.common.util.OpenStorefrontConstant;
import edu.usu.sdl.openstorefront.core.api.model.TaskFuture;
import edu.usu.sdl.openstorefront.core.api.model.TaskRequest;
import edu.usu.sdl.openstorefront.core.entity.Report;
import edu.usu.sdl.openstorefront.core.entity.RunStatus;
import edu.usu.sdl.openstorefront.security.SecurityUtil;
import edu.usu.sdl.openstorefront.service.ServiceProxy;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;

/**
 * Handles Background Reports
 *
 * @author dshurtleff
 */
public class ReportManager
		implements Initializable
{

	private static final Logger log = Logger.getLogger(ReportManager.class.getName());
	private static AtomicBoolean started = new AtomicBoolean(false);

	public static void init()
	{
		ServiceProxy serviceProxy = ServiceProxy.getProxy();
		//Restart any pending or working reports
		List<Report> allReports = getInprogessReports();

		if (!allReports.isEmpty()) {
			log.log(Level.INFO, MessageFormat.format("Resuming pending and working reports. (Running in the Background)  Reports to run:  {0}", allReports.size()));
			for (Report report : allReports) {
				TaskRequest taskRequest = new TaskRequest();
				taskRequest.setAllowMultiple(true);
				taskRequest.setName(TaskRequest.TASKNAME_REPORT);
				taskRequest.setDetails("Report: " + report.getReportType() + " Report id: " + report.getReportId() + " for user: " + SecurityUtil.getCurrentUserName());
				taskRequest.getTaskData().put(TaskRequest.DATAKEY_REPORT_ID, report.getReportId());
				serviceProxy.getAsyncProxy(serviceProxy.getReportService(), taskRequest).generateReport(report);
			}
		}

	}

	private static List<Report> getInprogessReports()
	{
		ServiceProxy serviceProxy = ServiceProxy.getProxy();

		List<Report> allReports = new ArrayList<>();
		Report reportExample = new Report();
		reportExample.setActiveStatus(Report.ACTIVE_STATUS);
		reportExample.setRunStatus(RunStatus.PENDING);

		List<Report> reports = serviceProxy.getPersistenceService().queryByExample(Report.class, reportExample);
		allReports.addAll(reports);

		reportExample.setRunStatus(RunStatus.WORKING);
		reports = serviceProxy.getPersistenceService().queryByExample(Report.class, reportExample);
		allReports.addAll(reports);

		return allReports;
	}

	public static void cleanup()
	{
		ServiceProxy serviceProxy = ServiceProxy.getProxy();
		List<Report> allReports = getInprogessReports();
		if (!allReports.isEmpty()) {
			log.log(Level.WARNING, MessageFormat.format("Reports are currently in progress.  Attempting to cancel and put back on queue.   Reports in progress:  {0}", allReports.size()));

			List<TaskFuture> taskFutures = AsyncTaskManager.getTasksByName(TaskRequest.TASKNAME_REPORT);
			for (TaskFuture taskFuture : taskFutures) {
				String reportId = (String) taskFuture.getTaskData().get(TaskRequest.DATAKEY_REPORT_ID);
				if (StringUtils.isNotBlank(reportId)) {
					if (taskFuture.cancel(true)) {
						Report report = serviceProxy.getPersistenceService().findById(Report.class, reportId);
						report.setRunStatus(RunStatus.PENDING);
						report.setUpdateUser(OpenStorefrontConstant.SYSTEM_USER);
						report.populateBaseUpdateFields();
						serviceProxy.getPersistenceService().persist(report);
					} else {
						log.log(Level.WARNING, MessageFormat.format("Unable to cancel report id: {0} it likely be in a fail state upon restart.  It can be safely deleted.", reportId));
					}
				} else {
					log.log(Level.WARNING, "Unable to find report id for a report task.  Unable to cleanly cancel.  Report can be clean up upon restart.");
				}
			}
		}
	}

	@Override
	public void initialize()
	{
		ReportManager.init();
		started.set(true);
	}

	@Override
	public void shutdown()
	{
		ReportManager.cleanup();
		started.set(false);
	}
	
	@Override
	public boolean isStarted()
	{
		return started.get();
	}	

}
