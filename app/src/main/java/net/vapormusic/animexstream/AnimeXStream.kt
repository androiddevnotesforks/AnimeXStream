package net.vapormusic.animexstream

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import io.realm.Realm
import net.vapormusic.animexstream.utils.model.SettingsModel
import net.vapormusic.animexstream.utils.realm.InitalizeRealm
import timber.log.Timber
import java.security.Security
import org.conscrypt.Conscrypt

class AnimeXStream : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        Security.insertProviderAt(Conscrypt.newProvider(), 1)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        InitalizeRealm.initializeRealm(this)


        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }


}