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

BASEDIR=`dirname $0`/..
BASEDIR=`(cd "$BASEDIR"; pwd)`
PROGRAM=$BASEDIR/bin/checkAgentPid.sh
CRONTAB_CMD=" * * * * * /bin/sh  $PROGRAM  > /dev/null 2>&1 &"
(crontab -l 2>/dev/null | grep -Fv $PROGRAM; echo "$CRONTAB_CMD") | crontab -
COUNT=`crontab -l | grep $PROGRAM | grep -v "grep"|wc -l `
if [ $COUNT -lt 1 ]; then
        echo "fail to add crontab $PROGRAM"
        exit 1
fi