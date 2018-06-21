Ext.define('Kira.controller.KiraClientConfigMgr', {
	extend : 'Ext.app.Controller',
	views : ['client.KiraClientConfigMgr','client.UpdateKiraClientConfig',
	'client.CreateKiraClientConfig','client.PoolServiceMethodCalledInfo'],
	stores : ['KiraClientConfigMgr'],
	/**
	 * @Override
	 */
	init : function(application) {
		var me = this;
		this.control({
			'kiraclientconfigmgr' : {
				'beforerender' : function(view) {
					var store = Ext.getStore('kiraClientConfigMgrStore'), 
					    options = store.proxy.extraParams;
					if(options['criteria.appId']) delete options['criteria.appId'];
					if(options['criteria.manuallyCreated']) delete options['criteria.manuallyCreated'];
			        if(store.fireEvent('beforeload',store,options) !== false){
			        	store.loadPage(1);//加载首页数据
			        }
                    view.loadView(); 
				}
			},
			'kiraclientconfigmgr button[action=select]' : {
				'click' : function() {
					var grid = Ext.ComponentQuery.query('kiraclientconfigmgr > grid')[0],
					    poolValue = grid.down('[name=app]').getValue(),
					    manuallyCreatedValue = grid.down('[name=manuallyCreated]').getValue(),
                        store = Ext.getStore('kiraClientConfigMgrStore'),
                        options = store.proxy.extraParams;
                    options['criteria.appId'] = poolValue;
                    //add manuallyCreated logic and validate the value
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
			'kiraclientconfigmgr actioncolumn[id=kiraClientConfigMgrIcon]' : {
				click : function(view,item,rowIndex,colIndex,e){
					var store = Ext.getStore('kiraClientConfigMgrStore'),
					    record = store.getAt(rowIndex),
					    id = record.get('id'),
					    appId = record.get('appId'),
					    manuallyCreated = record.get('manuallyCreated'),
					    menus = Ext.create('Ext.menu.Menu',{
					    	items : [{
					    		text: '创建Kira客户端配置信息',
					    		handler: function (){
					    			var view = Ext.widget('createkiraclientconfig');
					    			view.loadView();  
                                    view.show();
					    			    
					    		}
					    	},{
					    		text: '更新Kira客户端配置信息',
					    		handler: function (){
					    			var view = Ext.widget('updatekiraclientconfig'),
					    			    basicForm = view.down('form').getForm(),
					    			    updateKiraClientConfigModel = Ext.ModelManager.getModel('Kira.model.KiraClientConfigMgr'),
					    			    proxy = new Ext.data.proxy.Ajax({
									    	url : 'kiraClientMetadata/getKiraClientMetadataById.action'
									    });
									updateKiraClientConfigModel.setProxy(proxy);
									me._recordId = id;//保存recordId，传给后台使用
									updateKiraClientConfigModel.load(null,{
										params : {'criteria.id' : id},
										success : function(rec){
											var appId = rec['raw']['resultData']['appId'],
											    kiraClientVersion = rec['raw']['resultData']['kiraClientVersion'],
											    manuallyCreated = rec['raw']['resultData']['manuallyCreated'],
											    visibilityLimited = rec['raw']['resultData']['visibilityLimited'],
											    visibleForUsers = rec['raw']['resultData']['visibleForUsers'],
											    sendAlarmEmail = rec['raw']['resultData']['sendAlarmEmail'],
											    emailsToReceiveAlarm = rec['raw']['resultData']['emailsToReceiveAlarm'],
											    sendAlarmSMS = rec['raw']['resultData']['sendAlarmSMS'],
											    phoneNumbersToReceiveAlarmSMS = rec['raw']['resultData']['phoneNumbersToReceiveAlarmSMS'];
											    
											basicForm.findField('kiraClientMetadata.appId').setValue(appId);
											basicForm.findField('kiraClientMetadata.kiraClientVersion').setValue(kiraClientVersion);
											basicForm.findField('kiraClientMetadata.manuallyCreated').setValue(manuallyCreated === true?'是':'否');
											basicForm.findField('kiraClientMetadata.manuallyCreated').setDisabled(true);
											basicForm.findField('kiraClientMetadata.visibilityLimited').setValue(visibilityLimited);
											basicForm.findField('kiraClientMetadata.visibleForUsers').setValue(visibleForUsers);
											basicForm.findField('kiraClientMetadata.sendAlarmEmail').setValue(sendAlarmEmail);
											basicForm.findField('kiraClientMetadata.emailsToReceiveAlarm').setValue(emailsToReceiveAlarm);
											basicForm.findField('kiraClientMetadata.sendAlarmSMS').setValue(sendAlarmSMS);
											basicForm.findField('kiraClientMetadata.phoneNumbersToReceiveAlarmSMS').setValue(phoneNumbersToReceiveAlarmSMS);
										}
									});
					    			view.loadView();  
                                    view.show();
					    		}
					    	},{
					    		text: '删除Kira客户端配置信息',
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
					    			Ext.MessageBox.confirm('确认删除','您确定要删除该行记录吗？',handleDeleteKiraClient);
					    			function handleDeleteKiraClient(btn){
					    				if(btn == 'yes'){
					    					Ext.Ajax.request({
					    						method : 'POST',
										    	url : 'kiraClientMetadata/deleteKiraClientMetadata.action',
												async : true,
												params : {
													'criteria.appId' : appId
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
												    	Ext.getStore('kiraClientConfigMgrStore').reload();
													}else if(resultCode == 1){
												    	Ext.MessageBox.show({
								    		    			title: '警告',
													        msg: '部分删除成功!',
													        buttons: Ext.MessageBox.OK,
													        icon: Ext.MessageBox.WARNING
								    		    		});
												    	Ext.getStore('kiraClientConfigMgrStore').reload();
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
					    		text: 'App的服务方法被调用信息',
								handler: function (){
									var view = Ext.widget('poolservicemethodcalledinfo'),
									    poolServiceMethodCalledInfoStore = Ext.data.StoreManager.lookup('poolServiceMethodCalledInfoStore');
									poolServiceMethodCalledInfoStore.getProxy().extraParams['criteria.appId'] = appId;
				                    view.loadView();  
				                    view.show();  
								}
					    	}]
					    });
					    menus.showAt(e.getXY());
				}
			},
			'updatekiraclientconfig button[action=save]' : {
				'click' : function(button,event, opt) {
					var win = button.up('window'), 
                        form = win.down('form'),
                        f = form.getForm(),
                        id = me._recordId,
                        sendAlarmEmail = f.findField('kiraClientMetadata.sendAlarmEmail'),
                        sendAlarmSMS = f.findField('kiraClientMetadata.sendAlarmSMS'),
                        isSendAlarmEmail = false,
                        isSendAlarmSMS = false;
                        
                    sendAlarmEmail.inputValue = false;
                    sendAlarmSMS.inputValue = false;
                        
                    if(sendAlarmEmail.checked == true){
                    	isSendAlarmEmail = true;
                    	sendAlarmEmail.inputValue = true;
                    }
                    if(sendAlarmSMS.checked == true){
                    	isSendAlarmSMS = true;
                    	sendAlarmSMS.inputValue = true;
                    }
                    
                    //提交后台处理 
                 	f.submit({
                 		clientValidation : true,
                 		method : 'post',
                 		params : {
                 			'kiraClientMetadata.id' : id,
                 			'kiraClientMetadata.sendAlarmEmail' : isSendAlarmEmail,
                 			'kiraClientMetadata.sendAlarmSMS' : isSendAlarmSMS
                 		},
                 		url : 'kiraClientMetadata/updateKiraClientMetadata.action',
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
		    		    		Ext.getStore('kiraClientConfigMgrStore').reload();
							}else if(resultCode == 1){
						    	win.close();
						    	Ext.MessageBox.show({
		    		    			title: '警告',
							        msg: '部分更新成功!',
							        buttons: Ext.MessageBox.OK,
							        icon: Ext.MessageBox.WARNING
		    		    		});
						    	Ext.getStore('kiraClientConfigMgrStore').reload();
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
			},
			'updatekiraclientconfig button[action=close]' : {
				'click' : function(button,event, opt){
					var win = button.up('window');
					win.close();
				}
			},
			'createkiraclientconfig button[action=save]' : {
				'click' : function(button,event, opt){
					var win = button.up('window'), 
                        form = win.down('form'),
                        f = form.getForm(),
                        appId = f.findField('newKiraClientMetadata.appId'),
                        sendAlarmEmail = f.findField('newKiraClientMetadata.sendAlarmEmail'),
                        sendAlarmSMS = f.findField('newKiraClientMetadata.sendAlarmSMS'),
                        isSendAlarmEmail = false,
                        isSendAlarmSMS = false;
                        
                    sendAlarmEmail.inputValue = false;
                    sendAlarmSMS.inputValue = false;
                    
                    if(sendAlarmEmail.checked == true){
                    	isSendAlarmEmail = true;
                    	sendAlarmEmail.inputValue = true;
                    }
                    if(sendAlarmSMS.checked == true){
                    	isSendAlarmSMS = true;
                    	sendAlarmSMS.inputValue = true;
                    }
                    
                    if(Ext.isEmpty(appId.getValue())){
                    	Ext.MessageBox.show({
    		    			title: '警告',
					        msg: 'AppId不能为空!',
					        buttons: Ext.MessageBox.OK,
					        icon: Ext.MessageBox.WARNING
    		    		});
    		    		return ;
                    }
                    
                    if(!Ext.isEmpty(appId.getValue()) && Ext.isEmpty(Ext.util.Format.trim(appId.getValue()))){
                    	Ext.MessageBox.show({
    		    			title: '警告',
					        msg: 'AppId不能为空!',
					        buttons: Ext.MessageBox.OK,
					        icon: Ext.MessageBox.WARNING
    		    		});
    		    		return ;
                    }
                    //提交后台处理
                    f.submit({
                    	clientValidation : true,
                 		method : 'post',
                 		params : {
                 			'kiraClientMetadata.sendAlarmEmail' : isSendAlarmEmail,
                 			'kiraClientMetadata.sendAlarmSMS' : isSendAlarmSMS
                 		},
                 		url : 'kiraClientMetadata/createKiraClientMetadata.action',
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
		    		    		Ext.getStore('kiraClientConfigMgrStore').reload();
							}else if(resultCode == 1){
						    	win.close();
						    	Ext.MessageBox.show({
		    		    			title: '警告',
							        msg: '部分创建成功!',
							        buttons: Ext.MessageBox.OK,
							        icon: Ext.MessageBox.WARNING
		    		    		});
						    	Ext.getStore('kiraClientConfigMgrStore').reload();
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
			},
			'createkiraclientconfig button[action=close]' : {
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
		Ext.create('Kira.store.KiraClientConfigMgr', {
			storeId : 'kiraClientConfigMgrStore'
		});
		Ext.create('Kira.store.KiraClientPoolStore', {
			storeId : 'kiraClientPoolStore'
		});
		Ext.create('Kira.store.ManuallyCreatedStore', {
			storeId : 'manuallyCreatedStore'
		});
		Ext.create('Kira.store.PoolServiceMethodCalledInfo', {
			storeId : 'poolServiceMethodCalledInfoStore'
		});
	},

	/**
	 * @Override
	 */
	loadView : function() {}

});