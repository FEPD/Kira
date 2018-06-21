Ext.define('Kira.view.client.UpdateKiraClientConfig', {
	extend : 'Ext.window.Window',
	alias : 'widget.updatekiraclientconfig',
	title : '更新Kira客户端配置信息',
	layout: 'fit',
	closable : true,
	modal : true,
	initComponent : function() {
		var me = this;
		Ext.applyIf(me, {
			layout : 'fit',
			//title : '',
			items : [{
				xtype : 'form',
				border : false,
				width : 720,
				height : 250,
				layout : {
					type : 'hbox',
					align : 'middle'
				}
			}],
			buttons : ['->',{
				text : '确定',
				action : 'save',
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
					readOnly : true,
					readOnlyCls : 'x-item-disabled',
					name : 'kiraClientMetadata.appId'
				},{
					fieldLabel : 'Kira客户端版本',
					readOnly : true,
					readOnlyCls : 'x-item-disabled',
					name : 'kiraClientMetadata.kiraClientVersion'
				},{
					xtype : 'combo',
					fieldLabel : '是否手动创建',
					readOnly : true,
					readOnlyCls : 'x-item-disabled',
					name : 'kiraClientMetadata.manuallyCreated',
					store : Ext.getStore('manuallyCreatedStore'),
					displayField : 'manuallyCreatedName',
				    valueField : 'manuallyCreatedValue',
				    matchFieldWidth: true,
				    queryMode: 'local',
				    triggerAction:'all'
				},{
					fieldLabel : '是否发送报警邮件',
					xtype : 'checkboxfield',
					name : 'kiraClientMetadata.sendAlarmEmail'
				},{
					fieldLabel : '邮件地址(逗号分隔)',
					xtype : 'textarea',
					height : 100,
					name : 'kiraClientMetadata.emailsToReceiveAlarm'
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
					fieldLabel : 'Pool是否只对特定人可见',
					name : 'kiraClientMetadata.visibilityLimited'
				},{
					fieldLabel : '特定可见人列表(逗号分隔的域用户)',
					name : 'kiraClientMetadata.visibleForUsers'
				},{
					fieldLabel : '是否发送报警短信',
					xtype : 'checkboxfield',
					name : 'kiraClientMetadata.sendAlarmSMS'
				},{
					fieldLabel : '手机号码(逗号分隔)',
					xtype : 'textarea',
					height : 100,
					name : 'kiraClientMetadata.phoneNumbersToReceiveAlarmSMS'
				}]
			});
		formCmp.add(panel1);
		formCmp.add(panel2);
		formCmp.doLayout();
		this.flushView();
	}
});