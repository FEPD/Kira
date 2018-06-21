Ext.define('Kira.controller.OperationLogInfoQuery', {
	extend : 'Ext.app.Controller',
	views : ['log.OperationLogInfoQuery'],
	stores : ['OperationLogInfoQuery'],
	
	/**
	 * @Override
	 */
	init : function(application) {
		this.control({
			'operationloginfoquery' : {
				'beforerender' : function(view) {
					var store = Ext.getStore('operationLogInfoQueryStore'), 
					    options = store.proxy.extraParams;
					if(options['criteria.operationName']) delete options['criteria.operationName'];
					if(options['criteria.operatedBy']) delete options['criteria.operatedBy'];
					if(options['criteria.operateTimeStartAsString']) delete options['criteria.operateTimeStartAsString'];
					if(options['criteria.operateTimeEndAsString']) delete options['criteria.operateTimeEndAsString'];
			        if(store.fireEvent('beforeload',store,options) !== false){
			        	//store.loadPage(1);//加载首页数据
			        }
                    view.loadView(); 
				}
			},
			'operationloginfoquery button[action=select]' : {
				'click' : function() {  
                    var grid = Ext.ComponentQuery.query('operationloginfoquery > grid')[0],
                        operationName = grid.down('[name=operationName]').getValue(),
                        operatedBy = grid.down('[name=operatedBy]').getValue(),
                        operateTimeStart = grid.down('[name=operateTimeStart]').getValue(),
                        operateTimeEnd = grid.down('[name=operateTimeEnd]').getValue(),
                        store = Ext.getStore('operationLogInfoQueryStore'),
                        options = store.proxy.extraParams;

					if(Ext.isEmpty(operateTimeStart) && Ext.isEmpty(operateTimeEnd)) {
						Ext.MessageBox.alert("提示", "过滤条件:\"操作时间\" 不能为空，因为数据量很大，请设置合理的时间区间长度，否则可能出现查询超时导致页面无数据显示。");
						return;
					}

					if(!Ext.isEmpty(operateTimeStart)){
                    	operateTimeStartAsString = Ext.util.Format.date(operateTimeStart,'Y-m-d H:i:s');
                    	options['criteria.operateTimeStartAsString'] = operateTimeStartAsString;
                    }else{
                    	delete options['criteria.operateTimeStartAsString'];
                    }
                    if(!Ext.isEmpty(operateTimeEnd)){
                    	operateTimeEndAsString = Ext.util.Format.date(operateTimeEnd,'Y-m-d H:i:s');
                    	options['criteria.operateTimeEndAsString'] = operateTimeEndAsString;
                    }else{
                    	delete options['criteria.operateTimeEndAsString'];
                    }
                    options['criteria.operationName'] = operationName;
                    options['criteria.operatedBy'] = operatedBy;
                    //options['criteria.operateTimeStartAsString'] = operationName;
                    //options['criteria.operateTimeEndAsString'] = operatedBy;
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
		Ext.create('Kira.store.OperationLogInfoQuery', {
			storeId : 'operationLogInfoQueryStore'
		});
		Ext.create('Kira.store.OperationNameStore', {
			storeId : 'operationNameStore'
		})
	},

	/**
	 * @Override
	 */
	loadView : function() {}

});