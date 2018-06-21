Kira Quick Start
 --- 
 
### 一、Maven 配置

#### 1.引入 kira-client Jar

```
<dependency>
    <groupId>com.yihaodian.architecture</groupId>
    <artifactId>kira-client</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

#### 2.引入 spring-mvc Jar 

```
   <dependency>
     <groupId>org.springframework</groupId>
     <artifactId>spring-webmvc</artifactId>
     <version>2.5.6.SEC03</version>
   </dependency>
```

**注意：** 如果使用了spring 3.2.0版本，那么请加入对 spring-context-support 的依赖，否则会提示找不到某些类。
  
#### 3.引入开源 Quartz Jar 

* kira-client已经包含对quartz包的依赖，所以客户端可不用再次添加下面的quartz依赖。平台推荐客户端使用如下quartz版本，如果用到其他版本的quartz建议使用平台推荐配置：

  ```
    <dependency>
      <groupId>org.quartz-scheduler</groupId>
      <artifactId>quartz</artifactId>
      <version>1.8.6</version>
    </dependency>
  ``` 

#### 4.引入 slf4j Jar  

* Kira使用了1.7.5的版本的slf4j-api方便打印日志,请加入下面版本或更高版本的slf4j-api的依赖。


  ``` 
  <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-api</artifactId>
      <version>1.7.5</version>
  </dependency>
  ```


### 二、配置


#### 1.Web.xml 配置服务

* 由于需要暴露服务供Kira服务器端调用，所以需要做如下配置：（其中applicationContext-remote.xml为暴露Kira内部服务的spring配置文件。下面会对其进行描述。文件名和url-patten随意。这里只是举个例子。）

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
  
#### 2.暴露的Kira内部服务配置

* 即例子中的applicationContext-remote.xml

```
<!-- 应用只需要配置一个AppContext -->
     <bean name="XXXAppContext" class="com.yihaodian.architecture.hedwig.provider.AppProfile">
       <property name="domainName" value="替换为你的domainName"></property>
       <!--如果应用部署的Context Path在根路径下，则设置serviceAppName为/。如果不是部署在根路径下，比如部署的contextPath为/aaa，则设置serviceAppName为aaa -->
       <property name="serviceAppName" value="替换为你的serviceAppName"></property>
       <!-- 如果应用部署的context-path不是/,而是在/serviceAppName下，则设置assembleAppName为true,否则设置assembleAppName为false -->
       <property name="assembleAppName" value="替换为你的值：true或false"></property>
       <!-- 与web.xml中servlet-mapping中的url-pattern的前缀匹配部分相同 -->
       <property name="urlPattern" value="替换为你的urlPattern的前缀匹配部分，如这里配置为上面举例web.xml中的remote"></property>
       <!-- 根据Application server的端口修改-->
       <property name="port" value="替换为你所在服务器的http监听端口号"></property>
     </bean>
     <!-- 服务配置，下面的Bean的name一定要配置为 /centralScheduleService ，类型一定要配置为 com.yihaodian.architecture.kira.client.util.CentralScheduleServiceExporter -->
     <bean name="/centralScheduleService" class="com.yihaodian.architecture.kira.client.util.CentralScheduleServiceExporter">
       <property name="appProfile" ref="替换为你的AppProfile的bean name. 这个例子为XXXAppContext"></property>
     </bean>
```

     
### 3.配置AppId 和 zookeeper 的地址
    
* appId 配置和 加载 classpath 下面的zookeeper配置    

```
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

* zookeeper-cluster.properties 文件配置

```
#测试环境zookeeper地址
cluster1.id=1
cluster1.name=TestCluster
cluster1.serverList=xxx:2181,xxx:2181
cluster1.description=TestCluster
cluster1.zone=ZONE_Test
cluster1.idc=Test
```

### 三、定时任务配置

* 下面进入到比较核心的定时任务配置，kira尽量在支持原有quartz各种配置属性的基础上，增加了一些属性。
 
#### 1.quartz类替换为相关扩展定制的类

* a. 把 org.springframework.scheduling.quartz.SchedulerFactoryBean 替换为 com.yihaodian.architecture.kira.client.quartz.YHDSchedulerFactoryBean
* b. 把 org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean 替换为 com.yihaodian.architecture.kira.client.quartz.YHDMethodInvokingJobDetailFactoryBean
* c. 把 org.springframework.scheduling.quartz.SimpleTriggerBean 替换为 com.yihaodian.architecture.kira.client.quartz.YHDSimpleTriggerBean
* d. 把 org.springframework.scheduling.quartz.CronTriggerBean 替换为 com.yihaodian.architecture.kira.client.quartz.YHDCronTriggerBean
    

#### 2. 定时任务类型
* 请确保配置的YHDSimpleTriggerBean和YHDSimpleTriggerBean指定了在本App应用内唯一的bean id/name。 目前只支持SimpleTrigger和CronTriggerBean类型。

#### 3.YHDSimpleTriggerBean对象的具体属性配置

可配置属性说明如下（某些quartz属性的更详细说明请参考quartz具体说明）：

| 属性名 |	属性类型	|可选/必选   | 其他说明 |是否Quartz已有属性 |
|---|---|---|---|---|
| startDelay	| long |	 可选	| 启动延迟(毫秒) | Quartz已有属性 |
| repeatInterval |	 long | 必选 | 重复执行间隔（毫秒）	| Quartz已有属性 |
| repeatCount |	 int|	 可选	| 重复次数，如果为-1则代表无限次 |	Quartz已有属性 |
| description	| String	 | 必选（ 将定时任务的description属性|对此trigger的描述 |Quartz已有属性 |
| misfireInstruction|	 int	| 可选| 漏执行的对应策略。漏执行含义： 在本该执行的时候没执行。可取值有2个：取值为0时，  如果发现漏执行后后补跑一次 （此为默认策略）；取值为2时，如果发现漏执行后啥也不干。|Quartz已有属性 |
|startTime	| Date	| 可选	 | 开始时间 | Quartz已有属性|
| endTime	| Date	 | 可选	|结束时间|Quartz已有属性|
|priority|int|可选 |定时任务优先级。优先级越高，则在同一时间一起触发的时候被优先执行。默认值为5。取值范围： (0,9999) |Quartz已有属性 |  
| version | string | 可选 （第一次上线时是可选的，即系统会给分配一个版本号0，后面如果要升级配置，还是需要配置下最新的版本号才会更新配置。) | 这个属性很重要。 这是这个trigger对应的版本号。每次配置更新后，需要更新下此版本号，新版本号需要比原来的版本号大，这样平台才会更新配置。如果新版本号比旧的版本号大，则会通知Kira服务器端进行配置和调度的更新。 注意:请确保同一个App 的同一个定时任务只能同时只有1个版本存在，平台不支持同一个App的同一个定时任务的多个版本同时运行。开始为可选参数，如果不设置此参数，系统将其默认设置为0。kira-client 2.0.0（包括2.0.0版本）以后的版本号规则：1. 版本格式规则为：以点号分割的多个数字序列。 比如 0.0.1 。对版本号的格式进行了严格限制，对版本号配置不符合格式的将抛异常处理。2. 版本号大小比较规则为：版本号大小将根据点号分割开的各个数字部分的大小进行比较。 比如： 0.0.1.9 比 0.0.1.10 小。|自定义属性 |
|asynchronous	| boolean| 可选	| 是否异步执行任务。如果是异步执行，则服务器端不用等待任务执行完毕。推荐使用异步方式进行任务的执行。这样服务器端任务下发请求后就可以马上返回，任务的执行结果会异步上报给服务器端。 对于需要同步执行的任务，需要确保任务在50秒内能结束，否则服务器端将进行超时处理。默认值为true,即异步执行。|自定义属性 |
|onlyRunOnSingleProcess| boolean| 可选| 触发时是否只派送到一个执行地点执行。默认值为true,即当触发定时任务时，每次只触发派送到单个执行地点。同一个App可以部署多份，然后平台会根据这个属性决定在触发定时任务时候是否在这些部署的进程中同时派送执行任务，如果设置为true，则会挑选一个可用部署进程的执行任务，这样可以达到主备目的；如果设置为false,则会在所有可用的部署进程中同时派送执行任务，可以达到类似分布式计算的效果，提高执行效率。 你把他看成是类似MapReduce中一个job分成1个task还是多个task的情况。当onlyRunOnSingleProcess=true时就是分成1个task。 当onlyRunOnSingleProcess=false时就是分成多个task。(注意：默认情况下kira不保证并发性，即当下一次触发时间点到的时候如果上一个任务的业务方法还没执行完，仍然会继续派送执行任务，这会出现同一时间点有多个地方在执行业务方法的情况。如果想真正控制业务方法的分布式并发执行，请配合使用concurrent属性。)| 自定义属性|
| concurrent | boolean| 可选| 是否允许业务方法并发执行。默认为true，即每次到了定时任务的触发时间点的时候，不管现在是否有此定时任务对应的业务方法在执行，都会触发执行此定时任务的业务方法。而当concurrent=false时并且onlyRunOnSingleProcess=true时，平台将保证任何时候只允许一个地方正在执行定时任务的业务方法。(注意： 使用此功能的同时，请先保证业务方法对于可能的重复执行的情况进行自我保护，以免在在万一出现重复执行业务方法时影响业务安全。)|自定义属性 | 
|scheduledLocally	 | boolean	| 可选| 是否让此任务进行本地调度。即是否让app自己在本地利用quartz进行任务的调度，如果进行本地调度，则kira服务器将不对其进行任务调度执行。但是配置信息还是会上报给Kira服务器。 默认值为false,即不进行本地调度。|自定义属性|
|disabled	| boolean|可选| 是否使此trigger不可用。如果设置为不可用，那么Kira服务器平台将不对其进行调度执行。但是配置信息还是会上报给Kira服务器。这个功能可用于暂时关闭某个trigger，将来可能开启的情况。默认值为false,即默认可用。|自定义属性|
|locationsToRunJob |String| 可选|用于在trigger级别上设置定时任务的执行地点列表。如果运行时所指定的执行地点列表上所指定的执行地点都不存在，则平台将根据onlyRunOnSingleProcess属性的值去选择可运行的地点进行执行。如果onlyRunOnSingleProcess值为true,则选择随机选择一个可用的执行地点执行。如果onlyRunOnSingleProcess值为false,则将在全部可用的执行地点上执行。为了方便客户端进行任务执行地点列表的配置，运行可以同时在trigger级别（locationsToRunJob属性），scheduler级别（locationsToRunJobForAllTriggersOfThisScheduler属性），kira客户端级别（locationsToRunJobForAllTriggers属性）这3个级别上进行任务执行地点列表的配置。 其优先级如下：trigger级别>scheduler级别>kira客户端级别。  高级别的配置将覆盖低级别的配置。 如果已经在scheduler级别或kira客户端级别进行了配置，同时想把某个trigger的执行地点设置为空，则需要设置locationsToRunJob属性为一个特殊值"empty"(不包括双引号).默认值为空.格式： 逗号分隔的ip:port列表,或者一个特殊值"empty"(不包括双引号)例子：127.0.0.1:8080,127.0.0.2:80|自定义属性 |
|limitToSpecifiedLocations| boolean | 可选| 是否只限制在locationsToRunJob所指定的地点中执行定时任务。该参数在locationsToRunJob中有值的时候才起作用。如果locationsToRunJob参数有值，并且limitToSpecifiedLocations设置为true，则只会在locationsToRunJob指定的执行地点执行任务，即使所指定的执行地点都不可用。如果limitToSpecifiedLocations为false，并且locationsToRunJob参数所指定的执行地点不可用，则会随机选择其他可用的执行地点执行任务。默认值为false|自定义属性 |
|runTimeThreshold | long | 可选|设置此定时任务的运行时间阀值，如果运行的任务的运行时间超过此值，并且该app的KiraClientConfig设置开启了发送报警功能，则平台将根据此阀值发送相关超时报警提醒。 如果为空则表示不进行此类报警。 单位：毫秒. 默认值为空|自定义属性|
| requestsRecovery| boolean |可选 |当kira服务器宕机时如果该定时任务正在触发任务过程中，该属性设置是否在kira服务器宕机重启时重新触发原来因宕机而未执行完的任务。默认为false。（由于需要在每次执行前/后在ZK上保留/抹去现场，所以开启此功能的定时任务数量很多多，则会很消耗资源。）| 自定义属性 |
|copyFromMasterToSlaveZone |boolean | 可选|定时任务增加配置属性copyFromMasterToSlaveZone, 可以设置此定时任务的配置信息是否从MasterZone到SlaveZone进行复制。 默认为false，即不进行复制。|自定义属性|
|onlyScheduledInMasterZone | boolean | 可选|定时任务增加配置属性onlyScheduledInMasterZone，可以设置此定时任务是否只在Master Zone中被调度执行。 默认为true。 如果想在Slave Zone中也被调度执行，请把此属性设置为false. |自定义属性 |
|jobDispatchTimeoutEnabled| boolean| 可选| 此定时任务是否允许设置派送超时，默认为false, 即不允许派送超时设置。调度服务器在准备派送某个定时任务期间由于某些原因（比如zk挂了）会block住很久，此时会在多个线程中积累多次对定时任务派送请求，当随后释放block而继续执行(比如zk恢复)时可能导致积累的请求集中释放，而对app造成压力。设置此属性为true并配合设置jobDispatchTimeout属性可避免上述导致的雪崩效应。|自定义属性|
|jobDispatchTimeout| boolean| 可选| 如果此定时任务允许设置派送超时(即jobDispatchTimeoutEnabled=true)，则该项表示任务派送超时值(ms)，如果任务派送的时间超过了该值，系统不会派送运行该任务，而是抛出异常处理。取值范围为:(>=120000毫秒)|自定义属性|

* 配置例子如下：

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
   <property name="locationsToRunJob" value="xxx:8080,xxx:80" />
<!-- <property name="locationsToRunJob" value="empty" /> -->
</bean>
```

#### 4.YHDCronTriggerBean对象的具体属性配置  
 
 * 可配置属性说明如下（某些quartz属性的更详细说明请参考quartz具体说明）：

|属性名	|属性类型 |可选/必选 |	其他说明 |是否Quartz已有属性|             
|---|---|---|---|---|
|startTime |	Date|	可选 |开始时间| Quartz已有属性|
|endTime	| Date|	可选| 结束时间|Quartz已有属性|
|description |String|必选（ 根据京东审计意见，将定时任务的description属性强制不能为空。）|对此trigger的描述。| Quartz已有属性|
|misfireInstruction	|int	|可选|漏执行的对应策略。参考YHDSimpleTriggerBean中对应部分描述。|Quartz已有属性|
|cronExpression	|String|	必选| Cron表达式。参考Quartz文档。	|Quartz已有属性|
|priority |int |可选 |参考YHDSimpleTriggerBean中对应部分描述。|Quartz已有属性|	 	 
|version |	String|	可选 |参考YHDSimpleTriggerBean中对应部分描述。	|自定义属性|
|asynchronous	|boolean	|可选	|参考YHDSimpleTriggerBean中对应部分描述.|自定义属性|
|onlyRunOnSingleProcess|boolean |可选|参考YHDSimpleTriggerBean中对应部分描述。|自定义属性|
|concurrent|boolean|可选 |参考YHDSimpleTriggerBean中对应部分描述。 |自定义属性|
|scheduledLocally|	boolean	|可选|参考YHDSimpleTriggerBean中对应部分描述。|自定义属性|
|disabled|	boolean|	可选 |参考YHDSimpleTriggerBean中对应部分描述。|	自定义属性|
|locationsToRunJob| String	|可选|参考YHDSimpleTriggerBean中对应部分描述。| 自定义属性|
|limitToSpecifiedLocations|boolean|可选|参考YHDSimpleTriggerBean中对应部分描述。|自定义属性 |
|runTimeThreshold|long |可选| 参考YHDSimpleTriggerBean中对应部分描述。|自定义属性 |
|requestsRecovery|boolean| 可选|参考YHDSimpleTriggerBean中对应部分描述。|自定义属性| 
|copyFromMasterToSlaveZone|boolean|可选|参考YHDSimpleTriggerBean中对应部分描述。|自定义属性|
|onlyScheduledInMasterZone| boolean|可选|参考YHDSimpleTriggerBean中对应部分描述。|自定义属性|
|jobDispatchTimeoutEnabled|boolean|可选|参考YHDSimpleTriggerBean中对应部分描述。|自定义属性|
|jobDispatchTimeout|boolean|可选|参考YHDSimpleTriggerBean中对应部分描述。|自定义属性|

* 配置例子如下：

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
   <property name="locationsToRunJob" value="xxx:8080,xxx:80" />
   <!-- <property name="locationsToRunJob" value="empty" /> -->
</bean>
```

#### 5.JobDetail配置

 * a. 不带参数的方法配置例子如下：

```
<bean id="kiraClientTestCanceledFailedService-executeJobSuccess-JobDetail"
class="com.yihaodian.architecture.kira.client.quartz.YHDMethodInvokingJobDetailFactoryBean">
   <property name="targetObject">
    <ref bean="kiraClientTestCanceledFailedService" />
   </property>
   <property name="targetMethod" value="executeJobSuccess"/>
</bean>
```

* b. 带参数的方法配置例子如下（实际上支持带任意参数组合的方法。下面例子举例的是带一个LinkedHashMap参数的方法）：

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

* 如果需要支持其他类型的方法参数请在<list>子节点里面面定义相关bean即可：

```
<property name="arguments">
<list>
    <bean class="XXXXX"></bean>
    <bean class="YYYY"></bean>
 ......
</list>
</property> 
```


#### 6.Scheduler配置

* 为了支持App应用本地可以自己进行任务调度，仍然需要定义Scheduler，其定义和原来差不多，只是替换下类型即可.
 
* 下面增加了一些自定义属性的配置：

| 属性名	| 属性类型| 	可选/必选	| 说明| 
|---|---|---|---|
|locationsToRunJobForAllTriggersOfThisScheduler|String| 可选	|用于在Scheduler级别上指定定时任务的执行地点列表，提供这个属性是为了方便客户端设置定时任务的执行地点。执行地点的详细描述参见本文上面的关于trigger配置属性locationsToRunJob的说明。默认值为空.如果这个属性不为空，并且trigger的locationsToRunJob属性没进行配置，则其值将设置到所有定时任务trigger的“locationsToRunJob”属性中去。格式： 逗号分隔的ip:port列表例子：127.0.0.1:8080,127.0.0.2:80|
 	
``` 	 	 
 配置例子如下：
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
     <property name="locationsToRunJobForAllTriggersOfThisScheduler" value="xxx:8080,xxx:80" />
</bean>
```
 
#### 7.Kira客户端属性配置（可选）

* 可以定义一个KiraClientConfig类型的spring Bean.用于进行Kira客户端的配置。如果定义了客户端配置，比如配置的springbean的id为kiraClientConfig，那么请务必在scheduler的spring定义处设置depends-on="kiraClientConfig"，以保证正确的初始化顺序(很重要)。 另外请保证同一App应用使用相同的客户端配置。

* 客户端可配置项如下：

| 属性名	| 属性类型| 	可选/必选	| 说明| 
|---|---|---|---|
|workWithoutKira	 |boolean	| 可选|用于设置是否脱离kira系统运行。默认值为false.如果设置为true,则整个app的全部trigger将暂时用quartz进行本地调度，脱离kira系统影响，同时不暴露kira-client的内部提供的服务,此时将不上报配置信息给Kira服务器。（当kira平台暂时不可用的时候可以这么配置。）|
|locationsToRunJobForAllTriggers|String|可选	|用于在kira客户端级别上指定定时任务的执行地点列表，提供这个属性是为了方便客户端设置定时任务的执行地点。执行地点的详细描述参见本文上面的关于trigger配置属性locationsToRunJob的说明。默认值为空。如果这个属性不为空，并且trigger的locationsToRunJob属性没进行配置，则其值将设置到所有定时任务trigger的“locationsToRunJob”属性中去。格式： 逗号分隔的ip:port列表  例子： 127.0.0.1:8080,127.0.0.2:80|
|visibilityLimited| boolean	| 可选| 用于设置App的定时任务相关信息在平台界面上是否只对某些特定人可见，用于保护本App的定时任务。强烈建议进行此项设置，以防止本App应用的相关定时任务被其他App的人误操作。默认值为false.如果设置为true,则需要设置下面的visibleForUsers属性用来指定特定可见人列表。|
|visibleForUsers| String|可选|用于设置特定可见人列表。默认值为空。如果设置了上面的 visibilityLimited属性，则此属性需要进行设置。格式： 逗号分隔的域用户名列表。例子： domainUserName1,domainUserName2|
|sendAlarmEmail|boolean|可选|是否发送报警邮件.（当调度执行定时任务的过程中如果出现异常或者失败情况，报警通知相关人员。）默认值为false.|
|emailsToReceiveAlarm|String|可选|接收告警的邮件表.kira在product环境才会真正成功发送邮件。京东目前没有在测试环境，验证邮件发送的测试|
|sendAlarmSMS|boolean|	可选|是否发送告警短信。（当调度执行定时任务的过程中如果出现异常或者失败情况，报警通知相关人员。） 和product环境才会真正成功发送短信,另外最近移动联通电信在进行群发短信整顿，有可能会导致批量短信发送不成功哦。默认值为false.|
|phoneNumbersToReceiveAlarmSMS| String |可选|接收告警的手机号码列表。默认值为空。格式：以逗号分隔的手机号码列表例子：13812345678,13888888888|
|keepKiraClientConfigDataOnKiraServerUnchanged|boolean |可选	|允许用户配置当每次App应用机器重启后，是否保持平台上的kira客户端配置信息不被更新。默认值为false,即每次app机器重启后都会刷新平台上该App应用的Kira客户端配置信息。|
| waitForResourceTimeoutMillisecond|long|可选| 等待使用kira-client必备的相关资源准备完毕的超时时间设置，单位：毫秒。默认为2分钟。 如果在该时间内相关资源没有准备好，则会抛出异常。必备资源，比如：CentralScheduleService服务是否暴露成功。|

* kira客户端属性配置例子：

```
<bean id="kiraClientConfig" class="com.yihaodian.architecture.kira.client.util.KiraClientConfig">
   <property name="workWithoutKira" value="false" />
   <property name="locationsToRunJobForAllTriggers" value="xxx:8080,xxx:80" />
   <property name="visibilityLimited" value="false" />
   <property name="visibleForUsers" value="domainUserName1,domainUserName2" />
   <property name="sendAlarmEmail" value="true" />
   <property name="emailsToReceiveAlarm" value="aaa@jd.com,bbb@jd.com" />
   <property name="sendAlarmSMS" value="true" />
   <property name="phoneNumbersToReceiveAlarmSMS" value="13812345678,13888888888" />
</bean>
```

