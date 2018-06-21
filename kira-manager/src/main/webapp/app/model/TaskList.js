Ext.define('Kira.model.TaskList', {
	extend : 'Ext.data.Model',
	fields : ['id','asynchronous', 'createTimeAsString','manuallyCreated','manuallyCreatedBy','cronExpression',
	'description',"disabled","id","onlyRunOnSingleProcess",'locationsToRunJob','limitToSpecifiedLocations','concurrent',
	'reassignable','startTimeAsString','endTimeAsString','startDelay','repeatInterval','finalizedTimeAsString',
	'repeatCount','runTimeThreshold',"appId","scheduledLocally","targetMethod","targetMethodArgTypes",'argumentsAsJsonArrayString',
	"triggerId","triggerType","misfireInstruction","version",'targetAppId','targetTriggerId','priority','requestsRecovery',
	'copyFromMasterToSlaveZone','onlyScheduledInMasterZone','jobDispatchTimeoutEnabled','jobDispatchTimeout', 'comments']
});