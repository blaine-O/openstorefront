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

Ext.define('OSF.widget.SavedSearch', {
	extend: 'Ext.panel.Panel',
	alias: 'osf.widget.SavedSearch',

	layout: 'fit',
	
	initComponent: function () {
		this.callParent();
		
		var searchPanel = this;
		
		searchPanel.configPanel = Ext.create('Ext.form.Panel', {
			layout: 'anchor',
			bodyStyle: 'padding: 10px;',
			items: [
				{
					xtype: 'combobox',
					itemId: 'userSearchId',
					name: 'userSearchId',
					labelAlign: 'top',
					labelSeparator: '',
					width: '100%',
					allowBlank: false,
					fieldLabel: 'Select Save Search to Display',					
					displayField: 'searchName',
					valueField: 'userSearchId',
					editable: false,
					typeahead: false,
					queryMode: 'remote',
					store: {
						proxy: {
							type: 'ajax',
							url: '../api/v1/resource/usersavedsearches/user/current'
						}
					}
				}
			],		
			dockedItems: [
				{
					xtype: 'toolbar',
					dock: 'bottom',
					items: [
						{
							text: 'Apply',
							formBind: true,
							iconCls: 'fa fa-check',
							handler: function() {	
								var form = this.up('form');
								var data = form.getValues();
				
								searchPanel.userSearchId = data.userSearchId;
								searchPanel.loadSavedSearch();
								if (searchPanel.configChange) {
									searchPanel.configChange();
								}
							}
						}
					]
				}
			]
		});
		
		searchPanel.resultsStore = Ext.create('Ext.data.Store', {
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
		
		searchPanel.resultsStore.getProxy().buildRequest = function (operation) {
			var initialParams = Ext.apply({
				paging: true,
				sortField: operation.getSorters()[0].getProperty(),
				sortOrder: operation.getSorters()[0].getDirection(),
				offset: operation.getStart(),
				max: operation.getLimit()
			}, operation.getParams());
			var params = Ext.applyIf(initialParams, searchPanel.resultsStore.getProxy().getExtraParams() || {});

			var request = new Ext.data.Request({
				url: '/openstorefront/api/v1/service/search/advance',
				params: params,
				operation: operation,
				action: operation.getAction(),
				jsonData: Ext.util.JSON.encode(searchPanel.search)
			});
			operation.setRequest(request);

			return request;
		};
		
		searchPanel.resultsPanel = Ext.create('Ext.grid.Panel', {
			columnLines: true,
			store: searchPanel.resultsStore,
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
			listeners: {	
				selectionchange: function(selectionModel, selected, opts){
					var tools = this.getComponent('tools');

					if (selected.length > 0) {	
						tools.getComponent('view').setDisabled(false);
					} else {
						tools.getComponent('view').setDisabled(true);
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
							text: 'View Entry',
							itemId: 'view',
							disabled: true,
							iconCls: 'fa fa-binoculars',
							handler: function () {
								var componentId = this.up('grid').getSelectionModel().getSelection()[0].get('componentId');
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
							}
						},
						{
							xtype: 'tbfill'
						},
						{
							text: 'Configure',
							iconCls: 'fa fa-gear',
							handler: function(){
								searchPanel.cardPanel.getLayout().setActiveItem(searchPanel.configPanel);
							}
						}
					]
				},
				{
					xtype: 'pagingtoolbar',
					store: searchPanel.resultsStore,
					dock: 'bottom',
					displayInfo: true
				}
			]			
		});		
		
		searchPanel.cardPanel = Ext.create('Ext.panel.Panel', {
			layout: 'card',
			items: [
				searchPanel.configPanel,
				searchPanel.resultsPanel
			]
		});
		
		searchPanel.add(searchPanel.cardPanel);
		
		if (searchPanel.userSearchId) {
			searchPanel.loadSavedSearch();			
		}
		
	},
	refresh: function() {
		var searchPanel = this;
		if (searchPanel.userSearchId) {
			searchPanel.resultsStore.loadPage(1);	
		}
	},
	loadSavedSearch: function() {
		var searchPanel = this;
		
		if (searchPanel.userSearchId) {
			searchPanel.setLoading("Loading Search...");
			Ext.Ajax.request({
				url: '../api/v1/resource/usersavedsearches/' + searchPanel.userSearchId,
				callback: function(){
					searchPanel.setLoading(false);	
				},
				success: function(response, opts) {					
					searchPanel.search = Ext.decode(Ext.decode(response.responseText).searchRequest);
					
					searchPanel.cardPanel.getLayout().setActiveItem(searchPanel.resultsPanel);					
					searchPanel.resultsStore.loadPage(1);
				}, 
				failure: function(response, opts) {	
					searchPanel.userSearchId = null;
					searchPanel.cardPanel.getLayout().setActiveItem(searchPanel.configPanel);
				}
			});
		} else {
			searchPanel.cardPanel.getLayout().setActiveItem(searchPanel.configPanel);
		}
	},
	saveConfig: function() {
		var searchPanel = this;
		return {
			userSearchId: searchPanel.userSearchId
		};
	},
	restoreConfig: function(config) {
		var searchPanel = this;
		
		if (config && config.userSearchId) {
			searchPanel.userSearchId = config.userSearchId;
			searchPanel.configPanel.getComponent('userSearchId').setValue(searchPanel.userSearchId);
			searchPanel.loadSavedSearch();
		}
		
	}
	
});
