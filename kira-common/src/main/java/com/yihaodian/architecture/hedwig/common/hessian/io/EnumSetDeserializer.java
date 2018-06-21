/**
 *
 */
package com.yihaodian.architecture.hedwig.common.hessian.io;

import com.caucho.hessian.io.AbstractHessianInput;
import com.caucho.hessian.io.AbstractMapDeserializer;
import java.io.IOException;
import java.util.EnumSet;

/**
 * @author archer
 */
public class EnumSetDeserializer extends AbstractMapDeserializer {

  public Object readMap(AbstractHessianInput in) {
    EnumSet es = null;
    try {
      es = readEnumSet(in, Enum.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return es;
  }

  private <E extends Enum<E>> EnumSet<E> readEnumSet(AbstractHessianInput in,
      Class<E> klass) throws IOException {
    EnumSetSerializationProxy<E> essp = new EnumSetSerializationProxy<E>();
    int ref = in.addRef(essp);
    in.readObject();
    essp.elementType = in.readObject();
    in.readObject();
    essp.elements = (Enum<E>[]) in.readObject();
    EnumSet<E> es = null;
    if (essp.elements != null) {
      es = EnumSet.noneOf((Class<E>) essp.elementType);
      for (Object element : essp.elements) {
        es.add((E) element);
      }
    }

    return es;
  }

  private static class EnumSetSerializationProxy<E extends Enum<E>> {

    Object elementType;
    Enum<E>[] elements;
  }
}
