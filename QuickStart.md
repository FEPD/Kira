
# Kira Quick Start

-----           
 ### 一、Maven Config
 
 #### 1.Join kira-client dependency
 
 ```
 <dependency>
 <groupId>com.yihaodian.architecture</groupId>
     <artifactId>kira-client</artifactId>
     <version>1.0.0-SNAPSHOT</version>
 </dependency>
 ``` 
 
 #### 2.Join spring-mvc dependency
 * Because Kira internally uses its own service (hessian), it needs to add Spring-mvc dependencies. (If you have already configured it, you can skip it. The following is only an example. The version number is based on the actual situation.)
 
 ```
 <dependency>
         <groupId>org.springframework</groupId>         
         <artifactId>spring-webmvc</artifactId>       
         <version>2.5.6.SEC03</version>
 </dependency>
 ```
 
 * **Note:** If you are using spring version 3.2.0, please add the dependency on spring-context-support, otherwise you will be prompted to find some classes.
 
 #### 3.Quartz version
 * The kira-client already contains the dependency on the quartz package, so the client can not add the following quartz dependency again. The platform recommends that the client use the following version of quartz. If other versions of quartz are used, it is recommended to use the recommended configuration of the platform
 
 ```
 <dependency>
      <groupId>org.quartz-scheduler</groupId>
           <artifactId>quartz</artifactId>
           <version>1.8.6</version>
 </dependency>
 ```
 
 #### 4.Log jar & version
 * Kira uses version 1.7.5 of the slf4j-api to print logs. Add the following sllf4j-api dependencies.
  
  ```
     <dependency>
           <groupId>org.slf4j</groupId>
           <artifactId>slf4j-api</artifactId>
           <version>1.7.5</version>
     </dependency>
  ```
  
  
  
 ### 二、Config
  
 #### 1.Web.xml config service
  
 * Since the need to expose services for Kira server calls, you need to do the following configuration: (where applicationContext-remote.xml is the spring configuration file that exposes Kira's internal services. It will be described below. The file name and url-patten are arbitrary. This is just for lifting. Examples.)
  
  ```
   <servlet>
         <servlet-name>remote</servlet-name>
         <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
         <init-param>
           <param-name>contextConfigLocation</param-name>
           <param-value>
             classpath:spring/applicationContext-remote.xml
           </param-value>
          </init-param>
         <load-on-startup>1</load-on-startup>
  </servlet>
  
       <servlet-mapping>
         <servlet-name>remote</servlet-name>
         <url-pattern>/remote/*</url-pattern>
       </servlet-mapping>
 ```
  
 #### 2.Exposed Kira internal service configuration
  
 * The applicationContext-remote.xml in the example
  
  
  ```
 <!-- The application only needs to configure an AppContext -->
      <bean name="XXXAppContext" class="com.yihaodian.architecture.hedwig.provider.AppProfile">
        <property name="domainName" value="replace your domainName"></property>
        <!-- If the application deployment Context Path is in the root path, set serviceAppName to /. If it is not deployed in the root path, for example, the contextPath of the deployment is /aaa, set the serviceAppName to aaa. -->
        <property name="serviceAppName" value="替换为你的serviceAppName"></property>
        <!--If the application deployment context-path is not /, but is /serviceAppName, then set assembleAppName to true, otherwise set assembleAppName to false -->
        <property name="assembleAppName" value="replace your value：true或false"></property>
        <!-- Same as url-pattern prefix match in servlet-mapping in web.xml -->
        <property name="urlPattern" value="Replace the prefix matching part of your urlPattern, as here configured as the remote example in web.xml above."></property>
        <!--According to the Application server's port modification-->
        <property name="port" value="Replaced with the http listening port number of your server"></property>
      </bean>
      <!--Service configuration, the name of the following bean must be configured as /centralScheduleService, the type must be configured as com.yihaodian.architecture.kira.client.util.CentralScheduleServiceExporter -->
      <bean name="/centralScheduleService" class="com.yihaodian.architecture.kira.client.util.CentralScheduleServiceExporter">
        <property name="appProfile" ref="Replace the bean name with your AppProfile. This example is XXXAppContext"></property>
      </bean>
 ```
 
 #### 3.Configure the value of AppId and zookeeper
 
 * The zookeeper-cluster configuration file must be placed under the classpath，later will support the loading of the path
 
 ```
 #The following is in application-base.xml
     <bean id="kiraPropertyConfigurer"
 class="com.yihaodian.architecture.kira.common.util.LoadGlobalPropertyConfigurer">
    <property name="appId">
      <value>kira</value>
    </property>
    <property name="locations">
      <list>				
      <value>classpath:zookeeper-cluster.properties</value>
      </list>
    </property>
  </bean>
 ```
 
 * zookeeper-cluster.properties
 
 ```
 #Test zookeeper address
 cluster1.id=1
 cluster1.name=TestCluster
 cluster1.serverList=xxx:2181,xxx:2181,xxx:2181
 cluster1.description=TestCluster
 cluster1.zone=ZONE_xxx
 cluster1.idc=xxx
 ```
 
 ### 三.Timed Job Config
 
 *  Now that we have entered the core timig job configuration, Kira has added some attributes as far as possible to support the original quartz various configuration attributes.
 
 
 ####1. Quartz extended
 * Replace the original quartz class with the associated extended custom class
     * Replace **org.springframework.scheduling.quartz.SchedulerFactoryBean** with **com.yihaodian.architecture.kira.client.quartz.YHDSchedulerFactoryBean**
     * Replace **org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean** with **com.yihaodian.architecture.kira.client.quartz.YHDMethodInvokingJobDetailFactoryBean**
     * Replace **org.springframework.scheduling.quartz.SimpleTriggerBean** with **com.yihaodian.architecture.kira.client.quartz.YHDSimpleTriggerBean**
     * Replace **org.springframework.scheduling.quartz.CronTriggerBean** with **com.yihaodian.architecture.kira.client.quartz.YHDCronTriggerBean**
  
 ####2.Trigger Type
 * Make sure that the configured YHDSimpleTriggerBean and YHDSimpleTriggerBean specify the unique bean id/name within this App application. Currently only SimpleTrigger and CronTriggerBean types are supported.
 
 ####3.YHDSimpleTriggerBean concrete attribute configuration
 * The configurable properties are described below (for more detailed description of some quartz properties, please refer to the specific instructions of quartz)
 
 | Attributes |	Type	|Optional/Required  | Comment |Whether Quartz has properties|
 |---|---|---|---|---|
 | startDelay	| long |	 optional| Startup delay (milliseconds) | Yes |
 | repeatInterval |	 long | Required |Repeat execution interval (milliseconds)| Yes|
 | repeatCount |	 int|	 optional | The number of repetitions, if -1 means unlimited times |Yes |
 | description	| String	 | Required | Description of trigger |Yes |
 | misfireInstruction|	 int	| Optional |Corresponding strategy to perform the leak. Leakage execution meaning: Not executed at the time of execution. There are two possible values: When the value is 0, if it is found that the leak is executed, it will be run once (this is the default strategy); if the value is 2, if it is found that the leak is executed, it will not be done.|Yes |
 |startTime	| Date| Optional | start Time| Yes|
 | endTime	| Date| Optional| end Time|Yes|
 |priority|int|Optional | Scheduled job priority. The higher the priority, the higher the priority is when the trigger is triggered at the same time. The default is 5. Value range: (0,9999)|Yes |  
 | version | string |Optional (Optional when the user goes online for the first time. That is, the system assigns a version number 0. If you want to upgrade the configuration later, you still need to configure the latest version number to update the configuration.) | This property is important. This is the version number corresponding to this trigger. After each configuration update, the version number needs to be updated. The new version number needs to be larger than the original version number, so that the platform will update the configuration. If the new version number is larger than the old version number, Kira server will be notified of the configuration and scheduled updates. Note: Make sure that there is only one version of the same scheduled task for the same app at the same time. The platform does not support running multiple versions of the same scheduled task of the same app at the same time. Start is an optional parameter. If this parameter is not set, the system will set it to 0 by default. Version number rules for kira-client 1.0.0-SNAPSHOT (including 1.0.0-SNAPSHOT version): 1. The format rule of the version is: multiple number sequences separated by periods. For example 0.0.1. The format of the version number is strictly limited. If the version number configuration does not conform to the format, exception handling will be thrown. 2. The comparison rule for the version number size is: The size of the version number will be compared based on the size of each digit portion divided by the dot number. For example: 0.0.1.9 is less than 0.0.1.10.| No |
 |asynchronous	| boolean| Optional| Whether to perform tasks asynchronously. If it is executed asynchronously, the server does not have to wait for the task to complete. It is recommended to use asynchronous methods for task execution. In this way, the server task can be returned immediately after the request is delivered, and the execution result of the task is asynchronously reported to the server. For tasks that need to be executed synchronously, you need to ensure that the task can end within 50 seconds. Otherwise, the server will perform timeout processing. The default value is true, which means asynchronous execution.|No |
 |onlyRunOnSingleProcess| boolean| Optional|Whether it is dispatched to only one execution site for execution when triggered. The default value is true, that is, when the timing task is triggered, only the trigger is dispatched to a single execution place at a time. Multiple copies of the same app can be deployed. Then the platform will determine whether to dispatch the execution task in the deployment process when triggering the scheduled task based on this attribute. If it is set to true, it will pick an execution task of the available deployment process. To achieve the main and standby purposes; if it is set to false, it will dispatch the execution task at the same time in all available deployment processes, which can achieve similar distributed computing effects and improve the execution efficiency. You see him as a situation where a job in MapReduce is divided into 1 task or multiple tasks. When onlyRunOnSingleProcess=true is divided into 1 task. When onlyRunOnSingleProcess=false is divided into multiple tasks. (Note: By default, Kira does not guarantee concurrency, that is, if the business method of the previous task has not been executed when the next trigger time point is reached, the task will continue to be dispatched, and there will be multiple places at the same time. In the case of executing business methods, if you want to really control the distributed concurrent execution of business methods, use the concurrent attribute.)| No |
 | concurrent | boolean| Optional| Whether to allow business methods to be executed concurrently. The default is true, that is, each time when the timing task trigger time point is reached, no matter whether the business method corresponding to this timed task is executing, the business method of executing this timed task will be triggered. When concurrent=false and onlyRunOnSingleProcess=true, the platform will ensure that only one place is allowed to execute business methods for scheduled tasks at any time. (Note: While using this feature, please ensure that the business method protects itself against possible re-execution situations so as not to affect the security of the business in case of repetitive execution of the business method.)|No | 
 |scheduledLocally	 | boolean	| Optional| Whether to let this task be scheduled locally. That is, whether to let the app itself use quartz to schedule the task locally. If local scheduling is performed, the kira server will not perform task scheduling and execution. However, configuration information is still reported to the Kira server. The default value is false, which means no local scheduling.|No|
 |disabled	| boolean|Optional| Whether to make this trigger unavailable. If set to unavailable, the Kira server platform will not schedule it for execution. However, configuration information is still reported to the Kira server. This feature can be used to temporarily turn off a trigger, which may be opened in the future. The default value is false, which is available by default.|No|
 |locationsToRunJob |String| Optional|Used to set a list of execution locations for scheduled tasks at the trigger level. If none of the execution locations specified on the runtime execution list specified by the runtime exist, the platform will select the executable locations for execution based on the value of the onlyRunOnSingleProcess property. If the value of onlyRunOnSingleProcess is true, then choose to randomly choose one of the available execution locations to execute. If the onlyRunOnSingleProcess value is false, it will be executed at all available execution locations. In order to facilitate the configuration of the task execution site list on the client, the operation can be performed at the trigger level (locationsToRunJob attribute), scheduler level (locationsToRunJobForAllTriggersOfThisScheduler attribute), and the kira client level (locationsToRunJobForAllTriggers attribute) on the three levels to configure the task execution site list. . Its priority is as follows: trigger level> scheduler level> kira client level. High-level configurations will override low-level configurations. If you have configured at the scheduler level or the kira client level and you want to set the execution location of a trigger to be empty, you need to set the locationsToRunJob attribute to a special value of "empty" (excluding double quotes). The default value is empty. Format: A comma-separated list of ip:ports, or a special value "empty" (not including double quotes) Example: 127.0.0.1:8080,127.0.0.2:80|No |
 |limitToSpecifiedLocations| boolean | Optional| Whether to only perform scheduled tasks in the location specified by locationsToRunJob. This parameter works when there is a value in locationsToRunJob. If the locationsToRunJob parameter has a value and limitToSpecifiedLocations is set to true, then the task will only be performed at the execution location specified by locationsToRunJob even if the specified execution location is unavailable. If limitToSpecifiedLocations is false and the execution location specified by the locationsToRunJob parameter is not available, other available execution locations are randomly selected to perform the task. The default value is false|No|
 |runTimeThreshold | long | Optional|Set the running time threshold of this timed task. If the running time of the running task exceeds this value, and the app's KiraClientConfig setting turns on the sending alarm function, the platform will send a relevant time out alarm according to this threshold. If it is empty, this kind of alarm is not made. Unit: milliseconds. The default value is blank|No|
 | requestsRecovery| boolean |Optional |When the kira server is down, if the scheduled task is triggering the task, this attribute sets whether to re-trigger the task that was previously completed due to the downtime when the kira server is restarted. The default is false. (Because there is a need to keep/erase the scene on ZK before/after each execution, the number of scheduled tasks that turn this feature on will be very expensive.)|No |
 |copyFromMasterToSlaveZone |boolean | Optional|The scheduled task adds configuration attribute copyFromMasterToSlaveZone. You can set whether the configuration information of this scheduled task is copied from MasterZone to SlaveZone. The default is false, which means no replication.|No|
 |onlyScheduledInMasterZone | boolean | Optional| The scheduled task adds the configuration attribute onlyScheduledInMasterZone. You can set whether this scheduled task is scheduled and executed only in the master zone. The default is true. If you want to be scheduled and executed in the Slave Zone, set this property to false. (The scheduled tasks are scheduled by default in the Master Zone, and are not scheduled by default in the Slave Zone.)|No |
 |jobDispatchTimeoutEnabled| boolean| Optional| This scheduled task allows you to set the delivery timeout. The default is false, which means that the delivery timeout setting is not allowed. The scheduling server will block for a long time while preparing to dispatch a scheduled task (for example, zk hangs). At this time, it will accumulate multiple requests for the scheduled task in multiple threads, and then release the block to continue execution ( For example zk recovery) may cause the accumulation of requests to be released centrally and put pressure on the app. Set this property to true and set the jobDispatchTimeout property to avoid the above-mentioned avalanche effect.|No|
 |jobDispatchTimeout| boolean| Optional|If this scheduled task allows to set the dispatch timeout (ie, jobDispatchTimeoutEnabled=true), the item indicates the task dispatch timeout (ms). If the task is dispatched longer than this value, the system will not dispatch the task, but will throw an exception. deal with. The range of values ​​is: (>=120000 milliseconds)|No|
 
 
 
 * Demo
 
 ```
 <bean id="yhdSimpleTrigger_kiraClientTestService-executeJobSuccess-asynchronous-onlyRunOnSingleProcess-repeatCount2-misfireInstruction0" class="com.yihaodian.architecture.kira.client.quartz.YHDSimpleTriggerBean">
    <property name="jobDetail" ref="kiraClientTestService-executeJobSuccess-JobDetail" />
    <property name="startDelay" value="120000" />
    <property name="repeatInterval" value="300000" />
    <property name="repeatCount" value="2" />
    <property name="version" value="0.0.1" />
    <property name="description" value="2分钟启动延迟，然后以每5分钟执行一次成功的不可取消任务： 异步执行，同时只能运行在一个进程内,总共只执行3次。" />
    <property name="misfireInstruction" value="0" />
    <property name="asynchronous" value="true" />
    <property name="onlyRunOnSingleProcess" value="true" />
    <property name="scheduledLocally" value="false" />
    <property name="disabled" value="false" />
    <property name="locationsToRunJob" value="ip:8080,ip:80" />
 <!-- <property name="locationsToRunJob" value="empty" /> -->
 </bean>
 ```
 
 ####4.YHDCronTriggerBean concrete attribute configuration
 
 * The configurable properties are described below (for more detailed description of some quartz properties, please refer to the specific instructions of quartz)
 
 | Attributes |	Type	|Optional/Required  | Comment |Whether Quartz has properties|          
 |---|---|---|---|---|
 |startTime |	Date|	Optional|start time| Yes|
 |endTime	| Date|	Optional| end time|Yes|
 |description |String|Required|Trigger description| Yes|
 |misfireInstruction	|int	|Optional|Corresponding strategy to perform the leak. Refer to the corresponding section description in YHDSimpleTriggerBean.|Yes|
 |cronExpression	|String|	Required| Cron expression. Refer to the Quartz documentation.|Yes|
 |priority |int |Optional |Refer to the corresponding section description in YHDSimpleTriggerBean.|Yes|
 |version |	String| Optional |Refer to the corresponding section description in YHDSimpleTriggerBean.|No|
 |asynchronous	|boolean	|Optional	|Refer to the corresponding section description in YHDSimpleTriggerBean.|No|
 |onlyRunOnSingleProcess|boolean |Optional|Refer to the corresponding section description in YHDSimpleTriggerBean.。|No|
 |concurrent|boolean|Optional |Refer to the corresponding section description in YHDSimpleTriggerBean. |No|
 |scheduledLocally|	boolean	|Optional|Refer to the corresponding section description in YHDSimpleTriggerBean.|No|
 |disabled|	boolean|	Optional |Refer to the corresponding section description in YHDSimpleTriggerBean.|	No|
 |locationsToRunJob| String	|Optional|Refer to the corresponding section description in YHDSimpleTriggerBean.| No|
 |limitToSpecifiedLocations|boolean|Optional|Refer to the corresponding section description in YHDSimpleTriggerBean.|No |
 |runTimeThreshold|long |Optional| Refer to the corresponding section description in YHDSimpleTriggerBean.|No|
 |requestsRecovery|boolean| Optional|Refer to the corresponding section description in YHDSimpleTriggerBean.|No| 
 |copyFromMasterToSlaveZone|boolean|Optional|Refer to the corresponding section description in YHDSimpleTriggerBean.|No|
 |onlyScheduledInMasterZone| boolean|Optional|Refer to the corresponding section description in YHDSimpleTriggerBean.|No|
 |jobDispatchTimeoutEnabled|boolean|Optional|Refer to the corresponding section description in YHDSimpleTriggerBean.|No|
 |jobDispatchTimeout|boolean|Optional|Refer to the corresponding section description in YHDSimpleTriggerBean.|No|
 
 * Demo：
 
 ```
 <bean id="yhdCronTrigger_kiraClientTestCanceledSuccessService-executeJobSuccess-asynchronous-onlyRunOnSingleProcess" class="com.yihaodian.architecture.kira.client.quartz.YHDCronTriggerBean">
    <property name="jobDetail" ref="kiraClientTestCanceledSuccessService-executeJobSuccess-JobDetail" />
    <property name="cronExpression" value="0 0/15 * * * ?" />
    <property name="version" value="0.0.1" />
    <property name="description" value="每15分钟执行一次成功的可成功取消的任务： 异步执行，同时只能运行在一个进程内。" />
    <property name="asynchronous" value="true" />
    <property name="onlyRunOnSingleProcess" value="true" />
    <property name="scheduledLocally" value="false" />
    <property name="disabled" value="false" />
    <property name="locationsToRunJob" value="192.168.35.100:8080,192.168.35.200:80" />
 </bean>
 ```
 
 
 ####5.JobDetail Config
 
 
 * The method configuration example without parameters is as follows：
 
 ```
 <bean id="kiraClientTestCanceledFailedService-executeJobSuccess-JobDetail"
 class="com.yihaodian.architecture.kira.client.quartz.YHDMethodInvokingJobDetailFactoryBean">
    <property name="targetObject">
     <ref bean="kiraClientTestCanceledFailedService" />
    </property>
    <property name="targetMethod" value="executeJobSuccess"/>
 </bean>
 ```
 
 * The parametric method configuration example is as follows (in fact, a method with any combination of parameters is supported. The following example shows a method with a LinkedHashMap parameter) ：
 
 ```
 <bean id="kiraClientTestService-executeJobSuccessWithMapParam-JobDetail"
 class="com.yihaodian.architecture.kira.client.quartz.YHDMethodInvokingJobDetailFactoryBean">
   <property name="targetObject">
     <ref bean="kiraClientTestService" />
   </property>
   <property name="targetMethod" value="executeJobSuccessWithMapParam"/>
   <property name="arguments">
     <list>
        <map><!-- Please use java.util.LinkedHashMap as method param -->
         <entry key="key1">
          <value>value1</value>
         </entry>
         <entry>
           <key><value>key2</value></key>
           <value>value2</value>
         </entry> 
        </map>
      </list>
    </property> 
 </bean>
 ```
 
 
 * If you need to support other types of method parameters, you can define related beans inside the <list> subnode:
 
 ```
 <property name="arguments">
 <list>
     <bean class="XXXXX"></bean>
     <bean class="YYYY"></bean>
  ......
 </list>
 </property> 
 ```
 
 
 ####6.Scheudler Config
 
 * In order to support the application of the local application can be task scheduling, you still need to define the Scheduler, its definition is similar to the original, just replace the type.
 
 * The following adds some custom attribute configuration:
 
 | Attributes |	Type	|Optional/Required  | Comment |  
 |---|---|---|---|
 |locationsToRunJobForAllTriggersOfThisScheduler|String| Optional	|Used to specify the list of execution locations for scheduled tasks at the Scheduler level. This attribute is provided to facilitate the client to set the execution location of scheduled tasks. For a detailed description of the execution location, see the description of the trigger configuration attribute locationsToRunJob above in this article. The default value is empty. If this property is not empty and the triggers locationsToRunJob property is not configured, its value is set to the "locationsToRunJob" property of all timed task triggers. Format: Example of comma-separated ip:port list: 127.0.0.1:8080,127.0.0.2:80|
    
 ``` 	 	 
  #Demo：
 <bean id="yhdScheduler"
 class="com.yihaodian.architecture.kira.client.quartz.YHDSchedulerFactoryBean" lazy-init="false" autowire="no" depends-on="kiraClientConfig">
      <property name="triggers">
         <list>
          <ref local="yhdCronTrigger_kiraClientTestCanceledSuccessService-executeJobSuccess-asynchronous-onlyRunOnSingleProcess" />
         </list>
      </property>
      <property name="quartzProperties"> 
        <props>
          <prop key="org.quartz.threadPool.threadCount">1</prop>
        </props>
        </property>
      <property name="locationsToRunJobForAllTriggersOfThisScheduler" value="192.168.35.120:8080,192.168.35.100:80" />
 </bean>
 ```
  
 ####7.Kira Client Config(Optional）
 
 * A spring bean of type KiraClientConfig can be defined for configuring the Kira client. If you define a client configuration, such as the configuration of the spring bean id is kiraClientConfig, then be sure to set depends-on = "kiraClientConfig" in the scheduler spring definition to ensure the correct initialization order (important). Also make sure that the same app application uses the same client configuration.
 Client configurable items are as follows:
 
 | Attributes |	Type	|Optional/Required  | Comment |  
 |---|---|---|---|
 |workWithoutKira	 |boolean	| Optional|Used to set whether to run from the kira system. The default value is false. If set to true, all triggers of the entire app will temporarily be scheduled locally using quartz, free from the impact of the kira system, and will not expose services provided internally by kira-client. At this time, configuration information will not be reported to Kira. server. (This can be configured when the kira platform is temporarily unavailable.)|
 |locationsToRunJobForAllTriggers|String|Optional	|It is used to specify the execution task list of the scheduled task at the kira client level. This attribute is provided to facilitate the client to set the execution point of the scheduled task. For a detailed description of the execution location, see the description of the trigger configuration attribute locationsToRunJob above in this article. The default value is blank. If this property is not empty, and the triggers locationsToRunJob property is not configured, its value will be set to the "locationsToRunJob" property of all timed task triggers. Format: Comma-separated ip:port list Example: 127.0.0.1:8080,127.0.0.2:80|
 |visibilityLimited| boolean	| Optional| The scheduled task related information used to set the App is visible only to certain specific people on the platform interface, and is used to protect the regular tasks of the App. It is strongly recommended that this setting be used to prevent the related scheduled tasks of this App from being mishandled by other App users. The default value is false. If set to true, the following visibleForUsers property needs to be set to specify a specific visible person list.|
 |visibleForUsers| String|Optional|Used to set a specific visible person list. The default value is blank. If the above visibilityLimited property is set, this property needs to be set. Format: Comma-separated list of domain user names. Example: domainUserName1,domainUserName2|
 |sendAlarmEmail|boolean|Optional|Whether to send an alarm email. (If an exception or failure occurs during the scheduled execution of a scheduled task, the alarm informs the relevant person.) The default value is false.|
 |emailsToReceiveAlarm|String|Optional|Receive alarm emails|
 |sendAlarmSMS|boolean|	Optional|Whether to send alarm messages. (If an exception or failure occurs during the scheduled execution of a scheduled task, the alarm informs the relevant person.)|
 |phoneNumbersToReceiveAlarmSMS| String |Optional|List of mobile phone numbers to receive alarms. The default value is blank. Format: Comma-separated list of mobile phone numbers Example: 13812345678, 13888888888|
 |keepKiraClientConfigDataOnKiraServerUnchanged|boolean |Optional	|Allows the user to configure whether to keep kira client configuration information on the platform from being updated every time the App appliance machine restarts. The default value is false, that is, the Kira client configuration information of the App on the platform is refreshed after each app machine restart.|
 | waitForResourceTimeoutMillisecond|long|Optional| The time-out period to wait for the use of relevant resources required by the kira-client. The unit is milliseconds. The default is 2 minutes. If the relevant resources are not ready within this time, an exception will be thrown. Required resources, such as whether the CentralScheduleService service is successfully exposed|
 
 
 * Kira client config demo：
 
 ```
 <bean id="kiraClientConfig" class="com.yihaodian.architecture.kira.client.util.KiraClientConfig">
    <property name="workWithoutKira" value="false" />
    <property name="locationsToRunJobForAllTriggers" value="192.168.35.110:8080,192.168.35.119:80" />
    <property name="visibilityLimited" value="false" />
    <property name="visibleForUsers" value="domainUserName1,domainUserName2" />
    <property name="sendAlarmEmail" value="true" />
    <property name="emailsToReceiveAlarm" value="aaa@jd.com,bbb@jd.com" />
    <property name="sendAlarmSMS" value="true" />
    <property name="phoneNumbersToReceiveAlarmSMS" value="xxxxxx,13888888888" />
 </bean>
 ```
 

 
