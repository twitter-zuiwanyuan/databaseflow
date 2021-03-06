package controllers.admin

import controllers.BaseSiteController
import play.api.i18n.MessagesApi
import util.FutureUtils.defaultContext
import play.api.libs.ws.WSClient

@javax.inject.Singleton
class MetricsController @javax.inject.Inject() (implicit override val messagesApi: MessagesApi, ws: WSClient) extends BaseSiteController {
  def view() = withAdminSession("admin.metrics") { (_, request) =>
    implicit val req = request

    val url = "http://localhost:9001/metrics?pretty=true"
    val call = ws.url(url).withHttpHeaders("Accept" -> "application/json")
    val f = call.get()

    f.map { json =>
      Ok(views.html.admin.metrics(json.body))
    }
  }
}
