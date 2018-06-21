# Kira Deployment
This quick start guide is a detailed instruction of setting up Kira system 
on your local machine to scheduling job.

### Prerequisite 
* The following softwares are assumed installed:
  
  1. 64bit OS, Linux/Unix/Mac is recommended or win7/8/10
  2. 64bit JDK 1.6~1.7
  3. Maven 3.1.x
  4. Git
  5. Zookeeper 3.4.10-releases+ 
  6. mysql 5.6.x+
  7. Tomcat 6.x or 7.x

### Clone AND Build 

    > git clone https://github.com/FEPD/Kira.git
    > cd kira
    > mvn -Prelease-all -DskipTests clean install -U

### Start Zookeeper
* Download or use your owned zookeeper to provide service
* Follow instructions in http://kafka.apache.org/documentation.html#quickstart

### Start Mysql
* Download or use your owned mysql to provide service
* Follow instructions in https://dev.mysql.com/doc/refman/5.6/en/mysql-installer.html
* Create Kira DB, then Execute [SQL File](kira-manager/src/main/resources/db/kira.sql), insert Kira basic data

### Start Tomcat
* Download or use your owned tomcat to provide service
* Follow instructions in http://tomcat.apache.org/tomcat-6.0-doc/setup.html

### Start Kira-Manager
* Please config kira db properties in conf/dataSource_data.properties
* Please config kira zookeeper properties in zookeeper-cluster.properties
* Deploy kira-manager.war package in tomcat and start
* Please go to http://localhost:8080
* Login using the default account(account:test / password:test123), login can access the company LDAP or erp, providing an interface
