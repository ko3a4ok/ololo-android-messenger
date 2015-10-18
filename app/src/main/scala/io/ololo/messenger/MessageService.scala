package io.ololo.messenger

import java.net.URI

import android.app.Service
import android.content.{Context, Intent}
import android.os.{Binder, IBinder}
import android.support.v4.content.LocalBroadcastManager
import io.ololo.messenger.models.MessagesDB
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft_17
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject

import scala.concurrent.{ExecutionContext, Future}

object MessageService {
  val ACTION = "message"
}
class MessageService extends Service {

  var db: MessagesDB = _
  var name: String = _
  var webSocket: WebSocketClient = _
  import scala.language.implicitConversions
  implicit def toRunnable[F](f: => F): Runnable =
    new Runnable() { def run() = f }

  class MessageBinder extends Binder {
    def sendMessage(text: String, to: String): Unit = {
      val o = new JSONObject
      o.put("sender", name)
      o.put("message", text)
      o.put("to", to)
      webSocket.send(o.toString)
    }
  }

  private[this] val binder = new MessageBinder

  def onBind(intent: Intent): IBinder = {
    binder
  }


  override def onCreate(): Unit = {
    db = new MessagesDB(this)

    Future{
      initConnection
    }(ExecutionContext.global)
  }


  def initConnection: Unit = {
    name = getSharedPreferences("pref", Context.MODE_PRIVATE).getString("name", null)
    webSocket = new WebSocketClient(new URI(s"ws://192.168.0.103:8080/chat?name=$name"), new Draft_17) {
      override def onError(ex: Exception): Unit = {
        println("Error: " + ex)
      }

      override def onMessage(message: String): Unit = {
        println("Received message: " + message)
        val o = new JSONObject(message)
        db.storeMessage(o.optString("sender"), System.currentTimeMillis(), false, o.optString("message"))
        LocalBroadcastManager.getInstance(MessageService.this)sendBroadcast(new Intent(MessageService.ACTION))
      }

      override def onClose(code: Int, reason: String, remote: Boolean): Unit = {
        println("Closed")
      }

      override def onOpen(handshakedata: ServerHandshake): Unit = {
        println("Start Connection")
      }
    }
    println("Start Connecting")
    webSocket.connect()
  }
  override def onStartCommand(intent: Intent, flags: Int, startId: Int): Int = Service.START_STICKY
}