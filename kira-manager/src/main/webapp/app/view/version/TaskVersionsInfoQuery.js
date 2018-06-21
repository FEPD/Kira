Ext.define('Kira.view.version.TaskVersionsInfoQuery', {
	extend : 'Ext.container.Container',
	alias : 'widget.taskversionsinfoquery',
	title : '各版本配置信息查询',
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
			store : Ext.getStore('taskVersionsInfoQueryStore'),
			autoShow : true,
			scroll : true,
			viewConfig : {
				enableTextSelection : true
			},
			columns :[{
				text : 'AppId',
				dataIndex : 'appId',
				width : 200
			},{
				text : '定时任务Id',
				dataIndex : 'triggerId',
				width : 200
			},{
				text : '版本号',
				dataIndex : 'version',
				width : 50
			},{
				text : '描述',
				dataIndex : 'description',
				width : 600
			},{
				text : '被调用的AppId',
				dataIndex : 'targetAppId',
				width : 200
			},{
				text : '被调用的定时任务Id',
				dataIndex : 'targetTriggerId',
				width : 200
			},{
				text : '定时任务类型',
				dataIndex : 'triggerType',
				width : 100
			},{
				text : '创建时间',
				dataIndex : 'createTimeAsString',
				width : 150
			},{
				text : '是否手动创建',
				dataIndex : 'manuallyCreated',
				width : 80
			},{
				text : 'cron表达式',
				dataIndex : 'cronExpression',
				width : 100
			},{
				text : '开始时间',
				dataIndex : 'start_time',
				width : 150
			}, {
				text : '结束时间',
				dataIndex : 'end_time',
				width : 150
			},{
				text : '启动延迟(ms)',
				dataIndex : 'startDelay',
				width : 120
			},{
				text : '执行间隔(ms)',
				dataIndex : 'repeatInterval',
				width : 120
			},{
				text : '运行时间阀值(ms)',
				dataIndex : 'runTimeThreshold',
				width : 120
			},{
				text : '执行次数',
				dataIndex : 'repeatCount',
				width : 60,
				renderer : function(value){
					if(value == '-1'){
						return '无限次';
					}
					return value;
				}
			},{
				text : '最后一次执行结束时间',
				dataIndex : 'finalizedTimeAsString',
				width : 150
			},{
				text : '是否异步执行',
				dataIndex : 'asynchronous',
				width : 80
			},{
				text : '触发时是否只派送到一个执行地点执行',
				dataIndex : 'onlyRunOnSingleProcess',
				width : 180
			},{
				text : '任务执行地点',
				dataIndex : 'locationsToRunJob',
				width : 350
			},{
				text : '是否只在指定的执行地点执行任务',
				dataIndex : 'limitToSpecifiedLocations',
				width : 200
			},{
				text : '允许业务方法被并发执行',
				dataIndex : 'concurrent',
				width : 160
			},{
				text : 'Pool本地调度',
				dataIndex : 'scheduledLocally',
				width : 80
			},{
				text : '允许在集群服务器间重新分配',
				dataIndex : 'reassignable',
				width : 180
			},{
				text : 'Disabled',
				dataIndex : 'disabled',
				width : 50
			},{
				text : '执行方法',
				dataIndex : 'targetMethod',
				width : 120
			},{
				text : '方法参数类型',
				dataIndex : 'targetMethodArgTypes',
				width : 200
			},{
				text : '方法参数值',
				dataIndex : 'argumentsAsJsonArrayString',
				width : 200
			},{
				text : '未注册',
				dataIndex : 'unregistered',
				width : 50
			},{
				text : '未注册时间',
				dataIndex : 'unregisteredUpdateTimeAsString',
				width : 150
			},{
				text : '已删除',
				dataIndex : 'deleted',
				width : 50
			},{
				text : '已删除时间',
				dataIndex : 'deletedUpdateTimeAsString',
				width : 150
			},{
				text : '漏执行策略',
				dataIndex : 'misfireInstruction',
				width : 80
			},{
				text : '优先级',
				dataIndex : 'priority',
				width : 80
			},{
				text : '需要宕机恢复',
				dataIndex : 'requestsRecovery',
				width : 80
			},{
				text : '配置从MasterZone复制到SlaveZone',
				dataIndex : 'copyFromMasterToSlaveZone',
				width : 200
			},{
				text : '只在MasterZone中被调度执行',
				dataIndex : 'onlyScheduledInMasterZone',
				width : 180
			},{
				text : '任务派送超时Enabled',
				dataIndex : 'jobDispatchTimeoutEnabled',
				width : 130
			},{
				text : '任务派送超时时间(毫秒)',
				dataIndex : 'jobDispatchTimeout',
				width : 130
			},{
				text : '备注',
				dataIndex : 'comments',
				width : 200
			}],
			tbar : ['AppId:',{
				xtype : 'combo',
				//id : 'taskVersionsInfoQuery_poolId',
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
				//id : 'taskVersionsInfoQuery_triggerId',
				name : 'trigger',
				editable : true,
				value : '',
				store : Ext.getStore('triggerStore'),
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
                store: Ext.getStore('taskVersionsInfoQueryStore'),// same store GridPanel is using
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