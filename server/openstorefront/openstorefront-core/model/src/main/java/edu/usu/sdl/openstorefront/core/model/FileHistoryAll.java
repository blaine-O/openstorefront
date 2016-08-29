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
package edu.usu.sdl.openstorefront.core.model;

import edu.usu.sdl.openstorefront.core.entity.FileHistory;
import edu.usu.sdl.openstorefront.core.entity.FileHistoryError;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author dshurtleff
 */
public class FileHistoryAll
{

	private FileHistory fileHistory;
	private DataMapModel dataMapModel;
	private List<FileHistoryError> errors = new ArrayList<>();

	public FileHistoryAll()
	{
	}

	public void addError(String type, String error)
	{
		addError(type, error, null);
	}

	public void addError(String type, String error, Integer recordNumber)
	{
		FileHistoryError fileHistoryError = new FileHistoryError();
		fileHistoryError.setFileHistoryErrorType(type);
		fileHistoryError.setErrorMessage(error);
		fileHistoryError.setRecordNumber(recordNumber);
		errors.add(fileHistoryError);
	}

	public FileHistory getFileHistory()
	{
		return fileHistory;
	}

	public void setFileHistory(FileHistory fileHistory)
	{
		this.fileHistory = fileHistory;
	}

	public List<FileHistoryError> getErrors()
	{
		return errors;
	}

	public void setErrors(List<FileHistoryError> errors)
	{
		this.errors = errors;
	}

	public DataMapModel getDataMapModel()
	{
		return dataMapModel;
	}

	public void setDataMapModel(DataMapModel dataMapModel)
	{
		this.dataMapModel = dataMapModel;
	}

}
