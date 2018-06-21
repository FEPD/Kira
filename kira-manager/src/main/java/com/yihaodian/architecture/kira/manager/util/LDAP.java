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
package com.yihaodian.architecture.kira.manager.util;

import java.util.Hashtable;
import javax.naming.AuthenticationException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

public class LDAP {

  public int connect(String userid, String passwd, String url, String dns) {
    LdapContext ctx = null;
    Hashtable env = new Hashtable();
    try {
      env.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
      env.put("java.naming.provider.url", "ldap://" + url + "/");
      env.put("java.naming.security.principal", userid + "@" + dns);
      env.put("java.naming.security.credentials", passwd);

      ctx = new InitialLdapContext(env, null);
      int i = 0;
      return i;
    } catch (AuthenticationException e) {
      e.printStackTrace();
      return 1;
    } catch (Exception e) {
      e.printStackTrace();
      return 2;
    } finally {
      if (ctx != null) {
        try {
          ctx.close();
        } catch (Exception e) {
          System.out.println(e.toString());
          return -1;
        }
      }
      env = null;
    }
  }
}