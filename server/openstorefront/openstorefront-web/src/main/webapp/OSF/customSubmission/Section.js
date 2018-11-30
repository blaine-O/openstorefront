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

Ext.define('OSF.customSubmission.Section', {
	extend: 'Ext.form.Panel',
	alias: 'widget.osf-submissionform-section',
	requires: [		
		'OSF.customSubmission.form.AttributeRequired',
		'OSF.customSubmission.field.AttributesGrid',
		'OSF.customSubmission.field.ContactsGrid',
		'OSF.customSubmission.SubmissionFormWrapper',
		'OSF.customSubmission.field.DependenciesGrid',
		'OSF.customSubmission.field.MediaGrid',
		'OSF.customSubmission.field.RelationshipsGrid',
		'OSF.customSubmission.field.ResourcesGrid',
		'OSF.customSubmission.field.ResourceSimple',
		'OSF.customSubmission.field.TagsGrid',
		'OSF.customSubmission.field.TextArea',
		'OSF.customSubmission.field.Text',
		'OSF.customSubmission.field.Number',
		'OSF.customSubmission.field.Date',
		'OSF.customSubmission.field.RichText',
		'OSF.customSubmission.field.Organization',
		'OSF.customSubmission.field.AttributeSingle',
		'OSF.customSubmission.field.ChildSubmissions',
		'OSF.customSubmission.field.StaticContent'
	],
	
	width: '100%',
	showSectionName: true,
	scrollable: true,
	bodyStyle: 'padding: 20px;',
	layout: 'anchor',
	
	initComponent: function () {
		var section = this;
		section.callParent();
	},
	
	load: function(sectionData, template, userSubmission) {
		
		var section = this;
		
		var itemsToAdd = [];
		if (section.showSectionName) {
			itemsToAdd.push({
				xtype: 'panel',
				width: '100%',
				data: sectionData,
				tpl: new Ext.XTemplate(
					'<div class="submission-section-title">{name}</div>'
				)
			});		
		}
		itemsToAdd.push({
				xtype: 'panel',
				width: '100%',
				margin: '0 0 20 0',
				data: sectionData,
				tpl: new Ext.XTemplate(
					'<div class="submission-instructions">{instructions}</div>'
				)
		});
		
		
		Ext.Array.each(sectionData.fields, function(field){

			var createQuestionLabel = function() {
				var fullLabel = '';
				if (field.questionNumber) {
					fullLabel += '<span class="submission-question-number">' +field.questionNumber + '</span>  ';
				}

				if (field.label) {
					var tooltip = '';
					if (field.labelTooltip) {
						tooltip = ' <i class="fa fa-lg fa-question-circle"  data-qtip="' + field.labelTooltip+'"></i>';
					}
					var required = field.required ? '<span class="field-required" />' : '';			

					fullLabel += '<span class="submission-label">' + field.label + '</span>'
							+ required + tooltip;
				}	
				return fullLabel;
			};			
			
			var defaults = {
				fieldTemplate: field,
				createQuestionLabel: createQuestionLabel,
				componentType: section.componentType,
				section: section,
				margin: '0 0 20 0'
			};
			switch(field.fieldType) {
				case 'ATTRIBUTE_SINGLE':
				case 'ATTRIBUTE_RADIO':
				case 'ATTRIBUTE_MCHECKBOX':
					itemsToAdd.push(Ext.apply(defaults, {
						xtype: 'osf-submissionform-attributesingle'
					}));
				break;
				case 'ATTRIBUTE':
					itemsToAdd.push(Ext.apply(defaults, {
						xtype: 'osf-submissionform-formwrapper',
						actualForm: {
							xtype: 'osf-submissionform-attribute',
							componentType: section.componentType,
							section: section
						}						
					}));
				break;		
				case 'ATTRIBUTE_REQUIRED':
					itemsToAdd.push(Ext.apply(defaults, {
						xtype: 'osf-submissionform-formwrapper',
						actualForm: {
							xtype: 'osf-submissionform-attributerequired',
							componentType: section.componentType,
							section: section
						}						
					}));
				break;
				case 'ATTRIBUTE_MULTI':
					itemsToAdd.push(Ext.apply(defaults, {
						xtype: 'osf-submissionform-attributegrid'
					}));
				break;
				case 'CONTACT':
					itemsToAdd.push(Ext.apply(defaults, {
						xtype: 'osf-submissionform-formwrapper',
						actualForm: {
							xtype: 'osf-submissionform-contact',
							section: section
						}						
					}));
				break;				
				case 'CONTACT_MULTI':
					itemsToAdd.push(Ext.apply(defaults, {
						xtype: 'osf-submissionform-contactgrid'						
					}));
				break;	
				case 'EXT_DEPEND':
					itemsToAdd.push(Ext.apply(defaults, {
						xtype: 'osf-submissionform-formwrapper',
						actualForm: {
							xtype: 'osf-submissionform-dependency',
							section: section
						}						
					}));
				break;					
				case 'EXT_DEPEND_MULTI':
					itemsToAdd.push(Ext.apply(defaults, {
						xtype: 'osf-submissionform-dependencygrid'
					}));
				break;
				case 'MEDIA':
					itemsToAdd.push(Ext.apply(defaults, {
						xtype: 'osf-submissionform-formwrapper',
						actualForm: {
							xtype: 'osf-submissionform-media',
							section: section
						}						
					}));
				break;					
				case 'MEDIA_MULTI':
					itemsToAdd.push(Ext.apply(defaults, {
						xtype: 'osf-submissionform-mediagrid'						
					}));
				break;				
				case 'RELATIONSHIPS':
					itemsToAdd.push(Ext.apply(defaults, {
						xtype: 'osf-submissionform-formwrapper',
						actualForm: {
							xtype: 'osf-submissionform-relationships',
							section: section
						}						
					}));
				break;					
				case 'RELATIONSHIPS_MULTI':
					itemsToAdd.push(Ext.apply(defaults, {
						xtype: 'osf-submissionform-relationshipgrid'						
					}));
				break;
				case 'RESOURCE':
					itemsToAdd.push(Ext.apply(defaults, {
						xtype: 'osf-submissionform-formwrapper',
						actualForm: {
							xtype: 'osf-submissionform-resource',
							section: section
						}						
					}));
				break;	
				case 'RESOURCE_SIMPLE':
					itemsToAdd.push(Ext.apply(defaults, {
						xtype: 'osf-submissionform-resourcesimple'
					}));
				break;			
				case 'RESOURCE_MULTI':
					itemsToAdd.push(Ext.apply(defaults, {
						xtype: 'osf-submissionform-resourcegrid'						
					}));
				break;
				case 'TAG':
					itemsToAdd.push(Ext.apply(defaults, {
						xtype: 'osf-submissionform-formwrapper',
						actualForm: {
							xtype: 'osf-submissionform-tags',
							section: section
						}						
					}));
				break;					
				case 'TAG_MULTI':
					itemsToAdd.push(Ext.apply(defaults, {
						xtype: 'osf-submissionform-tagsgrid'						
					}));
				break;
				case 'SUBMISSIONS':
					itemsToAdd.push(Ext.apply(defaults, {
						xtype: 'osf-submissionform-childsubmissions'						
					}));
				break;			
				
				case 'TEXT':
					itemsToAdd.push(Ext.apply(defaults, {
						xtype: 'osf-submissionform-text',
						labelWidth: 300,
						labelAlign: field.labelAlign ? field.labelAlign : 'top'
					}));
				break;
				case 'TEXTAREA':
					itemsToAdd.push(Ext.apply(defaults, {
						xtype: 'osf-submissionform-textarea',
						labelWidth: 300,
						labelAlign: field.labelAlign ? field.labelAlign : 'top'
					}));
				break;
				case 'NUMBER':
					itemsToAdd.push(Ext.apply(defaults, {
						xtype: 'osf-submissionform-number',
						labelWidth: 300,
						labelAlign: field.labelAlign ? field.labelAlign : 'top'
					}));
				break;
				case 'DATE':
					itemsToAdd.push(Ext.apply(defaults, {
						xtype: 'osf-submissionform-date',
						labelWidth: 300,
						labelAlign: field.labelAlign ? field.labelAlign : 'top'
					}));
				break;
				case 'RICHTEXT':
					itemsToAdd.push(Ext.apply(defaults, {
						xtype: 'osf-submissionform-richtext',
						labelWidth: 300,
						labelAlign: field.labelAlign ? field.labelAlign : 'top'
					}));
				break;
				case 'ORGANIZATION':
					itemsToAdd.push(Ext.apply(defaults, {
						xtype: 'osf-submissionform-organization',
						labelWidth: 300,
						labelAlign: field.labelAlign ? field.labelAlign : 'top'
					}));
				break;	
				case 'CONTENT':
					itemsToAdd.push(Ext.apply(defaults, {						
						xtype: 'osf-submissionform-staticcontent'
					}));
				break;			
			}			
		});
				
		section.add(itemsToAdd);
	},		
	
	getUserValues: function () {
		var data = [];
		var section = this;
				
		Ext.Array.forEach(section.items.items, function (field) {
			if (!field.skipOnData && field.getUserData) {
				var userData = field.getUserData();
				if (userData) {
					data.push(field.getUserData());
				}
				if (field.xtype === "osf-submissionform-formwrapper") {
					field.checkHidden();
				}
			}
		});
		return data;
	},
	
	getReviewableQuestions: function () {
		var section = this;
		
		var questions = [];
		Ext.Array.forEach(section.items.items, function (field) {
			var skip = field.skipOnReview ? field.skipOnReview : false;
			if (field.createQuestionLabel && !skip) {
				questions.push({
					question: field.createQuestionLabel(),
					value: field.reviewDisplayValue ? field.reviewDisplayValue() : 'N/A',
					valid: field.isValid ? field.isValid() : true
				});
			}	
		});
		return questions;
	},
	valid: function() {
		var section = this;
		
		var valid = true;
		Ext.Array.forEach(section.items.items, function (field) {
			var skip = field.skipOnReview ? field.skipOnReview : false;
			if (field.createQuestionLabel && !skip) {
				if (field.isValid && !field.isValid()) {
					valid = false;
				}
			}
		});
		
		return valid;
	}
	
});
