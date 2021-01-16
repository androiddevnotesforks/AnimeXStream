package net.xblacky.animexstream.ui.main.player

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.media.session.MediaSessionCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.source.hls.HlsDataSourceFactory
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.*
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.TrackSelectionDialogBuilder
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.HttpDataSource.InvalidResponseCodeException
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.realm.Realm
import kotlinx.android.synthetic.main.error_screen_video_player.view.*
import kotlinx.android.synthetic.main.exo_player_custom_controls.*
import kotlinx.android.synthetic.main.exo_player_custom_controls.view.*
import kotlinx.android.synthetic.main.fragment_video_player.*
import kotlinx.android.synthetic.main.fragment_video_player.view.*
import kotlinx.android.synthetic.main.fragment_video_player_placeholder.view.*
import net.xblacky.animexstream.R
import net.xblacky.animexstream.Tls12SocketFactory
import net.xblacky.animexstream.ui.main.animeinfo.AnimeInfoRepository
import net.xblacky.animexstream.utils.constants.C.Companion.ERROR_CODE_DEFAULT
import net.xblacky.animexstream.utils.constants.C.Companion.NO_INTERNET_CONNECTION
import net.xblacky.animexstream.utils.constants.C.Companion.RESPONSE_UNKNOWN
import net.xblacky.animexstream.utils.model.Content
import net.xblacky.animexstream.utils.model.PaheModel.ResolutionURLs.ResolutionURLs
import net.xblacky.animexstream.utils.model.PaheModel.SessionURLs.SessionsURLs
import net.xblacky.animexstream.utils.model.SettingsModel
import net.xblacky.animexstream.utils.realm.InitalizeRealm
import okhttp3.*
import org.apache.commons.lang3.StringUtils
import org.mozilla.javascript.Scriptable
import timber.log.Timber
import java.io.IOException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext

class VideoPlayerFragment : Fragment(), View.OnClickListener, Player.EventListener,
    AudioManager.OnAudioFocusChangeListener {


    companion object {
        private val TAG = VideoPlayerFragment::class.java.simpleName
    }

    private lateinit var videoUrl: String
    private lateinit var rootView: View
    private lateinit var player: SimpleExoPlayer
    private lateinit var trackSelectionFactory: TrackSelection.Factory
    private var trackSelector: DefaultTrackSelector? = null
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private var AnimePaheEnabled: Boolean ? = false
    private var mappedTrackInfo: MappingTrackSelector.MappedTrackInfo? = null
    private lateinit var audioManager: AudioManager
    private lateinit var mFocusRequest: AudioFocusRequest
    private lateinit var content: Content
    private val DEFAULT_MEDIA_VOLUME = 1f
    private val DUCK_MEDIA_VOLUME = 0.2f
    private lateinit var handler: Handler
    private var isFullScreen = false
    private var isVideoPlaying: Boolean = false

    private val speeds = arrayOf(0.25f, 0.5f, 1f, 1.25f, 1.5f, 2f)
    private val showableSpeed = arrayOf("0.25x", "0.50x", "1x", "1.25x", "1.50x", "2x")
    private var checkedItem = 2
    private var selectedSpeed = 2
    private var episodenum = "1" ;
    private var pahejs = "" ;

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        rootView = inflater.inflate(R.layout.fragment_video_player, container, false)
        setClickListeners()
        initializeAudioManager()
        initializePlayer()
        checkAnimePaheEnabled()
        retainInstance = true
        return rootView
    }

    override fun onStart() {
        super.onStart()
        registerMediaSession()
    }

    override fun onDestroy() {
        player.release()
        if (::handler.isInitialized) {
            handler.removeCallbacksAndMessages(null)
        }
        super.onDestroy()
    }

    private fun initializePlayer() {
        rootView.exoPlayerFrameLayout.setAspectRatio(16f / 9f)
        trackSelectionFactory = AdaptiveTrackSelection.Factory()
        trackSelector = DefaultTrackSelector(trackSelectionFactory)
        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector)

        val audioAttributes: AudioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MOVIE)
            .build()

        player.playWhenReady = true
        player.audioAttributes = audioAttributes
        player.addListener(this)
        player.seekParameters = SeekParameters.CLOSEST_SYNC
        rootView.exoPlayerView.player = player


    }

    private fun setClickListeners() {
        rootView.exo_full_Screen.setOnClickListener(this)
        rootView.exo_track_selection_view.setOnClickListener(this)
        rootView.exo_speed_selection_view.setOnClickListener(this)
        rootView.errorButton.setOnClickListener(this)
        rootView.back.setOnClickListener(this)
        rootView.nextEpisode.setOnClickListener(this)
        rootView.previousEpisode.setOnClickListener(this)
    }

    private fun buildMediaSource(uri: Uri): MediaSource {


        val lastPath = uri.lastPathSegment
        val defaultDataSourceFactory = DefaultHttpDataSourceFactory("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.122 Safari/537.36")

        if(lastPath!!.contains("m3u8")){
            return HlsMediaSource.Factory(
                HlsDataSourceFactory {
                    val defaultclient = OkHttpClient.Builder()
                        .retryOnConnectionFailure(true)
                        .readTimeout(15, TimeUnit.SECONDS)
                        .connectTimeout(15, TimeUnit.SECONDS)
                    val dataSource: OkHttpDataSource =
                        OkHttpDataSource(
                            enableTls12OnPreLollipop(defaultclient)!!.build(),
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.122 Safari/537.36"
                        )
                    if (uri.toString().contains("uwu.m3u8")) {
                        dataSource.setRequestProperty("Referer", "https://kwik.cx/")
                    } else {
                        dataSource.setRequestProperty("Referer", content?.referer)
                        Timber.e(content?.referer)
                    }
                    dataSource
                })
                .setAllowChunklessPreparation(true)
                .createMediaSource(uri)
        }else{
//            val dashChunkSourceFactory = DefaultDashChunkSource.Factory(defaultDataSourceFactory)
            return ExtractorMediaSource.Factory(defaultDataSourceFactory)
                .createMediaSource(uri)
        }

    }

    fun updateContent(content: Content) {
        Timber.e("Content Updated uRL: ${content.url}")
        this.content = content
        episodeName.text = content.episodeName
        exoPlayerView.videoSurfaceView.visibility =View.GONE

        this.content.nextEpisodeUrl?.let {
            nextEpisode.visibility = View.VISIBLE
        } ?: kotlin.run {
            nextEpisode.visibility = View.GONE
        }
        this.content.previousEpisodeUrl?.let {
            previousEpisode.visibility = View.VISIBLE
        } ?: kotlin.run {
            previousEpisode.visibility = View.GONE
        }
        this.episodenum = StringUtils.substringAfterLast(content.episodeUrl, "-")

        if(AnimePaheEnabled == true){
            getPaheId(content.animeName)}
        else{
            if(!content.url.isNullOrEmpty()){
                updateVideoUrl(URLDecoder.decode(content.url, StandardCharsets.UTF_8.name()))
            }else{
                showErrorLayout(
                    show = true,
                    errorCode = RESPONSE_UNKNOWN,
                    errorMsgId = R.string.server_error
                )
            }}

    }

    private fun getPaheId(animename: String) {
       // val temp = "tonikaku"
        Timber.e("vapors2 :" + animename)
        val animeInfoRepository = AnimeInfoRepository()
        CompositeDisposable().add(
            animeInfoRepository.fetchPaheID(animename)
                .subscribeWith(getPaheIdObserver())
        )

    }

    private fun updateVideoUrl(videoUrl: String) {
        this.videoUrl = videoUrl
        loadVideo(seekTo = content.watchedDuration)
    }

    private fun loadVideo(seekTo: Long? = 0, playWhenReady: Boolean = true) {
        showLoading(true)
        showErrorLayout(false, 0, 0)
        val mediaSource = buildMediaSource(Uri.parse(videoUrl))
        seekTo?.let {
            player.seekTo(it)
        }
        player.prepare(mediaSource, false, false)
        player.playWhenReady = playWhenReady
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.exo_track_selection_view -> {
                showDialog()
            }
            R.id.exo_speed_selection_view -> {
                showDialogForSpeedSelection()
            }
            R.id.exo_full_Screen -> {
                toggleFullView()
            }
            R.id.errorButton -> {
                refreshData()
            }
            R.id.back -> {
                (activity as VideoPlayerActivity).enterPipModeOrExit()
            }
            R.id.nextEpisode -> {
                playNextEpisode()
            }
            R.id.previousEpisode -> {
                playPreviousEpisode()
            }
        }
    }


    private fun toggleFullView() {
        if (isFullScreen) {
            exoPlayerFrameLayout.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            exoPlayerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            isFullScreen = false
            context?.let {
                exo_full_Screen.setImageDrawable(
                    ContextCompat.getDrawable(
                        it,
                        R.drawable.exo_controls_fullscreen_enter
                    )
                )
            }

        } else {
            exoPlayerFrameLayout.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            exoPlayerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            isFullScreen = true
            context?.let {
                exo_full_Screen.setImageDrawable(
                    ContextCompat.getDrawable(
                        it,
                        R.drawable.exo_controls_fullscreen_exit
                    )
                )
            }
        }
    }

    private fun refreshData() {
        if (::content.isInitialized && !content.url.isNullOrEmpty()) {
            loadVideo(player.currentPosition, true)
        } else {
            (activity as VideoPlayerActivity).refreshM3u8Url()
        }

    }


    private fun playNextEpisode() {
        playOrPausePlayer(playWhenReady = false, loseAudioFocus = false)
        saveWatchedDuration()
        showLoading(true)
        (activity as VideoPlayerListener).playNextEpisode()

    }

    private fun playPreviousEpisode() {
        playOrPausePlayer(playWhenReady = false, loseAudioFocus = false)
        showLoading(true)
        saveWatchedDuration()
        (activity as VideoPlayerListener).playPreviousEpisode()

    }

    fun showLoading(showLoading: Boolean) {
        if (::rootView.isInitialized) {
            if (showLoading) {
                rootView.videoPlayerLoading.visibility = View.VISIBLE
            } else {
                rootView.videoPlayerLoading.visibility = View.GONE
            }
        }
    }


    fun showErrorLayout(show: Boolean, errorMsgId: Int, errorCode: Int) {
        if (show) {
            rootView.errorLayout.visibility = View.VISIBLE
            context.let {
                rootView.errorText.text = getString(errorMsgId)
                when (errorCode) {
                    ERROR_CODE_DEFAULT -> {
                        rootView.errorImage.setImageDrawable(
                            ResourcesCompat.getDrawable(
                                resources,
                                R.drawable.ic_error,
                                null
                            )
                        )
                    }
                    RESPONSE_UNKNOWN -> {
                        rootView.errorImage.setImageDrawable(
                            ResourcesCompat.getDrawable(
                                resources,
                                R.drawable.ic_error,
                                null
                            )
                        )
                    }
                    NO_INTERNET_CONNECTION -> {
                        rootView.errorImage.setImageDrawable(
                            ResourcesCompat.getDrawable(
                                resources,
                                R.drawable.ic_internet,
                                null
                            )
                        )
                    }
                }
            }
        } else {
            rootView.errorLayout.visibility = View.GONE
        }
    }


    private fun showDialog() {
        mappedTrackInfo = trackSelector?.currentMappedTrackInfo

        try {
            TrackSelectionDialogBuilder(
                context,
                getString(R.string.video_quality),
                trackSelector,
                0

            ).build().show()
        } catch (ignored: java.lang.NullPointerException) {
        }
    }

    // set playback speed for exoplayer
    private fun setPlaybackSpeed(speed: Float) {
        val params: PlaybackParameters = PlaybackParameters(speed)
        player.playbackParameters = params
    }

    // set the speed, selectedItem and change the text
    private fun setSpeed(speed: Int) {
        selectedSpeed = speed
        checkedItem = speed
        exo_speed_selection_view.text = showableSpeed[speed]
    }

    // show dialog to select the speed.
    private fun showDialogForSpeedSelection() {
        val builder = AlertDialog.Builder(context!!)
        builder.apply {
            setTitle("Set your playback speed")
            setSingleChoiceItems(showableSpeed, checkedItem) {_, which ->
                when (which) {
                    0 -> setSpeed(0)
                    1 -> setSpeed(1)
                    2 -> setSpeed(2)
                    3 -> setSpeed(3)
                    4 -> setSpeed(4)
                    5 -> setSpeed(5)
                }
            }
            setPositiveButton("OK") {dialog, _ ->
                setPlaybackSpeed(speeds[selectedSpeed])
                dialog.dismiss()
            }
            setNegativeButton("Cancel") {dialog, _ ->
                dialog.dismiss()
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    override fun onTracksChanged(
        trackGroups: TrackGroupArray?,
        trackSelections: TrackSelectionArray?
    ) {
        try {

            val videoQuality = trackSelections!!.get(0)!!.selectedFormat!!.height.toString() + "p"
            //TODO Change controls for quality
            exo_track_selection_view.text = videoQuality
        } catch (ignore: NullPointerException) {
        }

    }

    override fun onPlayerError(error: ExoPlaybackException?) {
        isVideoPlaying = false
        if (error!!.type === ExoPlaybackException.TYPE_SOURCE) {
            val cause: IOException = error!!.sourceException
            if (cause is HttpDataSource.HttpDataSourceException) {
                // An HTTP error occurred.
                val httpError: HttpDataSource.HttpDataSourceException = cause
                // This is the request for which the error occurred.
                // querying the cause.
                if (httpError is InvalidResponseCodeException) {
                    val responseCode = httpError.responseCode
                        content.url = ""
                    showErrorLayout(
                        show = true,
                        errorMsgId = R.string.server_error,
                        errorCode = RESPONSE_UNKNOWN
                    )

                    Timber.e("Response Code $responseCode")
                    // message and headers.
                } else {
                    showErrorLayout(
                        show = true,
                        errorMsgId = R.string.no_internet,
                        errorCode = NO_INTERNET_CONNECTION
                    )
                }
            }
        }
    }
    private fun getPaheIdObserver(): DisposableObserver<ResponseBody> {
        return object : DisposableObserver<ResponseBody>() {
            override fun onNext(t: ResponseBody) {
                var x = t.string()
                //  Timber.e("vapor paheid :" + x)
                if(x.contains("id")){
                    val id = StringUtils.substringBetween(x, "id\":", ",\"")
                    Timber.e("vapor anime pahe:" + id)
                    //     Timber.e("vapor paheid :" + t.string())
                    val animeInfoRepository = AnimeInfoRepository()
                    CompositeDisposable().add(
                        animeInfoRepository.fetchPaheEpisodeSessionList(id)
                            .subscribeWith(getPaheSessionListObserver())
                    )}else{playVideo("")}

            }

            override fun onComplete() {

            }

            override fun onError(e: Throwable) {

                Timber.e("vapor 3 :")
                playVideo("")
            }

        }}
    private fun getPaheSessionListObserver(): DisposableObserver<SessionsURLs> {
        return object : DisposableObserver<SessionsURLs>() {
            override fun onNext(t: SessionsURLs) {
                var x = t;
                if(Integer.parseInt(x.total) > 0){
                    var count = 0;
                    for (episodes in x.data){
                        if(episodes.episode.equals(episodenum)){
                            Timber.e("vapor anime pahe session:" + episodes.session)
                            val animeInfoRepository = AnimeInfoRepository()
                            CompositeDisposable().add(
                                animeInfoRepository.fetchPaheEpisodeResolutionURL(
                                    episodes.anime_id,
                                    episodes.session
                                )
                                    .subscribeWith(getPaheEpisodeResolutionURLObserver())
                            )
                            break
                        }
                        count = count + 1
                        if(count.toString().equals(x.total)) playVideo("")
                    }

                }else{playVideo("")}

            }

            override fun onComplete() {

            }

            override fun onError(e: Throwable) {
                Timber.e("vapor 4 :" + e)
                playVideo("")
            }

        }}
    private fun getPaheEpisodeResolutionURLObserver(): DisposableObserver<ResolutionURLs> {
        return object : DisposableObserver<ResolutionURLs>() {
            override fun onNext(t: ResolutionURLs) {
                var x = t;
                Timber.e("vapor : " + x.data.toString())
                var defres = "360"
                if(x.data[0].get360() !=null){defres = "360"}
                else if(x.data[0].get480() !=null){defres = "480"}
                else if(x.data[0].get720() !=null){defres = "720"}
                else {defres = "1080"}
                Timber.e("vapor : " + defres)
                if(!defres.equals("1080")){
                    var y =""
                    if(defres.equals("360")){
                        y = x.data[0].get360().kwik;
                    } else if(defres.equals("480")){
                        y = x.data[0].get480().kwik;
                    } else if(defres.equals("720")){
                        y =  x.data[0].get720().kwik;
                    }
                    Timber.e("vapor kwik :" + y)
                    val animeInfoRepository = AnimeInfoRepository()
                    CompositeDisposable().add(
                        animeInfoRepository.fetchPaheEpisodeURL(
                            StringUtils.substringAfter(
                                y,
                                "https://kwik.cx/e/"
                            )
                        )
                            .subscribeWith(getPaheEpisodeURLObserver())
                    )

                    //   Timber.e("vapor paheid :" + t.string())

                } else {playVideo("")}

            }

            override fun onComplete() {

            }

            override fun onError(e: Throwable) {
                Timber.e("vapor 5 :" + e)
                playVideo("")
            }

        }}
    private fun getPaheEpisodeURLObserver(): DisposableObserver<ResponseBody> {
        return object : DisposableObserver<ResponseBody>() {
            override fun onNext(t: ResponseBody) {
                var x = t.string()
                //    Timber.e("vapor paheid2 :" + x)
                var jsencoded = "eval(function(p,a,c,k,e,d){" + StringUtils.substringBetween(
                    x,
                    ";eval(function(p,a,c,k,e,d){",
                    "</script>"
                )
                //  Timber.e("vapor paheid3 :" + unpackJs( jsencoded))
                playVideo(StringUtils.substringBetween(unpackJs(jsencoded), "source='", "';"))

            }

            override fun onComplete() {

            }

            override fun onError(e: Throwable) {
                Timber.e("vapor 6 :" + e)
                playVideo("")
            }

        }}

    private fun playVideo(source: String) {
        if(source.length > 0){
            content.url = source}

        if(!content.url.isNullOrEmpty()){
            Timber.e("vapor true link:"+ content.url);
            updateVideoUrl(URLDecoder.decode(content.url, StandardCharsets.UTF_8.name()))
        }else{
            showErrorLayout(
                show = true,
                errorCode = RESPONSE_UNKNOWN,
                errorMsgId = R.string.server_error
            )
        }
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        isVideoPlaying = playWhenReady
        if (playbackState == Player.STATE_READY  && playWhenReady) {
            rootView.exo_play.setImageResource(R.drawable.ic_media_play)
            rootView.exo_pause.setImageResource(R.drawable.ic_media_pause)
            playOrPausePlayer(true)

        }
        if (playbackState == Player.STATE_BUFFERING  && playWhenReady) {
            rootView.exo_play.setImageResource(0)
            rootView.exo_pause.setImageResource(0)
            showLoading(false)
        }
        if (playbackState == Player.STATE_READY) {
            exoPlayerView.videoSurfaceView.visibility = View.VISIBLE
        }
    }


    private fun initializeAudioManager() {
        audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val mAudioAttributes = android.media.AudioAttributes.Builder()
            .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MOVIE)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(mAudioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setWillPauseWhenDucked(true)
                .setOnAudioFocusChangeListener(this)
                .build()
        }}

    }


    private fun requestAudioFocus(): Boolean {

        val focusRequest: Int

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (::audioManager.isInitialized && ::mFocusRequest.isInitialized) {
                focusRequest = audioManager.requestAudioFocus(mFocusRequest)
                checkFocusRequest(focusRequest = focusRequest)
            } else {
                false
            }

        } else {
            focusRequest = audioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
            checkFocusRequest(focusRequest)
        }

    }

    private fun checkFocusRequest(focusRequest: Int): Boolean {
        return when (focusRequest) {
            AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> true
            else -> false
        }
    }

    private fun loseAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(mFocusRequest)
        } else {
            audioManager.abandonAudioFocus(this)
        }
    }

    fun playOrPausePlayer(playWhenReady: Boolean, loseAudioFocus: Boolean = true) {
        if (playWhenReady && requestAudioFocus()) {
            player.playWhenReady = true
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            player.playWhenReady = false
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (loseAudioFocus) {
                loseAudioFocus()
            }
        }
    }

    override fun onStop() {
        saveWatchedDuration()
        if (::content.isInitialized) {
            (activity as VideoPlayerListener).updateWatchedValue(content)
        }
        playOrPausePlayer(false)
        unRegisterMediaSession()
        super.onStop()
    }

    override fun onAudioFocusChange(focusChange: Int) {

        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                player.volume = DEFAULT_MEDIA_VOLUME
                playOrPausePlayer(true)
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                playOrPausePlayer(false, loseAudioFocus = false)
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                player.volume = DUCK_MEDIA_VOLUME
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                playOrPausePlayer(false)
            }
        }
    }

    private fun registerMediaSession() {
        mediaSession = MediaSessionCompat(context, TAG)
//        if (::content.isInitialized) {
//
////            val mediaMetadataCompat = MediaMetadataCompat.Builder()
////                    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, content.title)
////                    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, resources.getString(R.string.app_name))
//////                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeResource(resources, R.drawable.app_icon))
////                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, content.title)
////                    .build()
////
////            mediaSession.setMetadata(mediaMetadataCompat)
//        }
        mediaSession.isActive = true
        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlayer(player)
    }

    private fun unRegisterMediaSession() {
        mediaSession.release()
        mediaSessionConnector.setPlayer(null)
    }

     fun saveWatchedDuration() {
        if (::content.isInitialized) {
            val watchedDuration = player.currentPosition
            content.duration = player.duration
            content.watchedDuration = watchedDuration
            if (watchedDuration > 0) {
                (activity as VideoPlayerListener).updateWatchedValue(content)
            }
        }
    }

    fun isVideoPlaying(): Boolean{
        return isVideoPlaying
    }


    fun checkAnimePaheEnabled(){
        val realm: Realm = Realm.getInstance(InitalizeRealm.getConfig())


        //   val list: ArrayList<SettingsModel> = ArrayList()
        try {

                realm.executeTransaction { realm1: Realm ->
                    val results =
                            realm1.where(SettingsModel::class.java)?.findFirst()
                    if (results == null ) {
                        val settings2 = realm.createObject(SettingsModel::class.java)
                        realm1.insertOrUpdate(settings2)
                        AnimePaheEnabled = false
                        // set the fields here
                    } else {
                        AnimePaheEnabled = results.paheanimeon
                    }

                }
        } catch (ignored: java.lang.Exception) {
        }

    }

}

interface VideoPlayerListener {
    fun updateWatchedValue(content: Content)
    fun playPreviousEpisode()
    fun playNextEpisode()
}

private fun unpackJs(jsPacked: String): String? {
    val ct: org.mozilla.javascript.Context = org.mozilla.javascript.Context.enter()
    ct.setOptimizationLevel(-1) // https://stackoverflow.com/a/3859485/6482350
    val scope: Scriptable = ct.initStandardObjects()
    ct.evaluateString(scope, jsPacked.replace("eval", "var _jsUnPacked = "), null, 1, null)
    val jsUnpacked: Any = scope.get("_jsUnPacked", scope)

    return jsUnpacked.toString()
}

fun enableTls12OnPreLollipop(client: OkHttpClient.Builder): OkHttpClient.Builder? {
    if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT < 22) {
        try {
            val sc = SSLContext.getInstance("TLSv1.2")
            sc.init(null, null, null)
            client.sslSocketFactory(Tls12SocketFactory(sc.socketFactory))
            val cs = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .cipherSuites(
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
                    CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
                    CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_RC4_128_SHA,
                    CipherSuite.TLS_ECDHE_RSA_WITH_RC4_128_SHA,
                    CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA,
                    CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA,
                    CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA
                )
                .build()
            val specs: MutableList<ConnectionSpec> = ArrayList()
            specs.add(cs)
            specs.add(ConnectionSpec.COMPATIBLE_TLS)
            specs.add(ConnectionSpec.CLEARTEXT)
            client.connectionSpecs(specs)
        } catch (exc: Exception) {
            Timber.e("OkHttpTLSCompat:" +"Error while setting TLS 1.2 /n" + exc.printStackTrace())
        }
    }
    return client
}

