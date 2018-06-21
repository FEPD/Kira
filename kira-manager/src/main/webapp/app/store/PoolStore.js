Ext.define('Kira.store.PoolStore', {
	extend : 'Ext.data.Store',
	model : 'Kira.model.PoolStore',
	remoteFilter : true,//远程过滤
	remoteSort : true,//远程排序
	proxy : {
        type : 'ajax',
        url : 'triggerMetadata/getAllPoolIdList.action',
        actionMethods : {
        	read : 'POST'
        },
        extractResponseData : function(response){
        	var jsonArr = [],jsonObj = {};
        	var resp =Ext.decode(response.responseText);
        	var resultData = resp["resultData"];//the rows object
        	for(var i=0,len = resultData.length;i<len;i++){
        		jsonObj['poolValue']=resultData[i];
        		jsonObj['poolName']=resultData[i];
        		jsonArr.push(jsonObj);
        		jsonObj = {};
        	}
		    return jsonArr;  
        },
        reader : {
            type : 'json',
            root : 'resultData'
        }
    }
	
});