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
package com.yihaodian.architecture.hedwig.common.config;

/*import com.yihaodian.configcentre.client.utils.YccGlobalPropertyConfigurer;
import com.yihaodian.configcentre.listener.ConfigureTargetListener;*/

/**
 * @author Archer
 */
public class ProperitesListener {//implements ConfigureTargetListener {

/*	private String configInfo;

	private Object targetObject;

	*//*
   * (non-Javadoc)
   *
   * @see com.yihaodian.configcentre.manager.ManagerListener#getExecutor()
   *//*
	@Override
	public Executor getExecutor() {
		// TODO Auto-generated method stub
		return null;
	}

	*//*
   * (non-Javadoc)
   *
   * @see
   * com.yihaodian.configcentre.manager.ManagerListener#receiveConfigInfo(
   * java.lang.String)
   *//*
	@Override
	public void receiveConfigInfo(String configInfo) {
		this.configInfo = configInfo;
		if (configInfo != null) {
			Hashtable<String, String> properties = YccGlobalPropertyConfigurer.loadProperties(configInfo);
			if (targetObject instanceof ProperitesContainer) {
				((ProperitesContainer) targetObject).pullAll(properties);
			}
		}

	}

	*//*
   * (non-Javadoc)
   *
   * @see
   * com.yihaodian.configcentre.listener.ConfigureTargetListener#setTargetObject
   * (java.lang.Object)
   *//*
	@Override
	public void setTargetObject(Object targetObj) {
		this.targetObject = targetObj;

	}*/

}
