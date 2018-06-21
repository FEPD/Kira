Ext.define('Kira.controller.TaskExecuteMgr', {
	extend : 'Ext.app.Controller',
	views : ['execute.TaskExecuteMgr','execute.TaskStatusChange','execute.TaskExecuteDetail',
	'execute.RepeatRunTask','execute.CancelExecuteTask','execute.subtask.SubTaskStateQuery',
	'execute.subtask.CancelExecuteTaskParam'],
	stores : ['TaskExecuteMgr'],
	
	/**
	 * @Override
	 */
	init : function(application) {
		var me = this;
		this.control({
			'taskexecutemgr' : {
				'beforerender' : function(view) {
					var store = Ext.getStore('taskExecuteMgrStore'), 
					    options = store.proxy.extraParams;
			        if(options['criteria.appId']) delete options['criteria.appId'];
					if(options['criteria.triggerId']) delete options['criteria.triggerId'];
					if(options['criteria.jobStatusId']) delete options['criteria.jobStatusId'];
					if(options['criteria.id']) delete options['criteria.id'];
					if(options['criteria.jobItemId']) delete options['criteria.jobItemId'];
					if(options['criteria.createTimeStartAsString']) delete options['criteria.createTimeStartAsString'];
					if(options['criteria.createTimeEndAsString']) delete options['criteria.createTimeEndAsString'];
			        if(store.fireEvent('beforeload',store,options) !== false){
			        	//store.loadPage(1);//加载首页数据
			        }
			        //清除triggerStore中的缓存数据
			        var triggerStore = Ext.data.StoreManager.lookup('triggerStore'); 
			        if(triggerStore.totalCount !== undefined){
			        	triggerStore.removeAll();
			        }
			        //清除store中缓存的数据
			        if(store.totalCount !== undefined){
			        	store.removeAll();
			        }
                    view.loadView(); 
				}
			},
			'taskexecutemgr combo[name = app]' : {
				'select' : function(obj,records,options) {
					/*var grid = ('taskexecutemgr > grid')[0],
					    triggerObj = grid.down('[name="trigger"]');*/
					var form = Ext.ComponentQuery.query('taskexecutemgr > form')[0],
					    triggerObj = form.down('[name="trigger"]');
					triggerObj.clearValue(); 
		            var triggerStore = Ext.data.StoreManager.lookup('triggerStore'); 
		            triggerStore.getProxy().extraParams['criteria.appId'] = obj.getValue();
		            triggerStore.load();
				}
			},
			'taskexecutemgr actioncolumn[id=taskExecuteMgrIcon]' : {
				click : function(view,item,rowIndex,colIndex,e){
					var store = Ext.getStore('taskExecuteMgrStore'),
					    record = store.getAt(rowIndex),
					    jobId = record.get('id'),
					    menus = Ext.create('Ext.menu.Menu',{
							items : [{
								text: '状态变化查询',
								handler: function (){
									var view = Ext.widget('taskstatuschange'),
									    taskStatusChangeStore = Ext.data.StoreManager.lookup('taskStatusChangeStore'); 
						            taskStatusChangeStore.getProxy().extraParams['criteria.jobId'] = jobId;
				                    view.loadView();  
				                    view.show();  
								}
							},{
								text: '执行明细',
								handler : function(){
									var view = Ext.widget('taskexecutedetail'),
									    taskExecuteDetailStore = Ext.data.StoreManager.lookup('taskExecuteDetailStore'); 
						            taskExecuteDetailStore.getProxy().extraParams['criteria.jobId'] = jobId;
				                    view.loadView();  
				                    view.show();  
								}
							},{
								text: '重跑任务',
								handler : function(){
									Ext.MessageBox.confirm('确认执行','您确定要执行该操作吗？',handleRepeatTask);
									function handleRepeatTask(btn){
										if(btn == 'yes'){
											var view = Ext.widget('repeatruntask'),
											    repeatRunTaskStore = Ext.data.StoreManager.lookup('repeatRunTaskStore'); 
								            repeatRunTaskStore.getProxy().extraParams['criteria.id'] = jobId;
						                    view.loadView();  
						                    view.show();
										}
									}
								}
							},{
								text: '取消执行任务',
								handler : function(){
									var view = Ext.widget('cancelexecutetaskparam');
									me._recordId = jobId;//保存recordId，传给后台使用    
									view.loadView();  
                                    view.show();
								}
							}]
						});
						menus.showAt(e.getXY());
				}
			},
			'cancelexecutetaskparam button[action=save]' : {
				'click' : function(button,event, opt){
					var view = Ext.widget('cancelexecutetask'), 
					    jobId = me._recordId,
					    win = button.up('window'), 
                        form = win.down('form'),
                        f = form.getForm(),
					    cancelJobJsonMapString = f.findField('criteria.cancelJobJsonMapString').getValue(),
					    cancelExecuteTaskStore = Ext.data.StoreManager.lookup('cancelExecuteTaskStore'); 
					win.close();
		            cancelExecuteTaskStore.getProxy().extraParams['criteria.id'] = jobId;
		            cancelExecuteTaskStore.getProxy().extraParams['criteria.cancelJobJsonMapString'] = cancelJobJsonMapString;
                    view.loadView();  
                    view.show(); 
				}
			},
			'cancelexecutetaskparam button[action=close]' : {
				'click' : function(button,event, opt){
					var win = button.up('window');
					win.close();
				}
			},
			'taskexecutemgr button[action=select]' : {
				'click' : function() {  
                    var form = Ext.ComponentQuery.query('taskexecutemgr > form')[0],
                        poolValue = form.down('[name=app]').getValue(),
                        triggerValue = form.down('[name=trigger]').getValue(),
                        jobStatusValue = form.down('[name=status]').getValue(),
                        jobIdValue = form.down('[name=jobId]').getValue(),
                        jobItemIdValue = form.down('[name=jobItemId]').getValue(),
                        createTimeFromValue = form.down('[name=createTimeFrom]').getValue(),
                        createTimeToValue = form.down('[name=createTimeTo]').getValue(),
                        store = Ext.getStore('taskExecuteMgrStore'),
                        options = store.proxy.extraParams;

					if(Ext.isEmpty(jobIdValue) && Ext.isEmpty(jobItemIdValue)) {
						if(Ext.isEmpty(createTimeFromValue) && Ext.isEmpty(createTimeToValue)) {
							Ext.MessageBox.alert("提示", "过滤条件:\"创建时间\" 不能为空，因为数据量很大，请设置合理的时间区间长度，否则可能出现查询超时导致页面无数据显示。");
							return;
						}
					}

					if(!Ext.isEmpty(createTimeFromValue)){
                    	createTimeFromValue = Ext.util.Format.date(createTimeFromValue,'Y-m-d H:i:s');
                    	options['criteria.createTimeStartAsString'] = createTimeFromValue;
                    }else{
                    	delete options['criteria.createTimeStartAsString'];
                    }
                    if(!Ext.isEmpty(createTimeToValue)){
                    	createTimeToValue = Ext.util.Format.date(createTimeToValue,'Y-m-d H:i:s');
                    	options['criteria.createTimeEndAsString'] = createTimeToValue;
                    }else{
                    	delete options['criteria.createTimeEndAsString'];
                    }
                    options['criteria.appId'] = poolValue;
                    options['criteria.triggerId'] = triggerValue;
                    options['criteria.jobStatusId'] = jobStatusValue;
                    options['criteria.id'] = jobIdValue;
                    options['criteria.jobItemId'] = jobItemIdValue;
                    //options['criteria.createTimeStartAsString'] = createTimeFromValue;
                    //options['criteria.createTimeEndAsString'] = createTimeToValue;
                    if(store.fireEvent('beforeload',store,options) !== false){
                    	store.loadPage(1);
                    }
                } 
			},
			'taskexecutemgr button[action=reset]' : {
				'click' : function(){
					var form = Ext.ComponentQuery.query('taskexecutemgr > form')[0],
					    store = Ext.getStore('taskExecuteMgrStore');
					form.getForm().reset();
					//清除store中缓存的数据
			        if(store.totalCount !== undefined){
			        	store.removeAll();
			        }
				}
			},
			'taskexecutedetail actioncolumn[id=taskExecuteDetailIcon]' : {
				click : function(view,item,rowIndex,colIndex,e){
					var store = Ext.getStore('taskExecuteDetailStore'),
					    record = store.getAt(rowIndex),
					    id = record.get('id'),
					    menus = Ext.create('Ext.menu.Menu',{
							items : [{
								text: '子任务状态查询',
								handler: function (){
									var view = Ext.widget('subtaskstatequery'),
									    subTaskStateQueryStore = Ext.data.StoreManager.lookup('subTaskStateQueryStore'); 
						            subTaskStateQueryStore.getProxy().extraParams['criteria.jobItemId'] = id;
				                    view.loadView();  
				                    view.show();  
								}
							}]
						});
						menus.showAt(e.getXY());
				}
			}
		});
	},
	
	/**
	 * @Override
	 */
	loadModel : function() {
		Ext.create('Kira.store.TaskExecuteMgr', {
			storeId : 'taskExecuteMgrStore'
		});
		Ext.create('Kira.store.PoolStore', {
			storeId : 'poolStore'
		});
		
		Ext.create('Kira.store.TriggerStore', {
			storeId : 'triggerStore'
		});
		Ext.create('Kira.store.StatusStore', {
			storeId : 'statusStore'
		});
		Ext.create('Kira.store.TaskStatusChange', {
			storeId : 'taskStatusChangeStore'
		});
		Ext.create('Kira.store.TaskExecuteDetail', {
			storeId : 'taskExecuteDetailStore'
		});
		Ext.create('Kira.store.RepeatRunTask', {
			storeId : 'repeatRunTaskStore'
		});
		Ext.create('Kira.store.CancelExecuteTask', {
			storeId : 'cancelExecuteTaskStore'
		});
		Ext.create('Kira.store.SubTaskStateQuery', {
			storeId : 'subTaskStateQueryStore'
		});
	},

	/**
	 * @Override
	 */
	loadView : function() {}

});