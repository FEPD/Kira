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
echo user.dir is: $CURRENT_PATH

#kill kira agent pid
PID=$(ps -ef | grep "kira.client.ShellMainBootStrap" | grep -v grep | awk '{print $2}' | head -1)
if [ $PID > 0  ];then
  kill -9  $PID
  echo "Kill Kira Agent Pid " + $PID
else
  echo "Kira Agent is not alive, shutdown now"
fi