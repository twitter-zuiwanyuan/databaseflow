package services.settings

import models.queries.settings.SettingQueries
import models.settings.{Setting, SettingKey}
import services.database.core.MasterDatabase

object SettingsService {
  private[this] var settings = Seq.empty[Setting]
  private[this] var settingsMap = Map.empty[SettingKey, String]

  def apply(key: SettingKey) = settingsMap.getOrElse(key, key.default)
  def asBool(key: SettingKey) = apply(key) == "true"
  def getOrSet(key: SettingKey, s: => String) = settingsMap.getOrElse(key, set(key, s))

  def load() = {
    settingsMap = MasterDatabase.query(SettingQueries.getAll()).map(s => s.key -> s.value).toMap
    settings = SettingKey.values.map(k => Setting(k, settingsMap.getOrElse(k, k.default)))
  }

  def isOverride(key: SettingKey) = settingsMap.isDefinedAt(key)

  def getAll = settings
  def getOverrides = settings.filter(s => isOverride(s.key))

  def set(key: SettingKey, value: String) = {
    val s = Setting(key, value)
    if (s.isDefault) {
      settingsMap = settingsMap - key
      MasterDatabase.executeUpdate(SettingQueries.removeById(key))
    } else {
      MasterDatabase.transaction { t =>
        t.query(SettingQueries.getById(key)) match {
          case Some(_) => t.executeUpdate(SettingQueries.Update(s))
          case None => t.executeUpdate(SettingQueries.insert(s))
        }
      }
      settingsMap = settingsMap + (key -> value)
    }
    settings = SettingKey.values.map(k => Setting(k, settingsMap.getOrElse(k, k.default)))
    value
  }

  def allowRegistration = asBool(SettingKey.AllowRegistration)
  def allowSignIn = asBool(SettingKey.AllowSignIn)
  def allowAuditRemoval = asBool(SettingKey.AllowAuditRemoval)
}
