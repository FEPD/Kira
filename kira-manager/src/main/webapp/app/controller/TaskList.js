Ext.define('Kira.controller.TaskList', {
	extend : 'Ext.app.Controller',
	views : ['task.TaskList','task.ExecuteEnvInfoQuery',
	'task.TaskScheduleInfo','task.ManuallyRunJobOnlyOne',
	'task.subtask.DeleteExecuteEnv','task.UpdateTaskConfig',
	'task.CreateTaskConfig','task.TaskServiceMethodCalledInfo','task.KiraTimerTriggerBusinessRunningInstanceListInfoQuery',
	'task.subtask.QueryKiraClientInfo'],
	stores : ['TaskList'],
	
	/**
	 * @Override
	 */
	init : function(application) {
		var me = this;
		this.control({
			'tasklist' : {
				'beforerender' : function(view) {
					var store = Ext.getStore('taskStore'), 
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
			'tasklist combo[name = app]' : {
				'select' : function(obj,records,options){
					var grid = Ext.ComponentQuery.query('tasklist > grid')[0],
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
			'updatetaskconfig combo[name = "triggerMetadata.triggerType"]' : {
				'select' : function(obj,records,options) {
			        var MisfireInstructionStore = Ext.data.StoreManager.lookup('commonStore'); 
			        MisfireInstructionStore.getProxy().extraParams['criteria.triggerType'] = obj.getValue();
			        MisfireInstructionStore.load(); 
				}
			},
			'tasklist actioncolumn[id=taskListIcon]' : {
				click : function(view,item,rowIndex,colIndex,e){
					var store = Ext.getStore('taskStore'),
					    record = store.getAt(rowIndex),
					    appId = record.get('appId'),
					    triggerId = record.get('triggerId'),
					    version = record.get('version'),
					    manuallyCreated = record.get('manuallyCreated'),
					    targetAppId = record.get('targetAppId'),
					    menus = Ext.create('Ext.menu.Menu',{
					    	items : [{
					    		text: '执行环境信息查询',
					    		handler: function (){
									var view = Ext.widget('executeenvinfoquery'),
									    executeEnvInfoQueryStore = Ext.data.StoreManager.lookup('executeEnvInfoQueryStore'); 
						            executeEnvInfoQueryStore.getProxy().extraParams['criteria.appId'] = appId;
						            executeEnvInfoQueryStore.getProxy().extraParams['criteria.triggerId'] = triggerId;
				                    view.loadView();  
				                    view.show();  
								}
					    	},{
					    		text: '任务调度信息查询',
								handler: function (){
									var view = Ext.widget('taskscheduleinfo'), 
									    taskScheduleInfoStore = Ext.data.StoreManager.lookup('taskScheduleInfoStore'); 
						            taskScheduleInfoStore.getProxy().extraParams['criteria.appId'] = appId;
						            taskScheduleInfoStore.getProxy().extraParams['criteria.triggerId'] = triggerId;
				                    view.loadView();  
				                    view.show();  
								}
					    	},{
					    		text: '手动执行一次调度任务',
								handler: function (){
									Ext.MessageBox.confirm('确认执行','您确定要执行该操作吗？',handleManuallyTask);
									function handleManuallyTask(btn){
										if(btn == 'yes'){
											var view = Ext.widget('manuallyrunjobonlyone'), 
											    manuallyRunJobOnlyOneStore = Ext.data.StoreManager.lookup('manuallyRunJobOnlyOneStore'); 
								            manuallyRunJobOnlyOneStore.getProxy().extraParams['criteria.appId'] = appId;
								            manuallyRunJobOnlyOneStore.getProxy().extraParams['criteria.triggerId'] = triggerId;
						                    view.loadView();  
						                    view.show(); 
										}
									}
								}
					    	},{
					    		text: '创建定时任务配置',
								handler: function (){
									var view = Ext.widget('createtaskconfig');
									//清除targetTriggerStore中缓存的数据
				                    var targetTriggerStore = Ext.data.StoreManager.lookup('targetTriggerStore'); 
				                    if(targetTriggerStore.totalCount !== undefined){
							        	targetTriggerStore.removeAll();
							        }
				                    view.loadView();  
				                    view.show();  
								}
					    	},{
					    		text: '更新定时任务配置',
					    		handler: function (){
					    			var view = Ext.widget('updatetaskconfig'), 
									    id = record.get('id');
									me._recordId = id;//保存recordId，传给后台使用
				                    view.loadView();  
				                    view.show();
				                    //清除targetTriggerStore中缓存的数据
				                    var targetTriggerStore = Ext.data.StoreManager.lookup('targetTriggerStore'); 
				                    if(targetTriggerStore.totalCount !== undefined){
							        	targetTriggerStore.removeAll();
							        }
					    			//初始化漏执行策略下拉列表
				                    var MisfireInstructionStore = Ext.data.StoreManager.lookup('commonStore'); 
								    MisfireInstructionStore.getProxy().extraParams['criteria.triggerType'] = record.get('triggerType');
								    MisfireInstructionStore.load(); 
								    //ajax request
								    Ext.Ajax.request({
								    	method : 'POST',
								    	url : 'triggerMetadata/getTriggerMetadataById.action',
										async : true,
										params : {
											'criteria.id' : id
										},
										success : function(response, options){
											var resp = Ext.decode(response.responseText),
											    resultData = resp['resultData'],
											    manuallyCreatedToStringValue = '',
											    basicForm = view.down('form').getForm();
											if(resp && resultData){
												if(resultData['scheduledLocally'] === true){
													var btnObj = Ext.ComponentQuery.query('updatetaskconfig button[action=save]')[0];
												    btnObj.setDisabled(true);
												}
												if(resultData['manuallyCreated'] === false){
													manuallyCreatedToStringValue = '否';
												}else if(resultData['manuallyCreated'] === true){
													manuallyCreatedToStringValue = '是';
												}
												//deal with manuallyCreated logic
								    		    if(resultData['manuallyCreated'] === true){
								    		    	var executeMethod = basicForm.findField('triggerMetadata.targetMethod'),
								    		    	    methodArgType = basicForm.findField('triggerMetadata.targetMethodArgTypes');
								    		    	executeMethod.setDisabled(true);
								    		    	methodArgType.setDisabled(true);
								    		    }else if(resultData['manuallyCreated'] === false){
								    		    	basicForm.findField('triggerMetadata.targetAppId').setReadOnly(true);
								    		    	basicForm.findField('triggerMetadata.targetAppId').addClass('x-item-disabled');
								    		    	basicForm.findField('triggerMetadata.targetTriggerId').setReadOnly(true);
								    		    	basicForm.findField('triggerMetadata.targetTriggerId').addClass('x-item-disabled');
								    		    }
								    		    
												basicForm.findField('triggerMetadata.appId').setValue(resultData['appId']);
												basicForm.findField('triggerMetadata.triggerId').setValue(resultData['triggerId']);
												basicForm.findField('triggerMetadata.version').setValue(resultData['version']);
												basicForm.findField('triggerMetadata.triggerType').setValue(resultData['triggerType']);
												basicForm.findField('triggerMetadata.manuallyCreated').setValue(manuallyCreatedToStringValue);
												basicForm.findField('triggerMetadata.manuallyCreated').setDisabled(true);
												basicForm.findField('triggerMetadata.cronExpression').setValue(resultData['cronExpression']);
												basicForm.findField('triggerMetadata.startTimeAsString').setValue(resultData['startTimeAsString']);
												basicForm.findField('triggerMetadata.endTimeAsString').setValue(resultData['endTimeAsString']);
												basicForm.findField('triggerMetadata.startDelay').setValue(resultData['startDelay']);
												basicForm.findField('triggerMetadata.repeatInterval').setValue(resultData['repeatInterval']);
												basicForm.findField('triggerMetadata.runTimeThreshold').setValue(resultData['runTimeThreshold']);
												basicForm.findField('triggerMetadata.repeatCount').setValue(resultData['repeatCount']);
												basicForm.findField('triggerMetadata.priority').setValue(resultData['priority']);
												basicForm.findField('triggerMetadata.requestsRecovery').setValue(resultData['requestsRecovery']);
												basicForm.findField('triggerMetadata.targetAppId').setValue(resultData['targetAppId']);
												basicForm.findField('triggerMetadata.targetTriggerId').setValue(resultData['targetTriggerId']);
												basicForm.findField('triggerMetadata.asynchronous').setValue(resultData['asynchronous']);
												basicForm.findField('triggerMetadata.onlyRunOnSingleProcess').setValue(resultData['onlyRunOnSingleProcess']);
												basicForm.findField('triggerMetadata.locationsToRunJob').setValue(resultData['locationsToRunJob']);
												basicForm.findField('triggerMetadata.limitToSpecifiedLocations').setValue(resultData['limitToSpecifiedLocations']);
												basicForm.findField('triggerMetadata.concurrent').setValue(resultData['concurrent']);
												basicForm.findField('triggerMetadata.copyFromMasterToSlaveZone').setValue(resultData['copyFromMasterToSlaveZone']);
												basicForm.findField('triggerMetadata.onlyScheduledInMasterZone').setValue(resultData['onlyScheduledInMasterZone']);
												basicForm.findField('triggerMetadata.scheduledLocally').setValue(resultData['scheduledLocally']);
												basicForm.findField('triggerMetadata.disabled').setValue(resultData['disabled']);
												basicForm.findField('triggerMetadata.targetMethod').setValue(resultData['targetMethod']);
												basicForm.findField('triggerMetadata.targetMethodArgTypes').setValue(resultData['targetMethodArgTypes']);
												basicForm.findField('triggerMetadata.argumentsAsJsonArrayString').setValue(resultData['argumentsAsJsonArrayString']);
												basicForm.findField('triggerMetadata.misfireInstruction').setValue(resultData['misfireInstruction']);
												basicForm.findField('triggerMetadata.description').setValue(resultData['description']);
												basicForm.findField('triggerMetadata.jobDispatchTimeoutEnabled').setValue(resultData['jobDispatchTimeoutEnabled']);
												basicForm.findField('triggerMetadata.jobDispatchTimeout').setValue(resultData['jobDispatchTimeout']);
												//初始化targetTriggerStore的下拉列表
										        targetTriggerStore.getProxy().extraParams['criteria.appId'] = resultData['targetAppId'];
									            targetTriggerStore.getProxy().extraParams['criteria.manuallyCreated'] = 'false';
									            targetTriggerStore.load();
												//初始化执行方法和方法参数类型，由targetPoolId决定其联动
												var poolObj = basicForm.findField('triggerMetadata.targetAppId'),
												    triggerObj = basicForm.findField('triggerMetadata.targetTriggerId'),
												    executeMethod = basicForm.findField('triggerMetadata.targetMethod'),
												    methodArgType = basicForm.findField('triggerMetadata.targetMethodArgTypes');
												
												//poolId、triggerId要么同时为空，要么同时非空。非空时，能唯一确定一条记录
												if(!Ext.isEmpty(poolObj.getValue()) && !Ext.isEmpty(triggerObj.getValue())){
													Ext.Ajax.request({
														method : 'POST',
														url : 'triggerMetadata/listLatestOnPage.action',
														async : true,
														params : {
															'criteria.appId' : poolObj.getValue(),
															'criteria.triggerId' : triggerObj.getValue()
														},
														success : function(response, options){
															var resp = Ext.decode(response.responseText),
																resultData = resp['resultData'];
																if(Ext.isEmpty(resultData)){
																	Ext.MessageBox.show({
											    		    			title: '警告',
																        msg: '被调用的定时任务不可用!',
																        buttons: Ext.MessageBox.OK,
																        icon: Ext.MessageBox.WARNING
											    		    		});
																	executeMethod.setValue('');
																	methodArgType.setValue('');
																	return;
																}
																if(!Ext.isEmpty(resultData) && (resultData.length > 0)){
																	executeMethod.setValue(resultData[0]['targetMethod']);
																	methodArgType.setValue(resultData[0]['targetMethodArgTypes']);
																}
														},
														failure : function(response, options){
															Ext.MessageBox.alert('提示','加载超时或连接错误！');
														}
													});
												}
											}
										},
										failure : function(response, options){
											Ext.MessageBox.alert('提示','加载超时或连接错误！');
										}
								    });
					    		}
					    	},{
					    		text: '删除定时任务配置',
								handler: function (){
									/*
									if(manuallyCreated !== true){
										Ext.MessageBox.show({
				    		    			title: '警告',
									        msg: '只能删除手动创建的定时任务配置!',
									        buttons: Ext.MessageBox.OK,
									        icon: Ext.MessageBox.WARNING
				    		    		});
				    		    		return;
									}*/
									Ext.MessageBox.confirm('确认删除','您确定要删除该行记录吗？',handleDeleteTask);
									function handleDeleteTask(btn){
										if(btn == 'yes'){
											Ext.Ajax.request({
												method : 'POST',
										    	url : 'triggerMetadata/deleteTrigger.action',
												async : true,
												params : {
													'criteria.appId' : appId,
													'criteria.triggerId' : triggerId,
													'criteria.version' : version
												},
												success : function(response, options){
													var resp = Ext.decode(response.responseText)
													    resultDataObj = resp['resultData'],
													    resultCode = resultDataObj['resultCode'],
													    resultData = resultDataObj['resultData'];
													if(resultCode == 0){
												    	Ext.MessageBox.show({
								    		    			title: '提示',
													        msg: '删除成功!',
													        buttons: Ext.MessageBox.OK,
													        icon: Ext.MessageBox.INFO
								    		    		});
												    	Ext.getStore('taskStore').reload();
													}else if(resultCode == 1){
												    	Ext.MessageBox.show({
								    		    			title: '警告',
													        msg: '部分删除成功!',
													        buttons: Ext.MessageBox.OK,
													        icon: Ext.MessageBox.WARNING
								    		    		});
												    	Ext.getStore('taskStore').reload();
												    }else if(resultCode == 2){
												    	Ext.MessageBox.show({
								    		    			title: '错误',
													        msg: '删除失败，'+resultData,
													        buttons: Ext.MessageBox.OK,
													        icon: Ext.MessageBox.ERROR
								    		    		});
												    }
													    
												},
												failure : function(response, options){
													Ext.MessageBox.alert('提示','加载超时或连接错误！');
												}
											});
										}
									}
								}
					    	},{
					    		text: '定时任务的服务方法被调用信息',
								handler: function (){
									var view = Ext.widget('taskservicemethodcalledinfo'),
									    taskServiceMethodCalledInfoStore = Ext.data.StoreManager.lookup('taskServiceMethodCalledInfoStore');
									taskServiceMethodCalledInfoStore.getProxy().extraParams['criteria.appId'] = appId;
									taskServiceMethodCalledInfoStore.getProxy().extraParams['criteria.triggerId'] = triggerId;
				                    view.loadView();  
				                    view.show();  
								}
					    	},{
								text: '查询定时任务对应的业务方法当前执行实例列表',
								handler: function (){
									var view = Ext.widget('kiraTimerTriggerBusinessRunningInstanceListInfoQuery'),
										kiraTimerTriggerBusinessRunningInstanceListInfoQueryStore = Ext.data.StoreManager.lookup('kiraTimerTriggerBusinessRunningInstanceListInfoQueryStore');
									kiraTimerTriggerBusinessRunningInstanceListInfoQueryStore.getProxy().extraParams['criteria.appId'] = appId;
									kiraTimerTriggerBusinessRunningInstanceListInfoQueryStore.getProxy().extraParams['criteria.triggerId'] = triggerId;
									view.loadView();
									view.show();
								}
							}]
					    });
					menus.showAt(e.getXY());
				}
			},
			'tasklist button[action=select]' : {
				'click' : function() {  
                    var grid = Ext.ComponentQuery.query('tasklist > grid')[0], 
                        poolValue = grid.down('[name=app]').getValue(),
                        triggerValue = grid.down('[name=trigger]').getValue(),
                        manuallyCreatedValue = grid.down('[name=manuallyCreated]').getValue(),
                        store = Ext.getStore('taskStore'),
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
			},
			'executeenvinfoquery actioncolumn[id=executeEnvInfoQueryIcon]' : {
				click : function(view,item,rowIndex,colIndex,e){
					var store = Ext.getStore('executeEnvInfoQueryStore'),
					    record = store.getAt(rowIndex),
					    path = record.get('triggerEnvironmentZKFullPath'),
					    serviceUrl = record.get('serviceUrl'),
					    appId = record.get('appId'),
					    targetAppId = record.get('targetAppId'),
					    menus = Ext.create('Ext.menu.Menu',{
							items : [{
								text: '删除执行环境',
								handler: function (){
									var grid = Ext.ComponentQuery.query('tasklist > grid')[0],
									    manuallyCreatedValue = grid.down('[name=manuallyCreated]').getValue();
									
									if(manuallyCreatedValue == '是'){
										Ext.MessageBox.show({
				    		    			title: '警告',
									        msg: '不能删除手动创建的定时任务执行环境!',
									        buttons: Ext.MessageBox.OK,
									        icon: Ext.MessageBox.WARNING
				    		    		});
				    		    		return;
									}
									if(appId != targetAppId && !Ext.isEmpty(targetAppId)){
										Ext.MessageBox.show({
				    		    			title: '警告',
									        msg: '只能删除自己Pool的定时任务执行环境!',
									        buttons: Ext.MessageBox.OK,
									        icon: Ext.MessageBox.WARNING
				    		    		});
				    		    		return;
									}
									Ext.MessageBox.confirm('确认删除','您确定要删除该行记录吗？',handleDelete);
									function handleDelete(btn){
										if(btn == 'yes'){
											var view = Ext.widget('deleteexecuteenv'),
											    deleteExecuteEnvStore = Ext.data.StoreManager.lookup('deleteExecuteEnvStore'); 
								            deleteExecuteEnvStore.getProxy().extraParams['criteria.triggerEnvironmentZKFullPath'] = path;
								            Ext.getStore('executeEnvInfoQueryStore').loadPage(1);  
						                    view.loadView();  
						                    view.show();  
										}
									}
								}
							},{
								text: '获取Kira客户端当前信息',
								handler: function (){
									var view = Ext.widget('querykiraclientinfo'),
									    queryKiraClientInfoStore = Ext.data.StoreManager.lookup('queryKiraClientInfoStore');
									queryKiraClientInfoStore.getProxy().extraParams['criteria.centralScheduleServiceUrl'] = serviceUrl;
									view.loadView();  
						            view.show();
								}
							}]
						});
						menus.showAt(e.getXY());
				}
			},
			'updatetaskconfig combo[name = "triggerMetadata.targetAppId"]' : {
				'select' : function(obj,records,options){
					var form = Ext.ComponentQuery.query('updatetaskconfig > form')[0], 
                        targetTriggerObj = form.down('[name="triggerMetadata.targetTriggerId"]');
                    targetTriggerObj.clearValue();
		            var targetTriggerStore = Ext.data.StoreManager.lookup('targetTriggerStore'); 
		            targetTriggerStore.getProxy().extraParams['criteria.appId'] = obj.getValue();
		            targetTriggerStore.getProxy().extraParams['criteria.manuallyCreated'] = 'false';
		            targetTriggerStore.load();
				}
			},
			'updatetaskconfig combo[name = "triggerMetadata.targetTriggerId"]' : {
				'select' : function(obj,records,options){
					var form = Ext.ComponentQuery.query('updatetaskconfig > form')[0],
					    poolObj = form.down('[name="triggerMetadata.targetAppId"]'),
					    triggerObj = form.down('[name="triggerMetadata.targetTriggerId"]'),
					    executeMethod = form.down('[name="triggerMetadata.targetMethod"]'),
					    methodArgType = form.down('[name="triggerMetadata.targetMethodArgTypes"]')
					   
					Ext.Ajax.request({
						method : 'POST',
						url : 'triggerMetadata/listLatestOnPage.action',
						async : true,
						params : {
							'criteria.appId' : poolObj.getValue(),
							'criteria.triggerId' : triggerObj.getValue()
						},
						success : function(response, options){
							var resp = Ext.decode(response.responseText),
								resultData = resp['resultData'];
								if(Ext.isEmpty(resultData)){
									Ext.MessageBox.show({
			    		    			title: '警告',
								        msg: '被调用的定时任务不可用!',
								        buttons: Ext.MessageBox.OK,
								        icon: Ext.MessageBox.WARNING
			    		    		});
									executeMethod.setValue('');
									methodArgType.setValue('');
									return;
								}
								if(!Ext.isEmpty(resultData) && (resultData.length > 0)){
									executeMethod.setValue(resultData[0]['targetMethod']);
									methodArgType.setValue(resultData[0]['targetMethodArgTypes']);
								}
						},
						failure : function(response, options){
							Ext.MessageBox.alert('提示','加载超时或连接错误！');
						}
					});
				}
			},
			'updatetaskconfig button[action=save]' : {
				'click' : function(button,event, opt) {
                    var win = button.up('window'), 
                        form = win.down('form'),
                        f = form.getForm(),
                        id = me._recordId,
                        repeatInterval = f.findField('triggerMetadata.repeatInterval').getValue(),
		    		    cronExpression = f.findField('triggerMetadata.cronExpression').getValue(),
		    		    startDelay = f.findField('triggerMetadata.startDelay').getValue(),
		    		    repeatCount = f.findField('triggerMetadata.repeatCount').getValue(),
		    		    runTimeThreshold = f.findField('triggerMetadata.runTimeThreshold').getValue(),
		    		    triggerType = f.findField('triggerMetadata.triggerType').getValue(),
		    		    targetAppId = f.findField('triggerMetadata.targetAppId').getValue(),
		    		    targetTriggerId = f.findField('triggerMetadata.targetTriggerId').getValue(),
		    		    limitToSpecifiedLocations = f.findField('triggerMetadata.limitToSpecifiedLocations').getValue(),
		    		    manuallyCreated = f.findField('triggerMetadata.manuallyCreated').getValue(),
						description = f.findField('triggerMetadata.description').getValue();;
		    		  
		    		// 提交后台处理 
                	if(f.isValid()){
                		if(Ext.isEmpty(limitToSpecifiedLocations)){
                			Ext.MessageBox.show({
		    		    			title: '警告',
							        msg: '是否只在指定的执行地点执行任务不能为空！',
							        buttons: Ext.MessageBox.OK,
							        icon: Ext.MessageBox.WARNING
		    		    		});
		    		    		return;
                		}
                		if(manuallyCreated == '是'){
                			if(Ext.isEmpty(targetAppId) || Ext.isEmpty(targetTriggerId)){
                				Ext.MessageBox.show({
		    		    			title: '警告',
							        msg: '手动创建的定时任务，被调用的AppId和定时任务Id必须同时为非空！',
							        buttons: Ext.MessageBox.OK,
							        icon: Ext.MessageBox.WARNING
		    		    		});
		    		    		return;
                			}
                		}
                		if(Ext.isEmpty(targetAppId)){
                			if(!Ext.isEmpty(targetTriggerId)){
                				Ext.MessageBox.show({
		    		    			title: '警告',
							        msg: '被调用的AppId和定时任务Id必须同时为空！',
							        buttons: Ext.MessageBox.OK,
							        icon: Ext.MessageBox.WARNING
		    		    		});
		    		    		return;
                			}
                		}else if(!Ext.isEmpty(targetAppId)){
                			if(Ext.isEmpty(targetTriggerId)){
                				Ext.MessageBox.show({
		    		    			title: '警告',
							        msg: '被调用的AppId和定时任务Id必须同时为非空！',
							        buttons: Ext.MessageBox.OK,
							        icon: Ext.MessageBox.WARNING
		    		    		});
		    		    		return;
                			}
                		}
                		if(triggerType == 'SimpleTrigger'){
		    		    	if(Ext.isEmpty(repeatInterval) || !Ext.isEmpty(cronExpression)){
		    		    		Ext.MessageBox.show({
		    		    			title: '警告',
							        msg: '执行间隔不能为空，cron表达式必须为空！',
							        buttons: Ext.MessageBox.OK,
							        icon: Ext.MessageBox.WARNING
		    		    		});
		    		    		return ;
		    		    	}
		    		    }else if(triggerType == 'CronTrigger'){
		    		    	if(Ext.isEmpty(cronExpression) || !Ext.isEmpty(startDelay) || !Ext.isEmpty(repeatCount) || !Ext.isEmpty(repeatInterval)){
		    		    		Ext.MessageBox.show({
		    		    			title: '警告',
							        msg: 'cron表达式不能为空，启动延迟、执行间隔和执行次数必须为空！',
							        buttons: Ext.MessageBox.OK,
							        icon: Ext.MessageBox.WARNING
		    		    		});
		    		    		return ;
		    		    	}
		    		    }
						if(Ext.isEmpty(description, false)){
							Ext.MessageBox.show({
								title: '警告',
								msg: '描述不能为空!',
								buttons: Ext.MessageBox.OK,
								icon: Ext.MessageBox.WARNING
							});
							return ;
						}
                		f.submit({
	                    	clientValidation : true,
							method : 'post',
							params : {
								'triggerMetadata.id' : id
							},
							url : 'triggerMetadata/updateTrigger.action',
							submitEmptyText : false,
							waitMsg : '数据保存中...',
							success : function(form, action){
								var resp = action.result.data,
								    resultCode = resp['resultCode'],
								    resultData = resp['resultData'];
								    if(resultCode == 0){
								    	win.close();
								    	Ext.MessageBox.show({
				    		    			title: '提示',
									        msg: '更新成功!',
									        buttons: Ext.MessageBox.OK,
									        icon: Ext.MessageBox.INFO
				    		    		});
								    	Ext.getStore('taskStore').reload();
								    }else if(resultCode == 1){
								    	win.close();
								    	Ext.MessageBox.show({
				    		    			title: '警告',
									        msg: '部分更新成功!',
									        buttons: Ext.MessageBox.OK,
									        icon: Ext.MessageBox.WARNING
				    		    		});
								    	Ext.getStore('taskStore').reload();
								    }else if(resultCode == 2){
								    	Ext.MessageBox.show({
				    		    			title: '错误',
									        msg: '更新失败，'+resultData,
									        buttons: Ext.MessageBox.OK,
									        icon: Ext.MessageBox.ERROR
				    		    		});
								    }
								    
							},
							failure : function(form, action){
								Ext.MessageBox.show({
		    		    			title: '错误',
							        msg: '提交失败!',
							        buttons: Ext.MessageBox.OK,
							        icon: Ext.MessageBox.ERROR
		    		    		});
							}
	                    });
                	}
                } 
			},
			'updatetaskconfig button[action=close]' : {
				'click' : function(button,event, opt){
					var win = button.up('window');
					win.close();
				}
			},
			'createtaskconfig combo[name = "newTriggerMetadata.triggerType"]' : {
				'select' : function(obj,records,options) {
			        var MisfireInstructionStore = Ext.data.StoreManager.lookup('commonStore'); 
			        MisfireInstructionStore.getProxy().extraParams['criteria.triggerType'] = obj.getValue();
			        MisfireInstructionStore.load(); 
				}
			},
			'createtaskconfig combo[name = "newTriggerMetadata.targetAppId"]' : {
				'select' : function(obj,records,options){
					var form = Ext.ComponentQuery.query('createtaskconfig > form')[0], 
                        targetTriggerObj = form.down('[name="newTriggerMetadata.targetTriggerId"]');
                    targetTriggerObj.clearValue();
		            var targetTriggerStore = Ext.data.StoreManager.lookup('targetTriggerStore'); 
		            targetTriggerStore.getProxy().extraParams['criteria.appId'] = obj.getValue();
		            targetTriggerStore.getProxy().extraParams['criteria.manuallyCreated'] = 'false';
		            targetTriggerStore.load();
				}
			},
			'createtaskconfig combo[name = "newTriggerMetadata.targetTriggerId"]' : {
				'select' : function(obj,records,options){
					var form = Ext.ComponentQuery.query('createtaskconfig > form')[0],
					    poolObj = form.down('[name="newTriggerMetadata.targetAppId"]'),
					    triggerObj = form.down('[name="newTriggerMetadata.targetTriggerId"]'),
					    executeMethod = form.down('[name="newTriggerMetadata.targetMethod"]'),
					    methodArgType = form.down('[name="newTriggerMetadata.targetMethodArgTypes"]')
					    
					Ext.Ajax.request({
						method : 'POST',
						url : 'triggerMetadata/listLatestOnPage.action',
						async : true,
						params : {
							'criteria.appId' : poolObj.getValue(),
							'criteria.triggerId' : triggerObj.getValue()
						},
						success : function(response, options){
							var resp = Ext.decode(response.responseText),
								resultData = resp['resultData'];
								if(Ext.isEmpty(resultData)){
									Ext.MessageBox.show({
			    		    			title: '警告',
								        msg: '被调用的定时任务不可用!',
								        buttons: Ext.MessageBox.OK,
								        icon: Ext.MessageBox.WARNING
			    		    		});
									executeMethod.setValue('');
									methodArgType.setValue('');
									return;
								}
								if(!Ext.isEmpty(resultData) && (resultData.length > 0)){
									executeMethod.setValue(resultData[0]['targetMethod']);
									methodArgType.setValue(resultData[0]['targetMethodArgTypes']);
								}
						},
						failure : function(response, options){
							Ext.MessageBox.alert('提示','加载超时或连接错误！');
						}
					});
				}
			},
			'createtaskconfig button[action=save]' : {
				'click' : function(button,event, opt){
					var win = button.up('window'),
					    form = win.down('form'),
					    f = form.getForm(),
                        id = me._recordId,
                        appId = f.findField('newTriggerMetadata.appId').getValue(),
                        triggerId = f.findField('newTriggerMetadata.triggerId').getValue(),
                        version = f.findField('newTriggerMetadata.version').getValue(),
                        repeatInterval = f.findField('newTriggerMetadata.repeatInterval').getValue(),
		    		    cronExpression = f.findField('newTriggerMetadata.cronExpression').getValue(),
		    		    startDelay = f.findField('newTriggerMetadata.startDelay').getValue(),
		    		    repeatCount = f.findField('newTriggerMetadata.repeatCount').getValue(),
		    		    runTimeThreshold = f.findField('newTriggerMetadata.runTimeThreshold').getValue(),
		    		    limitToSpecifiedLocations = f.findField('newTriggerMetadata.limitToSpecifiedLocations').getValue(),
		    		    triggerType = f.findField('newTriggerMetadata.triggerType').getValue(),
		    		    targetAppId = f.findField('newTriggerMetadata.targetAppId').getValue(),
		    		    targetTriggerId = f.findField('newTriggerMetadata.targetTriggerId').getValue(),
						description = f.findField('newTriggerMetadata.description').getValue();
		    		
		    		if(Ext.isEmpty(appId)){
                    	Ext.MessageBox.show({
    		    			title: '警告',
					        msg: 'AppId不能为空!',
					        buttons: Ext.MessageBox.OK,
					        icon: Ext.MessageBox.WARNING
    		    		});
    		    		return ;
                    }
                    if(Ext.isEmpty(triggerId)){
                    	Ext.MessageBox.show({
    		    			title: '警告',
					        msg: 'TriggerId不能为空!',
					        buttons: Ext.MessageBox.OK,
					        icon: Ext.MessageBox.WARNING
    		    		});
    		    		return ;
                    }
                    if(!Ext.isEmpty(appId) && Ext.isEmpty(Ext.util.Format.trim(appId))){
                    	Ext.MessageBox.show({
    		    			title: '警告',
					        msg: 'appId不能为空!',
					        buttons: Ext.MessageBox.OK,
					        icon: Ext.MessageBox.WARNING
    		    		});
    		    		return ;
                    }  
                    if(!Ext.isEmpty(triggerId) && Ext.isEmpty(Ext.util.Format.trim(triggerId))){
                    	Ext.MessageBox.show({
    		    			title: '警告',
					        msg: 'TriggerId不能为空!',
					        buttons: Ext.MessageBox.OK,
					        icon: Ext.MessageBox.WARNING
    		    		});
    		    		return ;
                    }
                    if(Ext.isEmpty(limitToSpecifiedLocations)){
            			Ext.MessageBox.show({
	    		    			title: '警告',
						        msg: '是否只在指定的执行地点执行任务不能为空！',
						        buttons: Ext.MessageBox.OK,
						        icon: Ext.MessageBox.WARNING
	    		    		});
	    		    		return;
                		}
					if(Ext.isEmpty(description, false)){
						Ext.MessageBox.show({
							title: '警告',
							msg: '描述不能为空!',
							buttons: Ext.MessageBox.OK,
							icon: Ext.MessageBox.WARNING
						});
						return ;
					}
		    		// 提交后台处理 
		    		if(f.isValid()){
            			if(Ext.isEmpty(targetAppId) || Ext.isEmpty(targetTriggerId)){
            				Ext.MessageBox.show({
	    		    			title: '警告',
						        msg: '手动创建的定时任务，被调用的appId和定时任务Id必须同时为非空！',
						        buttons: Ext.MessageBox.OK,
						        icon: Ext.MessageBox.WARNING
	    		    		});
	    		    		return;
            			}
		    			if(triggerType == 'SimpleTrigger'){
		    		    	if(Ext.isEmpty(repeatInterval) || !Ext.isEmpty(cronExpression)){
		    		    		Ext.MessageBox.show({
		    		    			title: '警告',
							        msg: '执行间隔不能为空，cron表达式必须为空！',
							        buttons: Ext.MessageBox.OK,
							        icon: Ext.MessageBox.WARNING
		    		    		});
		    		    		return ;
		    		    	}
		    		    }else if(triggerType == 'CronTrigger'){
		    		    	if(Ext.isEmpty(cronExpression) || !Ext.isEmpty(startDelay) || !Ext.isEmpty(repeatCount) || !Ext.isEmpty(repeatInterval)){
		    		    		Ext.MessageBox.show({
		    		    			title: '警告',
							        msg: 'cron表达式不能为空，启动延迟、执行间隔和执行次数必须为空！',
							        buttons: Ext.MessageBox.OK,
							        icon: Ext.MessageBox.WARNING
		    		    		});
		    		    		return ;
		    		    	}
		    		    }
		    		    f.submit({
	                    	clientValidation : true,
							method : 'post',
							//params : {'newTriggerMetadata.id' : id},
							url : 'triggerMetadata/createTrigger.action',
							submitEmptyText : false,
							waitMsg : '数据保存中...',
							success : function(form, action){
								var resp = action.result.data,
								    resultCode = resp['resultCode'],
								    resultData = resp['resultData'];
								    if(resultCode == 0){
								    	win.close();
								    	Ext.MessageBox.show({
				    		    			title: '提示',
									        msg: '创建成功!',
									        buttons: Ext.MessageBox.OK,
									        icon: Ext.MessageBox.INFO
				    		    		});
								    	Ext.getStore('taskStore').reload();
								    }else if(resultCode == 1){
								    	win.close();
								    	Ext.MessageBox.show({
				    		    			title: '警告',
									        msg: '部分创建成功!',
									        buttons: Ext.MessageBox.OK,
									        icon: Ext.MessageBox.WARNING
				    		    		});
								    	Ext.getStore('taskStore').reload();
								    }else if(resultCode == 2){
								    	Ext.MessageBox.show({
				    		    			title: '错误',
									        msg: '创建失败，'+resultData,
									        buttons: Ext.MessageBox.OK,
									        icon: Ext.MessageBox.ERROR
				    		    		});
								    }
								    
							},
							failure : function(form, action){
								Ext.MessageBox.show({
		    		    			title: '错误',
							        msg: '提交失败!',
							        buttons: Ext.MessageBox.OK,
							        icon: Ext.MessageBox.ERROR
		    		    		});
							}
	                    });
		    		}
		    		    
				}
			},
			'createtaskconfig button[action=close]' : {
				'click' : function(button,event, opt){
					var win = button.up('window');
					win.close();
				}
			}
		});
	},
	
	/**
	 * @Override
	 */
	loadModel : function() {
		Ext.create('Kira.store.TaskList', {
			storeId : 'taskStore'
		});
		Ext.create('Kira.store.PoolStore', {
			storeId : 'poolStore'
		});
		
		Ext.create('Kira.store.TriggerStore', {
			storeId : 'triggerStore'
		});
		Ext.create('Kira.store.ExecuteEnvInfoQuery', {
			storeId : 'executeEnvInfoQueryStore'
		});
		Ext.create('Kira.store.TaskScheduleInfo', {
			storeId : 'taskScheduleInfoStore'
		});
		Ext.create('Kira.store.ManuallyRunJobOnlyOne', {
			storeId : 'manuallyRunJobOnlyOneStore'
		});
		Ext.create('Kira.store.DeleteExecuteEnv', {
			storeId : 'deleteExecuteEnvStore'
		});
		Ext.create('Kira.store.TriggerTypeStore', {
			storeId : 'triggerTypeStore'
		});
		Ext.create('Kira.store.CommonStore', {
			storeId : 'commonStore'
		});
		Ext.create('Kira.store.ManuallyCreatedStore', {
			storeId : 'manuallyCreatedStore'
		});
		Ext.create('Kira.store.TaskServiceMethodCalledInfo', {
			storeId : 'taskServiceMethodCalledInfoStore'
		});
		Ext.create('Kira.store.KiraTimerTriggerBusinessRunningInstanceListInfoQuery', {
			storeId : 'kiraTimerTriggerBusinessRunningInstanceListInfoQueryStore'
		});
		Ext.create('Kira.store.QueryKiraClientInfo', {
			storeId : 'queryKiraClientInfoStore'
		});
		Ext.create('Kira.store.TargetPoolStore', {
			storeId : 'targetPoolStore'
		});
		Ext.create('Kira.store.TargetTriggerStore', {
			storeId : 'targetTriggerStore'
		});
	},

	/**
	 * @Override
	 */
	loadView : function() {
		
	}

});