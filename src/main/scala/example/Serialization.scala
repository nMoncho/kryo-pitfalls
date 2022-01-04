package example

import com.twitter.chill._
import org.objenesis.strategy.StdInstantiatorStrategy

import scala.collection.mutable.ArrayBuffer

object Serialization {

  type KryoRegistrar = (Kryo, Int) => Unit

  val GUESS_THREADS_PER_CORE = 4
  val poolSize: Int = GUESS_THREADS_PER_CORE * Runtime.getRuntime.availableProcessors

  trait Serde[T] {
    def serialize(obj: T): Array[Byte]
    def deserialize(bytes: Array[Byte]): T
  }

  def emptyRegister: KryoRegistrar = (kryo: Kryo, startId: Int) => ()

  def sequentialRegister(classes: Seq[Class[_]]): KryoRegistrar = {
    val classesWithIndex = classes.zipWithIndex
    (kryo: Kryo, startId: Int) =>
      classesWithIndex.foreach { case (clazz, index) =>
        kryo.register(clazz, startId + index)
      }
  }

  def kryoInstantiator(registrationRequired: Boolean)(registrar: KryoRegistrar): KryoInstantiator =
    (new ScalaKryoInstantiator)
      .withRegistrar((kryo: Kryo) => {

        kryo.addDefaultSerializer(
          classOf[scala.Enumeration#Value],
          classOf[EnumerationSerializer]
        )

        // Taken from org.apache.spark.serializer.KryoSerializer in Spark 1.6.0
        // Register types missed by Chill.
        // scalastyle:off
        kryo.register(classOf[Array[Tuple1[Any]]], 9001)
        kryo.register(classOf[Array[Tuple2[Any, Any]]], 9002)
        kryo.register(classOf[Array[Tuple3[Any, Any, Any]]], 9003)
        kryo.register(classOf[Array[Tuple4[Any, Any, Any, Any]]], 9004)
        kryo.register(classOf[Array[Tuple5[Any, Any, Any, Any, Any]]], 9005)
        kryo.register(
          classOf[Array[Tuple6[Any, Any, Any, Any, Any, Any]]],
          9006
        )
        kryo.register(
          classOf[Array[Tuple7[Any, Any, Any, Any, Any, Any, Any]]],
          9007
        )
        kryo.register(
          classOf[Array[Tuple8[Any, Any, Any, Any, Any, Any, Any, Any]]],
          9008
        )
        kryo.register(
          classOf[Array[Tuple9[Any, Any, Any, Any, Any, Any, Any, Any, Any]]],
          9009
        )
        kryo.register(
          classOf[Array[Tuple10[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any]]],
          9010
        )
        kryo.register(
          classOf[Array[Tuple11[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any]]],
          9011
        )
        kryo.register(
          classOf[Array[Tuple12[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any]]],
          9012
        )
        kryo.register(
          classOf[Array[Tuple13[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any]]],
          9013
        )
        kryo.register(
          classOf[Array[Tuple14[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any]]],
          9014
        )
        kryo.register(
          classOf[Array[Tuple15[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any]]],
          9015
        )
        kryo.register(
          classOf[Array[Tuple16[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any]]],
          9016
        )
        kryo.register(
          classOf[Array[Tuple17[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any]]],
          9017
        )
        kryo.register(
          classOf[Array[Tuple18[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any]]],
          9018
        )
        kryo.register(
          classOf[Array[
            Tuple19[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any]
          ]],
          9019
        )
        kryo.register(
          classOf[Array[
            Tuple20[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any]
          ]],
          9020
        )
        kryo.register(
          classOf[Array[
            Tuple21[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any]
          ]],
          9021
        )
        kryo.register(
          classOf[Array[
            Tuple22[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any]
          ]],
          9022
        )

        kryo.register(None.getClass, 9023)
        kryo.register(Nil.getClass, 9024)
        kryo.register(
          classOf[scala.collection.immutable.$colon$colon[_]],
          9025
        )
        kryo.register(classOf[ArrayBuffer[Any]], 9026)
        kryo.register(classOf[Array[Byte]], 9027)
        kryo.register(
          classOf[scala.collection.immutable.List[Nothing]],
          9028
        )

        // Register classes
        registrar(kryo, 9029)
      })
      .setRegistrationRequired(registrationRequired)
      .setInstantiatorStrategy(new StdInstantiatorStrategy())
}

class Serialization(poolSize: Int, kryoInstantiator: KryoInstantiator) {

  private val kryoPool: KryoPool =
    KryoPool.withByteArrayOutputStream(poolSize, kryoInstantiator)

  def writeClassAndObject(obj: AnyRef): Array[Byte] = kryoPool.toBytesWithClass(obj)

  def writeObject[T](obj: T): Array[Byte] = kryoPool.toBytesWithoutClass(obj)

  def readClassAndObject(bytes: Array[Byte]): AnyRef = kryoPool.fromBytes(bytes)

  def readObject[T](bytes: Array[Byte], clazz: Class[T]): T = kryoPool.fromBytes(bytes, clazz)

  def withoutClassSerde[T <: AnyRef](clazz: Class[T]): Serialization.Serde[T] = new Serialization.Serde[T] {
    override def serialize(obj: T): Array[Byte] =
      writeObject(obj)

    override def deserialize(bytes: Array[Byte]): T =
      readObject(bytes, clazz)
  }

  def withClassSerde[T <: AnyRef](): Serialization.Serde[T] = new Serialization.Serde[T] {
    override def serialize(obj: T): Array[Byte] =
      writeClassAndObject(obj)

    override def deserialize(bytes: Array[Byte]): T =
      readClassAndObject(bytes).asInstanceOf[T]
  }
}
