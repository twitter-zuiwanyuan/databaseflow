package services.schema

import java.sql.{ Connection, DatabaseMetaData }

import models.database.{ Query, Row }
import models.schema.{ Table, View }
import services.database.DatabaseConnection
import utils.NullUtils

object MetadataViews {
  def getViews(db: DatabaseConnection, conn: Connection, metadata: DatabaseMetaData, catalog: Option[String], schema: Option[String]) = {
    val rs = metadata.getTables(catalog.orNull, schema.orNull, NullUtils.inst, Array("VIEW"))
    new Row.Iter(rs).map(fromRow).toList.sortBy(_.name)
  }

  def withViewDetails(db: DatabaseConnection, conn: Connection, metadata: DatabaseMetaData, views: Seq[View]) = views.map { view =>
    getViewDetails(db, conn, metadata, view)
  }

  private[this] def getViewDetails(db: DatabaseConnection, conn: Connection, metadata: DatabaseMetaData, view: View) = {
    val definition = if (db.engine.showCreateSupported) {
      Some(db(conn, new Query[String] {
        override def sql = db.engine.showCreateView(view.name)
        override def reduce(rows: Iterator[Row]) = rows.map(_.as[String]("Create View")).toList.head
      }))
    } else {
      None
    }

    view.copy(
      definition = definition,
      columns = MetadataColumns.getColumns(metadata, view.catalog, view.schema, view.name)
    )
  }

  private[this] def fromRow(row: Row) = View(
    name = row.as[String]("TABLE_NAME"),
    catalog = row.asOpt[String]("TABLE_CAT"),
    schema = row.asOpt[String]("TABLE_SCHEM"),
    description = row.asOpt[String]("REMARKS"),
    definition = None
  )
}