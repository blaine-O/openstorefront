/* 
 * Copyright 2016 Space Dynamics Laboratory - Utah State University Research Foundation.
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
/* global Ext */

Ext.define('OSF.component.EntryChangeRequestWindow', {
	extend: 'Ext.window.Window',
	alias: 'osf.widget.EntryChangeRequestWindow',
	
	title: 'Change Requests',
	modal: true,
	maximizable: true,
	width: '80%',
	height: '80%',
	layout: {
		type: 'hbox',
		align: 'stretch'
	},	
	adminMode: false,
	dockedItems: [
		{
			xtype: 'toolbar',
			itemId: 'tools',
			dock: 'bottom',
			items: [
				{
					xtype: 'tbfill'
				}, 
				{
					text: 'Close',
					iconCls: 'fa fa-close',
					handler: function(){
						this.up('window').hide();
					}
				}
			]
		}						
	],
	initComponent: function () {
		this.callParent();
		
		var changeRequestWindow = this;
		
		var adminMode = changeRequestWindow.adminMode;
		
		var viewTemplate = new Ext.XTemplate();		
		Ext.Ajax.request({
			url: 'Router.action?page=shared/entrySimpleViewTemplate.jsp',
			success: function(response, opts){
				viewTemplate.set(response.responseText, true);
			}
		});
		
		changeRequestWindow.submissionPanel = Ext.create('OSF.component.SubmissionPanel', {											
				formWarningMessage: changeRequestWindow.changeRequestWarning,
				submitForReviewUrl: function (componentId){
					return 'api/v1/resource/componentsubmissions/' + componentId+ '/submitchangerequest';
				},
				cancelSubmissionHandlerYes: function(){
					changeRequestWindow.submissionWindow.completeClose=true;
					changeRequestWindow.submissionWindow.close();
					changeRequestWindow.submissionWindow.completeClose=false;
					if (changeRequestWindow.successHandler) {
						changeRequestWindow.successHandler();
					}
				},
				cancelSubmissionHandlerNo: function(){
					changeRequestWindow.submissionWindow.completeClose=true;
					changeRequestWindow.submissionWindow.close();
					changeRequestWindow.submissionWindow.completeClose=false;
					if (changeRequestWindow.successHandler) {
						changeRequestWindow.successHandler();
					}
				},				
				handleSubmissionSuccess: function(response, opts) {
					changeRequestWindow.submissionWindow.completeClose=true;
					changeRequestWindow.submissionWindow.close();
					changeRequestWindow.submissionWindow.completeClose=false;
					if (changeRequestWindow.changeGrid.getStore().getProxy().url) {
						changeRequestWindow.changeGrid.getStore().reload();
					}
					if (changeRequestWindow.successHandler) {
						changeRequestWindow.successHandler();
					}					
				}
		});
		
		changeRequestWindow.submissionWindow = Ext.create('Ext.window.Window', {				
			title: 'Change Request Form',
			iconCls: 'fa fa-file-text-o',
			layout: 'fit',
			modal: true,
			width: '90%',
			height: '90%',					
			maximizable: true,			
			completeClose: false,
			items: [
				changeRequestWindow.submissionPanel
			],
			listeners: {
				beforeclose: function(panel, opts) {
					if (!(panel.completeClose)){
						changeRequestWindow.submissionPanel.cancelSubmissionHandler(true);
					}
					return panel.completeClose;
				}
			}
		});
//		changeRequestWindow.submissionPanel.cancelSubmissionHandler = function() {
//			Ext.Msg.show({
//				title:'Confirm Cancel?',
//				message: 'Are you sure you want to cancel your change request? <br><br><b>Yes</b>, will remove change request<br> <b>No</b>, will cancel the form and will NOT remove existing change request',
//				buttons: Ext.Msg.YESNOCANCEL,
//				icon: Ext.Msg.QUESTION,
//				fn: function(btn) {
//					if (btn === 'yes') {
//							changeRequestWindow.submissionPanel.setLoading('Canceling Change Request...');
//							Ext.Ajax.request({
//								url: 'api/v1/resource/components/' + changeRequestWindow.changeRequestId + '/cascade',
//								method: 'DELETE',
//								callback: function () {
//									changeRequestWindow.submissionPanel.setLoading(false);
//								},
//								success: function (response, opts) {
//									changeRequestWindow.submissionWindow.completeClose=true;
//									changeRequestWindow.submissionWindow.close();
//									changeRequestWindow.submissionWindow.completeClose=false;
//									changeRequestWindow.changeGrid.getStore().reload();
//									if (changeRequestWindow.successHandler) {
//										changeRequestWindow.successHandler();
//									}
//								}
//							});							
//					} else if (btn === 'no') {
//						changeRequestWindow.submissionWindow.completeClose=true;
//						changeRequestWindow.submissionWindow.close();
//						changeRequestWindow.submissionWindow.completeClose=false;
//					} 
//				}				
//			});
//		};		

		changeRequestWindow.editChangeRequest = function(changeRequestId, record, editCallback) {
			changeRequestWindow.changeRequestId = changeRequestId;
			
			if (!adminMode) {
				//open form
				changeRequestWindow.submissionWindow.show();			
				changeRequestWindow.submissionPanel.editSubmission(changeRequestId);
				if (editCallback) {
					editCallback();
				}
			} else {
				changeRequestWindow.adminEditHandler(record, changeRequestWindow);
			}
		};
		
		changeRequestWindow.newChangeRequest = function(currentComponentId, callback, external) {
			Ext.Ajax.request({
				url: 'api/v1/resource/components/' + currentComponentId + '/changerequest',
				method: 'POST',
				callback: function () {
					changeRequestWindow.setLoading(false);
					if (callback) {
						callback();
					}
				},
				success: function (response, opts) {
					if (!external){
						changeRequestWindow.changeGrid.getStore().reload();
					}

					var data = Ext.decode(response.responseText);										
					changeRequestWindow.editChangeRequest(data.componentId);
					if (changeRequestWindow.successHandler) {
						changeRequestWindow.successHandler();
					}
				}
			});
		};
		
		changeRequestWindow.deleteChangeRequest = function(changeRequestId, external) {
			
			Ext.Msg.show({
				title: 'Delete Change Request?',
				message: 'Are you sure you want to delete selected change request? ',
				buttons: Ext.Msg.YESNO,
				icon: Ext.Msg.QUESTION,
				fn: function(btn) {
					if (btn === 'yes') {
						changeRequestWindow.setLoading('Removing change request...');
						Ext.Ajax.request({
							url: 'api/v1/resource/components/' + changeRequestId + '/cascade',
							method: 'DELETE',
							callback: function() {
								changeRequestWindow.setLoading(false);
							},
							success: function(response, opts) {	
								if (!external) {
									changeRequestWindow.changeGrid.getStore().reload();
									changeRequestWindow.changeViewPanel.update(undefined);
								}
								
								if (changeRequestWindow.successHandler) {
									changeRequestWindow.successHandler();
								}
							}
						});
					} 
				}
			});			
		};
				
		changeRequestWindow.changeGrid = Ext.create('Ext.grid.Panel', {
			region: 'west',
			split: true,
			border: true,
			columnLines: true,
			width: '33%',
			store: Ext.create('Ext.data.Store', {
				autoLoad: false,				
				proxy: {
					url: '',
					type: 'ajax'
				}, 
				fields: [
					{
						name: 'updateDts',
						type:	'date',
						dateFormat: 'c'
					}					
				],
				listeners: {
					load: function(store, records) {
						var grid = changeRequestWindow.changeGrid;
						var tools = grid.getComponent('tools');
						
						if (!adminMode) {
							if (records.length > 0) {
								tools.getComponent('newBtn').setHidden(true);
							} else {
								tools.getComponent('newBtn').setHidden(false);
							}
						}
					}
				}
			}),
			columns: [
				{ text: 'Name', dataIndex: 'name', width: 150 },
				{ text: 'Approval Status', align: 'center', dataIndex: 'approvalState', width: 150, tooltip: 'Changes are removed upon Approval',
					renderer: function(value, metaData){
								var text = value;
								if (value === 'A') {
									text = 'Approved';
									metaData.tdCls = 'alert-success';
								} else if (value === 'P') {
									text = 'Pending';
									metaData.tdCls = 'alert-warning';
								} else if (value === 'N') {
									text = 'Not Submitted';
								}
								return text;
					}
				},
				{ text: 'Update Date', dataIndex: 'updateDts', flex: 1, xtype: 'datecolumn', format:'m/d/y H:i:s' },
				{ text: 'Update User', dataIndex: 'updateUser', width: 200, hidden: true },
				{ text: 'Approval Email', dataIndex: 'notifyOfApprovalEmail', width: 200, hidden: true }
			],
			listeners: {
				selectionchange: function(selectionModel, records){
					var grid = changeRequestWindow.changeGrid;
					var tools = grid.getComponent('tools');
					if (records.length === 1) {
						tools.getComponent('editBtn').setDisabled(false);
						tools.getComponent('unsubmitBtn').setDisabled(false);
						tools.getComponent('removeBtn').setDisabled(false);
						tools.getComponent('tool-approveBtn').setDisabled(false);
						tools.getComponent('messageBtn').setDisabled(false);
					} else {
						tools.getComponent('editBtn').setDisabled(true);
						tools.getComponent('unsubmitBtn').setDisabled(true);
						tools.getComponent('removeBtn').setDisabled(true);
						tools.getComponent('tool-approveBtn').setDisabled(true);
						tools.getComponent('messageBtn').setDisabled(true);
					}

					//load preview
					if (records[0]){
						var componentId = records[0].get('componentId');
						
						changeRequestWindow.changeViewPanel.setLoading(true);
						Ext.Ajax.request({
							url: 'api/v1/resource/components/' + componentId + '/detail',
							callback: function(){
								changeRequestWindow.changeViewPanel.setLoading(false);
							},
							success: function(response, opts) {
								var data = Ext.decode(response.responseText);								
								changeRequestWindow.changeViewPanel.update(data);								
							}
						});
					} else {
						changeRequestWindow.changeViewPanel.update(undefined);						
					}
				}	
			},
			dockedItems: [
				{
					xtype: 'toolbar',
					dock: 'top',
					itemId: 'tools',
					items: [
						{
							text: 'Approve Change',
							itemId: 'tool-approveBtn',
							iconCls: 'fa fa-reply',
							disabled: true,
							hidden: true,
							handler: function(){
								var changeRequestWindow = this.up('window');
								var changeRequestComponentId = changeRequestWindow.changeGrid.getSelection()[0].get('componentId');

								changeRequestWindow.setLoading('Approving Change...');
								Ext.Ajax.request({
									url: 'api/v1/resource/components/' + changeRequestComponentId + '/mergechangerequest',
									method: 'PUT',
									callback: function(){
										changeRequestWindow.setLoading(false);
									},
									success: function(response, opts) {
										changeRequestWindow.changeGrid.getStore().reload();
										changeRequestWindow.loadCurrentView();								
										if (changeRequestWindow.successHandler) {
											changeRequestWindow.successHandler();
										}
									}
								});												
							}
						},						
						{
							text: 'New Change Request',
							itemId: 'newBtn',
							iconCls: 'fa fa-plus',						
							handler: function(){								
								changeRequestWindow.setLoading('Creating a new change request...');
								changeRequestWindow.newChangeRequest(changeRequestWindow.currentComponentId);
							}
						},						
						{
							text: 'Edit',
							itemId: 'editBtn',
							iconCls: 'fa fa-edit',
							disabled: true,
							handler: function(){ 
								var changeRequestComponentId = this.up('grid').getSelectionModel().getSelection()[0].get('componentId');
								changeRequestWindow.editChangeRequest(changeRequestComponentId, this.up('grid').getSelectionModel().getSelection()[0]);								
							}
						},
						{
							text: 'Message',
							itemId: 'messageBtn',
							iconCls: 'fa fa-envelope-o',
							disabled: true,
							hidden: true,
							handler: function(){
								var emails = changeRequestWindow.changeGrid.getSelection()[0].get('ownerEmail');
								var messageWindow = Ext.create('OSF.component.MessageWindow', {					
											closeAction: 'destory',
											alwaysOnTop: true,
											initialToUsers: emails
								}).show();
							}
						},
						{
							text: 'Unsubmit',
							itemId: 'unsubmitBtn',
							iconCls: 'fa fa-close',
							disabled: true,
							handler: function(){ 
								var changesGrid = this.up('grid');
								var changeRequestComponentId = this.up('grid').getSelectionModel().getSelection()[0].get('componentId');
								var name = this.up('grid').getSelectionModel().getSelection()[0].get('name');
								
								Ext.Msg.show({
									title:'Unsubmit?',
									message: 'Are sure you want to unsubmit change request <b>' + name + '</b>?',
									buttons: Ext.Msg.YESNO,
									icon: Ext.Msg.QUESTION,
									fn: function(btn) {
										if (btn === 'yes') {
											changeRequestWindow.setLoading('Unsubmitting...');
											Ext.Ajax.request({
												url: 'api/v1/resource/componentsubmissions/' + changeRequestComponentId + '/unsubmitchangerequest',
												method: 'PUT',
												callback: function(){
													changeRequestWindow.setLoading(false);
												},
												success: function(response, opts) {
													changesGrid.getStore().reload();
												}
											});	
										} 
									}
								});
							}
						},						
						{
							xtype: 'tbfill'
						},
						{
							text: 'Remove',	
							itemId: 'removeBtn',
							iconCls: 'fa fa-trash',							
							disabled: true,
							handler: function(){
								var changesGrid = this.up('grid');
								var changeRequestComponentId = this.up('grid').getSelectionModel().getSelection()[0].get('componentId');
								
								changeRequestWindow.deleteChangeRequest(changeRequestComponentId);
							}
						}
					]
				}
			]
		});
						
		changeRequestWindow.changeViewPanel = Ext.create('Ext.panel.Panel', {
			region: 'east',
			width: '33%',
			title: 'Selected Request',
			autoScroll: true,
			scrollable: true,
			border: true,
			split: true,
			bodyStyle: 'padding: 10px;',
			tpl: viewTemplate							
		});						
		
		changeRequestWindow.currentViewPanel = Ext.create('Ext.panel.Panel', {
			region: 'center',
			flex: 1,
			title: 'Current Entry',
			header: {
				cls: 'accent-background'
			},
			autoScroll: true,
			border: true,
			split: true,
			bodyStyle: 'padding: 10px;',
			tpl: viewTemplate
		});		
		
		changeRequestWindow.add(changeRequestWindow.changeGrid);
		changeRequestWindow.add(changeRequestWindow.changeViewPanel);
		changeRequestWindow.add(changeRequestWindow.currentViewPanel);
		
		if (adminMode) {
			var grid = changeRequestWindow.changeGrid;
			var tools = grid.getComponent('tools');
			tools.getComponent('newBtn').setHidden(true);	
			tools.getComponent('unsubmitBtn').setHidden(true);	
			tools.getComponent('tool-approveBtn').setHidden(false);
			tools.getComponent('messageBtn').setHidden(false);
		} 
		
	},
	
	loadComponent: function(componentId, name) {
		var changeRequestWindow = this;
		
		changeRequestWindow.currentComponentId = componentId;
		changeRequestWindow.setTitle('Change Requests - ' + name);
		
		//load grid		
		changeRequestWindow.changeGrid.getStore().load({
			url:'api/v1/resource/components/' + componentId + '/pendingchanges'			
		});
		
		changeRequestWindow.loadCurrentView();
	},
	
	loadCurrentView: function(){
		var changeRequestWindow = this;
		//load current view
		changeRequestWindow.currentViewPanel.setLoading(true);		
		Ext.Ajax.request({
			url: 'api/v1/resource/components/' + changeRequestWindow.currentComponentId + '/detail',
			callback: function(){
				changeRequestWindow.currentViewPanel.setLoading(false);		
			}, 
			success: function(response, opts) {
				var data = Ext.decode(response.responseText);				
				changeRequestWindow.currentViewPanel.update(data);
			}
		});		
	}
	
});
