package io.ololo.messenger

import java.util

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.{View, Menu, MenuItem}
import android.widget.AdapterView.OnItemClickListener
import android.widget.{AdapterView, SimpleAdapter, ListView}
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
    list = new ListView(this)
    list.setOnItemClickListener(new OnItemClickListener {
      override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long): Unit =
      openDialog(lastMessages(position).get(MessagesDB.COLUMN_FROM))
    })
    setContentView(list)

    db = new MessagesDB(this)
  }

  protected override def onResume(): Unit = {
    super.onResume()
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

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    getMenuInflater.inflate(R.menu.menu_main, menu)
    true
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    val id: Int = item.getItemId
    if (id == R.id.action_settings) {
      startActivityForResult(new Intent(this, classOf[ChooseContactActivity]), 0)
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
}