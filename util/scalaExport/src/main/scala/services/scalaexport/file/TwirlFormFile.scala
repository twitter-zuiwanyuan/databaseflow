package services.scalaexport.file

import models.scalaexport.TwirlFile
import services.scalaexport.config.ExportModel

object TwirlFormFile {
  def export(model: ExportModel) = {
    val formFile = TwirlFile(model.viewPackage, model.propertyName + "Form")
    formFile.add(s"@(user: models.user.User, model: ${model.modelClass}, act: Call, isNew: Boolean = false)(")
    formFile.add("    implicit request: Request[AnyContent], session: Session, flash: Flash, traceData: util.tracing.TraceData")
    val toInterp = model.pkFields.map(f => "${model." + f.propertyName + "}").mkString(", ")
    formFile.add(s""")@traceData.logViewClass(getClass)@layout.admin(user, "explore", s"${model.title} [$toInterp]") {""", 1)

    formFile.add(s"""<form action="@act" method="POST">""", 1)
    formFile.add("""<div class="collection with-header">""", 1)

    formFile.add("<div class=\"collection-header\">", 1)
    formFile.add("<div class=\"right\">", 1)
    formFile.add(s"""<button type="submit" class="btn theme">@if(isNew) {Create} else {Save} ${model.title}</button>""")
    formFile.add("</div>", -1)
    formFile.add("<h5>", 1)
    formFile.add(s"""<i class="fa @models.template.Icons.${model.propertyName}"></i>""")
    val toTwirl = model.pkFields.map(f => "@model." + f.propertyName).mkString(", ")
    formFile.add(s"""${model.title} [$toTwirl]""")
    formFile.add("</h5>", -1)
    formFile.add("</div>", -1)

    formFile.add("<div class=\"collection-item\">", 1)
    formFile.add("<table class=\"highlight\">", 1)
    formFile.add("<tbody>", 1)

    model.fields.foreach { field =>
      formFile.add("<tr>", 1)
      formFile.add("<td>", 1)
      val inputFields = s"""type="checkbox" name="${field.propertyName}.include" id="${field.propertyName}.include" value="true""""
      if (model.pkFields.exists(_.columnName == field.columnName)) {
        formFile.add(s"""<input $inputFields @if(isNew) { checked="checked" } />""")
      } else if (field.notNull) {
        formFile.add(s"""<input $inputFields @if(isNew) { checked="checked" } />""")
      } else {
        formFile.add(s"""<input $inputFields />""")
      }
      formFile.add(s"""<label for="${field.propertyName}.include">${field.title}</label>""")
      formFile.add("</td>", -1)

      formFile.add("<td>", 1)
      TwirlFormFields.inputFor(field, formFile)
      formFile.add(s"</td>", -1)
      formFile.add("</tr>", -1)
    }

    formFile.add("</tbody>", -1)
    formFile.add("</table>", -1)
    formFile.add("</div>", -1)

    formFile.add("</div>", -1)
    formFile.add("</form>", -1)

    formFile.add("}", -1)

    formFile
  }
}