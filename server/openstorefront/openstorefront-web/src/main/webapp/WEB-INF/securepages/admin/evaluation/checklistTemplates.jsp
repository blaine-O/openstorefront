<%-- 
    Document   : checklistTemplates
    Created on : Oct 11, 2016, 2:29:40 PM
    Author     : dshurtleff
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<stripes:layout-render name="../../../../layout/toplevelLayout.jsp">
    <stripes:layout-component name="contents">
	
	<stripes:layout-render name="../../../../layout/adminheader.jsp">		
	</stripes:layout-render>		
		
	<script type="text/javascript">
		/* global Ext, CoreUtil */
		Ext.onReady(function(){	
			
			var addEditWindow = Ext.create('Ext.window.Window', {
				title: 'Add/Edit Checkist Template',
				iconCls: 'fa fa-edit',
				modal: true,
				width: '75%',
				height: '75%',
				maximizable: true,
				layout: 'fit',
				items: [
					{
						xtype: 'form',
						itemId: 'form',
						layout: 'border',
						dockedItems: [
							{
								xtype: 'toolbar',
								dock: 'bottom',
								items: [
									{
										text: 'Save',
										formBind: true,
										iconCls: 'fa fa-2x fa-save icon-button-color-save icon-vertical-correction-edit',
										width: '100px',
										scale: 'medium',
										handler: function() {
											var form = this.up('form');
											var win = this.up('window');
											var data = form.getValues();

											data.questions = [];											
											Ext.getCmp('questionsInTemplate').getStore().each(function(item){
												data.questions.push({
													questionId: item.get('questionId')
												});
											});

											var method = 'POST';
											var update = '';
											if (data.checklistTemplateId) {
												update = '/' + data.checklistTemplateId;
												method = 'PUT';
											}

											CoreUtil.submitForm({
												url: 'api/v1/resource/checklisttemplates' + update,
												method: method,
												data: data,
												form: form,
												success: function(){
													actionRefresh();
													form.reset();
													win.close();
												}
											});	
											
										}
									},
									{
										xtype: 'tbfill'
									},
									{
										text: 'Cancel',									
										iconCls: 'fa fa-2x fa-close icon-button-color-warning icon-vertical-correction',
										scale: 'medium',
										handler: function() {
											this.up('window').close();
										}										
									}							
								]
							}
						],
						items: [
							{
								xtype: 'hidden',
								name: 'checklistTemplateId'
							},
							{
								xtype: 'panel',
								region: 'north',
								layout: 'anchor',
								bodyStyle: 'padding: 10px;',
								defaults: {
									labelAlign: 'top',
									labelSeparator: '',
									width: '100%'
								},
								items: [
									{
										xtype: 'textfield',
										name: 'name',
										fieldLabel: 'Name <span class="field-required" />',
										allowBlank: false,
										maxLength: 255										
									},
									{
										xtype: 'textfield',
										name: 'description',
										fieldLabel: 'Description <span class="field-required" />',
										allowBlank: false,
										maxLength: 255										
									},	
									{
										xtype: 'panel',
										html: '<b>Instructions</b>'
									},
									{
										xtype: 'tinymce_textarea',
										fieldStyle: 'font-family: Courier New; font-size: 12px;',
										style: { border: '0' },
										name: 'instructions',								
										height: 300,
										maxLength: 16384,
										tinyMCEConfig: CoreUtil.tinymceConfig()
									}									
								]
							},
							{
								xtype: 'panel',
								region: 'center',
								layout: {
									type: 'hbox',									
									align: 'stretch'
								},
								items: [
									{
										xtype: 'grid',
										title: 'Question Pool - <span class="alert-warning"> drag to add <i class="fa fa-lg fa-arrow-right"></i> </span>',
										id: 'questionPool',
										width: '50%',
										margin: '0 5 0 0',
										columnLines: true,
										store: {
											autoLoad: false,
											sorters: [
												new Ext.util.Sorter({
													property: 'qid',
													direction: 'ASC'
												})
											],
											proxy: {
												type: 'ajax',
												url: 'api/v1/resource/checklistquestions',
												reader: {
													type: 'json',
													rootProperty: 'data',
													totalProperty: 'totalNumber'
												}												
											}
										},
										viewConfig: {
											plugins: {
												ptype: 'gridviewdragdrop',
												dragText: 'Drag and drop to Add to template'
											}
										},										
										columns: [
											{ text: 'QID', dataIndex: 'qid', align: 'center', width: 125 },
											{ text: 'Section', dataIndex: 'evaluationSection', align: 'center', width: 200,
												renderer: function(value, metadata, record) {
													return record.get('evaluationSectionDescription');
												}
											},												
											{ text: 'Question', dataIndex: 'question',  flex: 1,
												renderer: function(value, metadata, record) {
													return Ext.util.Format.stripTags(value);
												}												
											}
										]
									},
									{
										xtype: 'grid',
										id: 'questionsInTemplate',
										title: 'Questions In Template - <span class="alert-warning"><i class="fa fa-lg fa-arrow-left"></i> drag to remove </span>',
										width: '50%',
										columnLines: true,
										store: {											
										},
										viewConfig: {
											plugins: {
												ptype: 'gridviewdragdrop',
												dragText: 'Drag and drop to delete from template'												
											},
											listeners: {
												drop: function(node, data, overModel, dropPostition, opts){													
												}
											}
										},										
										columns: [
											{ text: 'QID', dataIndex: 'qid', align: 'center', width: 125 },
											{ text: 'Section', dataIndex: 'evaluationSection', align: 'center', width: 200,
												renderer: function(value, metadata, record) {
													return record.get('evaluationSectionDescription');
												}
											},												
											{ text: 'Question', dataIndex: 'question',  flex: 1,
												renderer: function(value, metadata, record) {
													return Ext.util.Format.stripTags(value);
												}												
											}
										]
									}
								]
							}
						]
					}
				]
			});
			
			var checklistGrid = Ext.create('Ext.grid.Panel', {
				id: 'checklistGrid',
				title: 'Manage Checklist Templates <i class="fa fa-question-circle"  data-qtip="Manage checklist templates that can be added to evaluation template."></i>',
				columnLines: true,
				store: {
					autoLoad: true,
					fields: [
						{ name: 'createDts', 
							type: 'date',
							dateFormat: 'c'							
						},
						{ name: 'updateDts',
							type: 'date',
							dateFormat: 'c'				
						}							
					],
					proxy: {
						type: 'ajax',
						url: 'api/v1/resource/checklisttemplates'
					},
					listeners: {
						beforeLoad: function(store, operation, eOpts){
							store.getProxy().extraParams = {
								status: Ext.getCmp('filterActiveStatus').getValue()									
							};
						}
					}					
				},
				columns: [
					{ text: 'Name', dataIndex: 'name', width: 250},	
					{ text: 'Description', dataIndex: 'description', flex: 1},	
					{ text: 'Create Date', dataIndex: 'createDts', xtype: 'datecolumn', format:'m/d/y H:i:s',  width: 175 },
					{ text: 'Create User', dataIndex: 'createUser', width: 175 },
					{ text: 'Update Date', dataIndex: 'updateDts', xtype: 'datecolumn', format:'m/d/y H:i:s',  width: 175 },
					{ text: 'Update User', dataIndex: 'updateUser', width: 175 }
				],
				listeners: {
					itemdblclick: function(grid, record, item, index, e, opts){
						actionAddEdit(record);
					},						
					selectionchange: function(selModel, selected, opts) {
						var tools = Ext.getCmp('checklistGrid').getComponent('tools');

						if (selected.length > 0) {	
							tools.getComponent('view').setDisabled(false);
							tools.getComponent('copy').setDisabled(false);							
							tools.getComponent('edit').setDisabled(false);							
							tools.getComponent('togglestatus').setDisabled(false);
							tools.getComponent('delete').setDisabled(false);
						} else {
							tools.getComponent('view').setDisabled(true);
							tools.getComponent('copy').setDisabled(true);
							tools.getComponent('edit').setDisabled(true);							
							tools.getComponent('togglestatus').setDisabled(true);
							tools.getComponent('delete').setDisabled(true);
						}
					}
				},				
				dockedItems: [
					{
						xtype: 'toolbar',
						dock: 'top',
						items: [
							Ext.create('OSF.component.StandardComboBox', {
								id: 'filterActiveStatus',									
								emptyText: 'Active',
								value: 'A',
								fieldLabel: 'Active Status',
								name: 'activeStatus',									
								typeAhead: false,
								editable: false,
								width: 200,							
								listeners: {
									change: function(filter, newValue, oldValue, opts){
										actionRefresh();
									}
								},
								storeConfig: {
									customStore: {
										fields: [
											'code',
											'description'
										],
										data: [												
											{
												code: 'A',
												description: 'Active'
											},
											{
												code: 'I',
												description: 'Inactive'
											}
										]
									}
								}
							}) 															
						]
					},					
					{
						dock: 'top',
						xtype: 'toolbar',
						itemId: 'tools',
						items: [
							{
								text: 'Refresh',
								iconCls: 'fa fa-2x fa-refresh icon-button-color-refresh icon-vertical-correction',
								scale: 'medium',
								handler: function(){
									actionRefresh();
								}
							},
							{
								xtype: 'tbseparator'
							},
							{
								text: 'Add',
								iconCls: 'fa fa-2x fa-plus icon-button-color-save',									
								scale: 'medium',
								width: '100px',
								handler: function(){
									actionAddEdit();
								}
							},
							{
								text: 'Edit',
								iconCls: 'fa fa-2x fa-edit icon-button-color-edit icon-vertical-correction-edit',
								itemId: 'edit',
								disabled: true,									
								scale: 'medium',
								width: '100px',
								handler: function(){
									var record = Ext.getCmp('checklistGrid').getSelectionModel().getSelection()[0];
									actionAddEdit(record);
								}
							},
							{
								text: 'View',
								iconCls: 'fa fa-2x fa-eye icon-button-color-view icon-vertical-correction-view',
								itemId: 'view',
								disabled: true,									
								scale: 'medium',
								width: '110px',
								handler: function(){
									var record = Ext.getCmp('checklistGrid').getSelectionModel().getSelection()[0];
									actionView(record);
								}
							},							
							{
								xtype: 'tbseparator'
							},
							{
								text: 'Toggle Status',
								iconCls: 'fa fa-2x fa-power-off icon-button-color-default',
								itemId: 'togglestatus',
								disabled: true,								
								scale: 'medium',
								handler: function(){
									var record = Ext.getCmp('checklistGrid').getSelectionModel().getSelection()[0];
									actionToggleStatus(record);
								}
							},	
							{
								text: 'Copy',
								iconCls: 'fa fa-2x fa-clone icon-button-color-default icon-vertical-correction-edit',
								itemId: 'copy',
								disabled: true,								
								scale: 'medium',
								width: '110px',
								handler: function(){
									var record = Ext.getCmp('checklistGrid').getSelectionModel().getSelection()[0];
									actionCopy(record);
								}
							},								
							{
								xtype: 'tbfill'
							},							
							{
								text: 'Delete',
								iconCls: 'fa fa-2x fa-trash icon-button-color-warning icon-vertical-correction',
								itemId: 'delete',
								disabled: true,									
								scale: 'medium',
								handler: function(){
									var record = Ext.getCmp('checklistGrid').getSelectionModel().getSelection()[0];
									actionDelete(record);
								}
							}					
							
						]
					}
				]				
			});
			
			addComponentToMainViewPort(checklistGrid);	
			
			var actionRefresh = function() {
				Ext.getCmp('checklistGrid').getStore().reload();					
			};
			
			var actionAddEdit = function(record) {
				addEditWindow.show();
				addEditWindow.getComponent('form').reset();
				
				Ext.getCmp('questionsInTemplate').getStore().removeAll();
				if (record) {
					addEditWindow.getComponent('form').loadRecord(record);
					
					Ext.getCmp('questionPool').getStore().load(function(records, operation, success) {
						var recordsInTemplate = [];
						var recordsAvaliable = [];
						Ext.Array.each(records, function(question) {
							var questionInTemplate = false;
							
							if (record.get('questions')) {
								Ext.Array.each(record.get('questions'), function(templateQuestion) {
									if (templateQuestion.questionId === question.get('questionId')) {
										questionInTemplate = true;
									}
								});
							}
							
							if (questionInTemplate) {
								recordsInTemplate.push(question);
							} else {
								recordsAvaliable.push(question);
							}
						});
						Ext.getCmp('questionPool').getStore().loadData(recordsAvaliable);
						Ext.getCmp('questionsInTemplate').getStore().loadData(recordsInTemplate);
					});					
				} else {
					Ext.getCmp('questionPool').getStore().load();
				}
			};
			
			var actionView = function(record) {			
				var actionPreviewNextRecord = function(next) {
					if (next) {
						Ext.getCmp('checklistGrid').getSelectionModel().selectNext();						
					} else {
						Ext.getCmp('checklistGrid').getSelectionModel().selectPrevious();						
					}
					viewWin.update(Ext.getCmp('checklistGrid').getSelection()[0]);					
				};
				
				var previewCheckButtons = function() {	
					if (Ext.getCmp('checklistGrid').getSelectionModel().hasPrevious()) {
						Ext.getCmp('viewWinTools-previousBtn').setDisabled(false);
					} else {
						Ext.getCmp('viewWinTools-previousBtn').setDisabled(true);
					}
					
					if (Ext.getCmp('checklistGrid').getSelectionModel().hasNext()) {
						Ext.getCmp('viewWinTools-nextBtn').setDisabled(false);
					} else {
						Ext.getCmp('viewWinTools-nextBtn').setDisabled(true);
					}					
				};				
				
				var viewWin = Ext.create('Ext.window.Window', {
					title: 'Checklist View',
					iconCls: 'fa fa-eye',
					modal: true,
					closeAction: 'destroy',
					width: '75%',
					height: '75%',
					maxizable: true,
					autoScroll: true,					
					bodyStyle: 'padding: 10px;',
					dockedItems: [
						{
							xtype: 'toolbar',
							dock: 'bottom',
							items: [
								{
									text: 'Previous',
									id: 'viewWinTools-previousBtn',
									iconCls: 'fa fa-2x fa-arrow-left icon-button-color-default icon-vertical-correction',
									scale: 'medium',
									handler: function() {
										actionPreviewNextRecord(false);
									}									
								},
								{
									xtype: 'tbfill'
								},
								{
									text: 'Close',
									iconCls: 'fa fa-2x fa-close icon-button-color-warning icon-vertical-correction',
									scale: 'medium',
									handler: function() {
										this.up('window').close();
									}
								},
								{
									xtype: 'tbfill'
								},
								{
									text: 'Next',
									id: 'viewWinTools-nextBtn',
									iconCls: 'fa fa-2x fa-arrow-right icon-button-color-default icon-vertical-correction',
									iconAlign: 'right',
									scale: 'medium',
									handler: function() {
										actionPreviewNextRecord(true);
									}									
								}
							]
						}
					],
					tpl: '<h1>Template - {name}</h1>'+ 
						 '{instructions}<br>' + 
						 '<h2>Questions</h2>' +
						 '<table style="width:100%; border-collapse: collapse; border: 1px solid #ddd; text-align: left;">' +
						 '	<tr>'+
						 '		<th style="padding: 15px; background-color: grey; color: white;border: 1px solid #ddd;">QID</th>'+
						 '		<th style="padding: 15px; background-color: grey; color: white;border: 1px solid #ddd;">Section</th>'+
						 '		<th style="padding: 15px; background-color: grey; color: white;border: 1px solid #ddd;">Question</th>'+
						 '		<th style="padding: 15px; background-color: grey; color: white;border: 1px solid #ddd;">Objective</th>'+
						 '		<th style="padding: 15px; background-color: grey; color: white;border: 1px solid #ddd;">Scoring Criteria</th>'+
						 '	</tr>' +
						 '<tpl for="questions">'+ 
						 '  <tr>' +
						 '		<td style="padding: 15px;border: 1px solid #ddd;">{qid}</td> ' +
						 '		<td style="padding: 15px;border: 1px solid #ddd;">{evaluationSectionDescription}</td> ' +
						 '		<td style="padding: 15px;border: 1px solid #ddd;">{question}</td> ' +
						 '		<td style="padding: 15px;border: 1px solid #ddd;">{objective}</td> ' +
						 '		<td style="padding: 15px;border: 1px solid #ddd;">{scoreCriteria}</td> ' +
						 '  </tr>' + 
						 '</tpl>' 
				});		
				
				//get detail view				
				viewWin.show();
				viewWin.setLoading(true);
				Ext.Ajax.request({
					url: 'api/v1/resource/checklisttemplates/'+ record.get('checklistTemplateId') + '/details',
					callback: function(){
						viewWin.setLoading(false);
					},
					success: function(response, opts){	
						var data = Ext.decode(response.responseText);
						viewWin.update(data);
					}
				});
				previewCheckButtons();				
			};
		
			var actionToggleStatus = function(record) {
				Ext.getCmp('checklistGrid').setLoading("Updating Status...");
				var checklistTemplateId = Ext.getCmp('checklistGrid').getSelection()[0].get('checklistTemplateId');
				var currentStatus = Ext.getCmp('checklistGrid').getSelection()[0].get('activeStatus');

				var method = 'PUT';
				var urlEnd = '/activate';
				if (currentStatus === 'A') {
					method = 'DELETE';
					urlEnd = '';
				}					
				Ext.Ajax.request({
					url: 'api/v1/resource/checklisttemplates/' + checklistTemplateId + urlEnd,
					method: method,
					callback: function(){
						Ext.getCmp('checklistGrid').setLoading(false);
					},
					success: function(response, opts){						
						actionRefresh();
					}
				});				
			};			
			
			var actionCopy = function(record) {
				var data = record.data;
				data.name = data.name + ' - Copy';
				data.checklistTemplateId = null;
				
				Ext.Ajax.request({
					url: 'api/v1/resource/checklisttemplates',
					method: 'POST',
					jsonData: data,												
					success: function(){
						actionRefresh();													
					}
				});				
			};
			
			var actionDelete = function(record) {
				
				checklistGrid.setLoading('Checking for references...');
				Ext.Ajax.request({
					url: 'api/v1/resource/checklisttemplates/' + record.get('checklistTemplateId') + '/inuse',
					callback: function(){
						checklistGrid.setLoading(false);
					},
					success: function(response, opts){
						var references = response.responseText;

						if (references && references !== 'false') {
							Ext.Msg.alert('Existing References', 'Unable to delete; Delete evaluation checklists first.');
						} else {
							Ext.Msg.show({
								title:'Delete Checklist Template?',
								message: 'Are you sure you want to delete this checklist template?',
								buttons: Ext.Msg.YESNO,
								icon: Ext.Msg.QUESTION,
								fn: function(btn) {
									if (btn === 'yes') {
										checklistGrid.setLoading('Deleting...');
										Ext.Ajax.request({
											url: 'api/v1/resource/checklisttemplates/' + record.get('checklistTemplateId') + '?force=true',
											method: 'DELETE',
											callback: function(){
												checklistGrid.setLoading(false);
											},
											success: function(response, opts){
												actionRefresh();
											}
										});	
									} 
								}
							});									
						}
					}
				});				
			};			
					
		});
		
	</script>

    </stripes:layout-component>
</stripes:layout-render>
