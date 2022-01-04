package example

import example.Models._
import org.scalacheck.Gen

object Generators {

  val genByteArray: Gen[Array[Byte]] = Gen.asciiPrintableStr.map(_.getBytes())

  val genDiagnosticsType: Gen[DiagnosticsType] =
    Gen.oneOf(DiagnosticsType.NoInfo, DiagnosticsType.StandardInfo, DiagnosticsType.ExtendedInfo, DiagnosticsType.Unrecognized)

  val genNormalizedValue: Gen[NormalizedValue] =
    Gen.oneOf(
      Gen.choose(Double.MinValue, Double.MaxValue).map(PhysicalValue.apply),
      Gen.asciiPrintableStr.map(CategoricalValue.apply),
      genByteArray.map(PluginValue.apply),
      for {
        value <- Gen.choose(Double.MinValue, Double.MaxValue)
        diagType <- genDiagnosticsType
      } yield DiagnosticsValue(value, diagType)
    )

  val genSignal: Gen[Signal] = for {
    id <- Gen.choose(0, Int.MaxValue)
    name <- Gen.asciiPrintableStr
    raw <- Gen.either(Gen.choose(Long.MinValue, Long.MaxValue), genByteArray)
    value <- Gen.option(genNormalizedValue)
  } yield Signal(id, name, raw, value)

  val genMessage: Gen[Message] = for {
    id <- Gen.uuid
    seq <- Gen.choose(0, Long.MaxValue)
    ts <- Gen.choose(0, System.currentTimeMillis())
    anony <- Gen.oneOf(true, false)
    signals <- Gen.listOfN(10, genSignal)
  } yield Message(id, seq, ts, anony, signals)
}
