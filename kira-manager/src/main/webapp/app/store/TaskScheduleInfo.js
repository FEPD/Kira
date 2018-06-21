Ext.define('Kira.store.TaskScheduleInfo', {
	extend : 'Ext.data.Store',
	model : 'Kira.model.TaskScheduleInfo',
	pageSize : 10,//per page show data count(pageSize)
	proxy : {
        type : 'ajax',
        url : 'triggerMetadata/getPoolTriggerStatus.action',
        actionMethods : {
        	read : 'POST'
        },
        extractResponseData : function(response){
        	var jsonObj = {};
        	var resp =Ext.decode(response.responseText);
        	var contextData = resp["contextData"];
        	var paging = contextData["paging"];//page object
        	var totalResults = paging["totalResults"];//the total count of records
        	var resultData = resp["resultData"];//the rows object
        	jsonObj["totalResults"] = totalResults;
        	jsonObj["resultData"] = resultData;
		    return jsonObj;  
        },
        //pageParam: false, //to remove param "page"
        pageParam : "criteria.paging.currentPage",//currentPage index param
        startParam: false, //to remove param "start"
        limitParam: false, //to remove param "limit"
        //noCache: false, //to remove param "_dc"
        reader : {
            type : 'json',
            root : 'resultData',
            totalProperty: "totalResults"
        }
    }
	
});