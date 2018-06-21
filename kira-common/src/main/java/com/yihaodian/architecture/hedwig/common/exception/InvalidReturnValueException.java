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
package com.yihaodian.architecture.hedwig.common.exception;

/**
 * @author Archer Jiang
 */
public class InvalidReturnValueException extends HedwigException {

  /**
   *
   */
  private static final long serialVersionUID = 4991660928353116776L;

  public InvalidReturnValueException() {
    super();
  }

  public InvalidReturnValueException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidReturnValueException(String message) {
    super(message);
  }

  public InvalidReturnValueException(Throwable cause) {
    super(cause);
  }

}
