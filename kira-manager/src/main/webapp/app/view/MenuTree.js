Ext.define('Kira.view.MenuTree', {
	extend : 'Ext.panel.Panel',
	alias : 'widget.menutree',
	
	initComponent : function(config) {
		var me = this,
		    cfg = config || {};
		Ext.apply(me, cfg);
		me.callParent([cfg]);
	},
	
	addItems : function(items) {
		var me = this,
		    items = Ext.Array.from(items);
		this.add(items);
		this.flush();
	},
	
	addButtons : function(buttons) {
		var me = this,
		    buttons = Ext.Array.from(buttons),
		    toolbar = {
				xtype : 'toolbar',
				dock : 'bottom',
				ui : 'footer',
				layout : {
					pack : me.buttonAlign || 'end'
					},
					minWidth : me.minButtonWidth,
					items : buttons
			};
			this.addDocked(toolbar);
			this.flush();
		},
	
		addTools : function(tools) {
			var me = this,
			    tools = Ext.Array.from(tools);
			me.addTool(tools);
			this.flush();
		},
	
		flush : function() {
			var me = this;
			me.doLayout();
		}
	
	});