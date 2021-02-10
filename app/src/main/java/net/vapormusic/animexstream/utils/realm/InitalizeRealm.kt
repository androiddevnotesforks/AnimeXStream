package net.vapormusic.animexstream.utils.realm

import android.content.Context
import io.realm.Realm
import io.realm.RealmConfiguration

class InitalizeRealm{
    companion object{

        private lateinit var config: RealmConfiguration
        fun initializeRealm(context: Context){
            Realm.init(context)
            config = RealmConfiguration.Builder().name("animexstream.realm").schemaVersion(1).deleteRealmIfMigrationNeeded().build()
        }

        fun getConfig(): RealmConfiguration{
            return config
        }
    }
}