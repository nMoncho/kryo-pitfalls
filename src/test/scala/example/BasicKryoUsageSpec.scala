package example

import com.twitter.chill.{Input, Kryo, Output}
import example.BasicKryoUsageSpec._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.{FileInputStream, FileOutputStream}

object BasicKryoUsageSpec {
  // age, id, name
  case class Person(id: Long, name: String, age: Int) {
    def this() = this(0, "", 0)
  }

  // size, timestamp, wholePayload
  case class Message(wholePayload: String, size: Int, ts: Long) {
    def this() = this("", 0,  0)
  }
}

class BasicKryoUsageSpec extends AnyWordSpec with Matchers {

  "Kryo" should {
    val alice = Person(1L, "Alice", 42)

    "serialize objects with registration" in {
      val kryo = new Kryo()
      kryo.register(classOf[Person])

      val output = new Output(new FileOutputStream("object-with-registration.bin"))
      kryo.writeObject(output, alice)
      output.close()

      val input = new Input(new FileInputStream("object-with-registration.bin"))
      val aliceFromFile = kryo.readObject(input, classOf[Person])
      input.close()

      aliceFromFile shouldBe alice
    }

    "serialize objects _without_ registration" in {
      val kryo = new Kryo()
      kryo.setRegistrationRequired(false)

      val output = new Output(new FileOutputStream("object-without-registration.bin"))
      kryo.writeObject(output, alice)
      output.close()

      val input = new Input(new FileInputStream("object-without-registration.bin"))
      val aliceFromFile = kryo.readObject(input, classOf[Person])
      input.close()

      aliceFromFile shouldBe alice
    }

    "mistake different classes" in {
      val kryo = new Kryo()
      kryo.register(classOf[Person])
      kryo.register(classOf[Message])

      val output = new Output(new FileOutputStream("mistake-classes.bin"))
      kryo.writeObject(output, alice)
      output.close()

      val input = new Input(new FileInputStream("mistake-classes.bin"))
      val messageFromFile = kryo.readObject(input, classOf[Message])
      input.close()

      messageFromFile shouldBe a[Message]
    }

    "serialize classes inside the payload, with registration" in {
      val kryo = new Kryo()
      kryo.register(classOf[Person])
      kryo.register(classOf[Message])

      val output = new Output(new FileOutputStream("class-and-object-with-registration.bin"))
      kryo.writeClassAndObject(output, alice)
      output.close()

      val input = new Input(new FileInputStream("class-and-object-with-registration.bin"))
      val personFromFile = kryo.readClassAndObject(input)
      input.close()

      personFromFile shouldBe a[Person]
    }

    "serialize classes inside the payload, without registration" in {
      val kryo = new Kryo()
      kryo.setRegistrationRequired(false)

      val output = new Output(new FileOutputStream("class-and-object-without-registration.bin"))
      kryo.writeClassAndObject(output, alice)
      output.close()

      val input = new Input(new FileInputStream("class-and-object-without-registration.bin"))
      val personFromFile = kryo.readClassAndObject(input)
      input.close()

      personFromFile shouldBe a[Person]
    }
  }

}
