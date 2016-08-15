/* 
 * Copyright 2015 Space Dynamics Laboratory - Utah State University Research Foundation.
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

/* global Ext, URL */

var CoreUtil = {	
	showContextMenu: function (menu, event) {

		event.stopEvent();
		menu.showAt(0, 0);

		var avaliableHeight = Ext.getBody().getHeight();
		var avaliableWidth = Ext.getBody().getWidth();

		var maxHeight = avaliableHeight - menu.getHeight();
		var maxWidth = avaliableWidth - menu.getWidth();

		var showHeight = event.getY() + menu.getHeight();
		var showWidth = event.getX() + menu.getWidth();

		var x = event.getX();
		var y = event.getY();
		if (showHeight > maxHeight)
		{
			y = event.getY() - menu.getHeight();
		}

		if (showWidth > maxWidth)
		{
			x = event.getX() - menu.getWidth();
		}
		menu.showAt(x, y);
	},
	lookupStore: function (options) {

		var model = 'LookupDataModel';
		if (options.model)
		{
			model = options.model;
		}

		var autoLoad = true;
		if (options.autoLoad !== undefined && options.autoLoad !== null)
		{
			autoLoad = options.autoLoad;
		}

		var store;
		if (options.customStore) {
			store = Ext.create('Ext.data.Store', options.customStore);
		} else {
			store = Ext.create('Ext.data.Store', Ext.applyIf({
				model: model,
				autoLoad: autoLoad,
				proxy: {
					type: 'ajax',
					url: options.url
				}
			}, options));
			if (options.addRecords) {
				store.on('load', function (myStore, records, sucessful, opts) {
					myStore.add(options.addRecords);
				});
			}
		}

		return store;
	},
	downloadCSVFile: function (filename, csvContent) {
		var charset = "utf-8";
		var blob = new Blob([csvContent], {
			type: "text/csv;charset=" + charset + ";"
		});
		if (window.navigator.msSaveOrOpenBlob) {
			//console.log("Save Blob");
			window.navigator.msSaveBlob(blob, filename);
		} else {
			//console.log("Create Link");
			var link = document.createElement("a");
			if (link.download !== undefined) { // feature detection
				// Browsers that support HTML5 download attribute
				var url = URL.createObjectURL(blob);
				link.setAttribute("href", url);
				link.setAttribute("download", filename);
				link.style.visibility = 'hidden';
				document.body.appendChild(link);
				link.click();
				document.body.removeChild(link);
			}
		}
	},
	csvToHTML: function (csvData) {

		var lines = csvData.split("\n");
		var rows = [];
		var maxCols = 0;
		var htmlData = '';
		var foundHeaderFlag = 0;
		var lengthCheck = 3;
		var lengthCheckFlag = 0;

        var csv= Ext.util.CSV.decode(csvData);
		//console.log("CSV:",csv);
		for (ctr=0; ctr<csv.length; ctr++){
			if(csv[ctr][0] === '' && csv[ctr].length === 1)
			{
			  csv.splice(ctr,1);
			  continue;
			}
			if(csv[ctr].length>maxCols){
				maxCols = csv[ctr].length;
			}
		}

		htmlData += '<html><head><style>' +
				' .reportview-table {border-collapse: collapse; border: 2px black solid; font: 12px sans-serif} ' +
				' tr.reportview-table:nth-child(odd) { background: white;} ' +
				' tr.reportview-table:nth-child(even) { background: whitesmoke;} ' +
				' .reportview-td{border: 1px black solid; padding:5px;}' +
				' .reportview-th{padding:5px;}' +
				'</style>'+
				'</head><body><table class="reportview-table">';

		for (ctr = 0; ctr < csv.length; ctr++) {
			for (ctr2 = 0; ctr2 < csv[ctr].length; ctr2++) {
				if(typeof csv[ctr][ctr2]=== 'undefined'){
					csv[ctr][ctr2] = '&nbsp;';
				}
				
				var colDiff = maxCols-ctr2;
				if (ctr2 === 0) { //Start new row

					if ((ctr2 + 1) === csv[ctr].length) {
						if(colDiff !==0 ){
							htmlData += '<tr class="reportview-table"><td class="reportview-td" colspan="'+colDiff+'">' + csv[ctr][ctr2] + '</td></tr>';
						}
						else{
							htmlData += '<tr class="reportview-table"><td class="reportview-td" >' + csv[ctr][ctr2] + '</td></tr>';
						}
					}
					else{
						htmlData += '<tr class="reportview-table"><td class="reportview-td" >' + csv[ctr][ctr2] + '</td>';
					}


				}

				if ((ctr2 + 1) === csv[ctr].length) { //End row
					if (ctr2 !== 0) {
						if(colDiff !==0 ){
							htmlData += '<td class="reportview-td"  colspan="'+colDiff+'">' + csv[ctr][ctr2] + '</td></tr>';
						}
						else{
							htmlData += '<td class="reportview-td" >' + csv[ctr][ctr2] + '</td></tr>';
						}
					}
				}
				else if (ctr2 !== 0) { //Normal Data

					htmlData += '<td class="reportview-td" >' + csv[ctr][ctr2] + '</td>';
				}
			}
		}
		htmlData += '</table></body></html>';
		htmlData = htmlData.replace(/<td class="reportview-td" ><\/td>/g, '<td>&nbsp</td>');
        //console.log("htmlData",htmlData);
		return htmlData;
	},
	popupMessage: function (title, message, delay) {
		if (delay)
		{
			delay = 1000;
		}

		//'t' top and 'b' , 'r' 'l' (See the Fx class overview for valid anchor point options)
		if (!CoreUtil.popupMessageMsgCt) {
			CoreUtil.popupMessageMsgCt = Ext.core.DomHelper.insertFirst(document.body, {id: 'msg-div'}, true);
		}
		var m = Ext.core.DomHelper.append(CoreUtil.popupMessageMsgCt, '<div class="popup-message"><h3>' + title + '</h3><p>' + message + '</p></div>', true);
		m.hide();
		m.slideIn('t').ghost('t', {delay: delay, remove: true});
	},
	/**
	 * Chain load the store and then the form...so the form loads correctly
	 * 
	 * options: 
	 * 
	 * Stores (Array of stores)
	 * Form (form to load)
	 * formLoadOpts
	 * Components to mask 
	 *  @param options
	 */
	chainedStoreLoad: function (options) {

		//mask componets
		if (options.maskComponents)
		{
			if (Ext.isArray(options.maskComponents))
			{
				for (var i = 0; i < options.maskComponents.length; i++) {
					options.maskComponents[i].setLoading(true);
				}
			}
			else
			{
				options.maskComponents.setLoading(true);
			}
		}

		var loadFormFunc = function () {
			var options = this;
			if (options.maskComponents)
			{
				if (Ext.isArray(options.maskComponents))
				{
					for (var i = 0; i < options.maskComponents.length; i++) {
						options.maskComponents[i].setLoading(false);
					}
				}
				else
				{
					options.maskComponents.setLoading(false);
				}
			}

			options.form.load(options.formLoadOpts);
		};

		//chain the stores
		if (Ext.isArray(options.stores))
		{
			for (var i = 0; i < options.stores.length; i++) {

				if (i === (options.stores.length - 1)) {
					options.stores[i].on('load', loadFormFunc, options);
				} else {
					options.stores[i].on('load', function () {
						this.load();
					}, options.stores[i + 1]);
				}

			}
		}
		else
		{
			options.stores.on('load', loadFormFunc, options);
		}
	},
	cloneStoreRecords: function (originalStore)
	{
		var newStore = Ext.create('Ext.data.Store', {
			model: originalStore.model,
			fields: originalStore.fields
		});

		originalStore.each(function (record) {
			newStore.add(record);
		});

		return newStore;
	},
	sessionStorage: function () {
		if (sessionStorage) {
			return sessionStorage;
		} else {
			CoreUtil.popupMessage('Session Storage', 'Not Supported');
			return null;
		}
	},
	localStorage: function () {
		if (localStorage) {
			return localStorage;
		} else {
			CoreUtil.popupMessage('Local Storage', 'Not Supported');
			return null;
		}
	},
	removeBlankDataItem: function (originalData) {
		Ext.Object.each(originalData, function (key, value, data) {
			if (value === "") {
				delete data[key];
			}
		});
	},
	submitForm: function (options) {

		if (options.removeBlankDataItems) {
			Ext.Object.each(options.data, function (key, value, data) {
				if (value === "") {
					delete data[key];
				}
			});
		}

		var failurehandler = function (response, opts) {
			options.form.setLoading(false);

			var errorResponse = Ext.decode(response.responseText);
			var errorObj = {};
			Ext.Array.each(errorResponse.errors.entry, function (item, index, entry) {
				errorObj[item.key] = item.value;
			});
			options.form.getForm().markInvalid(errorObj);
			if (options.failure) {
				options.failure(response, opts);
			}
		};

		var loadingText = options.loadingText ? options.loadingText : 'Saving...';

		options.form.setLoading(loadingText);
		Ext.Ajax.request({
			url: options.url,
			method: options.method,
			jsonData: options.data,
			callback: options.callback,
			success: function (response, opts) {
				options.form.setLoading(false);
				if (response) {
					if (response.status === 304){
						options.success(response, opts);
					} else {
						if (response.responseText) {
							var data = Ext.decode(response.responseText);
							if ((data.success !== undefined && data.success !== null && data.success) ||
									data.success === undefined)
							{
								options.success(response, opts);
							} else {
								failurehandler(response, opts);
							}
						} else {
							//no response (assume success)
							options.success(response, opts);
						}
					}
				}
			},
			failure: function (response, opts) {
				failurehandler(response, opts);
			}
		});

	},
	pagingProxy: function (options) {
		var proxy = Ext.create('Ext.data.proxy.Ajax', {
			url: options.url,
			actionMethods: options.actionMethods ? options.actionMethods : {create: 'POST', read: 'GET', update: 'POST', destroy: 'POST'},
			params: options.params ? options.params : {},
			extraParams: options.extraParams ? options.extraParams : {},
			directionParam: 'sortOrder',
			sortParam: 'sortField',
			startParam: 'offset',
			limitParam: 'max',
			simpleSortMode: true,
			reader: options.reader ? options.reader : {type: 'json'}
		});

		return proxy;
	},
	/**
	 *  Return predfined configs
	 * @param {type} type (optional)
	 * @returns {CoreUtil.tinymceConfig.defaultConfig}
	 */
	tinymceConfig: function(type) {
		var defaultConfig = {
			plugins: [
			"advlist autolink lists link image charmap print preview hr anchor pagebreak",
			"searchreplace wordcount visualblocks visualchars code fullscreen",
			"insertdatetime media nonbreaking save table contextmenu directionality",
			"emoticons template paste textcolor placeholder"
			],

			toolbar1: "newdocument fullpage | bold italic underline strikethrough | alignleft aligncenter alignright alignjustify | styleselect formatselect fontselect fontsizeselect",
			toolbar2: "cut copy paste | searchreplace | bullist numlist | outdent indent blockquote | undo redo | link unlink anchor image media code | inserttime preview | forecolor backcolor",
			toolbar3: "table | hr removeformat | subscript superscript | charmap emoticons | print fullscreen | ltr rtl | spellchecker | visualchars visualblocks nonbreaking template pagebreak restoredraft",

			content_css : "contents.css",

			menubar: true,
			toolbar_items_size: 'small'				
		};
		
		return defaultConfig;
	},
	/**
	 *  Return a config which includes the tinymce plugin/tools for the insertion of saved search links
	 * @param {type} type (optional)
	 * @returns {CoreUtil.tinymceConfig.searchEntryConfig}
	 */
	tinymceSearchEntryConfig: function(type) {
		var searchEntryConfig = {
			plugins: [
			"advlist autolink lists link image charmap print preview hr anchor pagebreak",
			"searchreplace wordcount visualblocks visualchars code fullscreen",
			"insertdatetime media nonbreaking save table contextmenu directionality",
			"emoticons template paste textcolor placeholder savedsearchlink"
			],

			toolbar1: "newdocument fullpage | bold italic underline strikethrough | alignleft aligncenter alignright alignjustify | styleselect formatselect fontselect fontsizeselect",
			toolbar2: "cut copy paste | searchreplace | bullist numlist | outdent indent blockquote | undo redo | link unlink anchor image media code | inserttime preview | forecolor backcolor",
			toolbar3: "table | hr removeformat | subscript superscript | charmap emoticons | print fullscreen | ltr rtl | spellchecker | visualchars visualblocks nonbreaking template pagebreak restoredraft | savedsearchlink",

			content_css : "contents.css",

			menubar: true,
			toolbar_items_size: 'small'				

		};

		return searchEntryConfig;

	},
	/**
	 * Defaults the search to wildcard
	 * @param {string} query 
	 * 
	 */
	searchQueryAdjustment: function(query) {
		if (query && !Ext.isEmpty(query)) {	
			if (query.indexOf('"') === -1) {
				query = "*" + query + "*";
			}
		}
		return query;
	},
	/**
	 * Show related entries
	 * @param {type} attributeType
	 * @param {type} attributeCode
	 * @param {type} description
	 * @param {type} vitalType
	 * @param {type} tip
	 * @param {type} componentId
	 * @param {type} codeHasAttachment
	 * @returns {undefined}
	 */
	showRelatedVitalWindow: function(attributeType, attributeCode, description, vitalType, tip, componentId, codeHasAttachment) {
		
		var relatedStore = Ext.create('Ext.data.Store', {
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
			}),
			listeners: {
				load: function(store, records) {
					store.filterBy(function(record){
						return record.get('componentId') !== componentId;
					});
				}
			}
		});


		var attachmentLinkHTML = '';
		if (codeHasAttachment === 'true') {
			attachmentLinkHTML += '<p style="text-align: center;">' +
				'<i class="fa fa-paperclip"></i> ' +
				'<a href="/openstorefront/api/v1/resource/attributes/attributetypes/{attributeType}' +
				'/attributecodes/{attributeCode}/attachment">' +
				'Download Attachment' +
				'</a></p>';
		}

		console.log(codeHasAttachment);
		console.log(attachmentLinkHTML);

		var relatedWindow = Ext.create('Ext.window.Window', {
			title: 'Related Entries',
			width: '95%',
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
								return '<a class="details-table" href="view.jsp?id=' + record.get('componentId') + '&fullPage=true" target="_blank">' + value + '</a>';
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
								'<h2 style="text-align: center;">{description}</h2>',
								attachmentLinkHTML,
								'<hr>',
								'{tip}'
							)
						}
					]						
				}				
			]			
		});		
		


		relatedWindow.getComponent('grid').getComponent('description').update({
			description: description,
			attributeCode: attributeCode,
			attributeType: attributeType,
			tip: tip
		});

		relatedWindow.show();

		var searchObj = {
			"sortField": "name",
			"sortDirection": "ASC",
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

		var store = relatedWindow.getComponent('grid').getStore();
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
	
	/**
	 * Sort and transfer entry for display
	 * @param {type} entry (componentAll)
	 */
	processEntry: function(entry) {
		
		//sort and process						
		Ext.Array.sort(entry.resources, function(a, b){
			return a.resourceTypeDesc.localeCompare(b.resourceTypeDesc);	
		});	

		Ext.Array.sort(entry.contacts, function(a, b){
			return a.name.localeCompare(b.name);	
		});		

		Ext.Array.sort(entry.dependencies, function(a, b){
			return a.dependencyName.localeCompare(b.dependencyName);	
		});							

		var vitals = [];
		if (entry.attributes) {
			Ext.Array.each(entry.attributes, function(item){
				vitals.push({
					label: item.typeDescription,
					value: item.codeDescription,
					highlightStyle: item.highlightStyle,
					type: item.type,
					code: item.code,
					updateDts: item.updateDts,
					securityMarkingType: item.securityMarkingType,
					tip: item.codeLongDescription ? Ext.util.Format.escape(item.codeLongDescription).replace(/"/g, '') : item.codeLongDescription
				});				
			});
		}

		if (entry.metadata) {
			Ext.Array.each(entry.metadata, function(item){
				vitals.push({
					label: item.label,
					value: item.value,
					securityMarkingType: item.securityMarkingType,
					updateDts: item.updateDts
				});			
			});
		}

		Ext.Array.sort(vitals, function(a, b){
			return a.label.localeCompare(b.label);	
		});
		entry.vitals = vitals;	

		Ext.Array.each(entry.evaluation.evaluationSections, function(section){
			if (section.notAvailable || section.actualScore <= 0) {
				section.display = "N/A";
			} else {
				var score = Math.round(section.actualScore);
				section.display = "";
				for (var i= 0; i<score; i++){
					section.display += '<i class="fa fa-circle detail-evalscore"></i>';
				}
			}				
		});


		Ext.Array.sort(entry.evaluation.evaluationSections, function(a, b){
			return a.name.localeCompare(b.name);	
		});


		Ext.Array.each(entry.reviews, function(review){
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
		});
		
		if (entry.attributes && entry.attributes.length > 0) {
			var evalLevels = {};	
			Ext.Array.each(entry.attributes, function(item){
				if (item.type === 'DI2ELEVEL') {
					evalLevels.level = {};
					evalLevels.level.typeDesciption = item.typeDescription; 
					evalLevels.level.code = item.code; 
					evalLevels.level.label = item.codeDescription; 
					evalLevels.level.description = item.codeLongDescription;
					evalLevels.level.highlightStyle = item.highlightStyle;
					if (item.updateDts > entry.lastViewedDts) {
						updated = true;
					}
				} else if (item.type === 'DI2ESTATE') {
					evalLevels.state = {};
					evalLevels.state.typeDesciption = item.typeDescription; 
					evalLevels.state.code = item.code; 
					evalLevels.state.label = item.codeDescription; 
					evalLevels.state.description = item.codeLongDescription;
					evalLevels.state.highlightStyle = item.highlightStyle;
					if (item.updateDts > entry.lastViewedDts) {
						updated = true;
					}					
				} else if (item.type === 'DI2EINTENT') {
					evalLevels.intent = {};
					evalLevels.intent.typeDesciption = item.typeDescription; 
					evalLevels.intent.code = item.code; 
					evalLevels.intent.label = item.codeDescription; 
					evalLevels.intent.description = item.codeLongDescription; 
					evalLevels.intent.highlightStyle = item.highlightStyle;
					if (item.updateDts > entry.lastViewedDts) {
						updated = true;
					}					
				}
			});	
			entry.evalLevels = evalLevels;	
		}
	
		
		return entry;
	},
	securityBannerPanel: function(branding) {
		
		if (branding && branding.securityBannerText) {
			var securityBanner = Ext.create('Ext.panel.Panel', {
				bodyCls: 'security-banner',
				dock: 'top',
				width: '100%',
				html: branding.securityBannerText
			});
			return securityBanner;
		}
		return null;
	},
	maxFileSize: 1048576000,
	handleMaxFileLimit: function(field, value, opts) {
		var el = field.fileInputEl.dom;
		
		var errorMessage = ' <span style="color: red; font-weight: bold">File exceeded size limit.</span>';				
		field.setFieldLabel(field.getFieldLabel().replace(errorMessage, ''));
		
		if (el.files && el.files.length > 0) {
			var file = el.files[0];
			if (file.size >  CoreUtil.maxFileSize) {
				Ext.defer(function(){
					field.reset();
					field.markInvalid('File exceeds size limit.');
					field.setFieldLabel(field.getFieldLabel() + errorMessage);					
				}, 250);
			}
		}
		
	},
	descriptionOfAdvancedSearch : function(searchElements) {
		if (searchElements) {
			var desc = '';
			var count =0;
			
			Ext.Array.each(searchElements, function(element) {
				
				if (element.searchType) {
					desc += '<b>Search Type: </b>' + element.searchType + '<br>';
				}				
				if (element.field) {
					desc += '<b>Field: </b>' + element.field + '<br>';
				}
				if (element.value) {
					desc += '<b>Value: </b>' + element.value + '<br>';
				}
				if (element.keyField) {
					desc += '<b>Key Field: </b>' + element.keyField + '<br>';
				}
				if (element.keyValue) {
					desc += '<b>Key Value: </b>' + element.keyValue + '<br>';
				}
				if (element.startDate) {
					desc += '<b>Start Date: </b>' + element.startDate + '<br>';
				}
				if (element.endDate) {
					desc += '<b>End Date: </b>' + element.endDate + '<br>';
				}								
				if (element.caseInsensitive) {
					desc += '<b>Case Insensitive: </b>' + element.caseInsensitive + '<br>';
				}
				if (element.numberOperation) {
					desc += '<b>Number Operation: </b>' + element.numberOperation + '<br>';
				}
				if (element.stringOperation) {
					desc += '<b>String Operation: </b>' + element.stringOperation + '<br>';
				}
				
				if (count !== 0) {
					desc += '<br>' + element.mergeCondition + '<br><br>';
				}
				
				count++;
			});
			return desc;
		}
		return '';
	}
};
