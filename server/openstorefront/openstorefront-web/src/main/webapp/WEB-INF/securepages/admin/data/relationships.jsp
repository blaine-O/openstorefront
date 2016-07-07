<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<stripes:layout-render name="../../../../client/layout/adminlayout.jsp">
	<stripes:layout-component name="contents">

		<script src="scripts/component/visualSearch.js?v=${appVersion}" type="text/javascript"></script>	
		<script src="scripts/plugin/cellToCellDragDrop.js?v=${appVersion}" type="text/javascript"></script>	
		
		<script type="text/javascript">
			/* global Ext, CoreUtil */
			Ext.onReady(function(){	
				
				var visualPanel = Ext.create('OSF.component.VisualSearchPanel', {
					id: 'visualPanel',
					height: '100%',
					viewType: null
				});

			
				var relationshipsStore = Ext.create('Ext.data.Store', {
					storeId: 'relationshipsStore',
					listeners: {
						load: function() {
							var viewData = [];
								relationshipsStore.each(function(relationship){
									viewData.push({
										type: 'component',
										nodeId: relationship.get('relationshipId'),
										key: relationship.get('ownerComponentId'),
										label: relationship.get('ownerComponentName'),
										relationshipLabel: relationship.get('relationshipTypeDescription'),
										targetKey: relationship.get('targetComponentId'),
										targetName: relationship.get('targetComponentName'),
										targetType: 'component'
									});
								});
								var visPanel = Ext.getCmp('visualPanel');
								visPanel.viewType = null;
								visPanel.reset();
								visPanel.viewData = viewData;
								visPanel.initVisual(visPanel.viewData);
						}
					}
				});

				var relationshipTypeStore = Ext.create('Ext.data.Store', {
					storeId: 'relationshipTypeStore',
					proxy: {
						type: 'ajax',
						url: '../api/v1/resource/lookuptypes/RelationshipType/view'
					},
					autoLoad: true
				});
													   

				var componentsStore = Ext.create('Ext.data.Store', {
					storeId: 'componentsStore',
					fields: [
						// The griddragdrop plugin needs a type in order to operate!
						{
							name: 'description',
							type: 'string'
						}
					],
					sorters: new Ext.util.Sorter({
						property: 'description',
						direction: 'ASC'
					}),
					proxy: {
						id: 'componentsStoreProxy',
						type: 'ajax',
						url: '../api/v1/resource/components/lookup'
					},
					autoLoad: true
				}); 

				var originGrid = Ext.create('Ext.grid.Panel', {
					store: componentsStore,
					flex: 1,
					border: true,
					autoScroll: true,
					viewConfig: {
						plugins: {
							ddGroup: 'relationship',
							ptype: 'celltocelldragdrop',
							enableDrop: false
						}
					},
					columns: [
						{ text: 'Origin Entry', dataIndex: 'description', flex: 1 }
					],
					listeners: {
						select: function(grid, record, index, eOpts) {
							var id = record.get('code');
							relationshipsStore.setProxy({
								type: 'ajax',
								url: '../api/v1/resource/components/' + id + '/relationships'
							});
							relationshipsStore.load();
						}
					}
				});

				var targetGrid = Ext.create('Ext.grid.Panel', {
					store: componentsStore,
					flex: 1,
					border: true,
					selectable: false,
					viewConfig: {
						plugins: [
							Ext.create('OSF.plugin.CellToCellDragDrop', {
								ddGroup: 'relationship',
								enableDrop: true,
								onDrop: function onDrop(target, dd, e, dragData) {
									console.log(target);
									console.log(dragData);
								}
							})
						]
					},
					columns: [
						{ text: 'Target Entry', dataIndex: 'description', flex: 1 }
					],
					listeners: {
						beforeselect: function(grid, record, index, eOpts) {
							originGrid.getView().select(record);
							grid.getSelectionModel().deselectAll();
						},
						select: function(grid, record, index, eOpts) {
							grid.getSelectionModel().deselectAll();
							originGrid.focus();
						}
					}
				});

				var relationshipsGrid = Ext.create('Ext.grid.Panel', {
					region: 'center',
					store: relationshipsStore,
					viewConfig: {
						emptyText: 'Please select a component to view its relationships',
						deferEmptyText: false
					},
					columns: [
						{ text: 'Origin Entry', dataIndex: 'ownerComponentName', flex: 5 },
						{
							text: 'Relationship Type',
							flex: 2,
							xtype: 'widgetcolumn',
							dataIndex: 'relationshipType',
							widget: {
								xtype: 'combo',
								store: relationshipTypeStore,
								displayField: 'description',
								valueField: 'code'
							}
						},
						{ text: 'Target Entry', dataIndex: 'targetComponentName', flex: 5 }
					]
				});

				var visualizationPanel = Ext.create('Ext.panel.Panel', {
					height: '60%',
					region: 'south',
					title: 'Visualization',
					collapsible: true,
					resizable: true,
					items: [
						visualPanel
					]
				});

				var relationshipCreationGridsPanel = Ext.create('Ext.panel.Panel', {
					height: '100%',
					width: '100%',
					layout: {
						type: 'hbox',
						align: 'stretch'	
					},
					items: [
						originGrid, targetGrid
					],
					dockedItems: [
						{
							dock: 'top',
							xtype: 'toolbar',
							layout: {
								pack: 'center'
							},
							items: [
								Ext.create('OSF.component.StandardComboBox', {
									id: 'relationshipTypeComboBox',
									fieldLabel: 'Type of Relationship to Create',
									width: '60%',
									store: 'relationshipTypeStore',
									displayField: 'description',
									valueField: 'code'
								})
							]
						}
					]
				});

				var relationshipsMainLayout = Ext.create('Ext.panel.Panel', {
					title: 'Relationship Management Tool',
					layout: 'border',
					height: '100%',
					items: [
						{
							title: 'Relationship Creation',
							region: 'west',
							xtype: 'panel',
							margin: '5 5 5 5',
							flex: 2,
							id: 'west-container',
							autoScroll: true,
							layout: {
								type: 'hbox'
							},
							items: [
								relationshipCreationGridsPanel
							]
						},
						{
							title: 'Existing Relationships',
							margin: '5 5 5 5',
							borderWidth: '5px',
							region: 'center',
							xtype: 'panel',
							flex: 3,
							id: 'center-container',
							layout: 'border',
							items: [
								relationshipsGrid, visualizationPanel
							]
						}
					]
				});

				Ext.create('Ext.container.Viewport', {
					layout: 'fit',
					items: [
						relationshipsMainLayout
					]
				});

				Ext.defer(function(){
					visualPanel.setWidth(visualizationPanel.getWidth());
					visualPanel.setHeight(visualizationPanel.getHeight()-40);
				}, 400);	




			});
		</script>	
		
	</stripes:layout-component>
</stripes:layout-render>
