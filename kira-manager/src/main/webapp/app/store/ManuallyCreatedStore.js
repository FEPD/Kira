Ext.define('Kira.store.ManuallyCreatedStore', {
	extend : 'Ext.data.Store',
	model : 'Kira.model.ManuallyCreatedStore',
	data : [{
		manuallyCreatedName : '是',manuallyCreatedValue : '是'
	},{
		manuallyCreatedName : '否',manuallyCreatedValue : '否'
	}]
});