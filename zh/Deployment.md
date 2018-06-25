# Kira Deployment

* 快速部署Kira定时任务调度系统说明

### 环境准备 

 * 下面相关的软件需要提前安装： 
 
  1. 64bit OS, Linux/Unix/Mac is recommended or win7/8/10
  2. 64bit JDK 1.6~1.7
  3. Maven 3.1.x
  4. Git
  5. Zookeeper 3.4.10-releases+ 
  6. mysql 5.6.x+
  7. Tomcat 6.x or 7.x

### 克隆 & 编译

    > git clone -b 2.0-version https://github.com/FEPD/Kira
    > cd kira
    > mvn -Prelease-all -DskipTests clean install -U

### 部署 Zookeeper

* 下载自己需要的 Zookeeper 版本
* 部署参考 http://kafka.apache.org/documentation.html#quickstart

### 部署 Mysql

* 下载自己需要的 Mysql 版本
* 部署参考 https://dev.mysql.com/doc/refman/5.6/en/mysql-installer.html
* 创建一个kira的db库，然后执行  [SQL File](../kira-manager/src/main/resources/db/kira.sql), 创建基本的库表和基础数据

### 部署 Tomcat

* 下载自己需要的 Tomcat 版本
* 部署参考 http://tomcat.apache.org/tomcat-6.0-doc/setup.html

### 部署 Kira-Manager

* 配置Kira db 数据源文件，添加 db 链接和认证信息: conf/dataSource_data.properties
* 配置 Kira 的 Zookeeper 文件，添加 zookeeper地址：zookeeper-cluster.properties
* 将编译好的 kira-manager.war 部署到 Tomcat中
* 访问本地 http://localhost:8080，登录Kira 管理端页面
* 使用默认的登录账号，登录认证也可以接入公司的LDAP和ERP

 






