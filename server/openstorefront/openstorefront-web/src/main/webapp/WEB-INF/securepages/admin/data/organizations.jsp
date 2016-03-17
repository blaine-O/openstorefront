<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<stripes:layout-render name="../../../../client/layout/adminlayout.jsp">
	<stripes:layout-component name="contents">
		
		<script type="text/javascript">
			/* global Ext, CoreUtil */
			Ext.onReady(function(){	
				
				var orgGrid = Ext.create('Ext.grid.Panel', {
					id: 'orgGrid',
					title: 'Manage Organizations <i class="fa fa-question-circle"  data-qtip="Organizations found within the metadata of the site." ></i>',
					store: Ext.create('Ext.data.Store', {
						autoLoad: true,
						id: 'orgGridStore',
						pageSize: 100,
						remoteSort: true,
						sorters: [
							new Ext.util.Sorter({
								property: 'name',
								direction: 'ASC'
							})
						],
						proxy: CoreUtil.pagingProxy({
							type: 'ajax',
							url: '../api/v1/resource/organizations/',
							reader: {
								type: 'json',
								rootProperty: 'data',
								totalProperty: 'totalNumber'
							}
						})
					}),
					columnLines: true,
					columns: [						
						{ text: 'Name', dataIndex: 'name', minWidth: 200, flex:1},
						{ text: 'Description', dataIndex: 'description', flex: 1, minWidth: 200 },
						{ text: 'Type', dataIndex: 'organizationTypeDescription', width: 200 },
						{ text: 'Web Site', dataIndex: 'homeUrl', width: 200, hidden:true },
						{ text: 'Address', dataIndex: 'address', width: 150, hidden:true },
						{ text: 'Agency', dataIndex: 'agency', width: 150, hidden:true },
						{ text: 'Department', dataIndex: 'department', width: 150, hidden:true },
						{ text: 'Contact Name', dataIndex: 'contactName', width: 150, hidden:true },
						{ text: 'Contact Phone', dataIndex: 'contactPhone', width: 150, hidden:true },
						{ text: 'Contact Email', dataIndex: 'contactEmail', width: 150, hidden:true }
					],
					dockedItems: [
						{
							xtype: 'toolbar',
							dock: 'top',
							items: [
								{
									text: 'Refresh',
									scale: 'medium',								
									iconCls: 'fa fa-2x fa-refresh',
									tooltip: 'Refresh Grid',
									handler: function () {
										refreshGrid();
									}
								},
								{
									text: 'Add',
									scale: 'medium',
									iconCls: 'fa fa-2x fa-plus',
									tooltip: 'Add record',
									handler: function () {
										addRecord();
									}
								}, 								
								{
									text: 'Edit',
									id: 'editButton',
									scale: 'medium',								
									iconCls: 'fa fa-2x fa-edit',
									disabled: true,
									tooltip: 'Edit selected record',
									handler: function () {
										editRecord();
									}								
								},							
								{
									text: 'Merge',
									id: 'mergeButton',
									scale: 'medium',								
									iconCls: 'fa fa-2x fa-compress',
									disabled: true,
									tooltip: 'Merge selected record into another record',
									handler: function () {
										mergeRecords();
									}								
								},
								{
									text: 'References',
									id: 'refButton',
									scale: 'medium',								
									iconCls: 'fa fa-2x fa-paperclip',
									tooltip: 'View selected record references',
									disabled: true,
									handler: function () {
										referenceRecords();
									}								
								},
								{	text: 'Delete',
									id: 'deleteButton',
									scale: 'medium',
									iconCls: 'fa fa-2x fa-trash',
									disabled: true,
									tooltip: 'Delete record',
									handler: function () {
										deleteRecord();
								    }
								},
								{
									xtype: 'tbfill'
								},
								{	text: 'Run Extraction',
									scale: 'medium',
									iconCls: 'fa fa-2x fa-play',
									tooltip: 'Start extraction of organizations from metadata',
									handler: function () {
										runExtraction();
								    }
								},
								{	text: '"No Organization" References',
									scale: 'medium',
									iconCls: 'fa fa-2x fa-file-text-o',
									tooltip: 'Entries without organizations',
									handler: function () {
										noOrg();
								    }
								}
							]
						},
						{
							xtype: 'pagingtoolbar',
							dock: 'bottom',
							store: 'orgGridStore',
							displayInfo: true
						}
					],
					listeners: {
						itemdblclick: function(grid, record, item, index, e, opts){
							//console.log("double click");
							editRecord();
						},
						selectionchange: function(grid, record, index, opts){
							//console.log("change selection");
							checkButtonChanges();
						}
					}
				});
				
				Ext.create('Ext.container.Viewport', {
					layout: 'fit',
					items: [
						orgGrid
					]
				});
		
				var selectedObj=null;
				
				var checkButtonChanges = function() {
					var cnt = Ext.getCmp('orgGrid').getSelectionModel().getCount();
					if ( cnt === 1) {
						Ext.getCmp('editButton').setDisabled(false);
						Ext.getCmp('mergeButton').setDisabled(false);
						Ext.getCmp('refButton').setDisabled(false);
						Ext.getCmp('deleteButton').setDisabled(false);
					} else {
						Ext.getCmp('editButton').setDisabled(true);
						Ext.getCmp('mergeButton').setDisabled(true);
						Ext.getCmp('refButton').setDisabled(true);
						Ext.getCmp('deleteButton').setDisabled(true);						
					}
				};
				
				var refreshGrid = function() {
					Ext.getCmp('orgGrid').getStore().load();				
				};
				
				var addRecord = function() {
					addEditWin.show();
					addEditWin.setTitle("Add Organization");
//					//reset form
					Ext.getCmp('entryForm').reset(true);
					Ext.getCmp('entryForm').edit = false;
					
				};
				
				var editRecord = function() {
					
					addEditWin.show();
					addEditWin.setTitle("Edit Organization");
					selectedObj = Ext.getCmp('orgGrid').getSelection()[0];
					Ext.getCmp('entryForm').reset(true);
					Ext.getCmp('entryForm').edit = true;
					//load form
					Ext.getCmp('entryForm').loadRecord(selectedObj);
				    
				};
				
				var mergeRecords = function() {
				    selectedObj = Ext.getCmp('orgGrid').getSelection()[0].data;
					
					//console.log("Org ID", selectedObj);
					Ext.getCmp('mergeForm').reset(true);
					
				    Ext.getCmp('targetId').setValue(selectedObj.organizationId);
					Ext.getCmp('targetOrganization').setValue(selectedObj.name);
				 
					Ext.getCmp('mergeId').setStore(Ext.getCmp('orgGrid').getStore());
					
		            mergeWin.show();
					
				};
				
				var referenceRecords = function(){
				     selectedObj = Ext.getCmp('orgGrid').getSelection()[0].data;
					 //console.log("selObj",selectedObj);
					 
					 Ext.getCmp('refWin').show();
				};
				
				var deleteRecord = function(){
					selectedObj = Ext.getCmp('orgGrid').getSelection()[0].data;
					Ext.Ajax.request({
						url: '../api/v1/resource/organizations/'+encodeURIComponent(selectedObj.organizationId)+'/references',
						method: 'GET',
						success: function (response, opts) {
							
							var theData=[];
							theData = JSON.parse(response.responseText);
							//console.log("response:",theData.length);
							if(0 < theData.length){
								Ext.toast('That organization has references and cannot be deleted.');
								return;
							}
							else{
								Ext.Msg.show({
									title: 'Delete Organization?',
									message: 'Are you sure you want to delete the selected organization?',
									buttons: Ext.Msg.YESNO,
									icon: Ext.Msg.QUESTION,
									fn: function (btn) {
										if (btn === 'yes') {
											Ext.getCmp('orgGrid').setLoading(true);
											Ext.Ajax.request({
												url: '../api/v1/resource/organizations/'+encodeURIComponent(selectedObj.organizationId),
												method: 'DELETE',
												success: function (response, opts) {
													Ext.getCmp('orgGrid').setLoading(false);
													refreshGrid();
												},
												failure: function (response, opts) {
													Ext.getCmp('orgGrid').setLoading(false);
												}
											});
										}
									}
								});
							}
						},
						failure: function (response, opts) {
							Ext.toast('Failed to check for organization references.');
						}
					});
					
					
					
				};
				
				var noOrg = function(){
                    Ext.getCmp('noOrgWin').show();
					
				};
				
				var runExtraction = function(){
				    Ext.toast('Performing organization meta-data extraction ...');
					Ext.Ajax.request({
						url: '../api/v1/resource/organizations/extract',
						method: 'POST',
						success: function (response, opts) {
							Ext.toast('Organization meta-data extraction complete');
							refreshGrid();
						},
						failure: function (response, opts) {
							Ext.toast('Organization meta-data extraction failed');
						}
					});	
				};
				
				//
				//
				//  NO ORG WINDOW
				//
				//
				var noOrgWin = Ext.create('Ext.window.Window', {
					id: 'noOrgWin',
					title: 'No Organization References',
					modal: true,
					width: '40%',
					height: '50%',
					listeners:{
								show: function(){
									console.log("Loading No Org");
									Ext.getCmp('noOrgGrid').getStore().load();
								}
							},
					items:[
						{
							xtype:'grid',
							id: 'noOrgGrid',
							title: '',
							store: Ext.create('Ext.data.Store', {
								autoLoad: false,
								id: 'noOrgGridStore',
								pageSize: 100,
								remoteSort: true,
								sorters: [
									new Ext.util.Sorter({
										property: 'componentName',
										direction: 'ASC'
									})
								],
								fields: [
									{name: 'referenceType', mapping: function (data) {
											
											var retStr ='';
											if(typeof data.componentName !== 'undefined'){
												retStr=data.referenceType+'<br/><div style="font-size:.7em;">Entry: '+data.componentName+'</div>';
											}
											else{
												retStr=data.referenceType;
											}
											return retStr;
										}},
									{name: 'referenceName', mapping: function (data) {
											
											var retStr ='';
											if(data.referenceName.trim() !== ''){
												retStr=data.referenceName;
											}
											else if(typeof data.referenceId !== 'undefined'){
												retStr=data.referenceId;
											}
											else{
												retStr='No Reference Name';
											}
											return retStr;
										}}
											
								],
								proxy: CoreUtil.pagingProxy({
									type: 'ajax',
									url: '../api/v1/resource/organizations/references',
									reader: {
										type: 'json',
										rootProperty: 'data',
										totalProperty: 'totalNumber'
									}
								})
							}),
							columnLines: true,
							columns: [						
								{ text: 'Reference Name', dataIndex: 'referenceName', flex: 1, minWidth: 200 },
								{ text: 'Reference Type', dataIndex: 'referenceType', flex: 1, minWidth: 200 }
							]
							
						}
					],
					dockedItems: [
					{
						
							xtype: 'pagingtoolbar',
							dock: 'bottom',
							store: 'noOrgGridStore',
							displayInfo: true
					}]

					});
				
				//
				//
				//  REF WINDOW
				//
				//
				var refWin = Ext.create('Ext.window.Window', {
					id: 'refWin',
					title: 'Organization References',
					modal: true,
					width: '50%',
					height: '50%',
					layout:'fit',
					maximizable: true,
					scrollable:true,
					listeners:{
						show: function () {
							var refurl = '../api/v1/resource/organizations/'+encodeURIComponent(selectedObj.organizationId)+ '/references';
							//console.log("url",refurl);
							var store = Ext.data.StoreManager.lookup('refGridStore');
							store.setProxy({
									type: 'ajax',
									url: refurl,
									method: 'GET'
								});
							
							store.load();
						}
					},
					items:[
						{
							xtype:'grid',
							id: 'refGrid',
							width:'100%',
							title: '',
							store: Ext.create('Ext.data.Store', {
								autoLoad: false,
								id: 'refGridStore',
								pageSize: 100,
								remoteSort: true,
								sorters: [
									new Ext.util.Sorter({
										property: 'name',
										direction: 'ASC'
									})
								],
								fields: [
									{name: 'referenceType', mapping: function (data) {
											//console.log("Data",data);
											var retStr ='';
											if(typeof data.componentName !== 'undefined'){
												retStr=data.referenceType+'<br/><div style="font-size:.7em;">Entry: '+data.componentName+'</div>';
											}
											else{
												retStr=data.referenceType;
											}
											return retStr;
										}} 
											
								]
								
							}),
							columnLines: true,
							columns: [						
								{ text: 'Reference Name', dataIndex: 'referenceName', flex: 1, minWidth: 200 },
								{ text: 'Reference Type', dataIndex: 'referenceType', flex: 1, minWidth: 200 }
							]
						}
					]
					});	
				//
				//
				//  Merge Window
				//
				//
				var mergeWin = Ext.create('Ext.window.Window', {
					id: 'mergeWin',
					title: 'Merge Organizations',
					modal: true,
					width: '50%',
					height: 215,
					y: 40,
					layout: 'fit',
					resizable: false,
					items: [
						{	xtype: 'form',
							id: 'mergeForm',
							scrollable: true,
							layout:'fit',
							items: [
								{
									xtype: 'panel',
									style: 'padding: 10px;',
									layout: 'vbox',
									defaults: {
										labelAlign: 'top'
									},
									items:[
										{
											xtype: 'textfield',
											id: 'targetId',
											name: 'targetId',
											style: 'padding-top: 5px;',
											width: '100%',
											hidden: true
										},
										{
											xtype: 'combobox',
											name: 'mergeId',
											id: 'mergeId',
											fieldLabel: 'Merge references from',
											width: '100%',
											maxLength: 50,
											displayField: 'name',
											valueField: 'organizationId',
											editable: false,
											allowBlank: false
										},
										{
											xtype: 'textfield',
											id: 'targetOrganization',
											fieldLabel: 'Into this target organization',
											name: 'targetOrganization',
											style: 'padding-top: 5px;',
											width: '100%',
											readOnly:true
										}
									]
								}
							],
							 dockedItems: [
							{
								xtype: 'toolbar',
								dock: 'bottom',
								items: [
									{
										text: 'Save',
										iconCls: 'fa fa-save',
										formBind: true,
										handler: function(){
											var data = Ext.getCmp('mergeForm').getValues();
											if(data.mergeId === ''){
												Ext.toast('You must enter an merge organization that merges into the target.', '', 'tr');
												return;
											}
											else if(data.mergeId === data.targetId){
												Ext.toast('You cannot merge the same organizations together.', '', 'tr');
												return;
											}

											var url = '../api/v1/resource/organizations/'+data.targetId+'/merge/'+data.mergeId;     
											Ext.getCmp('mergeForm').setLoading(true);

											CoreUtil.submitForm({
												url: url,
												method: 'POST',
												removeBlankDataItems: true,
												form: Ext.getCmp('mergeForm'),
												success: function(response, opts) {
													Ext.toast('Merged Successfully', '', 'tr');
													Ext.getCmp('mergeForm').setLoading(false);
													Ext.getCmp('mergeWin').hide();													
													refreshGrid();												
												}
											});												
										}
									},
									{
										xtype: 'tbfill'
									},
									{
										text: 'Cancel',
										iconCls: 'fa fa-close',
										handler: function(){
											Ext.getCmp('mergeWin').close();
										}											
									}
								]
							}
						]
					}
					]				   
				});
				
				//
				//
				//  ADD EDIT ORG WINDOW
				//
				//
				var orgTypeStore = Ext.create('Ext.data.Store', {
					id:'orgTypeStore',
					autoLoad: true,
					sorters: [
						new Ext.util.Sorter({
							property: 'description',
							direction: 'DESC'
						})
					],
					proxy: CoreUtil.pagingProxy({
						url: '../api/v1/resource/lookuptypes/OrganizationType',
						reader: {
							type: 'json',							
						}
					})
				});
				
				var addEditWin = Ext.create('Ext.window.Window', {
					id: 'addEditWin',
					title: 'Organization',
					modal: true,
					width: '70%',					
					resizable: false,
					layout: 'fit',
					items: [
						{
							xtype: 'form',
							id: 'entryForm',
							scrollable: true,
							layout:'column',
							defaults: {
								labelAlign: 'top'
							},
							items: [
								{
									xtype: 'panel',
									columnWidth: 0.5,
									title: 'Organization Information',
									defaults: {
										labelAlign: 'top'
									},
									style: 'padding: 10px;',
									items: [
										{
											xtype: 'textfield',
											id: 'name',
											fieldLabel: 'Name<span class="field-required" />',
											name: 'name',
											style: 'padding-top: 5px;',
											width: '100%',
											allowBlank: false,
											maxLength: 30		
										},
										{
											xtype: 'textarea',
											id: 'description',
											fieldLabel: 'Description',
											name: 'description',
											style: 'padding-top: 5px;',
											width: '100%',
											maxLength: 16384		
										},
										{
											xtype: 'textfield',
											id: 'homeUrl',
											fieldLabel: 'Organization URL',
											name: 'homeUrl',
											style: 'padding-top: 5px;',
											width: '100%',
											maxLength: 150		
										},
										{
											xtype: 'combobox',
											name: 'organizationType',
											id: 'organizationType',
											fieldLabel: 'Organization Type',
											width: '100%',
											maxLength: 50,
											store: orgTypeStore,
											displayField: 'description',
											valueField: 'code',
											editable: false,
											allowBlank: true		
										}
										
									]
								},
								{
									xtype: 'panel',
									columnWidth: 0.5,
									title: 'Main Contact Information',
									defaults: {
										labelAlign: 'top'
									},
									style: 'padding: 10px;',
									items: [
										{
											xtype: 'textfield',
											id: 'contactName',
											fieldLabel: 'Contact Name',
											name: 'contactName',
											style: 'padding-top: 5px;',
											width: '100%',
											maxLength: 30	
										},
										{
											xtype: 'textfield',
											id: 'contactPhone',
											fieldLabel: 'Phone',
											name: 'contactPhone',
											style: 'padding-top: 5px;',
											width: '100%',
											maxLength: 20	
										},
										{
											xtype: 'textfield',
											id: 'contactEmail',
											fieldLabel: 'Email',
											name: 'contactEmail',
											style: 'padding-top: 5px;',
											width: '100%',
											maxLength: 120	
										},
										{
											xtype: 'textfield',
											id: 'agency',
											fieldLabel: 'Agency',
											name: 'agency',
											style: 'padding-top: 5px;',
											width: '100%',
											maxLength: 30	
										},
										{
											xtype: 'textfield',
											id: 'department',
											fieldLabel: 'Department',
											name: 'department',
											style: 'padding-top: 5px;',
											width: '100%',
											maxLength: 30	
										},
										{
											xtype: 'textfield',
											id: 'address',
											fieldLabel: 'Address',
											name: 'address',
											style: 'padding-top: 5px;',
											width: '100%',
											maxLength: 50	
										}
									]
								}
							],
							dockedItems: [
								{
									xtype: 'toolbar',
									dock: 'bottom',
									items: [
										{
											text: 'Save',
											iconCls: 'fa fa-save',
											formBind: true,
											handler: function(){
												
												var method = Ext.getCmp('entryForm').edit ? 'PUT' : 'POST'; 												
												var data = Ext.getCmp('entryForm').getValues();
												var url = Ext.getCmp('entryForm').edit ? '../api/v1/resource/organizations/' + selectedObj.data.organizationId : '../api/v1/resource/organizations';       
                                                //console.log("Made It ",selectedObj);
												CoreUtil.submitForm({
													url: url,
													method: method,
													data: data,
													removeBlankDataItems: true,
													form: Ext.getCmp('entryForm'),
													success: function(response, opts) {
														Ext.toast('Saved Successfully', '', 'tr');
														Ext.getCmp('entryForm').setLoading(false);
														Ext.getCmp('addEditWin').hide();													
														refreshGrid();												
													}
												});												
											}
										},
										{
											xtype: 'tbfill'
										},
										{
											text: 'Cancel',
											iconCls: 'fa fa-close',
											handler: function(){
												Ext.getCmp('addEditWin').close();
											}											
										}
									]
								}
							]
						}
					]
				});
				
			});
		</script>	
		
	</stripes:layout-component>
</stripes:layout-render>