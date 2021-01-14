package net.xblacky.animexstream

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import net.xblacky.animexstream.utils.realm.InitalizeRealm
import timber.log.Timber

class AnimeXStream : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        InitalizeRealm.initializeRealm(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }


}