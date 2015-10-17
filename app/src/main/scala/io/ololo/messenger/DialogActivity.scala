package io.ololo.messenger

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem

class DialogActivity extends AppCompatActivity {
  var contact: String = _
  protected override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    getSupportActionBar.setDisplayHomeAsUpEnabled(true)
    contact = getIntent.getStringExtra("contact")
    setTitle(contact)
    setContentView(R.layout.activity_dialog)
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    val id: Int = item.getItemId
    if (id == android.R.id.home) {
      finish()
    }
    super.onOptionsItemSelected(item)
  }
}