Ext.define('Kira.view.execute.TaskExecuteMgr', {
	extend : 'Ext.container.Container',
	requires : ['Ext.ux.form.field.DateTime'],
	//extend : 'Ext.form.Panel',
	alias : 'widget.taskexecutemgr',
	title : '任务执行管理',
	//layout: 'fit',
	closable : true,
	initComponent : function() {
		var me = this;
		Ext.applyIf(me, {
			layout : 'border',
			margins : '0 0 0 0'
		});
		me.callParent(arguments);
	},

	flushView : function() {
		this.doComponentLayout();
	},

	loadView : function() {
		var form = Ext.create('Ext.form.Panel',{
			title : '查询条件',
			baseCls : 'x-panel',
			bodyStyle: {
			    background: 'none',
			    padding: '10px'
			},
			border : true,
			height : 120,
			region : 'north',
			layout : 'hbox',
			buttonAlign : 'center',
			buttons : [{
				text : '查询',
				action : 'select',
				scope : this
			}, '-', {
				text : '重置',
				action : 'reset',
				scope : this
			}],
			items : [
				Ext.create('Ext.form.Panel',{
					border : false,
			        flex : 0.25,
			        bodyStyle: {
					    background: 'none'
					},
			        layout : 'vbox',
			        defaults : {
						xtype : 'textfield',
						labelWidth : 80,
						labelAlign : 'right'
					},
					items : [{
						fieldLabel : 'AppId',
						xtype : 'combo',
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
							width: 150,
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
					},{
						fieldLabel : 'JobItemId',
						width : 200,
						name : 'jobItemId'
					}]
				}),Ext.create('Ext.form.Panel',{
					border : false,
			        flex : 0.25,
			        bodyStyle: {
					    background: 'none'
					},
			        layout : 'vbox',
			        defaults : {
						xtype : 'textfield',
						labelWidth : 80,
						labelAlign : 'right'
					},
					items : [{
						fieldLabel : '定时任务Id',
						labelWidth : 100,
						xtype : 'combo',
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
					},{
						fieldLabel : '创建时间(from)',
						labelWidth : 90,
						xtype : 'datetimefield',
						format : 'Y-m-d H:i:s',
						name : 'createTimeFrom'
					}]
				}),Ext.create('Ext.form.Panel',{
					border : false,
			        flex : 0.25,
			        bodyStyle: {
					    background: 'none'
					},
			        layout : 'vbox',
			        defaults : {
						xtype : 'textfield',
						labelWidth : 80,
						labelAlign : 'right'
					},
					items : [{
						fieldLabel : '调度状态',
						labelWidth : 85,
						xtype : 'combo',
						name : 'status',
						editable : true,
						value : '',
						store : Ext.getStore('statusStore'),
						displayField : 'name',
						valueField : 'id',
						matchFieldWidth: true,
						//queryMode: 'local',
						triggerAction:'all'
					},{
						fieldLabel : '创建时间(to)',
						labelWidth : 90,
						xtype : 'datetimefield',
						format : 'Y-m-d H:i:s',
						name : 'createTimeTo'
					}]
				}),Ext.create('Ext.form.Panel',{
					border : false,
			        flex : 0.25,
			        bodyStyle: {
					    background: 'none'
					},
			        layout : 'vbox',
			        defaults : {
						xtype : 'textfield',
						labelWidth : 80,
						labelAlign : 'right'
					},
					items : [{
						fieldLabel : 'JobId',
						width : 226,
						name : 'jobId'
					}]
				})
			]
		})
		var grid = Ext.create('Ext.grid.Panel', {
			region : 'center',
			store : Ext.getStore('taskExecuteMgrStore'),
			autoShow : true,
			scroll : true,
			viewConfig : {
				enableTextSelection : true,
				forceFit:true
			},
			columns :[{
				text : '操作',
				xtype : 'actioncolumn',
				width : 50,
				align : 'center',
				id : 'taskExecuteMgrIcon',
				icon : 'extjs/resources/images/action.gif'
			},{
				text : 'JobId',
				dataIndex : 'id',
				width : 250
			},{
				text : 'AppId',
				dataIndex : 'appId',
				width : 100
			},{
				text : '定时任务Id',
				dataIndex : 'triggerId',
				width : 200
			},{
				text : '版本号',
				dataIndex : 'version',
				width : 50
			},{
				text : '创建时间',
				dataIndex : 'createTimeAsString',
				width : 150
			},{
				text : '当前状态',
				dataIndex : 'jobStatusName',
				width : 120
			},{
				text : '创建者',
				dataIndex : 'createdBy',
				width : 100
			},{
				text : '手动创建的任务',
				dataIndex : 'manuallyScheduled',
				width : 100
			},{
				text : '任务相关信息',
				dataIndex : 'resultData',
				width : 300
			}],
			dockedItems :[{
				xtype: 'pagingtoolbar',
                store: Ext.getStore('taskExecuteMgrStore'),// same store GridPanel is using
                dock: 'bottom', //分页 位置
                emptyMsg: '没有数据',
                displayInfo: true,
                displayMsg: '当前显示{0}-{1}条记录 / 共{2}条记录 ',
                beforePageText: '第',
                afterPageText: '页/共{0}页'
			}]
		});
		this.add(form);
		this.add(grid);
		this.flushView();
	}
});