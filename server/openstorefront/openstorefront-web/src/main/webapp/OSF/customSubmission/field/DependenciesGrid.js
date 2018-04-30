/* 
 * Copyright 2018 Space Dynamics Laboratory - Utah State University Research Foundation.
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
/* global Ext, CoreUtil, CoreService */

/* Author: cyearsley */

Ext.define('OSF.customSubmission.field.DependenciesGrid', {
	extend: 'OSF.customSubmission.SubmissionBaseGrid',
	alias: 'widget.osf-submissionform-dependencygrid',
	requires: [
		'OSF.customSubmission.form.Dependencies'
	],
	
	title: '',
	fieldType: 'EXT_DEPEND_MULTI',
	
	columns: [
		{ text: 'Name', dataIndex: 'dependencyName', width: 200 },
		{ text: 'Version', dataIndex: 'version', width: 200 },
		{ text: 'Link', dataIndex: 'dependancyReferenceLink', width: 200 },
		{ text: 'Comment', dataIndex: 'comment', flex: 1, minWidth: 200  },
		{ text: 'Security Marking', dataIndex: 'securityMarking', width: 200, hidden: true },
		{ text: 'Data Sensitivity', dataIndex: 'dataSensitivity', width: 200, hidden: true }
	],	
	
	initComponent: function () {
		var grid = this;
		grid.callParent();	
		
	},		
	
	actionAddEdit: function(record) {
		var grid = this;
		
		var addEditWin = Ext.create('Ext.window.Window', {
			title: 'Add/Edit Dependency',
			modal: true,
			width: 800,
			height: 515,
			closeMode: 'destroy',
			layout: 'fit',
			items: [
				{
					xtype: 'osf-submissionform-dependency',
					itemId: 'form',
					scrollable: true,
					dockedItems: [
						{
							xtype: 'toolbar',
							dock: 'bottom',
							items: [
								{
									text: 'Save',
									formBind: true,
									iconCls: 'fa fa-lg fa-edit icon-button-color-edit',
									handler: function () {
										var form = this.up('form');
										var data = form.getValues();
										
										grid.getStore().add(data);
										this.up('window').close();
									}
								},
								{
									xtype: 'tbfill'
								},
								{
									text: 'Cancel',
									iconCls: 'fa fa-lg fa-close icon-button-color-warning',
									handler: function () {
										this.up('window').close();												
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
			addEditWin.queryById('form').loadRecord(record);
		}		
		
	}	
	
});
