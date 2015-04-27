import argonaut._, Argonaut._
import JsonModel.Person
import scalaz.concurrent.Task
import scalaz.{\/-, -\/, \/}

object JsonConverter {
  def JsonStringToPerson(person: String): Person = {
    person.decodeOption[Person].get
  }

  def process(input: String): scalaz.stream.Process[Task, Person] = scalaz.stream.Process.eval(Task(JsonStringToPerson(input)))

}

