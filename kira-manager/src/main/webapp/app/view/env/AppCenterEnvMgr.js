Ext.define('Kira.view.env.AppCenterEnvMgr', {
	extend : 'Ext.container.Container',
	alias : 'widget.appcenterenvmgr',
	title : '集中执行环境管理',
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
			store : Ext.getStore('appCenterEnvMgrStore'),
			autoShow : true,
			scroll : true,
			viewConfig : {
				enableTextSelection : true
			},
			columns :[{
				text : '主机名',
				dataIndex : 'host',
				width : 150
			},{
				text : '端口',
				dataIndex : 'port',
				width : 50
			},{
				header : '所属AppId',
				dataIndex : 'appId',
				width : 200
			}],
			dockedItems :[{
				xtype: 'pagingtoolbar',
                store: Ext.getStore('appCenterEnvMgrStore'),// same store GridPanel is using
                dock: 'bottom', //分页 位置
                emptyMsg: '没有数据',
                displayInfo: true,
                displayMsg: '当前显示{0}-{1}条记录 / 共{2}条记录 ',
                beforePageText: '第',
                afterPageText: '页/共{0}页'
			}]
		});
		Ext.getStore('appCenterEnvMgrStore').loadPage(1);//加载首页数据
		this.add(grid);
		this.flushView();
	}
});