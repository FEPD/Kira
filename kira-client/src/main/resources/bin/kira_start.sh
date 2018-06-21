#
# Copyright 2018 jd.com
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#!/usr/bin/env bash

#PRGDIR=`dirname "$0"`
CURRENT_PATH=$(cd "$(dirname "$0")"; cd ..; pwd)
echo kira.user.dir is: $CURRENT_PATH

LOG_DIR="/export/Logs"
echo kira.log.dir is : $LOG_DIR

AGENT_DIR="$CURRENT_PATH/kira-client-0.0.1-SNAPSHOT-agent"
echo kira.agent_dir is: $AGENT_DIR

STD_OUT="${LOG_DIR}/app_catalina.log"

echo

if [ $? -eq 0 ];then
    echo "Kira Agent is alive,shutdown now"
else
    echo "Kira Agent is not alive"
fi


#check kira agent pid
PID=$(ps -ef | grep "kira.client.ShellMainBootStrap" | grep -v grep | awk '{print $2}' | head -1)
if [ $PID > 0  ];then
  kill -9  $PID
  echo "Kill Kira Agent Pid " + $PID
else
  echo "Kira Agent is not alive, shutdown now"
fi

#end

JMX_PORT=3997

CP_OPTS="-cp ${AGENT_DIR}/config:${AGENT_DIR}/libs/*"

#根据申请的系统的硬件配置来进行调整
#JVM_OPTS="-server -Xms2g -Xmx2g -XX:PermSize=128m -XX:MaxPermSize=256m -XX:MaxDirectMemorySize=512m -XX:ParallelCMSThreads=2 -XX:+HeapDumpOnOutOfMemoryError -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:+ExplicitGCInvokesConcurrent -XX:CMSInitiatingOccupancyFraction=70 -XX:+UseCMSInitiatingOccupancyOnly -XX:NewRatio=2 -XX:+PrintGCDetails -XX:+PrintGCTimeStamps"

#JMX 根据需要的的话，可以自己开启，默认不开启了
#JMX_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.port=${JMX_PORT}"
GCLOG_OPTS="-Xloggc:${LOG_DIR}/kira-agent-gc.log"
PRETEND="-Dtomcat=pretend"

OPTS="${CP_OPTS} ${JVM_OPTS} ${JMX_OPTS} ${GCLOG_OPTS} ${PRETEND}"

MAIN="com.yihaodian.architecture.kira.client.ShellMainBootStrap"

echo "Starting in ${LISTEN_IP} ..."

echo ${LOG_DIR}/kira-agent.log

exec java ${OPTS} ${MAIN} $*> "$STD_OUT" 2>&1 &