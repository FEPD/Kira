Ext.define('Kira.view.task.KiraTimerTriggerBusinessRunningInstanceListInfoQuery', {
	extend : 'Ext.window.Window',
	alias : 'widget.kiraTimerTriggerBusinessRunningInstanceListInfoQuery',
	title : '定时任务对应的业务方法当前执行实例列表',
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
			store : Ext.getStore('kiraTimerTriggerBusinessRunningInstanceListInfoQueryStore'),
			autoShow : true,
			scroll : true,
			viewConfig : {
				enableTextSelection : true
			},
			width : 720,
			height : 200,
			columns :[{
				text : 'JobItemId',
				dataIndex : 'jobId',
				width : 230
			},{
				text : '创建时间',
				dataIndex : 'createTimeAsString',
				width : 130
			},{
				text : '主机名',
				dataIndex : 'host',
				width : 100
			},{
				text : '端口',
				dataIndex : 'port',
				width : 50
			},{
				text : '进程号',
				dataIndex : 'pid',
				width : 50
			},{
				text : '服务地址',
				dataIndex : 'serviceUrl',
				width : 400
			},{
				text : '方法参数值',
				dataIndex : 'argumentsAsJsonArrayString',
				width : 300
			}]/*,
			dockedItems :[{
				xtype: 'pagingtoolbar',
                store: Ext.getStore('kiraTimerTriggerBusinessRunningInstanceListInfoQueryStore'),// same store GridPanel is using
                dock: 'bottom', //分页 位置
                emptyMsg: '没有数据',
                displayInfo: true,
                displayMsg: '当前显示{0}-{1}条记录 / 共{2}条记录 ',
                beforePageText: '第',
                afterPageText: '页/共{0}页'
			}] */
		});
		Ext.getStore('kiraTimerTriggerBusinessRunningInstanceListInfoQueryStore').loadPage(1);//加载首页数据
		this.add(grid);
		this.flushView();
	}
});