<%--
/* 
 * Copyright 2016 Space Dynamics Laboratory - Utah State University Research Foundation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * See NOTICE.txt for more information.
 */
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link href="apidoc/css/apidoc.css" rel="stylesheet" type="text/css"/>
	<script src="apidoc/script/jquery/jquery-1.11.1.min.js" type="text/javascript"></script>
        <title>API Details</title>
    </head>
    <body>
		
    <h1>${actionBean.resourceModel.resourceName}</h1>
	  ${actionBean.resourceModel.resourceDescription}<br>
	  
	<c:if test="${actionBean.resourceModel.securityRestriction != null}">
		<br>
		<span class="resource-admin">Requires:</span><br>
		<c:if test="${!empty actionBean.resourceModel.securityRestriction.permissions}">
			<b>Permissions:</b>
			<ul>							
				<c:forEach var="permission" items="${actionBean.resourceModel.securityRestriction.permissions}">
					<li>
						${permission}
					</li>
				</c:forEach>
			</ul>			
		</c:if>
		<c:if test="${!empty actionBean.resourceModel.securityRestriction.roles}">
			<b>Roles:</b>
			<ul>							
				<c:forEach var="role" items="${actionBean.resourceModel.securityRestriction.roles}">
					<li>
						${role}
					</li>
				</c:forEach>
			</ul>			
		</c:if>	
		<c:if test="${actionBean.resourceModel.securityRestriction.logicOperation != null}">
			<b>Logic Operation:</b> ${actionBean.resourceModel.securityRestriction.logicOperation}<br>
		</c:if>
		<c:if test="${actionBean.resourceModel.securityRestriction.specialCheck != null}">
			<b>Special Handling:</b> ${actionBean.resourceModel.securityRestriction.specialCheck}<br>
		</c:if>
	</c:if>	  
	
	<h3>${actionBean.classPathDescription} Path: ${actionBean.resourceModel.resourcePath}</h3>
	<c:if test="${!empty actionBean.resourceModel.methods}">
		<h2>Paths</h2>
		<ul>
		<c:forEach var="item" items="${actionBean.resourceModel.methods}">
			<li><span class="${item.restMethod}" style="line-height: 28px;">${item.restMethod}</span> - <a href="javascript:" onclick="scrollToAnchor('${item.id}');" >${actionBean.resourceModel.resourcePath}${item.methodPath}</a></li>
		</c:forEach>
		</ul>	
	</c:if>
	
	
	<c:if test="${!empty actionBean.resourceModel.resourceParams}">
		<h3>Resource Parameters: </h3>	  
		<table>
			<tr>
				<th>Parameter</th>
				<th>Description</th>
				<th>Required</th>
				<th>Default Value</th>
				<th>Restrictions</th>
				<th>Parameter Type</th>
			</tr>		
			<c:forEach var="item" items="${actionBean.resourceModel.resourceParams}">
			<tr>
				<td>${item.parameterName}</td>
				<td>${item.parameterDescription}</td>
				<td>${item.required}</td>
				<td>${item.defaultValue}</td>
				<td>${item.restrictions}</td>
				<td>${item.parameterType}</td>				
			</tr>
			</c:forEach>
		</table> 
		
	</c:if>
	<c:if test="${!empty actionBean.resourceModel.methods}">
		<h2>Details</h2>
		<table width="100%">
			<tr>
				<th style='text-align: center;'>Method</th>
				<th style='text-align: center;'>Restrictions</th>
				<th style='text-align: left;'>Description</th>
				<th style='text-align: left;'>Path</th>
				<th style='text-align: left;'>Parameters</th>				
				<th style='text-align: left;'>Produces/Consumes Type(s)</th>
			</tr>		
			<c:forEach var="item" items="${actionBean.resourceModel.methods}">
			<tr style="background-color: white;">
				<td align="center"><span class="${item.restMethod}">${item.restMethod}</span></td>
				<td align="center">
					<c:if test="${item.securityRestriction != null}">
						<br>
						<span class="resource-admin">Requires:</span><br>
						<c:if test="${!empty item.securityRestriction.permissions}">
							<b>Permissions:</b>
							<ul>							
								<c:forEach var="permission" items="${item.securityRestriction.permissions}">
									<li>
										${permission}
									</li>
								</c:forEach>
							</ul>			
						</c:if>
						<c:if test="${!empty item.securityRestriction.roles}">
							<b>Roles:</b>
							<ul>							
								<c:forEach var="role" items="${item.securityRestriction.roles}">
									<li>
										${role}
									</li>
								</c:forEach>
							</ul>			
						</c:if>	
						<c:if test="${item.securityRestriction.logicOperation != null}">
							<b>Logic Operation:</b> ${item.securityRestriction.logicOperation}<br>
						</c:if>
						<c:if test="${item.securityRestriction.specialCheck != null}">
							<b>Special Handling:</b> ${item.securityRestriction.specialCheck}<br>
						</c:if>
					</c:if>	  		
				</td>
				<td>${item.description}</td>
				<td id="${item.id}TD"><span class="resourcePath"><a id="${item.id}">${actionBean.resourceModel.resourcePath}${item.methodPath}</a></span></td>
				<td>
					<c:if test="${!empty item.methodParams}">
					<table width="100%">
						<tr>
							<th>Parameter</th>
							<th>Description</th>
							<th>Required</th>
							<th>Defaults</th>
							<th>Restrictions</th>
							<th>Parameter Type</th>
						</tr>		
						<c:forEach var="methodParam" items="${item.methodParams}">
						<tr>
							<td>${methodParam.parameterName}</td>
							<td>${methodParam.parameterDescription}</td>
							<td>${methodParam.required}</td>
							<td>${methodParam.defaultValue}</td>
							<td>${methodParam.restrictions}</td>
							<td>${methodParam.parameterType}</td>				
						</tr>
						</c:forEach>
					</table> 					
					</c:if>
				</td>
				<td>
					<c:if test="${item.producesTypes != null}">
						<b>Produces:</b><br> 					
						${item.producesTypes}<br>						
					</c:if>
					<c:if test="${item.consumesTypes != null}">
						<b>Consumes:</b><br> 					
						${item.consumesTypes}<br>						
					</c:if>
				</td>				
			</tr>
			<c:if test="${item.consumeObject != null}">
				<tr style="background-color: lightgrey;">
					<td colspan="6">
						<div class="returnInfo">
							<div id="ctitle-${item.id}" class="returnInfo-title"
								 onmouseover="this.style.cursor='pointer';" onmouseout="this.style.cursor='default';"
								 onclick="$('#cinfo-${item.id}').toggle('slow');">	
								<c:if test="${item.consumeObject.typeObject != null}">
									Consume Object: <span class="value-object-name">${item.consumeObject.valueObjectName} (${item.consumeObject.typeObjectName})</span>									
								</c:if>
								<c:if test="${item.consumeObject.typeObject == null}">
									Consume Object: <span class="value-object-name">${item.consumeObject.valueObjectName}</span>
								</c:if>
							</div>
							<div id="cinfo-${item.id}" class="returnInfo-contents">								
								<c:if test="${item.consumeObject.valueObject != null}">	
									<h5>${item.consumeObject.valueDescription}</h5>
									<pre>
${item.consumeObject.valueObject}									
									</pre>								
									<table>
										<tr>
											<th style='text-align: left;'>Field Name</th>
											<th style='text-align: center;'>Required</th>
											<th style='text-align: left;'>Type</th>
											<th style='text-align: left;'>Validation</th>
											<th style='text-align: left;'>Description</th>
										</tr>
										<c:forEach var="field" items="${item.consumeObject.valueFields}">
										<tr>
											<td>
												${field.fieldName}
											</td>
											<td align="center">
												${field.required}
											</td>
											<td>
												${field.type}
											</td>
											<td>
												${field.validation}
											</td>
											<td>
												${field.description}
											</td>												
										</tr>
										</c:forEach>
									</table>								
								</c:if>
								<c:if test="${item.consumeObject.typeObject != null}">
									<h3>Data Type Details</h3>
									<h5>${item.consumeObject.typeDescription}</h5>
									<pre>
${item.consumeObject.typeObject}									
									</pre>								
									<table>
										<tr>
											<th style='text-align: left;'>Field Name</th>
											<th style='text-align: center;'>Required</th>
											<th style='text-align: left;'>Type</th>
											<th style='text-align: left;'>Validation</th>
											<th style='text-align: left;'>Description</th>
										</tr>
										<c:forEach var="field" items="${item.consumeObject.typeFields}">
										<tr>
											<td>
												${field.fieldName}
											</td>
											<td  align="center">
												${field.required}
											</td>
											<td>
												${field.type}
											</td>
											<td>
												${field.validation}
											</td>
											<td>
												${field.description}
											</td>												
										</tr>
										</c:forEach>
									</table>								
								</c:if>
								<c:if test="${!empty item.consumeObject.allComplexTypes}">
									<h3>Complex Type(s) Details</h3>
									<c:forEach var="complexType" items="${item.consumeObject.allComplexTypes}">
										<h4>${complexType.name}</h4>
										<h5>${complexType.description}</h5>
										<pre>
${complexType.object}									
										</pre>								
										<table>
											<tr>
												<th style='text-align: left;'>Field Name</th>
												<th style='text-align: center;'>Required</th>
												<th style='text-align: left;'>Type</th>
												<th style='text-align: left;'>Validation</th>
												<th style='text-align: left;'>Description</th>
											</tr>
											<c:forEach var="field" items="${complexType.fields}">
											<tr>
												<td>
													${field.fieldName}
												</td>
												<td  align="center">
													${field.required}
												</td>
												<td>
													${field.type}
												</td>
												<td>
													${field.validation}
												</td>
												<td>
													${field.description}
												</td>												
											</tr>
											</c:forEach>
										</table>
									</c:forEach>
								</c:if>									
							</div>
						</div>
							
					</td>					
				</tr>	
			</c:if>			
			<c:if test="${item.responseObject != null}">
				<tr style="background-color: lightgrey;">
					<td colspan="6">
						<div class="returnInfo">
							<div id="rtitle-${item.id}" class="returnInfo-title"
								 onmouseover="this.style.cursor='pointer';" onmouseout="this.style.cursor='default';"
								 onclick="$('#rinfo-${item.id}').toggle('slow');">	
								<c:if test="${item.responseObject.typeObject != null}">
									Response Object: <span class="value-object-name">${item.responseObject.valueObjectName} (${item.responseObject.typeObjectName})</span>
								</c:if>
								<c:if test="${item.responseObject.typeObject == null}">
									Response Object: <span class="value-object-name">${item.responseObject.valueObjectName}</span>
								</c:if>
							</div>
													
							<div id="rinfo-${item.id}" class="returnInfo-contents">								
								<c:if test="${item.responseObject.valueObject != null}">
									<h5>${item.responseObject.valueDescription}</h5>
									<pre>
${item.responseObject.valueObject}									
									</pre>								
									<table>
										<tr>
											<th style='text-align: left;'>Field Name</th>
											<th style='text-align: center;'>Required</th>
											<th style='text-align: left;'>Type</th>
											<th style='text-align: left;'>Validation</th>
											<th style='text-align: left;'>Description</th>
										</tr>
										<c:forEach var="field" items="${item.responseObject.valueFields}">
										<tr>
											<td>
												${field.fieldName}
											</td>
											<td align="center">
												${field.required}
											</td>
											<td>
												${field.type}
											</td>
											<td>
												${field.validation}
											</td>
											<td>
												${field.description}
											</td>											
										</tr>
										</c:forEach>
									</table>								
								</c:if>
								<c:if test="${item.responseObject.typeObject != null}">
									<h3>Data Type Details</h3>
									<h5>${item.responseObject.typeDescription}</h5>
									<pre>
${item.responseObject.typeObject}									
									</pre>								
									<table>
										<tr>
											<th style='text-align: left;'>Field Name</th>
											<th style='text-align: center;'>Required</th>
											<th style='text-align: left;'>Type</th>
											<th style='text-align: left;'>Validation</th>
											<th style='text-align: left;'>Description</th>
										</tr>
										<c:forEach var="field" items="${item.responseObject.typeFields}">
										<tr>
											<td>
												${field.fieldName}
											</td>
											<td  align="center">
												${field.required}
											</td>
											<td>
												${field.type}
											</td>
											<td>
												${field.validation}
											</td>
											<td>
												${field.description}
											</td>											
										</tr>
										</c:forEach>
									</table>								
								</c:if>
								<c:if test="${!empty item.responseObject.allComplexTypes}">
									<h3>Complex Type(s) Details</h3>
									<c:forEach var="complexType" items="${item.responseObject.allComplexTypes}">
										<h4>${complexType.name}</h4>
										<h5>${complexType.description}</h5>
										<pre>
${complexType.object}									
										</pre>								
										<table>
											<tr>
												<th style='text-align: left;'>Field Name</th>
												<th style='text-align: center;'>Required</th>
												<th style='text-align: left;'>Type</th>
												<th style='text-align: left;'>Validation</th>
												<th style='text-align: left;'>Description</th>
											</tr>
											<c:forEach var="field" items="${complexType.fields}">
											<tr>
												<td>
													${field.fieldName}
												</td>
												<td  align="center">
													${field.required}
												</td>
												<td>
													${field.type}
												</td>
												<td>
													${field.validation}
												</td>
												<td>
													${field.description}
												</td>												
											</tr>
											</c:forEach>
										</table>
									</c:forEach>
								</c:if>									
							</div>
						</div>
							
					</td>					
				</tr>	
			</c:if>				
			</c:forEach>
		</table> 
			
	</c:if>
		<div class="toTopDiv" onclick="$('html,body').animate({scrollTop: 0},'slow');">Top</div>
		
		<script type="text/javascript">
			
			function scrollToAnchor(aid){
				$('*').removeClass('highlight');
				var aTag = $("a[id='"+ aid +"']");
				$('#'+aid+'TD').addClass('highlight');
				$('html,body').animate({scrollTop: (aTag.offset().top-200)},'slow');
			}	
		</script>
	   
    </body>
</html>
