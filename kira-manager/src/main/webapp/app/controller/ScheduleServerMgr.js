Ext.define('Kira.controller.ScheduleServerMgr', {
	extend : 'Ext.app.Controller',
	views : ['schedule.ScheduleServerMgr'],
	stores : ['ScheduleServerMgr'],
	
	/**
	 * @Override
	 */
	init : function(application) {
		this.control({
			'scheduleservermgr' : {
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
		Ext.create('Kira.store.ScheduleServerMgr', {
			storeId : 'scheduleServerMgrStore'
		});
	},

	/**
	 * @Override
	 */
	loadView : function() {}

});