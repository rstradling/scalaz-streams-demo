/**
 * Created by ryanstradling on 3/16/15.
 */


import com.typesafe.config.ConfigFactory
import doobie.hi
import doobie.util.update._
import doobie.imports._, scalaz._, Scalaz._, scalaz.concurrent.Task

import JsonModel.Person

object Database {
  val conf = ConfigFactory.load
  val xa = DriverManagerTransactor[Task](conf.getString("database.driver"), conf.getString("database.url"),
    conf.getString("database.username"), conf.getString("database.password"))
  def addPerson(person : Person) : scalaz.stream.Process[Task, Int] = {
    val x = sql"insert into person (firstName, lastName, email, homePhone, workPhone) values (${person.firstName}, ${person.lastName}, ${person.email}, ${person.homePhone}, ${person.workPhone})".update
    val t = for {
      a <- x.run
    } yield a
    val y: Task[Int] = t.transact(Database.xa)
    scalaz.stream.Process.eval(y)
  }

}

object DatabaseRunner extends App {
  override def main(args : Array[String]) : Unit = {
    Database.addPerson(JsonModel.Person("Ryan", "Stradling", "someone@some.com", None, None))
    val x: Unit = sql"select firstName, lastName, email, homePhone, workPhone from PERSON".query[Person].process.take(5).list.transact(Database.xa).run.foreach(println)


  }
}


