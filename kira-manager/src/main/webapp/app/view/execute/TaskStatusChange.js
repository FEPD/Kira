Ext.define('Kira.view.execute.TaskStatusChange', {
	extend : 'Ext.window.Window',
	alias : 'widget.taskstatuschange',
	title : '状态变化明细',
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
			store : Ext.getStore('taskStatusChangeStore'),
			autoShow : true,
			scroll : true,
			viewConfig : {
				enableTextSelection : true
			},
			width : 250,
			height : 200,
			columns :[{
				text : '状态',
				dataIndex : 'jobStatusName',
				width : 100
			},{
				text : '创建时间',
				dataIndex : 'createTimeAsString',
				width : 150
			}],
			dockedItems :[{
				xtype: 'pagingtoolbar',
                store: Ext.getStore('taskStatusChangeStore'),// same store GridPanel is using
                dock: 'bottom', //分页 位置
                emptyMsg: '没有数据',
                displayInfo: true,
                displayMsg: '当前显示{0}-{1}条记录 / 共{2}条记录 ',
                beforePageText: '第',
                afterPageText: '页/共{0}页'
			}]
		});
		Ext.getStore('taskStatusChangeStore').loadPage(1);//加载首页数据
		this.add(grid);
		this.flushView();
	}
});