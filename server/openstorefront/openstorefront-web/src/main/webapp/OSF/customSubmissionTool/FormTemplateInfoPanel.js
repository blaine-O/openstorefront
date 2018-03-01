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
Ext.define('OSF.customSubmissionTool.FormTemplateInfoPanel', {
	extend: 'Ext.form.Panel',
	alias: 'widget.osf-form-templateinfo-panel',
	
	layout: 'anchor',
	scrollable: true,
	bodyStyle: 'padding: 10px;',
	defaults: {
		labelAlign: 'top',
		labelSeparator: '',
		width: '100%'
	},

	initComponent: function () {
		this.callParent();
		var infoPanel = this;
		
		
		var items = [
			{
				xtype: 'textfield',
				fieldLabel: 'Name <span class="field-required" />',
				value: infoPanel.templateRecord.formName,
				maxLength: 255,
				allowBlank: false				
			},
			{
				xtype: 'textfield',
				fieldLabel: 'Description <span class="field-required" />',
				labelAlign: 'top',
				value: infoPanel.templateRecord.description,
				maxLength: 255,
				allowBlank: false	
			},
			{
				xtype: 'panel',
				html: '<b>Last Saved: </b>' + Ext.Date.format(new Date(), 'F j, Y, g:i a')
			}
			
		];		
		
		infoPanel.add(items);
	}
	
	
});
	

