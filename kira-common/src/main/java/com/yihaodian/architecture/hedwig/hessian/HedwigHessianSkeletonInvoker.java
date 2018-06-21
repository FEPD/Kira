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
package com.yihaodian.architecture.hedwig.hessian;

import com.caucho.hessian.io.SerializerFactory;
import java.io.InputStream;
import java.io.OutputStream;
import org.springframework.util.Assert;

abstract class HedwigHessianSkeletonInvoker {

  /**
   * Wrapped HessianSkeleton, available to subclasses.
   */
  protected final HedwigHessianSkeleton skeleton;

  /**
   * Hessian SerializerFactory (if any), available to subclasses.
   */
  protected final SerializerFactory serializerFactory;


  /**
   * Create a new HessianSkeletonInvoker for the given skeleton.
   *
   * @param skeleton the HessianSkeleton to wrap
   * @param serializerFactory the Hessian SerializerFactory to use, if any
   */
  public HedwigHessianSkeletonInvoker(HedwigHessianSkeleton skeleton,
      SerializerFactory serializerFactory) {
    Assert.notNull(skeleton, "HessianSkeleton must not be null");
    this.skeleton = skeleton;
    this.serializerFactory = serializerFactory;
  }


  /**
   * Invoke the given skeleton based on the given input/output streams.
   *
   * @param inputStream the stream containing the Hessian input
   * @param outputStream the stream to receive the Hessian output
   * @throws Throwable if the skeleton invocation failed
   */
  public abstract void invoke(InputStream inputStream, OutputStream outputStream) throws Throwable;
}
