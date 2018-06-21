/**
 *
 */
package com.yihaodian.architecture.hedwig.common.hessian.io;

import com.caucho.hessian.io.Deserializer;
import com.caucho.hessian.io.HessianProtocolException;
import com.caucho.hessian.io.Serializer;
import com.caucho.hessian.io.SerializerFactory;
import com.caucho.hessian.io.StringValueSerializer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Archer
 */
public class HedwigExtSerializerFactory extends SerializerFactory {

  private static Map<Class, Serializer> _extSerializerMap = new HashMap<Class, Serializer>();
  private static Map<Class, Deserializer> _extDeserializerMap = new HashMap<Class, Deserializer>();
  private static SerializerFactory factory = new HedwigExtSerializerFactory();

  static {
    _extSerializerMap.put(BigDecimal.class, new StringValueSerializer());
    _extSerializerMap.put(BigInteger.class, new StringValueSerializer());

    _extDeserializerMap.put(BigDecimal.class, new BigDecimalDeserializer());
    _extDeserializerMap.put(BigInteger.class, new BigIntegerDeserializer());

    _extDeserializerMap.put(Date.class, new SqlDateDeserializer(Date.class));

  }

  private HedwigExtSerializerFactory() {
    super();
  }

  public static SerializerFactory createFactory() {
    return factory;
  }

  @Override
  public Serializer getSerializer(Class clazz) throws HessianProtocolException {
    Serializer s = _extSerializerMap.get(clazz);
    if (s == null) {
      s = super.getSerializer(clazz);
    }
    return s;
  }

  @Override
  public Deserializer getDeserializer(Class clazz) throws HessianProtocolException {
//		if(clazz.getName().equals("java.util.EnumSet$SerializationProxy")){
//			_extDeserializerMap.put(clazz, new EnumSetDeserializer());
//		}
    Deserializer d = _extDeserializerMap.get(clazz);
    if (d == null) {
      d = super.getDeserializer(clazz);
    }
    return d;
  }
}
