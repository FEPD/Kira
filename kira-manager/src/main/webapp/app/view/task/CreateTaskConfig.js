Ext.define('Kira.view.task.CreateTaskConfig', {
	extend : 'Ext.window.Window',
	requires : ['Ext.ux.form.field.DateTime'],
	alias : 'widget.createtaskconfig',
	title : '创建定时任务配置',
	layout: 'fit',
	closable : true,
	modal : true,
	initComponent : function() {
		var me = this;
		Ext.applyIf(me, {
			layout : 'fit',
			title : '定时任务配置',
			items : [{
				xtype : 'form',
				border : false,
				width : 700,
				height : 520,
				layout : {
					type : 'hbox',
					align : 'middle'
				}
			}],
			buttons : ['->',{
				text : '确定',
				action : 'save',
				//disabled : true,
				scope : this
			}, '-', {
				text : '关闭',
				action : 'close',
				scope : this
			}]
		});
		me.callParent(arguments);
	},

	flushView : function() {
		this.doComponentLayout();
	},

	loadView : function() {
		var formCmp = this.getComponent(0); 
		var panel1 = Ext.create('Ext.form.Panel',{
			border : false,
			flex : 1,
			defaults : {
				xtype : 'textfield',
				labelWidth : 150,
				labelAlign : 'right'
			},
			items : [{
					fieldLabel : 'AppId',
					name : 'newTriggerMetadata.appId'
				},{
					fieldLabel : '定时任务Id',
					
					name : 'newTriggerMetadata.triggerId'
				},{
					fieldLabel : '版本号',
					value : 0,
					name : 'newTriggerMetadata.version'
				},{
					xtype : 'combo',
					fieldLabel : '定时任务类型',
					name : 'newTriggerMetadata.triggerType',
					store : Ext.getStore('triggerTypeStore'),
					displayField : 'triggerTypeName',
				    valueField : 'triggerTypeValue',
				    matchFieldWidth: true,
				    triggerAction:'all'
				},{
					xtype : 'combo',
					fieldLabel : '漏执行策略',
					name : 'newTriggerMetadata.misfireInstruction',
					value : 0,
					store : Ext.getStore('commonStore'),
					displayField : 'name',
					valueField : 'value',
					matchFieldWidth: true,
					queryMode: 'local',
					triggerAction:'all'
				},{
					fieldLabel : 'cron表达式',
					name : 'newTriggerMetadata.cronExpression'
				},{
					xtype : 'datetimefield',
					format : 'Y-m-d H:i:s',
					fieldLabel : '开始时间',
					name : 'newTriggerMetadata.startTimeAsString'
				},{
					xtype : 'datetimefield',
					format : 'Y-m-d H:i:s',
					fieldLabel : '结束时间',
					name : 'newTriggerMetadata.endTimeAsString'
				},{
					fieldLabel : '启动延迟(毫秒)',
					name : 'newTriggerMetadata.startDelay'
				},{
					fieldLabel : '执行间隔(毫秒)',
					name : 'newTriggerMetadata.repeatInterval'
				},{
					fieldLabel : '运行时间阀值(毫秒)',
					name : 'newTriggerMetadata.runTimeThreshold',
					regex : /^(([0-9]+[\.]?[0-9]+)|[1-9])$/,
					regexText : '输入值必须为正值！',
					msgTarget : 'qtip'
				},{
					fieldLabel : '执行次数(-1代表无限次)',
					name : 'newTriggerMetadata.repeatCount'
				},{
					fieldLabel : '优先级',
					allowBlank : false,
					regex : /^[0-9]*[1-9][0-9]*$/,
					regexText : '输入值必须为正整数！',
					name : 'newTriggerMetadata.priority',
					value : 5,
					msgTarget : 'qtip'
				},{
					fieldLabel : '需要宕机恢复',
					allowBlank : false,
					msgTarget : 'qtip',
					value : false,
					name : 'newTriggerMetadata.requestsRecovery'
				},{
					fieldLabel : '任务派送超时Enabled',
					value : false,
					name : 'newTriggerMetadata.jobDispatchTimeoutEnabled',
					regex : /^(true|false)$/,
					regexText : '输入值必须为true或false',
					msgTarget : 'qtip'
				},{
					fieldLabel : '任务派送超时时间(>=120000毫秒)',
					name : 'newTriggerMetadata.jobDispatchTimeout',
					regex : /^(([0-9]+[\.]?[0-9]+)|[1-9])$/,
					regexText : '输入值必须为>=120000正整数！',
					msgTarget : 'qtip'
				}]
		});
		var panel2 = Ext.create('Ext.form.Panel',{
			border : false,
			flex : 1,
			defaults : {
				xtype : 'textfield',
				labelWidth : 150,
				labelAlign : 'right'
			},
			items : [{
					xtype : 'combo',
					fieldLabel : '被调用的AppId',
					name : 'newTriggerMetadata.targetAppId',
					editable : true,
					store : Ext.getStore('targetPoolStore'),
					displayField : 'poolName',
				    valueField : 'poolValue',
				    matchFieldWidth: false,
				    triggerAction:'all',
				    listConfig : {
						loadingText: 'Loading...',
						width: 200,
						autoHeight:true
					}
				},{
					xtype : 'combo',
					fieldLabel : '被调用的定时任务Id',
					name : 'newTriggerMetadata.targetTriggerId',
					editable : true,
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
				},{
					fieldLabel : '执行方法',
					readOnly : true,
					readOnlyCls : 'x-item-disabled',
					name : 'newTriggerMetadata.targetMethod'
				},{
					fieldLabel : '方法参数类型(Json数组)',
					readOnly : true,
					readOnlyCls : 'x-item-disabled',
					name : 'newTriggerMetadata.targetMethodArgTypes'
				},{
					fieldLabel : '方法参数值(Json数组)',
					value : '[]',
					name : 'newTriggerMetadata.argumentsAsJsonArrayString'
				},{
					fieldLabel : '是否异步执行',
					value : true,
					name : 'newTriggerMetadata.asynchronous'
				},{
					fieldLabel : '触发时是否只派送到一个执行地点执行',
					value : true,
					name : 'newTriggerMetadata.onlyRunOnSingleProcess'
				},{
					fieldLabel : '任务执行地点(逗号分隔的ip:port列表)',
					name : 'newTriggerMetadata.locationsToRunJob'
				},{
					fieldLabel : '是否只在指定的执行地点执行任务',
					name : 'newTriggerMetadata.limitToSpecifiedLocations',
					value : false
				},{
					fieldLabel : '允许业务方法被并发执行',
					value : true,
					name : 'newTriggerMetadata.concurrent',
					regex : /^(true|false)$/,
					regexText : '输入值必须为true或false',
					msgTarget : 'qtip'
				},{
					fieldLabel : '配置从MasterZone复制到SlaveZone',
					allowBlank : false,
					msgTarget : 'qtip',
					value : false,
					name : 'newTriggerMetadata.copyFromMasterToSlaveZone'
				},{
					fieldLabel : '只在MasterZone中被调度执行',
					allowBlank : false,
					msgTarget : 'qtip',
					value : true,
					name : 'newTriggerMetadata.onlyScheduledInMasterZone'
				},{
					fieldLabel : 'App本地调度',
					value : false,
					readOnly : true,
					readOnlyCls : 'x-item-disabled',
					name : 'newTriggerMetadata.scheduledLocally'
				},{
					fieldLabel : 'Disabled',
					value : false,
					name : 'newTriggerMetadata.disabled'
				},{
					fieldLabel : '描述',
					name : 'newTriggerMetadata.description'
				}]
			});
		formCmp.add(panel1);
		formCmp.add(panel2);
		formCmp.doLayout();
		this.flushView();
	}
});