Ext.define('Kira.controller.TaskStatus', {
	extend : 'Ext.app.Controller',
	views : ['status.TaskStatus'],
	stores : ['TaskStatus'],
	
	/**
	 * @Override
	 */
	init : function(application) {
		this.control({
			'taskstatus' : {
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
		Ext.create('Kira.store.TaskStatus', {
			storeId : 'statusStore'
		});
	},

	/**
	 * @Override
	 */
	loadView : function() {}

});