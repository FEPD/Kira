Ext.define('Kira.controller.TaskVersionsInfoQuery', {
	extend : 'Ext.app.Controller',
	views : ['version.TaskVersionsInfoQuery'],
	stores : ['TaskVersionsInfoQuery'],
	
	/**
	 * @Override
	 */
	init : function(application) {
		this.control({
			'taskversionsinfoquery' : {
				'beforerender' : function(view) {
					var store = Ext.getStore('taskVersionsInfoQueryStore'), 
					    options = store.proxy.extraParams;
			        if(options['criteria.appId']) delete options['criteria.appId'];
					if(options['criteria.triggerId']) delete options['criteria.triggerId'];
					if(options['criteria.manuallyCreated']) delete options['criteria.manuallyCreated'];
			        if(store.fireEvent('beforeload',store,options) !== false){
			        	store.loadPage(1);//加载首页数据
			        }
			        //清除triggerStore中的缓存数据
			        var triggerStore = Ext.data.StoreManager.lookup('triggerStore'); 
			        if(triggerStore.totalCount !== undefined){
			        	triggerStore.removeAll();
			        }
                    view.loadView(); 
				}
			},
			'taskversionsinfoquery combo[name = app]' : {
				'select' : function(obj,records,options){
					//Ext.getCmp('taskVersionsInfoQuery_triggerId').clearValue(); 
					var grid = Ext.ComponentQuery.query('taskversionsinfoquery > grid')[0],
					    triggerObj = grid.down('[name="trigger"]');
					triggerObj.clearValue(); 
		            var triggerStore = Ext.data.StoreManager.lookup('triggerStore'); 
		            triggerStore.getProxy().extraParams['criteria.appId'] = obj.getValue();
		            triggerStore.load(); 
				}
			},
			'taskversionsinfoquery button[action=select]' : {
				'click' : function() {  
                    var grid = Ext.ComponentQuery.query('taskversionsinfoquery > grid')[0],  
                        poolValue = grid.down('[name=app]').getValue(),
                        triggerValue = grid.down('[name=trigger]').getValue(),
                        manuallyCreatedValue = grid.down('[name=manuallyCreated]').getValue(),
                        store = Ext.getStore('taskVersionsInfoQueryStore'),
                        options = store.proxy.extraParams;
                    options['criteria.appId'] = poolValue;
                    options['criteria.triggerId'] = triggerValue;
                    
                    if(Ext.isEmpty(manuallyCreatedValue)){
                    	delete options['criteria.manuallyCreated']
                    }
                    //trim the blank of value
                    if(!Ext.isEmpty(manuallyCreatedValue) && Ext.isEmpty(Ext.util.Format.trim(manuallyCreatedValue))){
                    	delete options['criteria.manuallyCreated']
                    }
                    if(!Ext.isEmpty(manuallyCreatedValue) && Ext.util.Format.trim(manuallyCreatedValue) == '是'){
                    	manuallyCreatedValue = 'true';
                    	options['criteria.manuallyCreated'] = manuallyCreatedValue;
                    }
                    if(!Ext.isEmpty(manuallyCreatedValue) && Ext.util.Format.trim(manuallyCreatedValue) == '否'){
                    	manuallyCreatedValue = 'false';
                    	options['criteria.manuallyCreated'] = manuallyCreatedValue;
                    }
                    if(store.fireEvent('beforeload',store,options) !== false){
                    	store.loadPage(1);
                    }
                } 
			}
		});
	},
	
	/**
	 * @Override
	 */
	loadModel : function() {
		Ext.create('Kira.store.TaskVersionsInfoQuery', {
			storeId : 'taskVersionsInfoQueryStore'
		});
		Ext.create('Kira.store.PoolStore', {
			storeId : 'poolStore'
		});
		
		Ext.create('Kira.store.TriggerStore', {
			storeId : 'triggerStore'
		});
		Ext.create('Kira.store.ManuallyCreatedStore', {
			storeId : 'manuallyCreatedStore'
		});
	},

	/**
	 * @Override
	 */
	loadView : function() {}

});