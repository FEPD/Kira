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

import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.exception.HedwigException;

public class HandlerException extends HedwigException {

  /**
   *
   */
  private static final long serialVersionUID = 1664923907531909180L;
  protected String reqId;

  public HandlerException() {
    super();
  }

  public HandlerException(String reqId, String message, Throwable cause) {
    super(createMessage(reqId, message), cause);
  }

  public HandlerException(String reqId, String message) {
    super(createMessage(reqId, message));
  }

  public HandlerException(String reqId, String requestId, String message) {
    super(createMessage(reqId, message));
  }

  public HandlerException(String reqId, Throwable cause) {
    super(createMessage(reqId, cause.getMessage()), cause);
  }

  public static String createMessage(String reqId, String message) {
    StringBuilder sb = new StringBuilder();
    sb.append("\n").append(InternalConstants.HEDWIG_REQUEST_ID).append(":").append(reqId);
    sb.append(", " + InternalConstants.HANDLE_LOG_PROFIX).append(message).append("; ");
    return sb.toString();

  }
}
