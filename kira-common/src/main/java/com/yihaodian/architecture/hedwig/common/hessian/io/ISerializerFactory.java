package com.yihaodian.architecture.hedwig.common.hessian.io;


public interface ISerializerFactory<S, D> {

  /**
   * Returns the serializer for a class.
   *
   * @param cl the class of the object that needs to be serialized.
   * @return a serializer object for the serialization.
   */
  public S getSerializer(Class cl);

  /**
   * Returns the deserializer for a class.
   *
   * @param cl the class of the object that needs to be deserialized.
   * @return a deserializer object for the serialization.
   */
  public D getDeserializer(Class cl);
}
