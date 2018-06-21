Ext.define('Kira.view.task.ExecuteEnvInfoQuery', {
	extend : 'Ext.window.Window',
	alias : 'widget.executeenvinfoquery',
	title : '执行环境信息查询',
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
			store : Ext.getStore('executeEnvInfoQueryStore'),
			autoShow : true,
			scroll : true,
			viewConfig : {
				enableTextSelection : true
			},
			width : 720,
			height : 200,
			columns :[{
				text : '操作',
				xtype : 'actioncolumn',
				width : 50,
				align : 'center',
				id : 'executeEnvInfoQueryIcon',
				icon : 'extjs/resources/images/action.gif'
			},{
				header : '执行环境',
				dataIndex : 'triggerEnvironmentZKFullPath',
				width : 300
			},{
				header : '当前在线服务地址',
				dataIndex : 'serviceUrl',
				width : 300
			},{
				header : '服务是否为可用状态',
				dataIndex : 'serviceAvailable',
				width : 120
			}]/*,
			dockedItems :[{
				xtype: 'pagingtoolbar',
                store: Ext.getStore('executeEnvInfoQueryStore'),// same store GridPanel is using
                dock: 'bottom', //分页 位置
                emptyMsg: '没有数据',
                displayInfo: true,
                displayMsg: '当前显示{0}-{1}条记录 / 共{2}条记录 ',
                beforePageText: '第',
                afterPageText: '页/共{0}页'
			}]*/
		});
		Ext.getStore('executeEnvInfoQueryStore').loadPage(1);//加载首页数据
		this.add(grid);
		this.flushView();
	}
});