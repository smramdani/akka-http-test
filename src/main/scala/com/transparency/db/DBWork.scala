package com.transparency.db


import akka.actor.Actor
import akka.pattern.pipe
import com.transparency.domain.{Work, WorkKey}

import scala.concurrent.Future

class DBWork extends Actor {

  private implicit val ec = context.dispatcher

  type Works = List[Work]

  override def receive: Receive = {
    case "tout" => listWorks().pipeTo(sender)
    case workId: Int => workById(workId).pipeTo(sender)
    case workKey: WorkKey => fetchWorks(workKey).pipeTo(sender)
  }

  // Fake function in place of SQL request implementation
  private def workById(workId: Int): Future[Option[Work]] = {
    DBAccess.workById(workId)
  }

  // Fake function in place of SQL request implementation
  private def fetchWorks(workKey: WorkKey): Future[Works] = {
    DBAccess.fetchWorks(workKey.isrc, workKey.iswc)
  }

  // Fake function in place of SQL request implementation
  private def listWorks(): Future[Works] = {
    DBAccess.listWorks()
  }
}
