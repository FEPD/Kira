/**
 * JS动态加载
 */
//如果超时或者其他异常
Ext.Ajax.on("requestexception", function(conn, response, options) {
	if (response.status == "999") {
		window.location.href = "controller?method=index";
	}
});
Ext.Loader.setConfig({
	enabled : true,
	paths : {
		'Ext' : 'extjs',
		'Kira': 'app',
		'Ext.ux' : 'extjs/ux'
	}
});

Ext.require([
	'Ext.app.Application', 
	'Ext.app.Controller'
]);

/**
 * MVC 动态加载接口
 */
Ext.app.Controller.implement({
	/**
	 * @MVC 动态加载模型
	 */
	loadModel : function() {},

	/**
	 * @MVC 动态加载视图
	 */
	loadView : function() {},
	
	getApplication : function() {
		return this.application;
	}
});

Ext.app.Application.implement({
	/**
	 * @MVC 动态加载控制器
	 * @param {String/Array} controllers
	 */
	loadModule : function(controllers) {
		var me = this,
		    controllers = Ext.Array.from(controllers), 
		    ln = controllers.length, 
		    controller,
		    i;
		for (i = 0; i < ln; i++) {
			var name = controllers[i];
			//如果controller还没被加载，则创建并加载
			if (!this.controllers.containsKey(name)) {
				controller = Ext.create(
					this.getModuleClassName(name, 'controller'), {
						application : this,
						id : name
					});
				this.controllers.add(controller);
				
				/**
				 * 优先加载模型
				 */
				controller.loadModel();
				controller.init(this);
				controller.onLaunch(this);
				
				/**
				 * 动态构建视图/绑定模型数据
				 */
				controller.loadView();
			}
		}
	}
});
//初始化Tip提示功能
Ext.QuickTips.init();
Ext.form.Field.prototype.msgTarget = 'side';