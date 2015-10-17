package io.ololo.messenger

import android.app.Activity
import android.content.Intent
import android.os.{AsyncTask, Bundle}
import android.support.v7.app.AppCompatActivity
import android.view.{Menu, MenuItem}
import io.ololo.messenger.models.MessagesDB

import scala.collection.mutable.ArrayBuffer

class MainActivity extends AppCompatActivity {

  var db: MessagesDB = _
  var lastMessages: ArrayBuffer[Bundle] = _
  protected override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    db = new MessagesDB(this)
  }

  protected override def onResume(): Unit = {
    super.onResume()
    new AsyncTask[Void, Void, Void] {
      override def doInBackground(params: Void*): Unit =
        lastMessages = db.getLastMessages
    }
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