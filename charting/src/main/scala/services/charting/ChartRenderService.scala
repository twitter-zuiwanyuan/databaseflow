package services.charting

import org.scalajs.dom
import org.scalajs.dom.Element
import org.scalajs.jquery.{jQuery => $}

import scala.scalajs.js

object ChartRenderService {
  private[this] var plotly: Option[js.Dynamic] = None
  private[this] val strippedTitles = Seq(
    "Save and edit plot in cloud", "Produced with Plotly",
    "Toggle show closest data on hover", "Show closest data on hover", "Compare data on hover",
    "Box Select", "Lasso Select", "Reset axes", "Reset camera to last save"
  )

  def init() = plotly = Some(js.Dynamic.global.Plotly)
  def resizeHandler(el: Element) = plotly.map(_.Plots.resize(el))

  def render(v: ChartingService.ChartValues) = {
    val chartData: Seq[js.Dynamic] = Seq(
      js.Dynamic.literal(
        "x" -> js.Array(1, 2, 3, 4, 5, 6),
        "y" -> js.Array(1, 2, 4, 8, 16, 32)
      )
    )
    val baseOptions = js.Dynamic.literal(
      "margin" -> js.Dynamic.literal("l" -> 0, "r" -> 0, "t" -> 0, "b" -> 0)
    )

    renderChart(v.chartPanel.get(0), chartData, baseOptions)
  }

  def renderChart(el: Element, data: Seq[js.Dynamic], options: js.Dynamic) = {
    plotly.map(_.newPlot(el, js.Array(data: _*), options))
    $(".modebar-btn", el).each { (x: Int, y: dom.Element) =>
      val jq = $(y)
      if (strippedTitles.contains(jq.data("title").toString)) {
        jq.remove()
      }
    }
  }
}
