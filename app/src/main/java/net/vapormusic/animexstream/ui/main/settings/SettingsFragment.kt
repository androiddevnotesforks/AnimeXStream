package net.vapormusic.animexstream.ui.main.settings

import android.content.Intent
import android.content.res.Configuration
import android.media.audiofx.Virtualizer
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.axiel7.moelist.utils.PkceGenerator
import io.realm.Realm
import io.realm.Realm.getDefaultInstance
import kotlinx.android.synthetic.main.fragment_settings.view.*
import net.vapormusic.animexstream.MainActivity
import net.vapormusic.animexstream.Private
import net.vapormusic.animexstream.R
import net.vapormusic.animexstream.utils.constants.C
import net.vapormusic.animexstream.utils.model.SettingsModel
import net.vapormusic.animexstream.utils.realm.InitalizeRealm
import timber.log.Timber

class SettingsFragment: Fragment(), View.OnClickListener {

    private lateinit var rootView: View
    private lateinit var codeVerifier: String
    private lateinit var codeChallenge: String

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_settings, container, false)
        setupClickListeners()
        setupToggleText()

        return rootView
    }


    override fun onResume() {
        setupToggleText()
        super.onResume()
    }

    private fun setupToggleText() {


        val realm: Realm = Realm.getInstance(InitalizeRealm.getConfig());
      ///  realm.beginTransaction()


        realm.executeTransaction { realm1: Realm ->
            val  settings = realm1.where(SettingsModel::class.java).findFirst()
            if (settings == null ) {
                if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {
                    rootView.toggleMode.text = getString(R.string.toggle_to_night_mode)
                } else {
                    rootView.toggleMode.text = getString(R.string.toggle_to_light_mode)
                }
                val settings2 = realm.createObject(SettingsModel::class.java)
                realm1.insertOrUpdate(settings2)
                rootView.toggleMode2.text = getString(R.string.animepahe_on)
                // set the fields here
            //    Toast.makeText(context, "new realm", Toast.LENGTH_SHORT).show()
            } else {
                if (settings?.paheanimeon == true){
                 //   Toast.makeText(context, "old realm on", Toast.LENGTH_SHORT).show()
                    rootView.toggleMode2.text = getString(R.string.animepahe_off)
                }else{
                 //   Toast.makeText(context, "old realm off", Toast.LENGTH_SHORT).show()
                    rootView.toggleMode2.text = getString(R.string.animepahe_on)
                }
                if (settings?.nightmodeon == true){
                    //   Toast.makeText(context, "old realm on", Toast.LENGTH_SHORT).show()
                    rootView.toggleMode.text = getString(R.string.toggle_to_light_mode)
                }else{
                    //   Toast.makeText(context, "old realm off", Toast.LENGTH_SHORT).show()
                    rootView.toggleMode.text = getString(R.string.toggle_to_night_mode)
                }
                if (settings?.malsyncon == true){
                    //   Toast.makeText(context, "old realm on", Toast.LENGTH_SHORT).show()
                    rootView.toggleMode3.text = getString(R.string.mal_on)
                }else{
                    //   Toast.makeText(context, "old realm off", Toast.LENGTH_SHORT).show()
                    rootView.toggleMode3.text = getString(R.string.mal_off)
                }
                if (settings?.playercontrolson == true){
                    //   Toast.makeText(context, "old realm on", Toast.LENGTH_SHORT).show()
                    rootView.toggleMode4.text = getString(R.string.advancedcontrols_on)
                }else{
                    //   Toast.makeText(context, "old realm off", Toast.LENGTH_SHORT).show()
                    rootView.toggleMode4.text = getString(R.string.advancedcontrols_off)
                }

                if (settings?.googlecdn == true){
                    //   Toast.makeText(context, "old realm on", Toast.LENGTH_SHORT).show()
                    rootView.toggleMode5.text = getString(R.string.googlecdn_on)
                }else{
                    //   Toast.makeText(context, "old realm off", Toast.LENGTH_SHORT).show()
                    rootView.toggleMode5.text = getString(R.string.googlecdn_off)
                }
            }

        }
      ///      realm.commitTransaction()

    }

    private fun setupClickListeners() {
        rootView.back.setOnClickListener(this)
        rootView.toggleMode.setOnClickListener(this)
        rootView.toggleMode2.setOnClickListener(this)
        rootView.toggleMode3.setOnClickListener(this)
        rootView.toggleMode4.setOnClickListener(this)
        rootView.toggleMode5.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.back -> findNavController().popBackStack()
            R.id.toggleMode -> setupToggle()
            R.id.toggleMode2 -> togglePahe()
            R.id.toggleMode3 -> MALSync()
            R.id.toggleMode4 -> toggleAdvancedControls()
            R.id.toggleMode5 -> toggleGoogleCDN()
        }
    }

    private fun setupToggle() {
        val realm: Realm = Realm.getInstance(InitalizeRealm.getConfig());
        ///  realm.beginTransaction()


        realm.executeTransaction { realm1: Realm ->
            val  settings = realm1.where(SettingsModel::class.java).findFirst()
            if (settings == null ) {
                val settings2 = realm.createObject(SettingsModel::class.java)
                realm1.insertOrUpdate(settings2)
            } else {
                if (settings.nightmodeon == true) {
                    settings.nightmodeon = false
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//                rootView.toggleMode.text = getString(R.string.toggle_to_night_mode)
                    Toast.makeText(context, "Light Mode", Toast.LENGTH_SHORT).show()
                } else {
                    settings.nightmodeon = true
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//                rootView.toggleMode.text = getString(R.string.toggle_to_light_mode)
                    Toast.makeText(context, "Night Mode", Toast.LENGTH_SHORT).show()
                }


            }







        }


    }
    private fun togglePahe() {

       val realm: Realm = Realm.getInstance(InitalizeRealm.getConfig());



        try {
            realm.executeTransaction { realm1: Realm ->
                val  settings = realm1.where(SettingsModel::class.java).findFirst()
                if (settings == null) {

                    val settings2 = realm1.createObject(SettingsModel::class.java)
                    realm1.insertOrUpdate(settings2)
                    settings2.paheanimeon = true

                    rootView.toggleMode2.text = getString(R.string.animepahe_off)
                    // set the fields here
                //    Toast.makeText(context, "on", Toast.LENGTH_SHORT).show()
                } else {
                    if (settings.paheanimeon == true) {
                        settings.paheanimeon = false
                        rootView.toggleMode2.text = getString(R.string.animepahe_on)
                  //      Toast.makeText(context, "off", Toast.LENGTH_SHORT).show()
                    } else {
                        settings.paheanimeon = true
                        rootView.toggleMode2.text = getString(R.string.animepahe_off)
                 //       Toast.makeText(context, "on", Toast.LENGTH_SHORT).show()
                    }
                }}
            } catch (ignored: Exception) {
            }




    }
    private fun toggleAdvancedControls() {

        val realm: Realm = Realm.getInstance(InitalizeRealm.getConfig());



        try {
            realm.executeTransaction { realm1: Realm ->
                val  settings = realm1.where(SettingsModel::class.java).findFirst()
                if (settings == null) {

                    val settings2 = realm1.createObject(SettingsModel::class.java)
                    realm1.insertOrUpdate(settings2)
                    settings2.playercontrolson = true

                    rootView.toggleMode4.text = getString(R.string.advancedcontrols_on)
                    // set the fields here
                    //    Toast.makeText(context, "on", Toast.LENGTH_SHORT).show()
                } else {
                    if (settings.playercontrolson == true) {
                        settings.playercontrolson = false
                        rootView.toggleMode4.text = getString(R.string.advancedcontrols_off)
                        //      Toast.makeText(context, "off", Toast.LENGTH_SHORT).show()
                    } else {
                        settings.playercontrolson = true
                        rootView.toggleMode4.text = getString(R.string.advancedcontrols_on)
                        //       Toast.makeText(context, "on", Toast.LENGTH_SHORT).show()
                    }
                }}
        } catch (ignored: Exception) {
        }




    }
    fun toggleGoogleCDN(){
        val realm: Realm = Realm.getInstance(InitalizeRealm.getConfig());
        try {
            realm.executeTransaction { realm1: Realm ->
                val  settings = realm1.where(SettingsModel::class.java).findFirst()
                if (settings == null) {

                    val settings2 = realm1.createObject(SettingsModel::class.java)
                    realm1.insertOrUpdate(settings2)
                    settings2.googlecdn= false

                    rootView.toggleMode5.text = getString(R.string.googlecdn_off)
                    // set the fields here
                    //    Toast.makeText(context, "on", Toast.LENGTH_SHORT).show()
                } else {
                    if (settings.googlecdn == true) {
                        settings.googlecdn = false
                        rootView.toggleMode5.text = getString(R.string.googlecdn_off)
                        //      Toast.makeText(context, "off", Toast.LENGTH_SHORT).show()
                    } else {
                        settings.googlecdn = true
                        rootView.toggleMode5.text = getString(R.string.googlecdn_on)
                        //       Toast.makeText(context, "on", Toast.LENGTH_SHORT).show()
                    }
                }}
        } catch (ignored: Exception) {
        }
    }
    fun MALSync(){
        val realm: Realm = Realm.getInstance(InitalizeRealm.getConfig());

        try {
            realm.executeTransaction { realm1: Realm ->
                val  settings = realm1.where(SettingsModel::class.java).findFirst()
                if (settings == null) {

                    val settings2 = realm1.createObject(SettingsModel::class.java)
                    settings2.malsyncon = true
                    realm1.insertOrUpdate(settings2)
                    rootView.toggleMode3.text = getString(R.string.mal_on)
                    codeVerifier = PkceGenerator.generateVerifier(128)
                    codeChallenge = codeVerifier
                    val loginUrl = Uri.parse(C.MAL_OAUTH2_BASE + "authorize" + "?response_type=code"
                            + "&client_id=" + Private.MAL_CLIENT_ID + "&code_challenge=" + codeVerifier + "&state=" + codeVerifier + "&redirect_uri=" +C.AUTH_DEEP_LINK )
                    Timber.e("""mal :${codeVerifier}""")
                    val intent = Intent(Intent.ACTION_VIEW, loginUrl)
                    startActivity(intent)

                } else {
                    if (settings.malsyncon == true) {
                        rootView.toggleMode3.text = getString(R.string.mal_off)
                        settings.malsyncon = false
                        settings.malaccesstoken = ""
                        settings.malrefreshtoken = ""
                        settings.malrefreshtoken = ""
                    } else {
                        settings.malsyncon = true
                        rootView.toggleMode3.text = getString(R.string.mal_on)
                        codeVerifier = PkceGenerator.generateVerifier(128)
                        codeChallenge = codeVerifier
                        val loginUrl = Uri.parse(C.MAL_OAUTH2_BASE + "authorize" + "?response_type=code"
                                + "&client_id=" + Private.MAL_CLIENT_ID + "&code_challenge=" + codeVerifier + "&state=" + codeVerifier + "&redirect_uri=" +C.AUTH_DEEP_LINK )
                        Timber.e("""mal :${codeVerifier}""")
                        val intent = Intent(Intent.ACTION_VIEW, loginUrl)
                        startActivity(intent)
                    }
                }}
        } catch (ignored: Exception) {
        }


    }

    fun addDataInRealm(settings : SettingsModel ) {
        val realm: Realm = Realm.getInstance(InitalizeRealm.getConfig())

        try {
            realm.executeTransaction { realm1: Realm ->
                realm1.insertOrUpdate(settings)
            }
        } catch (ignored: Exception) {
        }
    }

}