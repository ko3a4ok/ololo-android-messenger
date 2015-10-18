package io.ololo.messenger

import java.text.SimpleDateFormat
import java.util.{Date, Locale}

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.{ViewGroup, View, MenuItem}
import android.widget.{TextView, ListView, BaseAdapter, EditText}
import io.ololo.messenger.models.MessagesDB

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{ExecutionContext, Future}


object DialogActivity {
  def getDateTime(time: Long): String = {
    val dateFormat = new SimpleDateFormat("HH:mm\ndd MMM yyyy", Locale.getDefault())
    dateFormat.format(new Date(time))
  }
}
class DialogActivity extends AppCompatActivity {
  var contact: String = _
  var db: MessagesDB = _

  var message: EditText = _

  var messages: ArrayBuffer[Bundle] = new ArrayBuffer[Bundle]()
  var adapter: BaseAdapter = _

  import scala.language.implicitConversions
  implicit def toRunnable[F](f: => F): Runnable =
    new Runnable() { def run() = f }

  protected override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    getSupportActionBar.setDisplayHomeAsUpEnabled(true)
    contact = getIntent.getStringExtra("contact")
    setTitle(contact)
    setContentView(R.layout.activity_dialog)
    val list = findViewById(android.R.id.list).asInstanceOf[ListView]
    message = findViewById(R.id.message).asInstanceOf[EditText]
    db = new MessagesDB(this)
    adapter = new BaseAdapter() {

      class ViewHolder(val text: TextView, val time: TextView)

      override def getItemId(position: Int): Long = position

      override def getCount: Int = messages.length

      override def getView(position: Int, convertView: View, parent: ViewGroup): View = {
        var view = convertView
        if (view == null) {
          view = getLayoutInflater.inflate(R.layout.my_message_item, null)
          val vh = new ViewHolder(view.findViewById(R.id.message).asInstanceOf[TextView],
            view.findViewById(R.id.time).asInstanceOf[TextView])
          view.setTag(vh)
        }
        val vh = view.getTag.asInstanceOf[ViewHolder]
        val item = getItem(position)
        vh.text.setText(item.getString(MessagesDB.COLUMN_CONTENT))
        vh.time.setText(DialogActivity.getDateTime(item.getLong(MessagesDB.COLUMN_TIME)))
        view
      }



      override def getItemViewType(position: Int): Int = {
        val item = messages(position)
        val r = item.getInt(MessagesDB.COLUMN_IS_OWN)
        r
      }

      override def getViewTypeCount: Int = {
        2
      }

      override def getItem(position: Int): Bundle = messages(position)

    }
    list.setAdapter(adapter)
    Future {
      messages ++= db.getMessages(contact)
      runOnUiThread(adapter.notifyDataSetChanged())
    }(ExecutionContext.global)
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    val id: Int = item.getItemId
    if (id == android.R.id.home) {
      finish()
    }
    super.onOptionsItemSelected(item)
  }


  def storeMessageInDB(message: String, timestamp: Long, my: Boolean): Unit = {
    db.storeMessage(contact, timestamp, my, message)
    val bundle = new Bundle()
    bundle.putString(MessagesDB.COLUMN_CONTENT, message)
    bundle.putString(MessagesDB.COLUMN_FROM, contact)
    bundle.putInt(MessagesDB.COLUMN_IS_OWN, if (my) 1 else 0)
    bundle.putLong(MessagesDB.COLUMN_TIME, timestamp)
    messages += bundle
    adapter.notifyDataSetChanged()
  }
  def onSend(v: View) = {
    val msg = message.getText.toString
    message.getText.clear()
    storeMessageInDB(msg, System.currentTimeMillis(), true)
  }
}