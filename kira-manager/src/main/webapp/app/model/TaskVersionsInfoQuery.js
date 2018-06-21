Ext.define('Kira.model.TaskVersionsInfoQuery', {
	extend : 'Ext.data.Model',
	fields : ['id', 'onlyRunOnSingleProcess','appId','cronExpression','targetAppId','targetTriggerId',
	'repeatCount','runTimeThreshold','repeatInterval','scheduledLocally','locationsToRunJob','limitToSpecifiedLocations','concurrent','manuallyCreated',
	'reassignable','start_time','end_time','unregistered','unregisteredUpdateTimeAsString','deleted',
	'deletedUpdateTimeAsString','startDelay','targetMethod','targetMethodArgTypes','finalizedTimeAsString',
	'triggerId','triggerType','version','disabled','argumentsAsJsonArrayString',
	'asynchronous','createTimeAsString','misfireInstruction','priority','requestsRecovery',
	'copyFromMasterToSlaveZone','onlyScheduledInMasterZone', 'jobDispatchTimeoutEnabled','jobDispatchTimeout','description','comments'
	]
});