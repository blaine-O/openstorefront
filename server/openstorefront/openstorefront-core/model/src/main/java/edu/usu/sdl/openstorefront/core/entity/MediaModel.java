/*
 * Copyright 2017 Space Dynamics Laboratory - Utah State University Research Foundation.
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

/**
 *
 * @author cyearsley
 */

public interface MediaModel
{
	/**
	 * Saves a MediaFile and sets the MediaFile
	 *
	 * @param mediaFile
	 */
	public void setFile(MediaFile mediaFile);
	
	/**
	 * Gets the appropriate MediaFile
	 * @return the components mediaFile
	 */
	public MediaFile getFile();
	
	
	/**
	 * Saves a MediaFile and sets the MediaFile
	 *
	 * @param fileName
	 * @deprecated 
	 */
	public void setOriginalName(String fileName);
	
	/**
	 * Saves a MediaFile and sets the MediaFile
	 *
	 * @param fileName
	 * @deprecated 
	 */
	public void setFileName(String fileName);
	
	/**
	 * Saves a MediaFile and sets the MediaFile
	 *
	 * @param mimeType
	 * @deprecated 
	 */
	public void setMimeType(String mimeType);
	
}