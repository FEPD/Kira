Ext.define('Kira.store.OperationNameStore', {
	extend : 'Ext.data.Store',
	model : 'Kira.model.OperationNameStore',
	//requires : ['Kira.model.OperationNameStore'],
	proxy : {
        type : 'ajax',
        url : 'operation/getAllNotReadonlyOperations.action',
        actionMethods : {
        	read : 'POST'
        },
        extractResponseData : function(response){
        	var jsonArr = [],jsonObj = {};
        	var resp =Ext.decode(response.responseText);
        	var resultData = resp["resultData"];//the rows object
		    return resultData;  
        },
        reader : {
            type : 'json'
        }
    }
});