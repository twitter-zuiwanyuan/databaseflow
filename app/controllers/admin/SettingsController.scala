package controllers.admin

import controllers.BaseController
import models.settings.SettingKey
import services.settings.SettingsService
import services.user.UserService
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class SettingsController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def settings = withAdminSession("admin-settings") { implicit request =>
    Future.successful(Ok(views.html.admin.settings(request.identity, ctx.config.debug, SettingsService.getAll.map(s => s.key -> s.value).toMap)))
  }

  def saveSettings = withAdminSession("admin-settings-save") { implicit request =>
    val form = request.body.asFormUrlEncoded.getOrElse(throw new IllegalStateException()).flatMap(x => x._2.headOption.map(x._1 -> _)).toSeq
    form.foreach { x =>
      SettingKey.withNameOption(x._1) match {
        case Some(settingKey) => SettingsService.set(settingKey, x._2)
        case None => log.warn(s"Attempt to save invalid setting [${x._1}].")
      }
    }
    Future.successful(Redirect(controllers.routes.HomeController.index()))
  }
}
