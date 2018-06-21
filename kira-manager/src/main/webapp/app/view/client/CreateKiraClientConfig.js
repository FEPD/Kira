Ext.define('Kira.view.client.CreateKiraClientConfig', {
	extend : 'Ext.window.Window',
	alias : 'widget.createkiraclientconfig',
	title : '创建Kira客户端配置信息',
	layout: 'fit',
	closable : true,
	modal : true,
	initComponent : function() {
		var me = this;
		Ext.applyIf(me, {
			layout : 'fit',
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
					//readOnly : true,
					//readOnlyCls : 'x-item-disabled',
					name : 'newKiraClientMetadata.appId'
				},{
					fieldLabel : 'Kira客户端版本',
					readOnly : true,
					readOnlyCls : 'x-item-disabled',
					name : 'newKiraClientMetadata.kiraClientVersion'
				},{
					fieldLabel : '是否发送报警邮件',
					xtype : 'checkboxfield',
					name : 'newKiraClientMetadata.sendAlarmEmail'
				},{
					fieldLabel : '邮件地址(逗号分隔)',
					xtype : 'textarea',
					height : 100,
					name : 'newKiraClientMetadata.emailsToReceiveAlarm'
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
					name : 'newKiraClientMetadata.visibilityLimited'
				},{
					fieldLabel : '特定可见人列表(逗号分隔的域用户)',
					name : 'newKiraClientMetadata.visibleForUsers'
				},{
					fieldLabel : '是否发送报警短信',
					xtype : 'checkboxfield',
					name : 'newKiraClientMetadata.sendAlarmSMS'
				},{
					fieldLabel : '手机号码(逗号分隔)',
					xtype : 'textarea',
					height : 100,
					name : 'newKiraClientMetadata.phoneNumbersToReceiveAlarmSMS'
				}]
			});
		formCmp.add(panel1);
		formCmp.add(panel2);
		formCmp.doLayout();
		this.flushView();
	}
});