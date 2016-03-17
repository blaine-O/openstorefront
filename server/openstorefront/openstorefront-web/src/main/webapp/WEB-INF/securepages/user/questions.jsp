<%-- 
Copyright 2016 Space Dynamics Laboratory - Utah State University Research Foundation.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

    Document   : questions
    Created on : Mar 1, 2016, 11:41:25 AM
    Author     : dshurtleff
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<stripes:layout-render name="../../../client/layout/usertoolslayout.jsp">
    <stripes:layout-component name="contents">
		
	   <script src="scripts/component/questionWindow.js?v=${appVersion}" type="text/javascript"></script>	
		
	   <script type="text/javascript">
			/* global Ext, CoreUtil */

			Ext.onReady(function () {

				var responseGrid = Ext.create('Ext.grid.Panel', {
					title: 'Answers',
					id: 'responseGrid',
					columnLines: true,
					store: {
						sorters: [{
							property: 'componentName',
							direction: 'ASC'
						}],	
						autoLoad: true,
						fields: [
							{
								name: 'createDts',
								type:	'date',
								dateFormat: 'c'
							},
							{
								name: 'updateDts',
								type:	'date',
								dateFormat: 'c'
							}
						],
						proxy: {
							type: 'ajax',
							url: '../api/v1/resource/componentquestions/responses/' + '${user}'
						}		
					},
					columns: [
						{ text: 'Entry', dataIndex: 'componentName', width: 275	},
						{ text: 'Answer', dataIndex: 'response', flex: 1, minWidth: 200 },
						{ text: 'Post Date', dataIndex: 'answeredDate', width: 200, xtype: 'datecolumn', format:'m/d/y H:i:s' },
						{ text: 'Update Date', dataIndex: 'questionUpdateDts', width: 200, xtype: 'datecolumn', format:'m/d/y H:i:s' }
					],
					dockedItems: [
						{
							dock: 'top',
							xtype: 'toolbar',
							itemId: 'tools',
							items: [
								{
									text: 'Refresh',
									scale: 'medium',								
									iconCls: 'fa fa-2x fa-refresh',
									handler: function () {
										actionRefreshResponses();
									}
								},
								{
									xtype: 'tbseparator'
								},
								{
									text: 'View Entry',
									itemId: 'view',
									scale: 'medium',
									disabled: true,
									iconCls: 'fa fa-2x fa-binoculars',
									handler: function () {
										actionViewEntry(Ext.getCmp('responseGrid').getSelectionModel().getSelection()[0].get('componentId'));										
									}									
								},								
								{
									text: 'Edit',
									itemId: 'edit',
									scale: 'medium',
									disabled: true,
									iconCls: 'fa fa-2x fa-edit',
									handler: function () {
										actionEditResponse(Ext.getCmp('responseGrid').getSelectionModel().getSelection()[0]);										
									}									
								},
								{
									xtype: 'tbfill'
								},
								{
									text: 'Delete',
									itemId: 'delete',
									scale: 'medium',
									disabled: true,
									iconCls: 'fa fa-2x fa-trash-o',
									handler: function () {
										actionDeleteResponse(Ext.getCmp('responseGrid').getSelectionModel().getSelection()[0]);
									}									
								}								
							]
						}
					],
					listeners: {
						itemdblclick: function(grid, record, item, index, e, opts){
							actionEditResponse(record);
						},
						selectionchange: function(selectionModel, selected, opts){
							var tools = Ext.getCmp('responseGrid').getComponent('tools');
							
							if (selected.length > 0) {	
								tools.getComponent('view').setDisabled(false);
								tools.getComponent('edit').setDisabled(false);
								tools.getComponent('delete').setDisabled(false);
							} else {
								tools.getComponent('view').setDisabled(true);
								tools.getComponent('edit').setDisabled(true);
								tools.getComponent('delete').setDisabled(true);
							}
						}
					}					
				});
				
				var actionRefreshResponses = function() {
					Ext.getCmp('responseGrid').getStore().load();
				};
						
				var responseWindow = Ext.create('OSF.component.ResponseWindow', {
					title: 'Edit Answer',						
					postHandler: function(responseWin, response) {
						actionRefreshResponses();
					}
				});						
				var actionEditResponse = function(record) {
					responseWindow.componentId = record.get('componentId');
					responseWindow.show();
					responseWindow.edit(record);
				};
				
				var actionDeleteResponse = function(record) {
					Ext.Msg.show({
						title:'Remove Answer?',
						message: 'Are you sure you want to remove this Answer?',
						buttons: Ext.Msg.YESNO,
						icon: Ext.Msg.QUESTION,
						fn: function(btn) {
							if (btn === 'yes') {
								Ext.getCmp('questionGrid').setLoading("Removing...");
								Ext.Ajax.request({
									url: '../api/v1/resource/components/'+record.get('componentId')+'/questions/'+record.get('questionId') + '/responses/' + record.get('responseId'),
									method: 'DELETE',
									callback: function(){
										Ext.getCmp('responseGrid').setLoading(false);
									},
									success: function(){
										actionRefreshResponses();
									}
								});
							} 
						}
					});					
				};				
		
				var questionGrid = Ext.create('Ext.grid.Panel', {
					title: 'Questions',
					id: 'questionGrid',
					columnLines: true,
					store: {
						sorters: [{
							property: 'componentName',
							direction: 'ASC'
						}],	
						autoLoad: true,
						fields: [
							{
								name: 'createDts',
								type:	'date',
								dateFormat: 'c'
							},
							{
								name: 'questionUpdateDts',
								type:	'date',
								dateFormat: 'c'
							}
						],
						proxy: {
							type: 'ajax',
							url: '../api/v1/resource/componentquestions/' + '${user}'
						}		
					},
					columns: [
						{ text: 'Entry', dataIndex: 'componentName', width: 275
						},
						{ text: 'Question', dataIndex: 'question', flex: 1, minWidth: 200 },
						{ text: 'Post Date', dataIndex: 'createDts', width: 200, xtype: 'datecolumn', format:'m/d/y H:i:s' },
						{ text: 'Update Date', dataIndex: 'questionUpdateDts', width: 200, xtype: 'datecolumn', format:'m/d/y H:i:s' }
					],
					dockedItems: [
						{
							dock: 'top',
							xtype: 'toolbar',
							itemId: 'tools',
							items: [
								{
									text: 'Refresh',
									scale: 'medium',								
									iconCls: 'fa fa-2x fa-refresh',
									handler: function () {
										actionRefreshQuestions();
									}
								},
								{
									xtype: 'tbseparator'
								},
								{
									text: 'View Entry',
									itemId: 'view',
									scale: 'medium',
									disabled: true,
									iconCls: 'fa fa-2x fa-binoculars',
									handler: function () {
										actionViewEntry(Ext.getCmp('questionGrid').getSelectionModel().getSelection()[0].get('componentId'));										
									}									
								},								
								{
									text: 'Edit',
									itemId: 'edit',
									scale: 'medium',
									disabled: true,
									iconCls: 'fa fa-2x fa-edit',
									handler: function () {
										actionEditQuestion(Ext.getCmp('questionGrid').getSelectionModel().getSelection()[0]);										
									}									
								},
								{
									xtype: 'tbfill'
								},
								{
									text: 'Delete',
									itemId: 'delete',
									scale: 'medium',
									disabled: true,
									iconCls: 'fa fa-2x fa-trash-o',
									handler: function () {
										actionDeleteQuestion(Ext.getCmp('questionGrid').getSelectionModel().getSelection()[0]);
									}									
								}								
							]
						}
					],
					listeners: {
						itemdblclick: function(grid, record, item, index, e, opts){
							actionEditQuestion(record);
						},
						selectionchange: function(selectionModel, selected, opts){
							var tools = Ext.getCmp('questionGrid').getComponent('tools');
							
							if (selected.length > 0) {	
								tools.getComponent('view').setDisabled(false);
								tools.getComponent('edit').setDisabled(false);
								tools.getComponent('delete').setDisabled(false);
							} else {
								tools.getComponent('view').setDisabled(true);
								tools.getComponent('edit').setDisabled(true);
								tools.getComponent('delete').setDisabled(true);
							}
						}
					}					
				});
				
				var actionViewEntry = function(componentId){
				
					var frame = Ext.create('OSF.ux.IFrame', {							
					});
					
					var entryViewWindow = Ext.create('Ext.window.Window', {
						title: 'Entry',
						maximizable: true,
						modal: true,
						closeMode: 'destroy',
						width: '70%',
						height: '70%',
						layout: 'fit',
						items: [
							frame
						]
					});					
					entryViewWindow.show();
					frame.load('view.jsp?fullPage=true&id=' + componentId);
				};
				
				var actionRefreshQuestions = function() {
					Ext.getCmp('questionGrid').getStore().load();
				};
						
				var questionWindow = Ext.create('OSF.component.QuestionWindow', {
					title: 'Edit Question',						
					postHandler: function(questionWin, response) {
						actionRefreshQuestions();
					}
				});						
				var actionEditQuestion = function(record) {
					questionWindow.componentId = record.get('componentId');
					questionWindow.show();
					questionWindow.edit(record);
				};
				
				var actionDeleteQuestion = function(record) {
					Ext.Msg.show({
						title:'Remove Question?',
						message: 'Are you sure you want to remove this question?',
						buttons: Ext.Msg.YESNO,
						icon: Ext.Msg.QUESTION,
						fn: function(btn) {
							if (btn === 'yes') {
								Ext.getCmp('questionGrid').setLoading("Removing...");
								Ext.Ajax.request({
									url: '../api/v1/resource/components/'+record.get('componentId')+'/questions/'+record.get('questionId'),
									method: 'DELETE',
									callback: function(){
										Ext.getCmp('questionGrid').setLoading(false);
									},
									success: function(){
										actionRefreshQuestions();
									}
								});
							} 
						}
					});					
				};					
				
				
				var mainPanel = Ext.create("Ext.tab.Panel", {
					items: [
						questionGrid,
						responseGrid
					]
				})
				
				Ext.create('Ext.container.Viewport', {
					layout: 'fit',
					items: [
						mainPanel
					]
				});
				
			});
			
		</script>
		
	</stripes:layout-component>
</stripes:layout-render>			
