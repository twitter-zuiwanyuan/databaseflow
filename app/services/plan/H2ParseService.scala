package services.plan

import models.plan.{ PlanNode, PlanResult }

object H2ParseService extends PlanParseService("h2") {
  override def parse(sql: String, plan: String) = {
    PlanResult(
      name = "",
      action = "",
      sql = sql,
      asText = plan,
      node = PlanNode(title = "TODO", nodeType = "?")
    )
  }
}
