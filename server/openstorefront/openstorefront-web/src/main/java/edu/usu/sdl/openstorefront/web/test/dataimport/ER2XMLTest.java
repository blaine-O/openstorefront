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
package edu.usu.sdl.openstorefront.web.test.dataimport;

import edu.usu.sdl.openstorefront.common.manager.FileSystemManager;
import edu.usu.sdl.openstorefront.core.entity.DataSource;
import edu.usu.sdl.openstorefront.core.entity.FileFormat;
import edu.usu.sdl.openstorefront.core.entity.FileHistory;
import edu.usu.sdl.openstorefront.core.entity.FileHistoryOption;
import edu.usu.sdl.openstorefront.core.model.ImportContext;
import edu.usu.sdl.openstorefront.web.test.BaseTestCase;

/**
 *
 * @author dshurtleff
 */
public class ER2XMLTest
		extends BaseTestCase
{

	public ER2XMLTest()
	{
		this.description = "ER2XML Test";
	}

	@Override
	protected void runInternalTest()
	{
		ImportContext importContext = new ImportContext();
		importContext.setInput(FileSystemManager.getApplicationResourceFile("/data/test/assettest.xml"));

		FileHistory fileHistory = new FileHistory();
		fileHistory.setMimeType("application/xml");
		fileHistory.setDataSource(DataSource.ER2);
		fileHistory.setOriginalFilename("assettest.xml");
		fileHistory.setFileFormat(FileFormat.COMPONENT_ER2);
		fileHistory.setFileHistoryOption(new FileHistoryOption());
		importContext.getFileHistoryAll().setFileHistory(fileHistory);

		service.getImportService().importData(importContext);

	}

}
