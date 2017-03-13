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
package edu.usu.sdl.openstorefront.security;

import edu.usu.sdl.openstorefront.doc.security.CustomRequireHandler;
import edu.usu.sdl.openstorefront.doc.security.RequireSecurity;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;

/**
 * Special handling for user profile checking to make sure the current user can
 * access their resources.
 *
 * @author dshurtleff
 */
public class UserProfileRequireHandler
		implements CustomRequireHandler
{

	public static final String USERNAME_ID_PARAM = "id";

	@Override
	public String getDescription()
	{
		return "Allow current user";
	}

	@Override
	public boolean specialSecurityCheck(ResourceInfo resourceInfo, ContainerRequestContext requestContext, RequireSecurity requireSecurity)
	{
		boolean doAdminCheck = true;
		String useridPassIn = requestContext.getUriInfo().getPathParameters().getFirst(USERNAME_ID_PARAM);
		if (SecurityUtil.getCurrentUserName().equals(useridPassIn)) {
			doAdminCheck = false;
		}
		return doAdminCheck;
	}

}
