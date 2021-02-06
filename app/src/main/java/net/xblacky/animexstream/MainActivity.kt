package net.xblacky.animexstream

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.realm.Realm
import kotlinx.android.synthetic.main.main_activity.*
import net.xblacky.animexstream.ui.main.animeinfo.AnimeInfoRepository
import net.xblacky.animexstream.utils.constants.C
import net.xblacky.animexstream.utils.model.SettingsModel
import net.xblacky.animexstream.utils.realm.InitalizeRealm
import okhttp3.ResponseBody
import org.json.JSONObject
import timber.log.Timber
import java.lang.System.currentTimeMillis


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
                        settings2.nightmodeon = true
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
                        settings2.nightmodeon = false
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
        val uri: Uri? = intent?.data
        Timber.e("""mal :${uri.toString()}""")
        if (uri!=null && uri.toString().startsWith(C.AUTH_DEEP_LINK)) { getLoginData(uri) }
        //TODO: do something with new intent
    }


    private fun getLoginData(uri: Uri) {
        if (uri.toString().startsWith(C.AUTH_DEEP_LINK)) {
            val code = uri.getQueryParameter("code")
            val receivedState = uri.getQueryParameter("state")
            val animeInfoRepository = AnimeInfoRepository()
            CompositeDisposable().add(
                    animeInfoRepository.fetchMALAccessToken(
                            code = code!!, code_verifier = receivedState!!
                    )
                            .subscribeWith(generateMALAccessToken())
            )
        }
    }

    private fun generateMALAccessToken(): DisposableObserver<ResponseBody> {
        return object : DisposableObserver<ResponseBody>() {
            override fun onNext(t: ResponseBody) {
                val obj = JSONObject(t.string().toString())
                val realm: Realm = Realm.getInstance(InitalizeRealm.getConfig());
                realm.executeTransaction { realm1: Realm ->
                    val  settings = realm1.where(SettingsModel::class.java).findFirst()
                    if (settings == null ) {
                        val settings2 = realm.createObject(SettingsModel::class.java)
                        settings2.malsyncon =true
                        settings2.malaccesstoken = obj.getString("access_token")
                        settings2.malrefreshtoken = obj.getString("refresh_token")
                        settings2.malaccesstime = currentTimeMillis().toInt()
                        Timber.e("mal at: " + settings2.malaccesstoken)
                        Timber.e("mal rt: " + settings2.malrefreshtoken)
                        realm1.insertOrUpdate(settings2)
                    } else {
                        settings.malsyncon =true
                        settings.malaccesstoken = obj.getString("access_token")
                        settings.malrefreshtoken = obj.getString("refresh_token")
                        settings.malaccesstime = currentTimeMillis().toInt()
                        Timber.e("mal at: " + settings.malaccesstoken)
                        Timber.e("mal rt: " + settings.malrefreshtoken)
                    }


                }
            }

            override fun onComplete() {

            }

            override fun onError(e: Throwable) {
                Timber.e("vapor 6 :" + e)
            }

        }}
}
