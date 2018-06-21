Kira FAQ
---

* **Q1.如果在使用springmvc3.x的时候，如果提示这个错误javax.servlet.ServletException: No adapter for handler [com.yihaodian.architecture.kira.client.util.CentralScheduleServiceExporter@12a6e85]: The DispatcherServlet configuration needs to include a HandlerAdapter that supports this handler
at org.springframework.web.servlet.DispatcherServlet.getHandlerAdapter(DispatcherServlet.java:1128)**

    * **A1：** 这是由于使用注解MVC时，默认的AnnotationMethodHandlerAdapater不支持HttpRequestHandler接口（这个Adapter只支持具 有相应注解的类），而HessianServiceExporter正是该接口的实现，因此在DispatherServlet中找不到对应的处理器适配器。在springmvc的*-servlet.xml文件中添加一个HttpRequestHandlerAdapter定义即可：
   
 ```
    <bean id="hessianHandlerAdapter" class="org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter" />
```

* **Q2.如果执行日志里显示“任务派送失败”，并在任务执行明细里面发现报类似下面的错误：Error occurs for running JobItemRunTask. exceptionDesc=com.caucho.hessian.client.HessianRuntimeException: java.net.ConnectException: Connection refused at com.yihaodian.architecture.hedwig.common.hessian.HedwigHessianProxy.sendRequest(HedwigHessianProxy.java:214)**

    * **A2:** 这个可能是Kira内部服务配置的端口号和实际使用的端口号不匹配导致的。 检查下Kira内部配置服务的AppProfile配置中port属性的值是否和实际使用的tomcat/jetty所用的端口是否一致。
或者登陆到平台上看下该定时任务的执行环境里显示的端口号是否和实际使用的tomcat/jetty所用的端口是否一致。
   
* **Q3. 任务执行日志里显示“任务派送失败”，并在任务执行明细里面发现报类似下面错误：Error occurs for running JobItemRunTask. exceptionDesc=com.caucho.hessian.client.HessianRuntimeException: java.io.FileNotFoundException: http://XXX.XXX.XXX.XXX:9090/XXX/XXX/centralScheduleService**

    * **A3：** 上面url在浏览器中直接访问会提示404错误，这个一般是spring配置文件中Kira内部服务的AppProfile的相关配置值和实际运行的web容器（tomcat或jetty）的配置不一致导致的。比如所配置的Kira内部服务的serviceAppName属性和server.xml里面所指定的path属性不一致。还有一种可能就是所配置Kira内部服务的urlPattern值和web.xml中配置的远程服务的servlet-mapping的url-pattern不一致导致的。还有一种可能就是这个请求路径是否被App内部实现给拦截了。或者 <bean name="/centralScheduleService" 这个配置信息放在了其他文件里导致服务没暴露成功？
    
    
* **Q4.任务执行日志里显示“任务派送失败”，并在任务执行明细里面发现报类似下面错误：Can not create jobItemList. No environment available. May no such trigger exist at client side?**

    * **A4:** 这个是由于该App当前被调用的进程中没有找到该定时任务的配置导致的。这个一般出现在开发测试阶段同一个App的不同开发人员本地定时任务个数配置不一致的情况下导致的。开发人员A所开发的定时任务在开发人员B机器上被kira远程调用了，而开发人员B的机器上又没有开发人员A所开发的定时任务。 此时需要App内各开发人员协调下，确保同一个app的定时任务个数一致，比如在定好定时任务后，各自都提交相关定时任务配置到svn上，然后各个开发人员同步更新以保持一致，至于具体的业务逻辑实现可以放到后面实现。

* **Q5.我本地也没重启也没改啥配置，刚才我注册到kira平台上的定时任务配置为啥现在看不见了？** 
    
    * **A5:** 这个一般是由于同一个app内的不同开发人员其本地的定时任务配置个数不一样导致的，比如开发人员A本地代码有2个定时任务，此时开发人员A启动了webapp，然后就在kira平台上能看到这2个定时任务的配置注册上来了。 开发人员B本地代码此时只有1个定时任务，此时开发人员B启动webapp，就会把开发人员A中的1个定时任务给下线了。所以App内的开发人员得协调下，确保同一个App的定时任务个数一致，比如在定好定时任务后，各自都提交相关定时任务配置到svn上，然后各个开发人员同步更新以保持一致，至于具体的业务逻辑实现可以放到后面实现。

* **Q6.在开发测试阶段每次修改下本地代码中定时任务的配置信息都要升级下version属性，很麻烦？**
    
    * **A6：** 为了保证对定时任务配置的管理，引入了version号方便进行管理。 在staging和production环境，以修改app代码方式修改定时任务配置的方式需要严格按version号升级方式进行。如果是在开发测试阶段，由于定时任务配置变更可能会比较频繁，修改version号比较麻烦，此时可以按如下步骤在app代码中修改某个定时任务的配置后，快速把相关最新配置注册到Kira平台上: 
        * a. 先注释掉定时任务配置文件中<property name="triggers"><list>里面你那个最新修改的定时任务，比如下面的modified-Trigger
            <property name="triggers">
                <list>
                    <!-- <ref local="modified-Trigger" /> -->
                </list>
            </property>
        * b.然后保存该文件，启动你的webapp工程，此时会发现kira平台上会将此定时任务下线。
        * c.关闭你的webapp工程，然后再打开上面的注释，再次启动webapp，一会会发现最新的modified-Trigger定时任务配置已经注册到平台上了。
    
* **Q7. 启动时错误日志提示"The CentralScheduleServiceExporter must be initialized for using kira-client"**
   
    * **A7:** CentralScheduleServiceExporter未初始化，即使用kira-client所必备的CentralScheduleService未能在2分钟内准备好。。可能原因如下：比如是不是spring多版本共存（比如2.5.6和3.2.0共存）？是不是打断点了？ 或者是不是抛出其他异常导致spring初始化过程中断？ 或者是本地环境比较差导致app的应用无法在2分钟内启动完成？建议启动时候很多能异步处理就异步处理，不至于spring的root context初始化时间太长，导致CentralScheduleServiceExporter在2分钟内不能被初始化。所以最好把定时任务相关配置放到和暴露Kira内部服务的配置在同一层spring appcontext.
        （如果是因为测试环境偶尔出现网络或数据库压力太大等原因导致相关app启动期间所需资源、数据很久才能准备完毕，可以设置KiraClientConfig的waitForResourceTimeoutMillisecond属性，把默认2分钟改成更长时间。）


* **Q8.如果执行日志里显示“任务派送失败”，并在任务执行明细里面发现报类似下面的错误：Error occurs for running JobItemRunTask. exceptionDesc=com.caucho.hessian.client.HessianRuntimeException: java.net.SocketTimeoutException: connect timed out at com.yihaodian.architecture.hedwig.common.hessian.HedwigHessianProxy.sendRequest(HedwigHessianProxy.java:214))**
    
    * **A8：**
        * 1.这个可能是Kira内部服务配置的端口号和实际使用的端口号不匹配导致的。 检查下Kira内部服务的AppProfile配置中port属性的值是否和实际使用的tomcat/jetty所用的端口是否一致。
        * 2.确定机器的防火墙是否是关闭状态

* **Q9.No running and available centralScheduleService found**
   
    * **A9：** 异常问题这个问题可能由2种情况导致： 
        * 1.kira的服务列表中，没有可以调度的job的机器； 
        * 2.在kira的配置中，设置了指定的IP去执行定时job，并同时设置了【是否只在指定的地点执行任务】为 true的话，同时指定的IP不在当前的kira的定时任务调度的列表中，也会出现这个问题。建议重启一下当前运行的项目，启动完成后，再看一下kira的job中，你的机器IP是否在job的调度列表中


