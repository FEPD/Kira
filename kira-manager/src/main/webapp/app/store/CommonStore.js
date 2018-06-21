Ext.define('Kira.store.CommonStore', {
	extend : 'Ext.data.Store',
	model : 'Kira.model.CommonModel',
	autoLoad : false,
	proxy : {
        type : 'ajax',
        url : 'triggerMetadata/getMisfireInstructionList.action',
        actionMethods : {
        	read : 'POST'
        },
        extractResponseData : function(response){
        	var jsonArr = [],jsonObj = {};
        	var resp =Ext.decode(response.responseText);
        	var resultData = resp["resultData"];//the rows object
        	for(var i=0,len = resultData.length;i<len;i++){
        		jsonObj['value']=resultData[i];
        		jsonObj['name']=resultData[i];
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