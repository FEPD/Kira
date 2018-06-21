Ext.define('Kira.view.execute.subtask.CancelExecuteTaskParam', {
	extend : 'Ext.window.Window',
	alias : 'widget.cancelexecutetaskparam',
	title : '取消执行任务',
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
				width : 300,
				height : 120,
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
		var panel = Ext.create('Ext.form.Panel',{
			border : false,
			flex : 1,
			defaults : {
				xtype : 'textfield',
				labelWidth : 80,
				labelAlign : 'right'
			},
			items : [{
					fieldLabel : '方法参数值\n(如:{"key":"k",\n"value":"v"})',
					xtype : 'textarea',
					height : 100,
					name : 'criteria.cancelJobJsonMapString'
				}]
		});
		formCmp.add(panel);
		formCmp.doLayout();
		this.flushView();
	}
});