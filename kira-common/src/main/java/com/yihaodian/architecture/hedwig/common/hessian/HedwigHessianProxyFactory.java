/**
 *
 */
package com.yihaodian.architecture.hedwig.common.hessian;

import com.caucho.hessian.client.HessianProxyFactory;
import com.caucho.hessian.io.AbstractHessianInput;
import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.HessianRemoteObject;
import com.yihaodian.architecture.hedwig.common.util.HedwigTimeoutUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.codec.binary.Base64;

/**
 * @author Archer
 */
public class HedwigHessianProxyFactory extends HessianProxyFactory {

  private String _huser;
  private String _hpassword;
  private String _hbasicAuth;

  /**
   * Creates the URL connection.
   */
  protected URLConnection openConnection(URL url) throws IOException {
    URLConnection conn = url.openConnection();

    conn.setDoOutput(true);
    int timeout = 1000;
    try {
      Long reqReadTimeout = HedwigTimeoutUtil.getRequestReadTimeout();
      if (reqReadTimeout != null && reqReadTimeout > 0L) {
        timeout = reqReadTimeout.intValue();
      } else {
        timeout = (int) super.getReadTimeout();
      }
    } catch (Exception e) {
    }
    if (timeout > 0) {
      conn.setReadTimeout(timeout);
      conn.setConnectTimeout(timeout);
    }
    conn.setRequestProperty("Content-Type", "x-application/hessian");
    try {
      if (_hbasicAuth != null) {
        conn.setRequestProperty("Authorization", _hbasicAuth);
      } else if (_huser != null && _hpassword != null) {
        _hbasicAuth =
            "Basic " + new String(Base64.encodeBase64((_huser + ":" + _hpassword).getBytes()));
        conn.setRequestProperty("Authorization", _hbasicAuth);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return conn;
  }

  public Object create(Class api, String urlName, ClassLoader loader) throws MalformedURLException {
    if (api == null) {
      throw new NullPointerException("api must not be null for HessianProxyFactory.create()");
    }
    InvocationHandler handler = null;

    URL url = new URL(urlName);
    handler = new HedwigHessianProxy(this, url);

    return Proxy.newProxyInstance(loader, new Class[]{api, HessianRemoteObject.class}, handler);
  }

  public AbstractHessianOutput getHessianOutput(OutputStream os) {
    AbstractHessianOutput out = new HedwigHessianOutput(os);
    return out;
  }

  @Override
  public AbstractHessianInput getHessianInput(InputStream is) {
    AbstractHessianInput in = new HedwigHessianInput(is);
    return in;
  }

  @Override
  public void setPassword(String password) {
    super.setPassword(password);
    _hpassword = password;
  }

  @Override
  public void setUser(String user) {
    super.setUser(user);
    _huser = user;
  }

}
