package services.charting

import java.util.UUID

import models.charting.{ChartDataExtractor, ChartOptionsExtractor, ChartSettings}
import org.scalajs.dom
import org.scalajs.dom.UIEvent
import org.scalajs.jquery.{JQuery, jQuery => $}

import scala.scalajs.js

object ChartingService {
  private[this] var activeCharts = Map.empty[UUID, ChartValues]

  case class ChartValues(optionsPanel: JQuery, chartPanel: JQuery, settings: ChartSettings, columns: Seq[(String, String)], data: js.Array[js.Array[String]]) {
    lazy val chartData = ChartDataExtractor.getJsData(settings, columns, data)
    lazy val baseOptions = ChartOptionsExtractor.getJsOptions(settings)
  }

  def init() = {
    ChartRenderService.init()
    dom.window.onresize = (ev: UIEvent) => {
      activeCharts.foreach(x => ChartRenderService.resizeHandler(x._2.chartPanel.get(0)))
    }
  }

  def addChart(id: UUID, settings: ChartSettings, columns: Seq[(String, String)], data: js.Array[js.Array[String]]) = {
    val el = $(s"#$id")
    if (el.length != 1) {
      throw new IllegalStateException(s"Missing element for chart [$id].")
    }
    val optionsPanel = $(".chart-options-panel", el)
    if (optionsPanel.length != 1) {
      throw new IllegalStateException(s"Missing options panel for chart [$id].")
    }

    val chartPanel = $(".chart-panel", el)
    if (chartPanel.length != 1) {
      throw new IllegalStateException(s"Missing chart panel for chart [$id].")
    }

    val v = ChartValues(optionsPanel, chartPanel, settings, columns, data)
    activeCharts = activeCharts + (id -> v)
    ChartSettingsService.renderOptions(id, optionsPanel, columns)
    ChartRenderService.render(v)
  }

  def getSettings(id: UUID) = activeCharts.getOrElse(id, throw new IllegalStateException(s"Invalid chart [$id].")).settings
  def updateSettings(id: UUID, settings: ChartSettings) = activeCharts.get(id) match {
    case Some(v) =>
      val newV = v.copy(settings = settings)
      activeCharts = activeCharts + (id -> newV)
      ChartRenderService.render(newV)
    case None => throw new IllegalStateException(s"Cannot update settings for unknown chart [$id].")
  }

  def updateData(id: UUID, data: js.Array[js.Array[String]]) = activeCharts.get(id) match {
    case Some(v) =>
      val newV = v.copy(data = data)
      activeCharts = activeCharts + (id -> newV)
      ChartRenderService.render(newV)
    case None => throw new IllegalStateException(s"Cannot update data for unknown chart [$id].")
  }

  def removeChart(id: UUID) = {
    activeCharts = activeCharts - id
  }
}
