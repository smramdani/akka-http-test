package com.transparency.db

import com.transparency.domain.Work
import slick.jdbc.H2Profile.api._
//import slick.jdbc.MySQLProfile.api._
//import slick.driver.PostgresDriver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

class WorksTable(tag: Tag) extends Table[(Int, String, String, String)](tag, "WORK") {
  def id = column[Int]("WORK_ID", O.PrimaryKey) // This is the primary key column
  def title = column[String]("WORK_TITLE")

  def isrc = column[String]("WORK_ISRC")

  def iswc = column[String]("WORK_ISWC")

  // Every table needs a * projection with the same type as the table's type parameter
  def * = (id, title, isrc, iswc)
}

object DBAccess {

  val worksTable = TableQuery[WorksTable]

  //Choose your flavour. One only. The config string refers
  //to settings in application.conf
  val db = Database.forConfig("h2mem1")
  //val db = Database.forConfig("mysqlDB")
  //val db = Database.forConfig("postgresDB")

  //User the schema definition to generate DROP statement
  val dropCmd = DBIO.seq(worksTable.schema.drop)

  //User the schema definition to generate a CREATE TABLE
  //command, followed by INSERTs
  val setup = DBIO.seq(worksTable.schema.create,
    worksTable += (1, "Ma chanson", "1234", "5678"),
    worksTable += (2, "Une chanson", "1111", "2222"),
    worksTable += (3, "Autre chanson", "3333", "4444"),
  )

  def runTestQuery = {

    println(">>> Test query after DB init")
    val queryFuture = Future {
      //A very naive query which is the equivalent of SELECT * FROM TABLE
      //and having the FRM map the columns to the params of a partial function
      //
      db.run(worksTable.result).map(_.foreach {
        case (id, title, isrc, iswc) => println(s"   Result = ($title,$isrc,$iswc)")
      })
    }

    //Everything runs asynchronously. Failure to wait for results
    //usually leads to no results :)
    //NOTE: Await does not block here!
    Await.result(queryFuture, Duration.Inf).andThen {
      case Success(_) => println("DB OK") //db.close()  //cleanup DB connection
      case Failure(err) => println(err); println("DB KO!") //handy for debugging failure
    }

  }

  def dropDB: Future[Unit] = {

    //do a drop followed by create
    val dropFuture = Future {
      db.run(dropCmd)
    }

    //Attemp to drop the table, and don't care if it
    //fails (NOT GOOD!)
    Await.result(dropFuture, Duration.Inf).andThen {
      case Success(_) => println("Schema dropped!")
      case Failure(err) => println(err)
    }

  }

  def initDB: Unit = {

    //do a drop followed by create
    val setupFuture = Future {
      db.run(setup)
    }

    //once our DB has finished initializing we are ready to roll !
    //NOTE: Await does not block here!
    Await.result(setupFuture, Duration.Inf).andThen {
      case Success(_) => runTestQuery
      case Failure(err) => println(err);
    }
  }

  def workById(workId: Int): Future[Option[Work]] = {
    val query = worksTable.withFilter(w => w.id === workId).map(w => (w.id, w.title, w.isrc, w.iswc))
    db.run(query.result).map(_.map { case (id, title, isrc, iswc) => Work(id, title, isrc, iswc) }.headOption)
  }

  def listWorks(): Future[List[Work]] = {
    db.run(worksTable.result).map(_.map { case (id, title, isrc, iswc) => Work(id, title, isrc, iswc) }.toList)
  }

  def fetchWorks(isrcVal: String, iswcVal: String): Future[List[Work]] = {
    val query = worksTable.withFilter(w => w.isrc === isrcVal || w.iswc === iswcVal).map(w => (w.id, w.title, w.isrc, w.iswc))
    db.run(query.result).map(_.map { case (id, title, isrc, iswc) => Work(id, title, isrc, iswc) }.toList)
  }

}
