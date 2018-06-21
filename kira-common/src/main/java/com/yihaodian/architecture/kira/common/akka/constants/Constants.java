/*
 *  Copyright 2018 jd.com
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.yihaodian.architecture.kira.common.akka.constants;


public interface Constants {

  public static final String VALUE_NULL = "NULL";

  public final static int CALLTYPE_REPLY = 1;
  public final static int CALLTYPE_NOREPLY = 2;

  public static final String CALL_SYNC = "syncPool";
  public static final String CALL_ONEWAY = "oneway";
  public static final String CALL_FUTURE = "future";

  public static final String CONNECTOR = "-";
  public static final String SEPERATOR_UNDERSCORE = "_";
  public static final String SEPERATOR_SLASH = "/";

  public static final String HOSTS_CONNECTOR = ",";
  public static final String CHILD_CONNECTOR = "$";
  public static final String AkkaSystemName = "YHDendpointSystem";
  public static final String ClientAkkaSystemName = "YHDemitterSystem";
  public static final String CLIENT_ACKTIMEOUT = "client.ackTimeout";

  public static final int DEFAULT_METHOD_CAPACITY = 800;
  public static final int DEFAULT_ENDPOINT_CAPACITY = 10000;
  public static final int INTEGER_BARRIER = Integer.MAX_VALUE / 2;

  public static final String BALANCER_NAME_ROUNDROBIN = "RoundRobin";
  public static final int DEFAULT_CHILDCOUNT = 200;
  public static final int DEFAULT_WORKERCOUNT = 10;
  public static final int DEFAULT_RESIZER_LOWBOUND = 15;

  public static final int DEFAULT_RECEIVERCOUNT = 100;
  public final static int MESSAGE_TYPE_SUCCESS = 1;
  public final static int MESSAGE_TYPE_EXCEPTION = 2;
  public final static int MESSAGE_TYPE_SERVICE_EXCEPTION = 3;
  public final static int MESSAGE_TYPE_SECURITY_EXCEPTION = 4;
  public final static int MESSAGE_TYPE_TIMEOUT = 5;
  public static final int DEFAULT_VALUE_PORT = 20001;
  public static final String SYNC_PIPELINE_FACTORY_NAME = "syncPipelineFactory";
  public static final String DEFAULT_SYNC_PIPELINE_FACTORY_NAME = "com.yhd.arch.photon.organ.netty.BasePiplineFactory";
  public static final String ASYNC_PIPELINE_FACTORY_NAME = "asyncPipelineFactory";
  public static final String DEFAULT_ASYNC_PIPELINE_FACTORY_NAME = "AsyncPiplineFactory";
  public static final int DEFAULT_SYNC_MAX_CHANNEL = 1000;
  public static final int DEFAULT_TICK_DURATION = 10;
  public static final String PHOTON_PROFIX = "Photon said:";

  public static final int DEFAULT_CHANNEL_COUNT = 50;
  public static final int DEFAULT_ACTOR_COUNT = 10;

  public static final String ENABLE = "enable";
  public static final int DEFAULT_THROTTLE_TPS = 3000;
  public static final int MIN_THROTTLE_TPS = 1;
  public static final int MAX_THROTTLE_TPS = 100000;
  public static final int MIN_THROTTLE_INTERVAL = 50;
  public static final int MAX_THROTTLE_INTERVAL = 10000;

  public static final String SERIALIZE_TRIGGER_CUSTOMIZE = "customize";
  public static final String HEADER_KEY_CODEC_TYPE = "ct";
  public static final String BODY_KEY_DATA = "bd";

}
