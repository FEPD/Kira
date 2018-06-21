
 Kira FAQ
---

* **Q1.If when using springmvc3.x, suggesting that this error javax.servlet.ServletException: No adapter for handler [com.yihaodian.architecture.kira.client.util.CentralScheduleServiceExporter@12a6e85]: The DispatcherServlet configuration needs to include a HandlerAdapter that supports this handler
at org.springframework.web.servlet.DispatcherServlet.getHandlerAdapter(DispatcherServlet.java:1128)**

    * **A1：** This is due to that using the annotation MVC, the default AnnotationMethodHandlerAdapater does not support the HttpRequestHandler interface (this adapter only supports classes with corresponding annotations), and the HessianServiceExporter is the interface implementation, so no corresponding processor adapter can be found in DispatherServlet. Add a HttpRequestHandlerAdapter can be defined in the springmvc * -servlet.xml file：
    
```
<bean id="hessianHandlerAdapter"class="org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter" />
``` 

* **Q2.If log of application shows "task delivery failed", and found in the task execution details reported similar to the following error：Error occurs for running JobItemRunTask. exceptionDesc=com.caucho.hessian.client.HessianRuntimeException: java.net.ConnectException: Connection refused at com.yihaodian.architecture.hedwig.common.hessian.HedwigHessianProxy.sendRequest(HedwigHessianProxy.java:214)**

    * **A2:** This may be caused by a mismatch between the Kira internal service configured port number and the actual port number used. Check whether the value of the port attribute in the AppProfile configuration of the Kira internal configuration service is the same as the port used by the actually used tomcat/jetty. Or log on to the platform to see if the port number displayed in the execution environment of the scheduled task is the same as the port used by the actual tomcat/jetty.
   
* **Q3. The task execution log shows "task dispatch failed", and the following error is found in the task execution details：Error occurs for running JobItemRunTask. exceptionDesc=com.caucho.hessian.client.HessianRuntimeException: java.io.FileNotFoundException: http://XXX.XXX.XXX.XXX:9090/XXX/XXX/centralScheduleService**

    * **A3：** The above url directly in the browser will prompt 404 error, this is generally the Spring configuration file Kira internal service AppProfile related configuration values and the actual operation of the web container (tomcat or jetty) configuration is inconsistent. For example, the serviceAppName attribute of the configured Kira internal service is inconsistent with the path attribute specified in server.xml. 
        * Another possibility is that the urlPattern value of the configured Kira internal service is inconsistent with the url-pattern of the servlet-mapping of the remote service configured in web.xml. Another possibility is whether this request path is intercepted by an App internal implementation. Or <bean name="/centralScheduleService"/> This configuration information is placed in other files, resulting in the service did not expose the success?

* **4.The task execution log shows "task dispatch failed", and the following error is found in the task execution details：Can not create jobItemList. No environment available. May no such trigger exist at client side?**

    * **A4:** This is due to that the configuration of the scheduled task was not found in the currently invoked process of the application. This usually occurs when the number of configurations of the local timed tasks of different developers of the same app in the development test phase is inconsistent. The timer task developed by developer A was remotely called by Kira on the developer's B machine, and there was no timing task developed by developer A on the developer's B machine. In this case, developers in the App need to coordinate with each other to ensure that the number of scheduled tasks of the same app is the same. For example, after the scheduled tasks are scheduled, each of them submits the related scheduled tasks to svn, and then each developer synchronizes them to keep consistent. As for the specific business logic implementation can be implemented later.


* **Q5.I did not restart the local nor did I change the configuration. I just registered the scheduled task on the kira platform as I can't see it now?**

    * **A5:** This is generally due to that different developers in the same app have different numbers of local scheduled tasks. For example, the developer A local code has 2 timed tasks. At this time, developer A starts the webapp, and then it is in kira. On the platform you can see the registration of the two scheduled tasks. Developer B's native code has only 1 scheduled task at this time. At this time, developer B launches the webapp, and it will put off a scheduled task in developer A. Therefore, the developers within the App must coordinate to ensure that the number of scheduled tasks of the same App is consistent. For example, after the scheduled tasks are scheduled, each of them submits the related scheduled tasks to the git/svn, and each developer synchronizes them to keep consistent. As for the specific business logic implementation can be implemented later.


* **Q6.In the development and testing phase, you need to upgrade the version attribute every time you modify the configuration information of the scheduled task in the local code.**

    * **A6：** To ensure the management of scheduled tasks, a version number is introduced to facilitate management. In the staging and production environments, modifying the configuration of the scheduled task by modifying the app code needs to be performed in strict accordance with the upgrade of the version number. If you are in the development and testing stage, because the timing task configuration changes may be more frequent, modify the version number is too much trouble, you can quickly modify the latest configuration to Kira by following the steps in the app code to modify the configuration of a regular task. on the platform:
    
        * 1.First comment out the timed task in the <property name="triggers"><list> in the timed task configuration file, such as the following modified-Trigger
           
           ```
            <property name="triggers">
                <list>
                    <!-- <ref local="modified-Trigger" /> -->
                </list>
            </property>
            ```
            
        * 2.Then save the file and start your webapp project. At this point, you will find that the kira platform will offline this scheduled task.
        * 3.Close your webapp project, and then open the above comment, start the webapp again, and you will find that the latest modified-Trigger timer task configuration has been registered to the platform.
    
* **Q7. Error log prompt at startup
："The CentralScheduleServiceExporter must be initialized for using kira-client"**

    * **A7:** CentralScheduleServiceExporter is not initialized, that is, the CentralScheduleService required by using kira-client fails to be ready within 2 minutes. Possible reasons are as follows: For example, is it possible that spring multi-version coexistence (such as coexistence of 2.5.6 and 3.2.0)? Is it a break point? Or is it throwing other exceptions causing the spring initialization process to break? Or is the local environment relatively poor and the application of the app fails to start within 2 minutes? It is recommended that many asynchronous processes be started asynchronously, so that the root context initialization of spring is too long, causing the CentralScheduleServiceExporter not to be initialized within 2 minutes. So it's best to put the timing task-related configuration in the same spring appcontext as the configuration that exposes Kira's internal services.
         (If it is because the test environment occasionally causes network or database pressure to cause the resources and data required for the relevant app startup period to take a long time to complete, you can set the WaitForResourceTimeoutMillisecond property of KiraClientConfig to change the default 2 minutes to a longer one.)

* **Q8.If the execution log shows "task delivery failed", and found in the task execution details reported similar to the following error：Error occurs for running JobItemRunTask. exceptionDesc=com.caucho.hessian.client.HessianRuntimeException: java.net.SocketTimeoutException: connect timed out at com.yihaodian.architecture.hedwig.common.hessian.HedwigHessianProxy.sendRequest(HedwigHessianProxy.java:214))**

    * **A8：**
       * 1.This may be caused by a mismatch between the port number configured by Kira's internal service and the port number actually used. Check whether the value of the port attribute in the AppProfile configuration of the Kira internal service is the same as the port used by the actually used tomcat/jetty.
       * 2.Determine if the machine's firewall is off
       
* **9.No running and available centralScheduleService found**

    * **A9：** Abnormal problem This problem may be caused by two kinds of situations:
        *  1.There is no job-scheduled job machine in the kira service list;
        *  2.In the configuration of kira, the specified IP is set to execute the timing job, and at the same time, if [If only the task is performed in the specified location] is set to true, the specified IP is not in the scheduled task of the current kira. This problem also appears in the list. It is recommended to restart the currently running project. After the startup is complete, look at the job of kira, whether your machine IP is in the job's schedule list.
        




