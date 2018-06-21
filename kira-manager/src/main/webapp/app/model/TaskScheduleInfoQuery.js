Ext.define('Kira.model.TaskScheduleInfoQuery', {
	extend : 'Ext.data.Model',
	fields : ['appId', 'triggerId','startTimeAsString','triggerStateAsString',
	'previousFireTimeAsString','nextFireTimeAsString','finalFireTimeAsString']
});