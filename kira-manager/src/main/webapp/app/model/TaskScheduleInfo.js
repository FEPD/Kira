Ext.define('Kira.model.TaskScheduleInfo', {
	extend : 'Ext.data.Model',
	fields : ['appId', 'triggerId','startTimeAsString','triggerStateAsString',
	'previousFireTimeAsString','nextFireTimeAsString','finalFireTimeAsString']
});