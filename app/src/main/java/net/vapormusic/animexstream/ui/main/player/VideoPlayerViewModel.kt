package net.vapormusic.animexstream.ui.main.player

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_settings.view.*
import net.vapormusic.animexstream.BuildConfig
import net.vapormusic.animexstream.utils.CommonViewModel
import net.vapormusic.animexstream.utils.constants.C
import net.vapormusic.animexstream.utils.model.Content
import net.vapormusic.animexstream.utils.model.SettingsModel
import net.vapormusic.animexstream.utils.parser.HtmlParser
import net.vapormusic.animexstream.utils.realm.InitalizeRealm
import okhttp3.ResponseBody
import timber.log.Timber


class VideoPlayerViewModel : CommonViewModel() {

    private val episodeRepository = EpisodeRepository()
    private var compositeDisposable = CompositeDisposable()
    private var _content = MutableLiveData<Content>(Content())
    var liveContent: LiveData<Content> = _content

    init {
        episodeRepository.clearContent()
    }

    fun fetchEpisodeMediaUrl(fetchFromDb: Boolean = true) {
        liveContent.value?.episodeUrl?.let {
            updateErrorModel(show = false, e = null, isListEmpty = false)
            updateLoading(loading = true)
            val result = episodeRepository.fetchContent(it)
            val animeName = _content.value?.animeName
            if (fetchFromDb) {
                result?.let {
                    result.animeName = animeName ?: ""
                    _content.value = result
                    updateLoading(false)
                } ?: kotlin.run {
                    fetchFromInternet(it)
                }
            } else {
                fetchFromInternet(it)
            }
        }
    }

    private fun fetchFromInternet(url: String) {
        compositeDisposable.add(
            episodeRepository.fetchEpisodeMediaUrl(url = url).subscribeWith(
                getEpisodeUrlObserver(
                    C.TYPE_MEDIA_URL
                )
            )
        )
    }

    fun updateEpisodeContent(content: Content) {
        _content.value = content
    }

    private fun getEpisodeUrlObserver(type: Int): DisposableObserver<ResponseBody> {
        return object : DisposableObserver<ResponseBody>() {
            override fun onComplete() {
                updateErrorModel(show = false, e = null, isListEmpty = false)
            }

            override fun onNext(response: ResponseBody) {
                var googlecdn_on = false
                val realm: Realm = Realm.getInstance(InitalizeRealm.getConfig());
                try {
                    realm.executeTransaction { realm1: Realm ->
                        val  settings = realm1.where(SettingsModel::class.java).findFirst()
                        if (settings == null) {
                            val settings2 = realm1.createObject(SettingsModel::class.java)
                            realm1.insertOrUpdate(settings2)
                        } else {
                            googlecdn_on = settings.googlecdn
                        }}
                } catch (ignored: Exception) {
                }
                if (type == C.TYPE_MEDIA_URL) {
                    val episodeInfo = HtmlParser.parseMediaUrl(response = response.string())

                    episodeInfo.vidcdnUrl?.let {
                        Timber.e("lolxd2 :"+response.string())
                        var url = episodeInfo.vidcdnUrl!!.replace("steraming.php", "loadserver.php")

                        if (googlecdn_on){url = episodeInfo.vidcdnUrl!!
                            compositeDisposable.add(
                                episodeRepository.fetchGoogleUrl(url).subscribeWith(
                                    getEpisodeUrlObserver(C.TYPE_M3U8_URL)
                                    //                              episodeRepository.fetchM3u8Url("https://gogo-stream.com/videos"+ _content.value?.episodeUrl).subscribeWith(
                                    //                               getEpisodeUrlObserver(C.TYPE_M3U8_PREPROCESS_URL)
                                )
                            )
                        }
                        else{
                        compositeDisposable.add(
                            episodeRepository.fetchM3u8Url(url).subscribeWith(
                                getEpisodeUrlObserver(C.TYPE_M3U8_URL)
                                //                              episodeRepository.fetchM3u8Url("https://gogo-stream.com/videos"+ _content.value?.episodeUrl).subscribeWith(
                                //                               getEpisodeUrlObserver(C.TYPE_M3U8_PREPROCESS_URL)
                            )
                        )}

                    }
                    val watchedEpisode =
                        episodeRepository.fetchWatchedDuration(_content.value?.episodeUrl.hashCode())
                    _content.value?.watchedDuration = watchedEpisode?.watchedDuration ?: 0
                    _content.value?.previousEpisodeUrl = episodeInfo.previousEpisodeUrl
                    _content.value?.nextEpisodeUrl = episodeInfo.nextEpisodeUrl
                } else if (type == C.TYPE_M3U8_URL) {
               // Timber.v("vapor xcx:"+ response.string())
                    val m3u8Url = HtmlParser.parseM3U8Url(response = response.string())
                    val content = _content.value
                    content?.url = m3u8Url
                    _content.value = content
                    saveContent(content!!)
                    updateLoading(false)
                }  else if (type == C.TYPE_M3U8_PREPROCESS_URL) {
                    val m3u8Urlx = HtmlParser.getGoGoHLS(response = response.string())
                    val content = _content.value
                    content?.referer = m3u8Urlx!!.replace("amp;", "").replace(
                        "streaming",
                        "loadserver"
                    )
                    _content.value = content
                    saveContent(content!!)
                    compositeDisposable.add(
                        episodeRepository.fetchM3u8Urlv2(
                            m3u8Urlx!!.replace("amp;", "").replace(
                                "streaming",
                                "loadserver"
                            ), m3u8Urlx!!.replace("amp;", "").replace("////", "//")
                        ).subscribeWith(
                            getEpisodeUrlObserver(C.TYPE_M3U8_URL)


                        )
                    )
                 //   Timber.e("vapor_x: "+m3u8Urlx)

                }

            }

            override fun onError(e: Throwable) {
                updateLoading(false)
                updateErrorModel(true, e, false)
            }

        }
    }

    fun saveContent(content: Content) {
        if (!content.url.isNullOrEmpty()) {
            episodeRepository.saveContent(content)
        }
    }


    override fun onCleared() {
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
        super.onCleared()
    }
}