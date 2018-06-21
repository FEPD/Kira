Ext.define('Kira.view.execute.TaskExecuteDetail', {
	extend : 'Ext.window.Window',
	alias : 'widget.taskexecutedetail',
	title : '执行明细',
	layout: 'fit',
	/*
	layout : {
	    type : 'hbox',
	    pack:'center',
	    align:'middle'
	},*/
	closable : true,
	modal : true,
	//closeAction : 'hide',//Ext4 window close默认关闭非隐藏，与之前Ext版本相反
	//x : 20,
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
			store : Ext.getStore('taskExecuteDetailStore'),
			width : 770,
			height : 200,
			autoShow : true,
			scroll : true,
			viewConfig : {
				enableTextSelection : true
			},
			columns :[{
				text : '操作',
				xtype : 'actioncolumn',
				width : 50,
				align : 'center',
				id : 'taskExecuteDetailIcon',
				icon : 'extjs/resources/images/action.gif'
			},{
				header : 'JobItemId',
				dataIndex : 'id',
				width : 200
			},{
				header : 'AppId',
				dataIndex : 'appId',
				width : 200
			},{
				header : '定时任务Id',
				dataIndex : 'triggerId',
				width : 120
			},{
				header : '版本号',
				dataIndex : 'version',
				width : 50
			},{
				header : '当前状态',
				dataIndex : 'jobStatusName',
				width : 100
			},{
				header : '任务相关信息',
				dataIndex : 'resultData',
				width : 150
			},{
				header : '任务执行地点',
				dataIndex : 'serviceUrl',
				width : 150
			}],
			dockedItems :[{
				xtype: 'pagingtoolbar',
                store: Ext.getStore('taskExecuteDetailStore'),// same store GridPanel is using
                dock: 'bottom', //分页 位置
                emptyMsg: '没有数据',
                displayInfo: true,
                displayMsg: '当前显示{0}-{1}条记录 / 共{2}条记录 ',
                beforePageText: '第',
                afterPageText: '页/共{0}页'
			}]
		});
		Ext.getStore('taskExecuteDetailStore').loadPage(1);//加载首页数据
		this.add(grid);
		this.flushView();
	}
});