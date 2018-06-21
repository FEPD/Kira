Ext.define('Kira.view.client.PoolServiceMethodCalledInfo', {
	extend : 'Ext.window.Window',
	alias : 'widget.poolservicemethodcalledinfo',
	title : 'Pool的服务方法被调用信息',
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
			store : Ext.getStore('poolServiceMethodCalledInfoStore'),
			autoShow : true,
			scroll : true,
			viewConfig : {
				enableTextSelection : true
			},
			width : 720,
			height : 200,
			columns :[{
				text : 'AppId',
				dataIndex : 'appId',
				width : 200
			},{
				text : '定时任务Id',
				dataIndex : 'triggerId',
				width : 200
			},{
				text : '版本号',
				dataIndex : 'version',
				width : 50
			},{
				text : '描述',
				dataIndex : 'description',
				width : 600
			}],
			dockedItems :[{
				xtype: 'pagingtoolbar',
                store: Ext.getStore('poolServiceMethodCalledInfoStore'),// same store GridPanel is using
                dock: 'bottom', //分页 位置
                emptyMsg: '没有数据',
                displayInfo: true,
                displayMsg: '当前显示{0}-{1}条记录 / 共{2}条记录 ',
                beforePageText: '第',
                afterPageText: '页/共{0}页'
			}]
		});
		Ext.getStore('poolServiceMethodCalledInfoStore').loadPage(1);//加载首页数据
		this.add(grid);
		this.flushView();
	}
});