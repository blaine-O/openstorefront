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
/* global Ext, CoreService */

Ext.define('OSF.component.AdvanceSearchPanel', {
	extend: 'Ext.panel.Panel',
	alias: 'osf.widget.AdvanceSearchPanel',
	layout: 'fit',
	
	initComponent: function () {
		this.callParent();

		var advancePanel = this;
		
		var searchTypes = [
			{
				searchType: 'COMPONENT',
				label: 'Entry',
				options: Ext.create('Ext.panel.Panel', {					
					defaults: {
						labelAlign: 'top',
						labelSeparator: ''
					},	
					items: [
						{
							xtype: 'combobox', 
							itemId: 'field',
							width: '100%',
							name: 'field',
							fieldLabel: 'Field <span class="field-required" />',
							allowBlank: false,
							editable: false,
							displayField: 'label',
							valueField: 'code',					
							store: {
								data: [
									{code: 'name', label: 'Name'},
									{code: 'description', label: 'Description'},
									{code: 'componentType', label: 'Entry Type/Topic'},
									{code: 'organization', label: 'Organization'},
									{code: 'version', label: 'Version'},
									{code: 'releaseDate', label: 'Release Date'},
									{code: 'approvalDts', label: 'Approval Date'},
									{code: 'lastActivityDts', label: 'Last Activity Date'},
									{code: 'dataSource', label: 'Data Source'}
								]
							},
							listeners: {
								change: function(cb, newValue, oldValue, opt) {
									var optionPanel = cb.up('panel');
									
									optionPanel.getComponent('componentType').setHidden(true);
									optionPanel.getComponent('componentType').setDisabled(true);	
									optionPanel.getComponent('dataSource').setHidden(true);
									optionPanel.getComponent('dataSource').setDisabled(true);										
									optionPanel.getComponent('startDate').setHidden(true);
									optionPanel.getComponent('startDate').setDisabled(true);										
									optionPanel.getComponent('endDate').setHidden(true);
									optionPanel.getComponent('endDate').setDisabled(true);	
									
									optionPanel.getComponent('value').setHidden(false);
									optionPanel.getComponent('value').setDisabled(false);
									optionPanel.getComponent('stringOperation').setHidden(false);
									optionPanel.getComponent('stringOperation').setDisabled(false);	
									optionPanel.getComponent('caseInsensitive').setHidden(false);
									optionPanel.getComponent('caseInsensitive').setDisabled(false);										
									
									if (newValue === 'componentType') {
										optionPanel.getComponent('componentType').setHidden(false);
										optionPanel.getComponent('componentType').setDisabled(false);									
									} 
									if (newValue === 'dataSource') {
										optionPanel.getComponent('dataSource').setHidden(false);
										optionPanel.getComponent('dataSource').setDisabled(false);									
									} 									
									if (newValue === 'releaseDate' || 
										newValue === 'approvalDts' ||
										newValue === 'lastActivityDts') {
										optionPanel.getComponent('startDate').setHidden(false);
										optionPanel.getComponent('startDate').setDisabled(false);
										optionPanel.getComponent('endDate').setHidden(false);
										optionPanel.getComponent('endDate').setDisabled(false);										
									}
																		
									if (newValue === 'componentType' ||
										newValue === 'dataSource' ||
										newValue === 'releaseDate' ||
										newValue === 'approvalDts' ||
										newValue === 'lastActivityDts') 
									{
										optionPanel.getComponent('value').setHidden(true);
										optionPanel.getComponent('value').setDisabled(true);
										optionPanel.getComponent('stringOperation').setHidden(true);
										optionPanel.getComponent('stringOperation').setDisabled(true);
										optionPanel.getComponent('caseInsensitive').setHidden(true);
										optionPanel.getComponent('caseInsensitive').setDisabled(true);	
									}
								}
							}
						},
						{
							xtype: 'textfield',
							itemId: 'value',
							name: 'value',
							width: '100%',
							fieldLabel: 'Value  <span class="field-required" />',
							allowBlank: false,
							maxLength: 1024
						},	
						{
							xtype: 'combobox', 
							itemId: 'componentType',
							width: '100%',
							name: 'value',
							fieldLabel: 'Value <span class="field-required" />',
							allowBlank: false,
							editable: false,
							disabled: true,
							hidden: true,
							displayField: 'description',
							valueField: 'code',					
							queryMode: 'remote',
							store: {
								proxy: {
									type: 'ajax',
									url: '../api/v1/resource/componenttypes/lookup'									
								}
							}							
						},
						{
							xtype: 'combobox', 
							itemId: 'dataSource',
							width: '100%',
							name: 'value',
							fieldLabel: 'Value <span class="field-required" />',
							allowBlank: false,
							editable: false,
							disabled: true,
							hidden: true,
							displayField: 'description',
							valueField: 'code',					
							queryMode: 'remote',
							store: {
								proxy: {
									type: 'ajax',
									url: '../api/v1/resource/lookuptypes/DataSource'									
								}
							}							
						},
						{
							xtype: 'datefield',
							itemId: 'startDate',
							name: 'startDate',
							allowBlank: false,
							disabled: true,
							hidden: true,								
							width: '100%',
							fieldLabel: 'Start Date <span class="field-required" />'					
						},						
						{
							xtype: 'datefield',
							itemId: 'endDate',
							name: 'endDate',
							allowBlank: false,
							width: '100%',
							disabled: true,
							hidden: true,							
							fieldLabel: 'End Date <span class="field-required" />'					
						},	
						{
							xtype: 'checkbox',
							itemId: 'caseInsensitive',
							name: 'caseInsensitive',
							boxLabel: 'Case Insensitive'
						},						
						{								
							xtype: 'combobox',
							itemId: 'stringOperation',
							width: '100%',
							name: 'stringOperation',
							fieldLabel: 'String Operation',
							queryMode: 'local',
							displayField: 'description',
							valueField: 'code',
							value: 'EQUALS',					
							editable: false,
							store: {
								data: [
									{
										code: 'EQUALS',
										description: 'Equals'
									},
									{
										code: 'STARTS_LIKE',
										description: 'Starts Like'
									},
									{
										code: 'ENDS_LIKE',
										description: 'Ends Like'
									},
									{
										code: 'CONTAINS',
										description: 'Contains'
									}							
								]
							}					
						}						
					]					
				})
			},
			{
				searchType: 'ATTRIBUTE',
				label: 'Attribute',
				options: Ext.create('Ext.panel.Panel', {
					disabled: true,
					defaults: {
						labelAlign: 'top',
						labelSeparator: ''
					},	
					items: [
						{
							xtype: 'combobox', 
							itemId: 'keyField',
							width: '100%',
							name: 'keyField',
							fieldLabel: 'Attribute Category <span class="field-required" />',
							editable: false,
							allowBlank: false,
							displayField: 'description',
							valueField: 'attributeType',
							queryMode: 'remote',
							store: {
								proxy: {
									type: 'ajax',
									url: '../api/v1/resource/attributes/attributetypes',
									reader: {
										type: 'json',
										rootProperty: 'data',
										totalProperty: 'totalNumber'
									}
								}
							},
							listeners: {
								change: function(cb, newValue, oldValue, opts){
									var codeCb = cb.up('panel').getComponent('keyValue');
									codeCb.getStore().load({
										url: '../api/v1/resource/attributes/attributetypes/' + newValue + '/attributecodes'
									});
								}
							} 
						},
						{
							xtype: 'combobox', 
							itemId: 'keyValue',
							width: '100%',
							name: 'keyValue',
							fieldLabel: 'Specific Category <span class="field-required" />',
							allowBlank: false,
							editable: false,
							displayField: 'label',
							valueField: 'code',	
							queryMode: 'local',
							store: {
								fields: [
									{
										name: 'code',
										mapping: function(data) {
											return data.attributeCodePk.attributeCode;
										}
									}
								],
								autoLoad: false,
								proxy: {
									type: 'ajax',
									url: '../api/v1/resource/attributes/attributetypes',
									reader: {
										type: 'json',
										rootProperty: 'data',
										totalProperty: 'totalNumber'
									}
								}
							}
						}					
					]
				})
			},
			{
				searchType: 'ARCHITECTURE',
				label: 'Architecture',
				options: Ext.create('Ext.panel.Panel', {
					disabled: true,
					defaults: {
						labelAlign: 'top',
						labelSeparator: ''
					},	
					items: [
					{
							xtype: 'combobox', 
							itemId: 'keyField',
							width: '100%',
							name: 'keyField',
							fieldLabel: 'Architecture <span class="field-required" />',
							editable: false,
							allowBlank: false,
							displayField: 'description',
							valueField: 'attributeType',
							queryMode: 'remote',
							store: {
								proxy: {
									type: 'ajax',
									url: '../api/v1/resource/attributes/attributetypes',
									reader: {
										type: 'json',
										rootProperty: 'data',
										totalProperty: 'totalNumber'
									}
								},
								listeners: {
									load: function(store, records, success, opts) {
										store.filterBy(function(record){
											return record.get('architectureFlg');
										});
									}
								}
							},
							listeners: {
								change: function(cb, newValue, oldValue, opts){
									var codeCb = cb.up('panel').getComponent('keyValue');
									codeCb.getStore().load({
										url: '../api/v1/resource/attributes/attributetypes/' + newValue + '/attributecodes'
									});
								}
							} 
						},
						{
							xtype: 'combobox', 
							itemId: 'keyValue',
							width: '100%',
							name: 'keyValue',
							fieldLabel: 'Category <span class="field-required" />',
							allowBlank: false,
							editable: false,
							displayField: 'label',
							valueField: 'code',	
							queryMode: 'local',
							store: {
								fields: [
									{
										name: 'code',
										mapping: function(data) {
											return data.attributeCodePk.attributeCode;
										}
									}
								],
								autoLoad: false,
								proxy: {
									type: 'ajax',
									url: '../api/v1/resource/attributes/attributetypes',
									reader: {
										type: 'json',
										rootProperty: 'data',
										totalProperty: 'totalNumber'
									}
								}
							}
						}						
					]					
				})
			},
			{
				searchType: 'INDEX',
				label: 'Index',
				options: Ext.create('Ext.panel.Panel', {
					disabled: true,
					defaults: {
						labelAlign: 'top',
						labelSeparator: ''
					},										
					items: [
						{
							xtype: 'textfield',
							itemId: 'value',
							name: 'value',
							width: '100%',
							fieldLabel: 'Value',							
							maxLength: 1024
						}						
					]
				})
			},
			{
				searchType: 'TAG',
				label: 'Tag',
				options: Ext.create('Ext.panel.Panel', {
					disabled: true,
					defaults: {
						labelAlign: 'top',
						labelSeparator: ''
					},					
					items: [
						{
							xtype: 'textfield',
							itemId: 'value',
							name: 'value',
							width: '100%',
							fieldLabel: 'Value <span class="field-required" />',
							allowBlank: false,
							maxLength: 1024
						},
						{
							xtype: 'checkbox',
							name: 'caseInsensitive',
							boxLabel: 'Case Insensitive'
						},
						{								
							xtype: 'combobox',
							itemId: 'stringOperation',
							width: '100%',
							name: 'stringOperation',
							fieldLabel: 'String Operation',
							queryMode: 'local',
							displayField: 'description',
							valueField: 'code',
							value: 'EQUALS',					
							editable: false,
							store: {
								data: [
									{
										code: 'EQUALS',
										description: 'Equals'
									},
									{
										code: 'STARTS_LIKE',
										description: 'Starts Like'
									},
									{
										code: 'ENDS_LIKE',
										description: 'Ends Like'
									},
									{
										code: 'CONTAINS',
										description: 'Contains'
									}							
								]
							}					
						}						
					]					
				})
			},
			{
				searchType: 'METADATA',
				label: 'Meta Data',
				options: Ext.create('Ext.panel.Panel', {
					disabled: true,
					defaults: {
						labelAlign: 'top',
						labelSeparator: ''
					},	
					items: [
						{
							xtype: 'textfield',
							itemId: 'keyField',
							name: 'keyField',
							width: '100%',
							fieldLabel: 'Key Field <span class="field-required" />',
							allowBlank: false,
							maxLength: 1024
						},						
						{
							xtype: 'textfield',
							itemId: 'keyValue',
							name: 'keyValue',
							width: '100%',
							fieldLabel: 'Key Value',
							maxLength: 1024
						},
						{
							xtype: 'checkbox',
							name: 'caseInsensitive',
							boxLabel: 'Case Insensitive'
						},
						{								
							xtype: 'combobox',
							itemId: 'stringOperation',
							width: '100%',
							name: 'stringOperation',
							fieldLabel: 'String Operation',
							queryMode: 'local',
							displayField: 'description',
							valueField: 'code',
							value: 'EQUALS',					
							editable: false,
							store: {
								data: [
									{
										code: 'EQUALS',
										description: 'Equals'
									},
									{
										code: 'STARTS_LIKE',
										description: 'Starts Like'
									},
									{
										code: 'ENDS_LIKE',
										description: 'Ends Like'
									},
									{
										code: 'CONTAINS',
										description: 'Contains'
									}							
								]
							}					
						}						
					]
				})
			},
			{
				searchType: 'USER_RATING',
				label: 'User Rating',
				options: Ext.create('Ext.panel.Panel', {
					disabled: true,
					defaults: {
						labelAlign: 'top',
						labelSeparator: ''
					},	
					items: [
						{
							xtype: 'numberfield',
							itemId: 'value',
							name: 'value',
							width: '100%',
							fieldLabel: 'Rating (0-5) <span class="field-required" />',
							allowBlank: false,
							maxValue: 5,
							minValue: 0
						},
						{								
							xtype: 'combobox',
							itemId: 'numberOperation',
							width: '100%',
							name: 'numberOperation',
							fieldLabel: 'Number Operation',
							queryMode: 'local',
							displayField: 'description',
							valueField: 'code',
							value: 'EQUALS',					
							editable: false,
							store: {
								data: [
									{
										code: 'EQUALS',
										description: '='
									},
									{
										code: 'GREATERTHAN',
										description: '>'
									},
									{
										code: 'GREATERTHANEQUALS',
										description: '>='
									},
									{
										code: 'LESSTHAN',
										description: '<'
									},
									{
										code: 'LESSTHANEQUALS',
										description: '<='
									}							
								]
							}					
						}						
					]
				})
			},
			{
				searchType: 'CONTACT',
				label: 'Contact',
				options: Ext.create('Ext.panel.Panel', {
					disabled: true,
					defaults: {
						labelAlign: 'top',
						labelSeparator: ''
					},	
					items: [
						{
							xtype: 'combobox', 
							itemId: 'field',
							width: '100%',
							name: 'field',
							fieldLabel: 'Field <span class="field-required" />',
							allowBlank: false,
							editable: false,
							displayField: 'label',
							valueField: 'code',					
							store: {
								data: [
									{code: 'contactType', label: 'Contact Type'},
									{code: 'firstName', label: 'First Name'},
									{code: 'lastName', label: 'Last Name'},
									{code: 'email', label: 'Email'},
									{code: 'phone', label: 'Phone'},
									{code: 'organization', label: 'Organization'}
								]
							},
							listeners: {
								change: function(cb, newValue, oldValue, opt) {
									var optionPanel = cb.up('panel');
									
									optionPanel.getComponent('contactType').setHidden(true);
									optionPanel.getComponent('contactType').setDisabled(true);									
									optionPanel.getComponent('value').setHidden(false);
									optionPanel.getComponent('value').setDisabled(false);
									optionPanel.getComponent('stringOperation').setHidden(false);
									optionPanel.getComponent('stringOperation').setDisabled(false);
									optionPanel.getComponent('caseInsensitive').setHidden(false);
									optionPanel.getComponent('caseInsensitive').setDisabled(false);
									
									if (newValue === 'contactType') {
										optionPanel.getComponent('contactType').setHidden(false);
										optionPanel.getComponent('contactType').setDisabled(false);
									
										optionPanel.getComponent('value').setHidden(true);
										optionPanel.getComponent('value').setDisabled(true);
										optionPanel.getComponent('stringOperation').setHidden(true);
										optionPanel.getComponent('stringOperation').setDisabled(true);
										optionPanel.getComponent('caseInsensitive').setHidden(true);
										optionPanel.getComponent('caseInsensitive').setDisabled(true);										
									} 
								}
							}
						},
						{
							xtype: 'textfield',
							itemId: 'value',
							name: 'value',
							width: '100%',
							fieldLabel: 'Value  <span class="field-required" />',
							allowBlank: false,
							maxLength: 1024
						},						
						{
							xtype: 'combobox', 
							itemId: 'contactType',
							width: '100%',
							name: 'value',
							fieldLabel: 'Value <span class="field-required" />',
							allowBlank: false,
							editable: false,
							disabled: true,
							hidden: true,
							displayField: 'description',
							valueField: 'code',					
							queryMode: 'remote',
							store: {
								proxy: {
									type: 'ajax',
									url: '../api/v1/resource/lookuptypes/ContactType'									
								}
							}							
						},
						{
							xtype: 'checkbox',
							itemId: 'caseInsensitive',
							name: 'caseInsensitive',
							boxLabel: 'Case Insensitive'
						},						
						{								
							xtype: 'combobox',
							itemId: 'stringOperation',
							width: '100%',
							name: 'stringOperation',
							fieldLabel: 'String Operation',
							queryMode: 'local',
							displayField: 'description',
							valueField: 'code',
							value: 'EQUALS',					
							editable: false,
							store: {
								data: [
									{
										code: 'EQUALS',
										description: 'Equals'
									},
									{
										code: 'STARTS_LIKE',
										description: 'Starts Like'
									},
									{
										code: 'ENDS_LIKE',
										description: 'Ends Like'
									},
									{
										code: 'CONTAINS',
										description: 'Contains'
									}							
								]
							}					
						}						
					]
				})
			},
			{
				searchType: 'REVIEW',
				label: 'User Review',
				options: Ext.create('Ext.panel.Panel', {
					disabled: true,
					defaults: {
						labelAlign: 'top',
						labelSeparator: ''
					},	
					items: [
						{
							xtype: 'combobox', 
							itemId: 'field',
							width: '100%',
							name: 'field',
							fieldLabel: 'Field <span class="field-required" />',
							allowBlank: false,
							editable: false,
							displayField: 'label',
							valueField: 'code',					
							store: {
								data: [
									{code: 'userTypeCode', label: 'User Type'},
									{code: 'title', label: 'Title'},
									{code: 'comment', label: 'Comment'},
									{code: 'organization', label: 'Organization'},
									{code: 'userTimeCode', label: 'Experience'},
									{code: 'lastUsed', label: 'Last Used'},
									{code: 'recommend', label: 'Recommend'},
									{code: 'createDts', label: 'Post Date'},
									{code: 'createUser', label: 'User'}
								]
							},
							listeners: {
								change: function(cb, newValue, oldValue, opt) {
									var optionPanel = cb.up('panel');
									
									optionPanel.getComponent('userTypeCode').setHidden(true);
									optionPanel.getComponent('userTypeCode').setDisabled(true);	
									optionPanel.getComponent('userTimeCode').setHidden(true);
									optionPanel.getComponent('userTimeCode').setDisabled(true);										
									optionPanel.getComponent('recommend').setHidden(true);
									optionPanel.getComponent('recommend').setDisabled(true);										
									optionPanel.getComponent('startDate').setHidden(true);
									optionPanel.getComponent('startDate').setDisabled(true);										
									optionPanel.getComponent('endDate').setHidden(true);
									optionPanel.getComponent('endDate').setDisabled(true);	
									
									optionPanel.getComponent('value').setHidden(false);
									optionPanel.getComponent('value').setDisabled(false);
									optionPanel.getComponent('stringOperation').setHidden(false);
									optionPanel.getComponent('stringOperation').setDisabled(false);	
									optionPanel.getComponent('caseInsensitive').setHidden(false);
									optionPanel.getComponent('caseInsensitive').setDisabled(false);										
									
									if (newValue === 'userTypeCode') {
										optionPanel.getComponent('userTypeCode').setHidden(false);
										optionPanel.getComponent('userTypeCode').setDisabled(false);									
									} 
									if (newValue === 'userTimeCode') {
										optionPanel.getComponent('userTimeCode').setHidden(false);
										optionPanel.getComponent('userTimeCode').setDisabled(false);									
									} 									
									if (newValue === 'lastUsed' || newValue === 'createDts') {
										optionPanel.getComponent('startDate').setHidden(false);
										optionPanel.getComponent('startDate').setDisabled(false);
										optionPanel.getComponent('endDate').setHidden(false);
										optionPanel.getComponent('endDate').setDisabled(false);										
									}
									if (newValue === 'recommend') {
										optionPanel.getComponent('recommend').setHidden(false);
										optionPanel.getComponent('recommend').setDisabled(false);									
									} 
																		
									if (newValue === 'userTypeCode' ||
										newValue === 'userTimeCode' ||
										newValue === 'recommend' ||
										newValue === 'lastUsed' ||
										newValue === 'createDts') 
									{
										optionPanel.getComponent('value').setHidden(true);
										optionPanel.getComponent('value').setDisabled(true);
										optionPanel.getComponent('stringOperation').setHidden(true);
										optionPanel.getComponent('stringOperation').setDisabled(true);
										optionPanel.getComponent('caseInsensitive').setHidden(true);
										optionPanel.getComponent('caseInsensitive').setDisabled(true);	
									}
								}
							}
						},
						{
							xtype: 'textfield',
							itemId: 'value',
							name: 'value',
							width: '100%',
							fieldLabel: 'Value  <span class="field-required" />',
							allowBlank: false,
							maxLength: 1024
						},	
						{
							xtype: 'combobox', 
							itemId: 'userTypeCode',
							width: '100%',
							name: 'value',
							fieldLabel: 'Value <span class="field-required" />',
							allowBlank: false,
							editable: false,
							disabled: true,
							hidden: true,
							displayField: 'description',
							valueField: 'code',					
							queryMode: 'remote',
							store: {
								proxy: {
									type: 'ajax',
									url: '../api/v1/resource/lookuptypes/UserTypeCode'									
								}
							}							
						},
						{
							xtype: 'combobox', 
							itemId: 'userTimeCode',
							width: '100%',
							name: 'value',
							fieldLabel: 'Value <span class="field-required" />',
							allowBlank: false,
							editable: false,
							disabled: true,
							hidden: true,
							displayField: 'description',
							valueField: 'code',					
							queryMode: 'remote',
							store: {
								proxy: {
									type: 'ajax',
									url: '../api/v1/resource/lookuptypes/ExperienceTimeType'									
								}
							}							
						},
						{
							xtype: 'datefield',
							itemId: 'startDate',
							name: 'startDate',
							allowBlank: false,
							disabled: true,
							hidden: true,								
							width: '100%',
							fieldLabel: 'Start Date <span class="field-required" />'					
						},						
						{
							xtype: 'datefield',
							itemId: 'endDate',
							name: 'endDate',
							allowBlank: false,
							width: '100%',
							disabled: true,
							hidden: true,							
							fieldLabel: 'End Date <span class="field-required" />'					
						},	
						{
							xtype: 'checkbox',
							itemId: 'recommend',
							name: 'value',
							uncheckedValue: 'false',
							disabled: true,
							hidden: true,
							boxLabel: 'Recommended'
						},
						{
							xtype: 'checkbox',
							itemId: 'caseInsensitive',
							name: 'caseInsensitive',
							boxLabel: 'Case Insensitive'
						},						
						{								
							xtype: 'combobox',
							itemId: 'stringOperation',
							width: '100%',
							name: 'stringOperation',
							fieldLabel: 'String Operation',
							queryMode: 'local',
							displayField: 'description',
							valueField: 'code',
							value: 'EQUALS',					
							editable: false,
							store: {
								data: [
									{
										code: 'EQUALS',
										description: 'Equals'
									},
									{
										code: 'STARTS_LIKE',
										description: 'Starts Like'
									},
									{
										code: 'ENDS_LIKE',
										description: 'Ends Like'
									},
									{
										code: 'CONTAINS',
										description: 'Contains'
									}							
								]
							}					
						}						
					]
				})
			},
			{
				searchType: 'REVIEWPRO',
				label: 'User Review Pro',
				options: Ext.create('Ext.panel.Panel', {
					disabled: true,
					defaults: {
						labelAlign: 'top',
						labelSeparator: ''
					},	
					items: [
						{
							xtype: 'textfield',
							itemId: 'value',
							name: 'value',
							width: '100%',
							fieldLabel: 'Value <span class="field-required" />',
							allowBlank: false,
							maxLength: 1024
						},
						{
							xtype: 'checkbox',
							name: 'caseInsensitive',
							boxLabel: 'Case Insensitive'
						},
						{								
							xtype: 'combobox',
							itemId: 'stringOperation',
							width: '100%',
							name: 'stringOperation',
							fieldLabel: 'String Operation',
							queryMode: 'local',
							displayField: 'description',
							valueField: 'code',
							value: 'EQUALS',					
							editable: false,
							store: {
								data: [
									{
										code: 'EQUALS',
										description: 'Equals'
									},
									{
										code: 'STARTS_LIKE',
										description: 'Starts Like'
									},
									{
										code: 'ENDS_LIKE',
										description: 'Ends Like'
									},
									{
										code: 'CONTAINS',
										description: 'Contains'
									}							
								]
							}					
						}
					]
				})
			},
			{
				searchType: 'REVIECON',
				label: 'User Review Con',
				options: Ext.create('Ext.panel.Panel', {
					disabled: true,
					defaults: {
						labelAlign: 'top',
						labelSeparator: ''
					},	
					items: [
						{
							xtype: 'textfield',
							itemId: 'value',
							name: 'value',
							width: '100%',
							fieldLabel: 'Value <span class="field-required" />',
							allowBlank: false,
							maxLength: 1024
						},
						{
							xtype: 'checkbox',
							name: 'caseInsensitive',
							boxLabel: 'Case Insensitive'
						},
						{								
							xtype: 'combobox',
							itemId: 'stringOperation',
							width: '100%',
							name: 'stringOperation',
							fieldLabel: 'String Operation',
							queryMode: 'local',
							displayField: 'description',
							valueField: 'code',
							value: 'EQUALS',					
							editable: false,
							store: {
								data: [
									{
										code: 'EQUALS',
										description: 'Equals'
									},
									{
										code: 'STARTS_LIKE',
										description: 'Starts Like'
									},
									{
										code: 'ENDS_LIKE',
										description: 'Ends Like'
									},
									{
										code: 'CONTAINS',
										description: 'Contains'
									}							
								]
							}					
						}
					]
				})
			},
			{
				searchType: 'QUESTION',
				label: 'Question',
				options: Ext.create('Ext.panel.Panel', {
					disabled: true,
					defaults: {
						labelAlign: 'top',
						labelSeparator: ''
					},	
					items: [
						{
							xtype: 'combobox', 
							itemId: 'field',
							width: '100%',
							name: 'field',
							fieldLabel: 'Field <span class="field-required" />',
							allowBlank: false,
							editable: false,
							displayField: 'label',
							valueField: 'code',					
							store: {
								data: [
									{code: 'userTypeCode', label: 'User Type'},
									{code: 'question', label: 'Question'},
									{code: 'organization', label: 'Organization'},
									{code: 'createDts', label: 'Post Date'},
									{code: 'createUser', label: 'User'}
								]
							},
							listeners: {
								change: function(cb, newValue, oldValue, opt) {
									var optionPanel = cb.up('panel');
									
									optionPanel.getComponent('userTypeCode').setHidden(true);
									optionPanel.getComponent('userTypeCode').setDisabled(true);	
									optionPanel.getComponent('startDate').setHidden(true);
									optionPanel.getComponent('startDate').setDisabled(true);										
									optionPanel.getComponent('endDate').setHidden(true);
									optionPanel.getComponent('endDate').setDisabled(true);	
									
									optionPanel.getComponent('value').setHidden(false);
									optionPanel.getComponent('value').setDisabled(false);
									optionPanel.getComponent('stringOperation').setHidden(false);
									optionPanel.getComponent('stringOperation').setDisabled(false);	
									optionPanel.getComponent('caseInsensitive').setHidden(false);
									optionPanel.getComponent('caseInsensitive').setDisabled(false);										
									
									if (newValue === 'userTypeCode') {
										optionPanel.getComponent('userTypeCode').setHidden(false);
										optionPanel.getComponent('userTypeCode').setDisabled(false);									
									} 								
									if ( newValue === 'createDts') {
										optionPanel.getComponent('startDate').setHidden(false);
										optionPanel.getComponent('startDate').setDisabled(false);
										optionPanel.getComponent('endDate').setHidden(false);
										optionPanel.getComponent('endDate').setDisabled(false);										
									}									
																		
									if (newValue === 'userTypeCode' ||									
										newValue === 'createDts') 
									{
										optionPanel.getComponent('value').setHidden(true);
										optionPanel.getComponent('value').setDisabled(true);
										optionPanel.getComponent('stringOperation').setHidden(true);
										optionPanel.getComponent('stringOperation').setDisabled(true);
										optionPanel.getComponent('caseInsensitive').setHidden(true);
										optionPanel.getComponent('caseInsensitive').setDisabled(true);	
									}
								}
							}
						},
						{
							xtype: 'textfield',
							itemId: 'value',
							name: 'value',
							width: '100%',
							fieldLabel: 'Value  <span class="field-required" />',
							allowBlank: false,
							maxLength: 1024
						},	
						{
							xtype: 'combobox', 
							itemId: 'userTypeCode',
							width: '100%',
							name: 'value',
							fieldLabel: 'Value <span class="field-required" />',
							allowBlank: false,
							editable: false,
							disabled: true,
							hidden: true,
							displayField: 'description',
							valueField: 'code',					
							queryMode: 'remote',
							store: {
								proxy: {
									type: 'ajax',
									url: '../api/v1/resource/lookuptypes/UserTypeCode'									
								}
							}							
						},
						{
							xtype: 'datefield',
							itemId: 'startDate',
							name: 'startDate',
							allowBlank: false,
							disabled: true,
							hidden: true,								
							width: '100%',
							fieldLabel: 'Start Date <span class="field-required" />'					
						},						
						{
							xtype: 'datefield',
							itemId: 'endDate',
							name: 'endDate',
							allowBlank: false,
							width: '100%',
							disabled: true,
							hidden: true,							
							fieldLabel: 'End Date <span class="field-required" />'					
						},	
						{
							xtype: 'checkbox',
							itemId: 'caseInsensitive',
							name: 'caseInsensitive',
							boxLabel: 'Case Insensitive'
						},						
						{								
							xtype: 'combobox',
							itemId: 'stringOperation',
							width: '100%',
							name: 'stringOperation',
							fieldLabel: 'String Operation',
							queryMode: 'local',
							displayField: 'description',
							valueField: 'code',
							value: 'EQUALS',					
							editable: false,
							store: {
								data: [
									{
										code: 'EQUALS',
										description: 'Equals'
									},
									{
										code: 'STARTS_LIKE',
										description: 'Starts Like'
									},
									{
										code: 'ENDS_LIKE',
										description: 'Ends Like'
									},
									{
										code: 'CONTAINS',
										description: 'Contains'
									}							
								]
							}					
						}						
					]
				})
			},
			{
				searchType: 'QUESTION_RESPONSE',
				label: 'Question Response',
				options: Ext.create('Ext.panel.Panel', {
					disabled: true,
					defaults: {
						labelAlign: 'top',
						labelSeparator: ''
					},	
					items: [
						{
							xtype: 'combobox', 
							itemId: 'field',
							width: '100%',
							name: 'field',
							fieldLabel: 'Field <span class="field-required" />',
							allowBlank: false,
							editable: false,
							displayField: 'label',
							valueField: 'code',					
							store: {
								data: [
									{code: 'userTypeCode', label: 'User Type'},
									{code: 'response', label: 'Response'},
									{code: 'organization', label: 'Organization'},
									{code: 'createDts', label: 'Post Date'},
									{code: 'createUser', label: 'User'}
								]
							},
							listeners: {
								change: function(cb, newValue, oldValue, opt) {
									var optionPanel = cb.up('panel');
									
									optionPanel.getComponent('userTypeCode').setHidden(true);
									optionPanel.getComponent('userTypeCode').setDisabled(true);	
									optionPanel.getComponent('startDate').setHidden(true);
									optionPanel.getComponent('startDate').setDisabled(true);										
									optionPanel.getComponent('endDate').setHidden(true);
									optionPanel.getComponent('endDate').setDisabled(true);	
									
									optionPanel.getComponent('value').setHidden(false);
									optionPanel.getComponent('value').setDisabled(false);
									optionPanel.getComponent('stringOperation').setHidden(false);
									optionPanel.getComponent('stringOperation').setDisabled(false);	
									optionPanel.getComponent('caseInsensitive').setHidden(false);
									optionPanel.getComponent('caseInsensitive').setDisabled(false);										
									
									if (newValue === 'userTypeCode') {
										optionPanel.getComponent('userTypeCode').setHidden(false);
										optionPanel.getComponent('userTypeCode').setDisabled(false);									
									} 								
									if ( newValue === 'createDts') {
										optionPanel.getComponent('startDate').setHidden(false);
										optionPanel.getComponent('startDate').setDisabled(false);
										optionPanel.getComponent('endDate').setHidden(false);
										optionPanel.getComponent('endDate').setDisabled(false);										
									}									
																		
									if (newValue === 'userTypeCode' ||									
										newValue === 'createDts') 
									{
										optionPanel.getComponent('value').setHidden(true);
										optionPanel.getComponent('value').setDisabled(true);
										optionPanel.getComponent('stringOperation').setHidden(true);
										optionPanel.getComponent('stringOperation').setDisabled(true);
										optionPanel.getComponent('caseInsensitive').setHidden(true);
										optionPanel.getComponent('caseInsensitive').setDisabled(true);	
									}
								}
							}
						},
						{
							xtype: 'textfield',
							itemId: 'value',
							name: 'value',
							width: '100%',
							fieldLabel: 'Value  <span class="field-required" />',
							allowBlank: false,
							maxLength: 1024
						},	
						{
							xtype: 'combobox', 
							itemId: 'userTypeCode',
							width: '100%',
							name: 'value',
							fieldLabel: 'Value <span class="field-required" />',
							allowBlank: false,
							editable: false,
							disabled: true,
							hidden: true,
							displayField: 'description',
							valueField: 'code',					
							queryMode: 'remote',
							store: {
								proxy: {
									type: 'ajax',
									url: '../api/v1/resource/lookuptypes/UserTypeCode'									
								}
							}							
						},
						{
							xtype: 'datefield',
							itemId: 'startDate',
							name: 'startDate',
							allowBlank: false,
							disabled: true,
							hidden: true,								
							width: '100%',
							fieldLabel: 'Start Date <span class="field-required" />'					
						},						
						{
							xtype: 'datefield',
							itemId: 'endDate',
							name: 'endDate',
							allowBlank: false,
							width: '100%',
							disabled: true,
							hidden: true,							
							fieldLabel: 'End Date <span class="field-required" />'					
						},	
						{
							xtype: 'checkbox',
							itemId: 'caseInsensitive',
							name: 'caseInsensitive',
							boxLabel: 'Case Insensitive'
						},						
						{								
							xtype: 'combobox',
							itemId: 'stringOperation',
							width: '100%',
							name: 'stringOperation',
							fieldLabel: 'String Operation',
							queryMode: 'local',
							displayField: 'description',
							valueField: 'code',
							value: 'EQUALS',					
							editable: false,
							store: {
								data: [
									{
										code: 'EQUALS',
										description: 'Equals'
									},
									{
										code: 'STARTS_LIKE',
										description: 'Starts Like'
									},
									{
										code: 'ENDS_LIKE',
										description: 'Ends Like'
									},
									{
										code: 'CONTAINS',
										description: 'Contains'
									}							
								]
							}					
						}						
					]
				})
			},
			{
				searchType: 'EVALUTATION_SCORE',
				label: 'Evaluation Score',
				options: Ext.create('Ext.panel.Panel', {
					disabled: true,
					defaults: {
						labelAlign: 'top',
						labelSeparator: ''
					},	
					items: [
						{
							xtype: 'combobox', 
							itemId: 'evaluationSection',
							width: '100%',
							name: 'keyField',
							fieldLabel: 'Section <span class="field-required" />',
							allowBlank: false,	
							editable: false,
							displayField: 'description',
							valueField: 'code',					
							queryMode: 'remote',
							store: {
								proxy: {
									type: 'ajax',
									url: '../api/v1/resource/lookuptypes/EvaluationSection'									
								}
							}							
						},
						{
							xtype: 'numberfield',
							itemId: 'value',
							name: 'value',
							width: '100%',
							fieldLabel: 'Value  <span class="field-required" />',
							allowBlank: false,
							allowDecimal: true,
							maxValue: 5,
							minValue: 0,
							maxLength: 1
						},
						{								
							xtype: 'combobox',
							itemId: 'numberOperation',
							width: '100%',
							name: 'numberOperation',
							fieldLabel: 'Number Operation',
							queryMode: 'local',
							displayField: 'description',
							valueField: 'code',
							value: 'EQUALS',					
							editable: false,
							store: {
								data: [
									{
										code: 'EQUALS',
										description: '='
									},
									{
										code: 'GREATERTHAN',
										description: '>'
									},
									{
										code: 'GREATERTHANEQUALS',
										description: '>='
									},
									{
										code: 'LESSTHAN',
										description: '<'
									},
									{
										code: 'LESSTHANEQUALS',
										description: '<='
									}							
								]
							}					
						}						
					]
				})
			}			
						
		];
				
		advancePanel.entryForm = Ext.create('Ext.form.Panel', {
			layout: 'anchor',
			bodyStyle: 'padding: 10px;',
			scrollable: true,
			defaults: {
				labelAlign: 'top',
				labelSeparator: ''
			},			
			items: [
				{
					xtype: 'combobox',
					width: '100%',
					itemId: 'searchType',
					name: 'searchType',
					fieldLabel: 'Search Type',
					queryMode: 'local',
					displayField: 'label',
					valueField: 'searchType',					
					allowBlank: false,
					editable: false,
					value: 'COMPONENT',
					store: {
						data: searchTypes
					},
					listeners: {
						change: function(typeCB, newValue, oldValue, opts) {
							var optionsPanel = advancePanel.entryForm.getComponent('options');
							optionsPanel.getLayout().getActiveItem().setDisabled(true);
							optionsPanel.getLayout().setActiveItem(typeCB.getSelection().data.options);
							optionsPanel.getLayout().getActiveItem().setDisabled(false);
						}
					}
				},
				{
					xtype: 'panel',
					itemId: 'options',
					layout: 'card',
					items: [								
					]
				},
				{								
					xtype: 'combobox',
					width: '100%',
					name: 'mergeCondition',
					fieldLabel: 'Merge Condition',
					queryMode: 'local',
					displayField: 'description',
					valueField: 'code',
					value: 'OR',
					allowBlank: false,
					editable: false,
					store: {
						data: [
							{
								code: 'OR',
								description: 'OR'
							},
							{
								code: 'AND',
								description: 'AND'
							},
							{
								code: 'NOT',
								description: 'NOT'
							}							
						]
					}					
				}, 
				{
					xtype: 'panel',
					itemId: 'buttonPanel',
					layout: 'hbox',
					items: [
						{
							xtype: 'button',
							itemId: 'saveButton',
							formBind: true,
							text: 'Add',
							iconCls: 'fa fa-plus',
							handler: function() {
								var saveButton = this;
								var data = advancePanel.entryForm.getValues();						
								data.typeDescription = advancePanel.entryForm.getComponent('searchType').getSelection().data.label;
								if(data.startDate) {
									data.startDate = Ext.Date.parse(data.startDate, 'm/d/Y');
									data.startDate = Ext.Date.format(data.startDate, 'Y-m-d\\TH:i:s.u');
								}
								if(data.endDate) {
									data.endDate = Ext.Date.parse(data.endDate, 'm/d/Y');
									data.endDate = Ext.Date.add(data.endDate, Ext.Date.DAY, 1);
									data.endDate = Ext.Date.subtract(data.endDate, Ext.Date.MILLI, 1);
									data.endDate = Ext.Date.format(data.endDate, 'Y-m-d\\TH:i:s.u');
								}

								var search = {
									searchElements: [
										data
									]
								};

								CoreUtil.submitForm({
									url: '../api/v1/service/search/advance',
									method: 'POST',
									data: search,
									form: advancePanel.entryForm,
									loadingText: 'Adding Search Criteria...',
									success: function(response, opts) {
										advancePanel.entryForm.reset();
										saveButton.setText('Add');
										var grid = advancePanel.entryForm.getComponent('searchGrid');
										if (advancePanel.entryForm.updateRecord) {
											grid.getStore().remove(advancePanel.entryForm.updateRecord);								
										}
										grid.getStore().add(data);								
									},
									failure: function(response, opts) {
										var errorResponse = Ext.decode(response.responseText);
										var errorMessage = '';
										Ext.Array.each(errorResponse.errors.entry, function (item, index, entry) {
											errorMessage += '<b>' + item.key + ': </b> ' + item.value + '<br>';									
										});
										Ext.Msg.show({
											title:'Validation',
											message: errorMessage,
											buttons: Ext.Msg.OK,
											icon: Ext.Msg.ERROR,
											fn: function(btn) {
											}
										});								
									}
								});

							}			
						}
//						{
//							xtype: 'button',
//							text: 'Cancel',							
//							iconCls: 'fa fa-close',
//							margin: '0 0 0 20',
//							handler: function() {
//								advancePanel.entryForm.reset();
//								advancePanel.entryForm.updateRecord = null;
//								advancePanel.entryForm.get('buttonPanel').getComponent('saveButton').setText('Add');
//							}
//						}						
					]
				},				
				{
					xtype: 'grid',
					itemId: 'searchGrid',
					title: 'Search Criteria',
					columnLines: true,
					width: '100%',
					//height: 250,
					style: 'margin-top: 20px;',
					store: {						
					},
					columns: [
						{ text: 'Type', dataIndex: 'typeDescription', width: 200 },
						{ text: 'Criteria', dataIndex: 'value',flex: 1, minWidth: 200,
							renderer: function(value, meta, record) {
								var options = '';
								
								if (record.get('field')) {
									options += '<b>Field: </b>' + record.get('field') + '<br>';
								}
								if (record.get('value')) {
									options += '<b>Value: </b>' + record.get('value') + '<br>';
								}
								if (record.get('keyField')) {
									options += '<b>Key Field: </b>' + record.get('keyField') + '<br>';
								}
								if (record.get('keyValue')) {
									options += '<b>Key Value: </b>' + record.get('keyValue') + '<br>';
								}
								if (record.get('startDate')) {
									options += '<b>Start Date: </b>' + record.get('startDate') + '<br>';
								}
								if (record.get('endDate')) {
									options += '<b>End Date: </b>' + record.get('endDate') + '<br>';
								}
								if (record.get('endDate')) {
									options += '<b>End Date: </b>' + record.get('endDate') + '<br>';
								}								
								if (record.get('caseInsensitive')) {
									options += '<b>Case Insensitive: </b>' + record.get('caseInsensitive') + '<br>';
								}
								if (record.get('numberOperation')) {
									options += '<b>Number Operation: </b>' + record.get('numberOperation') + '<br>';
								}
								if (record.get('stringOperation')) {
									options += '<b>String Operation: </b>' + record.get('stringOperation') + '<br>';
								}								
								
								return options;
							}							
						},
						{ text: 'Operation', dataIndex: 'mergeCondition', width: 200 },
						{ 
							xtype:'actioncolumn',
							width: 50,
							items: [
//								{
//									iconCls: 'fa fa-edit action-icon',
//									tooltip: 'Edit',									
//									handler: function(grid, rowIndex, colIndex) {
//										var rec = grid.getStore().getAt(rowIndex);
//										advancePanel.entryForm.updateRecord = rec;
//										
//										//manually set
//										advancePanel.entryForm.getComponent('searchType').setValue(rec.get('searchType'));
//										
//										//The rest is tricky since it's not normalized
//										
//										//change button to update
//										advancePanel.entryForm.get('buttonPanel').getComponent('saveButton').setText('Update');										
//										
//									}									
//								},
								{
									iconCls: 'fa fa-trash action-icon',
									tooltip: 'Remove',									
									handler: function(grid, rowIndex, colIndex) {
										var rec = grid.getStore().getAt(rowIndex);
										
										Ext.Msg.show({
											title:'Remove?',
											message: 'Remove search criteria?',
											buttons: Ext.Msg.YESNO,
											icon: Ext.Msg.QUESTION,
											fn: function(btn) {
												if (btn === 'yes') {
													grid.getStore().removeAt(rowIndex);
												} 
											}
										});																				
									}
								}
							]
						}
					]
				}
			]
		});		
		
		Ext.Array.each(searchTypes, function(type) {
			advancePanel.entryForm.getComponent('options').add(type.options);
		});
		advancePanel.entryForm.getComponent('searchType').setValue('COMPONENT');
		
		advancePanel.add(advancePanel.entryForm);		
		
	},
	
	getSearch: function(){
		var advancePanel = this;
		
		//return the search object
		if (advancePanel.checkForCriteria()) {
			var store = advancePanel.entryForm.getComponent('searchGrid').getStore();
			
			var searchElements = [];
			store.each(function(record){
				searchElements.push(record.data);
			});
			
			var search = {
				sortField: 'name',
                sortDirection: 'ASC',
				searchElements: searchElements
			};
			
			return search;
		}
		return null;
	},
	
	saveSearch: function(){
		var advancePanel = this;
		//prompt for name 
		//save
		
		if (advancePanel.checkForCriteria()) {
			
		}
		
	},	
	
	previewResults: function() {
		var advancePanel = this;
		
		//run search and show results window
		if (advancePanel.checkForCriteria()) {
			var search = advancePanel.getSearch();
			
			
			var resultsStore = Ext.create('Ext.data.Store', {
				fields: ['name', 'description'],
				pageSize: 50,
				autoLoad: false,
				remoteSort: true,
				sorters: [
					new Ext.util.Sorter({
						property: 'name',
						direction: 'ASC'
					})
				],
				proxy: CoreUtil.pagingProxy({
					actionMethods: {create: 'POST', read: 'POST', update: 'POST', destroy: 'POST'},
					reader: {
						type: 'json',
						rootProperty: 'data',
						totalProperty: 'totalNumber'
					}
				})
			});
			
			var previewWin = Ext.create('Ext.window.Window', {
				title: 'Search Results',
				modal: true,
				width: '70%',
				height: '50%',
				maximizable: true,
				closeAction: 'destory',
				layout: 'fit',
				items: [
					{
						xtype: 'grid',
						columnLines: true,
						store: resultsStore,
						columns: [
							{text: 'Name',
								cellWrap: true,
								dataIndex: 'name',
								width: 150,
								autoSizeColumn: false,
								renderer: function (value) {
									return '<span class="search-tools-column-orange-text">' + value + '</span>';
								}
							},
							{text: 'Description',
								dataIndex: 'description',
								flex: 1,
								autoSizeColumn: true,
								cellWrap: true,
								renderer: function (value) {
									value = Ext.util.Format.stripTags(value);
									var str = value.substring(0, 500);
									if (str === value) {
										return str;
									} else {
										str = str.substr(0, Math.min(str.length, str.lastIndexOf(' ')));
										return str += ' ... <br/>';
									}
								}
							}
						],
						dockedItems: [{
								xtype: 'pagingtoolbar',
								store: resultsStore,
								dock: 'bottom',
								displayInfo: true
						}]
					}
				]
			});
			previewWin.show();
			
			
			
			resultsStore.getProxy().buildRequest = function (operation) {
				var initialParams = Ext.apply({
					paging: true,
					sortField: operation.getSorters()[0].getProperty(),
					sortOrder: operation.getSorters()[0].getDirection(),
					offset: operation.getStart(),
					max: operation.getLimit()
				}, operation.getParams());
				params = Ext.applyIf(initialParams, resultsStore.getProxy().getExtraParams() || {});

				var request = new Ext.data.Request({
					url: '/openstorefront/api/v1/service/search/advance',
					params: params,
					operation: operation,
					action: operation.getAction(),
					jsonData: Ext.util.JSON.encode(search)
				});
				operation.setRequest(request);

				return request;
			};

			resultsStore.loadPage(1);
			
		}
	},
	
	checkForCriteria: function() {
		var advancePanel = this;
		
		var store = advancePanel.entryForm.getComponent('searchGrid').getStore();
		if (store.getCount() <= 0) {
			Ext.Msg.show({
				title:'Validation',
				message: 'Enter search criteria to continue.',
				buttons: Ext.Msg.OK,
				icon: Ext.Msg.ERROR,
				fn: function(btn) {
				}
			});			
			return false;
		}
		return true;
	}
	
});


