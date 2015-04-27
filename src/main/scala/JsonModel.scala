import scalaz._, Scalaz._
import argonaut._, Argonaut._


object JsonModel {

  // parse the string as json, attempt to decode it to a list of person,
  // otherwise just take it as an empty list.
//  val people = input.decodeOption[List[Person]].getOrElse(Nil)

  // work with your data types as you normally would
 // val nice = people.map(person =>
 //   person.copy(greeting = person.greeting.orElse(Some("Hello good sir!"))))

  // convert back to json, and then to a pretty printed string, alternative
  // ways to print may be nospaces, spaces2, or a custom format

//  val result = nice.asJson
//  println(result.spaces4)

 // assert(result.array.exists(_.length == 3))

  case class Person(firstName: String, lastName : String,
                    email: String, homePhone: Option[String], workPhone: Option[String])

  object Person {
    implicit def PersonCodecJson: CodecJson[Person] =
      casecodec5(Person.apply, Person.unapply)("firstName", "lastName", "email",
        "homePhone", "workPhone")
  }
}





