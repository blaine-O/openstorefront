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
package edu.usu.sdl.openstorefront.service.io.parser;

import edu.usu.sdl.openstorefront.core.model.AttributeAll;
import edu.usu.sdl.openstorefront.core.spi.parser.BaseAttributeParser;
import edu.usu.sdl.openstorefront.core.spi.parser.mapper.AttributeMapper;
import edu.usu.sdl.openstorefront.core.spi.parser.mapper.MapModel;
import edu.usu.sdl.openstorefront.core.spi.parser.reader.CSVMapReader;
import edu.usu.sdl.openstorefront.core.spi.parser.reader.GenericReader;
import java.io.InputStream;

/**
 *
 * @author dshurtleff
 */
public class AttributeMappedTSVParser
		extends BaseAttributeParser
{

	@Override
	public String checkFormat(String mimeType, InputStream input)
	{
		if (mimeType.contains("tsv")
				|| mimeType.contains("application/octet-stream")
				|| mimeType.contains("excel")
				|| mimeType.contains("text")) {
			return "";
		} else {
			return "Invalid format. Please upload a TSV file.";
		}
	}

	@Override
	protected GenericReader getReader(InputStream in)
	{
		CSVMapReader reader = new CSVMapReader(in);
		reader.setSeparator('\t');
		return reader;
	}

	@Override
	protected <T> Object parseRecord(T record)
	{
		MapModel mapModel = (MapModel) record;

		AttributeMapper attributeMapper = new AttributeMapper(() -> {
			AttributeAll attributeAll = defaultAttributeAll();
			return attributeAll;
		}, fileHistoryAll);

		AttributeAll attributeAll = attributeMapper.singleMapData(mapModel);
		return attributeAll;
	}
}
