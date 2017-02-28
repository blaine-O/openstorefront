<%-- 

/* 
 * Copyright 2017 Space Dynamics Laboratory - Utah State University Research Foundation.
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

    Document   : approveChange
    Created on : Feb 27, 2017, 3:19:45 PM
    Author     : dshurtleff
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<stripes:layout-render name="layout/externalLayout.jsp">
    <stripes:layout-component name="contents">
		
		<style> 
			
			.header-background{
				color: white;
				background: #7d7e7d;
				background: url(data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiA/Pgo8c3ZnIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyIgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIgdmlld0JveD0iMCAwIDEgMSIgcHJlc2VydmVBc3BlY3RSYXRpbz0ibm9uZSI+CiAgPGxpbmVhckdyYWRpZW50IGlkPSJncmFkLXVjZ2ctZ2VuZXJhdGVkIiBncmFkaWVudFVuaXRzPSJ1c2VyU3BhY2VPblVzZSIgeDE9IjAlIiB5MT0iMCUiIHgyPSIwJSIgeTI9IjEwMCUiPgogICAgPHN0b3Agb2Zmc2V0PSIwJSIgc3RvcC1jb2xvcj0iIzdkN2U3ZCIgc3RvcC1vcGFjaXR5PSIxIi8+CiAgICA8c3RvcCBvZmZzZXQ9IjAlIiBzdG9wLWNvbG9yPSIjNTk1OTU5IiBzdG9wLW9wYWNpdHk9IjEiLz4KICAgIDxzdG9wIG9mZnNldD0iMTAwJSIgc3RvcC1jb2xvcj0iIzBlMGUwZSIgc3RvcC1vcGFjaXR5PSIxIi8+CiAgPC9saW5lYXJHcmFkaWVudD4KICA8cmVjdCB4PSIwIiB5PSIwIiB3aWR0aD0iMSIgaGVpZ2h0PSIxIiBmaWxsPSJ1cmwoI2dyYWQtdWNnZy1nZW5lcmF0ZWQpIiAvPgo8L3N2Zz4=);
				background: -moz-linear-gradient(top, #7d7e7d 0%, #595959 0%, #0e0e0e 100%);
				background: -webkit-linear-gradient(top, #7d7e7d 0%,#595959 0%,#0e0e0e 100%);
				background: linear-gradient(to bottom, #7d7e7d 0%,#595959 0%,#0e0e0e 100%);
				filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#7d7e7d', endColorstr='#0e0e0e',GradientType=0 );				
			}
			
		</style>		
		
		<script type="text/javascript">
			/* global Ext, CoreUtil */

			Ext.onReady(function () {
				
				var approvalCode = '${param.approvalCode}';
				
				var approveWindow = Ext.create('Ext.window.Window', {
					title: 'Verifying Approval Code',
					iconCls: 'fa fa-check-square',
					y: 200,
					width: 700,
					height: 300,
					closable: false,
					draggable: false,
					onEsc: Ext.emptyFn,
					layout: 'fit',
					items: [
						{
							xtype: 'panel',							
							itemId: 'infopanel',							
							bodyStyle: 'padding: 20px;',						
							html: ''
						}
					],
					dockedItems: [
						{
							xtype: 'toolbar',
							dock: 'bottom',
							items: [
								{
									xtype: 'tbfill'
								},
								{
									text: 'Login',
									iconCls: 'fa fa-2x fa-sign-in',
									scale: 'medium',
									handler: function(){										
										window.location.href = 'login.jsp';
									}									
								},
								{
									xtype: 'tbfill'
								}
							]
						}
					]
				});
				approveWindow.show();
				
				var checkApprovalCode = function() {
					var infoPanel = approveWindow.getComponent('infopanel');
					
					if (!approvalCode || approvalCode === '') {
						infoPanel.update("<h2><i class='fa fa-2x fa-warning text-danger'></i> Approval code is missing.</h2><br><br>Make sure to click or copy the complete the url in the email.");
					} else {					
						infoPanel.setLoading('Checking...');
						Ext.Ajax.request({
							url: 'api/v1/service/security/approveResetPassword/' + approvalCode,
							method: 'PUT',
							callback: function() {
								infoPanel.setLoading(false);
							},
							success: function(response, opts) {
								var message = '';
								if (response.responseText === 'true') {
									message = "<h2><i class='fa fa-2x fa-check text-success'></i> Password was reset successfully.</h2><br><br>Login to continue.";
								} else {
									message = "<h2><i class='fa fa-2x fa-warning text-danger'></i> Unable to verify approval code.</h2><br>The code may be bad or may be already used.<br>Check the url and try again. <br>Or request a new approval code on the reset password page.";
								}
								infoPanel.update(message);
							}
						});
					}
				};
				checkApprovalCode();
				
				Ext.create('Ext.container.Viewport', {
					layout: 'border',					
					items: [
						{
							region: 'north',							
							bodyStyle: 'padding-top: 20px;padding-bottom: 20px;',
							bodyCls: 'header-background',
							html: '<h1 style="text-align: center">${appTitle}</h1>'
							
						},
						{
							region: 'center',
							bodyStyle: 'background-image: url(images/grid.png); background-repeat: repeat;',
						}
					]
				});					
				
			});
		
		</script>
		
	</stripes:layout-component>
</stripes:layout-render>					