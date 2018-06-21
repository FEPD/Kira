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
package com.yihaodian.architecture.kira.manager.web;

import com.yihaodian.architecture.kira.manager.util.KiraServerConstants;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class WapperedHttpServletResponse extends HttpServletResponseWrapper {

  private ByteArrayOutputStream buffer = null;
  private ServletOutputStream out = null;
  private PrintWriter writer = null;

  public WapperedHttpServletResponse(HttpServletResponse resp)
      throws IOException {
    super(resp);
    buffer = new ByteArrayOutputStream();// 真正存储数据的流
    out = new WapperedOutputStream(buffer);
    writer = new PrintWriter(new OutputStreamWriter(buffer, KiraServerConstants.DEFAULT_CHARSET));
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  // 重载父类获取outputstream的方法
  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    return out;
  }

  // 重载父类获取writer的方法
  @Override
  public PrintWriter getWriter() throws UnsupportedEncodingException {
    return writer;
  }

  // 重载父类获取flushBuffer的方法
  @Override
  public void flushBuffer() throws IOException {
    if (out != null) {
      out.flush();
    }
    if (writer != null) {
      writer.flush();
    }
  }

  @Override
  public void reset() {
    buffer.reset();
  }

  public byte[] getResponseData() throws IOException {
    flushBuffer();// 将out、writer中的数据强制输出到WapperedResponse的buffer里面，否则取不到数据
    return buffer.toByteArray();
  }

  public String getResponseDataAsString() throws IOException {
    flushBuffer();// 将out、writer中的数据强制输出到WapperedResponse的buffer里面，否则取不到数据
    //return buffer.toString(this.getCharacterEncoding());
    return buffer.toString(KiraServerConstants.DEFAULT_CHARSET);
  }

  // 内部类，对ServletOutputStream进行包装
  private class WapperedOutputStream extends ServletOutputStream {

    private ByteArrayOutputStream bos = null;

    public WapperedOutputStream(ByteArrayOutputStream stream)
        throws IOException {
      bos = stream;
    }

    @Override
    public void write(int b) throws IOException {
      bos.write(b);
    }
  }

}
