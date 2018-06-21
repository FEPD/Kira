Ext.define('Kira.model.OperationLogInfoQuery', {
	extend : 'Ext.data.Model',
	fields : ['operateTimeAsString', 'operatedBy','operationDisplay',
	          'resultCode','resultDetails','operationDetails'
	]
});