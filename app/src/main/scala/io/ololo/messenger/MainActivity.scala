package io.ololo.messenger

import java.util

import android.app.AlertDialog.Builder
import android.app.{Dialog, Activity}
import android.content.DialogInterface.{OnCancelListener, OnClickListener}
import android.content._
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.view.{View, Menu, MenuItem}
import android.widget.AdapterView.OnItemClickListener
import android.widget.{EditText, AdapterView, SimpleAdapter, ListView}
import io.ololo.messenger.models.MessagesDB

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.collection.JavaConversions.bufferAsJavaList

class MainActivity extends AppCompatActivity {

  var db: MessagesDB = _
  var lastMessages: ArrayBuffer[util.Map[String, String]] = _
  var list: ListView = _

  import scala.language.implicitConversions
  implicit def toRunnable[F](f: => F): Runnable =
    new Runnable() { def run() = f }

  protected override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    if (getSharedPreferences("pref", Context.MODE_PRIVATE).contains("name"))
      startService(new Intent(this, classOf[MessageService]))
    else
      showDialog(1)
    list = new ListView(this)
    list.setOnItemClickListener(new OnItemClickListener {
      override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long): Unit =
      openDialog(lastMessages(position).get(MessagesDB.COLUMN_FROM))
    })
    setContentView(list)

    db = new MessagesDB(this)
    LocalBroadcastManager.getInstance(this).registerReceiver(broadcast, new IntentFilter(MessageService.ACTION))
  }

  def update: Unit = {
    Future {
      lastMessages = db.getLastMessages
      runOnUiThread(
      {
        val adapter = new SimpleAdapter(this,
          lastMessages,
          R.layout.main_item,
          Array(MessagesDB.COLUMN_FROM, MessagesDB.COLUMN_CONTENT, MessagesDB.COLUMN_TIME),
          Array(android.R.id.text1, android.R.id.text2, R.id.time))
        list.setAdapter(adapter)
      }
      )
    } (ExecutionContext.global)
  }
  protected override def onResume(): Unit = {
    super.onResume()
    update
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    getMenuInflater.inflate(R.menu.menu_main, menu)
    true
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    val id: Int = item.getItemId
    if (id == R.id.action_settings) {
      showDialog(0)
    }
    super.onOptionsItemSelected(item)
  }

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent): Unit = {
    if (resultCode == Activity.RESULT_OK && requestCode == 0) {
      val contact = data.getStringExtra("contact")
      println(s"CONTACT: $contact")
      openDialog(contact)
    }
  }

  def openDialog(contact: String) ={
    startActivity(new Intent(this, classOf[DialogActivity]).putExtra("contact", contact))
  }

  override def onCreateDialog(id: Int): Dialog = {
    id match {
      case 0 => {
        val input = new EditText(this)
        val d = new Builder(this)
          .setTitle(R.string.input_user_name)
          .setView(input)
          .setPositiveButton(android.R.string.ok, new OnClickListener {
          override def onClick(dialog: DialogInterface, which: Int): Unit = {
            val name = input.getText.toString
            openDialog(name)
          }
        })
        d.create()
      }
      case 1 => {
        val input = new EditText(this)
        val d = new Builder(this)
          .setTitle(R.string.input_your_name)
          .setView(input)
          .setPositiveButton(android.R.string.ok, new OnClickListener {
            override def onClick(dialog: DialogInterface, which: Int): Unit = {
              val name = input.getText.toString
              getSharedPreferences("pref", Context.MODE_PRIVATE).edit().putString("name", name).commit()
              startService(new Intent(MainActivity.this, classOf[MessageService]))
            }
          })
          .setOnCancelListener(new OnCancelListener {
          override def onCancel(dialog: DialogInterface): Unit = {
            MainActivity.this.finish
          }})
        d.create()
      }
    }
  }

  val broadcast = new BroadcastReceiver {
    override def onReceive(context: Context, intent: Intent): Unit = {
      update
    }
  }
}