package services

import java.util.UUID

import models._
import models.template.query.{QueryErrorTemplate, QueryPlanTemplate}
import org.scalajs.jquery.{jQuery => $}
import ui.ProgressManager
import ui.modal.PlanNodeDetailManager
import utils.JQueryUtils

object QueryPlanService {
  def handlePlanResultResponse(pr: PlanResultResponse) = {
    //Logging.info(s"Received plan with [${pr.plan.maxRows}] rows.")
    val occurred = new scalajs.js.Date(pr.result.occurred.toDouble)
    val content = QueryPlanTemplate.forPlan(pr, occurred.toISOString)
    ProgressManager.completeProgress(pr.result.queryId, pr.id, content)

    val workspace = $(s"#workspace-${pr.result.queryId}")
    val panel = $(s"#${pr.id}", workspace)

    workOutPlanWidth(pr.id)

    val chart = $(".plan-chart", panel)
    val raw = $(".plan-raw", panel)

    val nodes = pr.result.node.withChildren()
    nodes.foreach { node =>
      JQueryUtils.clickHandler($(s"#plan-node-${node.id}", panel), (x) => {
        PlanNodeDetailManager.show(node, pr.result.node.costs.cost.getOrElse(0))
      })
    }

    val planViewToggle = $(".plan-view-toggle", workspace)
    var showingChart = true

    JQueryUtils.clickHandler(planViewToggle, (f) => {
      if (showingChart) {
        planViewToggle.text("View Plan Chart")
        chart.hide()
        raw.show()
      } else {
        planViewToggle.text("View Raw Plan")
        chart.show()
        raw.hide()
      }
      showingChart = !showingChart
    })
  }

  def handlePlanErrorResponse(per: PlanErrorResponse) = {
    val occurred = new scalajs.js.Date(per.error.occurred.toDouble)
    val content = QueryErrorTemplate.forPlanError(per, occurred.toISOString)

    ProgressManager.completeProgress(per.error.queryId, per.id, content)
  }

  private[this] def workOutPlanWidth(id: UUID) = {
    val container = $("#" + id)
    val tree = $(".tree", container)
    val rootNode = $(".root-node", container)

    val originalTreeWidth = tree.width()
    var treeWidth = originalTreeWidth
    var nodeWidth = rootNode.width()
    var continue = true
    while (continue) {
      treeWidth = tree.width(treeWidth + 500).width()
      val newNodeWidth = rootNode.width()
      if (newNodeWidth.toInt == nodeWidth.toInt) {
        continue = false
      } else {
        nodeWidth = newNodeWidth
      }
    }
    tree.width(nodeWidth + 20)
  }
}
