Ext.define('Kira.model.KiraClientConfigMgr', {
	extend : 'Ext.data.Model',
	fields : ['id','appId', 'kiraClientVersion','visibilityLimited','visibleForUsers',
	'createTimeAsString','lastRegisterTimeAsString','lastManuallyUpdateTimeAsString',
	'lastManuallyUpdateBy','lastRegisterDetail','sendAlarmEmail','emailsToReceiveAlarm',
	'sendAlarmSMS','phoneNumbersToReceiveAlarmSMS','manuallyCreated','manuallyCreatedBy','manuallyCreatedDetail']
});