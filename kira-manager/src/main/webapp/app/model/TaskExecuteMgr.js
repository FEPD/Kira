Ext.define('Kira.model.TaskExecuteMgr', {
	extend : 'Ext.data.Model',
	fields : ['id', 'jobStatusName','manuallyScheduled','appId',
	'createTimeAsString','createdBy','resultData','triggerId','version']
});