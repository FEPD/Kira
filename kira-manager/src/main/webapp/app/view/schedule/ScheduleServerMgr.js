Ext.define('Kira.view.schedule.ScheduleServerMgr', {
	extend : 'Ext.container.Container',
	alias : 'widget.scheduleservermgr',
	title : '调度服务器管理',
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
			store : Ext.getStore('scheduleServerMgrStore'),
			autoShow : true,
			//width : 800,
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
				text : '服务器启动时间',
				dataIndex : 'serverBirthTimeAsString',
				width : 150
			},{
				text : '服务器角色',
				dataIndex : 'serverRoleName',
				width : 100
			}]
		});
		Ext.getStore('scheduleServerMgrStore').loadPage(1);//加载首页数据
		this.add(grid);
		this.flushView();
	}
});