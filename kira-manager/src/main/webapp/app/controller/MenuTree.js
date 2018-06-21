Ext.define('Kira.controller.MenuTree', {
	extend : 'Ext.app.Controller',
	views : ['MenuTree'],
	refs : [
        {
        	ref : 'viewport',
        	selector: 'viewport'
        },
        {
            ref: 'tab',
            selector: 'viewport tabpanel'
        }
    ],
	
	/**
	 * @Override
	 */
	init : function(application) {
		this.control({
			'viewport tool[action=refresh]' : {
				'click' : function() {
					var stores = ['config'],
					    treepanels = this.getViewport().query('treepanel');
					Ext.each(treepanels, function(treepanel, index) {
						treepanel.store.on('load', function(store, records, successful, operation, opts) {
							treepanel.doLayout();
						});
					});
				}
			},
			'viewport' : {
				'beforerender' : function() {
					var application = this.application,
					    treepanels = this.getViewport().query('treepanel'),
					    tabpanel = this.getTab();
					Ext.each(treepanels, function(treepanel, index) {
						treepanel.on('itemclick', function(treepanel, record, item, index, e, opts) {
				            if (record.get('leaf')) {
				            	var moduleId = record.get('url'); 
				            	application.loadModule(moduleId);
				            	var module = application.getController(moduleId);
				            	/**
				            	 * 
				            	 * 第一个控制器(第一个视图为模块主功能视图)
				            	 */
				            	var viewName = module.views[0];
				            	var view = module.getView(viewName);
				            	
				            	/***
				            	 * 视图结构: 目录为模块包名, 目录内文件为模块名, 视图类型为小写的模块名.
				            	 * @例如
				            	 * Kira.viw.task.TaskList
				            	 * alias : 'widget.tasklist'
				            	 * 
				            	 * viewType: tasklist
				            	 */
				            	var viewType = viewName.split('.')[1].toLowerCase();
				            	/**
				            	 * 如果还没有创建视图, 则创建视图.
				            	 */
				            	if (!tabpanel.down(viewType)) {
				                    var panel = view.create();
				                    tabpanel.add(panel);
				                    tabpanel.setActiveTab(panel);
				                    panel.doLayout();
				            	}
				            	/**
				            	 * 如果该视图已经存在, 则刷新视图.
				            	 */
				            	else {
				            		var panel = tabpanel.down(viewType);
				                    tabpanel.setActiveTab(panel);
				                    panel.doLayout();
				            	}
				            } else {
				            	treepanel.expand(record);
				            }
				            
						});
					});
				}
			}
		});
	},

	/**
	 * @Override
	 */
	loadModel : function() {
		var model = Ext.define("TreeModel",{
			extend : "Ext.data.Model",
			fields : [
			    {name : "id",type : "string"},
			    {name : "text",type : "string"},
			    {name : "pid",type : "string"},  
                {name : "leaf",type : "boolean"},  
                {name : "url",type : "string"}
			] 
		});
		Ext.create('Ext.data.TreeStore', {
			storeId : 'config',
			autoLoad : true,
			model : model,
			root : {
				id : -1,
				expanded : true
			},
			proxy : {
                type : 'ajax',
                url : 'menu/getMenuTree.action',
                extractResponseData : function(response){
					var resp =Ext.decode(response.responseText),
					    jsonObj = resp["resultData"];
		            return jsonObj;  
				},
                reader : {
                    type : 'json'
                }
            }
		});
	},

	/**
	 * @Override
	 */
	loadView : function() {
		var userInfo, ipInfo;
		//zone相关信息
		var currentKiraZoneId, kiraCrossMultiZoneRoleName, kiraMasterZoneId;
		Ext.Ajax.request({
			method : 'POST',
			url : 'user/getLoginUserContextData.action',
			async : false,
			success : function(response, options){
				var resp = Ext.decode(response.responseText);
				if(resp && resp['resultData']){
					userInfo = resp['resultData']['userName'];
				}
			},
			failure : function(response, options){
				Ext.MessageBox.alert('提示','加载超时或连接错误！');
			}
		});
		Ext.Ajax.request({
			method : 'POST',
			url : 'other/getCurrentServerIp.action',
			async : false,
			success : function(response, options){
				var resp = Ext.decode(response.responseText);
				if(resp && resp['resultData']){
					ipInfo = resp['resultData'];
				}
			},
			failure : function(response, options){
				Ext.MessageBox.alert('提示','加载超时或连接错误！');
			}
		});
		Ext.Ajax.request({
			method : 'POST',
			url : 'other/getKiraZoneContextData.action',
			async : false,
			success : function(response, options){
				var resp = Ext.decode(response.responseText);
				if(resp && resp['resultData']){
					currentKiraZoneId = resp['resultData']['currentKiraZoneId'];
					kiraCrossMultiZoneRoleName = resp['resultData']['kiraCrossMultiZoneRoleName'];
					kiraMasterZoneId = resp['resultData']['kiraMasterZoneId'];
				}
			},
			failure : function(response, options){
				Ext.MessageBox.alert('提示','加载超时或连接错误！');
			}
		});
		Ext.create('Ext.container.Viewport', {
			layout : 'border',
			items : [{
				region : 'north',
				cls: 'header',
				split : true,
				html: '<h1>Kira平台</h1>',
				height: 80,
				bbar : [{
					iconCls : 'icon-user',
					text : '用户：'+userInfo
				},'-',{
					text : '服务器地址：'+ipInfo
				},'-',{
					text : '当前Zone：'+currentKiraZoneId
				},'-',{
					text : '角色是：'+kiraCrossMultiZoneRoleName
				},'-',{
					text : 'MasterZone：'+kiraMasterZoneId
				},'-',{
					text : Ext.Date.format(new Date(),'Y年m月d日')
				},'-',{
					text : '定时任务预执行报告下载',
					iconCls : 'icon-file',
					handler : function() {
						window.open("other/exportTriggersPredictReport.action");
					}
				},'->',{
					text : '退出',
					iconCls : 'icon-logout',
					handler : function() {
						Ext.MessageBox.confirm('确认退出','您确定要退出系统吗？',handleLogout);
						function handleLogout(btn){
							if(btn == 'yes'){
								window.location = "controller?method=logout";
							}
						}
					}
				}],
				bodyStyle : 'backgroud-color:#99bbe8;line-height : 50px;padding-left:20px;font-size:22px;color:#000000;font-family:黑体;font-weight:bolder;'+
                'background:rgba(153,187, 232,0.4); filter: alpha(opacity=100 finishopacity=20) progid:DXImageTransform.Microsoft.gradient(startcolorstr=#99BBE8,endcolorstr=#99BBE8)'
			}, {
				region : 'west',
				split : true,
				collapsible : true,
				collapseFirst : false,
				title : '功能导航',
				width : 200,
				layout : 'accordion',
				items : [{
					xtype : 'treepanel',
					//iconCls : 'x-tree-icon x-tree-icon-parent',
					rootVisible : false,
					//title : '系统菜单',
					width : 200,
					//height : 150,
					store : Ext.getStore('config')
				}],
				tools : [{
					type : 'refresh',
					action : 'refresh'
				}]
			}, {
				region : 'south',
				id:'bottom',
				xtype: 'toolbar',
				cls: 'south',
				height:23,
				buttonAlign:'center',
				items:["->","<span>京东&copy;版权所有<span>"]
			}, {
				region : 'center',
				xtype : 'tabpanel',
				activeTab : 0,
				closeAction : 'destroy',
				items : [{
					title : '欢迎访问'
				}]
			}]
		});
	}
});