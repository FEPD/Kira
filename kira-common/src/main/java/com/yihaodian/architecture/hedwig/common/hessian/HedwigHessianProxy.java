/**
 *
 */
package com.yihaodian.architecture.hedwig.common.hessian;

import com.caucho.hessian.client.HessianRuntimeException;
import com.caucho.hessian.io.AbstractHessianInput;
import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.HessianProtocolException;
import com.caucho.services.server.AbstractSkeleton;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.WeakHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Archer
 */
public class HedwigHessianProxy implements InvocationHandler {

  private static final Logger log = LoggerFactory.getLogger(HedwigHessianProxy.class);

  private HedwigHessianProxyFactory _factory;
  private WeakHashMap<Method, String> _mangleMap = new WeakHashMap<Method, String>();
  private URL _url;

  HedwigHessianProxy(HedwigHessianProxyFactory hedwigHessianProxyFactory, URL url) {
    _factory = hedwigHessianProxyFactory;
    _url = url;
  }

  /**
   * Returns the proxy's URL.
   */
  public URL getURL() {
    return _url;
  }

  /**
   * Handles the object invocation.
   *
   * @param proxy the proxy object to invoke
   * @param method the method to call
   * @param args the arguments to the proxy object
   */
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    String mangleName;

    synchronized (_mangleMap) {
      mangleName = _mangleMap.get(method);
    }

    if (mangleName == null) {
      String methodName = method.getName();
      Class[] params = method.getParameterTypes();

      // equals and hashCode are special cased
      if (methodName.equals("equals") && params.length == 1 && params[0].equals(Object.class)) {
        Object value = args[0];
        if (value == null || !Proxy.isProxyClass(value.getClass())) {
          return Boolean.valueOf(false);
        }

        HedwigHessianProxy handler = (HedwigHessianProxy) Proxy.getInvocationHandler(value);
        URL pUrl = handler.getURL();
        return new Boolean(pUrl != null && _url.equals(pUrl));
      } else if (methodName.equals("hashCode") && params.length == 0) {
        return Integer.valueOf(_url.hashCode());
      } else if (methodName.equals("getHessianType")) {
        return proxy.getClass().getInterfaces()[0].getName();
      } else if (methodName.equals("getHessianURL")) {
        return _url.toString();
      } else if (methodName.equals("toString") && params.length == 0) {
        return "HessianProxy[" + _url + "]";
      }

      if (!_factory.isOverloadEnabled()) {
        mangleName = method.getName();
      } else {
        mangleName = mangleName(method);
      }

      synchronized (_mangleMap) {
        _mangleMap.put(method, mangleName);
      }
    }

    InputStream is = null;
    URLConnection conn = null;
    HttpURLConnection httpConn = null;

    try {
      conn = sendRequest(mangleName, args);

      if (conn instanceof HttpURLConnection) {
        httpConn = (HttpURLConnection) conn;
        int code = 500;

        try {
          code = httpConn.getResponseCode();
        } catch (Exception e) {
        }

        if (code != 200) {
          StringBuffer sb = new StringBuffer();
          int ch;

          try {
            is = httpConn.getInputStream();

            if (is != null) {
              while ((ch = is.read()) >= 0) {
                sb.append((char) ch);
              }
              is.close();
            }

            is = httpConn.getErrorStream();
            if (is != null) {
              while ((ch = is.read()) >= 0) {
                sb.append((char) ch);
              }
            }
          } catch (FileNotFoundException e) {
            throw new HessianRuntimeException(String.valueOf(e));
          } catch (IOException e) {
            if (is == null) {
              throw new HessianProtocolException(code + ": " + e, e);
            }
          }

          if (is != null) {
            is.close();
          }

          throw new HessianProtocolException(code + ": " + sb.toString());
        }
      }

      is = conn.getInputStream();

      AbstractHessianInput in = _factory.getHessianInput(is);

      in.startReply();

      Object value = in.readObject(method.getReturnType());

      if (value instanceof InputStream) {
        value = new ResultInputStream(httpConn, is, in, (InputStream) value);
        is = null;
        httpConn = null;
      } else {
        in.completeReply();
      }

      return value;
    } catch (HessianProtocolException e) {
      throw new HessianRuntimeException(e);
    } finally {
      try {
        if (is != null) {
          is.close();
        }
      } catch (Throwable e) {
        log.error(e.getMessage(), e);
      }

      try {
        if (httpConn != null) {
          httpConn.disconnect();
        }
      } catch (Throwable e) {
        log.error(e.getMessage(), e);
      }
    }
  }

  protected String mangleName(Method method) {
    Class[] param = method.getParameterTypes();

    if (param == null || param.length == 0) {
      return method.getName();
    } else {
      return AbstractSkeleton.mangleName(method, false);
    }
  }

  protected URLConnection sendRequest(String methodName, Object[] args) throws IOException {
    URLConnection conn = null;

    conn = _factory.openConnection(_url);

    // Used chunked mode when available, i.e. JDK 1.5.
    if (_factory.isChunkedPost() && conn instanceof HttpURLConnection) {
      try {
        HttpURLConnection httpConn = (HttpURLConnection) conn;

        httpConn.setChunkedStreamingMode(8 * 1024);
      } catch (Throwable e) {
      }
    }

    OutputStream os = null;

    try {
      os = conn.getOutputStream();
    } catch (Exception e) {
      throw new HessianRuntimeException(e);
    }

    try {
      AbstractHessianOutput out = _factory.getHessianOutput(os);
      out.call(methodName, args);
      out.flush();

      return conn;
    } catch (IOException e) {
      if (conn instanceof HttpURLConnection) {
        ((HttpURLConnection) conn).disconnect();
      }
      throw e;
    } catch (RuntimeException e) {
      if (conn instanceof HttpURLConnection) {
        ((HttpURLConnection) conn).disconnect();
      }

      throw e;
    }
  }

  static class ResultInputStream extends InputStream {

    private HttpURLConnection _conn;
    private InputStream _connIs;
    private AbstractHessianInput _in;
    private InputStream _hessianIs;

    ResultInputStream(HttpURLConnection conn, InputStream is, AbstractHessianInput in,
        InputStream hessianIs) {
      _conn = conn;
      _connIs = is;
      _in = in;
      _hessianIs = hessianIs;
    }

    public int read() throws IOException {
      if (_hessianIs != null) {
        int value = _hessianIs.read();

        if (value < 0) {
          close();
        }

        return value;
      } else {
        return -1;
      }
    }

    public int read(byte[] buffer, int offset, int length) throws IOException {
      if (_hessianIs != null) {
        int value = _hessianIs.read(buffer, offset, length);

        if (value < 0) {
          close();
        }

        return value;
      } else {
        return -1;
      }
    }

    public void close() throws IOException {
      HttpURLConnection conn = _conn;
      _conn = null;

      InputStream connIs = _connIs;
      _connIs = null;

      AbstractHessianInput in = _in;
      _in = null;

      InputStream hessianIs = _hessianIs;
      _hessianIs = null;

      try {
        if (hessianIs != null) {
          hessianIs.close();
        }
      } catch (Exception e) {
        log.debug(e.getMessage(), e);
      }

      try {
        if (in != null) {
          in.completeReply();
          in.close();
        }
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }

      try {
        if (connIs != null) {
          connIs.close();
        }
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }

      try {
        if (conn != null) {
          conn.disconnect();
        }
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    }
  }
}
