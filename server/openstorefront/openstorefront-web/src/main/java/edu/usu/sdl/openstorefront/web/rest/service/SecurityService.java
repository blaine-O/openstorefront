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
package edu.usu.sdl.openstorefront.web.rest.service;

import edu.usu.sdl.openstorefront.common.exception.OpenStorefrontRuntimeException;
import edu.usu.sdl.openstorefront.common.manager.FileSystemManager;
import edu.usu.sdl.openstorefront.core.annotation.APIDescription;
import edu.usu.sdl.openstorefront.core.annotation.DataType;
import edu.usu.sdl.openstorefront.core.entity.SecurityPermission;
import edu.usu.sdl.openstorefront.core.entity.UserSecurity;
import edu.usu.sdl.openstorefront.core.view.GenericDataView;
import edu.usu.sdl.openstorefront.core.view.RestErrorModel;
import edu.usu.sdl.openstorefront.core.view.UserCredential;
import edu.usu.sdl.openstorefront.doc.security.RequireSecurity;
import edu.usu.sdl.openstorefront.validation.ValidationResult;
import edu.usu.sdl.openstorefront.web.rest.resource.BaseResource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author dshurtleff
 */
@Path("v1/service/security")
@APIDescription("Provides distinct end-point that can be open for user-control security operations")
public class SecurityService
	extends BaseResource
{
	private static final Logger LOG = Logger.getLogger(SecurityService.class.getName());
	
	
	@GET
	@APIDescription("Gets the shiro config.")
	@RequireSecurity(SecurityPermission.ADMIN_SECURITY)
	@Produces({MediaType.TEXT_PLAIN})
	@Path("/shiroconfig")
	public Response getShiroConfig() 
	{
		File shiroConfig = FileSystemManager.getConfig("shiro.ini");
		
		String config = "";
		try {
			byte data[] = Files.readAllBytes(shiroConfig.toPath());
			config = new String(data);
		} catch (IOException ex) {
			LOG.log(Level.WARNING, "Unable to load shiro config", ex);
			Response.status(Response.Status.NOT_FOUND).build();
		}
		return Response.ok(config).build();
	}

	@PUT
	@RequireSecurity(SecurityPermission.ADMIN_SECURITY)	
	@APIDescription("Saves shiro config")
	@Consumes({MediaType.APPLICATION_JSON})
	@Path("/shiroconfig")
	public Response saveShiroConfig(
			GenericDataView dataView	
	) 
	{
		File shiroConfig = FileSystemManager.getConfig("shiro.ini");
		
		try {
			//backup the original 
			Files.copy(shiroConfig.toPath(), 
					Paths.get(FileSystemManager.CONFIG_DIR + "/shiro.ini.back"), 
					StandardCopyOption.REPLACE_EXISTING, 					
					StandardCopyOption.COPY_ATTRIBUTES);
			
			//write changes
			Files.write(shiroConfig.toPath(), dataView.getData().getBytes(), StandardOpenOption.CREATE);
		} catch (IOException ex) {
			throw new OpenStorefrontRuntimeException("Unable to save shiro.ini file.",  "Check system permission and disk space.", ex);
		}
		return Response.ok().build();
	}
	
	@PUT	
	@APIDescription("Allows a user to reset their password.")	
	@Produces({MediaType.TEXT_PLAIN})
	@Consumes({MediaType.TEXT_PLAIN})
	@Path("/{username}/resetpassword")
	public Response resetUserPassword(
		@PathParam("username") String username,
		UserCredential userCredential
	)
	{
		UserSecurity userSecurity = new UserSecurity();
		userSecurity.setUsername(username);
		userSecurity = userSecurity.find();
		if (userSecurity != null) {
			String approvalCode = service.getSecurityService().resetPasswordUser(username, userCredential.getPassword());
			return Response.ok(approvalCode).build();
		}	
		return Response.status(Response.Status.NOT_FOUND).build();		
	}	
	
	@PUT	
	@APIDescription("Allows a user to approve their new password.")	
	@Produces({MediaType.TEXT_PLAIN})
	@Path("/approveResetPassword/{approvalcode}")
	public Response approvePasswordReset(		
		@PathParam("approvalcode") String approvalcode	
	)
	{	
		boolean successful =  service.getSecurityService().approveUserPasswordReset(approvalcode);
		return Response.ok(Boolean.toString(successful)).build();		
	}	

	@POST
	@APIDescription("Allows a user to approve their new password.")	
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes({MediaType.TEXT_PLAIN})
	@DataType(RestErrorModel.class)
	@Path("/checkPassword")
	public Response checkPassword(
		char[] password	
	)
	{
		ValidationResult result = service.getSecurityService().validatePassword(password);
		RestErrorModel restErrorModel = result.toRestError();
		return sendSingleEntityResponse(restErrorModel);
	}
	
}
