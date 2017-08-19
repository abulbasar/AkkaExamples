package com.example.Music

import akka.actor.{Actor, ActorSystem, Props}
import com.example.Music.MusicController._
import com.example.Music.MusicPlayer._



//Music controller message
object MusicController{
  sealed trait ControllerMessage
  case object Play extends ControllerMessage
  case object Stop extends ControllerMessage

  def props = Props[MusicController]
}


// Music controller
class MusicController extends Actor{
  override def receive: Receive = {
    case Play =>
      println("[MusicController] Music started ....")
    case Stop =>
      print("[MusicController] Music stopped ...")
    case _ =>
      println("[MusicController] Unknown message")
  }
}



// Music player message
object MusicPlayer{
  sealed trait PlayMessage
  case object StopMusic extends PlayMessage
  case object StartMusic extends PlayMessage
  def props = Props[MusicPlayer]
}


// Music Player
class MusicPlayer extends Actor{
  override def receive: Receive = {
    case StopMusic =>
      println("[Music Player] I don't want to stop music")
    case StartMusic =>
      println("[MusicPlayer] sending message to MusicPlayer")
      val controller = context.actorOf(MusicController.props, "controller")
      controller ! Play
    case _ =>
      println("[MusicPlayer] Unknown message")
  }
}


object MainApp{

  def main(args: Array[String]): Unit = {

    // Create actor system
    val system = ActorSystem("MusicActorSystem")

    //Create music player
    val player = system.actorOf(Props[MusicPlayer], "player")

    // Send message to music player actor
    player ! StartMusic

    println("[MainApp] Waiting to terminate the actor system")

    Thread.sleep(3000)
    system.terminate()
    println("[MainApp] Terminated the actor system")
  }

}

