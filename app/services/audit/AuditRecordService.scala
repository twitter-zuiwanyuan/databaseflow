package services.audit

import java.util.UUID

import akka.actor.ActorRef
import models.audit.{AuditRecord, AuditStatus, AuditType}
import models.queries.audit.{AuditRecordQueries, AuditReportQueries}
import models.{AuditRecordRemoved, AuditRecordResponse, GetQueryHistory, RemoveAuditHistory}
import util.FutureUtils.defaultContext
import services.database.core.MasterDatabase
import util.{DateUtils, Logging}

import scala.concurrent.Future

object AuditRecordService extends Logging {
  val rowLimit = 100

  def getAll(limit: Int, offset: Int) = MasterDatabase.query(AuditRecordQueries.GetPage(None, limit, offset))
  def getForUser(userId: UUID, limit: Int, offset: Int) = MasterDatabase.query(AuditReportQueries.GetForUser(userId, limit, offset))

  def handleGetQueryHistory(connectionId: UUID, userId: UUID, gqh: GetQueryHistory, out: ActorRef) = {
    val matching = MasterDatabase.query(AuditReportQueries.GetMatchingQueries(connectionId, userId, gqh.limit, gqh.offset))
    out ! AuditRecordResponse(matching)
  }

  def handleRemoveAuditHistory(userId: UUID, connectionId: Option[UUID], rqh: RemoveAuditHistory, out: ActorRef) = rqh.id match {
    case Some(auditId) => AuditRecordService.removeAudit(auditId, Some(out))
    case None => AuditRecordService.removeAuditsForUser(userId, connectionId, Some(out))
  }

  def removeAudit(id: UUID, out: Option[ActorRef]) = if (delete(id) == 1) {
    out.foreach(_ ! AuditRecordRemoved(Some(id)))
  }

  def removeAuditsForUser(userId: UUID, connectionId: Option[UUID], out: Option[ActorRef]) = if (deleteAllForUser(userId, connectionId) > 1) {
    out.foreach(_ ! AuditRecordRemoved(None))
  }

  def removeAuditsForGuest(connectionId: Option[UUID], out: Option[ActorRef]) = if (deleteAllForGuest(connectionId) > 1) {
    out.foreach(_ ! AuditRecordRemoved(None))
  }

  def start(
    auditId: UUID,
    t: AuditType,
    owner: UUID,
    connection: Option[UUID] = None,
    sql: Option[String] = None
  ) = insert(AuditRecord(
    id = auditId,
    auditType = t,
    owner = owner,
    connection = connection,
    status = AuditStatus.Started,
    sql = sql,
    error = None,
    rowsAffected = None,
    elapsed = 0,
    occurred = DateUtils.nowMillis
  ))

  def complete(auditId: UUID, newType: AuditType, rowsAffected: Int, elapsed: Int) = {
    MasterDatabase.executeUpdate(AuditRecordQueries.Complete(auditId, newType, rowsAffected, elapsed))
  }
  def error(auditId: UUID, message: String, elapsed: Int) = {
    MasterDatabase.executeUpdate(AuditRecordQueries.Error(auditId, message, elapsed))
  }

  def create(t: AuditType, owner: UUID, connection: Option[UUID], sql: Option[String] = None, elapsed: Int = 0) = insert(AuditRecord(
    auditType = t,
    owner = owner,
    connection = connection,
    sql = sql,
    elapsed = elapsed,
    occurred = DateUtils.nowMillis
  ))

  def insert(auditRecord: AuditRecord) = Future {
    MasterDatabase.executeUpdate(AuditRecordQueries.insert(auditRecord))
  }.failed.foreach(x => log.warn(s"Unable to log audit [$auditRecord].", x))

  def delete(id: UUID) = MasterDatabase.executeUpdate(AuditRecordQueries.removeById(id))
  def deleteAllForUser(userId: UUID, connectionId: Option[UUID]) = {
    MasterDatabase.executeUpdate(AuditRecordQueries.RemoveForUser(userId, connectionId))
  }
  def deleteAllForGuest(connectionId: Option[UUID]) = MasterDatabase.executeUpdate(AuditRecordQueries.RemoveForAnonymous(connectionId))
  def deleteAll() = MasterDatabase.executeUpdate(AuditRecordQueries.RemoveAll)
}
