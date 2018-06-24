# Kira
[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

### Overview
* Kira is a distributed platform with scalability and fault tolerant, and high available job scheduling service.


### Architecture

![Kira_architecture](kira-manager/src/main/resources/files/Kira_architecture.jpg)


* Kira include components：
    * Zookeeper Cluster
    * Kira Cluster(Kira-Manager)
        * Web
        * Kira-Server
        * Kira-Schedule 
    * Mysql
    * kira-client(
provide the kira-client jar external to the applications)
 
 
* Kira System Module 
 
| NO. | module | Is the public component | Features |
| --- | --- | --- | --- |
| 1 | kira-client  | yes | Common components for access party access|
| 2 | kira-manager | no | Provides a unified management platform and unified scheduling functions. Multiple servers deploying Kira-manager form a distributed high availability cluster. |
| 3 | kira-scheduler | no | Multi-dimensional scheduler tool library. In the early stage, only the time-based task scheduling sub-module kira-time-scheduler was implemented. Currently, it is only used by the Kira-manager module.| 


### Feature

- Open source quartz extension, compatible with quartz usage
- State machine model of model
- Millisecond-level distributed timing job scheduling
- multi-datacenter of active deployment support
- Supports cross-IDC timed disaster recovery
- Supports multi-active datacenter of scheduled job
- Use [Actor Model](https://akka.io/) to collect job data
- Built-in task service registration and discovery module
- Supports java and shell job scheduling
- Shell tasks deploy using agents
- Job scheduling data visualisation
- Dynamically modify task configuration parameters
- Fail detection & failover support
- Task failure warning
- Provides visual job management
- and more

### User Guide
- [Quick Start](QuickStart.md)
- [FAQ](FAQ.md)
- [KiraConfigDemo](KiraConfigDemo.md)


### Deployment
- [Deployment](Deployment.md)

### Translations

* [English](README.md)
* [简体中文](zh/README.md)

### Contributors


* [chenchangjian](https://github.com/ccj119)
* [chenyuyao](https://github.com/CYYemily)
* [jianglie](https://github.com/ArcherJ)
* xucongwei
* yaohaiqing
* [zhoufeiqiang](https://github.com/DavidZ1)



