Ext.define('Kira.view.status.TaskStatus', {
	extend : 'Ext.container.Container',
	alias : 'widget.taskstatus',
	title : '任务状态说明',
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
			store : Ext.getStore('statusStore'),
			autoShow : true,
			scroll : true,
			viewConfig : {
				enableTextSelection : true
			},
			columns :[{
				header : 'ID',
				dataIndex : 'id',
				hidden : true
			},{
				header : '名称',
				dataIndex : 'name',
				width : 150
			}, {
				header : '描述',
				dataIndex : 'description',
				flex : 1
			}],
			dockedItems :[{
				xtype: 'pagingtoolbar',
                store: Ext.getStore('statusStore'),// same store GridPanel is using
                dock: 'bottom', //分页 位置
                emptyMsg: '没有数据',
                displayInfo: true,
                displayMsg: '当前显示{0}-{1}条记录 / 共{2}条记录 ',
                beforePageText: '第',
                afterPageText: '页/共{0}页'
			}]
		});
		Ext.getStore('statusStore').loadPage(1);//加载首页数据
		this.add(grid);
		this.flushView();
	}
});