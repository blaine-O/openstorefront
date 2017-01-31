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

package edu.usu.sdl.openstorefront.web.rest;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import org.apache.commons.lang3.StringUtils;

/**
 * Adds Jsonp Support to any GET REST request;
 * @author dshurtleff
 */
@Provider
public class JsonpInterceptor implements WriterInterceptor, ContainerResponseFilter
{
	private static final String CALLBACK = "CALLBACK";
	
	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException
	{
		String callback = requestContext.getUriInfo().getQueryParameters().getFirst("callback");
		requestContext.setProperty(CALLBACK, callback);
	}
	
	@Override
	public void aroundWriteTo(WriterInterceptorContext responseContext) throws IOException			
	{	
		String callback = (String) responseContext.getProperty(CALLBACK);
		if (StringUtils.isNotBlank(callback))
		{			
			responseContext.getOutputStream().write((callback + "(").getBytes());
			responseContext.proceed();
			responseContext.getOutputStream().write(")".getBytes());			
		}
		else
		{
			responseContext.proceed();
		}
	}


	
}
