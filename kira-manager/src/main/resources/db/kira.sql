#
#  Copyright 2018 jd.com
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

CREATE DATABASE  kira CHARACTER SET utf8 COLLATE utf8_general_ci;

USE  kira;

-- ----------------------------
-- Table structure for hdc_menu
-- ----------------------------
DROP TABLE IF EXISTS `hdc_menu`;
CREATE TABLE `hdc_menu` (
  `id` int(10) NOT NULL AUTO_INCREMENT  COMMENT '自增id',
  `pid` int(10) DEFAULT '0' COMMENT '父菜单',
  `code` varchar(50) NOT NULL COMMENT 'code',
  `name` varchar(100) DEFAULT NULL COMMENT 'memu name',
  `url` varchar(100) DEFAULT NULL COMMENT 'memu url',
  `app` varchar(150) DEFAULT NULL COMMENT 'app name ',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1145 DEFAULT CHARSET=utf8 COMMENT 'memu info';

-- ----------------------------
-- Table structure for hdc_pvg
-- ----------------------------
DROP TABLE IF EXISTS `hdc_pvg`;
CREATE TABLE `hdc_pvg` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `roleId` int(5) NOT NULL COMMENT '角色id',
  `associationId` int(10) NOT NULL COMMENT '关联id',
  `associationType` int(11) NOT NULL COMMENT '1代表菜单权限;2代表数据库权限',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5766 DEFAULT CHARSET=utf8 COMMENT 'menu pvg';

-- ----------------------------
-- Table structure for hdc_role
-- ----------------------------
DROP TABLE IF EXISTS `hdc_role`;
CREATE TABLE `hdc_role` (
  `roleCode` varchar(50) NOT NULL  COMMENT 'role code ',
  `roleName` varchar(100) DEFAULT NULL  COMMENT 'role name ',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time ',
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `appName` varchar(150) DEFAULT NULL COMMENT 'app name ',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=101 DEFAULT CHARSET=utf8 COMMENT 'hdc role table ';

CREATE UNIQUE INDEX index_roleCode ON hdc_role (roleCode);

-- ----------------------------
-- Table structure for hdc_user
-- ----------------------------
DROP TABLE IF EXISTS `hdc_user`;
CREATE TABLE `hdc_user` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `name` varchar(50) DEFAULT NULL COMMENT '用户名',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '自增id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COMMENT 'hdc user ';

-- ----------------------------
-- Table structure for `job_status`
-- ----------------------------
DROP TABLE IF EXISTS `job_status`;
CREATE TABLE `job_status` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `name` varchar(50) NOT NULL COMMENT '状态名称',
  `description` varchar(512) DEFAULT NULL COMMENT '状态描述',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用于保存job的各种状态定义信息';

-- ----------------------------
-- Table structure for `operation`
-- ----------------------------
DROP TABLE IF EXISTS `operation`;
CREATE TABLE `operation` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键，操作编号。每个部分预留100个编号，前50个为非只读操作，后50个为只读操作。',
  `name` varchar(100) NOT NULL COMMENT '操作名',
  `display` varchar(256) NOT NULL COMMENT '此操作在界面上显示的字符串',
  `description` varchar(512) DEFAULT NULL COMMENT '对此操作的描述',
  `type` int(11) NOT NULL COMMENT '操作类型。0:只读操作，1：非只读操作。',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'operation log';

CREATE UNIQUE INDEX index_name ON operation (name);

-- ----------------------------
-- Table structure for `timer_trigger_schedule`
-- ----------------------------
DROP TABLE IF EXISTS `timer_trigger_schedule`;
CREATE TABLE `timer_trigger_schedule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `app_id` varchar(50) NOT NULL COMMENT 'app id ',
  `trigger_id` varchar(255) NOT NULL COMMENT 'trigger id ',
  `start_time` BIGINT(13) NULL COMMENT '开始时间,可为空，为空时代表此定时任务未分配给服务器进行调度或者刚被分配给某个服务器并且那个服务器还未刷新此定时任务的调度信息',
  `previous_fire_time` BIGINT(13) NULL COMMENT '上一次执行时间，可为空，为空时代表以前没执行过',
  `next_fire_time` BIGINT(13) NULL COMMENT '下一次执行时间，可为空，为空时代表此定时任务未分配给服务器进行调度或者刚被分配给某个服务器并且那个服务器还未刷新此定时任务的调度信息或者以后不需要再执行了',
  `times_triggered` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '已经被触发执行的次数',
  `assigned_server_id` varchar(100) NULL COMMENT '负责处理此定时任务调度的服务器id,可为空，为空时代表此定时任务未分配给服务器进行调度。',
  `create_time` BIGINT(13) NOT NULL COMMENT '创建时间。',
  `data_version` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '数据的版本号。',
  PRIMARY KEY (`id`),
  KEY `idx_app_id` (`app_id`),
  KEY `idx_next_fire_time` (`next_fire_time`),
  KEY `idx_assigned_server_id` (`assigned_server_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用于记录定时任务调度信息.';


CREATE UNIQUE INDEX index_app_trigger ON timer_trigger_schedule (app_id, trigger_id);


-- ----------------------------
-- Table structure for `job_run_statistics`
-- ----------------------------
DROP TABLE IF EXISTS `job_run_statistics`;
CREATE TABLE `job_run_statistics` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `app_id` varchar(50) NOT NULL COMMENT 'app id ',
  `trigger_id` varchar(255) NOT NULL COMMENT 'trigger id ',
  `begin_time` datetime DEFAULT NULL COMMENT '统计区间：起始时间点。',
  `end_time` datetime DEFAULT NULL COMMENT '统计区间：结束时间点。',
  `sample_count` int(11) DEFAULT NULL COMMENT '统计样本个数。',
  `max_in_seconds` int(11) DEFAULT NULL COMMENT '最大耗时（秒）。',
  `min_in_seconds` int(11) DEFAULT NULL COMMENT '最小耗时（秒）。',
  `avg_in_seconds` int(11) DEFAULT NULL COMMENT '平均耗时（秒）。',
  `create_time` datetime NOT NULL COMMENT '创建时间。',
  PRIMARY KEY (id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用于保存Job运行耗时统计信息（只统计运行成功的）。';

CREATE UNIQUE INDEX index_app_triggers ON job_run_statistics (app_id, trigger_id);

-- ----------------------------
-- Table structure for `job`
-- ----------------------------
DROP TABLE IF EXISTS `job`;
CREATE TABLE `job` (
  `id` varchar(40) NOT NULL COMMENT 'job id ',
  `trigger_metadata_id` bigint(20) NOT NULL COMMENT '对应trigger_metadata表主键，对于暂不执行的job调度任务，需要确保trigger_metadata_id对应最新版本。',
  `app_id` varchar(50) DEFAULT NULL COMMENT 'appId，冗余数据，防止跨表查询。',
  `trigger_id` varchar(255) DEFAULT NULL COMMENT 'triggerId，冗余数据，防止跨表查询。',
  `version` varchar(100) DEFAULT NULL COMMENT 'version，冗余数据，防止跨表查询。',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `last_update_time` datetime DEFAULT NULL COMMENT '上次更新时间',
  `manually_scheduled` tinyint(1) NOT NULL COMMENT '是否为人工调度执行的任务。1则为人工创建的。0为调度服务器创建的。',
  `created_by` varchar(50) NOT NULL COMMENT '创建此次任务的用户名或者调度集群机器名。如果为手动触发的，则为用户名；如果是调度服务器自动调度任务，则为调度服务器名。',
  `run_at_time` datetime DEFAULT NULL COMMENT '此任务的执行时间点。如果为空则表示暂不执行的任务，对其可进行更新操作，以后可以手动触发执行。',
  `job_status_id` int(11) NOT NULL COMMENT '任务最新状态',
  `arguments_as_json_array_string` text NOT NULL COMMENT '调用服务时携带的执行方法所需的参数值数组：Json数组格式的字符串，不允许为空，如果方法没有参数则设置为[]。',
  `result_data` text COMMENT '任务执行结果详细信息',
  `data_version` int(11) NOT NULL DEFAULT 0 COMMENT 'job数据的版本号。',
  PRIMARY KEY (`id`),
  KEY `idx_triggerMetadataId` (`trigger_metadata_id`),
  KEY `idx_triggerversion` (`app_id`, `trigger_id`, `version`),
  KEY `idx_jobStatusId` (`job_status_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用于保存job最新调度状态信息';

-- ----------------------------
-- Table structure for `job_history`
-- ----------------------------
DROP TABLE IF EXISTS `job_history`;
CREATE TABLE `job_history` (
  `id` varchar(40) NOT NULL COMMENT 'job id ',
  `job_id` varchar(40) NOT NULL COMMENT 'job表主键。唯一标识一个正在处理的任务ID。',
  `job_status_id` int(11) NOT NULL COMMENT '任务状态',
  `result_data` text COMMENT '任务执行结果详细信息',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_jobId` (`job_id`),
  KEY `idx_jobStatusId` (`job_status_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'job history';


-- ----------------------------
-- Table structure for `job_item`
-- ----------------------------
DROP TABLE IF EXISTS `job_item`;
CREATE TABLE `job_item` (
  `id` varchar(40) NOT NULL COMMENT 'job id ',
  `job_id` varchar(40) NOT NULL COMMENT '对应Job表的主键',
  `service_url` varchar(4096) DEFAULT NULL COMMENT '调用执行此job的服务url',
  `arguments_as_json_array_string` text NOT NULL COMMENT '调用服务时携带的执行方法所需的参数值数组：Json数组格式的字符串，不允许为空，如果方法没有参数则设置为[]。',
  `job_status_id` int(11) NOT NULL COMMENT '任务最新状态',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `last_update_time` datetime DEFAULT NULL COMMENT '上次更新时间',
  `result_data` text COMMENT '任务执行结果详细信息',
  `data_version` int(11) NOT NULL DEFAULT 0 COMMENT 'jobItem数据的版本号。',
  PRIMARY KEY (`id`),
  KEY `idx_jobStatusId` (`job_status_id`),
  KEY `idx_job_id` (`job_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用于保存每个任务项(可看做Job表对应任务的子任务)的最新状态。';

-- ----------------------------
-- Table structure for `job_item_history`
-- ----------------------------
DROP TABLE IF EXISTS `job_item_history`;
CREATE TABLE `job_item_history` (
  `id` varchar(40) NOT NULL COMMENT 'job id',
  `job_item_id` varchar(40) NOT NULL COMMENT 'job_item表主键。唯一标识一个正在处理的任务项ID。',
  `job_status_id` int(11) NOT NULL COMMENT '任务状态',
  `result_data` text COMMENT '任务执行结果详细信息',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_jobStatusId` (`job_status_id`),
  KEY `idx_jobItemId` (`job_item_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='保存job_item的历史状态信息，便于查询各种状态变化日志。';


-- ----------------------------
-- Table structure for `trigger_metadata`
-- ----------------------------
DROP TABLE IF EXISTS `trigger_metadata`;
CREATE TABLE `trigger_metadata` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `app_id` varchar(50) NOT NULL COMMENT 'app id ',
  `trigger_id` varchar(255) NOT NULL COMMENT '定义trigger的beanId',
  `version` varchar(100) NOT NULL COMMENT '此定时任务配置的版本号，如果含有snapshot-timestamp说明是手动配置过了。',
  `priority` int(11) NOT NULL DEFAULT 5 COMMENT '优先级，高优先级的定时任务将被优先调度执行。取值大于0，默认值为5。',
  `manually_created` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否是手动创建的定时任务。',
  `manually_created_by` varchar(50) DEFAULT NULL COMMENT '手动创建者',
  `target_app_id` varchar(50) DEFAULT NULL COMMENT '指定appId,用于决定将被此定时任务调用的服务方法所属的appId。 如果为空，则表示调用自己暴露的服务。',
  `target_trigger_id` varchar(255) DEFAULT NULL COMMENT '指定triggerId,用于决定将被此定时任务调用的服务方法所属的triggerId。 如果为空，则表示调用自己暴露的服务。',
  `target_method` varchar(255) DEFAULT NULL COMMENT '执行的方法名, 如果为空则表示使用被调用的定时任务所使用的方法名。',
  `target_method_argTypes` text DEFAULT NULL COMMENT '执行的方法的参数类型数组，json数组格式，如果方法没有参数则设置为[]. 如果为空则表示使用被调用的定时任务所使用的参数类型数组。',
  `trigger_type` varchar(50) NOT NULL COMMENT 'SimpleTrigger 或者CronTrigger',
  `start_time` datetime DEFAULT NULL COMMENT '设置的开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '设置的结束时间',
  `start_delay` bigint(20) DEFAULT NULL COMMENT '启动延时（毫秒）。如果triggerType为CronTrigger则为空，如果为SimpleTrigger则非空。',
  `repeat_count` int(11) DEFAULT NULL COMMENT '重复次数。如果triggerType为CronTrigger则为空，如果为SimpleTrigger则非空。',
  `repeat_interval` bigint(20) DEFAULT NULL COMMENT '重复间隔（毫秒）。如果triggerType为CronTrigger则为空，如果为SimpleTrigger则非空。',
  `cron_expression` varchar(255) DEFAULT NULL COMMENT '定义类似quartz的cron表达式，具体可以参考quartz的官方文档。如果triggerType为CronTrigger则非空，如果为SimpleTrigger则为空。',
  `description` varchar(512) DEFAULT NULL COMMENT '对此定时任务的描述信息',
  `misfire_instruction` int(11) NOT NULL COMMENT '检测到漏执行后的策略，参考quartz的misfire_instruction说明。如果不设置将采用Quartz设置的默认值。',
  `asynchronous` tinyint(1) NOT NULL COMMENT '是否异步执行',
  `only_run_on_single_process` tinyint(1) NOT NULL COMMENT '触发时是否只派送到一个执行地点执行。',
  `locations_to_run_job` text DEFAULT NULL COMMENT '定时任务的执行地点列表，逗号分隔的ip:port列表。',
  `limit_to_specified_locations` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否限制任务只能在指定的执行地点执行。默认不限制。如果没有限制，则平台在发现可用的执行地点都不可用的时候会自动选择App中的其他未指定机器来执行任务。',
  `scheduled_locally` tinyint(1) NOT NULL COMMENT '是否在本地进行任务调度。',
  `disabled` tinyint(1) NOT NULL COMMENT '是否进行任务调度。',
  `requests_recovery` tinyint(1) NOT NULL DEFAULT 0 COMMENT '当正在触发执行定时任务时kira服务器异常退出，这时是否要求进行恢复操作即是否需要让服务器自动重跑此任务。',
  `arguments_as_json_array_string` text DEFAULT NULL COMMENT '调用服务时携带的执行方法所需的参数值数组：Json数组格式的字符串，如果方法没有参数值则设置为[]。 如果为空则表示使用被调用的定时任务所使用的参数值数组。',
  `concurrent` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否允许业务方法并发执行。默认为true，即每次到了定时任务的触发时间点的时候，不管现在是否有此定时任务对应的业务方法在执行，都会触发执行此定时任务的业务方法。只有当only_run_on_single_process为1的时候这个属性才有效。',
  `unregistered` tinyint(1) NOT NULL COMMENT '是否未在ZK上注册。',
  `unregistered_update_time` datetime DEFAULT NULL COMMENT 'unregistered状态的更新时间。',
  `deleted` tinyint(1) NOT NULL COMMENT '是否已经标记为删除状态,用户在调度平台上进行手动删除会标记此为删除状态，同时会删除ZK上对应节点。',
  `deleted_update_time` datetime DEFAULT NULL COMMENT 'deleted状态的更新时间。',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `finalized_time` datetime DEFAULT NULL COMMENT '记录此定时任务的最后一次执行后的时间',
  `rum_time_threshold` bigint(20) DEFAULT NULL COMMENT '任务运行时间阀值（毫秒），即任务最大的预计执行耗时。 可为空.',
  `copy_from_master_to_slave_zone` tinyint(1) NOT NULL DEFAULT 0 COMMENT '此定时任务的配置信息是否从MasterZone到SlaveZone进行复制。 默认为false，即不进行复制。',
  `only_scheduled_in_master_zone` tinyint(1) NOT NULL DEFAULT 1 COMMENT '此定时任务是否只在Master Zone中被调度执行。 默认为true。 如果想在Slave Zone中也被调度执行，请把此属性设置为false.',
  `job_dispatch_timeout_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '此定时任务是否允许设置派送超时，默认不允许派送超时设置',
  `job_dispatch_timeout` bigint(20) DEFAULT NULL COMMENT '如果此定时任务允许设置派送超时，该项表示任务派送超时值(ms)，如果任务派送的时间超过了该值，系统不会运行该任务',
  `comments` text DEFAULT NULL COMMENT '定时任务配置相关的注释信息,可用于保存修改定时任务配置的相关上下文等信息。',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_triggerMetaData` (`app_id`,`trigger_id`,`version`),
  KEY `idx_appId` (`app_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_manually_created` (`manually_created`),
  KEY `idx_triggerAppId` (`target_app_id`,`target_trigger_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用于保存各版本trigger的配置信息';


-- ----------------------------
-- Table structure for `operation_log`
-- ----------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id` varchar(40) NOT NULL COMMENT 'operation id ',
  `operation_id` int(11) NOT NULL COMMENT 'operation id ',
  `operated_by` varchar(50) NOT NULL COMMENT '操作者',
  `operate_time` datetime NOT NULL COMMENT '操作时间',
  `operation_details` longtext COMMENT '操作明细',
  `result_code` varchar(50) DEFAULT NULL COMMENT '操作结果代码。0:成功，1：部分成功，2：失败。',
  `result_details` longtext DEFAULT NULL COMMENT '操作结果详细信息。',
  PRIMARY KEY (`id`),
  KEY `idx_operationId` (`operation_id`),
  KEY `idx_operatedBy` (`operated_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'operation logs';

-- ----------------------------
-- Table structure for `kira_client_metadata`
-- ----------------------------
DROP TABLE IF EXISTS `kira_client_metadata`;
CREATE TABLE `kira_client_metadata` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `app_id` varchar(50) NOT NULL COMMENT 'app id ',
  `manually_created` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否是手动创建的kira客户端。',
  `manually_created_by` varchar(50) DEFAULT NULL COMMENT '手动创建者。',
  `manually_created_detail` text DEFAULT NULL COMMENT '手动创建时候的详细信息，如果是动态注册上来的则此项为空。',
  `kira_client_version` varchar(100) DEFAULT NULL COMMENT 'kira客户端版本号。',
  `visibility_limited` tinyint(1) NOT NULL COMMENT '是否对可见性进行限制。',
  `visible_for_users` text COMMENT '对于这些用户可见，以逗号分隔的域用户名',
  `send_alarm_email` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否发送告警邮件',
  `emails_to_receive_alarm` text COMMENT '接收告警的邮件列表，以逗号分隔的邮件地址',
  `send_alarm_sms` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否发送告警短信',
  `phone_numbers_to_receive_alarm_sms` text COMMENT '接收告警的手机号码列表，以逗号分隔的手机号码',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `last_register_time` datetime DEFAULT NULL COMMENT '上次注册时间，手动创建时此项为空。',
  `last_register_detail` text DEFAULT NULL COMMENT '上次注册的详细信息，手动创建时此项为空。',
  `last_manually_update_time` datetime DEFAULT NULL COMMENT '上次手动更新时间',
  `last_manually_update_by` varchar(50) DEFAULT NULL COMMENT '上次手动更新者',
  PRIMARY KEY (`id`),
  KEY `idx_visibilityLimited` (`visibility_limited`),
  KEY `idx_manuallyCreated` (`manually_created`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用于保存Kira客户端信息';

CREATE UNIQUE INDEX index_appId ON kira_client_metadata (app_id);

-- ----------------------------
-- Table structure for `job_timeout_tracker`
-- ----------------------------
DROP TABLE IF EXISTS `job_timeout_tracker`;
CREATE TABLE `job_timeout_tracker` (
  `id` varchar(100) NOT NULL COMMENT 'id ',
  `create_time` datetime NOT NULL COMMENT '创建时间。',
  `job_id` varchar(100) NOT NULL COMMENT 'job id ',
  `rum_time_threshold` bigint(20) NOT NULL COMMENT '任务运行时间阀值（毫秒），即任务最大的预计执行耗时。',
  `expect_timeout_time` datetime NOT NULL COMMENT '预计超时时间。',
  `state` int(11) NOT NULL COMMENT '状态。0:初始状态。1:未超时。2:超时后处理成功。 3. 超时后处理失败。4:超时后处理时发现已经不需要进一步处理了，原因：相关定时任务的运行数据不存在。5:超时后处理时发现已经不需要进一步处理了，原因：相关定时任务已经不存在。6:超时后处理时发现已经不需要进一步处理了，原因：相关定时任务的运行时间阀值被设置为空。7:超时后处理时发现已经不需要进一步处理了，原因：所在Pool已经设置为不发送报警了或者报警收件人未设置。8:超时后处理时发现已经不需要进一步处理了，原因：超时后处理失败次数太多。 ',
  `last_update_state_time` datetime DEFAULT NULL COMMENT '上次更新状态的时间。',
  `last_update_state_details` text DEFAULT NULL COMMENT '上次更新状态时候的详细信息。',
  `handle_timeout_failed_count` int(11) COMMENT '超时后处理失败的次数。可为空，只有因为超时并且处理失败过了才有值。',
  `data_version` int(11) NOT NULL DEFAULT 0 COMMENT '数据的版本号。',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_job_id` (`job_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_expect_timeout_time_state` (`expect_timeout_time`,`state`),
  KEY `idx_state_expect_timeout_time` (`state`,`expect_timeout_time`),
  KEY `idx_last_update_state_time` (`last_update_state_time`),
  KEY `idx_handle_timeout_failed_count` (`handle_timeout_failed_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用于跟踪job运行超时信息。';

-- ----------------------------
-- Table structure for `upgrade_roadmap`
-- ----------------------------
DROP TABLE IF EXISTS `upgrade_roadmap`;
CREATE TABLE `upgrade_roadmap` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `name` varchar(100) NOT NULL COMMENT 'app name',
  `upgrade_details` longtext COMMENT '升级明细',
  `create_time` datetime NOT NULL COMMENT '创建时间。',
  PRIMARY KEY (id),
  KEY `idx_name` (`name`),
  KEY `idx_create_time` (`create_time`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用于记录升级过程。';

-- ----------------------------
-- Records of hdc_menu
-- ----------------------------
INSERT INTO `hdc_menu` (id, pid, code, name, url, app)  VALUES ('1000', '0', 'unknown', '调度服务器集群', null, 'kira');
INSERT INTO `hdc_menu` (id, pid, code, name, url, app)  VALUES ('1001', '1000', 'unknown', '调度服务器管理', 'ScheduleServerMgr', 'kira');
INSERT INTO `hdc_menu` (id, pid, code, name, url, app)  VALUES ('1010', '0', 'unknown', '定时任务', null, 'kira');
INSERT INTO `hdc_menu` (id, pid, code, name, url, app)  VALUES ('1011', '1010', 'unknown', '定时任务配置管理', 'TaskList', 'kira');
INSERT INTO `hdc_menu` (id, pid, code, name, url, app)  VALUES ('1012', '1010', 'unknown', '定时任务各版本配置信息查询', 'TaskVersionsInfoQuery', 'kira');
INSERT INTO `hdc_menu` (id, pid, code, name, url, app)  VALUES ('1013', '1010', 'unknown', '定时任务调度信息查询', 'TaskScheduleInfoQuery', 'kira');
INSERT INTO `hdc_menu` (id, pid, code, name, url, app)  VALUES ('1014', '1010', 'unknown', '定时任务状态说明', 'TaskStatus', 'kira');
INSERT INTO `hdc_menu` (id, pid, code, name, url, app)  VALUES ('1015', '1010', 'unknown', '定时任务执行管理', 'TaskExecuteMgr', 'kira');
INSERT INTO `hdc_menu` (id, pid, code, name, url, app)  VALUES ('1040', '0', 'unknown', 'Kira客户端', null, 'kira');
INSERT INTO `hdc_menu` (id, pid, code, name, url, app)  VALUES ('1041', '1040', 'unknown', 'Kira客户端配置管理', 'KiraClientConfigMgr', 'kira');
INSERT INTO `hdc_menu` (id, pid, code, name, url, app)  VALUES ('1050', '0', 'unknown', '集中式执行环境', null, 'kira');
INSERT INTO `hdc_menu` (id, pid, code, name, url, app)  VALUES ('1051', '1050', 'unknown', '集中式执行环境管理', 'AppCenterEnvMgr', 'kira');
INSERT INTO `hdc_menu` (id, pid, code, name, url, app)  VALUES ('1090', '0', 'unknown', '操作日志', null, 'kira');
INSERT INTO `hdc_menu` (id, pid, code, name, url, app)  VALUES ('1099', '1090', 'unknown', '操作日志信息查询', 'OperationLogInfoQuery', 'kira');


-- ----------------------------
-- Records of hdc_pvg
-- ----------------------------
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('37', '2', '3', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('38', '2', '10', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('39', '2', '1', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('40', '2', '7', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('41', '2', '6', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('42', '2', '4', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('43', '2', '9', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('44', '2', '11', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5761', '15', '96', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('49', '3', '11', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('48', '3', '9', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5760', '15', '90', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5710', '100', '113', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5709', '100', '111', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5708', '100', '110', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5707', '100', '96', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5706', '100', '90', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5705', '100', '91', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5704', '100', '94', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5703', '100', '95', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5702', '100', '92', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5701', '100', '93', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5700', '100', '80', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5699', '100', '72', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5698', '100', '1021', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5697', '100', '1020', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5696', '100', '1036', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5695', '100', '1019', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5694', '100', '1035', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5693', '100', '71', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5692', '100', '1018', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5691', '100', '1034', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5690', '100', '70', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5689', '100', '1033', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5688', '100', '1032', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5687', '100', '1141', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5686', '100', '179', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5685', '100', '1142', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5684', '100', '1143', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5759', '15', '91', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5758', '15', '94', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5757', '15', '95', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5683', '100', '52', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5756', '15', '92', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5755', '15', '93', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5754', '15', '80', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5753', '15', '1021', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5752', '15', '1020', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5751', '15', '1036', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5750', '15', '1019', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5749', '15', '1035', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5748', '15', '1018', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5747', '15', '1034', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5746', '15', '70', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5745', '15', '1033', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5744', '15', '1032', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5743', '15', '1141', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5742', '15', '179', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5741', '15', '53', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5740', '15', '54', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5682', '100', '1144', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5681', '100', '53', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5680', '100', '54', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5679', '100', '55', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5739', '15', '55', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5678', '100', '50', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5738', '15', '50', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5677', '100', '51', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5676', '100', '160', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5737', '15', '51', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5736', '15', '160', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5675', '100', '161', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5674', '100', '162', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5735', '15', '161', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5734', '15', '162', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5733', '15', '163', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5732', '15', '173', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5731', '15', '1131', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5730', '15', '1133', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5673', '100', '163', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5729', '15', '1132', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5672', '100', '173', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5728', '15', '169', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5671', '100', '1131', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5670', '100', '174', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5727', '15', '151', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5726', '15', '1106', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5669', '100', '1133', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5725', '15', '30', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5668', '100', '33', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5667', '100', '1132', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5666', '100', '169', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5665', '100', '32', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5664', '100', '151', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('1323', '1', '50', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('1324', '1', '53', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5724', '15', '150', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5663', '100', '1106', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5662', '100', '30', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5661', '100', '150', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5660', '100', '31', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5723', '15', '1104', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5722', '15', '1105', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5659', '100', '1104', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5721', '15', '1108', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5658', '100', '1105', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5720', '15', '1109', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5657', '100', '1108', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5656', '100', '1109', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5655', '100', '1112', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5719', '15', '152', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5654', '100', '1113', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5653', '100', '152', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5652', '100', '132', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5718', '15', '133', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5717', '15', '130', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5716', '15', '131', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5651', '100', '133', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5715', '15', '1103', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5714', '15', '1102', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5650', '100', '130', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5649', '100', '131', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5648', '100', '1103', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5647', '100', '1102', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5646', '100', '1101', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5713', '15', '1101', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5712', '15', '1100', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5645', '100', '1100', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5711', '100', '112', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5762', '15', '110', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5763', '15', '111', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5764', '15', '113', '1');
INSERT INTO `hdc_pvg` (id, roleId, associationId, associationType) VALUES ('5765', '15', '112', '1');


-- ----------------------------
-- Records of hdc_role
-- ----------------------------
INSERT INTO `hdc_role` (roleCode, roleName, id, appName) VALUES ('normal', 'normal', '1', null);
INSERT INTO `hdc_role` (roleCode, roleName, id, appName) VALUES ('admin', 'admin', '2', null);


-- ----------------------------
-- Records of hdc_user
-- ----------------------------
INSERT INTO `hdc_user` (id, name, createTime) VALUES ('1', 'admin', '2017-09-09 09:09:09');

-- ----------------------------
-- Records of job_status
-- ----------------------------
INSERT INTO `job_status` (id, name, description) VALUES ('1', '任务已创建', '创建的任务分为人工手动创建的任务和服务器自动创建的正在调度执行的定时任务。');
INSERT INTO `job_status` (id, name, description) VALUES ('2', '任务配置已更新', '如果任务是人工手动创建的并且现在没有在执行，则任务创建后其配置可以被更新。');
INSERT INTO `job_status` (id, name, description) VALUES ('3', '任务无法派送', '异步执行的任务未满足可派送条件。');
INSERT INTO `job_status` (id, name, description) VALUES ('4', '任务正在被派送', '异步执行的任务正在被派送。');
INSERT INTO `job_status` (id, name, description) VALUES ('5', '任务已送达', '异步执行的任务已经成功派发到全部执行端。');
INSERT INTO `job_status` (id, name, description) VALUES ('6', '任务派送失败', '异步执行的任务未能成功派发到全部执行端。');
INSERT INTO `job_status` (id, name, description) VALUES ('7', '任务派送部分成功', '异步执行的任务只成功派发到部分执行端。');
INSERT INTO `job_status` (id, name, description) VALUES ('8', '任务无法执行', '同步执行的任务未满足可执行条件。');
INSERT INTO `job_status` (id, name, description) VALUES ('9', '任务正在执行', '任务正在执行端执行。');
INSERT INTO `job_status` (id, name, description) VALUES ('10', '任务执行成功', '任务在全部执行端执行成功。');
INSERT INTO `job_status` (id, name, description) VALUES ('11', '任务执行失败', '任务在全部执行端执行失败。');
INSERT INTO `job_status` (id, name, description) VALUES ('12', '任务执行部分成功', '任务只在部分执行端执行成功。');
INSERT INTO `job_status` (id, name, description) VALUES ('13', '无需派送定时任务', '某些情况下无需派送定时任务。比如当定时任务的concurrent属性为false时，如果此时定时任务对应的业务方法在正在某地执行，则无需派送定时任务。');
INSERT INTO `job_status` (id, name, description) VALUES ('14', '无需执行业务方法', '某些情况下无需执行业务方法。比如当任务已送达，并且定时任务的concurrent属性为false时，如果此时定时任务对应的业务方法在正在某地执行，则无需执行业务方法。');
INSERT INTO `job_status` (id, name, description) VALUES ('15', '调度过程中出现异常', '调度系统在对该任务调度过程中捕获到异常。');


-- ----------------------------
-- Records of operation
-- ----------------------------
-- 整体功能部分
INSERT INTO `operation` (id, name, display, description, type) VALUES ('51', '/menu/getMenuTree.action', '查询功能菜单树', null, 0);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('52', '/other/getCurrentServerIp.action', '获取当前服务器IP', null, 0);
-- 调度服务器集群管理,查询部分
INSERT INTO `operation` (id, name, display, description, type) VALUES ('101', '/kiraServer/stopServer.action', '停止服务器', null, 1);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('102', '/kiraServer/startServer.action', '启动服务器', null, 1);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('103', '/kiraServer/restartServer.action', '重启服务器', null, 1);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('104', '/kiraServer/recoverServer.action', '修复服务器', null, 1);

INSERT INTO `operation` (id, name, display, description, type) VALUES ('151', '/kiraServer/getKiraServerDetailDataList.action', '查询调度服务器集群信息', null, 0);
-- 定时任务配置，调度，查询部分
INSERT INTO `operation` (id, name, display, description, type) VALUES ('201', '/triggerMetadata/updateTrigger.action', '更新定时任务配置', null, 1);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('202', '/triggerMetadata/deleteTrigger.action', '删除定时任务', null, 1);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('203', '/triggerMetadata/pauseTrigger.action', '暂停任务调度', null, 1);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('204', '/triggerMetadata/resumeTrigger.action', '恢复任务调度', null, 1);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('205', '/triggerMetadata/unscheduleJob.action', '取消任务调度', null, 1);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('206', '/triggerMetadata/rescheduleJob.action', '重新执行任务调度', null, 1);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('207', '/triggerMetadata/manuallyRunJobByTriggerMetadata.action', '手动执行一次调度任务', null, 1);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('208', '/triggerMetadata/deleteTriggerEnvironment.action', '删除执行环境', null, 1);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('209', '/triggerMetadata/createTrigger.action', '创建定时任务', null, 1);

INSERT INTO `operation` (id, name, display, description, type) VALUES ('251', '/triggerMetadata/listLatestOnPage.action', '定时任务配置查询', null, 0);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('252', '/triggerMetadata/getAllPoolIdList.action', '获得所有AppId列表', null, 0);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('253', '/triggerMetadata/getAllTriggerIdList.action', '获得AppId对应的所有定时任务Id列表', null, 0);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('254', '/triggerMetadata/getTriggerEnvironmentDetailDataListOnPage.action', '执行环境信息查询', null, 0);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('255', '/triggerMetadata/getPoolTriggerStatus.action', '任务调度信息查询', null, 0);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('256', '/triggerMetadata/getTriggerMetadataById.action', '根据id获得调度任务的详细配置信息', null, 0);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('257', '/triggerMetadata/getTriggerTypeList.action', '获得所有调度任务类型列表', null, 0);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('258', '/triggerMetadata/getMisfireInstructionList.action', '根据调度任务类型获得漏执行策略列表', null, 0);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('261', '/triggerMetadata/listOnPage.action', '定时任务各版本配置信息查询', null, 0);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('271', '/triggerMetadata/getPoolTriggerStatusListOnPage.action', '定时任务调度信息查询', null, 0);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('272', '/triggerMetadata/getLatestTriggerMetadatasWhichSetTriggerAsTargetOnPage.action', '查询以某个定时任务所暴露的服务方法为执行目标的定时任务列表信息', null, 0);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('273', '/triggerMetadata/getLatestTriggerMetadatasWhichSetTriggersOfPoolAsTargetOnPage.action', '查询以某个App的那些定时任务所暴露的服务方法为执行目标的定时任务列表信息', null, 0);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('274', '/triggerMetadata/getAllAvailablePoolIdList.action', '获得所有可用的AppId列表', null, 0);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('275', '/triggerMetadata/getAllAvailableTriggerIdList.action', '获得PoolId对应的所有可用的定时任务Id列表', null, 0);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('276', '/triggerMetadata/getKiraTimerTriggerBusinessRunningInstanceListOnPage.action', '查询定时任务对应的业务方法当前执行实例列表', null, 0);

-- 定时任务状态说明部分
INSERT INTO `operation` (id, name, display, description, type) VALUES ('351', '/jobStatus/listOnPage.action', '定时任务状态说明', null, 0);
-- 任务执行管理，查询部分
INSERT INTO `operation` (id, name, display, description, type) VALUES ('401', '/job/manuallyReRunJob.action', '重跑任务', null, 1);
INSERT INTO `operation`(id, name, display, description, type)  VALUES ('402', '/job/cancelJob.action', '取消执行任务', null, 1);

INSERT INTO `operation` (id, name, display, description, type) VALUES ('451', '/job/getJobDetailDataListOnPage.action', '定时任务执行情况查询', null, 0);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('452', '/jobHistory/getJobHistoryDetailDataListOnPage.action', '状态变化查询', null, 0);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('453', '/jobItem/getJobItemDetailDataListOnPage.action', '执行明细', null, 0);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('454', '/jobItemHistory/getJobItemHistoryDetailDataListOnPage.action', '子任务状态查询', null, 0);
-- 集中式执行环境管理，查询部分
INSERT INTO `operation` (id, name, display, description, type) VALUES ('651', '/appCenter/getAppCenterDetailDataList.action', '集中式执行环境查询', null, 0);
-- 操作日志管理，查询部分
INSERT INTO `operation` (id, name, display, description, type) VALUES ('751', '/operation/list.action', '查询所有操作列表', null, 0);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('752', '/operation/listOnPage.action', '分页查询操作列表', null, 0);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('753', '/operation/getAllNotReadonlyOperations.action', '查询所有非只读操作', null, 0);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('754', '/operationLog/getOperationLogDetailDataListOnPage.action', '分页查询操作日志详细信息', null, 0);
-- kira客户端管理，查询部分
INSERT INTO `operation` (id, name, display, description, type) VALUES ('801', '/kiraClientMetadata/updateKiraClientMetadata.action', '更新Kira客户端配置信息', null, 1);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('802', '/kiraClientMetadata/createKiraClientMetadata.action', '创建Kira客户端配置信息', null, 1);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('803', '/kiraClientMetadata/deleteKiraClientMetadata.action', '删除Kira客户端配置信息', null, 1);

INSERT INTO `operation` (id, name, display, description, type) VALUES ('851', '/kiraClientMetadata/listOnPage.action', '分页查询各app的kira客户端配置信息', null, 0);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('852', '/kiraClientMetadata/getAllPoolIdList.action', '获取Kira客户端对应的所有AppId列表', null, 0);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('853', '/kiraClientMetadata/getKiraClientMetadataById.action', '根据Id获取Kira客户端配置信息', null, 0);
INSERT INTO `operation` (id, name, display, description, type) VALUES ('854', '/kiraClient/queryKiraClientInfoAsMap.action', '获取Kira客户端当前信息', null, 0);


