Ext.define('Kira.view.client.KiraClientConfigMgr', {
	extend : 'Ext.container.Container',
	alias : 'widget.kiraclientconfigmgr',
	title : 'Kira客户端配置管理',
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
			store : Ext.getStore('kiraClientConfigMgrStore'),
			autoShow : true,
			scroll : true,
			viewConfig : {
				enableTextSelection : true
			},
			columns :[{
				text : '操作',
				xtype : 'actioncolumn',
				width : 50,
				align : 'center',
				id : 'kiraClientConfigMgrIcon',
				icon : 'extjs/resources/images/action.gif'
			},{
				text : 'AppId',
				dataIndex : 'appId',
				width : 200
			},{
				text : '客户端版本',
				dataIndex : 'kiraClientVersion',
				width : 100
			},{
				text : 'App只对特定人可见',
				dataIndex : 'visibilityLimited',
				width : 150
			},{
				text : '特定可见人列表',
				dataIndex : 'visibleForUsers',
				width : 150
			},{
				text : '创建时间',
				dataIndex : 'createTimeAsString',
				width : 150
			},{
				text : '是否手动创建',
				dataIndex : 'manuallyCreated',
				width : 80
			},{
				text : '手动创建人',
				dataIndex : 'manuallyCreatedBy',
				width : 100
			},{
				text : '手动创建信息',
				dataIndex : 'manuallyCreatedDetail',
				width : 300
			},{
				text : '上次注册时间',
				dataIndex : 'lastRegisterTimeAsString',
				width : 150
			},{
				text : '上次手动更新时间',
				dataIndex : 'lastManuallyUpdateTimeAsString',
				width : 150
			}, {
				text : '上次手动更新人',
				dataIndex : 'lastManuallyUpdateBy',
				width : 150
			},/*{
				text : '上次注册信息',
				dataIndex : 'lastRegisterDetail',
				width : 300
			},*/{
				text : '是否发送报警邮件',
				dataIndex : 'sendAlarmEmail',
				width : 120
			},{
				text : '邮件地址',
				dataIndex : 'emailsToReceiveAlarm',
				width : 200
			},{
				text : '是否发送报警短信',
				dataIndex : 'sendAlarmSMS',
				width : 120
			},{
				text : '手机号码',
				dataIndex : 'phoneNumbersToReceiveAlarmSMS',
				width : 200
			}],
			tbar : ['AppId:',{
				xtype : 'combo',
				//id : 'kiraClientConfigMgr_poolId',
				name : 'app',
				editable : true,
				value : '',
				store : Ext.getStore('kiraClientPoolStore').load(),
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
                store: Ext.getStore('kiraClientConfigMgrStore'),// same store GridPanel is using
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