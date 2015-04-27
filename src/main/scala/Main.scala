import RabbitMQ.Subscriber
import argonaut.Json
import com.rabbitmq.client.Channel
import JsonModel._
import doobie.free.connection.ConnectionOp
import scalaz.concurrent.Task
import scalaz._
import doobie.imports._, scalaz._, Scalaz._, scalaz.concurrent.Task

/**
 * Created by ryanstradling on 3/15/15.
 */

object Main extends App {
  lazy val server = "localhost"
  lazy val port = 5672
  lazy val qName = "MYQUEUE"

  def createContacts() : List[Person] = {
    List(
       Person("Joe", "Blow", "email@.com", None, None),
         Person("Sarah", "Noon", "email@.com", None, None),
         Person("Heather", "Runner", "email@.com", None, None),
         Person("John", "Friend", "email@.com", None, None),
         Person("Sam", "Walback",  "email@.com", None, None),
         Person("Mike", "Plane",  "email@.com", None, None),
         Person("Super", "Friend", "email@.com", None, None),
         Person("Lois", "Lane", "email@.com", None, None))
  }
  import scalaz.stream.Process
  def publishContacts(channel : Channel, data : List[Json]) : Process[Task, Unit] = {
    val pub = RabbitMQ.Publisher(channel, qName)
    Process.emitAll(data.map(_.toString)) flatMap pub.publishProcess
  }


  override def main (args : Array[String]): Unit = {
    import argonaut._, Argonaut._
    val jsonContacts: List[Json] = createContacts.map(x => x.asJson)
    val connection = RabbitMQ.Connection(server, port)
    val channel = RabbitMQ.Channel(connection)
    val queue = RabbitMQ.Queue(channel)(qName)

    val sub = Subscriber(channel, qName)
    val ret: Process[Task, Int] = for {
      a <- sub.subscribeZAll
      b <- JsonConverter.process(a)
      c <- Database.addPerson(b)
    } yield (c)

    ret.run.runAsync (_ match {
      case \/-(right) => System.out.println(s"Right = $right")
      case -\/(left) => System.out.println(s"Left = $left")
    })

    publishContacts(channel, jsonContacts).runLog.runAsync(x => x match {
      case \/-(right) => println(right)
      case -\/(left) => println(left)
    })
  }
}
