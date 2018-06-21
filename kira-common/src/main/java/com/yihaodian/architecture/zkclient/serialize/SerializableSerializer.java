/**
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.yihaodian.architecture.zkclient.serialize;

import com.yihaodian.architecture.zkclient.exception.ZkMarshallingError;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SerializableSerializer implements ZkSerializer {

  private static Logger logger = LoggerFactory.getLogger(SerializableSerializer.class);
  private static byte[] streamHeaderByteArray = buildStreamHeaderByteArray();

  private static byte[] buildStreamHeaderByteArray() {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream stream = new ObjectOutputStream(baos);
      stream.close();
      byte[] resultBytes = baos.toByteArray();
      if (logger.isDebugEnabled()) {
        logger.debug("###valid stream header bytes[]=" + convertBytesToString(resultBytes));
      }
      return resultBytes;
    } catch (Exception e) {
      e.printStackTrace();
      throw new ZkMarshallingError(e);
    }
  }

  //是否是有效的字符串流头信息
  private static boolean isValidStreamHeader(byte[] bytes) {
    byte[] a1 = streamHeaderByteArray;
    byte[] a2 = bytes;
    if (a1 != null && a1.length > 0 && a2 != null && a2.length > 0
        && a2.length >= a1.length) {
      int length = a1.length;
      for (int i = 0; i < length; i++) {
        if (a1[i] != a2[i]) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  private static String convertBytesToString(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    if (bytes != null && bytes.length > 0) {
      sb.append("[");
      for (int i = 0; i < bytes.length; i++) {
        sb.append(bytes[i]).append(",");
      }
      sb.append("]");
    }
    return sb.toString();
  }

  @Override
  public Object deserialize(byte[] bytes) throws ZkMarshallingError {
    try {
      //反序列化之前检查header，信息是否有效（注：ycache python zk client 序列化是不会写入这个标准header信息）
      if (isValidStreamHeader(bytes)) {
//                if(logger.isDebugEnabled()){
//                    logger.debug("###deserialize(),input stream bytes="+convertBytesToString(bytes));
//                }
        ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Object object = inputStream.readObject();
        return object;
      } else {
        if (logger.isDebugEnabled()) {
          logger.debug(
              "###invalid stream header!! Can't deserialize object by java deserializer , use raw data directly!!! input stream bytes="
                  + convertBytesToString(bytes)
                  + ",need valid stream header=" + convertBytesToString(streamHeaderByteArray));
        }
        return bytes;
      }
    } catch (ClassNotFoundException e) {
      throw new ZkMarshallingError("Unable to find object class.", e);
    } catch (IOException e) {
      throw new ZkMarshallingError(e);
    }
  }

  @Override
  public byte[] serialize(Object serializable) throws ZkMarshallingError {
    try {
      ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
      ObjectOutputStream stream = new ObjectOutputStream(byteArrayOS);
      stream.writeObject(serializable);
      stream.close();
      byte[] resultBytes = byteArrayOS.toByteArray();
//            if(logger.isDebugEnabled()){
//                logger.debug("###resultBytes="+convertBytesToString(resultBytes));
//            }
      return resultBytes;
    } catch (IOException e) {
      throw new ZkMarshallingError(e);
    }
  }
}