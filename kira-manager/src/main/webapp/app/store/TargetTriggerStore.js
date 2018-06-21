Ext.define('Kira.store.TargetTriggerStore', {
	extend : 'Ext.data.Store',
	model : 'Kira.model.TriggerStore',
	autoLoad : false,
	proxy : {
        type : 'ajax',
        url : 'triggerMetadata/getAllAvailableTriggerIdList.action',
        actionMethods : {
        	read : 'POST'
        },
        extractResponseData : function(response){
        	var jsonArr = [],jsonObj = {};
        	var resp =Ext.decode(response.responseText);
        	var resultData = resp["resultData"];//the rows object
        	for(var i=0,len = resultData.length;i<len;i++){
        		jsonObj['triggerValue']=resultData[i];
        		jsonObj['triggerName']=resultData[i];
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