package example

import org.scalameter.{Context, CurveData, Measurement, Persistor, Reporter}
import org.scalameter.utils.Tree

import java.io.{File, FileWriter}
import scala.collection.Seq

case class CsvReporter[T](param: String, separator: String = ",") extends Reporter[T] {
  override def report(result: CurveData[T], persistor: Persistor): Unit = {
    val addHeader: Boolean = {
      val f = new File(filename(result.context))

      !f.exists() || f.length() == 0
    }

    val fw = new FileWriter(filename(result.context), true)
    try {
      if (addHeader) {
        fw.write(s"${header(result.measurements)}\n")
      }

      fw.write(s"${dataRow(result.measurements)}\n")
    } finally {
      fw.close()
    }
  }

  private def filename(ctx: Context): String =
    s"${ctx.scope.replaceAll("\\W", "_")}.csv"

  private def header(measurements: Seq[Measurement[T]]): String =
    measurements.map { measurement =>
      s"${param}_${measurement.params(param)}"
    }.mkString(separator)

  private def dataRow(measurements: Seq[Measurement[T]]): String =
    measurements.map { measurement =>
      measurement.value.toString
    }.mkString(separator)

  override def report(results: Tree[CurveData[T]], persistor: Persistor): Boolean = true
}
