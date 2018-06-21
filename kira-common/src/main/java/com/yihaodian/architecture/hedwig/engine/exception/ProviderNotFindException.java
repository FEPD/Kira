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
package com.yihaodian.architecture.hedwig.engine.exception;


/**
 * @author Archer
 */
public class ProviderNotFindException extends HandlerException {

  /**
   *
   */
  private static final long serialVersionUID = 8290861787325999884L;

  public ProviderNotFindException() {
    super();
  }

  public ProviderNotFindException(String reqId, String requestId, String message) {
    super(reqId, requestId, message);
  }

  public ProviderNotFindException(String reqId, String message, Throwable cause) {
    super(reqId, message, cause);
  }

  public ProviderNotFindException(String reqId, String message) {
    super(reqId, message);
  }

  public ProviderNotFindException(String reqId, Throwable cause) {
    super(reqId, cause);
  }

}
