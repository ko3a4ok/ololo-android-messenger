package io.ololo.messenger

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.{Menu, MenuItem}

class MainActivity extends AppCompatActivity {
  protected override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    getMenuInflater.inflate(R.menu.menu_main, menu)
    return true
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    val id: Int = item.getItemId
    if (id == R.id.action_settings) {
      return true
    }
    return super.onOptionsItemSelected(item)
  }
}