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
package com.yihaodian.architecture.kira.manager.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*import com.yihaodian.architecture.jumper.common.message.Destination;
import com.yihaodian.architecture.jumper.common.message.Message;
import com.yihaodian.architecture.jumper.consumer.Consumer;
import com.yihaodian.architecture.jumper.consumer.ConsumerConfig;
import com.yihaodian.architecture.jumper.consumer.MessageListener;
import com.yihaodian.architecture.jumper.consumer.impl.ConsumerFactoryImpl;
import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.manager.core.metadata.kiraclient.message.KiraClientRegisterDataMessageHandler;
import com.yihaodian.architecture.kira.manager.jms.iface.IMessageHandler;
import com.yihaodian.architecture.kira.manager.util.KiraManagerConstants;
import com.yihaodian.architecture.kira.manager.util.KiraManagerDataCenter;*/

public class MessageReceiverRegister {

  private static Logger logger = LoggerFactory.getLogger(MessageReceiverRegister.class);
	
/*	//<queueName,Consumer>
	private static Map<String, Consumer> consumers = new HashMap<String, Consumer>();
	
	public static void registerKiraClientRegisterDataReceiver(IMessageHandler messageHandler, boolean forceRestartConsumer) throws Exception {
		logger.info("Start registerKiraClientRegisterDataReceiver...");
		try {
			register(KiraCommonConstants.QUEUE_KIRA_CLIENT_REGISTER_DATA,messageHandler,KiraManagerConstants.JUMPER_CONSUMER_THREADPOOLSIZE_FOR_KIRA_CLIENT_REGISTER_DATA,forceRestartConsumer);	
		} finally {
			logger.info("End registerKiraClientRegisterDataReceiver");
		}
	}
	
	public static void unRegisterKiraClientRegisterDataReceiver() {
		if(null!=logger) {
			logger.info("Start unRegisterKiraClientRegisterDataReceiver...");
		}
		try {
			if(null!=consumers) {
				Consumer consumer = consumers.get(KiraCommonConstants.QUEUE_KIRA_CLIENT_REGISTER_DATA);
				if(null!=consumer) {
					consumer.close();
				}
			}
		} catch(Throwable t) {
			if(null!=logger) {
				logger.error("Error occurs when unRegisterKiraClientRegisterDataReceiver",t);
			}
		} finally {
			if(null!=logger) {
				logger.info("End unRegisterKiraClientRegisterDataReceiver");
			}
		}
	}
	
	public static void registerJobItemStatusReportReceiver(IMessageHandler messageHandler, boolean forceRestartConsumer) throws Exception{
		logger.info("Start registerJobItemStatusReportReceiver...");
		try {
			register(KiraCommonConstants.QUEUE_KIRA_JOB_ITEM_STATUS,messageHandler,KiraManagerConstants.JUMPER_CONSUMER_THREADPOOLSIZE,forceRestartConsumer);	
		} finally {
			logger.info("End registerJobItemStatusReportReceiver");
		}
	}
	
	public static void unRegisterJobItemStatusReportReceiver() {
		if(null!=logger) {
			logger.info("Start unRegisterJobItemStatusReportReceiver...");
		}
		try {
			if(null!=consumers) {
				Consumer consumer = consumers.get(KiraCommonConstants.QUEUE_KIRA_JOB_ITEM_STATUS);
				if(null!=consumer) {
					consumer.close();
				}
			}
		} catch(Throwable t) {
			if(null!=logger) {
				logger.error("Error occurs when unRegisterJobItemStatusReportReceiver",t);
			}
		} finally {
			if(null!=logger) {
				logger.info("End unRegisterJobItemStatusReportReceiver");
			}
		}
	}	
	
	public static void closeAllConsumers() {
		if(null!=logger) {
			logger.info("Start closeAllConsumers...");
		}
		try{
			if(null!=consumers) {
				String queueName = null;
				Consumer consumer = null;
				for(Map.Entry<String, Consumer> entry : consumers.entrySet()) {
					try{
						queueName = entry.getKey();
						consumer = entry.getValue();
						if(null!=consumer) {
							consumer.close();
						}
					}catch(Throwable t) {
						if(null!=logger) {
							logger.error("Error occurs when try to close consumer. queueName="+queueName+" and consumer="+consumer,t);
						}
					}
				}
			}
		} catch(Exception e) {
			if(null!=logger) {
				logger.error("Error occurs for closeAllConsumers.",e);
			}
		} finally {
			if(null!=logger) {
				logger.info("End closeAllConsumers");
			}
		}
	}
	
	*//**
   * 注册消息处理者
   *
   * @param queueName
   * @param messageHandler
   *//*
	private static synchronized void register(String queueName, final IMessageHandler messageHandler, int threadPoolSize, boolean forceRestartConsumer) {
		if (consumers.containsKey(queueName)) {
			if(forceRestartConsumer) {
				Consumer consumer = consumers.get(queueName);
				if(null!=consumer) {
					consumer.restart();
				}
			}
			return;
		}
		
		String consumerId = null;
		if(KiraCommonConstants.QUEUE_KIRA_JOB_ITEM_STATUS.equals(queueName)) {
			consumerId = KiraManagerDataCenter.getKiraJobItemStatusConsumerId();
		} else if(KiraCommonConstants.QUEUE_KIRA_CLIENT_REGISTER_DATA.equals(queueName)) {
			consumerId = KiraManagerDataCenter.getKiraClientRegisterDataConsumerId();
		}
		if(StringUtils.isNotBlank(consumerId)) {
			ConsumerConfig config = new ConsumerConfig();
			config.setThreadPoolSize(threadPoolSize);
			//final Consumer clientConsumer = ConsumerFactoryImpl.getInstance().createConsumer(Destination.topic(queueName), queueName + "Consumer", config);
			final Consumer clientConsumer = ConsumerFactoryImpl.getInstance().createConsumer(Destination.topic(queueName), consumerId.trim(), config);
			clientConsumer.setListener(new MessageListener() {
				public void onMessage(Message msg) {
					messageHandler.doHandle(msg);
				}
			});
			clientConsumer.start();
			consumers.put(queueName, clientConsumer);
		} else {
			logger.error("consumerId is blank. So can not create consumer for queueName={}",queueName);
		}
	}
	
	public static synchronized Consumer getConsumer(String queueName) {
		Consumer consumer = consumers.get(queueName);
		return consumer;
	}
	
	public static void main(String[] args) throws Exception {
		//-Dglobal.config.path=C:\studio-win7\work\yihaodian\global-config\dev-local-9080\
		System.setProperty("global.config.path", "C:/studio-win7/work/yihaodian/global-config/dev-local-9080/");
		//System.setProperty("global.config.path", "C:/studio-win7/work/yihaodian/global-config/dev/");
		MessageReceiverRegister.registerKiraClientRegisterDataReceiver(new KiraClientRegisterDataMessageHandler(null), true);
		System.out.println("done");
	}*/
}
