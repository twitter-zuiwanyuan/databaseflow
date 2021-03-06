package models.queries.export

import java.io.OutputStream

import com.github.tototoshi.csv.CSVWriter
import models.database.{Query, Row}

case class CsvExportQuery(override val sql: String, override val values: Seq[Any], out: OutputStream) extends Query[Unit] {
  override def reduce(rows: Iterator[Row]) = {
    val writer = CSVWriter.open(out)
    if (rows.hasNext) {
      val firstRow = rows.next()
      val md = firstRow.rs.getMetaData
      val cc = md.getColumnCount
      val columns = (1 to cc).map(md.getColumnLabel)
      writer.writeRow(columns)

      val firstRowData = (1 to cc).map(i => firstRow.asOpt[Any](i).fold("")(_.toString))
      writer.writeRow(firstRowData)

      rows.foreach { row =>
        val data = (1 to cc).map(i => row.asOpt[Any](i).fold("")(_.toString))
        writer.writeRow(data)
      }
    }
    writer.close()
  }
}
