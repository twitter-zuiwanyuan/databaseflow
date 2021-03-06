package services.scalaexport.config

case class ExportConfiguration(
    key: String,
    projectId: String,
    projectTitle: String,
    models: Seq[ExportModel],
    source: String = "boilerplay",
    engine: ExportEngine = ExportEngine.PostgreSQL,
    projectLocation: Option[String] = None
) {
  def getModel(k: String) = getModelOpt(k).getOrElse(throw new IllegalStateException(s"No model available with name [$k]."))
  def getModelOpt(k: String) = getModelOptWithIgnored(k).filterNot(_.ignored)
  def getModelOptWithIgnored(k: String) = models.find(m => m.tableName == k || m.propertyName == k || m.className == k)
}
