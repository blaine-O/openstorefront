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
package edu.usu.sdl.openstorefront.selenium.apitestclient;

import edu.usu.sdl.apiclient.ClientAPI;
import edu.usu.sdl.apiclient.rest.resource.AttributeClient;
import edu.usu.sdl.openstorefront.core.entity.AttributeCode;
import edu.usu.sdl.openstorefront.core.entity.AttributeCodePk;
import edu.usu.sdl.openstorefront.core.entity.AttributeType;
import edu.usu.sdl.openstorefront.core.view.AttributeTypeSave;
import edu.usu.sdl.openstorefront.core.view.FilterQueryParams;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ccummings
 */
public class AttributeTestClient extends BaseTestClient
{

	private AttributeClient apiAttribute;
	private static List<String> attributeIDs = new ArrayList<>();
	
	public AttributeTestClient(ClientAPI client, APIClient apiClient)
	{
		super(client, apiClient);
		apiAttribute = new AttributeClient(client);
	}
	
	public AttributeType createAPIAttribute(String attributeType, String attrDefaultCode, String codeLabel)
	{
		AttributeType type = new AttributeType();
		type.setAttributeType(attributeType);
		type.setDescription("The Star Betelgeuse");
		type.setVisibleFlg(Boolean.TRUE);
		type.setImportantFlg(Boolean.TRUE);
		type.setRequiredFlg(Boolean.TRUE);
		type.setDefaultAttributeCode(attrDefaultCode);
		AttributeTypeSave attributeTypeSave = new AttributeTypeSave();
		attributeTypeSave.setAttributeType(type);
		
		AttributeType apiAttrType = apiAttribute.postAttributeType(attributeTypeSave);
		addAttributeCode(apiAttrType.getAttributeType(), attrDefaultCode, codeLabel);
		attributeIDs.add(apiAttrType.getAttributeType());
		
		return apiAttrType;
	}
	
	public void deleteAPIAttribute(String type)
	{
		apiAttribute.hardDeleteAttributeType(type);
	}
	
	public List<AttributeType> getReqAttributeTypes(String componentType)
	{
		return apiAttribute.getRequiredAttributeTypes(componentType);
	}
	
	public AttributeCode addAttributeCode(String attributeType, String codeLabel, String code)
	{	
		AttributeType type = apiAttribute.getAttributeTypeById(attributeType, false, false);
		
		AttributeCodePk codePk = new AttributeCodePk();
		codePk.setAttributeCode(code);
		codePk.setAttributeType(type.getAttributeType());
		
		AttributeCode attrCode = new AttributeCode();
		attrCode.setLabel(codeLabel);
		attrCode.setAttributeCodePk(codePk);
		
		return apiAttribute.postAttributeCode(attributeType, attrCode);
	}
	
	public List<AttributeCode> getListAttributeCodes(String attrType, FilterQueryParams params)
	{
		return apiAttribute.getAttributeCodes(attrType, params);
	}

	@Override
	public void cleanup()
	{
		for (String id : attributeIDs) {
			deleteAPIAttribute(id);
		}
	}	
}
