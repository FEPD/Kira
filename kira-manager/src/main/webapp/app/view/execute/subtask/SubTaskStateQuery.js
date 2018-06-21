Ext.define('Kira.view.execute.subtask.SubTaskStateQuery', {
	extend : 'Ext.window.Window',
	alias : 'widget.subtaskstatequery',
	title : '子任务状态查询',
	layout: 'fit',
	closable : true,
	modal : true,
	//width : 800,
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
			store : Ext.getStore('subTaskStateQueryStore'),
			autoShow : true,
			scroll : true,
			width : 450,
			height : 200,
			viewConfig : {
				enableTextSelection : true
			},
			columns :[{
				header : '创建时间',
				dataIndex : 'createTimeAsString',
				width : 150
			},{
				header : '任务状态',
				dataIndex : 'jobStatusName',
				width : 150
			},{
				header : '任务相关信息',
				dataIndex : 'resultData',
				width : 150
			}],
			dockedItems :[{
				xtype: 'pagingtoolbar',
                store: Ext.getStore('subTaskStateQueryStore'),// same store GridPanel is using
                dock: 'bottom', //分页 位置
                emptyMsg: '没有数据',
                displayInfo: true,
                displayMsg: '当前显示{0}-{1}条记录 / 共{2}条记录 ',
                beforePageText: '第',
                afterPageText: '页/共{0}页'
			}]
		});
		Ext.getStore('subTaskStateQueryStore').loadPage(1);//加载首页数据
		this.add(grid);
		this.flushView();
	}
});