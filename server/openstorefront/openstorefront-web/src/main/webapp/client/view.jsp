<%--
Copyright 2016 Space Dynamics Laboratory - Utah State University Research Foundation.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<stripes:layout-render name="layout/toplevelLayout.jsp">
    <stripes:layout-component name="contents">
		
	<script src="scripts/component/templateBlocks.js?v=${appVersion}" type="text/javascript"></script>
	<script src="scripts/component/mediaViewer.js?v=${appVersion}" type="text/javascript"></script>
	<script src="scripts/component/relationshipVisualization.js?v=${appVersion}" type="text/javascript"></script>		
	<script src="scripts/component/reviewWindow.js?v=${appVersion}" type="text/javascript"></script>
	<script src="scripts/component/questionWindow.js?v=${appVersion}" type="text/javascript"></script>
	
	<div style="display:none; visibility: hidden;" id="templateHolder"></div>	
		
	<script type="text/javascript">
		/* global Ext, CoreService, CoreApp */	
		
		var DetailPage = {
			showRelatedWindow: function(attributeType, attributeCode, description, vitalType, tip) {
				DetailPage.relatedWindow.show();
				DetailPage.relatedWindow.setTitle('Related Entries');
								
				DetailPage.relatedWindow.getComponent('grid').getComponent('description').update({
					description: description,
					tip: tip
				});
				
				var searchObj = {
					"sortField": "name",
					"sortDirection": "DESC",				
					"searchElements": [{
							"searchType": vitalType,
							"keyField": attributeType,
							"keyValue": attributeCode,
							"caseInsensitive": true,
							"numberOperation": "EQUALS",
							"stringOperation": "EQUALS",
							"mergeCondition": "OR" 
					}]
				 };
				
				var store = DetailPage.relatedWindow.getComponent('grid').getStore();
				store.getProxy().buildRequest = function (operation) {
					var initialParams = Ext.apply({
						paging: true,
						sortField: operation.getSorters()[0].getProperty(),
						sortOrder: operation.getSorters()[0].getDirection(),
						offset: operation.getStart(),
						max: operation.getLimit()
					}, operation.getParams());
					params = Ext.applyIf(initialParams, store.getProxy().getExtraParams() || {});

					var request = new Ext.data.Request({
						url: '/openstorefront/api/v1/service/search/advance',
						params: params,
						operation: operation,
						action: operation.getAction(),
						jsonData: Ext.util.JSON.encode(searchObj)
					});
					operation.setRequest(request);

					return request;
				};
				store.loadPage(1);
			},
			showRelatedOrganizations: function(organization) {
				DetailPage.relatedWindow.show();
				DetailPage.relatedWindow.setTitle('Related Entries');
							
			    var descriptionPanel = DetailPage.relatedWindow.getComponent('grid').getComponent('description');
				descriptionPanel.update({
					description: organization,
					tip: ''
				});
				
				Ext.Ajax.request({
					url: '../api/v1/resource/organizations/name/' + organization,
					success: function(response, opts) {
						var org = Ext.decode(response.responseText);
						
						var fullDescription = '';
						if (org.organizationType) {
							fullDescription += '<b>Organization Type:</b> ' + org.organizationType + '<br>';
						}
						if (org.agency) {
							fullDescription += '<b>Agency:</b> ' + org.agency + '<br>';
						}
						if (org.department) {
							fullDescription += '<b>Department:</b> ' + org.department + '<br>';
						}	
						if (org.department) {
							fullDescription += '<b>Home Page:</b> ' + org.homeUrl + '<br>';
						}						
						if (org.description) {
							fullDescription += org.description + '<br>';
						}
						
						descriptionPanel.update({
							description: organization,
							tip: fullDescription
						});
					}
				});
				
				var searchObj = {
					"sortField": "name",
					"sortDirection": "DESC",				
					"searchElements": [{
							"searchType": 'COMPONENT',
							"field": 'organization',
							"value": organization,
							"caseInsensitive": true,
							"numberOperation": "EQUALS",
							"stringOperation": "EQUALS",
							"mergeCondition": "OR" 
					}]
				 };
				
				var store = DetailPage.relatedWindow.getComponent('grid').getStore();
				store.getProxy().buildRequest = function (operation) {
					var initialParams = Ext.apply({
						paging: true,
						sortField: operation.getSorters()[0].getProperty(),
						sortOrder: operation.getSorters()[0].getDirection(),
						offset: operation.getStart(),
						max: operation.getLimit()
					}, operation.getParams());
					params = Ext.applyIf(initialParams, store.getProxy().getExtraParams() || {});

					var request = new Ext.data.Request({
						url: '/openstorefront/api/v1/service/search/advance',
						params: params,
						operation: operation,
						action: operation.getAction(),
						jsonData: Ext.util.JSON.encode(searchObj)
					});
					operation.setRequest(request);

					return request;
				};
				store.loadPage(1);				
			}
		};
		
		var ViewPage = {
			editReview: function(reviewId) {
				var reviewData;
				Ext.Array.each(ViewPage.reviews, function(review){
					if (review.reviewId === reviewId) {
						reviewData = review;
					}
				});
				
				var record = Ext.create('Ext.data.Model', {					
				});
				record.set(reviewData);
				
				ViewPage.reviewWindow.show();
				ViewPage.reviewWindow.editReview(record);
			},
			
			deleteReview: function(reviewId, componentId) {
				Ext.Msg.show({
					title:'Remove Review?',
					message: 'Are you sure you want to review this review?',
					buttons: Ext.Msg.YESNO,
					icon: Ext.Msg.QUESTION,
					fn: function(btn) {
						if (btn === 'yes') {
							Ext.getCmp('reviewPanel').setLoading("Removing...");
							Ext.Ajax.request({
								url: '../api/v1/resource/components/'+componentId+'/reviews/'+reviewId,
								method: 'DELETE',
								callback: function(){
									Ext.getCmp('reviewPanel').setLoading(false);
								},
								success: function(){
									ViewPage.refreshReviews();
								}
							});
						} 
					}
				});				
			},
			editResponse: function(responseId) {
				var responseData;
				Ext.Array.each(ViewPage.questions, function(question){
					Ext.Array.each(question.responses, function(response){
						if (response.responseId === responseId) {
							responseData = response;							
						}						
					});
				});
				
				var record = Ext.create('Ext.data.Model', {					
				});
				record.set(responseData);
				
				ViewPage.responseWindow.show();
				ViewPage.responseWindow.edit(record);				
				
			},
			deleteResponse: function(responseId, questionId, componentId){
				
				Ext.Msg.show({
					title:'Remove Answer?',
					message: 'Are you sure you want to review this answer?',
					buttons: Ext.Msg.YESNO,
					icon: Ext.Msg.QUESTION,
					fn: function(btn) {
						if (btn === 'yes') {
							Ext.getCmp('questionPanel').setLoading("Removing...");
							Ext.Ajax.request({
								url: '../api/v1/resource/components/'+componentId+'/questions/' + questionId + '/responses/' + responseId,
								method: 'DELETE',
								callback: function(){
									Ext.getCmp('questionPanel').setLoading(false);
								},
								success: function(){
									ViewPage.refreshQuestions();
								}
							});
						} 
					}
				});				
			}			
			
		};	
					
		
		
		Ext.onReady(function(){		
			
			var componentId = '${param.id}';
			var fullPage = '${param.fullPage}' !== '' ? true : false;
			
			var relatedStore = Ext.create('Ext.data.Store', {
				pageSize: 50,
				autoLoad: false,
				remoteSort: true,
				sorters: [
					new Ext.util.Sorter({
					property: 'name',
					direction: 'DESC'
					})
				],				
				proxy: CoreUtil.pagingProxy({
					actionMethods: {create: 'POST', read: 'POST', update: 'POST', destroy: 'POST'},
					reader: {
						type: 'json',
						rootProperty: 'data',
						totalProperty: 'totalNumber'
					}
				}),
				listeners: {
					load: function(store, records) {
						store.filterBy(function(record){
							return record.get('componentId') !== componentId;
						});
					}
				}
			});
			
			DetailPage.relatedWindow = Ext.create('Ext.window.Window', {
				title: 'Related Entries - ',
				//x: 5,		
				//y: 220,
				width: '50%',
				height: '60%',
				modal: true,
				maximizable: true,
				layout: 'fit',
				items: [
					{
						xtype: 'grid',
						itemId: 'grid',
						columnLines: true,
						store: relatedStore,
						columns: [
							{ text: 'Name', dataIndex: 'name', flex:2, minWidth: 250, cellWrap: true, 
								renderer: function (value, meta, record) {
									return '<a class="details-table" href="view.jsp?id=' + record.get('componentId') + '&fullPage=true" target="_blank">' + value + '</a>'
								}
							},
							{ text: 'Description', dataIndex: 'description', flex: 2,
								cellWrap: true,
								renderer: function (value) {
									value = Ext.util.Format.stripTags(value);
									return Ext.String.ellipsis(value, 300);
								}
							},							
							{ text: 'Type', align: 'center', dataIndex: 'componentTypeDescription', width: 150 }							
						],
						dockedItems: [
							{
								xtype: 'pagingtoolbar',							
								dock: 'bottom',
								store: relatedStore,
								displayInfo: true
							},
							{
								xtype: 'panel',
								itemId: 'description',
								maxHeight: 200,
								bodyStyle: 'padding-left: 5px; padding-right: 5px;',
								scrollable: true,
								tpl: new Ext.XTemplate(
									'<h2 style="text-align: center;">{description}</h2><hr>',
									'{tip}'
								)
							}
						]						
					}				
				]			
			});		
		
			var headerPanel = Ext.create('Ext.panel.Panel', {
				region: 'north',
				bodyStyle: 'background: white; padding: 15px;',
				layout: {
					type: 'hbox',
					align: 'stretch'
				},
				items: [				
					{
						xtype: 'panel',
						id: 'titlePanel',
						flex: 1,
						minHeight: 125,						
						tpl: new Ext.XTemplate(
							'<div class="details-title-name">{name} <span class="details-title-info" style="font-size: 10px">({componentTypeLabel})</span></div>',
							'<div class="details-title-info">',							
							'Organization: <b><a href="#" onclick="DetailPage.showRelatedOrganizations(\'{organization}\')">{organization}</a></b><tpl if="version"> Version: <b>{version}</b></tpl><tpl if="version"> Release Date: <b>{[Ext.util.Format.date(values.releaseDate)]}</b></tpl>',							
							'</div>',
							'  <tpl for="attributes">',
							'    <tpl if="badgeUrl"><img src="{badgeUrl}" title="{codeDescription}" width="40" /></tpl>',
							'  </tpl>'
						)
					},
					{
						xtype: 'panel',
						id: 'toolsPanel',
						layout: {
							type: 'hbox'
						},
						dockedItems: [
							{
								xtype: 'panel',
								itemId: 'updatedInfo',
								dock: 'bottom',
								tpl: 'Updated: {[Ext.util.Format.date(values.lastActivityDts, "m/d/y H:i:s")]}</span>'
							}
						],
						items: [
							{
								xtype: 'button',
								iconCls: 'fa fa-2x fa-tags icon-top-padding',
								tooltip: 'Tags',
								scale: 'large',
								margin: '0 10 0 0',
								handler: function(){	
									if (Ext.getCmp('tagPanel').isHidden()){
										Ext.getCmp('tagPanel').setHidden(false);
									} else {
										Ext.getCmp('tagPanel').setHidden(true);
									}
								}
							},
							{
								xtype: 'button',
								iconCls: 'fa fa-2x fa-binoculars icon-top-padding',
								id: 'watchBtn',
								tooltip: 'Watch',
								scale: 'large',
								margin: '0 10 0 0',
								handler: function(){
									var watch = {
											componentId: componentId,
											lastViewDts: new Date(),
											username: '${user}',
											notifyFlg: false
									};
									Ext.Ajax.request({
										url: '../api/v1/resource/userprofiles/' +'${user}'+ '/watches',
										method: 'POST',
										jsonData: watch,
										success: function(response, opts){
											currentWatch = Ext.decode(response.responseText);
											Ext.getCmp('watchBtn').setHidden(true);
											Ext.getCmp('watchRemoveBtn').setHidden(false);
										}
									});
								}
							},
							{
								xtype: 'button',								
								text: '<span class="fa-stack" style="margin-left: -10px;margin-right: -10px;"><i class="fa fa-binoculars fa-stack-1x"></i><i class="fa fa-ban fa-stack-2x text-danger"></i></span>',
								id: 'watchRemoveBtn',
								tooltip: 'Remove Watch',
								scale: 'large',								
								margin: '0 10 0 0',
								hidden: true,
								handler: function(){
									var watchId = currentWatch.userWatchId ? currentWatch.userWatchId : currentWatch.watchId;
									Ext.Ajax.request({
										url: '../api/v1/resource/userprofiles/' +'${user}'+ '/watches/' + watchId,
										method: 'DELETE',										
										success: function(response, opts){
											Ext.getCmp('watchRemoveBtn').setHidden(true);
											Ext.getCmp('watchBtn').setHidden(false);
										}
									})
								}
							},							
							{
								xtype: 'button',
								iconCls: 'fa fa-2x fa-print icon-top-padding',
								tooltip: 'Print',
								scale: 'large',
								margin: '0 10 0 0',
								handler: function(){	
									var printWin = window.open('print.jsp?id=' + componentId, 'printwin', 'toolbar=no, location=no, directories=no, status=no, menubar=no, scrollbars=yes, resizable=yes, width=840, height=840');
									if (!printWin) {
										printWin = window.open('print.jsp?id=' + componentId, 'printwin');
									}
								}
							},
							{
								xtype: 'button',
								iconCls: 'fa fa-2x fa-arrows-alt icon-top-padding',
								tooltip: 'Full Page',
								hidden: fullPage,
								scale: 'large',
								margin: '0 10 0 0',
								href: 'view.jsp?id=' + componentId + '&fullPage=true',
								hrefTarget: '_blank'
							},
							{
								xtype: 'button',								
								id: 'nonOwnerMenu',
								iconCls: 'fa fa-2x fa-navicon icon-top-padding',								
								scale: 'large',								
								arrowVisible: false,
								margin: '0 10 0 0',
								menu: {
									items: [										
										{
											text: 'Submit Correction',
											iconCls: 'fa fa-comment-o',
											handler: function() {
												var feedbackWin = Ext.create('OSF.component.FeedbackWindow', {
													closeAction: 'destroy',
													title: 'Submit Correction',
													extraDescription: 'Entry Name: ' + entry.name,
													hideType: 'Correction Requested',
													hideSummary: Ext.String.ellipsis(entry.name, 50),
													labelForDescription: 'Correction: <br>(Please include the section needing the correction. Eg. Contacts)',
													successHandler: function() {														
													}
												});
												feedbackWin.show();
											}
										},
										{	
											xtype: 'menuseparator'
										},
										{
											text: 'Request Ownership',
											iconCls: 'fa fa-envelope-square',
											handler: function() {
												var feedbackWin = Ext.create('OSF.component.FeedbackWindow', {
													closeAction: 'destroy',
													title: 'Request Ownership',
													extraDescription: 'Entry Name: ' + entry.name,												
													hideType: 'Request Ownership',
													hideSummary: Ext.String.ellipsis(entry.name, 50),
													labelForDescription: 'Request Reason: <br>(Entries you own show in the User Tools->Submissions which provides tools for management.',
													successHandler: function() {														
													}
												});
												feedbackWin.show();												
											}
										}										
									]
								}
							}							
						]
					}
				],
				dockedItems: [
					{						
						xtype: 'panel',
						id: 'tagPanel',
						dock: 'bottom',
						bodyStyle: 'padding-left: 10px; padding-right: 10px;',
						hidden: true,
						items: [
							{
								xtype: 'panel',
								layout: 'hbox',
								items: [
									Ext.create('OSF.component.StandardComboBox', {
										name: 'text',	
										id: 'tagField',
										allowBlank: false,									
										margin: '0 0 0 0',
										flex: 1,
										fieldLabel: 'Add Tag',
										forceSelection: false,
										valueField: 'text',
										displayField: 'text',
										margin: '0 10 10 0',
										maxLength: 120,
										storeConfig: {
											url: '../api/v1/resource/components/tags'
										},
										listeners:{
											specialkey: function(field, e) {
												var value = this.getValue();
												if (e.getKey() === e.ENTER && !Ext.isEmpty(value)) {
													actionAddTag(value);
												}	
											}
										}
									}),
									{
										xtype: 'button',
										text: 'Add',
										iconCls: 'fa fa-plus',
										margin: '25 0 0 0',
										minWidth: 75,
										handler: function(){
											var tagField = Ext.getCmp('tagField');
											if (tagField.isValid()) {
												actionAddTag(tagField.getValue());
											}
										}
									}
								]
							}
						]
					}
				]
			});
			
			var actionAddTag = function(tag) {
				
				//add tag
				Ext.getCmp('tagPanel').setLoading('Tagging Entry...');
				Ext.Ajax.request({
					url: '../api/v1/resource/components/' + componentId + '/tags',
					method: 'POST',
					jsonData: {
						text: tag
					},
					callback: function(){
						Ext.getCmp('tagPanel').setLoading(false);
					},
					success: function(response, opt){
						var tag = Ext.decode(response.responseText);
						processTags(tag);
						
						var tagField = Ext.getCmp('tagField');
						tagField.reset();
						tagField.getStore().load();
					}
				});				
			};
			
			var detailPanel = Ext.create('Ext.panel.Panel', {
				id: 'detailPanel',
				title: 'Details',
				bodyStyle: 'padding: 10px;',
				scrollable: true
			});
			
			ViewPage.refreshReviews = function() {
				Ext.getCmp('reviewPanel').setLoading('Refreshing...');
				Ext.Ajax.request({
					url: '../api/v1/resource/components/' + componentId + '/reviews/view',
					callback: function(){
						Ext.getCmp('reviewPanel').setLoading(false);
					}, 						
					success: function(response, opts){
						var reviews = Ext.decode(response.responseText);
						var entryLocal = {};
						entryLocal.reviews = reviews;
						processReviews(entryLocal);							
					}
				});				
			};
			
			ViewPage.reviewWindow = Ext.create('OSF.component.ReviewWindow', {	
				componentId: componentId,
				postHandler: function(reviewWin, response) {
					ViewPage.refreshReviews();
				}
			});			
			
			var reviews = Ext.create('Ext.panel.Panel', {				
				id: 'reviewPanel',		
				title: 'Reviews',
				bodyStyle: 'padding: 10px;',
				scrollable: true,
				layout: {
					type: 'vbox',
					align: 'stretch'
				},
				dockedItems: [
					{
						xtype: 'button',
						text: 'Write a Review',
						maxWidth: 200,
						scale: 'medium',
						margin: 10,
						iconCls: 'fa fa-lg fa-star-half-o icon-top-padding-5',
						handler: function(){							
							ViewPage.reviewWindow.refresh();
							ViewPage.reviewWindow.show();
						}
					}
				],				
				items: [
					{
						xtype: 'panel',
						itemId: 'summary',
						title: 'Review Summary',
						titleCollapse: true,
						collapsible: true,
						hidden: true,
						margin: '0 0 1 0',
						bodyStyle: 'padding: 10px;',
						tpl: new Ext.XTemplate(
							'<table style="width:100%"><tr>',
							'	<td valign="top">',
							'		<tpl if="totalReviews && totalReviews &gt; 0">',
							'		    <div class="review-summary-rating">Average Rating: <tpl for="averageRatingStars"><i class="fa fa-{star} rating-star-color"></i></tpl></div>',							
							'			<b>{recommended} out of {totalReviews} ({[Math.round((values.recommended/values.totalReviews)*100)]}%)</b> reviewers recommended',
							'		</tpl>',
							'   <td>',
							'	<td valign="top" width="20%">',
							'		<tpl if="pros.length &gt; 0">',
							'			<div class="review-pro-con-header">Pros</div>',
							'			<tpl for="pros">',
							'				- {text} <span class="review-summary-count">({count})</span><br>',	
							'			</tpl>',
							'		</tpl>',
							'   <td>',
							'	<td valign="top" width="20%">',
							'		<tpl if="cons.length &gt; 0">',
							'			<div class="review-pro-con-header">Cons</div>',							
							'			<tpl for="cons">',
							'				- {text} <span class="review-summary-count">({count})</span><br>',	
							'			</tpl>',
							'		</tpl>',
							'   <td>',
							'</tr></table>'
						)						
					},
					{
						xtype: 'panel',
						itemId: 'reviews',
						title: 'User Reviews',
						hidden: true,						
						titleCollapse: true,
						collapsible: true,
						bodyStyle: 'padding: 10px;',
						tpl: new Ext.XTemplate(
							'<tpl for=".">',	
							'<table style="width:100%"><tr>',
							'	<td valign="top">',
							'		<h1>{title} <br> <tpl for="ratingStars"><i class="fa fa-{star} rating-star-color"></i></tpl></h1>',								
							'		<div class="review-who-section">{username} ({userTypeCode}) - {[Ext.util.Format.date(values.updateDate, "m/d/y")]}<tpl if="recommend"> - <b>Recommend</b></tpl>', 
							'		<tpl if="owner"><i class="fa fa-edit small-button-normal" title="Edit" onclick="ViewPage.editReview(\'{reviewId}\')"> Edit</i> <i class="fa fa-trash-o small-button-danger" title="Remove" onclick="ViewPage.deleteReview(\'{reviewId}\', \'{componentId}\')"> Remove</i></tpl>',			
							'		</div><br>',
							'		<b>Organization:</b> {organization}<br>',
							'		<b>Experience:</b> {userTimeDescription}<br>',							
							'		<b>Last Used:</b> {[Ext.util.Format.date(values.lastUsed, "m/Y")]}<br>',
							'   <td>',
							'	<td valign="top" width="20%">',
							'		<tpl if="pros.length &gt; 0">',									
							'		<div class="review-pro-con-header">Pros</div>',
							'		<tpl for="pros">',
							'			- {text}<br>',	
							'		</tpl></tpl>',
							'   <td>',
							'	<td valign="top" width="20%">',
							'		<tpl if="cons.length &gt; 0">',
							'		<div class="review-pro-con-header">Cons</div>',
							'		<tpl for="cons">',
							'			- {text}<br>',	
							'		</tpl></tpl>',
							'   <td>',
							'</tr></table>',
							'<br><b>Comments:</b><br>{comment}',
							' <br><br><hr>',
							'</tpl>'
						)						
					}
				]
			});
			
			ViewPage.refreshQuestions = function(){
				Ext.getCmp('questionPanel').setLoading('Refreshing...');
				Ext.Ajax.request({
					url: '../api/v1/resource/components/' + componentId + '/questions/view',
					callback: function(){
						Ext.getCmp('questionPanel').setLoading(false);
					}, 						
					success: function(response, opts){
						var questions = Ext.decode(response.responseText);
						var entryLocal = {};
						entryLocal.questions = questions;
						processQuestions(entryLocal);							
					}
				});
			};
			
			ViewPage.questionWindow = Ext.create('OSF.component.QuestionWindow', {
				componentId: componentId,
				postHandler: function(questionWin, response) {
					ViewPage.refreshQuestions();
				}				
			});		
			
			ViewPage.responseWindow = Ext.create('OSF.component.ResponseWindow', {
				componentId: componentId,
				postHandler: function(responseWin, response) {
					ViewPage.refreshQuestions();
				}
			});			
			
			var questionPanel = Ext.create('Ext.panel.Panel', {
				title: 'Questions & Answers',
				id: 'questionPanel',
				bodyStyle: 'padding: 10px;',
				scrollable: true,
				layout: {
					type: 'vbox',
					align: 'stretch'
				},
				dockedItems: [
					{
						xtype: 'button',
						text: 'Ask a Question',
						maxWidth: 200,
						scale: 'medium',
						margin: 10,
						iconCls: 'fa  fa-lg fa-comment icon-top-padding-5',
						handler: function(){							
							ViewPage.questionWindow.show();
							ViewPage.questionWindow.refresh();
						}
					}
				]
				
			});			
			
			var contentPanel = Ext.create('Ext.panel.Panel', {
				region: 'center',
				bodyStyle: 'background: white; padding: 5px;',
				layout: 'border',
				items: [				
					{
						region: 'center',
						xtype: 'tabpanel',						
						tabBar: {
							defaults: {
								width: '33%'
							},
							dock: 'top',
							layout: {
								pack: 'left'
							}
						},						
						items: [
							
							detailPanel,
							reviews,
							questionPanel
						]
					}
				]
			});			
			
			Ext.create('Ext.container.Viewport', {
				layout: 'border',
				items: [						
					headerPanel,
					contentPanel
				]
			});
			
			var entry;
			var componentTypeDetail;
			var loadDetails = function(){
				if (componentId) {
					headerPanel.setLoading(true);
					contentPanel.setLoading(true);
					Ext.Ajax.request({
						url: '../api/v1/resource/components/' + componentId + '/detail',
						callback: function(){
							headerPanel.setLoading(false);							
						},
						success: function(response, opts) {
							entry = Ext.decode(response.responseText);
							
							Ext.getCmp('titlePanel').update(entry);
							Ext.defer(function(){
								headerPanel.updateLayout(true, true);
							}, 1000);
														
							if (entry.createUser === '${user}'){
								Ext.getCmp('nonOwnerMenu').setHidden(true);
							}
							
							Ext.getCmp('toolsPanel').getComponent('updatedInfo').update(entry);
							
							if (entry.approvalState !== "A") {
								headerPanel.addDocked({
									xtype: 'panel',
									dock: 'top',
									bodyCls: 'alert-warning',
									bodyStyle: 'padding: 20px; font-size: 20px; text-align: center;',
									html: 'This entry has not yet been approved for the site and is still under review.'
								});
								Ext.getCmp('watchBtn').setHidden(true);
								Ext.getCmp('watchRemoveBtn').setHidden(true);
							} else {
								//check for watch
								if (currentWatch) {
									if (entry.lastActivityDts > currentWatch.lastViewDts) {
									headerPanel.addDocked({
										xtype: 'panel',
										dock: 'top',
										bodyCls: 'alert-warning',
										bodyStyle: 'padding: 20px; font-size: 20px; text-align: center;',
										html: 'This entry has recently been updated.'
									});										
									}
								}
								
							}
							
							//get component type and determine review & q&a
							Ext.Ajax.request({
								url: '../api/v1/resource/componenttypes/' + entry.componentType,								
								success: function(response, opts) {
									componentTypeDetail = Ext.decode(response.responseText);
									
									if (componentTypeDetail.dataEntryReviews) {
										processReviews(entry);
									} else {
										Ext.getCmp('reviewPanel').close();
									}
									if (componentTypeDetail.dataEntryQuestions) {
										processQuestions(entry);
									} else {
										Ext.getCmp('questionPanel').close();
									}
									processTags(entry.tags);
									
									var templateUrl;
									if (componentTypeDetail.componentTypeTemplate) {
										//load custom										
										templateUrl= '../api/v1/resource/componenttypetemplates/' + componentTypeDetail.componentTypeTemplate + '/template';
									} else if (entry.componentType === 'ARTICLE') {										
										templateUrl= 'Router.action?page=template/article.jsp';
									} else {
										templateUrl= 'Router.action?page=template/standard.jsp';
									}
									
									
									//populate detail via template
									Ext.Ajax.request({
										url: templateUrl,
										callback: function(){
											contentPanel.setLoading(false);
										},										
										success: function(response, opt) {
											var text = response.responseText;											
											Ext.dom.Element.get("templateHolder").setHtml(text, true, function(){
												template.refresh(Ext.getCmp('detailPanel'), entry);
											});
										}
									});
									
									
								}
							});
							
							
							
														
							
						}
					});
				}
			};
			
			var currentWatch;			
			var loadWatches = function(){
				Ext.Ajax.request({
					url: '../api/v1/resource/userprofiles/' + '${user}' + '/watches',
					success: function(response, opts) {
						var watches = Ext.decode(response.responseText);
						Ext.Array.each(watches, function(watch){
							if (watch.componentId === componentId) {
								currentWatch = watch;
							}
						});
						if (currentWatch) {
							Ext.getCmp('watchBtn').setHidden(true);
							Ext.getCmp('watchRemoveBtn').setHidden(false);							
						}
						
						loadDetails();
					}
				});
			};
			loadWatches();
			
			
			var processTags = function(tagList){
				
				var tags = [];
				Ext.Array.each(tagList,  function(tag){
					var tagButton;
					
					if (tag.createUser === '${user}') {
						tagButton = Ext.create('Ext.button.Button', {
							text: tag.text,
							entryTag: tag,
							iconCls: 'fa fa-close',
							iconAlign: 'right',
							margin: '0 10 0 0',
							handler: function(){
								var tagButton = this;
								var tag = this.entryTag;
								Ext.getCmp('tagPanel').setLoading('Removing Tag...');
								Ext.Ajax.request({
									url: '../api/v1/resource/components/' + componentId + '/tags/' + tag.tagId,
									method: 'DELETE',
									callback: function(){
										Ext.getCmp('tagPanel').setLoading(false);
									},
									success: function(response, opt){
										Ext.getCmp('tagPanel').remove(tagButton, true);
									}
								});								
							}
						});
					} else {
						tagButton = Ext.create('Ext.button.Button', {
							text: tag.text,							
							margin: '0 10 0 0'							
						});						
					}
					
					tags.push(tagButton);
				});
			
				Ext.getCmp('tagPanel').add(tags);
				
			};
			
			var processReviews = function(entryLocal) {
				
				//gather summary
				var summaryData = {					
					totalRatings: 0,
					averageRatingStars: [],
					pros: [],
					cons: [],
					totalReviews: entryLocal.reviews.length,
					recommended: 0
				};
				
				Ext.Array.each(entryLocal.reviews, function(review){
					summaryData.totalRatings += review.rating;
					if (review.recommend) {					
						summaryData.recommended++;
					}
					
					Ext.Array.each(review.pros, function(pro) {
						var found = false;
					
						Ext.Array.each(summaryData.pros, function(sumpro){
							if (sumpro.text === pro.text) {
								sumpro.count++;
								found = true;
							}
						});
						
						if (!found) {
							summaryData.pros.push({
								text: pro.text,
								count: 1
							});
						}
					});
					
					Ext.Array.each(review.cons, function(con) {
						var found = false;
					
						Ext.Array.each(summaryData.cons, function(sumpro){
							if (sumpro.text === con.text) {
								sumpro.count++;
								found = true;
							}
						});
						
						if (!found) {
							summaryData.cons.push({
								text: con.text,
								count: 1
							});
						}
					});	
					
					Ext.Array.sort(review.pros, function(a, b){
						return a.text.localeCompare(b.text);	
					});
					Ext.Array.sort(review.cons, function(a, b){
						return a.text.localeCompare(b.text);	
					});	
					
					review.ratingStars = [];
					for (var i=0; i<5; i++){					
						review.ratingStars.push({						
							star: i < review.rating ? (review.rating - i) > 0 && (review.rating - i) < 1 ? 'star-half-o' : 'star' : 'star-o'
						});
					}
					
					if (review.username === '${user}' || ${admin}) {
						review.owner = true;
					}
					
				});
				ViewPage.reviews = entryLocal.reviews;
				
				var reviewPanelReviews = Ext.getCmp('reviewPanel').getComponent('reviews');
				var reviewPanelSummary = Ext.getCmp('reviewPanel').getComponent('summary');
				
				Ext.Array.sort(summaryData.pros, function(a, b){
					return a.text.localeCompare(b.text);	
				});
				Ext.Array.sort(summaryData.cons, function(a, b){
					return a.text.localeCompare(b.text);	
				});				
				var averageRating = Math.round((summaryData.totalRatings / summaryData.totalReviews)* 10) / 10;
				summaryData.averageRating = averageRating;
				for (var i=0; i<5; i++){					
					summaryData.averageRatingStars.push({						
						star: i <= averageRating ? (averageRating - i) > 0 && (averageRating - i) < 1 ? 'star-half-o' : 'star' : 'star-o'
					});
				}
				
				if (entryLocal.reviews.length > 0) {
					reviewPanelSummary.setHidden(false);
					reviewPanelSummary.update(summaryData);
					reviewPanelReviews.setHidden(false);
					reviewPanelReviews.update(entryLocal.reviews);					
				}
				
				
			};
		
			var processQuestions = function(entryLocal) {
				
				var questionPanels = [];
				ViewPage.questions = entryLocal.questions;
				Ext.Array.each(entryLocal.questions, function(question){
					
					var text = '<div class="question-question"><span class="question-response-letter-q">Q.</span> '+ question.question + '</div>';
					text += '<div class="question-info">' +
							question.username + ' (' + question.userType + ') - ' + Ext.util.Format.date(question.questionUpdateDts, "m/d/Y") +
							'</div>';
					
					Ext.Array.each(question.responses, function(response){
						response.questionId = question.questionId;
						response.componentId = question.componentId;
					});
					
				
					var panel = Ext.create('Ext.panel.Panel', {
						titleCollapse: true,
						collapsible: true,
						title: text,
						bodyStyle: 'padding: 10px;',
						data: question.responses,
						tpl: new Ext.XTemplate(							
							'<tpl for=".">',
							'	<tpl if="activeStatus === \'A\'">',
							'		<div class="question-response"><span class="question-response-letter">A.</span> {response}</div>',
							'		<tpl if="username === \'${user}\' || ${admin}"><i class="fa fa-edit small-button-normal" title="Edit" onclick="ViewPage.editResponse(\'{responseId}\')"> Edit</i> <i class="fa fa-trash-o small-button-danger" title="Remove" onclick="ViewPage.deleteResponse(\'{responseId}\', \'{questionId}\', \'{componentId}\')"> Remove</i></tpl>',
							'		<div class="question-info">{username} ({userType}) - {[Ext.util.Format.date(values.answeredDate, "m/d/Y")]}</div><br>',	
							'		<hr>',
							'	</tpl>',
							'</tpl>'
						),
						dockedItems: [
							{
								xtype: 'button',
								dock: 'bottom',
								text: 'Answer',
								maxWidth: 150,
								scale: 'medium',								
								margin: 10,
								iconCls: 'fa  fa-lg fa-comments-o icon-top-padding-5',
								handler: function(){
									ViewPage.responseWindow.questionId = question.questionId;
									ViewPage.responseWindow.show();
									ViewPage.responseWindow.refresh();
								}
							}
						]				
					});
					if (question.username === '${user}' || ${admin}) {
						panel.addDocked(
							{
								xtype: 'toolbar',
								dock: 'top',								
								items: [
									{
										text: 'Edit',
										tooltip: 'Edit Question',
										iconCls: 'fa fa-edit',
										handler: function(){
											ViewPage.questionWindow.show();
											
											var record = Ext.create('Ext.data.Model');
											record.set(question);											
											ViewPage.questionWindow.edit(record);
										}
									},
									{	
										text: 'Delete',
										tooltip: 'Delete Question',
										iconCls: 'fa fa-trash-o',
										handler: function(){
											Ext.Msg.show({
												title:'Remove Question?',
												message: 'Are you sure you want to review this Question?',
												buttons: Ext.Msg.YESNO,
												icon: Ext.Msg.QUESTION,
												fn: function(btn) {
													if (btn === 'yes') {
														Ext.getCmp('questionPanel').setLoading("Removing...");
														Ext.Ajax.request({
															url: '../api/v1/resource/components/' + componentId + '/questions/' + question.questionId,
															method: 'DELETE',
															callback: function(){
																Ext.getCmp('questionPanel').setLoading(false);
															},
															success: function(){
																ViewPage.refreshQuestions();
															}
														});
													} 
												}
											});
										}										
									}
								]
							}
						);
					}											
					
					questionPanels.push(panel);				
					
				});
				Ext.getCmp('questionPanel').removeAll();
				Ext.getCmp('questionPanel').add(questionPanels);
		
			};
		
		});
		
	</script>	
		
    </stripes:layout-component>
</stripes:layout-render>
