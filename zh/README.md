# Kira
[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

### 简述

* kira 是一个分布式定时任务调度平台，具有容错性，高性能，弹性水平扩展，对外提供一个高可靠的定时任务服务。


### 整体架构

![Kira_architecture](https://github.com/FEPD/Kira/blob/1.0-version/kira-manager/src/main/resources/files/kira_architecture.jpg)

 * Kira 包括下面组件：
    * zookeeper 集群
    * Kira 集群(Kira-Manager)
        * web
        * Kira-Server
        * Kira-Schedule
    * Mysql
    * Kira-cluster(对外提供接入Kira的Jar包)  
 
* Kira 系统模块
 
| 序号 | 模块 | 是否公共组件 | 功能 |
| --- | --- | --- | --- |
| 1 | kira-client  | 是 |供接入方接入使用的公共组件|
| 2 | kira-manager | 否 |提供统一管理平台和统一调度等功能。多台部署Kira-manager的服务器组成一个分布式高可用集群。 |
| 3 | kira-scheduler | 否 | 为多维度调度器工具库。前期只实现基于时间的定时任务调度子模块kira-time-scheduler，目前只供Kira-manager模块使用。| 

* 系统总体流程如下：

    * a)	定时任务相关pool使用kira-client公共组件进行接入。
    * b)	使用kira-client公共组件接入的pool 系统启动后，其相关的定时任务配置被注册到Zookeeper Cluster上，并且对外暴露了Kira-client组件提供的Hedwig服务，该服务用于被Kira-manager远程调用触发执行pool的业务方法。
    * c)	Kira-manger 集群能感知到注册的定时任务，把相关的定时任务信息保存到数据库，并根据定时任务的配置进行调度和管理。当定时任务的触发时间到了以后，远程调用执行定时任务对应的pool所暴露的上述Kira-clieng组件提供的Hedwig服务，进而触发执行pool的业务方法。
    * d)	Pool在其业务方法执行前后，kira-client组件将会把任务执行状态信息通过Jumper消息中间件上报给kira-manager集群进行保存。
    * e)	各方通过使用Kira-manger 集群提供的web管理界面进行日常定时任务管理操作。


### 功能

- 开源Quartz基础上进行扩展，兼容Quartz用法
- 任务状态机模型
- 毫秒级别的定时任务调度
- 任务支持同步和异步调度
- 异地多活
- 支持跨IDC定时任务容灾切换
- 支持定时任务多IDC多活
- 使用 Actor 模型来采集job数据
- 内置任务服务注册和发现模块
- 支持Java和Shell任务调度
- Shell 任务使用agent部署 
- 任务调度数据展示
- 动态修改任务配置参数
- 失败检查和故障转移
- 任务失败预警
- 提供可视化任务管理
- 等等

### 用户手册
- [Quick Start](QuickStart.md)
- [FAQ](FAQ.md)
- [KiraConfigDemo](KiraConfigDemo.md)


### 部署手册
- [Deployment](Deployment.md)

### 翻译

* [English](../README.md)
* [简体中文](README.md)

### 贡献者

* [chenchangjian](https://github.com/ccj119)
* [chenyuyao](https://github.com/CYYemily)
* [jianglie](https://github.com/ArcherJ)
* xucongwei
* yaohaiqing
* [zhoufeiqiang](https://github.com/DavidZ1)






