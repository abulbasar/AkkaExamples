package com.example.UserRegistration

import akka.pattern.ask
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.util.Timeout
import com.example.UserRegistration.Checker.{BlackUser, CheckUser, WhiteUser}
import com.example.UserRegistration.Recorder.NewUser
import com.example.UserRegistration.Storage.AddUser

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._



case class User(username: String, email: String)

object Recorder{
  sealed trait RecorderMsg
  //Recorder Messages
  case class NewUser(user:User) extends RecorderMsg

  def props(checker:ActorRef, storage:ActorRef) = Props(new Recorder(checker, storage))

}


object Checker {
  sealed trait CheckerMsg
  // Checker messages
  case class CheckUser(user:User) extends CheckerMsg


  // Checker response messages
  sealed trait CheckerResponseMessage
  case class BlackUser(user:User) extends CheckerResponseMessage
  case class WhiteUser(user:User) extends CheckerResponseMessage

}

object Storage{
  sealed trait StorageMsg
  // Storage Messages
  case class AddUser(user:User) extends StorageMsg
}

class Storage extends Actor{
  private val users = ArrayBuffer.empty[User]

  override def receive: Receive = {
    case AddUser(user) =>
      users += user
      print(s"[Storage] $user added")

  }
}

class Checker extends Actor{

  private val blacklist = Array("Adam", "Ed")

  override def receive: Receive = {
    case CheckUser(user) if blacklist.contains(user.username) =>
      println(s"[Checker] User $user is in the blacklist")
      sender() ! BlackUser(user)
    case CheckUser(user) =>
      println(s"[Checker] User $user is not in the blacklist")
      sender() ! WhiteUser(user)
  }
}

class Recorder(checker: ActorRef, storage: ActorRef) extends Actor {
  import scala.concurrent.ExecutionContext.Implicits.global
  implicit val timeout = Timeout(5 seconds)

  def receive = {
    case NewUser(user) =>
      val f = checker ? CheckUser(user)
      f.map{
        case WhiteUser(user) =>
          storage ! AddUser(user)
        case BlackUser(user) =>
          println(s"[Recorder] $user in the blacklist, so not sent to storage")
      }
      println("Request is sent to checker for blacklist checking")
  }
}


object MainApp {
  val system = ActorSystem("UserRegistrationSystem")
  //Create checker actor
  val checker = system.actorOf(Props[Checker], "checker")

  //Create storage actor
  val storage = system.actorOf(Props[Storage], "storage")

  // Create recorder actor
  val recorder = system.actorOf(Recorder.props(checker, storage), "recorder")

  def requestHandler(): Unit ={
    recorder ! Recorder.NewUser(User("Adam", "adam@email.com"))
    recorder ! Recorder.NewUser(User("John", "john@email.com"))

  }

  def main(args: Array[String]): Unit = {
    // Create user registration actor system
    requestHandler()


    Thread.sleep(5000)
    system.terminate()



  }
}
