Ext.define('Kira.model.TaskStatusChange', {
	extend : 'Ext.data.Model',
	fields : ['id', 'jobId','jobStatusId',
	'jobStatusName','createTimeAsString']
});