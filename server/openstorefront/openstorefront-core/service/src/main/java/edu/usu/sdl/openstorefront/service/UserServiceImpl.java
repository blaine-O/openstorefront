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

import com.orientechnologies.orient.core.record.impl.ODocument;
import edu.usu.sdl.openstorefront.common.exception.OpenStorefrontRuntimeException;
import edu.usu.sdl.openstorefront.common.manager.PropertiesManager;
import edu.usu.sdl.openstorefront.common.util.Convert;
import edu.usu.sdl.openstorefront.common.util.NetworkUtil;
import edu.usu.sdl.openstorefront.common.util.OpenStorefrontConstant;
import edu.usu.sdl.openstorefront.common.util.ReflectionUtil;
import edu.usu.sdl.openstorefront.common.util.StringProcessor;
import edu.usu.sdl.openstorefront.common.util.TimeUtil;
import edu.usu.sdl.openstorefront.core.api.ExternalUserManager;
import edu.usu.sdl.openstorefront.core.api.UserService;
import edu.usu.sdl.openstorefront.core.api.query.GenerateStatementOption;
import edu.usu.sdl.openstorefront.core.api.query.QueryByExample;
import edu.usu.sdl.openstorefront.core.api.query.QueryType;
import edu.usu.sdl.openstorefront.core.api.query.SpecialOperatorModel;
import edu.usu.sdl.openstorefront.core.entity.Alert;
import edu.usu.sdl.openstorefront.core.entity.ApprovalStatus;
import edu.usu.sdl.openstorefront.core.entity.AttributeCode;
import edu.usu.sdl.openstorefront.core.entity.Component;
import edu.usu.sdl.openstorefront.core.entity.Highlight;
import edu.usu.sdl.openstorefront.core.entity.NotificationEvent;
import edu.usu.sdl.openstorefront.core.entity.NotificationEventType;
import edu.usu.sdl.openstorefront.core.entity.TrackEventCode;
import edu.usu.sdl.openstorefront.core.entity.UserMessage;
import edu.usu.sdl.openstorefront.core.entity.UserMessageType;
import edu.usu.sdl.openstorefront.core.entity.UserProfile;
import edu.usu.sdl.openstorefront.core.entity.UserSavedSearch;
import edu.usu.sdl.openstorefront.core.entity.UserTracking;
import edu.usu.sdl.openstorefront.core.entity.UserTypeCode;
import edu.usu.sdl.openstorefront.core.entity.UserWatch;
import edu.usu.sdl.openstorefront.core.model.AdminMessage;
import edu.usu.sdl.openstorefront.core.view.FilterQueryParams;
import edu.usu.sdl.openstorefront.core.view.UserTrackingResult;
import edu.usu.sdl.openstorefront.security.SecurityUtil;
import edu.usu.sdl.openstorefront.security.UserContext;
import edu.usu.sdl.openstorefront.security.UserRecord;
import edu.usu.sdl.openstorefront.service.api.UserServicePrivate;
import edu.usu.sdl.openstorefront.service.manager.MailManager;
import edu.usu.sdl.openstorefront.service.manager.UserAgentManager;
import edu.usu.sdl.openstorefront.service.message.ApprovalMessageGenerator;
import edu.usu.sdl.openstorefront.service.message.BaseMessageGenerator;
import edu.usu.sdl.openstorefront.service.message.ChangeRequestApprovedMessageGenerator;
import edu.usu.sdl.openstorefront.service.message.ChangeRequestMessageGenerator;
import edu.usu.sdl.openstorefront.service.message.ComponentSubmissionMessageGenerator;
import edu.usu.sdl.openstorefront.service.message.ComponentWatchMessageGenerator;
import edu.usu.sdl.openstorefront.service.message.MessageContext;
import edu.usu.sdl.openstorefront.service.message.RecentChangeMessage;
import edu.usu.sdl.openstorefront.service.message.RecentChangeMessageGenerator;
import edu.usu.sdl.openstorefront.service.message.SystemErrorAlertMessageGenerator;
import edu.usu.sdl.openstorefront.service.message.TestMessageGenerator;
import edu.usu.sdl.openstorefront.service.message.UserDataAlertMessageGenerator;
import edu.usu.sdl.openstorefront.validation.ValidationModel;
import edu.usu.sdl.openstorefront.validation.ValidationResult;
import edu.usu.sdl.openstorefront.validation.ValidationUtil;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.servlet.http.HttpServletRequest;
import net.sf.uadetector.ReadableUserAgent;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.codemonkey.simplejavamail.MailException;
import org.codemonkey.simplejavamail.email.Email;

/**
 * Handles all user business logic
 *
 * @author dshurtleff
 */
public class UserServiceImpl
		extends ServiceProxy
		implements UserService, UserServicePrivate
{

	private static final Logger log = Logger.getLogger(UserServiceImpl.class.getName());

	private static final int MAX_NAME_CHECK = 100;

	@Override
	public List<UserWatch> getWatches(String userId)
	{
		UserWatch temp = new UserWatch();
		temp.setUsername(userId);
		temp.setActiveStatus(UserWatch.ACTIVE_STATUS);
		return persistenceService.queryByExample(UserWatch.class, new QueryByExample(temp));
	}

	@Override
	public UserWatch getWatch(String watchId)
	{
		UserWatch temp = new UserWatch();
		temp.setUserWatchId(watchId);
		return persistenceService.queryOneByExample(UserWatch.class, new QueryByExample(temp));
	}

	/**
	 *
	 * @param watch
	 * @return
	 */
	@Override
	public UserWatch saveWatch(UserWatch watch)
	{
		UserWatch oldWatch = persistenceService.findById(UserWatch.class, watch.getUserWatchId());
		if (oldWatch != null) {
			oldWatch.setActiveStatus(watch.getActiveStatus());
			oldWatch.setLastViewDts(watch.getLastViewDts());
			oldWatch.setNotifyFlg(watch.getNotifyFlg());
			oldWatch.setUpdateUser(watch.getUpdateUser());
			return persistenceService.persist(oldWatch);
		}
		watch.setUserWatchId(persistenceService.generateId());
		watch.setCreateDts(TimeUtil.currentDate());
		watch.setUpdateDts(TimeUtil.currentDate());
		watch.setLastViewDts(TimeUtil.currentDate());
		return persistenceService.persist(watch);
	}

	@Override
	public Boolean deleteWatch(String watchId)
	{
		UserWatch temp = persistenceService.findById(UserWatch.class, watchId);
		persistenceService.delete(temp);
		return Boolean.TRUE;
	}

	@Override
	public UserProfile getUserProfile(String userId)
	{
		UserProfile profile = persistenceService.findById(UserProfile.class, userId);
		return profile;
	}

	@Override
	public List<UserProfile> getAllProfiles(Boolean all)
	{
		UserProfile example = new UserProfile();
		if (!all) {
			example.setActiveStatus(UserProfile.ACTIVE_STATUS);
		}
		return persistenceService.queryByExample(UserProfile.class, new QueryByExample(example));
	}

	@Override
	public UserProfile saveUserProfile(UserProfile user)
	{
		return saveUserProfile(user, true);
	}

	@Override
	public UserProfile saveUserProfile(UserProfile user, boolean refreshSession)
	{
		UserProfile userProfile = persistenceService.findById(UserProfile.class, user.getUsername());
		if (userProfile != null) {
			if (StringUtils.isBlank(user.getActiveStatus())) {
				userProfile.setActiveStatus(UserProfile.ACTIVE_STATUS);
			} else {
				userProfile.setActiveStatus(user.getActiveStatus());
			}
			userProfile.setEmail(user.getEmail());
			userProfile.setPhone(user.getPhone());
			userProfile.setFirstName(user.getFirstName());
			userProfile.setLastName(user.getLastName());
			userProfile.setNotifyOfNew(user.getNotifyOfNew());
			userProfile.setOrganization(user.getOrganization());
			userProfile.setUserTypeCode(user.getUserTypeCode());
			if (StringUtils.isNotBlank(userProfile.getInternalGuid())) {
				userProfile.setInternalGuid(persistenceService.generateId());
			}
			userProfile.populateBaseUpdateFields();
			persistenceService.persist(userProfile);
		} else {
			user.setInternalGuid(persistenceService.generateId());
			user.populateBaseCreateFields();
			userProfile = persistenceService.persist(user);
		}

		userProfile = persistenceService.deattachAll(userProfile);
		getOrganizationService().addOrganization(userProfile.getOrganization());
		if (refreshSession) {
			UserContext userContext = SecurityUtil.getUserContext();
			if (userContext != null) {
				if (userContext.getUserProfile().getUsername().equals(userProfile.getUsername())) {
					userContext.setUserProfile(userProfile);
					SecurityUtils.getSubject().getSession().setAttribute(SecurityUtil.USER_CONTEXT_KEY, userContext);
				}
			}
		}
		return userProfile;
	}

	@Override
	public void deleteProfile(String username)
	{
		UserProfile profile = persistenceService.findById(UserProfile.class, username);
		if (profile != null) {
			profile.setActiveStatus(UserProfile.INACTIVE_STATUS);
			profile.setUpdateDts(TimeUtil.currentDate());
			profile.setUpdateUser(SecurityUtil.getCurrentUserName());
			if (StringUtils.isBlank(profile.getInternalGuid())) {
				//old profiles add missing info
				profile.setInternalGuid((persistenceService.generateId()));
			}
			persistenceService.persist(profile);

			UserWatch userwatchExample = new UserWatch();
			userwatchExample.setUsername(username);

			UserWatch userwatchSetExample = new UserWatch();
			userwatchSetExample.setActiveStatus(UserWatch.INACTIVE_STATUS);
			userwatchSetExample.setUpdateDts(TimeUtil.currentDate());
			userwatchSetExample.setUpdateUser(SecurityUtil.getCurrentUserName());

			persistenceService.updateByExample(UserWatch.class, userwatchSetExample, userwatchExample);

			UserMessage userMessageExample = new UserMessage();
			userMessageExample.setUsername(username);

			UserMessage userMessageSetExample = new UserMessage();
			userMessageSetExample.setActiveStatus(UserWatch.INACTIVE_STATUS);
			userMessageSetExample.setUpdateDts(TimeUtil.currentDate());
			userMessageSetExample.setUpdateUser(SecurityUtil.getCurrentUserName());
			persistenceService.updateByExample(UserMessage.class, userMessageSetExample, userMessageExample);
		}
	}

	@Override
	public void reactiveProfile(String username)
	{
		UserProfile profile = persistenceService.findById(UserProfile.class, username);
		if (profile != null) {
			profile.setActiveStatus(UserProfile.ACTIVE_STATUS);
			profile.setUpdateDts(TimeUtil.currentDate());
			profile.setUpdateUser(SecurityUtil.getCurrentUserName());
			persistenceService.persist(profile);

			UserWatch userwatchExample = new UserWatch();
			userwatchExample.setUsername(username);

			UserWatch userwatchSetExample = new UserWatch();
			userwatchSetExample.setActiveStatus(UserWatch.ACTIVE_STATUS);
			userwatchSetExample.setUpdateDts(TimeUtil.currentDate());
			userwatchSetExample.setUpdateUser(SecurityUtil.getCurrentUserName());

			persistenceService.updateByExample(UserWatch.class, userwatchSetExample, userwatchExample);
		} else {
			throw new OpenStorefrontRuntimeException("Unable to reactivate profile.  Userprofile not found: " + username, "Check userprofiles and username");
		}
	}

	@Override
	public UserTracking saveUserTracking(UserTracking tracking)
	{
		UserTracking oldTracking = persistenceService.findById(UserTracking.class, tracking.getTrackingId());
		if (oldTracking != null) {
			oldTracking.setActiveStatus(tracking.getActiveStatus());
			oldTracking.setBrowser(tracking.getBrowser());
			oldTracking.setBrowserVersion(tracking.getBrowserVersion());
			oldTracking.setClientIp(tracking.getClientIp());
			oldTracking.setUpdateDts(TimeUtil.currentDate());
			oldTracking.setUpdateUser(tracking.getUpdateUser());
			oldTracking.setEventDts(tracking.getEventDts());
			oldTracking.setDeviceType(tracking.getDeviceType());
			oldTracking.setOsPlatform(tracking.getOsPlatform());
			oldTracking.setScreenHeight(tracking.getScreenHeight());
			oldTracking.setScreenWidth(tracking.getScreenWidth());
			oldTracking.setTrackEventTypeCode(tracking.getTrackEventTypeCode());
			oldTracking.setOrganization(tracking.getOrganization());
			oldTracking.setUserTypeCode(tracking.getUserTypeCode());
			oldTracking.setUserAgent(tracking.getUserAgent());
			return persistenceService.persist(oldTracking);
		}
		tracking.setActiveStatus(UserTracking.ACTIVE_STATUS);
		tracking.setCreateDts(TimeUtil.currentDate());
		tracking.setUpdateDts(TimeUtil.currentDate());
		tracking.setTrackingId(persistenceService.generateId());
		return persistenceService.persist(tracking);
	}

//  This will be fleshed out more later
//	@Override
//	public List<Component> getRecentlyViewed(String userId)
//	{
//
//		//CONTINUE HERE... left off after work on friday.
//		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//	}
//
	@Override
	public UserContext handleLogin(UserProfile userprofile, HttpServletRequest request, Boolean admin)
	{
		Objects.requireNonNull(userprofile, "User Profile is required");
		Objects.requireNonNull(userprofile.getUsername(), "User Profile -> username is required");

		UserContext userContext = new UserContext();

		if (StringUtils.isBlank(userprofile.getUserTypeCode())) {
			userprofile.setUserTypeCode(UserTypeCode.END_USER);
		}

		//validate
		ValidationModel validationModelCode = new ValidationModel(userprofile);
		validationModelCode.setConsumeFieldsOnly(true);
		ValidationResult validationResult = ValidationUtil.validate(validationModelCode);
		if (validationResult.valid()) {

			//check for an existing profile
			UserProfile profile = persistenceService.findById(UserProfile.class, userprofile.getUsername());
			if (profile == null) {
				profile = userprofile;
				saveUserProfile(profile, false);
			} else {
				//Check user id
				boolean conflictUsername = false;
				if (StringUtils.isNotBlank(profile.getExternalGuid())
						|| StringUtils.isNotBlank(userprofile.getExternalGuid())) {
					if (profile.getExternalGuid() != null
							&& profile.getExternalGuid().equals(userprofile.getExternalGuid()) == false) {
						conflictUsername = true;
					} else if (userprofile.getExternalGuid() != null
							&& userprofile.getExternalGuid().equals(profile.getExternalGuid()) == false) {
						conflictUsername = true;
					}
				}

				if (conflictUsername) {
					//create a new user name and then save the profile  (We may get multiple "john.doe" so this handles that situation)
					boolean unique = false;
					int idIndex = 1;
					do {
						String userName = userprofile.getUsername() + "_" + (idIndex++);
						UserProfile profileCheck = persistenceService.findById(UserProfile.class, userName);
						if (profileCheck == null) {
							profile = userprofile;
							profile.setExternalUserId(userprofile.getUsername());
							profile.setUsername(userName);
							saveUserProfile(profile, false);
							unique = true;
						}
					} while (!unique && idIndex < MAX_NAME_CHECK);

					if (unique == false) {
						throw new OpenStorefrontRuntimeException("Failed to create a unique username.", "Check username and make sure it's unique.");
					}
				}
			}

			//Activative profile on login
			if (UserProfile.INACTIVE_STATUS.equals(profile.getActiveStatus())) {
				log.log(Level.INFO, MessageFormat.format("User: {0} profile was INACTIVE reactivating it on login.", profile.getUsername()));
				getUserService().reactiveProfile(userprofile.getUsername());
			}

			profile = persistenceService.deattachAll(profile);
			userContext.setUserProfile(profile);
			if (admin != null) {
				userContext.setAdmin(admin);
			} else {
				userContext.setAdmin(SecurityUtil.isAdminUser());
			}
			SecurityUtils.getSubject().getSession().setAttribute(SecurityUtil.USER_CONTEXT_KEY, userContext);

			//Add tracking if it's a client request
			if (request != null) {
				UserTracking userTracking = new UserTracking();
				userTracking.setTrackEventTypeCode(TrackEventCode.LOGIN);
				userTracking.setEventDts(TimeUtil.currentDate());
				userTracking.setUpdateUser(profile.getUsername());
				userTracking.setCreateUser(profile.getUsername());
				userTracking.setOrganization(profile.getOrganization());
				userTracking.setUserTypeCode(profile.getUserTypeCode());

				String userAgent = request.getHeader(OpenStorefrontConstant.HEADER_USER_AGENT);
				ReadableUserAgent readableUserAgent = UserAgentManager.parse(userAgent);
				if (readableUserAgent != null) {
					userTracking.setBrowser(readableUserAgent.getName());
					userTracking.setBrowserVersion(readableUserAgent.getVersionNumber().toVersionString());
					userTracking.setDeviceType(readableUserAgent.getTypeName());
					userTracking.setOsPlatform(readableUserAgent.getOperatingSystem().getName() + "  version: " + readableUserAgent.getOperatingSystem().getVersionNumber().toVersionString());
				}
				userTracking.setClientIp(NetworkUtil.getClientIp(request));
				userTracking.setUserAgent(userAgent);

				saveUserTracking(userTracking);
			} else {
				log.log(Level.INFO, MessageFormat.format("Login handled for user: {0} (Not an external client request...not tracking", profile.getUsername()));
			}
			String adminLog = "";
			if (userContext.isAdmin()) {
				adminLog = "(Admin)";
			}

			log.log(Level.INFO, MessageFormat.format("User {0} sucessfully logged in. {1}", profile.getUsername(), adminLog));

		} else {
			throw new OpenStorefrontRuntimeException("Failed to validate the userprofile. Validation Message: " + validationResult.toString(), "Check data");
		}

		return userContext;
	}

	@Override
	public void removeAllWatchesForComponent(String componentId)
	{
		UserWatch userWatchExample = new UserWatch();
		userWatchExample.setComponentId(componentId);
		persistenceService.deleteByExample(userWatchExample);
	}

	@Override
	public void sendTestEmail(String username, String overrideEmail)
	{
		UserProfile userProfile = getUserProfile(username);
		if (userProfile != null) {
			if (StringUtils.isNotBlank(overrideEmail) || StringUtils.isNotBlank(userProfile.getEmail())) {
				if (StringUtils.isNotBlank(overrideEmail)) {
					userProfile.setEmail(overrideEmail);
				}
				TestMessageGenerator testMessageGenerator = new TestMessageGenerator(new MessageContext(userProfile));
				Email email = testMessageGenerator.generateMessage();
				MailManager.send(email);
				log.log(Level.INFO, MessageFormat.format("Sent test email to: {0}", userProfile.getEmail()));
			} else {
				throw new OpenStorefrontRuntimeException("User is missing email address.", "Add a valid email address.");
			}
		} else {
			throw new OpenStorefrontRuntimeException("Unable to find user.", "Check username.");
		}
	}

	@Override
	public void checkComponentWatches(Component component)
	{
		UserWatch userWatchExample = new UserWatch();
		userWatchExample.setActiveStatus(UserMessage.ACTIVE_STATUS);
		userWatchExample.setComponentId(component.getComponentId());

		List<UserWatch> userWatches = persistenceService.queryByExample(UserWatch.class, userWatchExample);
		for (UserWatch userWatch : userWatches) {
			if (component.getLastActivityDts().after(userWatch.getLastViewDts())) {
				if (Convert.toBoolean(userWatch.getNotifyFlg())) {
					UserMessage userMessage = new UserMessage();
					userMessage.setUsername(userWatch.getUsername());
					userMessage.setComponentId(component.getComponentId());
					userMessage.setUserMessageType(UserMessageType.COMPONENT_WATCH);
					userMessage.setCreateUser(OpenStorefrontConstant.SYSTEM_USER);
					userMessage.setUpdateUser(OpenStorefrontConstant.SYSTEM_USER);
					getUserService().queueUserMessage(userMessage);
				}

				NotificationEvent notificationEvent = new NotificationEvent();
				notificationEvent.setEventType(NotificationEventType.WATCH);
				notificationEvent.setUsername(userWatch.getUsername());
				notificationEvent.setMessage("Component: " + component.getName() + " has been updated.");
				notificationEvent.setEntityName(Component.class.getSimpleName());
				notificationEvent.setEntityId(component.getComponentId());
				getNotificationService().postEvent(notificationEvent);
			}
		}
	}

	@Override
	public void queueUserMessage(UserMessage userMessage)
	{
		UserMessage userMessageExample = new UserMessage();
		userMessageExample.setActiveStatus(UserMessage.ACTIVE_STATUS);
		userMessageExample.setUsername(userMessage.getUsername());
		userMessageExample.setComponentId(userMessage.getComponentId());
		userMessageExample.setUserMessageType(userMessage.getUserMessageType());
		userMessageExample.setAlertId(userMessage.getAlertId());
		userMessageExample.setEmailAddress(userMessage.getEmailAddress());

		//Duplicate check;
		UserMessage userMessageExisting = persistenceService.queryOneByExample(UserMessage.class, userMessageExample);
		if (userMessageExisting == null) {
			userMessage.setUserMessageId(persistenceService.generateId());
			userMessage.setRetryCount(0);
			userMessage.populateBaseCreateFields();
			persistenceService.persist(userMessage);
		}
	}

	@Override
	public void cleanupOldUserMessages()
	{
		int maxDays = Convert.toInteger(PropertiesManager.getValue(PropertiesManager.KEY_MESSAGE_KEEP_DAYS, "30"));

		LocalDateTime archiveTime = LocalDateTime.now();
		archiveTime = archiveTime.minusDays(maxDays);
		archiveTime = archiveTime.truncatedTo(ChronoUnit.DAYS);
		String deleteQuery = "updateDts < :maxUpdateDts AND activeStatus = :activeStatusParam";

		ZonedDateTime zdt = archiveTime.atZone(ZoneId.systemDefault());
		Date archiveDts = Date.from(zdt.toInstant());

		Map<String, Object> queryParams = new HashMap<>();
		queryParams.put("maxUpdateDts", archiveDts);
		queryParams.put("activeStatusParam", UserMessage.INACTIVE_STATUS);

		persistenceService.deleteByQuery(UserMessage.class, deleteQuery, queryParams);
	}

	@Override
	public void sendAdminMessage(AdminMessage adminMessage)
	{
		String appTitle = getBrandingService().getCurrentBrandingView().getBranding().getApplicationName();

		UserProfile userProfileExample = new UserProfile();
		userProfileExample.setActiveStatus(UserProfile.ACTIVE_STATUS);
		
		List<UserProfile> usersToSend = new ArrayList<>();

		if (StringUtils.isNotBlank(adminMessage.getUserTypeCode())) {
			log.log(Level.INFO, MessageFormat.format("(Admin Message) Sending email to users of type: {0}", adminMessage.getUserTypeCode()));
			userProfileExample.setUserTypeCode(adminMessage.getUserTypeCode());
			List<UserProfile> userProfiles = persistenceService.queryByExample(UserProfile.class, userProfileExample);
			for (UserProfile userProfile : userProfiles) {
				if (StringUtils.isNotBlank(userProfile.getEmail())) {
					usersToSend.add(userProfile);
				}
			}
		} else if (adminMessage.getUsersToEmail().isEmpty() == false) {
			log.log(Level.INFO, "(Admin Message) Sending email to specfic users");
			List<String> emailList = new ArrayList<>();
			for (String email : adminMessage.getUsersToEmail()) {
				if (StringUtils.isNotBlank(email)) {
					emailList.add(email.trim());
				}
			}

			StringBuilder query = new StringBuilder();
			query.append("select from ").append(UserProfile.class.getSimpleName()).append(" where email IS NOT NULL AND username IN :userList OR email IN :userList2");
			Map<String, Object> params = new HashMap<>();
			params.put("userList", emailList);
			params.put("userList2", emailList);
			usersToSend = persistenceService.query(query.toString(), params);
			for (String email : emailList) {
				Boolean found = false;
				for (UserProfile user : usersToSend) {
					if (StringUtils.equalsIgnoreCase(user.getEmail(), email)) {
						found = true;
					} else if (StringUtils.equalsIgnoreCase(user.getUsername(), email)) {
						found = true;
					}
				}
				if (!found && StringProcessor.isEmail(email)) {
					UserProfile temp = new UserProfile();
					temp.setEmail(email);
					temp.setFirstName("");
					temp.setLastName("");
					usersToSend.add(temp);
				}
			}
		}

		//filter out duplicate email addresses
		Set<String> emailAddressSet = new HashSet<>();
		for (int i = usersToSend.size() - 1; i >= 0; i--) {
			UserProfile userProfile = usersToSend.get(i);
			if (emailAddressSet.contains(userProfile.getEmail())) {
				usersToSend.remove(i);
			} else {
				emailAddressSet.add(userProfile.getEmail());
			}
		}

		int emailCount = 0;
		Email email = MailManager.newEmail();
		email.setSubject(appTitle + " - " + adminMessage.getSubject());
		email.setTextHTML(adminMessage.getMessage());		
		
		for (UserProfile userProfile : usersToSend) {
			String name = userProfile.getFirstName() + " " + userProfile.getLastName();
			email.addRecipient(name, userProfile.getEmail(), Message.RecipientType.TO);
			emailCount++;
		}		
		for (String emailAddress : adminMessage.getCcEmails()) {
			email.addRecipient("", emailAddress, Message.RecipientType.CC);			
			emailCount++;
		}
		for (String emailAddress : adminMessage.getBccEmails()) {
			email.addRecipient("", emailAddress, Message.RecipientType.BCC);			
			emailCount++;
		}
		MailManager.send(email);		
		log.log(Level.INFO, MessageFormat.format("(Admin Message) {0} email(s) sent (in one message)", emailCount));
	}

	@Override
	public void processAllUserMessages(boolean sendNow)
	{
		cleanupOldUserMessages();

		UserMessage userMessageExample = new UserMessage();
		userMessageExample.setActiveStatus(UserMessage.ACTIVE_STATUS);

		List<UserMessage> userMessages = persistenceService.queryByExample(UserMessage.class, userMessageExample);
		int minQueueMinutes = Convert.toInteger(PropertiesManager.getValue(PropertiesManager.KEY_MESSAGE_MIN_QUEUE_MINUTES, "10"));
		int maxRetries = Convert.toInteger(PropertiesManager.getValue(PropertiesManager.KEY_MESSAGE_MAX_RETRIES, "5"));
		if (minQueueMinutes < 0) {
			minQueueMinutes = 0;
		}
		long queueMills = System.currentTimeMillis() - TimeUtil.minutesToMillis(minQueueMinutes);

		//remove dups
		Map<String, UserMessage> messageMap = new HashMap<>();
		for (UserMessage userMessage : userMessages) {
			if (messageMap.containsKey(userMessage.uniqueKey())) {
				log.log(Level.FINE, MessageFormat.format("Removing duplicate user message: ", userMessage.uniqueKey()));
			} else {
				messageMap.put(userMessage.uniqueKey(), userMessage);
			}
		}

		for (UserMessage userMessage : messageMap.values()) {

			if (sendNow || userMessage.getCreateDts().getTime() <= queueMills) {

				boolean updateUserMessage = false;
				UserMessage userMessageExisting = persistenceService.findById(UserMessage.class, userMessage.getUserMessageId());
				if (userMessage.getRetryCount() < maxRetries) {
					try {
						getUserServicePrivate().sendUserMessage(userMessage);
					} catch (MailException mailException) {
						log.log(Level.FINE, "Unable to send message.", mailException);
						userMessageExisting.setBodyOfMessage("Unable to send message to user.  Mail Server down? " + mailException.getMessage());
						userMessageExisting.setRetryCount(userMessage.getRetryCount() + 1);
						updateUserMessage = true;
					} catch (Exception e) {
						log.log(Level.SEVERE, "Unexpected error.  Failed sending message. (Halt sending of " + userMessage.getUserMessageId() + " message.)", e);
						userMessageExisting.setActiveStatus(UserMessage.INACTIVE_STATUS);
						userMessageExisting.setBodyOfMessage("System expection occured while sending message. See logs for details");
						updateUserMessage = true;
					}
				} else {
					userMessageExisting.setActiveStatus(UserMessage.INACTIVE_STATUS);
					userMessageExisting.setBodyOfMessage("Exceed max reties.  Inactivated  message.");
					updateUserMessage = true;
				}

				if (updateUserMessage) {

					userMessageExisting.setUpdateUser(OpenStorefrontConstant.SYSTEM_USER);
					userMessageExisting.setUpdateDts(TimeUtil.currentDate());
					persistenceService.persist(userMessageExisting);
				}

			} else {
				log.log(Level.FINEST, MessageFormat.format("Not time yet to send email to user: {0}", userMessage.getUsername()));
			}

		}

	}

	@Override
	public void sendUserMessage(UserMessage userMessage)
	{
		UserProfile userProfile = getUserProfile(userMessage.getUsername());
		UserMessage userMessageExisting = persistenceService.findById(UserMessage.class, userMessage.getUserMessageId());

		String emailAddress = userMessage.getEmailAddress();
		if (StringUtils.isBlank(emailAddress)) {
			if (userProfile != null) {
				emailAddress = userProfile.getEmail();
			}
		}

		if (StringUtils.isNotBlank(emailAddress)) {
			MessageContext messageContext = new MessageContext(userProfile);
			messageContext.setUserMessage(userMessage);
			if (StringUtils.isNotBlank(userMessage.getAlertId())) {
				messageContext.setAlert(persistenceService.findById(Alert.class, userMessage.getAlertId()));
			}

			BaseMessageGenerator generator = null;
			if (null != userMessage.getUserMessageType()) {
				switch (userMessage.getUserMessageType()) {
					case UserMessageType.COMPONENT_WATCH:
						generator = new ComponentWatchMessageGenerator(messageContext);
						break;
					case UserMessageType.USER_DATA_ALERT:
						generator = new UserDataAlertMessageGenerator(messageContext);
						break;
					case UserMessageType.SYSTEM_ERROR_ALERT:
						generator = new SystemErrorAlertMessageGenerator(messageContext);
						break;
					case UserMessageType.COMPONENT_SUBMISSION_ALERT:
						generator = new ComponentSubmissionMessageGenerator(messageContext);
						break;
					case UserMessageType.APPROVAL_NOTIFICATION:
						generator = new ApprovalMessageGenerator(messageContext);
						break;
					case UserMessageType.CHANGE_REQUEST_APPROVAL_NOTIFICATION:
						generator = new ChangeRequestApprovedMessageGenerator(messageContext);
						break;
					case UserMessageType.CHANGE_REQUEST_ALERT:
						generator = new ChangeRequestMessageGenerator(messageContext);
						break;
				}
			}

			if (generator == null) {
				throw new UnsupportedOperationException("Message type not supported.  Type: " + userMessage.getUserMessageType());
			}

			Email email = generator.generateMessage();
			if (email != null) {
				MailManager.send(email);
				userMessageExisting.setSubject(email.getSubject());
				userMessageExisting.setBodyOfMessage(email.getTextHTML());
				userMessageExisting.setSentEmailAddress(emailAddress);
			} else {
				userMessageExisting.setBodyOfMessage("Message was empty no email sent.");
			}
		} else {
			userMessageExisting.setBodyOfMessage("No email address set for user");
		}

		userMessageExisting.setActiveStatus(UserMessage.INACTIVE_STATUS);
		userMessageExisting.setUpdateDts(TimeUtil.currentDate());
		userMessageExisting.setUpdateUser(OpenStorefrontConstant.SYSTEM_USER);
		persistenceService.persist(userMessageExisting);
	}

	@Override
	public void sendRecentChangeEmail(Date lastRunDts)
	{
		sendRecentChangeEmail(lastRunDts, null);
	}

	@Override
	public void sendRecentChangeEmail(Date lastRunDts, String emailAddress)
	{
		Objects.requireNonNull(lastRunDts, "Last Run Dts is required");

		UserProfile userProfileExample = new UserProfile();
		userProfileExample.setActiveStatus(UserProfile.ACTIVE_STATUS);
		userProfileExample.setNotifyOfNew(Boolean.TRUE);

		List<UserProfile> userProfiles = persistenceService.queryByExample(UserProfile.class, userProfileExample);
		RecentChangeMessage recentChangeMessage = new RecentChangeMessage();
		recentChangeMessage.setLastRunDts(lastRunDts);

		String componentQuery = "select from " + Component.class.getSimpleName() + " where lastActivityDts > :lastActivityParam and activeStatus = :activeStatusParam";

		Map<String, Object> queryParams = new HashMap<>();
		queryParams.put("lastActivityParam", lastRunDts);
		queryParams.put("activeStatusParam", Component.ACTIVE_STATUS);
		List<Component> components = persistenceService.query(componentQuery, queryParams);
		for (Component component : components) {
			if (ApprovalStatus.APPROVED.equals(component.getApprovalState())) {
				if (component.getApprovedDts() != null
						&& component.getApprovedDts().after(lastRunDts)) {
					recentChangeMessage.getComponentsAdded().add(component);
				} else {
					recentChangeMessage.getComponentsUpdated().add(component);
				}
			}
		}

		String highlightQuery = "select from " + Highlight.class.getSimpleName() + " where updateDts > :updateDtsParam and activeStatus = :activeStatusParam";
		queryParams = new HashMap<>();
		queryParams.put("updateDtsParam", lastRunDts);
		queryParams.put("activeStatusParam", AttributeCode.ACTIVE_STATUS);
		List<Highlight> highLights = persistenceService.query(highlightQuery, queryParams);
		for (Highlight highLight : highLights) {
			if (highLight.getCreateDts().after(lastRunDts)) {
				recentChangeMessage.getHighlightsAdded().add(highLight);
			} else {
				recentChangeMessage.getHighlightsUpdated().add(highLight);
			}
		}

		if (StringUtils.isNotBlank(emailAddress)) {
			MessageContext messageContext = new MessageContext(null);
			messageContext.setRecentChangeMessage(recentChangeMessage);

			RecentChangeMessageGenerator messageGenerator = new RecentChangeMessageGenerator(messageContext);
			Email email = messageGenerator.generateMessage();
			if (email != null) {
				email.addRecipient("", emailAddress, Message.RecipientType.TO);
				MailManager.send(email);
			}

		} else {
			for (UserProfile userProfile : userProfiles) {
				if (StringUtils.isNotBlank(userProfile.getEmail())) {
					MessageContext messageContext = new MessageContext(userProfile);
					messageContext.setRecentChangeMessage(recentChangeMessage);

					RecentChangeMessageGenerator messageGenerator = new RecentChangeMessageGenerator(messageContext);
					Email email = messageGenerator.generateMessage();
					if (email != null) {
						MailManager.send(email);
					}
				}
			}
		}

	}

	@Override
	public void removeUserMessage(String userMessageId)
	{
		UserMessage userMessage = persistenceService.findById(UserMessage.class, userMessageId);
		if (userMessage != null) {
			persistenceService.delete(userMessage);
		}
	}

	@Override
	public Map<String, Date> getLastLogin(List<UserProfile> userProfiles)
	{
		Map<String, Date> userLoginMap = new HashMap<>();

		if (userProfiles.isEmpty() == false) {
			StringBuilder query = new StringBuilder();
			query.append("select MAX(eventDts), createUser from ").append(UserTracking.class.getSimpleName());
			query.append(" where activeStatus = :userTrackingActiveStatusParam  and  trackEventTypeCode = :trackEventCodeParam and createUser IN :userListParam");

			List<String> usernames = new ArrayList<>();
			userProfiles.stream().forEach((userProfile) -> {
				usernames.add(userProfile.getUsername());
			});
			query.append(" group by createUser");

			if (usernames.isEmpty() == false) {
				Map<String, Object> paramMap = new HashMap<>();
				paramMap.put("userTrackingActiveStatusParam", UserTracking.ACTIVE_STATUS);
				paramMap.put("trackEventCodeParam", TrackEventCode.LOGIN);
				paramMap.put("userListParam", usernames);
				List<ODocument> documents = persistenceService.query(query.toString(), paramMap);
				documents.stream().forEach((document) -> {
					userLoginMap.put(document.field("createUser"), document.field("MAX"));
				});
			}
		}

		return userLoginMap;
	}

	@Override
	public UserTrackingResult getUserTracking(FilterQueryParams filter, String userId)
	{
		UserTrackingResult result = new UserTrackingResult();

		UserTracking userTrackingExample = new UserTracking();
		userTrackingExample.setActiveStatus(filter.getStatus());
		userTrackingExample.setCreateUser(userId);

		UserTracking userTrackingStartExample = new UserTracking();
		userTrackingStartExample.setEventDts(filter.getStart());

		UserTracking userTrackingEndExample = new UserTracking();
		userTrackingEndExample.setEventDts(filter.getEnd());

		QueryByExample queryByExample = new QueryByExample(userTrackingExample);

		SpecialOperatorModel specialOperatorModel = new SpecialOperatorModel();
		specialOperatorModel.setExample(userTrackingStartExample);
		specialOperatorModel.getGenerateStatementOption().setOperation(GenerateStatementOption.OPERATION_GREATER_THAN);
		queryByExample.getExtraWhereCauses().add(specialOperatorModel);

		specialOperatorModel = new SpecialOperatorModel();
		specialOperatorModel.setExample(userTrackingEndExample);
		specialOperatorModel.getGenerateStatementOption().setOperation(GenerateStatementOption.OPERATION_LESS_THAN_EQUAL);
		specialOperatorModel.getGenerateStatementOption().setParameterSuffix(GenerateStatementOption.PARAMETER_SUFFIX_END_RANGE);
		queryByExample.getExtraWhereCauses().add(specialOperatorModel);

		queryByExample.setMaxResults(filter.getMax());
		queryByExample.setFirstResult(filter.getOffset());
		queryByExample.setSortDirection(filter.getSortOrder());

		UserTracking userTrackingOrderExample = new UserTracking();
		Field sortField = ReflectionUtil.getField(userTrackingOrderExample, filter.getSortField());
		if (sortField != null) {
			try {
				BeanUtils.setProperty(userTrackingOrderExample, sortField.getName(), QueryByExample.getFlagForType(sortField.getType()));
			} catch (IllegalAccessException | InvocationTargetException ex) {
				log.log(Level.WARNING, "Unable set sort field", ex);
			}
			queryByExample.setOrderBy(userTrackingOrderExample);
		}

		result.setResult(persistenceService.queryByExample(UserTracking.class, queryByExample));
		queryByExample.setQueryType(QueryType.COUNT);
		result.setCount(persistenceService.countByExample(queryByExample));

		return result;
	}

	@Override
	public void syncUserProfilesWithUserManagement(ExternalUserManager userManager)
	{
		UserProfile userProfileExample = new UserProfile();
		userProfileExample.setActiveStatus(UserProfile.ACTIVE_STATUS);

		//page through users
		long pageSize = 200;
		long maxRecords = persistenceService.countByExample(userProfileExample);
		for (long i = 0; i < maxRecords; i = i + pageSize) {
			QueryByExample queryByExample = new QueryByExample(userProfileExample);
			queryByExample.setFirstResult((int) i);
			queryByExample.setMaxResults((int) pageSize);
			queryByExample.setReturnNonProxied(false);

			List<UserProfile> userProfiles = persistenceService.queryByExample(UserProfile.class, queryByExample);
			List<String> usernames = new ArrayList<>();
			for (UserProfile userProfile : userProfiles) {
				usernames.add(userProfile.getUsername());
			}
			List<UserRecord> userRecords = userManager.findUsers(usernames);
			Set<String> activeUserSet = new HashSet<>();
			for (UserRecord userRecord : userRecords) {
				activeUserSet.add(userRecord.getUsername());
			}
			for (UserProfile userProfile : userProfiles) {
				if (activeUserSet.contains(userProfile.getUsername()) == false) {
					log.log(Level.INFO, "User not found in external user management, Inacvtivating user. (Sync Service)");
					deleteProfile(userProfile.getUsername());
				}
			}
		}
	}

	@Override
	public UserSavedSearch saveUserSearch(UserSavedSearch userSavedSearch)
	{
		UserSavedSearch existing = persistenceService.findById(UserSavedSearch.class, userSavedSearch.getSearchName());
		if (existing != null) {
			existing.updateFields(userSavedSearch);
			existing = persistenceService.persist(existing);
		} else {
			userSavedSearch.setUserSearchId(persistenceService.generateId());			
			userSavedSearch.populateBaseCreateFields();
			existing = persistenceService.persist(userSavedSearch);
		}
		
		return existing;
	}

}
