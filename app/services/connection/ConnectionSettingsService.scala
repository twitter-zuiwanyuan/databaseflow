package services.connection

import java.util.UUID

import models.connection.ConnectionSettings
import models.queries.connection.ConnectionSettingsQueries
import models.user.{Role, User}
import services.database.{DatabaseRegistry, MasterDatabase, ResultCacheDatabase}

object ConnectionSettingsService {
  def getAll = MasterDatabase.query(ConnectionSettingsQueries.getAll()) ++ MasterDatabase.settings.toSeq
  def getVisible(user: Option[User]) = MasterDatabase.query(ConnectionSettingsQueries.getVisible(user)) ++ MasterDatabase.settings.toSeq

  def canRead(user: Option[User], cs: ConnectionSettings) = Role.matchPermissions(user, cs.owner, "connection", "read", cs.read)
  def canEdit(user: Option[User], cs: ConnectionSettings) = if (cs.id == services.database.MasterDatabase.connectionId) {
    false -> "Cannot edit master database."
  } else {
    Role.matchPermissions(user, cs.owner, "connection", "edit", cs.edit)
  }

  def getById(id: UUID) = if (id == MasterDatabase.connectionId) {
    MasterDatabase.settings
  } else if (id == ResultCacheDatabase.connectionId) {
    ResultCacheDatabase.settings
  } else {
    MasterDatabase.query(ConnectionSettingsQueries.getById(id))
  }

  def insert(connSettings: ConnectionSettings) = MasterDatabase.executeUpdate(ConnectionSettingsQueries.insert(connSettings))
  def update(connSettings: ConnectionSettings) = {
    DatabaseRegistry.flush(connSettings.id)
    MasterDatabase.executeUpdate(ConnectionSettingsQueries.Update(connSettings))
  }
  def delete(id: UUID) = MasterDatabase.executeUpdate(ConnectionSettingsQueries.removeById(id))
}
