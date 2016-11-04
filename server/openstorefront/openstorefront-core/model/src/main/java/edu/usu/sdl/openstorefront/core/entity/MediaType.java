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
package edu.usu.sdl.openstorefront.core.entity;

import edu.usu.sdl.openstorefront.core.annotation.APIDescription;

/**
 *
 * @author jlaw
 */
@APIDescription("Media Types: Video, Image, Text, etc")
public class MediaType
		extends LookupEntity<MediaType>
{

	public static final String IMAGE = "IMG";
	public static final String VIDEO = "VID";
	public static final String TEXT = "TEX";
	public static final String AUDIO = "AUD";
	public static final String ARCHIVE = "ARC";
	public static final String OTHER = "OTH";

	public MediaType()
	{
	}

}
