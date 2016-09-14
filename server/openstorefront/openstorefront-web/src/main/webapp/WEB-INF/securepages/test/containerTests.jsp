<%--
Copyright 2014 Space Dynamics Laboratory - Utah State University Research Foundation.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<link href="../apidoc/css/apidoc.css" rel="stylesheet" type="text/css"/>
	<script src="../apidoc/script/jquery/jquery-1.11.1.min.js" type="text/javascript"></script>		
        <title>Container Test Page</title>
		<style>
			.test-guide{
				left: 0px;
				position: fixed;
				top: 0px;
				bottom: 0px;
				margin-top: 45px;
				padding-left: 10px;
				border-right: 1px solid #dadfe5;
				overflow: hidden;
				overflow-y: auto;
				width: 300px;				
				z-index: 25;	
				background-color: white;
			}

			.test-doc{
				background: white;
				margin-top: 25px;
				margin-left: 300px;
				min-width: 750px;		
			}
			
			.loading{
				text-align: center;
				font-size: 24px;
				padding: 25% 0px 0px 0px;
				background-color: #666666;
				color: whitesmoke;
				top: 0px;
				left: 0px;
				bottom: 0px;
				right: 0px;				
			}
			
			.loading-top{
				position: absolute;
				top: 0px;
				left: 0px;
				bottom: 0px;
				right: 0px;
				opacity: .7;
				text-align: center;
				font-size: 24px;
				padding: 25% 0px 0px 0px;
				z-index: 9999;
				background-color: black;
				color: white;
				animation-name: example;
				animation-duration: 1s;
				animation-iteration-count: infinite;
				animation-direction: alternate;
			}
			
			@keyframes example {
				from {color: white;}
				to {color: yellow;}
			}			
			
		</style>
    </head>
    <body id="fullbody">
				<header class="header">
			<span class="api-title">Open Storefront Container Tests</span>
		</header>
		
		<div id="nav" class="test-guide" >
			<ul>
				<li>
					<a href="javascript:i=0;" onclick="runTest('');" >Run All Tests</a>
					<hr>
				</li>				
			<c:forEach var="testSuite" items="${actionBean.testSuites}">
				<li>
					<a href="javascript:i=0;" onclick="runTest('&suite=${testSuite.name}');" >${testSuite.name}</a>
					<ul>	
					<c:forEach var="item" items="${testSuite.tests}">						
						<li>
							<a href="javascript:i=0;" onclick="runTest('&suite=${testSuite.name}&test=${item.description}');" >${item.description}</a>
						</li>	
					</c:forEach>					
					</ul>
				</li>
			</c:forEach>	
			</ul>	
		</div>
		<div id="outputId" class="test-doc">
		</div>		
		
		<script type="text/javascript">
			function runTest(query)
			{	 
				//$('#outputId').html("<div class='loading'>Running Test(s)...</div>");				
				$('#fullbody').append("<div id='nav-loader' class='loading-top'>Running Test(s)...</div>");
				$('#outputId').load('ServiceTest.action?RunTest' + query.replace(/ /g, '%20'), 
					function(responseText,status, xhr){
						$('#nav-loader').remove();
					}
				);				
			}
		</script>
		
    </body>
</html>
