Ext.define('Kira.store.StatusStore', {
	extend : 'Ext.data.Store',
	model : 'Kira.model.TaskStatus',//复用model
	remoteFilter : true,//远程过滤
	remoteSort : true,//远程排序
	proxy : {
        type : 'ajax',
        url : 'jobStatus/list.action',
        actionMethods : {
        	read : 'POST'
        },
        extractResponseData : function(response){
        	var jsonArr = [],jsonObj = {};
        	var resp =Ext.decode(response.responseText);
        	var resultData = resp["resultData"];//the rows object
        	for(var i=0,len = resultData.length;i<len;i++){
        		jsonObj['id']=resultData[i]['id'];
        		jsonObj['name']=resultData[i]['name'];
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