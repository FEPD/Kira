Ext.define('Kira.view.task.subtask.QueryKiraClientInfo', {
	extend : 'Ext.window.Window',
	alias : 'widget.querykiraclientinfo',
	title : '获取Kira客户端当前信息',
	layout: 'fit',
	closable : true,
	modal : true,
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
			store : Ext.getStore('queryKiraClientInfoStore'),
			autoShow : true,
			scroll : true,
			width : 320,
			height : 150,
			viewConfig : {
				enableTextSelection : true
			},
			columns :[{
				header : 'Key',
				dataIndex : 'key',
				width : 100
			},{
				header : 'Value',
				dataIndex : 'value',
				width : 250
			}]/*,
			dockedItems :[{
				xtype: 'pagingtoolbar',
                store: Ext.getStore('queryKiraClientInfoStore'),// same store GridPanel is using
                dock: 'bottom', //分页 位置
                emptyMsg: '没有数据',
                displayInfo: true,
                displayMsg: '当前显示{0}-{1}条记录 / 共{2}条记录 ',
                beforePageText: '第',
                afterPageText: '页/共{0}页'
			}]*/
		});
		Ext.getStore('queryKiraClientInfoStore').loadPage(1);//加载首页数据
		this.add(grid);
		this.flushView();
	}
});