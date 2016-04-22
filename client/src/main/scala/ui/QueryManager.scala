package ui

import java.util.UUID

import models.SubmitQuery
import models.template.{ Icons, QueryResultsTemplate }
import org.scalajs.jquery.{ JQuery, jQuery => $ }

import scala.scalajs.js
import scala.scalajs.js.timers.setTimeout

object QueryManager {
  var activeQueries = Seq.empty[UUID]
  var sqlEditors = Map.empty[UUID, js.Dynamic]

  lazy val workspace = $("#workspace")

  def addQuery(queryId: UUID, queryPanel: JQuery, onChange: (String) => Unit, onClose: () => Unit): Unit = {
    val sqlEditor = EditorManager.initSqlEditor(queryId, onChange)

    def wire(q: JQuery, action: String) = utils.JQueryUtils.clickHandler(q, (jq) => {
      val resultId = UUID.randomUUID

      val html = QueryResultsTemplate.loadingPanel(queryId, "Loading Test", resultId).render
      val queryWorkspace = $(s"#workspace-$queryId", workspace)
      queryWorkspace.prepend(html)

      val sql = sqlEditor.getValue().toString
      utils.NetworkMessage.sendMessage(SubmitQuery(queryId, sql, Some(action), resultId))
    })

    wire($(".run-query-link", queryPanel), "run")
    wire($(".explain-query-link", queryPanel), "explain")
    wire($(".analyze-query-link", queryPanel), "analyze")

    utils.JQueryUtils.clickHandler($(s".${Icons.close}", queryPanel), (jq) => {
      QueryManager.closeQuery(queryId)
      onClose()
    })

    sqlEditor.selection.selectAll()
    setTimeout(500) {
      sqlEditor.focus()
    }

    activeQueries = activeQueries :+ queryId
    sqlEditors = sqlEditors + (queryId -> sqlEditor)
  }

  def getSql(queryId: UUID) = sqlEditors.get(queryId) match {
    case Some(editor) => editor.getValue().toString
    case None => ""
  }

  def closeQuery(queryId: UUID): Unit = {
    if (activeQueries.size == 1) {
      AdHocQueryManager.addNewQuery()
    }

    //utils.Logging.info(s"Closing [$queryId].")

    val originalIndex = activeQueries.indexOf(queryId)
    activeQueries = activeQueries.filterNot(_ == queryId)
    sqlEditors = sqlEditors - queryId

    sqlEditors.get(queryId).foreach(_.destroy())
    $(s"#panel-$queryId").remove()
    TabManager.removeTab(queryId)

    val newId = activeQueries(if (originalIndex < 1) { 0 } else { originalIndex - 1 })
    TabManager.selectTab(newId)
  }
}
