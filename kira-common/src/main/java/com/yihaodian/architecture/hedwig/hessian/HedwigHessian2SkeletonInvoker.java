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

import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.HessianDebugInputStream;
import com.caucho.hessian.io.HessianDebugOutputStream;
import com.caucho.hessian.io.SerializerFactory;
import com.yihaodian.architecture.hedwig.common.hessian.HedwigHessianInput;
import com.yihaodian.architecture.hedwig.common.hessian.HedwigHessianOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import org.apache.commons.logging.Log;
import org.springframework.util.ClassUtils;
import org.springframework.util.CommonsLogWriter;

/**
 * @author Archer
 */
public class HedwigHessian2SkeletonInvoker extends HedwigHessianSkeletonInvoker {

  private static final boolean debugOutputStreamAvailable = ClassUtils.isPresent(
      "com.caucho.hessian.io.HessianDebugOutputStream",
      HedwigHessian2SkeletonInvoker.class.getClassLoader());

  private final Log debugLogger;

  public HedwigHessian2SkeletonInvoker(HedwigHessianSkeleton skeleton,
      SerializerFactory serializerFactory,
      Log debugLog) {
    super(skeleton, serializerFactory);
    this.debugLogger = debugLog;
  }

  public void invoke(final InputStream inputStream, final OutputStream outputStream)
      throws Throwable {
    InputStream isToUse = inputStream;
    OutputStream osToUse = outputStream;

    if (this.debugLogger != null && this.debugLogger.isDebugEnabled()) {
      PrintWriter debugWriter = new PrintWriter(new CommonsLogWriter(this.debugLogger));
      isToUse = new HessianDebugInputStream(inputStream, debugWriter);
      if (debugOutputStreamAvailable) {
        osToUse = DebugStreamFactory.createDebugOutputStream(outputStream, debugWriter);
      }
    }

    HedwigHessianInput in = new HedwigHessianInput(isToUse);
    if (this.serializerFactory != null) {
      in.setSerializerFactory(this.serializerFactory);
    }

    int code = in.read();
    if (code != 'c') {
      throw new IOException("expected 'c' in hessian input at " + code);
    }

    AbstractHessianOutput out = null;
    int major = in.read();
    int minor = in.read();
    out = new HedwigHessianOutput(osToUse);
    if (this.serializerFactory != null) {
      out.setSerializerFactory(this.serializerFactory);
    }

    try {
      this.skeleton.invoke(in, out);
    } finally {
      try {
        in.close();
        isToUse.close();
      } catch (IOException ex) {
      }
      try {
        out.close();
        osToUse.close();
      } catch (IOException ex) {
      }
    }
  }

  /**
   * Inner class to avoid hard dependency on Hessian 3.1.3's HessianDebugOutputStream.
   */
  private static class DebugStreamFactory {

    public static OutputStream createDebugOutputStream(OutputStream os, PrintWriter debug) {
      return new HessianDebugOutputStream(os, debug);
    }
  }
}
