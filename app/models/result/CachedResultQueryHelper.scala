package models.result

import java.sql.ResultSetMetaData
import java.util.UUID

import akka.actor.ActorRef
import models.QueryResultResponse
import models.queries.{ DynamicQuery, QueryTranslations }
import models.queries.result.{ CreateResultTable, InsertResultRow }
import models.query.QueryResult
import services.database.ResultCacheDatabase
import utils.Logging

object CachedResultQueryHelper extends Logging {
  def createResultTable(resultId: UUID, columns: Seq[QueryResult.Col]) = {
    val q = CreateResultTable(resultId, columns)(ResultCacheDatabase.conn.engine)
    //log.info(q.sql)
    ResultCacheDatabase.conn.executeUpdate(q)
  }

  def insertRow(tableName: String, columnNames: Seq[String], data: Seq[Any]) = {
    ResultCacheDatabase.conn.executeUpdate(InsertResultRow(tableName, columnNames, data)(ResultCacheDatabase.conn.engine))
  }

  def getResultResponseFor(resultId: UUID, queryId: UUID, sql: String, columns: Seq[QueryResult.Col], data: Seq[Seq[Option[Any]]], elapsedMs: Int) = {
    val mappedData = data.map(_.map(_.map(DynamicQuery.transform)))
    QueryResultResponse(resultId, QueryResult(
      queryId = queryId,
      sql = sql,
      columns = columns,
      data = mappedData,
      rowsAffected = mappedData.size
    ), elapsedMs)
  }

  def sendResult(
    result: CachedResult,
    out: Option[ActorRef],
    columns: Seq[QueryResult.Col],
    rowData: Seq[Seq[Option[Any]]],
    elapsedMs: Int,
    moreRowsAvailable: Boolean
  ) = {
    out.foreach { o =>
      val mappedData = rowData.map(_.map(_.map(DynamicQuery.transform)))
      val msg = QueryResultResponse(result.resultId, QueryResult(
        queryId = result.queryId,
        sql = result.sql,
        columns = columns,
        data = mappedData,
        rowsAffected = mappedData.size,
        moreRowsAvailable = moreRowsAvailable,
        source = Some(QueryResult.Source(
          t = "cache",
          name = result.tableName,
          sortable = true
        ))
      ), elapsedMs)
      o ! msg
    }
  }
}

