Ext.define('Kira.model.ScheduleServerMgr', {
	extend : 'Ext.data.Model',
	fields : ['host','port','managedTriggersCount',
	'serverBirthTimeAsString','serverRoleName']
});