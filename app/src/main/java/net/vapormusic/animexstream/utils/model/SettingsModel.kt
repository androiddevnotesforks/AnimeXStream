package net.vapormusic.animexstream.utils.model

import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


public open class SettingsModel  (
  //  @PrimaryKey
    var paheanimeon: Boolean = false,
    var nightmodeon: Boolean = false,
    var playercontrolson : Boolean = true,
    var malsyncon : Boolean = false,
    var malaccesstoken : String = "",
    var malrefreshtoken : String = "",
    var malaccesstime : Int = 0,
    var googlecdn: Boolean = false
): RealmObject()
