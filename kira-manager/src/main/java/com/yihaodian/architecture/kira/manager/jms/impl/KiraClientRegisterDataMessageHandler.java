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
package com.yihaodian.architecture.kira.manager.jms.impl;

public class KiraClientRegisterDataMessageHandler {//implements IMessageHandler {
	/*private static Logger logger = LoggerFactory.getLogger(KiraClientRegisterDataMessageHandler.class);
	
	private KiraClientMetadataService kiraClientMetadataService;

	public KiraClientMetadataService getKiraClientMetadataService() {
		return kiraClientMetadataService;
	}

	public void setKiraClientMetadataService(
			KiraClientMetadataService kiraClientMetadataService) {
		this.kiraClientMetadataService = kiraClientMetadataService;
	}

	public KiraClientRegisterDataMessageHandler() {
	}

	@Override
	public void doHandle(Message message) {
		KiraClientRegisterData kiraClientRegisterData = message.transferContentToBean(KiraClientRegisterData.class);
		if(logger.isDebugEnabled()) {
			logger.debug("kiraClientRegisterData={}",kiraClientRegisterData);
		}
		*//*if(null!=kiraClientRegisterData) {
			//change to use the server side time instead of client side time.
			kiraClientRegisterData.setCreateTime(new Date());
			kiraClientMetadataService.handleKiraClientRegisterData(kiraClientRegisterData);	
		}*//*
	}
*/

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

}
