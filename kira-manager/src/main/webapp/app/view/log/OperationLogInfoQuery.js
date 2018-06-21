Ext.define('Kira.view.log.OperationLogInfoQuery', {
	extend : 'Ext.container.Container',
	requires : ['Ext.ux.form.field.DateTime'],
	alias : 'widget.operationloginfoquery',
	title : '操作日志信息查询',
	layout: 'fit',
	closable : true,
	initComponent : function() {
		var me = this;
		Ext.applyIf(me, {
					//margin : '0 10 10 10'
				});
		me.callParent(arguments);
	},

	flushView : function() {
		this.doComponentLayout();
	},

	loadView : function() {
		var grid = Ext.create('Ext.grid.Panel', {
			store : Ext.getStore('operationLogInfoQueryStore'),
			autoShow : true,
			scroll : true,
			viewConfig : {
				enableTextSelection : true
			},
			columns :[{
				text : '操作名称',
				dataIndex : 'operationDisplay',
				width : 150
			},{
				text : '操作时间',
				dataIndex : 'operateTimeAsString',
				width : 150
			},{
				text : '操作人',
				dataIndex : 'operatedBy',
				width : 100
			},{
				text : '操作结果',
				dataIndex : 'resultCode',
				width : 60,
				renderer : function(value){
					if(value == 0){
						return '成功';
					}else if(value == 1){
						return '部分成功';
					}else if(value == 2){
						return '失败';
					}
				}
			},{
				text : '结果详细信息',
				dataIndex : 'resultDetails',
				width : 300
			},{
				text : '操作详细信息',
				dataIndex : 'operationDetails',
				flex : 1,
				renderer: function (value, meta, record){
					var max = 15;
					meta.tdAttr = 'data-qtip="' + value + '"';
					return value.length < max ? value : value.substring(0, max - 3) + '...';
				}
			}],
			tbar : ['操作名称:',{
				xtype : 'combo',
				//id : 'operationLogInfoQuery_operationName',
				name : 'operationName',
				width: 120,
				editable : true,
				value : '',
				store : Ext.getStore('operationNameStore'),
				displayField : 'display',
				valueField : 'name',
				matchFieldWidth: false,
				//queryMode: 'remote',
				triggerAction:'all',
				listConfig : {
					loadingText: 'Loading...',
					width: 200,
					autoHeight:true
				}
			},'-','操作人:',{
				xtype : 'textfield',
				name : 'operatedBy',
				width : 120
			},'-','操作时间(from):',{
				xtype : 'datetimefield',
				format : 'Y-m-d H:i:s',
				name : 'operateTimeStart',
				width : 150
			},'-','操作时间(to):',{
				xtype : 'datetimefield',
				format : 'Y-m-d H:i:s',
				name : 'operateTimeEnd',
				width : 150
			},'-',{
				text : '查询',
				iconCls : 'icon-search',
				action : 'select'
			}],
			dockedItems :[{
				xtype: 'pagingtoolbar',
	            store: Ext.getStore('operationLogInfoQueryStore'),// same store GridPanel is using
	            dock: 'bottom', //分页 位置
	            emptyMsg: '没有数据',
	            displayInfo: true,
	            displayMsg: '当前显示{0}-{1}条记录 / 共{2}条记录 ',
	            beforePageText: '第',
	            afterPageText: '页/共{0}页'
			}]
		});
		this.add(grid);
		this.flushView();
	}
});