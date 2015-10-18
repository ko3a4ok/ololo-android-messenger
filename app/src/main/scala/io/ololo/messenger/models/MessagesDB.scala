package io.ololo.messenger.models

import java.util

import android.content.{ContentValues, Context}
import android.database.Cursor
import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}
import android.os.Bundle
import io.ololo.messenger.DialogActivity

import scala.collection.mutable.ArrayBuffer


/**
 * Created by ko3a4ok on 10/17/15.
 */

object MessagesDB {
  val COLUMN_FROM = "contact"
  val COLUMN_TIME = "message_time"
  val COLUMN_CONTENT = "message_text"
  val COLUMN_IS_OWN = "is_own"
  val TABLE = "MESSAGES"
}
class MessagesDB(ctx: Context) extends {
  val DATABASE_VERSION = 1
  val DATABASE_NAME = "Messages.db"
} with SQLiteOpenHelper(ctx, DATABASE_NAME, null, DATABASE_VERSION) {
  import MessagesDB._
  override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int): Unit = {
    db.execSQL(s"DROP TABLE IF EXISTS $TABLE");
    onCreate(db)
  }

  val SQL_CREATE_ENTRIES = s"CREATE TABLE if not exists $TABLE($COLUMN_FROM text, $COLUMN_IS_OWN integer, $COLUMN_TIME integer, $COLUMN_CONTENT text)"

  override def onCreate(db: SQLiteDatabase): Unit = {
    println("EXECUTING: " + SQL_CREATE_ENTRIES)
    db.execSQL(SQL_CREATE_ENTRIES)
  }

  import scala.language.implicitConversions
  implicit def toBundle(res: Cursor) = {
    val b = new Bundle()
    b.putString(COLUMN_CONTENT, res.getString(res.getColumnIndex(COLUMN_CONTENT)))
    b.putString(COLUMN_FROM, res.getString(res.getColumnIndex(COLUMN_FROM)))
    b.putInt(COLUMN_IS_OWN, res.getInt(res.getColumnIndex(COLUMN_IS_OWN)))
    b.putLong(COLUMN_TIME, res.getLong(res.getColumnIndex(COLUMN_TIME)))
    b
  }
  implicit def toMap(res: Cursor):java.util.Map[String, String] = {
    val b = new util.HashMap[String, String]()
    b.put(COLUMN_CONTENT, res.getString(res.getColumnIndex(COLUMN_CONTENT)))
    b.put(COLUMN_FROM, res.getString(res.getColumnIndex(COLUMN_FROM)))
//    b.put(COLUMN_IS_OWN, res.getInt(res.getColumnIndex(COLUMN_IS_OWN)))
    b.put(COLUMN_TIME, DialogActivity.getDateTime(res.getLong(res.getColumnIndex(COLUMN_TIME))))
    b
  }

  val SQL_LAST_MESSAGES = s"select * from $TABLE group by $COLUMN_FROM Order by $COLUMN_TIME DESC"
  def getLastMessages: ArrayBuffer[java.util.Map[String, String]] = {
    val db = getReadableDatabase
    val res = db.rawQuery(SQL_LAST_MESSAGES, null)
    res.moveToFirst()
    val arr = new ArrayBuffer[util.Map[String, String]]()
    while (!res.isAfterLast) {
      arr += res
      res.moveToNext()
    }
    arr
  }

  def storeMessage(contact: String, timestamp: Long, my: Boolean, message: String): Unit = {
    val cv = new ContentValues()
    cv.put(MessagesDB.COLUMN_CONTENT, message)
    cv.put(MessagesDB.COLUMN_FROM, contact)
    cv.put(MessagesDB.COLUMN_IS_OWN, Integer.valueOf(if (my) 1 else 0))
    cv.put(MessagesDB.COLUMN_TIME, java.lang.Long.valueOf(timestamp))
    getWritableDatabase.insert(TABLE, null, cv)
    getWritableDatabase.close()
  }

  val SQL_USER_MESSAGES = s"select * from $TABLE where $COLUMN_FROM = ?"
  def getMessages(contact: String): ArrayBuffer[Bundle] = {
    val res = getReadableDatabase.rawQuery(SQL_USER_MESSAGES, Array(contact))
    res.moveToFirst()
    val arr = new ArrayBuffer[Bundle]()
    while (!res.isAfterLast) {
      arr += res
      res.moveToNext()
    }
    arr
  }
}
