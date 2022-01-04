package example

import example.Models.Message
import org.scalameter.Reporter
import org.scalameter.Reporter.Composite
import org.scalameter.api.{Bench, Gen}
import org.scalameter.reporting.LoggingReporter

/** Runs a speed benchmark on Serialization With/Without Registration, persisting a run into a CSV file.
 */
object RegistrationBenchmark extends Bench.LocalTime {

  override def reporter: Reporter[Double] = Composite[Double](
    LoggingReporter(),
    CsvReporter("size")
  )

  private val sizes = Gen.range("size")(10000, 50000, 10000)

  private val ranges = for {
    size <- sizes
  } yield (0 until size).map(_ => Generators.genMessage.sample.get)

  performance of "WithRegistration" in {
    val kryo = new Serialization(
      Serialization.poolSize,
      Serialization.kryoInstantiator(registrationRequired = true)(Serialization.sequentialRegister(Models.classes))
    )
    val withoutClassSerde = kryo.withoutClassSerde(classOf[Message])
    val withClassSerde = kryo.withClassSerde[Message]()

    measure method "serialization without class in payload" in {
      using(ranges) in { messages =>
        messages.foreach { message =>
          withoutClassSerde.deserialize(
            withoutClassSerde.serialize(message)
          )
        }
      }
    }

    measure method "serialization with class in payload" in {
      using(ranges) in { messages =>
        messages.foreach { message =>
          withClassSerde.deserialize(
            withClassSerde.serialize(message)
          )
        }
      }
    }
  }

  performance of "WithoutRegistration" in {
    val kryo = new Serialization(
      Serialization.poolSize,
      Serialization.kryoInstantiator(registrationRequired = false)(Serialization.emptyRegister)
    )
    val withoutClassSerde = kryo.withoutClassSerde(classOf[Message])
    val withClassSerde = kryo.withClassSerde[Message]()

    measure method "serialization without class in payload" in {
      using(ranges) in { messages =>
        messages.foreach { message =>
          withoutClassSerde.deserialize(
            withoutClassSerde.serialize(message)
          )
        }
      }
    }

    measure method "serialization with class in payload" in {
      using(ranges) in { messages =>
        messages.foreach { message =>
          withClassSerde.deserialize(
            withClassSerde.serialize(message)
          )
        }
      }
    }
  }

}
