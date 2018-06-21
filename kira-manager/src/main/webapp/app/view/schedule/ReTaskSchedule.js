Ext.define('Kira.view.schedule.ReTaskSchedule', {
	extend : 'Ext.window.Window',
	alias : 'widget.retaskschedule',
	title : '重新执行任务调度',
	layout: 'fit',
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
			store : Ext.getStore('reTaskScheduleStore'),
			autoShow : true,
			scroll : true,
			viewConfig : {
				enableTextSelection : true
			},
			width : 560,
			height : 200,
			columns :[{
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
			},{
				header : '相关信息',
				dataIndex : 'resultData',
				width : 500
			}],
			dockedItems :[{
				xtype: 'pagingtoolbar',
                store: Ext.getStore('reTaskScheduleStore'),// same store GridPanel is using
                dock: 'bottom', //分页 位置
                emptyMsg: '没有数据',
                displayInfo: true,
                displayMsg: '当前显示{0}-{1}条记录 / 共{2}条记录 ',
                beforePageText: '第',
                afterPageText: '页/共{0}页'
			}]
		});
		Ext.getStore('reTaskScheduleStore').loadPage(1);//加载首页数据
		this.add(grid);
		this.flushView();
	}
});