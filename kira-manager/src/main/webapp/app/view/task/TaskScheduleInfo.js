Ext.define('Kira.view.task.TaskScheduleInfo', {
	extend : 'Ext.window.Window',
	alias : 'widget.taskscheduleinfo',
	title : '任务调度信息',
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
			store : Ext.getStore('taskScheduleInfoStore'),
			autoShow : true,
			scroll : true,
			viewConfig : {
				enableTextSelection : true
			},
			width : 960,
			height : 200,
			columns :[{
				text : 'AppId',
				dataIndex : 'appId',
				width : 200
			},{
				text : '定时任务Id',
				dataIndex : 'triggerId',
				width : 100
			}, {
				text : '开始时间',
				dataIndex : 'startTimeAsString',
				width : 150
			},{
				text : '上一次触发时间',
				dataIndex : 'previousFireTimeAsString',
				width : 150
			},{
				text : '下一次触发时间',
				dataIndex : 'nextFireTimeAsString',
				width : 150
			},{
				text : '最后一次触发时间',
				dataIndex : 'finalFireTimeAsString',
				width : 150
			},{
				text : '调度状态',
				dataIndex : 'triggerStateAsString',
				width : 60,
				renderer : function(v){
					if(v=='normal'){
						return '调度中';
					}else if(v=='paused'){
						return '暂停调度';
					}else if(v=='complete'){
						return '调度完成';
					}else if(v=='error'){
						return '调度错误';
					}else if(v=='blocked'){
						return '调度阻塞';
					}else if(v=='none'){
						return '';
					}else{
					    
					}
				}
			}],
			dockedItems :[{
				xtype: 'pagingtoolbar',
                store: Ext.getStore('taskScheduleInfoStore'),// same store GridPanel is using
                dock: 'bottom', //分页 位置
                emptyMsg: '没有数据',
                displayInfo: true,
                displayMsg: '当前显示{0}-{1}条记录 / 共{2}条记录 ',
                beforePageText: '第',
                afterPageText: '页/共{0}页'
			}]
		});
		Ext.getStore('taskScheduleInfoStore').loadPage(1);//加载首页数据
		this.add(grid);
		this.flushView();
	}
});