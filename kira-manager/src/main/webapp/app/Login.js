Ext.define("Kira.Login", {
	extend : "Ext.window.Window",
	singleton : true,
	title : 'Kira调度平台',
	width : 240,
	height : 130,
	modal : true,
	closable : false,
	resizable : false,
	closeAction : 'hide',
	hideMode : 'offsets',
	initComponent : function() {
		var me = this;
		me.image = Ext.create(Ext.Img, {
			src : "controller?method=verifyImg&_dc=" + (new Date()).getTime(),
			listeners : {
				click : me.onRefrehImage,
				element : "el",
				scope : me
			}
		});

		me.form = Ext.create(Ext.form.Panel, {
			border : false,
			bodyPadding : 5,
			url : "controller?method=loginCheck",
			defaultType : "textfield",
			
			fieldDefaults : {
				labelWidth : 80,
				labelSeparator : "：",
				anchor : "0",
				labelAlign : "right",
				allowBlank : false
			},
			items : [ {
				fieldLabel : "用户名",
				name : "userName"
			}, {
				fieldLabel : "密码",
				name : "password",
				inputType : "password",
				listeners : {
					'specialkey' : function(text, e){//增加回车事件
						if (e.getKey() == Ext.EventObject.ENTER){
							me.onLogin();
						} 
					}
				}
			}/*, {
				fieldLabel : "验证码",
				name : "vcode",
				minLength : 4,
				maxLength : 4,
				listeners : {
					'specialkey' : function(text, e){//增加回车事件
						if (e.getKey() == Ext.EventObject.ENTER){
							me.onLogin();
						} 
					}
				}
			}, {
				xtype : "panel",
				width : 100,
				height : 40,
				anchor : "-5",
				layout : "fit",
				items : [me.image]
			}, {
				xtype : "container",
				anchor : "-5",
				html : "如果看不清验证码，可单击图片刷新。"
			}*/],
			dockedItems : [{
				xtype : 'toolbar',
				dock : 'bottom',
				ui : 'footer',
				layout : {
					pack : "center"
				},
				items : [{
					text : "登录",
					width : 80,
					disabled : true,
					formBind : true,
					handler : me.onLogin,
					scope : me
				}, {
					text : "重置",
					width : 80,
					handler : me.onReset,
					scope : me
				}]
			}]
		});

		me.items = [me.form]
		me.callParent(arguments);
	},

	onRefrehImage : function() {
		this.image.setSrc("controller?method=verifyImg&_dc="
				+ (new Date()).getTime());
	},

	onReset : function() {
		var me = this;
		me.form.getForm().reset();
		if (me.form.items.items[0]) {
			me.form.items.items[0].focus(true, 10);
		}
		me.onRefrehImage();
	},
	
	onLogin : function() {
		//alert(Ext.isIE);
		var me = this, f = me.form.getForm();
		if (f.isValid()) {
			f.submit({
				waitMsg : "正在登录，请等待...",
				waitTitle : "正在登录",
				success : function(form, action) {
					window.location.href = "controller?method=index";
				},
				failure : function(form, action) {
					Ext.MessageBox.alert("提示", action.result.msg);
				},
				scope : me
			});
		}
	}

});