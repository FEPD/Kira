Ext.define('Kira.view.task.ManuallyRunJobOnlyOne', {
	extend : 'Ext.window.Window',
	alias : 'widget.manuallyrunjobonlyone',
	title : '手动执行一次调度任务',
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
			store : Ext.getStore('manuallyRunJobOnlyOneStore'),
			autoShow : true,
			scroll : true,
			viewConfig : {
				enableTextSelection : true
			},
			width : 210,
			height : 100,
			columns :[{
				header : '执行时间',
				dataIndex : 'createTimeAsString',
				width : 150
			},{
				header : '是否成功',
				dataIndex : 'resultCode',
				width : 60,
				renderer : function(value){
					if(value=='0'){
						return '成功';
					}else if(value=='1'){
						return '部分成功';
					}else if(value=='2'){
						return '失败';
					}
				}
			}]/*,
			dockedItems :[{
				xtype: 'pagingtoolbar',
                store: Ext.getStore('manuallyRunJobOnlyOneStore'),// same store GridPanel is using
                dock: 'bottom', //分页 位置
                emptyMsg: '没有数据',
                displayInfo: true,
                displayMsg: '当前显示{0}-{1}条记录 / 共{2}条记录 ',
                beforePageText: '第',
                afterPageText: '页/共{0}页'
			}]*/
		});
		Ext.getStore('manuallyRunJobOnlyOneStore').loadPage(1);//加载首页数据
		this.add(grid);
		this.flushView();
	}
});