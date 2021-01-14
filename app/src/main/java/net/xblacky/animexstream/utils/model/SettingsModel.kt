package net.xblacky.animexstream.utils.model

import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


public open class SettingsModel  (
  //  @PrimaryKey
    var paheanimeon: Boolean = false

): RealmObject()
