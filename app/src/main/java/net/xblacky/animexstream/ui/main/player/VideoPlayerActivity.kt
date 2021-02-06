package net.xblacky.animexstream.ui.main.player

import android.app.AppOpsManager
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.realm.Realm
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_video_player.*
import kotlinx.android.synthetic.main.fragment_video_player.*
import net.xblacky.animexstream.MainActivity
import net.xblacky.animexstream.R
import net.xblacky.animexstream.ui.main.animeinfo.AnimeInfoRepository
import net.xblacky.animexstream.utils.constants.C
import net.xblacky.animexstream.utils.model.AnimeMetaModel
import net.xblacky.animexstream.utils.model.Content
import net.xblacky.animexstream.utils.model.SettingsModel
import net.xblacky.animexstream.utils.realm.InitalizeRealm
import net.xblacky.animexstream.utils.rertofit.NetworkInterface
import okhttp3.ResponseBody
import org.json.JSONObject
import timber.log.Timber
import java.lang.Exception

class VideoPlayerActivity : AppCompatActivity(), VideoPlayerListener {

    private lateinit var viewModel: VideoPlayerViewModel
    private var episodeNumber: String? = ""
    private var animeName: String? = ""
    private var MALAnimeID: String? = ""
    private var MALAccessToken: String? = ""


    private lateinit var content: Content
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
        viewModel = ViewModelProvider(this).get(VideoPlayerViewModel::class.java)
        getExtra(intent)
//        (playerFragment as VideoPlayerFragment).updateContent(Content(
//            url = url,
//            episodeNumber = "153"
//        ))
        setObserver()
        goFullScreen()

    }

    override fun onNewIntent(intent: Intent?) {
        (playerFragment as VideoPlayerFragment).playOrPausePlayer(
            playWhenReady = false,
            loseAudioFocus = false
        )
        (playerFragment as VideoPlayerFragment).saveWatchedDuration()
        getExtra(intent)
        super.onNewIntent(intent)

    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        enterPipMode()
    }


    override fun onResume() {
        super.onResume()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            goFullScreen()
        }
    }

    private fun getExtra(intent: Intent?) {
        val url = intent?.extras?.getString("episodeUrl")
        episodeNumber = intent?.extras?.getString("episodeNumber")
        animeName = intent?.extras?.getString("animeName")
        MALAnimeID = intent?.extras?.getString("MALID")
        viewModel.updateEpisodeContent(
            Content(
                animeName = animeName ?: "",
                episodeUrl = url,
                episodeName = animeName!! + " (" + episodeNumber!! + ")",
                url = ""
            )
        )
        viewModel.fetchEpisodeMediaUrl()
    }

    @Suppress("DEPRECATION")
    private fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
            && packageManager
                .hasSystemFeature(
                    PackageManager.FEATURE_PICTURE_IN_PICTURE
                )
            && hasPipPermission()
            && (playerFragment as VideoPlayerFragment).isVideoPlaying()
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val params = PictureInPictureParams.Builder()
                this.enterPictureInPictureMode(params.build())
            } else {
                this.enterPictureInPictureMode()
            }
        }
    }

    override fun onStop() {
        UpdateMALTracking()
        Timber.e("video player stopped")
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
            &&  hasPipPermission()
        ) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                finishAndRemoveTask()
            }
        }

        super.onStop()
    }

    override fun finish() {

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        ) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                finishAndRemoveTask()
            }
        }
        super.finish()
    }

    fun enterPipModeOrExit() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
            && packageManager
                .hasSystemFeature(
                    PackageManager.FEATURE_PICTURE_IN_PICTURE
                )
            && (playerFragment as VideoPlayerFragment).isVideoPlaying()
            && hasPipPermission()
        ) {
            try{

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val params = PictureInPictureParams.Builder()
                    this.enterPictureInPictureMode(params.build())
                } else {
                    this.enterPictureInPictureMode()
                }
            }catch (ex:Exception){
                Timber.e(ex.message)
            }

        } else {
            finish()

        }
    }

    private fun UpdateMALTracking(){
        val animeInfoRepository = AnimeInfoRepository()
        val realm: Realm = Realm.getInstance(InitalizeRealm.getConfig());
        realm.executeTransaction { realm1: Realm ->
            val  settings = realm1.where(SettingsModel::class.java).findFirst()
            Timber.e("oklad: " + settings?.malsyncon!!)
            if (settings != null && settings.malsyncon == true) {

              try{ MALAccessToken = settings.malaccesstoken!!
                  Timber.e("oklad")
                  CompositeDisposable().add(
                        animeInfoRepository.MALCurrentTracking(settings.malaccesstoken!!,MALAnimeID!!).subscribeWith(MALAnimeTrackingObserver(C.MAL_GET_TRACKING))
                )}catch (e:Exception){}
            }


        }

    }
    private fun MALAnimeTrackingObserver(type: Int): DisposableObserver<ResponseBody> {
        return object : DisposableObserver<ResponseBody>() {
            override fun onNext(t: ResponseBody) {
                if(type == C.MAL_GET_TRACKING){
                    val obj = JSONObject(t.string())
                    val status = obj.getJSONObject("my_list_status")
                    val mal_number =  status.getInt("num_episodes_watched")
                    Timber.e("ep on mal: " + mal_number )
                           try{
                               Timber.e("current ep: " + Integer.parseInt(episodeNumber!!.substring(3)))
                               if (mal_number <  Integer.parseInt(episodeNumber!!.substring(3))){

                                    CompositeDisposable().add(
                                           AnimeInfoRepository().MALUpdateTracking(MALAccessToken!!,MALAnimeID!!,episodeNumber!!.substring(3)).subscribeWith(MALAnimeTrackingObserver(C.MAL_SET_TRACKING)))

                               }
                           }catch (e : Exception){}
                }
                else if(type == C.MAL_SET_TRACKING){

                }
            }

            override fun onComplete() {

            }

            override fun onError(e: Throwable) {
            }

        }}
    private fun MALAnimeIDObserver(): DisposableObserver<ResponseBody> {
        return object : DisposableObserver<ResponseBody>() {
            override fun onNext(t: ResponseBody) {
                val obj = JSONObject(t.string())
                val array = obj.getJSONArray("results")
                val mal_id =  array.getJSONObject(0).getString("mal_id")
                Timber.e("mal id: " + mal_id)

            }

            override fun onComplete() {

            }

            override fun onError(e: Throwable) {
            }

        }}

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration?
    ) {
        exoPlayerView.useController = !isInPictureInPictureMode
    }

    private fun hasPipPermission(): Boolean {
        val appsOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                appsOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                    android.os.Process.myUid(),
                    packageName
                ) == AppOpsManager.MODE_ALLOWED
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                appsOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                    android.os.Process.myUid(),
                    packageName
                ) == AppOpsManager.MODE_ALLOWED
            }
            else -> {
                false
            }
        }
    }

    private fun setObserver() {
        viewModel.liveContent.observe(this, Observer {
            this.content = it
            it?.let {
                if (!it.url.isNullOrEmpty()) {
                    (playerFragment as VideoPlayerFragment).updateContent(it)
                }
            }
        })
        viewModel.isLoading.observe(this, Observer {
            (playerFragment as VideoPlayerFragment).showLoading(it.isLoading)
        })
        viewModel.errorModel.observe(this, Observer {
            (playerFragment as VideoPlayerFragment).showErrorLayout(
                it.show,
                it.errorMsgId,
                it.errorCode
            )
        })
    }

    override fun onBackPressed() {
        enterPipModeOrExit()
    }

    private fun goFullScreen() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    override fun updateWatchedValue(content: Content) {
        viewModel.saveContent(content)
    }

    override fun playNextEpisode() {
        viewModel.updateEpisodeContent(
            Content(
                episodeUrl = content.nextEpisodeUrl,
                episodeName = "$animeName (EP ${incrimentEpisodeNumber(content.episodeName!!)})",
                url = "",
                animeName = content.animeName
            )
        )
        viewModel.fetchEpisodeMediaUrl()

    }

    override fun playPreviousEpisode() {

        viewModel.updateEpisodeContent(
            Content(
                episodeUrl = content.previousEpisodeUrl,
                episodeName = "$animeName (EP ${decrimentEpisodeNumber(content.episodeName!!)})",
                url = "",
                animeName = content.animeName
            )
        )
        viewModel.fetchEpisodeMediaUrl()
    }

    private fun incrimentEpisodeNumber(episodeName: String): String {
        return try {
            val episodeString = episodeName.substring(
                episodeName.lastIndexOf(' ') + 1,
                episodeName.lastIndexOf(')')
            )
            var episodeNumber = Integer.parseInt(episodeString)
            episodeNumber++
            this.episodeNumber = "EP "+episodeNumber.toString()
            episodeNumber.toString()

        } catch (obe: ArrayIndexOutOfBoundsException) {
            ""
        }
    }

    private fun decrimentEpisodeNumber(episodeName: String): String {
        return try {
            val episodeString = episodeName.substring(
                episodeName.lastIndexOf(' ') + 1,
                episodeName.lastIndexOf(')')
            )
            var episodeNumber = Integer.parseInt(episodeString)
            episodeNumber--
            episodeNumber.toString()

        } catch (obe: ArrayIndexOutOfBoundsException) {
            ""
        }
    }

    fun refreshM3u8Url() {

        viewModel.fetchEpisodeMediaUrl(fetchFromDb = false)
    }



}