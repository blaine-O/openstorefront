/* 
 * Copyright 2018 Space Dynamics Laboratory - Utah State University Research Foundation.
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
 /* Author: cyearsley */
/* global Ext, CoreUtil, CoreService, data */

Ext.define('OSF.workplanManagementTool.StepFormPanel', {
	extend: 'OSF.workplanManagementTool.WPDefaultPanel',
	alias: 'widget.osf.wp.stepForm',
	requires: [
		'OSF.workplanManagementTool.AddStepActionWindow'
	],

	style: 'background: #fff; border-bottom: 1px solid #ececec;',
	title: 'Step Configuration',
	layout: {
		type: 'vbox',
		pack: 'center',
		align: 'middle'
	},
	canSave: true,
	items: [
		{
			xtype: 'container',
			itemId: 'defaultContainer',
			style: 'border: 1px solid #ccc; border-radius: 5px; padding: 15px;',
			html: '<span style="font-size: 20px;">You can modify step configurations here<br />Select or create a step <b>above</b> to continue</span>',
			hidden: true
		},
		{
			xtype: 'form',
			itemId: 'stepFormPanel',
			hidden: true,
			style: 'text-align: left;',
			width: '100%',
			height: '100%',
			padding: '5%',
			scrollable: true,
			layout: {
				type: 'table',
				columns: 2
			},
			defaults: {
				labelAlign: 'top',
				width: '90%',
				canAlertOnChange: false,
				listeners: {
					change: function (field, newVal, oldVal) {

						var wpWindow = field.up('window');
						var stepForm = field.up('[itemId=stepFormPanel]').getForm();

						// save the current form
						if (wpWindow.stepForm.canSave) {
							Ext.apply(wpWindow.getSelectedStep(), stepForm.getValues());
						}
						if (field.canAlertOnChange) {
							wpWindow.stepForm.alert();
						}
					}
				}
			},
			items: [
				{
					xtype: 'textfield',
					fieldLabel: 'Step name <i class="fa fa-question-circle" data-qtip="This is the name of the step (will be displayed to end users)" ></i> <span class="field-required" />',
					name: 'name',
					maxLength: 20,
					enforceMaxLength: true,
					canAlertOnChange: true
				},
				{
					xtype: 'ActiveOnMultiCombo',
					fieldLabel: 'Active On <i class="fa fa-question-circle" data-qtip="Will be set as the current step if one of these events occurs" ></i>',
					name: 'triggerEvents',
					width: '100%'
				},
				{
					xtype: 'textarea',
					fieldLabel: 'Short Description <i class="fa fa-question-circle" data-qtip="A short description of what the step is for" ></i> <span class="field-required" />',
					name: 'description',
					allowBlank: false,
					canAlertOnChange: true
				},
				{
					fieldLabel: 'Role Access <i class="fa fa-question-circle" data-qtip="Roles that will have access to manipulate a record on this step" ></i>',
					xtype: 'RoleGroupMultiSelectComboBox',
					width: '100%',
					name: 'stepRole'
				},
				{
					fieldLabel: 'Approval State to Match <i class="fa fa-question-circle" data-qtip="This will be the <b>default</b> active step for an record that has been assigned to this workplan that has this record status" ></i>',
					xtype: 'combo',
					name: 'approvalStateToMatch',
					colspan: 2,
					width: '45.1%',
					displayField: 'description',
					valueField: 'code',
					editable: false,
					value: 'none',
					store: {
						autoLoad: true,
						proxy: {
							type: 'ajax',
							url: 'api/v1/resource/lookuptypes/ApprovalStatus'
						},
						fields: ['code', 'description'],
						listeners: {
							load: function (store, records) {
								store.add({code: 'none', description: 'None'});
							}
						}
					}
				},
				{
					xtype: 'grid',
					sortableColumns: false,
					itemId: 'stepActionGrid',
					title: 'Step Actions <i class="fa fa-question-circle" data-qtip="These action will be performed once this step becomes active" ></i>',
					colspan: 2,
					width: '100%',
					style: 'border: 1px solid #ccc;',
					height: 300,
					store:  {
						sortInfo: { field: 'actionOrder', direction: 'DESC' },
						handleRecordChange: function (store) {

							var wpWindow = Ext.getCmp('workplanWindow');
							wpWindow.getSelectedStep().actions = store.getData().items.map(function (item) {
								return item.getData();
							});
						},
						listeners: {
							add: function (store, records) {

								this.handleRecordChange(store);
							},
							remove: function (store, records) {

								this.handleRecordChange(store);
							}
						}
					},
					columns: [
						{ text: 'Action Type', dataIndex: 'workPlanStepActionType' ,flex: 3 },
						{ text: 'Metadata', dataIndex: 'actionOption', flex: 3,
							renderer: function (metadata) {

								if (metadata.fixedEmails === '' || metadata.fixedEmails) {
									var emails = '';
									metadata.fixedEmails = metadata.fixedEmails === '' ? [] : metadata.fixedEmails;
									Ext.Array.forEach(metadata.fixedEmails, function (email) {
										emails += 'Email to: ' + email + '<b style="font-size: 1.2em;">;</b> ';
									});

									return emails === '' ? 'No emails specified' : emails;
								}
								else if (typeof metadata.assignGroup !== 'undefined' || typeof metadata.assignUser !== 'undefined') {
									if (metadata.assignGroup === '' || metadata.assignUser === '') {
										return metadata.assignType === 'group' ? 'No group specified' : 'No user specified';
									}
									return metadata.assignGroup || metadata.assignUser;
								}
								return 'N/A';
							}
						},
						{ text: 'Order', dataIndex: 'actionOrder', flex: 1 }
					],
					listeners: {
						selectionchange: function (grid, recordsSelected) {

							grid = this;
							if (recordsSelected.length > 0) {
								grid.down('[itemId=removeActionButton]').enable();
								grid.down('[itemId=editActionButton]').enable();
								grid.down('[itemId=incrementOrderButton]').enable();
								grid.down('[itemId=decrementOrderButton]').enable();
							}
							else {
								grid.down('[itemId=removeActionButton]').disable();
								grid.down('[itemId=editActionButton]').disable();
								grid.down('[itemId=incrementOrderButton]').disable();
								grid.down('[itemId=decrementOrderButton]').disable();
							}
						}
					},
					addEditRecord: function (isEditing) {

						var grid = this;
						Ext.create({
							xtype: 'osf.wp.AddStepActionWindow',
							width: 620,
							maximizable: true,
							height: 900,
							stepActionGrid: grid,
							isEditing: isEditing,
							recordToLoad: grid.getSelection().length > 0 && isEditing ? grid.getSelection()[0].getData() : null
						}).show();
					},
					dockedItems: [{
						xtype: 'toolbar',
						dock: 'bottom',
						items: [
							{
								xtype: 'button',
								text: 'Add Action',
								scale: 'medium',
								iconCls: 'fa fa-2x fa-plus icon-button-color-save icon-vertical-correction',
								handler: function () {
									this.up('grid').addEditRecord(false);
								}
							},
							{
								xtype: 'button',
								itemId: 'editActionButton',
								text: 'Edit Action',
								scale: 'medium',
								disabled: true,
								iconCls: 'fa fa-2x fa-pencil-square-o icon-button-color-save icon-vertical-correction',
								handler: function () {
									this.up('grid').addEditRecord(true);
								}
							},
							{
								xtype: 'button',
								itemId: 'incrementOrderButton',
								text: 'Increment Order',
								scale: 'medium',
								disabled: true,
								hidden: true, // remove this property when functionality has been implemented
								iconCls: 'fa fa-2x fa-chevron-up icon-button-color-save icon-vertical-correction'
							},
							{
								xtype: 'button',
								itemId: 'decrementOrderButton',
								text: 'Decrement Order',
								scale: 'medium',
								disabled: true,
								hidden: true, // remove this property when functionality has been implemented
								iconCls: 'fa fa-2x fa-chevron-down icon-button-color-save icon-vertical-correction'
							},
							{
								xtype: 'tbfill'
							},
							{
								xtype: 'button',
								text: 'Remove Action',
								scale: 'medium',
								itemId: 'removeActionButton',
								iconCls: 'fa fa-2x fa-trash icon-button-color-warning icon-vertical-correction',
								disabled: true,
								handler: function () {
									var grid = this.up('[itemId=stepFormPanel]').down('[itemId=stepActionGrid]');
									grid.getStore().remove(grid.getSelection());

									grid.down('[itemId=removeActionButton]').disable();
									grid.down('[itemId=editActionButton]').disable();
									grid.down('[itemId=incrementOrderButton]').disable();
									grid.down('[itemId=decrementOrderButton]').disable();
								}
							}
						]
					}]
				}
			]
		}
	],

	alertChange: function () {

		var defaultContainer = this.down('[itemId=defaultContainer]');
		var stepFormPanel = this.down('[itemId=stepFormPanel]');
		if (this.getWpWindow().getSelectedStep() === null) {
			defaultContainer.show();
			stepFormPanel.hide();
		}
		else {
			defaultContainer.hide();
			stepFormPanel.show();

			stepFormPanel.getForm().setValues(this.getWpWindow().getSelectedStep());
			this.down('[itemId=stepActionGrid]').getStore().setData(this.getWpWindow().getSelectedStep().actions);
		}
	},

	clearForm: function () {
		this.down('form').getForm().reset();
	}
});
