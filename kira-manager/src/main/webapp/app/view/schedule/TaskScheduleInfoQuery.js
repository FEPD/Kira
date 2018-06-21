Ext.define('Kira.view.schedule.TaskScheduleInfoQuery', {
	extend : 'Ext.container.Container',
	alias : 'widget.taskscheduleinfoquery',
	title : '任务调度信息查询',
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
			store : Ext.getStore('taskScheduleInfoQueryStore'),
			autoShow : true,
			//width : 800,
			scroll : true,
			viewConfig : {
				enableTextSelection : true
			},
			columns :[{
				text : '操作',
				xtype : 'actioncolumn',
				width : 50,
				align : 'center',
				id : 'taskScheduleInfoQueryIcon',
				icon : 'extjs/resources/images/action.gif'
			},{
				text : 'AppId',
				dataIndex : 'appId',
				width : 200
			},{
				text : '定时任务Id',
				dataIndex : 'triggerId',
				width : 200
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
				width : 150,
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
			tbar : ['AppId:',{
				xtype : 'combo',
				//id : 'taskscheduleinfoquery_poolId',
				name : 'app',
				editable : true,
				value : '',
				store : Ext.getStore('poolStore').load(),
				displayField : 'poolName',
				valueField : 'poolValue',
				matchFieldWidth: false,
				//queryMode: 'remote',
				triggerAction:'all',
				listConfig : {
					loadingText: 'Loading...',
					emptyText:'未找到匹配值',
					width: 200,
					autoHeight:true
				},
				listeners : {
					'beforequery':function(e){

						var combo = e.combo;
						if(!e.forceAll){
							var input = e.query;
							// 检索的正则
							var regExp = new RegExp(".*" + input + ".*");
							// 执行检索
							combo.store.filterBy(function(record,id){
								// 得到每个record的项目名称值
								var text = record.get(combo.displayField);
								return regExp.test(text);
							});
							combo.expand();
							return false;
						}
					}
				}
			},'-','定时任务Id:',{
				xtype : 'combo',
				//id : 'taskscheduleinfoquery_triggerId',
				name : 'trigger',
				editable : true,
				value : '',
				store : Ext.getStore('targetTriggerStore'),
				displayField : 'triggerName',
				valueField : 'triggerValue',
				matchFieldWidth: false,
				queryMode: 'local',
				triggerAction:'all',
				listConfig : {
					loadingText: 'Loading...',
					width: 200,
					autoHeight:true
				}
			},'-','是否手动创建:',{
				xtype : 'combo',
				name : 'manuallyCreated',
				editable : true,
				value : '',
				store : Ext.getStore('manuallyCreatedStore'),
				displayField : 'manuallyCreatedName',
				valueField : 'manuallyCreatedValue',
				matchFieldWidth: true,
				queryMode: 'local',
				triggerAction:'all'
			},'-',{
				text : '查询',
				iconCls : 'icon-search',
				action : 'select'
			}],
			dockedItems :[{
				xtype: 'pagingtoolbar',
                store: Ext.getStore('taskScheduleInfoQueryStore'),// same store GridPanel is using
                dock: 'bottom', //分页 位置
                emptyMsg: '没有数据',
                displayInfo: true,
                displayMsg: '当前显示{0}-{1}条记录 / 共{2}条记录 ',
                beforePageText: '第',
                afterPageText: '页/共{0}页'
			}]
		});
		this.add(grid);
		this.flushView();
	}
});