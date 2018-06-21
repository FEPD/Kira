Ext.define('Kira.model.TaskExecuteDetail', {
	extend : 'Ext.data.Model',
	fields : ['id', 'jobId','jobStatusId',
	'jobStatusName','createTimeAsString',
	'lastUpdateTimeAsString', 'appId','resultData',
	'serviceUrl', 'triggerId','triggerMetadataId',
	'version'
	]
});