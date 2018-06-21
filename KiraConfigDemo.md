# Kira Config Demo

###一、Schedule Config
#### 1.zookeeper-cluster.properties 

```
cluster1.id=1
cluster1.name=TestCluster
cluster1.serverList=xxx:2181,xxx:2181,xxx:2181
cluster1.description=TestCluster
cluster1.zone=ZONE_xxx
cluster1.idc=xxx
```
#### 2.applicationContext-base.xml 

```
# config appId and zookeeper properties
<bean id="kiraPropertyConfigurer"  class="com.yihaodian.architecture.kira.common.util.LoadGlobalPropertyConfigurer">
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

#### 3.applicationContext-remote.xml 

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

####  4.web.xml（config Kira Service）

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

  
####  5.applicationContext-schedule.xml 

* Job Schedule details config

```
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd">
<bean id="yhdScheduler"	class="com.yihaodian.architecture.kira.client.quartz.YHDSchedulerFactoryBean" lazy-init="false"  autowire="no" depends-on="kiraClientConfig">
		<property name="triggers">
			<list>
					<ref local="testSucess-Trigger" />
					<ref local="testFailed-Trigger" />
			</list>
		</property>
		<property name="quartzProperties">
			<props>
				<prop key="org.quartz.threadPool.threadCount">1</prop>
			</props>
		</property>
	</bean>
	<bean id="testSucess-Trigger" class="com.yihaodian.architecture.kira.client.quartz.YHDSimpleTriggerBean">
		<property name="jobDetail" ref="testSucess-JobDetail" />
		<property name="repeatInterval" value="30000" />
		<property name="version" value="0.0.1" />
		<property name="description" value="用于测试运行成功的定时任务." />
		<property name="disabled" value="true" />
	</bean>		
	<bean id="testFailed-Trigger" class="com.yihaodian.architecture.kira.client.quartz.YHDSimpleTriggerBean">
		<property name="jobDetail" ref="testFailed-JobDetail" />
		<property name="repeatInterval" value="60000" />
		<property name="version" value="0.0.1" />
		<property name="description" value="用于测试运行失败的定时任务." />
		<property name="disabled" value="true" />
	</bean>
	<bean id="testSucess-JobDetail"
class="com.yihaodian.architecture.kira.client.quartz.YHDMethodInvokingJobDetailFactoryBean">
		<property name="targetObject">
			<ref bean="otherService" />
		</property>
		<property name="targetMethod" value="testSucess"/>
	<property name="concurrent" value="false"/>
</bean>		
<bean id="testFailed-JobDetail" class="com.yihaodian.architecture.kira.client.quartz.YHDMethodInvokingJobDetailFactoryBean">
		<property name="targetObject">
			<ref bean="otherService" />
		</property>
		<property name="targetMethod" value="testFailed"/>
<property name="concurrent" value="false"/>
</bean>	
</beans>
```

#### 6.application-kira-client.xml 

* Apply to the Kira client section property configuration


```
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd"
>
	<bean id="kiraClientConfig" class="com.yihaodian.architecture.kira.client.util.KiraClientConfig">
		<property name="appCenter" value="false" />
		<property name="waitForResourceTimeoutMillisecond" value="120000" />
		<property name="autoDeleteTriggersOnZK" value="true" />		
		<property name="visibilityLimited" value="false" />
		<property name="visibleForUsers" value="xxx />
		<property name="sendAlarmEmail" value="true" />
		<property name="emailsToReceiveAlarm" value="xxx@aaa.com" />
		<property name="sendAlarmSMS" value="true" />
		<property name="phoneNumbersToReceiveAlarmSMS" value="" />
	</bean>
	<import resource="applicationContext-schedule.xml" />
</beans>
```

#### 7.application.xml 

* Configuration File Initialization Order (Reference)

```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE beans PUBLIC 
	"-//SPRING//DTD BEAN//EN" 
	"http://www.springframework.org/dtd/spring-beans.dtd">
<beans>
	<import resource="spring/applicationContext-base.xml" />
	<import resource="spring/applicationContext-kira-client.xml" />
</beans>
```

###二、Download demo config 
* [Download](kira-manager/src/main/resources/files/kira_config_demo.zip)

### 三、Config dependency 
![Kira_config_graph](kira-manager/src/main/resources/files/Kira_config_graph.jpg)



