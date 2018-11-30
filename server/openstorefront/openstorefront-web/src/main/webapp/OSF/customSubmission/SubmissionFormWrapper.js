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
/* global Ext, submissionGrid */

Ext.define('OSF.customSubmission.SubmissionFormWrapper', {
	extend: 'Ext.form.FieldSet',
	alias: 'widget.osf-submissionform-formwrapper',
	
	//Set this to the form 
	actualForm: null,
	
	fieldTemplate: {
		fieldType: null,
		mappingType: 'COMPLEX',
		questionNumber: null,
		label: null,
		labelTooltip: null,
		required: null
	},
	
	initComponent: function () {
		var submissionField = this;
		submissionField.callParent();
		submissionField.display();
	},
	
	display: function() {
		var submissionField = this;
		
		submissionField.setTitle(submissionField.createQuestionLabel());
		
		if (submissionField.actualForm) {
			submissionField.actualForm.fieldTemplate = submissionField.fieldTemplate;
			submissionField.actualForm.itemId = 'component';
			submissionField.add(submissionField.actualForm);
		}	
	},

	checkHidden: function() {
		var submissionField = this;
		var formComponent = submissionField.queryById('component')
		if (formComponent.checkIsHidden ? formComponent.checkIsHidden() : 'N/A') {
			submissionField.setHidden(true);
		}
	},
	
	reviewDisplayValue: function() {
		var submissionField = this;
		var formComponent = submissionField.queryById('component');
		return formComponent.reviewDisplayValue ? formComponent.reviewDisplayValue() : 'N/A';
	},
	
	/**
	 * Override to add options
	 */
	getOptionPanel: function() {
		return Ext.create('Ext.panel.Panel', {});
	},
	
	/**
	 * Override to handle validation
	 */	
	isValid: function() {
		var submissionField = this;
		var formComponent = submissionField.queryById('component');
		return formComponent.isValid();
	},
	
	/**
	 * Always call this as it may be overriden 
	 */
	getTemplateData: function() {
		var submissionField = this;
		return submissionField.fieldTemplate;
	},
	
	setTemplateData: function(editData) {
		var submissionField = this;
		submissionField.fieldTemplate = editData;
		submissionField.display();
	},

	getUserData: function() {
		var submissionField = this;
		var formComponent = submissionField.queryById('component');
		return formComponent.getSubmissionValue();
	}
	
});

