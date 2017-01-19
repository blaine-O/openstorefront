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

/* global Ext, CoreUtil */

Ext.define('OSF.form.ChecklistSummary', {
	extend: 'Ext.form.Panel',
	alias: 'osf.form.ChecklistSummary',

	layout: 'border',

	dockedItems: [
		{
			xtype: 'toolbar',
			itemId: 'tools',
			dock: 'bottom',
			items: [		
				{
					xtype: 'combo',
					itemId: 'workflowStatus',
					name: 'workflowStatus',										
					labelAlign: 'right',												
					margin: '0 0 5 0',
					editable: false,
					typeAhead: false,
					width: 400,
					fieldLabel: 'Status <span class="field-required" />',	
					displayField: 'description',
					valueField: 'code',
					labelSeparator: '',
					store: {
						autoLoad: true,
						proxy: {
							type: 'ajax',
							url: 'api/v1/resource/lookuptypes/WorkflowStatus'
						}
					}
				},
				{
					xtype: 'tbfill'
				},
				{
					xtype: 'tbtext',
					itemId: 'status'
				}				
			]
		}
	],
	initComponent: function () {		
		this.callParent();
		
		var summaryForm = this;
				
		summaryForm.topPanel = Ext.create('Ext.panel.Panel', {
			region: 'north',
			items: [
				{
					xtype: 'panel',	
					dock: 'top',
					html: '<b>Summary</b>'
				},
				{
					xtype: 'tinymce_textarea',		
					itemId: 'summary',
					dock: 'top',
					fieldStyle: 'font-family: Courier New; font-size: 12px;',
					style: { border: '0' },
					height: 300,
					width: '100%',
					name: 'summary',			
					maxLength: 32000,
					tinyMCEConfig: CoreUtil.tinymceConfigNoMedia()			
				}
			]
		});	
				
		summaryForm.recommendations = Ext.create('Ext.grid.Panel', {
			title: 'Recommendations',
			region: 'center',
			columnLines: true,
			store: {	
				proxy: {
					type: 'ajax',
					url: 'api/v1/resource/evaluations/{evaluationId}/checklist/{checklistId}/recommendations'
				}
			},
			columns: [
				{ text: 'Type', dataIndex: 'recommendationTypeDescription', width: 200 },
				{ text: 'Recommendation', dataIndex: 'recommendation', flex: 1, minWidth: 200, cellWrap: true },
				{ text: 'Reason', dataIndex: 'reason', width: 250, cellWrap: true }
			],
			listeners: {
					itemdblclick: function(grid, record, item, index, e, opts){
						actionAddEdit(record);
					},						
					selectionchange: function(selModel, selected, opts) {
						var tools = summaryForm.recommendations.getComponent('tools');

						if (selected.length > 0) {	
							tools.getComponent('delete').setDisabled(false);
							tools.getComponent('edit').setDisabled(false);	
												
						} else {							
							tools.getComponent('delete').setDisabled(true);
							tools.getComponent('edit').setDisabled(true);																				
						}
					}
			},
			dockedItems: [	
				{
					xtype: 'toolbar',
					itemId: 'tools',
					items: [
						{
							text: 'Add',
							iconCls: 'fa fa-2x fa-plus text-success',
							scale: 'medium',							
							handler: function() {
								actionAddEdit();
							}
						},
						{
							xtype: 'tbseparator'
						},
						{
							text: 'Edit',
							itemId: 'edit',
							disabled: true,
							iconCls: 'fa fa-2x fa-edit',
							scale: 'medium',							
							handler: function() {
								var record = summaryForm.recommendations.getSelectionModel().getSelection()[0];
								actionAddEdit(record);
							}
						},
						{
							xtype: 'tbfill'
						},
						{
							text: 'Delete',
							itemId: 'delete',
							disabled: true,
							iconCls: 'fa fa-2x fa-close text-danger',
							scale: 'medium',							
							handler: function() {
								var record = summaryForm.recommendations.getSelectionModel().getSelection()[0];								
								actionDelete(record);
							}
						}
					]
				}
			]			
		});
		summaryForm.add(summaryForm.topPanel);
		summaryForm.add(summaryForm.recommendations);
		
		summaryForm.loadRecommendations = function(){
			summaryForm.recommendations.getStore().load({
				url: 'api/v1/resource/evaluations/' + summaryForm.evaluationId + '/checklist/' + summaryForm.checklistId + '/recommendations'
			});
		};
		
		var actionAddEdit = function(record) {
			
			var addEditWin = Ext.create('Ext.window.Window', {
				title: 'Add/Edit Recommendations',
				modal: true,
				width: 700,
				height: 600,
				closeAction: 'destroy',
				layout: 'fit',
				items: [
					{
						xtype: 'form',
						itemId: 'form',
						bodyStyle: 'padding: 10px;',
						scrollable: true,
						layout: 'anchor',
						defaults: {
							labelAlign: 'top',
							labelSeparator: '',
							width: '100%'
						},
						items: [
							{
								xtype: 'hidden',
								name: 'recommendationId'
							},
							{
								xtype: 'combo',								
								name: 'recommendationType',																													
								margin: '0 0 5 0',
								editable: false,
								typeAhead: false,
								allowBlank: false,
								emptyText: 'Select',
								fieldLabel: 'Recommendation Type <span class="field-required" />',	
								displayField: 'description',
								valueField: 'code',
								labelSeparator: '',
								store: {
									autoLoad: true,
									proxy: {
										type: 'ajax',
										url: 'api/v1/resource/lookuptypes/RecommendationType'
									}
								}
							},
							{
								xtype: 'panel',	
								dock: 'top',
								html: '<b>Recommendation <span class="field-required" /></b>'
							},							
							{
								xtype: 'tinymce_textarea',										
								dock: 'top',
								fieldStyle: 'font-family: Courier New; font-size: 12px;',
								style: { border: '0' },
								height: 300,
								width: '100%',
								name: 'recommendation',			
								maxLength: 32000,
								tinyMCEConfig: CoreUtil.tinymceConfigNoMedia()		
							},
							{
								xtype: 'panel',	
								dock: 'top',
								html: '<b>Reason</b>'
							},							
							{
								xtype: 'tinymce_textarea',										
								dock: 'top',
								fieldStyle: 'font-family: Courier New; font-size: 12px;',
								style: { border: '0' },
								height: 300,
								width: '100%',
								name: 'reason',			
								maxLength: 32000,
								tinyMCEConfig: CoreUtil.tinymceConfigNoMedia()		
							}							
						],
						dockedItems: [
							{
								xtype: 'toolbar',
								dock: 'bottom',
								items: [
									{
										text: 'Save',
										formBind: true,
										scale: 'medium',
										iconCls: 'fa fa-2x fa-save',
										handler: function() {

											var recommendForm = addEditWin.getComponent('form');
											var data = recommendForm.getValues();

											if (!data.recommendation ||
												data.recommendation === '') 
											{
												Ext.Msg.show({
													title:'Validation',
													message: 'A recommendation is required.',
													buttons: Ext.Msg.OK,
													icon: Ext.Msg.ERROR,
													fn: function(btn) {												
													}
												});
											} else {
												CoreUtil.submitForm({
													url: 'api/v1/resource/evaluations/' + 
														summaryForm.evaluationId 
														+ '/checklist/' + 
														summaryForm.checklistId
														+ '/recommendations',
													method: 'POST',
													data: data,
													form: recommendForm,
													success: function(action, opts) {			
														summaryForm.loadRecommendations();
														addEditWin.close();
													}	
												});
											}
										}
									},
									{
										xtype: 'tbfill'
									},
									{
										text: 'Cancel',
										scale: 'medium',
										iconCls: 'fa fa-2x fa-close',
										handler: function() {
											addEditWin.close();
										}								
									}
								]
							}
						]						
					}
				]
			});
			addEditWin.show();	
			if (record) {
				addEditWin.getComponent('form').loadRecord(record);
			}
		};

		var actionDelete = function(record) {
			Ext.Msg.show({
				title:'Delete Recommendation?',
				message: 'Are you sure you want to delete this recommendation?',
				buttons: Ext.Msg.YESNO,
				icon: Ext.Msg.QUESTION,
				fn: function(btn) {
					if (btn === 'yes') {
						
						summaryForm.setLoading('Deleting...');
						Ext.Ajax.request({
							url: 'api/v1/resource/evaluations/' + 
									summaryForm.evaluationId 
									+ '/checklist/' + 
									summaryForm.checklistId
									+ '/recommendations/' +
									record.get('recommendationId'),
							method: 'DELETE',
							callback: function() {
								summaryForm.setLoading(false);
							},
							success: function() {
								summaryForm.loadRecommendations();
							}
						});						
						
					}
				}
			});			
		};

	},
	loadData: function(evaluationId, componentId, data, opts) {
		
		var summaryForm = this;
		
		summaryForm.setLoading(true);
		Ext.Ajax.request({
			url: 'api/v1/resource/evaluations/' + evaluationId + '/checklist/' + data.evaluationChecklist.checklistId,
			callback: function() {
				summaryForm.setLoading(false);
			},
			success: function(response, opts) {
				var recordData = Ext.decode(response.responseText);			
				var record = Ext.create('Ext.data.Model', {			
				});
				record.set(recordData);
				
				summaryForm.loadRecord(record);		
				summaryForm.fullEvalData = recordData;
				summaryForm.evaluationId = evaluationId; 
				summaryForm.checklistId = data.evaluationChecklist.checklistId; 

				summaryForm.loadRecommendations();

				summaryForm.getComponent('tools').getComponent('workflowStatus').on('change', function(){
					summaryForm.saveData();			
				});

				summaryForm.topPanel.getComponent('summary').on('change', function(){
					summaryForm.saveData();			
				}, undefined, {
					buffer: 2000
				});
			}
		});
				
		opts.commentPanel.loadComments(evaluationId, "Checklist Summary", data.evaluationChecklist.checklistId);	
	},
	saveData: function() {
		var summaryForm = this;
		
		var data = summaryForm.getValues();
		
		if (data.summary !== summaryForm.fullEvalData.summary ||
			data.workflowStatus !== summaryForm.fullEvalData.workflowStatus) {
				CoreUtil.submitForm({
					url: 'api/v1/resource/evaluations/' + 
						summaryForm.evaluationId 
						+ '/checklist/' + 
						summaryForm.checklistId,
					method: 'PUT',
					data: data,
					form: summaryForm,
					noLoadmask: true,
					success: function(action, opts) {			
						Ext.toast('Saved Summary');
						summaryForm.getComponent('tools').getComponent('status').setText('Saved at ' + Ext.Date.format(new Date(), 'g:i:s A'));
					}	
				});
		}
	}
	
});
