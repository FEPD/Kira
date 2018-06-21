Ext.define('Kira.controller.TaskScheduleInfoQuery', {
	extend : 'Ext.app.Controller',
	views : ['schedule.TaskScheduleInfoQuery','schedule.PauseTaskSchedule','schedule.ResumeTaskSchedule',
	         'schedule.UnTaskSchedule','schedule.ReTaskSchedule'
	],
	stores : ['TaskScheduleInfoQuery'],
	
	/**
	 * @Override
	 */
	init : function(application) {
		this.control({
			'taskscheduleinfoquery' : {
				'beforerender' : function(view) {
					var store = Ext.getStore('taskScheduleInfoQueryStore'), 
					    options = store.proxy.extraParams;
			        if(options['criteria.appId']) delete options['criteria.appId'];
					if(options['criteria.triggerId']) delete options['criteria.triggerId'];
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
			'taskscheduleinfoquery combo[name = app]' : {
				'select' : function(obj,records,options){
					var grid = Ext.ComponentQuery.query('taskscheduleinfoquery > grid')[0],
					    triggerObj = grid.down('[name="trigger"]');
					triggerObj.clearValue(); 
					/*
		            var triggerStore = Ext.data.StoreManager.lookup('triggerStore'); 
		            triggerStore.getProxy().extraParams['criteria.poolId'] = obj.getValue();
		            triggerStore.load(); 
		            */
					var targetTriggerStore = Ext.data.StoreManager.lookup('targetTriggerStore'); 
		            targetTriggerStore.getProxy().extraParams['criteria.appId'] = obj.getValue();
		            targetTriggerStore.load(); 
				}
			},
			'taskscheduleinfoquery actioncolumn[id=taskScheduleInfoQueryIcon]' : {
				click : function(view,item,rowIndex,colIndex,e){
					var store = Ext.getStore('taskScheduleInfoQueryStore'),
					    record = store.getAt(rowIndex),
					    appId = record.get('appId'),
					    triggerId = record.get('triggerId'),
					    menus = Ext.create('Ext.menu.Menu',{
							items : [/*{
								text: '暂停任务调度',
								handler: function (){
									var view = Ext.widget('pausetaskschedule'), 
									    pauseTaskScheduleStore = Ext.data.StoreManager.lookup('pauseTaskScheduleStore'); 
						            pauseTaskScheduleStore.getProxy().extraParams['criteria.poolId'] = poolId;
						            pauseTaskScheduleStore.getProxy().extraParams['criteria.triggerId'] = triggerId;
				                    view.loadView();  
				                    view.show();  
								}
							},{
								text: '恢复任务调度',
								handler: function (){
									var view = Ext.widget('resumetaskschedule'), 
									    resumeTaskScheduleStore = Ext.data.StoreManager.lookup('resumeTaskScheduleStore'); 
						            resumeTaskScheduleStore.getProxy().extraParams['criteria.poolId'] = poolId;
						            resumeTaskScheduleStore.getProxy().extraParams['criteria.triggerId'] = triggerId;
				                    view.loadView();  
				                    view.show();  
								}
							},{
								text: '取消任务调度',
								handler: function (){
									Ext.MessageBox.confirm('确认执行','您确定要执行该操作吗？',handleUnTask);
									function handleUnTask(btn){
										if(btn == 'yes'){
											var view = Ext.widget('untaskschedule'), 
											    unTaskScheduleStore = Ext.data.StoreManager.lookup('unTaskScheduleStore'); 
								            unTaskScheduleStore.getProxy().extraParams['criteria.poolId'] = poolId;
								            unTaskScheduleStore.getProxy().extraParams['criteria.triggerId'] = triggerId;
						                    view.loadView();  
						                    view.show();
										}
									}
								}
							},*/{
								text: '重新执行任务调度',
								handler: function (){
									Ext.MessageBox.confirm('确认执行','您确定要执行该操作吗？',handleReTask);
									function handleReTask(btn){
										if(btn == 'yes'){
											var view = Ext.widget('retaskschedule'), 
											    reTaskScheduleStore = Ext.data.StoreManager.lookup('reTaskScheduleStore'); 
								            reTaskScheduleStore.getProxy().extraParams['criteria.appId'] = appId;
								            reTaskScheduleStore.getProxy().extraParams['criteria.triggerId'] = triggerId;
						                    view.loadView();  
						                    view.show(); 
										}
									}
								}
							}]
					});
					menus.showAt(e.getXY());
				}
			},
			'taskscheduleinfoquery button[action=select]' : {
				'click' : function() {  
                    var grid = Ext.ComponentQuery.query('taskscheduleinfoquery > grid')[0],  
                        poolValue = grid.down('[name=app]').getValue(),
                        triggerValue = grid.down('[name=trigger]').getValue(),
                        manuallyCreatedValue = grid.down('[name=manuallyCreated]').getValue(),
                        store = Ext.getStore('taskScheduleInfoQueryStore'),
                        options = store.proxy.extraParams;
                        
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
                    options['criteria.appId'] = poolValue;
                    options['criteria.triggerId'] = triggerValue;
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
		Ext.create('Kira.store.TaskScheduleInfoQuery', {
			storeId : 'taskScheduleInfoQueryStore'
		});
		Ext.create('Kira.store.PoolStore', {
			storeId : 'poolStore'
		});
		
		Ext.create('Kira.store.TriggerStore', {
			storeId : 'triggerStore'
		});
		Ext.create('Kira.store.PauseTaskSchedule', {
			storeId : 'pauseTaskScheduleStore'
		});
		Ext.create('Kira.store.ResumeTaskSchedule', {
			storeId : 'resumeTaskScheduleStore'
		});
		Ext.create('Kira.store.UnTaskSchedule', {
			storeId : 'unTaskScheduleStore'
		});
		Ext.create('Kira.store.ReTaskSchedule', {
			storeId : 'reTaskScheduleStore'
		});
		Ext.create('Kira.store.TargetTriggerStore', {
			storeId : 'targetTriggerStore'
		});
		Ext.create('Kira.store.ManuallyCreatedStore', {
			storeId : 'manuallyCreatedStore'
		});
	},

	/**
	 * @Override
	 */
	loadView : function() {
		
	}

});