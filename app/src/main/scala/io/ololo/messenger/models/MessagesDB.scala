package io.ololo.messenger.models

import android.content.Context
import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}
import android.os.Bundle

import scala.collection.mutable.ArrayBuffer


/**
 * Created by ko3a4ok on 10/17/15.
 */
class MessagesDB(ctx: Context) extends {
  val DATABASE_VERSION = 1
  val DATABASE_NAME = "Messages.db"
} with SQLiteOpenHelper(ctx, DATABASE_NAME, null, DATABASE_VERSION) {

  override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int): Unit = {
    db.execSQL(s"DROP TABLE IF EXISTS $TABLE");
    onCreate(db)
  }

  val COLUMN_FROM = "contact"
  val COLUMN_TIME = "message_time"
  val COLUMN_CONTENT = "message_text"
  val COLUMN_IS_OWN = "is_own"
  val TABLE = "MESSAGES"
  val SQL_CREATE_ENTRIES = s"CREATE TABLE if not exists $TABLE($COLUMN_FROM text, $COLUMN_IS_OWN integer, $COLUMN_TIME DATETIME, $COLUMN_CONTENT text)"

  override def onCreate(db: SQLiteDatabase): Unit = {
    println("EXECUTING: " + SQL_CREATE_ENTRIES)
    db.execSQL(SQL_CREATE_ENTRIES)
  }

  val SQL_LAST_MESSAGES = s"select * from $TABLE"
  def getLastMessages: ArrayBuffer[Bundle] = {
    val db = getReadableDatabase
    val res = db.rawQuery(SQL_LAST_MESSAGES, null)
    res.moveToFirst()
    val arr = new ArrayBuffer[Bundle]()
    while (!res.isAfterLast) {
      arr += res.getExtras
      res.moveToNext()
    }
    arr
  }
}
