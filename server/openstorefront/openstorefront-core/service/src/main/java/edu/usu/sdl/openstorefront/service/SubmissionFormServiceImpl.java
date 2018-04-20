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

import edu.usu.sdl.openstorefront.common.exception.OpenStorefrontRuntimeException;
import edu.usu.sdl.openstorefront.common.util.LockSwitch;
import edu.usu.sdl.openstorefront.common.util.OpenStorefrontConstant;
import edu.usu.sdl.openstorefront.core.api.PersistenceService;
import edu.usu.sdl.openstorefront.core.api.SubmissionFormService;
import edu.usu.sdl.openstorefront.core.entity.ApprovalStatus;
import edu.usu.sdl.openstorefront.core.entity.ComponentRelationship;
import edu.usu.sdl.openstorefront.core.entity.ComponentType;
import edu.usu.sdl.openstorefront.core.entity.MediaFile;
import edu.usu.sdl.openstorefront.core.entity.SubmissionFormResource;
import edu.usu.sdl.openstorefront.core.entity.SubmissionFormTemplate;
import edu.usu.sdl.openstorefront.core.entity.SubmissionTemplateStatus;
import edu.usu.sdl.openstorefront.core.entity.UserSubmission;
import edu.usu.sdl.openstorefront.core.entity.UserSubmissionField;
import edu.usu.sdl.openstorefront.core.entity.UserSubmissionMedia;
import edu.usu.sdl.openstorefront.core.model.ComponentAll;
import edu.usu.sdl.openstorefront.core.model.ComponentFormSet;
import edu.usu.sdl.openstorefront.core.model.VerifySubmissionTemplateResult;
import edu.usu.sdl.openstorefront.core.util.MediaFileType;
import edu.usu.sdl.openstorefront.service.mapping.MappingController;
import edu.usu.sdl.openstorefront.service.mapping.MappingException;
import edu.usu.sdl.openstorefront.validation.ValidationResult;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles Submission Forms and Templates
 *
 * @author dshurtleff
 */
public class SubmissionFormServiceImpl
		extends ServiceProxy
		implements SubmissionFormService
{

	private static final Logger LOG = Logger.getLogger(SubmissionFormServiceImpl.class.getName());

	private MappingController mappingController = new MappingController();

	public SubmissionFormServiceImpl()
	{
	}

	public SubmissionFormServiceImpl(PersistenceService persistenceService)
	{
		super(persistenceService);
	}

	public void setMappingController(MappingController mappingController)
	{
		this.mappingController = mappingController;
	}

	@Override
	public SubmissionFormTemplate saveSubmissionFormTemplate(SubmissionFormTemplate template)
	{
		Objects.requireNonNull(template);

		//Find type to verify against pick one
		List<ComponentType> componentType = getComponentService().getAllComponentTypes();
		if (componentType.isEmpty()) {
			throw new OpenStorefrontRuntimeException("At least one component type needs to be defined and active", "Add Component Type");
		}

		ValidationResult validationResult = validateTemplate(template, componentType.get(0).getComponentType());
		if (validationResult.valid()) {
			template.setTemplateStatus(SubmissionTemplateStatus.PENDING_VERIFICATION);
		} else {
			template.setTemplateStatus(SubmissionTemplateStatus.INCOMPLETE);
		}

		SubmissionFormTemplate existing = persistenceService.findById(SubmissionFormTemplate.class, template.getSubmissionTemplateId());
		if (existing != null) {
			existing.updateFields(template);
			template = persistenceService.persist(existing);
		} else {
			template.setSubmissionTemplateId(persistenceService.generateId());
			template.populateBaseCreateFields();
			template = persistenceService.persist(template);
		}
		template = persistenceService.unwrapProxyObject(template);
		return template;
	}

	@Override
	public void deleteSubmissionFormTemplate(String templateId)
	{
		SubmissionFormTemplate existing = persistenceService.findById(SubmissionFormTemplate.class, templateId);
		if (existing != null) {

			SubmissionFormResource resourceExample = new SubmissionFormResource();
			resourceExample.setTemplateId(templateId);

			List<SubmissionFormResource> resources = resourceExample.findByExample();
			resources.forEach(resource -> {
				deleteSubmissionFormResource(resource.getResourceId());
			});

			persistenceService.delete(existing);
		}
	}

	@Override
	public ValidationResult validateTemplate(SubmissionFormTemplate template, String componentType)
	{
		Objects.requireNonNull(template);
		return mappingController.verifyTemplate(template, componentType);
	}

	@Override
	public SubmissionFormResource saveSubmissionFormResource(SubmissionFormResource resource, InputStream in)
	{
		Objects.requireNonNull(resource);
		Objects.requireNonNull(resource.getFile());
		Objects.requireNonNull(in);

		SubmissionFormResource savedResource = resource.save();
		try (InputStream fileInput = in) {
			MediaFile mediaFile = savedResource.getFile();
			mediaFile.setMediaFileId(persistenceService.generateId());
			mediaFile.setFileName(persistenceService.generateId() + OpenStorefrontConstant.getFileExtensionForMime(mediaFile.getMimeType()));
			mediaFile.setFileType(MediaFileType.RESOURCE);
			Path path = Paths.get(MediaFileType.RESOURCE.getPath(), mediaFile.getFileName());
			Files.copy(fileInput, path, StandardCopyOption.REPLACE_EXISTING);

			persistenceService.persist(savedResource);
		} catch (IOException ex) {
			throw new OpenStorefrontRuntimeException("Unable to store media file.", "Contact System Admin.  Check file permissions and disk space ", ex);
		}

		savedResource = persistenceService.unwrapProxyObject(savedResource);
		return savedResource;

	}

	@Override
	public void deleteSubmissionFormResource(String resourceId)
	{
		SubmissionFormResource resource = persistenceService.findById(SubmissionFormResource.class, resourceId);

		if (resource.getFile() != null) {
			Path path = resource.getFile().path();
			if (path != null && path.toFile().exists()) {
				try {
					Files.delete(path);
				} catch (IOException ex) {
					LOG.log(Level.WARNING, MessageFormat.format("Unable to delete local media. Path: {0}", path));
					LOG.log(Level.FINE, null, ex);
				}
			}

			persistenceService.delete(resource);
		}
	}

	@Override
	public List<UserSubmission> getUserSubmissions(String ownerUsername)
	{
		UserSubmission userSubmissionExample = new UserSubmission();
		userSubmissionExample.setActiveStatus(UserSubmission.ACTIVE_STATUS);
		userSubmissionExample.setOwnerUsername(ownerUsername);
		return userSubmissionExample.findByExample();
	}

	@Override
	public UserSubmission saveUserSubmission(UserSubmission userSubmission)
	{
		UserSubmission existing = persistenceService.findById(UserSubmission.class, userSubmission.getUserSubmissionId());
		if (existing != null) {
			existing.updateFields(userSubmission);
			existing = persistenceService.persist(existing);
		} else {
			userSubmission.setUserSubmissionId(persistenceService.generateId());
			userSubmission.populateBaseCreateFields();
			existing = persistenceService.persist(userSubmission);
		}
		existing = persistenceService.unwrapProxyObject(existing);
		return existing;
	}

	@Override
	public UserSubmission saveSubmissionFormMedia(UserSubmission userSubmission, String fieldId, MediaFile mediaFile, InputStream in)
	{
		Objects.requireNonNull(userSubmission);
		Objects.requireNonNull(fieldId);
		Objects.requireNonNull(in);

		UserSubmissionField field = null;
		if (userSubmission.getFields() != null) {
			for (UserSubmissionField existing : userSubmission.getFields()) {
				if (existing.getFieldId().equals(fieldId)) {
					field = existing;
				}
			}
		}

		if (field == null) {
			throw new OpenStorefrontRuntimeException(
					"Unable to find user submission field id: "
					+ fieldId
					+ " submission Id: "
					+ userSubmission.getUserSubmissionId(), "Check Data");
		}

		try (InputStream fileInput = in) {
			UserSubmissionMedia userSubmissionMedia = new UserSubmissionMedia();
			userSubmissionMedia.setFieldId(fieldId);
			userSubmissionMedia.setSubmissionMediaId(persistenceService.generateId());

			mediaFile.setMediaFileId(persistenceService.generateId());
			mediaFile.setFileName(persistenceService.generateId() + OpenStorefrontConstant.getFileExtensionForMime(mediaFile.getMimeType()));
			mediaFile.setFileType(MediaFileType.MEDIA);
			Path path = Paths.get(MediaFileType.MEDIA.getPath(), mediaFile.getFileName());
			Files.copy(fileInput, path, StandardCopyOption.REPLACE_EXISTING);

			userSubmissionMedia.setFile(mediaFile);
			if (field.getMedia() != null) {
				field.setMedia(new ArrayList<>());
			}
			field.getMedia().add(userSubmissionMedia);
		} catch (IOException ex) {
			throw new OpenStorefrontRuntimeException("Unable to store media file.", "Contact System Admin.  Check file permissions and disk space ", ex);
		}
		userSubmission = saveUserSubmission(userSubmission);
		return userSubmission;
	}

	@Override
	public VerifySubmissionTemplateResult verifySubmission(UserSubmission userSubmission)
	{
		Objects.requireNonNull(userSubmission);

		VerifySubmissionTemplateResult verifySubmissionTemplateResult = new VerifySubmissionTemplateResult();

		SubmissionFormTemplate formTemplate = persistenceService.findById(SubmissionFormTemplate.class, userSubmission.getTemplateId());
		if (formTemplate != null) {
			try {
				ComponentFormSet componentFormSet = mappingController.mapUserSubmissionToEntry(formTemplate, userSubmission);
				ValidationResult validationResult = componentFormSet.validate(true);

				verifySubmissionTemplateResult.setComponentFormSet(componentFormSet);
				verifySubmissionTemplateResult.setValidationResult(validationResult);

				if (validationResult.valid()) {
					formTemplate.setTemplateStatus(SubmissionTemplateStatus.VERIFIED);
					formTemplate.populateBaseUpdateFields();
					persistenceService.persist(formTemplate);
				}

			} catch (MappingException ex) {
				LOG.log(Level.WARNING, "Failed to mapped user submisson");
				if (LOG.isLoggable(Level.FINE)) {
					LOG.log(Level.FINE, null, ex);
				}
			}

		} else {
			throw missingFormTemplateException(userSubmission.getTemplateId());
		}
		return verifySubmissionTemplateResult;
	}

	private OpenStorefrontRuntimeException missingFormTemplateException(String templateId)
	{
		return new OpenStorefrontRuntimeException("Unable to find form template. Template Id: " + templateId, "Check Data");
	}

	@Override
	public void submitUserSubmissionForApproval(UserSubmission userSubmission)
	{
		Objects.requireNonNull(userSubmission);

		SubmissionFormTemplate formTemplate = persistenceService.findById(SubmissionFormTemplate.class, userSubmission.getTemplateId());
		if (formTemplate != null) {
			try {
				ComponentFormSet componentFormSet = mappingController.mapUserSubmissionToEntry(formTemplate, userSubmission);

				componentFormSet.getPrimary().getComponent().setApprovalState(ApprovalStatus.PENDING);
				getComponentService().saveFullComponent(componentFormSet.getPrimary());
				for (ComponentAll componentAll : componentFormSet.getChildren()) {
					getComponentService().saveFullComponent(componentAll);
				}
				deleteUserSubmission(userSubmission.getUserSubmissionId());

			} catch (MappingException ex) {
				Logger.getLogger(SubmissionFormServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
			}

		} else {
			throw missingFormTemplateException(userSubmission.getTemplateId());
		}
	}

	@Override
	public UserSubmission editComponentForSubmission(String submissionTemplateId, String componentId)
	{
		Objects.isNull(componentId);

		UserSubmission userSubmission = null;
		SubmissionFormTemplate formTemplate = persistenceService.findById(SubmissionFormTemplate.class, submissionTemplateId);
		if (formTemplate != null) {
			ComponentFormSet componentFormSet = new ComponentFormSet();
			ComponentAll componentAll = getComponentService().getFullComponent(componentId);
			componentFormSet.setPrimary(componentAll);

			for (ComponentRelationship relationship : componentAll.getRelationships()) {
				componentFormSet.getChildren().add(getComponentService().getFullComponent(relationship.getRelatedComponentId()));
			}

			try {
				userSubmission = mappingController.mapEntriesToUserSubmission(formTemplate, componentFormSet);
			} catch (MappingException ex) {
				throw new OpenStorefrontRuntimeException("Unable to map entry to submission.", "Check error ticket/logs", ex);
			}

			saveUserSubmission(userSubmission);

		} else {
			throw missingFormTemplateException(submissionTemplateId);
		}

		return userSubmission;
	}

	@Override
	public void submitChangeRequestForApproval(UserSubmission userSubmission)
	{
		Objects.requireNonNull(userSubmission);
		Objects.requireNonNull(userSubmission.getOriginalComponentId());

		SubmissionFormTemplate formTemplate = persistenceService.findById(SubmissionFormTemplate.class, userSubmission.getTemplateId());
		if (formTemplate != null) {
			try {
				ComponentFormSet componentFormSet = mappingController.mapUserSubmissionToEntry(formTemplate, userSubmission);

				componentFormSet.getPrimary().getComponent().setApprovalState(ApprovalStatus.PENDING);
				getComponentService().saveFullComponent(componentFormSet.getPrimary());
				for (ComponentAll componentAll : componentFormSet.getChildren()) {
					getComponentService().saveFullComponent(componentAll);
				}
				getComponentService().submitChangeRequest(userSubmission.getOriginalComponentId());

				deleteUserSubmission(userSubmission.getUserSubmissionId());

			} catch (MappingException ex) {
				Logger.getLogger(SubmissionFormServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
			}

		} else {
			throw missingFormTemplateException(userSubmission.getTemplateId());
		}
	}

	@Override
	public void reassignUserSubmission(String userSubmissionId, String newOwnerUsername)
	{
		Objects.requireNonNull(userSubmissionId);

		UserSubmission existing = persistenceService.findById(UserSubmission.class, userSubmissionId);
		if (existing != null) {
			existing.setOwnerUsername(newOwnerUsername);
			existing.populateBaseUpdateFields();
			persistenceService.persist(existing);
		} else {
			throw new OpenStorefrontRuntimeException("Unable to find user submission. Id: " + userSubmissionId, "Check Id and refresh");
		}
	}

	@Override
	public void deleteUserSubmission(String userSubmissionId)
	{
		UserSubmission existing = persistenceService.findById(UserSubmission.class, userSubmissionId);
		if (existing != null) {

			if (existing.getFields() != null) {
				for (UserSubmissionField field : existing.getFields()) {
					handleMediaDelete(field);
				}
			}

			persistenceService.delete(existing);
		}
	}

	private void handleMediaDelete(UserSubmissionField field)
	{
		if (field.getMedia() != null) {
			for (UserSubmissionMedia media : field.getMedia()) {
				if (media.getFile() != null) {
					deleteSubmissionMedia(media.getFile());
				}
			}
		}
	}

	private void deleteSubmissionMedia(MediaFile mediaFile)
	{
		Path path = mediaFile.path();
		try {
			if (path != null
					&& Files.deleteIfExists(path)) {
				LOG.log(Level.WARNING, () -> MessageFormat.format("Unable to delete local media...unable to find it. Path: {0}", path));
			}
		} catch (IOException ex) {
			LOG.log(Level.WARNING, ex, () -> MessageFormat.format("Unable to delete local media...check permissions or it may be in use. Path: {0}", path));
		}
	}

	@Override
	public void deleteUserSubmissionMedia(String userSubmissionId, String mediaId)
	{
		UserSubmission existing = persistenceService.findById(UserSubmission.class, userSubmissionId);
		if (existing != null) {

			LockSwitch lockSwitch = new LockSwitch();
			if (existing.getFields() != null) {
				for (UserSubmissionField field : existing.getFields()) {
					lockSwitch.setSwitched(findMediaAndDelete(field, mediaId));
				}
			}

			if (lockSwitch.isSwitched()) {
				existing.populateBaseUpdateFields();
				persistenceService.persist(existing);
			}
		}

	}

	private boolean findMediaAndDelete(UserSubmissionField field, String mediaId)
	{
		boolean updated = false;
		if (field.getMedia() != null) {
			for (UserSubmissionMedia media : field.getMedia()) {
				if (media.getSubmissionMediaId().equals(mediaId)) {
					deleteSubmissionMedia(media.getFile());
					updated = true;
				}
			}
			field.getMedia().removeIf((media) -> {
				return media.getSubmissionMediaId().equals(mediaId);
			});
		}
		return updated;
	}

}