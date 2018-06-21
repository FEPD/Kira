/*
 * UUID.java
 *
 * Created 07.02.2003
 *
 * eaio: UUID - an implementation of the UUID specification
 * Copyright (c) 2003-2009 Johann Burkard (jb@eaio.com) http://eaio.com.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package com.yihaodian.architecture.hedwig.common.uuid;

/**
 * com/eaio/uuid/UUIDHolder.java . Generated by the IDL-to-Java compiler (portable), version "3.1"
 * from uuid.idl Sonntag, 7. März 2004 21.35 Uhr CET
 */


/**
 * The UUID struct.
 */
public final class UUIDHolder implements org.omg.CORBA.portable.Streamable {

  public UUID value = null;

  public UUIDHolder() {
  }

  public UUIDHolder(UUID initialValue) {
    value = initialValue;
  }

  public void _read(org.omg.CORBA.portable.InputStream i) {
    value = UUIDHelper.read(i);
  }

  public void _write(org.omg.CORBA.portable.OutputStream o) {
    UUIDHelper.write(o, value);
  }

  public org.omg.CORBA.TypeCode _type() {
    return UUIDHelper.type();
  }

}
