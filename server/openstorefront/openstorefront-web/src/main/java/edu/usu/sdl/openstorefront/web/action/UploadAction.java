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
package edu.usu.sdl.openstorefront.web.action;

import au.com.bytecode.opencsv.CSVReader;
import com.fasterxml.jackson.core.type.TypeReference;
import edu.usu.sdl.openstorefront.common.exception.OpenStorefrontRuntimeException;
import edu.usu.sdl.openstorefront.common.manager.FileSystemManager;
import edu.usu.sdl.openstorefront.common.util.StringProcessor;
import edu.usu.sdl.openstorefront.core.api.model.TaskRequest;
import edu.usu.sdl.openstorefront.core.entity.AttributeCode;
import edu.usu.sdl.openstorefront.core.entity.AttributeType;
import edu.usu.sdl.openstorefront.core.entity.FileHistory;
import edu.usu.sdl.openstorefront.core.entity.FileHistoryOption;
import edu.usu.sdl.openstorefront.core.entity.LookupEntity;
import edu.usu.sdl.openstorefront.core.model.ComponentAll;
import edu.usu.sdl.openstorefront.core.model.FileFormatCheck;
import edu.usu.sdl.openstorefront.core.model.ImportContext;
import edu.usu.sdl.openstorefront.security.SecurityUtil;
import edu.usu.sdl.openstorefront.service.io.parser.MainAttributeParser;
import edu.usu.sdl.openstorefront.service.io.parser.OldBaseAttributeParser;
import edu.usu.sdl.openstorefront.service.io.parser.SvcAttributeParser;
import edu.usu.sdl.openstorefront.service.manager.DBManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletResponse;
import net.java.truevfs.access.TFile;
import net.java.truevfs.access.TFileInputStream;
import net.java.truevfs.access.TVFS;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.validation.Validate;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author dshurtleff
 */
public class UploadAction
		extends BaseAction
{

	private static final Logger log = Logger.getLogger(UploadAction.class.getName());

	@Validate(required = true, on = {
		"UploadLookup",
		"UploadComponent",
		"UploadArticles",
		"UploadAttributes",
		"UploadSvcv4",
		"UploadPlugin",
		"ImportData"
	})
	private FileBean uploadFile;

	@Validate(required = true, on = "UploadLookup")
	private String entityName;

	private FileHistoryOption componentUploadOptions = new FileHistoryOption();

	@Validate(required = true, on = "ImportData")
	private String fileType;

	@Validate(required = true, on = "ImportData")
	private String fileFormat;
	private String dataMappingId;
	private String dataSource;

	@HandlesEvent("UploadLookup")
	public Resolution uploadLookup()
	{
		Map<String, String> errors = new HashMap<>();
		if (SecurityUtil.isAdminUser()) {
			//log
			log.log(Level.INFO, MessageFormat.format("(Admin) Uploading lookup: {0}", uploadFile));

			//check content type
			Set<String> allowTypes = new HashSet<>();
			allowTypes.add("text/csv");
			allowTypes.add("application/vnd.ms-excel");
			allowTypes.add("application/vnd.oasis.opendocument.spreadsheet");

			if (allowTypes.contains(uploadFile.getContentType()) == false) {
				errors.put("uploadFile", "Format not supported.  Requires a csv text file.");
			}

			if (errors.isEmpty()) {
				//parse
				List<LookupEntity> lookupEntities = new ArrayList<>();

				Class lookupClass = null;
				StringBuilder errorsMessages = new StringBuilder();
				try (CSVReader reader = new CSVReader(new InputStreamReader(uploadFile.getInputStream()))) {

					lookupClass = Class.forName(DBManager.ENTITY_MODEL_PACKAGE + "." + entityName);
					List<String[]> allData = reader.readAll();
					for (String data[] : allData) {
						try {
							LookupEntity lookupEntity = (LookupEntity) lookupClass.newInstance();
							lookupEntity.importData(data);
							lookupEntities.add(lookupEntity);
						} catch (Exception e) {
							errorsMessages.append(MessageFormat.format(e.toString() + " -  Unable Process line: {0}", new Object[]{Arrays.toString(data)}));
						}
					}
					if (errorsMessages.length() > 0) {
						errors.put("uploadFile", errorsMessages.toString());
					}
				} catch (IOException ex) {
					throw new OpenStorefrontRuntimeException("Unable to read file: " + uploadFile.getFileName(), ex);
				} catch (ClassNotFoundException ex) {
					errors.put("entityName", "Unable to find Lookup Class:  " + entityName);
				} finally {
					try {
						uploadFile.delete();
					} catch (IOException ex) {
						throw new OpenStorefrontRuntimeException(ex);
					}
				}

				if (errors.isEmpty()) {
					if (lookupClass != null) {
						service.getLookupService().syncLookupImport(lookupClass, lookupEntities);
					}
				}
			}
			return streamUploadResponse(errors);
		} else {
			return new ErrorResolution(HttpServletResponse.SC_FORBIDDEN, "Access denied");
		}
	}

	@HandlesEvent("UploadAttributes")
	public Resolution uploadAttribute()
	{
		return handleAttributeUpload(new MainAttributeParser());
	}

	private Resolution handleAttributeUpload(OldBaseAttributeParser parser)
	{
		Map<String, String> errors = new HashMap<>();
		if (SecurityUtil.isAdminUser()) {
			//log
			log.log(Level.INFO, MessageFormat.format("(Admin) Uploading attributes: {0}", uploadFile));

			//check content type
			Set<String> allowSvcTypes = new HashSet<>();
			allowSvcTypes.add("text/csv");
			allowSvcTypes.add("application/vnd.ms-excel");
			allowSvcTypes.add("application/vnd.oasis.opendocument.spreadsheet");

			Set<String> allowAttrbuteTypes = new HashSet<>();
			allowAttrbuteTypes.add("text/json");
			allowAttrbuteTypes.add("application/json");

			String bestGuessContentType = uploadFile.getContentType();
			if (parser instanceof SvcAttributeParser) {
				if (allowSvcTypes.contains(bestGuessContentType) == false) {

					bestGuessContentType = getContext().getServletContext().getMimeType(uploadFile.getFileName());
					if (allowSvcTypes.contains(bestGuessContentType) == false) {
						errors.put("uploadFile", "Format not supported.  Requires a CSV text file.");
					}
				}
			} else if (allowAttrbuteTypes.contains(bestGuessContentType) == false) {

				bestGuessContentType = getContext().getServletContext().getMimeType(uploadFile.getFileName());
				if (allowAttrbuteTypes.contains(bestGuessContentType) == false) {
					errors.put("uploadFile", "Format not supported.  Requires a JSON text file.");
				}
			}

			if (errors.isEmpty()) {

				try (InputStream in = uploadFile.getInputStream()) {
					Map<AttributeType, List<AttributeCode>> attributeMap = parser.parse(in);

					TaskRequest taskRequest = new TaskRequest();
					taskRequest.setAllowMultiple(false);
					taskRequest.setName("Uploading Attribute(s)");
					taskRequest.setDetails("File name: " + uploadFile.getFileName());
					service.getAsyncProxy(service.getAttributeService(), taskRequest).syncAttribute(attributeMap);
				} catch (IOException ex) {
					throw new OpenStorefrontRuntimeException("Unable to read file: " + uploadFile.getFileName(), ex);
				} finally {
					try {
						uploadFile.delete();
					} catch (IOException ex) {
						throw new OpenStorefrontRuntimeException(ex);
					}
				}
			}
			return streamUploadResponse(errors);
		} else {
			return new ErrorResolution(HttpServletResponse.SC_FORBIDDEN, "Access denied");
		}
	}

	@HandlesEvent("UploadSvcv4")
	public Resolution uploadSvcv4()
	{
		return handleAttributeUpload(new SvcAttributeParser());
	}

	@HandlesEvent("UploadComponent")
	public Resolution uploadComponent()
	{
		Map<String, String> errors = new HashMap<>();
		if (SecurityUtil.isAdminUser()) {
			log.log(Level.INFO, SecurityUtil.adminAuditLogMessage(getContext().getRequest()));

			try {
				Set<String> allowTextTypes = new HashSet<>();
				Set<String> allowZipTypes = new HashSet<>();
				allowTextTypes.add("text/json");
				allowTextTypes.add("text");
				allowTextTypes.add("application/json");
				allowZipTypes.add("application/zip");
				allowZipTypes.add("application/x-zip-compressed");

				//This comes from the client
				String bestGuessContentType = uploadFile.getContentType();
				if (allowTextTypes.contains(bestGuessContentType) == false
						&& allowZipTypes.contains(bestGuessContentType) == false) {

					//check extentsion (this comes from the server config...based on the file extension)
					bestGuessContentType = getContext().getServletContext().getMimeType(uploadFile.getFileName());
					if (allowTextTypes.contains(bestGuessContentType) == false
							&& allowZipTypes.contains(bestGuessContentType) == false) {
						errors.put("uploadFile", "Format not supported.  Requires json text file or zip file");
					}
				}

				if (errors.isEmpty()) {
					List<ComponentAll> components = new ArrayList<>();
					if (allowTextTypes.contains(bestGuessContentType)) {
						try (InputStream in = uploadFile.getInputStream()) {
							components = StringProcessor.defaultObjectMapper().readValue(in, new TypeReference<List<ComponentAll>>()
							{
							});
						} catch (IOException ex) {
							throw ex;
						}
					} else {
						//zip handling
						File tempFile = new File(FileSystemManager.getDir(FileSystemManager.SYSTEM_TEMP_DIR) + "/" + System.currentTimeMillis() + "-Temp.zip");
						uploadFile.save(tempFile);
						TFile archive = new TFile(tempFile.getPath());
						for (TFile file : archive.listFiles()) {
							if (file.isFile()) {
								try (InputStream in = new TFileInputStream(file)) {
									components = StringProcessor.defaultObjectMapper().readValue(in, new TypeReference<List<ComponentAll>>()
									{
									});
								} catch (IOException ex) {
									throw ex;
								}
							} else if (file.isDirectory() && "media".equalsIgnoreCase(file.getName())) {
								for (TFile mediaFile : file.listFiles()) {
									Files.copy(mediaFile.toPath(), FileSystemManager.getDir(FileSystemManager.MEDIA_DIR).toPath().resolve(mediaFile.getName()), StandardCopyOption.REPLACE_EXISTING);
								}
							} else if (file.isDirectory() && "resources".equalsIgnoreCase(file.getName())) {
								for (TFile resourceFile : file.listFiles()) {
									Files.copy(resourceFile.toPath(), FileSystemManager.getDir(FileSystemManager.RESOURCE_DIR).toPath().resolve(resourceFile.getName()), StandardCopyOption.REPLACE_EXISTING);
								}
							}
						}

						//cleanup temp zip
						if (tempFile.delete() == false) {
							log.log(Level.WARNING, MessageFormat.format("Unable to remove temp upload file.  It can be safely removed from: {0}", tempFile.getPath()));
						}
					}

					TaskRequest taskRequest = new TaskRequest();
					taskRequest.setAllowMultiple(false);
					taskRequest.setName("Uploading Component(s)");
					taskRequest.setDetails("Component(s) Processing: " + components.size() + " from Filename: " + uploadFile.getFileName());
					service.getAsyncProxy(service.getComponentService(), taskRequest).importComponents(components, componentUploadOptions);
				}
			} catch (IOException ex) {
				log.log(Level.FINE, "Unable to read file: " + uploadFile.getFileName(), ex);
				errors.put("uploadFile", "Unable to read file: " + uploadFile.getFileName() + " Make sure the file in the proper format.");
			} finally {
				try {
					TVFS.umount();
				} catch (IOException ex) {
					log.log(Level.WARNING, "Unable to unmount tvfs");
				}

				try {
					uploadFile.delete();
				} catch (IOException ex) {
					log.log(Level.WARNING, "Unable to remove temp upload file.", ex);
				}
			}
			return streamUploadResponse(errors);
		}
		return new ErrorResolution(HttpServletResponse.SC_FORBIDDEN, "Access denied");
	}

	@HandlesEvent("UploadPlugin")
	public Resolution uploadPlugin()
	{
		Map<String, String> errors = new HashMap<>();
		if (SecurityUtil.isAdminUser()) {
			log.log(Level.INFO, SecurityUtil.adminAuditLogMessage(getContext().getRequest()));
			try {
				//just copy plugin to  plugin directory...to avoid double pickup
				File pluginDir = FileSystemManager.getDir(FileSystemManager.PLUGIN_DIR);
				uploadFile.save(new File(pluginDir + "/" + StringProcessor.cleanFileName(uploadFile.getFileName())));
			} catch (IOException ex) {
				log.log(Level.FINE, "Unable to read file: " + uploadFile.getFileName(), ex);
				errors.put("uploadFile", "Unable to read file: " + uploadFile.getFileName() + " Make sure the file in the proper format.");
			} finally {
				try {
					if (uploadFile != null) {
						uploadFile.delete();
					}
				} catch (IOException ex) {
					log.log(Level.WARNING, "Unable to remove temp upload file.", ex);
				}
			}
			return streamUploadResponse(errors);
		}
		return new ErrorResolution(HttpServletResponse.SC_FORBIDDEN, "Access denied");
	}

	@HandlesEvent("ImportData")
	public Resolution importData()
	{
		Map<String, String> errors = new HashMap<>();
		if (SecurityUtil.isAdminUser()) {
			log.log(Level.INFO, SecurityUtil.adminAuditLogMessage(getContext().getRequest()));

			File tempFile = null;
			try {
				tempFile = File.createTempFile("import-", "tmp", FileSystemManager.getDir(FileSystemManager.MAIN_TEMP_DIR));
				uploadFile.save(tempFile);

				try (InputStream input = new FileInputStream(tempFile)) {
					FileFormatCheck fileFormatCheck = new FileFormatCheck();
					fileFormatCheck.setMimeType(uploadFile.getContentType());
					fileFormatCheck.setFileFormat(fileFormat);
					fileFormatCheck.setInput(input);

					String formatError = service.getImportService().checkFormat(fileFormatCheck);
					if (StringUtils.isNotBlank(formatError)) {
						errors.put("uploadFile", formatError);
					}
				} catch (IOException ioe) {
					throw ioe;
				}

				if (errors.isEmpty()) {
					ImportContext importContext = new ImportContext();
					importContext.setInput(new FileInputStream(tempFile));

					FileHistory fileHistory = new FileHistory();
					fileHistory.setMimeType(uploadFile.getContentType());
					if (StringUtils.isNotBlank(dataSource)) {
						fileHistory.setDataSource(dataSource);
					}
					fileHistory.setOriginalFilename(uploadFile.getFileName());
					fileHistory.setFileFormat(fileFormat);
					fileHistory.setFileHistoryOption(componentUploadOptions);
					importContext.getFileHistoryAll().setFileHistory(fileHistory);

					service.getImportService().importData(importContext);
				}

			} catch (IOException ex) {
				log.log(Level.FINE, "Unable to read file: " + uploadFile.getFileName(), ex);
				errors.put("uploadFile", "Unable to read file: " + uploadFile.getFileName() + " Make sure the file in the proper format.");
			} finally {
				try {
					if (uploadFile != null) {
						uploadFile.delete();
					}
					if (tempFile != null) {
						tempFile.delete();
					}
				} catch (IOException ex) {
					log.log(Level.WARNING, "Unable to remove temp upload file.", ex);
				}
			}

			return streamUploadResponse(errors);
		}
		return new ErrorResolution(HttpServletResponse.SC_FORBIDDEN, "Access denied");
	}

	public FileBean getUploadFile()
	{
		return uploadFile;
	}

	public void setUploadFile(FileBean uploadFile)
	{
		this.uploadFile = uploadFile;
	}

	public String getEntityName()
	{
		return entityName;
	}

	public void setEntityName(String entityName)
	{
		this.entityName = entityName;
	}

	public FileHistoryOption getComponentUploadOptions()
	{
		return componentUploadOptions;
	}

	public void setComponentUploadOptions(FileHistoryOption componentUploadOptions)
	{
		this.componentUploadOptions = componentUploadOptions;
	}

	public String getFileType()
	{
		return fileType;
	}

	public void setFileType(String fileType)
	{
		this.fileType = fileType;
	}

	public String getFileFormat()
	{
		return fileFormat;
	}

	public void setFileFormat(String fileFormat)
	{
		this.fileFormat = fileFormat;
	}

	public String getDataMappingId()
	{
		return dataMappingId;
	}

	public void setDataMappingId(String dataMappingId)
	{
		this.dataMappingId = dataMappingId;
	}

	public String getDataSource()
	{
		return dataSource;
	}

	public void setDataSource(String dataSource)
	{
		this.dataSource = dataSource;
	}

}
