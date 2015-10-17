package io.ololo.messenger

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.{View, MenuItem}
import android.widget.AdapterView.OnItemClickListener
import android.widget.{AdapterView, ArrayAdapter, ListView}
import io.ololo.messenger.models.Contacts

class ChooseContactActivity extends AppCompatActivity with Contacts {

  var list: ListView = _
  protected override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    getSupportActionBar.setDisplayHomeAsUpEnabled(true)
    setContentView(R.layout.activity_choose_contact)
    list = findViewById(android.R.id.list).asInstanceOf[ListView]
    val adapter = new ArrayAdapter[String](this, android.R.layout.simple_list_item_1, CONTACTS)
    list.setAdapter(adapter)
    list.setOnItemClickListener(new OnItemClickListener {
      override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long): Unit = {
        setResult(Activity.RESULT_OK, new Intent().putExtra("contact", adapter.getItem(position)))
        finish()
      }
    })
  }


  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    val id: Int = item.getItemId
    if (id == android.R.id.home) {
      finish()
    }
    super.onOptionsItemSelected(item)
  }
}