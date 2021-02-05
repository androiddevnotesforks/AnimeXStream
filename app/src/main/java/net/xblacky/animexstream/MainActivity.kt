package net.xblacky.animexstream

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import io.realm.Realm
import kotlinx.android.synthetic.main.main_activity.*
import net.xblacky.animexstream.utils.model.SettingsModel
import net.xblacky.animexstream.utils.realm.InitalizeRealm
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    @SuppressLint("BinaryOperationInTimber")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        if (Build.VERSION.SDK_INT < VERSION_CODES.Q){
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
//        }else{
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
//        }
        toggleDayNight()
        setContentView(R.layout.main_activity)
        bottomNavigationView.setupWithNavController(container.findNavController())

    }

    private fun toggleDayNight() {
        val realm: Realm = Realm.getInstance(InitalizeRealm.getConfig());
        realm.executeTransaction { realm1: Realm ->
            val  settings = realm1.where(SettingsModel::class.java).findFirst()
            if (settings == null ) {
                val settings2 = realm.createObject(SettingsModel::class.java)
                when (resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        Timber.e("Night Mode")
                        settings2.nightmodeon =true
                    }

                    Configuration.UI_MODE_NIGHT_NO -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                            } else {
                                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                            }
                            window.decorView.systemUiVisibility = flags
                        }
                        settings2.nightmodeon =false
                        Timber.e("Day Mode")
                    }
                }

                realm1.insertOrUpdate(settings2)
            } else {
                if (settings.nightmodeon == false) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//                rootView.toggleMode.text = getString(R.string.toggle_to_night_mode)
                  //  Toast.makeText(this, "Light Mode", Toast.LENGTH_SHORT).show()
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//                rootView.toggleMode.text = getString(R.string.toggle_to_light_mode)
                  //  Toast.makeText(this, "Night Mode", Toast.LENGTH_SHORT).show()
                }


            }







        }

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val data: String? = intent?.dataString
        Timber.e("""mal :${data}""")
        //TODO: do something with new intent
    }
}
