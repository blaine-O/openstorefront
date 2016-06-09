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
package edu.usu.sdl.openstorefront.service;

import edu.usu.sdl.openstorefront.common.exception.OpenStorefrontRuntimeException;
import edu.usu.sdl.openstorefront.common.manager.FileSystemManager;
import edu.usu.sdl.openstorefront.common.manager.PropertiesManager;
import edu.usu.sdl.openstorefront.common.util.Convert;
import edu.usu.sdl.openstorefront.common.util.OpenStorefrontConstant;
import edu.usu.sdl.openstorefront.common.util.StringProcessor;
import edu.usu.sdl.openstorefront.common.util.TimeUtil;
import edu.usu.sdl.openstorefront.core.api.SystemService;
import edu.usu.sdl.openstorefront.core.api.model.TaskFuture;
import edu.usu.sdl.openstorefront.core.entity.AlertType;
import edu.usu.sdl.openstorefront.core.entity.ApplicationProperty;
import edu.usu.sdl.openstorefront.core.entity.AsyncTask;
import edu.usu.sdl.openstorefront.core.entity.ComponentIntegration;
import edu.usu.sdl.openstorefront.core.entity.DBLogRecord;
import edu.usu.sdl.openstorefront.core.entity.ErrorTicket;
import edu.usu.sdl.openstorefront.core.entity.GeneralMedia;
import edu.usu.sdl.openstorefront.core.entity.HelpSection;
import edu.usu.sdl.openstorefront.core.entity.Highlight;
import edu.usu.sdl.openstorefront.core.model.AlertContext;
import edu.usu.sdl.openstorefront.core.model.ErrorInfo;
import edu.usu.sdl.openstorefront.core.model.HelpSectionAll;
import edu.usu.sdl.openstorefront.core.view.GlobalIntegrationModel;
import edu.usu.sdl.openstorefront.core.view.SystemErrorModel;
import edu.usu.sdl.openstorefront.security.SecurityUtil;
import edu.usu.sdl.openstorefront.service.manager.DBLogManager;
import edu.usu.sdl.openstorefront.service.manager.JobManager;
import edu.usu.sdl.openstorefront.validation.ValidationModel;
import edu.usu.sdl.openstorefront.validation.ValidationResult;
import edu.usu.sdl.openstorefront.validation.ValidationUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * Handles System related entities
 *
 * @author dshurtleff
 */
@SuppressWarnings("ClassWithMultipleLoggers")
public class SystemServiceImpl
		extends ServiceProxy
		implements SystemService
{

	private static final Logger log = Logger.getLogger(SystemServiceImpl.class.getName());
	private static final Logger errorLog = Logger.getLogger(OpenStorefrontConstant.ERROR_LOGGER);

	private static final int MAX_DB_CLEAN_AMOUNT = 1000;
	private static final int MIN_DB_CLEAN_AMOUNT = 1000;

	@Override
	public ApplicationProperty getProperty(String key)
	{
		ApplicationProperty applicationProperty = persistenceService.findById(ApplicationProperty.class, key);
		return applicationProperty;
	}

	@Override
	public String getPropertyValue(String key)
	{
		ApplicationProperty property = getProperty(key);
		if (property != null) {
			return property.getValue();
		}
		return null;
	}

	@Override
	public void saveProperty(String key, String value)
	{
		if (StringUtils.isBlank(value)) {
			//remove existing
			ApplicationProperty existingProperty = persistenceService.findById(ApplicationProperty.class, key);
			if (existingProperty != null) {
				persistenceService.delete(existingProperty);
			}
		} else {
			ApplicationProperty existingProperty = persistenceService.findById(ApplicationProperty.class, key);
			if (existingProperty != null) {
				existingProperty.setValue(value);
				existingProperty.setUpdateDts(TimeUtil.currentDate());
				existingProperty.setUpdateUser(OpenStorefrontConstant.SYSTEM_USER);
				persistenceService.persist(existingProperty);
			} else {
				ApplicationProperty property = new ApplicationProperty();
				property.setKey(key);
				property.setValue(value);
				property.setActiveStatus(ApplicationProperty.ACTIVE_STATUS);
				property.setCreateDts(TimeUtil.currentDate());
				property.setUpdateDts(TimeUtil.currentDate());
				property.setCreateUser(OpenStorefrontConstant.SYSTEM_USER);
				property.setUpdateUser(OpenStorefrontConstant.SYSTEM_USER);
				persistenceService.persist(property);
			}
		}
	}

	@Override
	public void saveHightlight(List<Highlight> highlights)
	{
		for (Highlight hightlight : highlights) {
			saveHightlight(hightlight);
		}
	}

	@Override
	public void saveHightlight(Highlight highlight)
	{
		Highlight existing = null;
		if (StringUtils.isNotBlank(highlight.getHighlightId())) {
			existing = persistenceService.findById(Highlight.class, highlight.getHighlightId());
		}
		if (existing != null) {
			existing.updateFields(highlight);
			persistenceService.persist(existing);
		} else {
			highlight.setHighlightId(persistenceService.generateId());
			highlight.populateBaseCreateFields();
			persistenceService.persist(highlight);
		}
	}

	@Override
	public void removeHighlight(String hightlightId)
	{
		Highlight highlight = persistenceService.findById(Highlight.class, hightlightId);
		if (highlight != null) {
			highlight.setActiveStatus(Highlight.INACTIVE_STATUS);
			highlight.setUpdateUser(SecurityUtil.getCurrentUserName());
			highlight.setUpdateDts(TimeUtil.currentDate());
			persistenceService.persist(highlight);
		}
	}

	@Override
	public void deleteHighlight(String hightlightId)
	{
		Highlight highlight = persistenceService.findById(Highlight.class, hightlightId);
		if (highlight != null) {
			persistenceService.delete(highlight);
		}
	}

	@Override
	public void activateHighlight(String hightlightId)
	{
		Highlight highlight = persistenceService.findById(Highlight.class, hightlightId);
		if (highlight != null) {
			highlight.setActiveStatus(Highlight.ACTIVE_STATUS);
			highlight.setUpdateUser(SecurityUtil.getCurrentUserName());
			highlight.setUpdateDts(TimeUtil.currentDate());
			persistenceService.persist(highlight);
		}
	}

	@Override
	public void syncHighlights(List<Highlight> highlights)
	{
		int removeCount = persistenceService.deleteByExample(new Highlight());
		log.log(Level.FINE, MessageFormat.format("Old Highlights removed: {0}", removeCount));

		for (Highlight highlight : highlights) {
			try {
				ValidationModel validationModel = new ValidationModel(highlight);
				validationModel.setConsumeFieldsOnly(true);
				ValidationResult validationResult = ValidationUtil.validate(validationModel);
				if (validationResult.valid()) {
					highlight.setCreateUser(OpenStorefrontConstant.SYSTEM_ADMIN_USER);
					highlight.setUpdateUser(OpenStorefrontConstant.SYSTEM_ADMIN_USER);
					getSystemService().saveHightlight(highlight);
				}

			} catch (Exception e) {
				log.log(Level.SEVERE, "Unable to save highlight.  Title: " + highlight.getTitle(), e);
			}
		}
	}

	@Override
	public SystemErrorModel generateErrorTicket(ErrorInfo errorInfo)
	{
		Objects.requireNonNull(errorInfo);

		errorLog.log(Level.SEVERE, "System Error Occured", errorInfo.getError());

		SystemErrorModel systemErrorModel = new SystemErrorModel();
		systemErrorModel.setMessage(errorInfo.getError().getMessage());
		if (errorInfo.getError() instanceof OpenStorefrontRuntimeException) {
			OpenStorefrontRuntimeException openStorefrontRuntimeException = (OpenStorefrontRuntimeException) errorInfo.getError();
			systemErrorModel.setPotentialResolution(openStorefrontRuntimeException.getPotentialResolution());
			errorInfo.setErrorTypeCode(openStorefrontRuntimeException.getErrorTypeCode());
		}
		try {

			String ticketNumber = persistenceService.generateId();
			StringBuilder ticket = new StringBuilder();
			ticket.append("TicketNumber: ").append(ticketNumber).append("\n");
			ticket.append("Client IP: ").append(errorInfo.getClientIp()).append("\n");
			ticket.append("User: ").append(SecurityUtil.getCurrentUserName()).append("\n");
			ticket.append("Message: ").append(systemErrorModel.getMessage()).append("\n");
			ticket.append("Potential Resolution: ").append(StringProcessor.blankIfNull(systemErrorModel.getPotentialResolution())).append("\n");
			ticket.append("Request: ").append(errorInfo.getRequestUrl()).append("\n");
			ticket.append("Request Method: ").append(errorInfo.getRequestMethod()).append("\n");
			ticket.append("Input Data: \n").append(errorInfo.getInputData()).append("\n\n");
			ticket.append("StackTrace: ").append("\n\n");

			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			errorInfo.getError().printStackTrace(printWriter);
			ticket.append(stringWriter.toString());

			systemErrorModel.setErrorTicketNumber(ticketNumber);
			ErrorTicket errorTicket = new ErrorTicket();
			errorTicket.setErrorTicketId(ticketNumber);
			errorTicket.setTicketFile(ticketNumber);
			errorTicket.setClientIp(errorInfo.getClientIp());
			errorTicket.setMessage(systemErrorModel.getMessage());
			errorTicket.setPotentialResolution(systemErrorModel.getPotentialResolution());
			if (StringUtils.isNotBlank(errorInfo.getRequestUrl())) {
				errorTicket.setCalledAction(errorInfo.getRequestUrl() + " Method: " + errorInfo.getRequestMethod());
			}
			errorTicket.setErrorTypeCode(errorInfo.getErrorTypeCode());
			errorTicket.setInput(errorInfo.getInputData());
			errorTicket.setActiveStatus(ErrorTicket.ACTIVE_STATUS);
			errorTicket.setCreateDts(TimeUtil.currentDate());
			errorTicket.setUpdateDts(TimeUtil.currentDate());
			errorTicket.setCreateUser(SecurityUtil.getCurrentUserName());
			errorTicket.setUpdateUser(SecurityUtil.getCurrentUserName());
			persistenceService.persist(errorTicket);

			//save file
			Path path = Paths.get(FileSystemManager.getDir(FileSystemManager.ERROR_TICKET_DIR).getPath() + "/" + errorTicket.getTicketFile());
			Files.write(path, ticket.toString().getBytes(Charset.defaultCharset()));

			AlertContext alertContext = new AlertContext();
			alertContext.setAlertType(AlertType.SYSTEM_ERROR);
			alertContext.setDataTrigger(errorTicket);
			getAlertService().checkAlert(alertContext);

		} catch (Throwable t) {
			//NOTE: this is a critial path.  if an error is thrown and not catch it would result in a info link or potential loop.
			//So that's why there is a catch all here.
			log.log(Level.SEVERE, "Error was thrown while processing the error", t);
		}
		return systemErrorModel;
	}

	@Override
	public String errorTicketInfo(String errorTicketId)
	{
		String ticketData = null;
		ErrorTicket errorTicket = persistenceService.findById(ErrorTicket.class, errorTicketId);
		if (errorTicket != null) {
			Path path = Paths.get(FileSystemManager.getDir(FileSystemManager.ERROR_TICKET_DIR).getPath() + "/" + errorTicket.getTicketFile());
			try {
				byte data[] = Files.readAllBytes(path);
				ticketData = new String(data);
			} catch (IOException io) {
				//We don't want to throw an error here if there something going on with the system.
				ticketData = "Unable to retrieve ticket information.  (Check log for more details) Message: " + io.getMessage();
				log.log(Level.WARNING, ticketData, io);
			}
		}
		return ticketData;
	}
	
	@Override
	public void deleteErrorTickets(List<String> ticketIds)
	{
		List<ErrorTicket> errorTickets = new ArrayList<>();
		for (String id : ticketIds) {
			ErrorTicket errorTicket = persistenceService.findById(ErrorTicket.class, id);			
			errorTickets.add(errorTicket);
		}
		performDelete(errorTickets);
	}	

	@Override
	public void cleanupOldErrors()
	{
		long count = persistenceService.countClass(ErrorTicket.class);
		long max = Long.parseLong(PropertiesManager.getValue(PropertiesManager.KEY_MAX_ERROR_TICKETS, OpenStorefrontConstant.ERRORS_MAX_COUNT_DEFAULT));

		if (count > max) {

			//query ticket
			long limit = count - max;
			String query = "SELECT FROM ErrorTicket ORDER BY updateDts ASC LIMIT " + limit;
			List<ErrorTicket> errorTickets = persistenceService.query(query, null);
			performDelete(errorTickets);
		}
	}
	
	private void performDelete(List<ErrorTicket> errorTickets) {
		errorTickets.stream().forEach((errorTicket) -> {
			Path path = Paths.get(FileSystemManager.getDir(FileSystemManager.ERROR_TICKET_DIR).getPath() + "/" + errorTicket.getTicketFile());
			if (path.toFile().exists()) {
				path.toFile().delete();
			}
			persistenceService.delete(errorTicket);
		});
	}

	@Override
	public GlobalIntegrationModel getGlobalIntegrationConfig()
	{
		GlobalIntegrationModel globalIntegrationModel = new GlobalIntegrationModel();

		String refreshTime = getPropertyValue(ApplicationProperty.GLOBAL_INTEGRATION_REFRESH);
		if (refreshTime == null) {
			refreshTime = GlobalIntegrationModel.DEFAULT_REFRESH_RATE;
		}
		globalIntegrationModel.setJiraRefreshRate(refreshTime);

		return globalIntegrationModel;
	}

	@Override
	public void saveGlobalIntegrationConfig(GlobalIntegrationModel globalIntegrationModel)
	{
		saveProperty(ApplicationProperty.GLOBAL_INTEGRATION_REFRESH, globalIntegrationModel.getJiraRefreshRate());

		List<ComponentIntegration> integrations = getComponentService().getComponentIntegrationModels(ComponentIntegration.ACTIVE_STATUS);
		for (ComponentIntegration integration : integrations) {
			if (StringUtils.isBlank(integration.getRefreshRate())) {
				JobManager.updateComponentIntegrationJob(integration);
			}
		}
	}

	@Override
	public void saveGeneralMedia(GeneralMedia generalMedia, InputStream fileInput)
	{
		Objects.requireNonNull(generalMedia);
		Objects.requireNonNull(fileInput);
		Objects.requireNonNull(generalMedia.getName(), "Name must be set.");

		generalMedia.setFileName(generalMedia.getName());
		try (InputStream in = fileInput) {
			Files.copy(in, generalMedia.pathToMedia(), StandardCopyOption.REPLACE_EXISTING);
			generalMedia.populateBaseCreateFields();
			persistenceService.persist(generalMedia);
		} catch (IOException ex) {
			throw new OpenStorefrontRuntimeException("Unable to store media file.", "Contact System Admin.  Check file permissions and disk space ", ex);
		}
	}

	@Override
	public void removeGeneralMedia(String mediaName)
	{
		GeneralMedia generalMedia = persistenceService.findById(GeneralMedia.class, mediaName);
		if (generalMedia != null) {
			Path path = generalMedia.pathToMedia();
			if (path != null) {
				if (path.toFile().exists()) {
					path.toFile().delete();
				}
			}
			persistenceService.delete(generalMedia);
		}
	}

	@Override
	public void saveAsyncTask(TaskFuture taskFuture)
	{
		AsyncTask existingTask = persistenceService.findById(AsyncTask.class, taskFuture.getTaskId());
		if (existingTask != null) {
			persistenceService.delete(existingTask);
		}

		AsyncTask asyncTask = new AsyncTask();
		asyncTask.setTaskId(taskFuture.getTaskId());
		asyncTask.setAllowMultiple(taskFuture.isAllowMultiple());
		asyncTask.setCompletedDts(taskFuture.getCompletedDts());
		asyncTask.setError(taskFuture.getError());
		asyncTask.setStatus(taskFuture.getStatus());
		asyncTask.setSubmitedDts(taskFuture.getSubmitedDts());
		asyncTask.setTaskName(taskFuture.getTaskName());
		asyncTask.setDetails(taskFuture.getDetails());

		asyncTask.setCreateUser(taskFuture.getCreateUser());
		asyncTask.setUpdateUser(taskFuture.getCreateUser());
		asyncTask.populateBaseCreateFields();

		persistenceService.persist(asyncTask);

	}

	@Override
	public void removeAsyncTask(String taskId)
	{
		AsyncTask task = persistenceService.findById(AsyncTask.class, taskId);
		if (task != null) {
			persistenceService.delete(task);
		}
	}

	@Override
	public void addLogRecord(DBLogRecord logRecord)
	{
		logRecord.setLogId(persistenceService.generateId());
		persistenceService.saveNonBaseEntity(logRecord);
	}

	@Override
	public void cleanUpOldLogRecords()
	{
		long count = persistenceService.countClass(DBLogRecord.class);
		long max = DBLogManager.getMaxLogEntries();

		if (count > max) {
			log.log(Level.INFO, MessageFormat.format("Cleaning old log records:  {0}", (count - max)));

			long limit = count - max - MIN_DB_CLEAN_AMOUNT;
			if (limit > MAX_DB_CLEAN_AMOUNT) {
				limit = MAX_DB_CLEAN_AMOUNT;
			}
			if (limit < 0) {
				limit = 1;
			}
			String query = "SELECT FROM DBLogRecord ORDER BY eventDts ASC LIMIT " + limit;
			List<DBLogRecord> logRecords = persistenceService.query(query, null);
			logRecords.stream().forEach((record) -> {
				persistenceService.delete(record);
			});
		}
	}

	@Override
	public void clearAllLogRecord()
	{
		int recordsRemoved = persistenceService.deleteByQuery(DBLogRecord.class, "", new HashMap<>());
		log.log(Level.WARNING, MessageFormat.format("DB log records were cleared.  Records cleared: {0}", recordsRemoved));
	}

	@Override
	public void loadNewHelpSections(List<HelpSection> helpSections)
	{
		Objects.requireNonNull(helpSections, "Help sections required");

		int recordsRemoved = persistenceService.deleteByQuery(HelpSection.class, "", new HashMap<>());
		log.log(Level.FINE, MessageFormat.format("Help records were cleared.  Records cleared: {0}", recordsRemoved));

		log.log(Level.FINE, MessageFormat.format("Saving new Help records: {0}", helpSections.size()));
		for (HelpSection helpSection : helpSections) {
			helpSection.setId(persistenceService.generateId());
			persistenceService.persist(helpSection);
		}
	}

	@Override
	public HelpSectionAll getAllHelp(Boolean includeAdmin)
	{
		HelpSectionAll helpSectionAll = new HelpSectionAll();

		HelpSection helpSectionExample = new HelpSection();
		helpSectionExample.setAdminSection(includeAdmin);

		List<HelpSection> helpSections = persistenceService.queryByExample(HelpSection.class, helpSectionExample);

		if (helpSections.isEmpty() == false) {

			//Root Section
			HelpSection helpSectionRoot = new HelpSection();
			helpSectionRoot.setTitle(PropertiesManager.getValue(PropertiesManager.KEY_APPLICATION_TITLE));
			helpSectionRoot.setContent("<center><h2>User Guide</h2>Version: " + PropertiesManager.getApplicationVersion() + "</center>");
			helpSectionAll.setHelpSection(helpSectionRoot);

			for (HelpSection helpSection : helpSections) {
				String codeTokens[] = helpSection.getSectionNumber().split(Pattern.quote("."));
				HelpSectionAll rootHelp = helpSectionAll;
				StringBuilder codeKey = new StringBuilder();
				for (String codeToken : codeTokens) {
					codeKey.append(codeToken);
					//put in stubs as needed
					boolean found = false;
					String compare = codeKey.toString();
					if (codeKey.toString().length() == 1) {
						compare += ".";
					}
					for (HelpSectionAll child : rootHelp.getChildSections()) {
						if (child.getHelpSection().getSectionNumber().equals(compare)) {
							found = true;
							rootHelp = child;
							break;
						}
					}
					if (!found) {
						HelpSectionAll newChild = new HelpSectionAll();
						HelpSection childHelp = new HelpSection();
						childHelp.setSectionNumber(compare);
						newChild.setHelpSection(childHelp);
						rootHelp.getChildSections().add(newChild);
						rootHelp = newChild;
					}
					codeKey.append(".");
				}
				rootHelp.setHelpSection(helpSection);
			}
		}
		//reorder help number so missing sections do cause holes
		reorderHelpSectionTitles(helpSectionAll, "");
		return helpSectionAll;
	}

	private void reorderHelpSectionTitles(HelpSectionAll helpSectionAll, String parentSection)
	{
		if (helpSectionAll.getChildSections().isEmpty()) {
			return;
		}

		int sectionNumber = 1;
		for (HelpSectionAll helpSection : helpSectionAll.getChildSections()) {
			if (helpSection.getHelpSection().getTitle() == null) {
				helpSection.getHelpSection().setTitle("");
				log.log(Level.FINE, "This is a stub help section.  Check help data to make sure that is desired.  *=admin sections; make sure child sections are appropriately starred.");
			}

			String titleSplit[] = helpSection.getHelpSection().getTitle().split(" ");
			String titleNumber;
			if (StringUtils.isBlank(parentSection)) {
				titleNumber = sectionNumber + ". ";

			} else {
				titleNumber = parentSection + sectionNumber + " ";
			}

			StringBuilder restOfTitle = new StringBuilder();
			for (int i = 1; i < titleSplit.length; i++) {
				if (restOfTitle.length() != 0) {
					restOfTitle.append(" ");
				}
				restOfTitle.append(titleSplit[i]);
			}
			helpSection.getHelpSection().setTitle(titleNumber + restOfTitle.toString());

			if (titleNumber.endsWith(". ") == false) {
				StringBuilder temp = new StringBuilder();
				temp.append(titleNumber);
				temp = temp.deleteCharAt(temp.length() - 1);
				temp.append(".");
				titleNumber = temp.toString();
			} else {
				StringBuilder temp = new StringBuilder();
				temp.append(titleNumber);
				temp = temp.deleteCharAt(temp.length() - 1);
				titleNumber = temp.toString();
			}
			reorderHelpSectionTitles(helpSection, titleNumber);

			sectionNumber++;
		}
	}

	@Override
	public void toggleDBlogger(boolean activate)
	{
		PropertiesManager.setProperty(PropertiesManager.KEY_DBLOG_ON, "" + Convert.toBoolean(activate));

		//restart
		DBLogManager.cleanup();
		DBLogManager.init();
	}

}
