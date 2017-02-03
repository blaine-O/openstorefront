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

import edu.usu.sdl.openstorefront.common.util.OpenStorefrontConstant;
import edu.usu.sdl.openstorefront.core.annotation.APIDescription;
import edu.usu.sdl.openstorefront.core.annotation.ConsumeField;
import edu.usu.sdl.openstorefront.core.annotation.DataType;
import edu.usu.sdl.openstorefront.core.annotation.PK;
import java.util.List;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author dshurtleff
 */
@APIDescription("Holds the security role information")
public class SecurityRole
		extends StandardEntity<SecurityRole>
{
	public static final String DEFAULT_GROUP = "DEFAULT-GROUP";	

	@PK(generated = false)
	@NotNull
	@ConsumeField
	@APIDescription("Should match LDAP group if use external IDAM")
	private String roleName;

	@NotNull
	@ConsumeField
	@Size(min = 1, max = OpenStorefrontConstant.FIELD_SIZE_255)
	private String description;

	@NotNull
	@ConsumeField
	@Size(min = 1, max = OpenStorefrontConstant.FIELD_SIZE_255)
	private String landingPage;

	@ConsumeField
	@DataType(SecurityRolePermission.class)
	@OneToMany(orphanRemoval = true)
	private List<SecurityRolePermission> permissions;

	@ConsumeField
	@DataType(SecurityRoleData.class)
	@OneToMany(orphanRemoval = true)
	private List<SecurityRoleData> dataSecurity;

	@NotNull
	@ConsumeField
	private Boolean allowUnspecifiedDataSource;

	@NotNull
	@ConsumeField
	private Boolean allowUnspecifiedDataSensitivity;

	public SecurityRole()
	{
	}

	@Override
	public <T extends StandardEntity> void updateFields(T entity)
	{
		super.updateFields(entity); 
		
		SecurityRole securityRole = (SecurityRole) entity;
		setRoleName(securityRole.getRoleName());
		setDescription(securityRole.getDescription());
		setLandingPage(securityRole.getLandingPage());
		setDataSecurity(securityRole.getDataSecurity());
		setPermissions(securityRole.getPermissions());
		setAllowUnspecifiedDataSensitivity(securityRole.getAllowUnspecifiedDataSensitivity());
		setAllowUnspecifiedDataSource(securityRole.getAllowUnspecifiedDataSource());
				
	}
	
	public String getRoleName()
	{
		return roleName;
	}

	public void setRoleName(String roleName)
	{
		this.roleName = roleName;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getLandingPage()
	{
		return landingPage;
	}

	public void setLandingPage(String landingPage)
	{
		this.landingPage = landingPage;
	}

	public List<SecurityRolePermission> getPermissions()
	{
		return permissions;
	}

	public void setPermissions(List<SecurityRolePermission> permissions)
	{
		this.permissions = permissions;
	}

	public List<SecurityRoleData> getDataSecurity()
	{
		return dataSecurity;
	}

	public void setDataSecurity(List<SecurityRoleData> dataSecurity)
	{
		this.dataSecurity = dataSecurity;
	}

	public Boolean getAllowUnspecifiedDataSource()
	{
		return allowUnspecifiedDataSource;
	}

	public void setAllowUnspecifiedDataSource(Boolean allowUnspecifiedDataSource)
	{
		this.allowUnspecifiedDataSource = allowUnspecifiedDataSource;
	}

	public Boolean getAllowUnspecifiedDataSensitivity()
	{
		return allowUnspecifiedDataSensitivity;
	}

	public void setAllowUnspecifiedDataSensitivity(Boolean allowUnspecifiedDataSensitivity)
	{
		this.allowUnspecifiedDataSensitivity = allowUnspecifiedDataSensitivity;
	}

}
