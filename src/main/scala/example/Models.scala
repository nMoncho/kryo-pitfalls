package example

import java.util.UUID

object Models {

  case class Message(id: UUID, sequence: Long, timestamp: Long, anonymous: Boolean, signals: List[Signal])
  case class Signal(id: Int, name: String, raw: Either[Long, Array[Byte]], normalized: Option[NormalizedValue])

  sealed trait NormalizedValue extends Product with Serializable
  case class PhysicalValue(value: Double) extends NormalizedValue
  case class CategoricalValue(value: String) extends NormalizedValue
  case class PluginValue(value: Array[Byte]) extends NormalizedValue {
    override def equals(obj: Any): Boolean =
      obj match {
        case PluginValue(v) => value.sameElements(v)
        case _ => false
      }

    override def hashCode(): Int = {
      val NumberOfFields: Int = 1
      var ret                 = NumberOfFields
      ret = ret * 41 + (if (value.isEmpty) 0 else value.hashCode())
      ret
    }
  }
  case class DiagnosticsValue(value: Double, kind: DiagnosticsType) extends NormalizedValue

  sealed trait DiagnosticsType
  object DiagnosticsType {
    case object NoInfo extends DiagnosticsType
    case object StandardInfo extends DiagnosticsType
    case object ExtendedInfo extends DiagnosticsType
    case object Unrecognized extends DiagnosticsType
  }

  val classes: Seq[Class[_]] = {
    import DiagnosticsType._
    Seq(classOf[Message], classOf[Signal], classOf[PhysicalValue], classOf[CategoricalValue],
      classOf[PluginValue], classOf[DiagnosticsValue], classOf[NoInfo.type], classOf[StandardInfo.type],
      classOf[ExtendedInfo.type], classOf[Unrecognized.type])
  }
}
