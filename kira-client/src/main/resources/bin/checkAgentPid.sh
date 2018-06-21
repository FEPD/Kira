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

#!/bin/bash
# chkconfig: 2345 45 65
# description: kira-client-agent daemon

#shell env bash
source ~/.bashrc

BASE_BIN_DIR=$(cd "$(dirname "$0")"; cd ..; pwd)
echo user.dir is: $BASE_BIN_DIR
BASE=`basename $0`


echo_success(){
  echo "====Kira Agent started successful====="
}

echo_failure(){
  echo "==== Kira Agent started fail===="
}

check_pid() {
PID=$(ps -ef |grep "kira.client.ShellMainBootStrap" | grep -v grep | awk '{print $2}' | head -1)
    [ x"$PID" == x ] && return 0 || return 1  #1:exist; 0:not exist
}

start() {
    check_pid

    if [ x$PID != x ]; then
        echo "${BASE} (pid $PID) is running..."
    else
       echo -n "Starting $BASE:"
        sh $BASE_BIN_DIR/bin/start.sh > /dev/null 2>&1
        retval=$?
        sleep 0.1
        check_pid
         if [ x$PID != x -a $retval -eq 0 ]; then
            echo_success
            echo
        else
            echo_failure
            echo
        fi
    fi
}

stop() {
    check_pid
    if [ x$PID == x ]; then
        echo "${BASE} is stopped."
    else
        echo -n "Stopping $BASE:"
        retval=$?
        sleep 0.5
        check_pid
        if [ x$PID == x -a $retval -eq 0 ]; then
            echo_success
            echo
        else
            echo_failure
            echo
        fi
    fi
}

status() {
    check_pid
    if [ x$PID != x ]; then
        echo "${BASE} (pid $PID) is running..."
    else
        echo "${BASE} is stopped."
    fi
}

check() {
    check_pid
    if [ x$PID != x ]; then
        echo "${BASE} (pid $PID) is running..."
    else
        echo -e "${BASE} is stopped, trying to start:"
        start
    fi
}

#main
case $1 in
    start)
        start
        ;;
    stop)
        stop
        ;;
    ''|restart)
        stop
        sleep 0.5
        start
        ;;
    status)
        status
        ;;
    check)
        check
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|status|check}"
        exit 1
esac

