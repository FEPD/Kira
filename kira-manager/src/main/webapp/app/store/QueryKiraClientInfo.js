Ext.define('Kira.store.QueryKiraClientInfo', {
	extend : 'Ext.data.Store',
	model : 'Kira.model.QueryKiraClientInfo',
	proxy : {
        type : 'ajax',
        url : 'kiraClient/queryKiraClientInfoAsMap.action',
        actionMethods : {
        	read : 'POST'
        },
        extractResponseData : function(response){
        	var jsonArr = [],jsonObj = {};
        	var resp =Ext.decode(response.responseText);
        	var resultData = resp["resultData"];//the rows object
        	for(var key in resultData){
        		if(resultData.hasOwnProperty(key)){
        			jsonObj['key']=key;
	        		jsonObj['value']=resultData[key];
	        		jsonArr.push(jsonObj);
	        		jsonObj = {};
        		}
        	}
		    return jsonArr;  
        },
        reader : {
            type : 'json',
            root : 'resultData'
        }
    }
	
});