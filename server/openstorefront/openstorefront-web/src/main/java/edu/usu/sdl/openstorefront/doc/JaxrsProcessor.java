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

package edu.usu.sdl.openstorefront.doc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import edu.usu.sdl.openstorefront.util.StringProcessor;
import edu.usu.sdl.openstorefront.web.rest.RestConfiguration;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author dshurtleff
 */
public class JaxrsProcessor
{
	private static final Logger log = Logger.getLogger(JaxrsProcessor.class.getName());

	private JaxrsProcessor()
	{
	}
	
	public static APIResourceModel processRestClass(Class resource)
	{
		APIResourceModel resourceModel = new APIResourceModel();
		
		resourceModel.setClassName(resource.getName());
		resourceModel.setResourceName(String.join(" ", StringUtils.splitByCharacterTypeCamelCase(resource.getSimpleName())));
		
		APIDescription aPIDescription = (APIDescription) resource.getAnnotation(APIDescription.class);
		if (aPIDescription != null)
		{
			resourceModel.setResourceDescription(aPIDescription.value());
		}
		
		//base path
		ApplicationPath basePath = (ApplicationPath) RestConfiguration.class.getAnnotation(ApplicationPath.class);
		
		
		Path path = (Path) resource.getAnnotation(Path.class);
		if (path != null)
		{
			resourceModel.setResourcePath(basePath.value() + "/" + path.value());
		}		
		
		RequireAdmin requireAdmin = (RequireAdmin) resource.getAnnotation(RequireAdmin.class);
		if (requireAdmin != null)
		{
			resourceModel.setRequireAdmin(true);			
		}
		
		
		//class parameters		
		mapParameters(resourceModel.getResourceParams(), resource.getDeclaredFields());
		
		//methods
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		int methodId = 0;
		for (Method method : resource.getDeclaredMethods())
		{

			APIMethodModel methodModel = new APIMethodModel();
			methodModel.setId(methodId++);
						
			//rest method
			List<String> restMethods = new ArrayList<>();			
			GET getMethod = (GET) method.getAnnotation(GET.class);
			POST postMethod = (POST) method.getAnnotation(POST.class);
			PUT putMethod = (PUT) method.getAnnotation(PUT.class);
			DELETE deleteMethod = (DELETE) method.getAnnotation(DELETE.class);
			if (getMethod != null)
			{
				restMethods.add("GET");
			}
			if (postMethod != null)
			{
				restMethods.add("POST");
			}
			if (putMethod != null)
			{
				restMethods.add("PUT");
			}			
			if (deleteMethod != null)
			{
				restMethods.add("DELETE");
			}
			methodModel.setRestMethod(String.join(",", restMethods));
									
			//produces
			Produces produces = (Produces) method.getAnnotation(Produces.class);
			if (produces != null)
			{
				methodModel.setProducesTypes(String.join(",", produces.value()));
			}
			
			//consumes
			Consumes consumes = (Consumes) method.getAnnotation(Consumes.class);
			if (consumes != null)
			{
				methodModel.setConsumesTypes(String.join(",", consumes.value()));
			}			
			
			aPIDescription = (APIDescription) method.getAnnotation(APIDescription.class);
			if (aPIDescription != null)
			{
				methodModel.setDescription(aPIDescription.value());
			}

			path = (Path) method.getAnnotation(Path.class);
			if (path != null)
			{
				methodModel.setMethodPath(path.value());
			}

			requireAdmin = (RequireAdmin) method.getAnnotation(RequireAdmin.class);
			if (requireAdmin != null)
			{
				methodModel.setRequireAdmin(true);
			}

			try
			{
				if (!(method.getReturnType().getSimpleName().equals(Void.class.getSimpleName())) &&
				    !("javax.ws.rs.core.Response".equals(method.getReturnType().getName())))
				{
					APIValueModel valueModel = new APIValueModel();
					valueModel.setValueObjectName(method.getReturnType().getSimpleName());					
					valueModel.setValueObject(objectMapper.writeValueAsString(method.getReturnType().newInstance()));
					mapValueField(valueModel.getValueFields(), method.getReturnType().getDeclaredFields());
					mapComplexTypes(valueModel.getAllComplexTypes(), method.getReturnType().getDeclaredFields(), false);
					
					DataType dataType = (DataType) method.getAnnotation(DataType.class);
					if (dataType != null)
					{
						valueModel.setTypeObjectName(dataType.value().getSimpleName());
						valueModel.setTypeObject(objectMapper.writeValueAsString(dataType.value().newInstance()));
						mapValueField(valueModel.getTypeFields(), dataType.value().getDeclaredFields());	
						mapComplexTypes(valueModel.getAllComplexTypes(), dataType.value().getDeclaredFields(), false);
					}					

					methodModel.setResponseObject(valueModel);					
				}
			}
			catch (InstantiationException | IllegalAccessException | JsonProcessingException ex)
			{
				log.log(Level.WARNING, null, ex);
			}
			
			
			//method parameters	
			mapMethodParameters(methodModel.getMethodParams(), method.getParameters());
						
			//Handle Consumed Objects
			mapConsumedObjects(methodModel, method.getParameters());
			
			resourceModel.getMethods().add(methodModel);
		}	
		return resourceModel;
	}
	
	private static void mapConsumedObjects(APIMethodModel methodModel, Parameter parameters[])
	{
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);		
		for (Parameter parameter : parameters)
		{
			//deterimine if this is "Body" object
			List<Annotation> paramAnnotation = new ArrayList<>();
			paramAnnotation.add(parameter.getAnnotation(QueryParam.class));
			paramAnnotation.add(parameter.getAnnotation(FormParam.class));
			paramAnnotation.add(parameter.getAnnotation(MatrixParam.class));
			paramAnnotation.add(parameter.getAnnotation(HeaderParam.class));
			paramAnnotation.add(parameter.getAnnotation(CookieParam.class));
			paramAnnotation.add(parameter.getAnnotation(PathParam.class));
			paramAnnotation.add(parameter.getAnnotation(BeanParam.class));
	
			boolean consumeObject = true;
			for (Annotation annotation : paramAnnotation)
			{
				if (annotation != null)
				{
					consumeObject = false;
					break;
				}
			}
			
			if (consumeObject)
			{
				APIValueModel valueModel = new APIValueModel();
				try
				{
					valueModel.setValueObject(objectMapper.writeValueAsString(parameter.getType().newInstance()));
					valueModel.setValueObjectName(parameter.getType().getSimpleName());	
					Set<String> fieldList = mapValueField(valueModel.getValueFields(), parameter.getType().getDeclaredFields(), true);	
					String cleanUpJson = StringProcessor.stripeFieldJSON(valueModel.getValueObject(), fieldList);
					valueModel.setValueObject(cleanUpJson);
					mapComplexTypes(valueModel.getAllComplexTypes(), parameter.getType().getDeclaredFields(), true);
					
					DataType dataType = (DataType) parameter.getAnnotation(DataType.class);
					if (dataType != null)
					{
						valueModel.setTypeObjectName(dataType.value().getSimpleName());
						valueModel.setTypeObject(objectMapper.writeValueAsString(dataType.value().newInstance()));
						fieldList = mapValueField(valueModel.getTypeFields(), dataType.value().getDeclaredFields(), true);							
						cleanUpJson = StringProcessor.stripeFieldJSON(valueModel.getValueObject(), fieldList);
						valueModel.setValueObject(cleanUpJson);						
						mapComplexTypes(valueModel.getAllComplexTypes(), dataType.value().getDeclaredFields(), true);
					}					
				}
				catch (InstantiationException | IllegalAccessException | JsonProcessingException ex)
				{
					log.log(Level.WARNING, null, ex);
				}
				
				//There can only be one consume(Request Body Parameter) object 
				//We take the first one and ignore the rest.  
				methodModel.setConsumeObject(valueModel);
				break;
			}
		}	
	}
	
	private static void mapComplexTypes(List<APITypeModel> typeModels,  Field fields[], boolean onlyConsumeField)
	{
		//Should strip duplicate types
		Set<String> typesInList = new HashSet<>();
		typeModels.forEach(type -> {
			typesInList.add(type.getName());
		});
		
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		for (Field field : fields)
		{
			boolean capture = true;
			if (onlyConsumeField)
			{
				ConsumeField consumeField = (ConsumeField) field.getAnnotation(ConsumeField.class);
				if (consumeField == null)
				{
					capture = false;
				}
			}
			
			if (capture)
			{			
				
				Class fieldClass = field.getType();
				DataType dataType = (DataType) field.getAnnotation(DataType.class);
				if (dataType != null)
				{
					fieldClass = dataType.value();
				}				
				
				if (!fieldClass.isPrimitive()  && 
					!fieldClass.isArray() &&
					!typesInList.contains(fieldClass.getSimpleName()) &&
					!fieldClass.getSimpleName().equalsIgnoreCase(String.class.getSimpleName()) &&
					!fieldClass.getSimpleName().equalsIgnoreCase(Long.class.getSimpleName()) &&
					!fieldClass.getSimpleName().equalsIgnoreCase(Integer.class.getSimpleName()) &&
					!fieldClass.getSimpleName().equalsIgnoreCase(Boolean.class.getSimpleName()) &&
					!fieldClass.getSimpleName().equalsIgnoreCase(Double.class.getSimpleName()) &&
					!fieldClass.getSimpleName().equalsIgnoreCase(Float.class.getSimpleName()) &&
					!fieldClass.getSimpleName().equalsIgnoreCase(BigDecimal.class.getSimpleName()) &&
					!fieldClass.getSimpleName().equalsIgnoreCase(Date.class.getSimpleName()) &&
					!fieldClass.getSimpleName().equalsIgnoreCase(List.class.getSimpleName()) &&
					!fieldClass.getSimpleName().equalsIgnoreCase(Map.class.getSimpleName()) &&
					!fieldClass.getSimpleName().equalsIgnoreCase(Set.class.getSimpleName()) &&
					!fieldClass.getSimpleName().equalsIgnoreCase(BigInteger.class.getSimpleName())
					)
				{	

					
					APITypeModel typeModel = new APITypeModel();
					typeModel.setName(fieldClass.getSimpleName());
					mapValueField(typeModel.getFields(), fieldClass.getDeclaredFields(), onlyConsumeField);
					try
					{
						typeModel.setObject(objectMapper.writeValueAsString(fieldClass.newInstance()));
					} catch (InstantiationException | IllegalAccessException | JsonProcessingException ex)
					{
						log.log(Level.WARNING, "Unable to process/map complex field: " + fieldClass.getSimpleName() , ex);
						typeModel.setObject("{ Unable to view }");
					}					
					typeModels.add(typeModel);
					typesInList.add(typeModel.getName());
					
					mapComplexTypes(typeModels, fieldClass.getDeclaredFields(), onlyConsumeField);
				}
					
			}
		}
	}
	
	private static void mapValueField(List<APIValueFieldModel> fieldModels,  Field fields[])
	{
		mapValueField(fieldModels, fields, false);
	}
	
	private static Set<String> mapValueField(List<APIValueFieldModel> fieldModels,  Field fields[], boolean onlyComsumeField)
	{
		Set<String> fieldNamesCaptured = new HashSet<>();
		
		for (Field field : fields)
		{
			boolean capture = true;
						
			if (onlyComsumeField)
			{
				ConsumeField consumeField = (ConsumeField) field.getAnnotation(ConsumeField.class);
				if (consumeField == null)
				{
					capture = false;
				}
			}
			
			if (capture)
			{
				APIValueFieldModel fieldModel = new APIValueFieldModel();
				fieldModel.setFieldName(field.getName());
				fieldNamesCaptured.add(field.getName());
				fieldModel.setType(field.getType().getSimpleName());
				
				DataType dataType = (DataType) field.getAnnotation(DataType.class);
				if (dataType != null)
				{
					fieldModel.setType(fieldModel.getType() + ":  " + dataType.value().getSimpleName());
				}

				NotNull requiredParam = (NotNull) field.getAnnotation(NotNull.class);
				if (requiredParam != null)
				{
					fieldModel.setRequired(true);
				}
				
				StringBuilder validation = new StringBuilder();
				ParamTypeDescription description = (ParamTypeDescription) field.getAnnotation(ParamTypeDescription.class);
				if (description != null)
				{					
					validation.append(description.value()).append("<br>");
				}
				
				if ("Date".equals(field.getType().getSimpleName()))
				{
					validation.append("Timestamp (milliseconds since UNIX Epoch<br>");
				}
				
				if ("boolean".equalsIgnoreCase(field.getType().getSimpleName()))
				{
					validation.append("T | F");
				}

				ValidationRequirement validationRequirement = (ValidationRequirement) field.getAnnotation(ValidationRequirement.class);
				if (validationRequirement != null)
				{
					validation.append(validationRequirement.value()).append("<br>");
				}

				Min min = (Min) field.getAnnotation(Min.class);
				if (min != null)
				{
					validation.append("Min Value: ").append(min.value()).append("<br>");
				}

				Max max = (Max) field.getAnnotation(Max.class);
				if (max != null)
				{
					validation.append("Max Value: ").append(max.value()).append("<br>");
				}

				Size size = (Size) field.getAnnotation(Size.class);
				if (size != null)
				{
					validation.append("Min Length: ").append(size.min()).append(" Max Length: ").append(size.max()).append("<br>");
				}

				Pattern pattern = (Pattern) field.getAnnotation(Pattern.class);
				if (pattern != null)
				{
					validation.append("Needs to Match: ").append(pattern.regexp()).append("<br>");
				}

				ValidValueType validValueType = (ValidValueType) field.getAnnotation(ValidValueType.class);
				if (validValueType != null)
				{
					validation.append("Set of valid values: ").append(Arrays.toString(validValueType.value())).append("<br>");
				}				

				fieldModel.setValidation(validation.toString());

				fieldModels.add(fieldModel);
			}
		}
		return fieldNamesCaptured;
	}
	
	private static void mapMethodParameters(List<APIParamModel> parameterList, Parameter parameters[])
	{
		for (Parameter parameter : parameters)
		{
			APIParamModel paramModel = new APIParamModel();
			paramModel.setFieldName(parameter.getName());
			
			QueryParam queryParam = (QueryParam) parameter.getAnnotation(QueryParam.class);
			FormParam formParam = (FormParam) parameter.getAnnotation(FormParam.class);
			MatrixParam matrixParam = (MatrixParam) parameter.getAnnotation(MatrixParam.class);
			HeaderParam headerParam = (HeaderParam) parameter.getAnnotation(HeaderParam.class);
			CookieParam cookieParam = (CookieParam) parameter.getAnnotation(CookieParam.class);
			PathParam pathParam = (PathParam) parameter.getAnnotation(PathParam.class);
			BeanParam beanParam = (BeanParam) parameter.getAnnotation(BeanParam.class);

			if (queryParam != null)
			{
				paramModel.setParameterType(QueryParam.class.getSimpleName());
				paramModel.setParameterName(queryParam.value());
			}
			if (formParam != null)
			{
				paramModel.setParameterType(FormParam.class.getSimpleName());
				paramModel.setParameterName(formParam.value());
			}
			if (matrixParam != null)
			{
				paramModel.setParameterType(MatrixParam.class.getSimpleName());
				paramModel.setParameterName(matrixParam.value());
			}
			if (pathParam != null)
			{
				paramModel.setParameterType(PathParam.class.getSimpleName());
				paramModel.setParameterName(pathParam.value());
			}
			if (headerParam != null)
			{
				paramModel.setParameterType(HeaderParam.class.getSimpleName());
				paramModel.setParameterName(headerParam.value());
			}
			if (cookieParam != null)
			{
				paramModel.setParameterType(CookieParam.class.getSimpleName());
				paramModel.setParameterName(cookieParam.value());
			}		

			if (beanParam != null)
			{
				Class paramClass = parameter.getType();
				mapParameters(parameterList, paramClass.getDeclaredFields());
			}
			if (StringUtils.isNotBlank(paramModel.getParameterType()))
			{
				APIDescription aPIDescription = (APIDescription) parameter.getAnnotation(APIDescription.class);
				if (aPIDescription != null)
				{
					paramModel.setParameterDescription(aPIDescription.value());
				}

				ParameterRestrictions restrictions = (ParameterRestrictions) parameter.getAnnotation(ParameterRestrictions.class);
				if (restrictions != null)
				{
					paramModel.setRestrictions(restrictions.value());
				}

				RequiredParam requiredParam = (RequiredParam) parameter.getAnnotation(RequiredParam.class);
				if (requiredParam != null)
				{
					paramModel.setRequired(true);
				}

				DefaultValue defaultValue = (DefaultValue) parameter.getAnnotation(DefaultValue.class);
				if (defaultValue != null)
				{
					paramModel.setDefaultValue(defaultValue.value());
				}
				
				parameterList.add(paramModel);
			}			
		}		
	}
	
	private static void mapParameters(List<APIParamModel> parameterList,  Field fields[])
	{
		for (Field field : fields)
		{
			APIParamModel paramModel = new APIParamModel();
			paramModel.setFieldName(field.getName());

			QueryParam queryParam = (QueryParam) field.getAnnotation(QueryParam.class);
			FormParam formParam = (FormParam) field.getAnnotation(FormParam.class);
			MatrixParam matrixParam = (MatrixParam) field.getAnnotation(MatrixParam.class);
			HeaderParam headerParam = (HeaderParam) field.getAnnotation(HeaderParam.class);
			CookieParam cookieParam = (CookieParam) field.getAnnotation(CookieParam.class);			
			PathParam pathParam = (PathParam) field.getAnnotation(PathParam.class);
			BeanParam beanParam = (BeanParam) field.getAnnotation(BeanParam.class);

			if (queryParam != null)
			{
				paramModel.setParameterType(QueryParam.class.getSimpleName());
				paramModel.setParameterName(queryParam.value());
			}
			if (formParam != null)
			{
				paramModel.setParameterType(FormParam.class.getSimpleName());
				paramModel.setParameterName(formParam.value());
			}
			if (matrixParam != null)
			{
				paramModel.setParameterType(MatrixParam.class.getSimpleName());
				paramModel.setParameterName(matrixParam.value());
			}
			if (pathParam != null)
			{
				paramModel.setParameterType(PathParam.class.getSimpleName());
				paramModel.setParameterName(pathParam.value());
			}
			if (headerParam != null)
			{
				paramModel.setParameterType(HeaderParam.class.getSimpleName());
				paramModel.setParameterName(headerParam.value());
			}
			if (cookieParam != null)
			{
				paramModel.setParameterType(CookieParam.class.getSimpleName());
				paramModel.setParameterName(cookieParam.value());
			}			

			if (beanParam != null)
			{
				Class fieldClass = field.getDeclaringClass();				
				mapParameters(parameterList, fieldClass.getDeclaredFields());
			}

			if (StringUtils.isNotBlank(paramModel.getParameterType()))
			{

				APIDescription aPIDescription = (APIDescription) field.getAnnotation(APIDescription.class);
				if (aPIDescription != null)
				{
					paramModel.setParameterDescription(aPIDescription.value());
				}

				ParameterRestrictions restrictions = (ParameterRestrictions) field.getAnnotation(ParameterRestrictions.class);
				if (restrictions != null)
				{
					paramModel.setRestrictions(restrictions.value());
				}

				RequiredParam requiredParam = (RequiredParam) field.getAnnotation(RequiredParam.class);
				if (requiredParam != null)
				{
					paramModel.setRequired(true);
				}

				DefaultValue defaultValue = (DefaultValue) field.getAnnotation(DefaultValue.class);
				if (defaultValue != null)
				{
					paramModel.setDefaultValue(defaultValue.value());
				}

				parameterList.add(paramModel);
			}
		}	
	}
	
}
