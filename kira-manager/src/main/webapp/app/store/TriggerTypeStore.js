Ext.define('Kira.store.TriggerTypeStore', {
	extend : 'Ext.data.Store',
	model : 'Kira.model.TriggerTypeStore',
	proxy : {
        type : 'ajax',
        url : 'triggerMetadata/getTriggerTypeList.action',
        actionMethods : {
        	read : 'POST'
        },
        extractResponseData : function(response){
        	var jsonArr = [],jsonObj = {};
        	var resp =Ext.decode(response.responseText);
        	var resultData = resp["resultData"];//the rows object
        	for(var i=0,len = resultData.length;i<len;i++){
        		jsonObj['triggerTypeValue']=resultData[i];
        		jsonObj['triggerTypeName']=resultData[i];
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