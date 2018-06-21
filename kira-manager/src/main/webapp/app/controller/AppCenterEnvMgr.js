Ext.define('Kira.controller.AppCenterEnvMgr', {
	extend : 'Ext.app.Controller',
	views : ['env.AppCenterEnvMgr'],
	stores : ['AppCenterEnvMgr'],
	
	/**
	 * @Override
	 */
	init : function(application) {
		this.control({
			'appcenterenvmgr' : {
				'beforerender' : function(view) {
                   view.loadView(); 
				}
			}
		});
	},
	
	/**
	 * @Override
	 */
	loadModel : function() {
		Ext.create('Kira.store.AppCenterEnvMgr', {
			storeId : 'appCenterEnvMgrStore'
		});
	},

	/**
	 * @Override
	 */
	loadView : function() {}

});