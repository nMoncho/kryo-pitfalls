package example

import example.Models.Message
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import java.io.{File, FileWriter}

/** Runs a space benchmark on Serialization With/Without Registration, persisting a run into a CSV file.
 */
class SpaceSerializationBenchmark extends AnyWordSpec with Matchers {

  private val sizes = 10000.to(50000).by(10000)
  private val values = for {
    size <- sizes
  } yield (size -> (0 until size).map(_ => Generators.genMessage.sample.get))


  private def calculateSize(serde: Serialization.Serde[Message]): Map[Int, Long] =
    values.map { case (size, messages) =>
      size -> messages.foldLeft(0L){ case (acc, message) => acc + serde.serialize(message).length }
    }.toMap

  "SequentialRegistrar" should {
    val suite = "SequentialRegistrar.serialization"
    val requiredRegistration = new Serialization(
      Serialization.poolSize,
      Serialization.kryoInstantiator(registrationRequired = true)(
        Serialization.sequentialRegister(Models.classes))
    )
    val rrWithoutClassSerde = requiredRegistration.withoutClassSerde(classOf[Message])
    val rrWithClassSerde = requiredRegistration.withClassSerde[Message]()

    val notRequiredRegistration = new Serialization(
      Serialization.poolSize,
      Serialization.kryoInstantiator(registrationRequired = false)(
        Serialization.sequentialRegister(Models.classes))
    )
    val nrWithoutClassSerde = notRequiredRegistration.withoutClassSerde(classOf[Message])
    val nrWithClassSerde = notRequiredRegistration.withClassSerde[Message]()

    "serialization without class in payload (registration required)" in {
      val measurements = calculateSize(rrWithoutClassSerde)
      persist(
        suite = suite,
        test = "serialization without class in payload (registration required)",
        measurements = measurements
      )
    }

    "serialization with class in payload (registration required)" in {
      val measurements = calculateSize(rrWithClassSerde)
      persist(
        suite = suite,
        test = "serialization with class in payload (registration required)",
        measurements = measurements
      )
    }

    "serialization without class in payload (registration not required)" in {
      val measurements = calculateSize(nrWithoutClassSerde)
      persist(
        suite = suite,
        test = "serialization without class in payload (registration not required)",
        measurements = measurements
      )
    }

    "serialization with class in payload (registration not required)" in {
      val measurements = calculateSize(nrWithClassSerde)
      persist(
        suite = suite,
        test = "serialization with class in payload (registration not required)",
        measurements = measurements
      )
    }
  }

  "EmptyRegistrar" should {
    val suite = "EmptyRegistrar.serialization"
    val kryo = new Serialization(
      Serialization.poolSize,
      Serialization.kryoInstantiator(registrationRequired = false)(Serialization.emptyRegister)
    )
    val withoutClassSerde = kryo.withoutClassSerde(classOf[Message])
    val withClassSerde = kryo.withClassSerde[Message]()

    "serialization without class in payload" in {
      val measurements = calculateSize(withoutClassSerde)
      persist(
        suite = suite,
        test = "serialization without class in payload",
        measurements = measurements
      )
    }

    "serialization with class in payload" in {
      val measurements = calculateSize(withClassSerde)
      persist(
        suite = suite,
        test = "serialization with class in payload",
        measurements = measurements
      )
    }
  }

  private def persist(suite: String, test: String, measurements: Map[Int, Long], separator: String = ","): Unit = {
    def filename(): String = {
      val name = s"${suite}_${test}".replaceAll("\\W", "_")
      s"${name}_space.csv"
    }

    def header(): String = measurements.map { case (size, _) =>
      s"size_${size}"
    }.mkString(separator)

    def dataRow(): String = measurements.map { case (_, value) => value.toString }.mkString(separator)

    val addHeader: Boolean = {
      val f = new File(filename())

      !f.exists() || f.length() == 0
    }

    val fw = new FileWriter(filename(), true)
    try {
      if (addHeader) {
        fw.write(s"${header()}\n")
      }

      fw.write(s"${dataRow()}\n")
    } finally {
      fw.close()
    }
  }
}
