Ext.define('Kira.store.KiraClientPoolStore', {
	extend : 'Ext.data.Store',
	model : 'Kira.model.PoolStore',
	proxy : {
        type : 'ajax',
        url : 'kiraClientMetadata/getAllPoolIdList.action',
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