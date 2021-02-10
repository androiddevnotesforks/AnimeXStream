package net.vapormusic.animexstream.ui.main.animeinfo.epoxy

import android.content.Intent
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.airbnb.epoxy.TypedEpoxyController
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import net.vapormusic.animexstream.ui.main.animeinfo.AnimeInfoRepository
import net.vapormusic.animexstream.ui.main.player.VideoPlayerActivity
import net.vapormusic.animexstream.utils.model.AnimeInfoModel
import net.vapormusic.animexstream.utils.model.EpisodeModel
import net.vapormusic.animexstream.utils.model.FavouriteModel
import net.vapormusic.animexstream.utils.rertofit.NetworkInterface
import okhttp3.ResponseBody
import org.json.JSONObject
import timber.log.Timber

class AnimeInfoController : TypedEpoxyController<ArrayList<EpisodeModel>>(){
    var animeName: String = ""
    var MALAnimeID: String? = ""
    private lateinit var isWatchedHelper: net.vapormusic.animexstream.utils.helper.WatchedEpisode
    private var _animeInfoModel: MutableLiveData<AnimeInfoModel> = MutableLiveData()
    override fun buildModels(data: ArrayList<EpisodeModel>?) {
        data?.forEach {
            EpisodeModel_()
                .id(it.episodeurl)
                .episodeModel(it)
                .clickListener { model, _, clickedView, _ ->
                    startVideoActivity(model.episodeModel(),clickedView)
                }
                .spanSizeOverride { totalSpanCount, _, _ ->
                    totalSpanCount/totalSpanCount
                }
                .watchedProgress(isWatchedHelper.getWatchedDuration(it.episodeurl.hashCode()))
                .addTo(this)
        }

    }

    fun setAnime(animeName: String){
        this.animeName = animeName
        isWatchedHelper = net.vapormusic.animexstream.utils.helper.WatchedEpisode(animeName)
    }

    fun setMALID( animeName: String){
        val animeInfoRepository = AnimeInfoRepository()
        CompositeDisposable().add(
                animeInfoRepository.MALAnimeID(animeName).subscribeWith(MALAnimeIDObserver())
        )
    }

    private fun MALAnimeIDObserver(): DisposableObserver<ResponseBody> {
        return object : DisposableObserver<ResponseBody>() {
            override fun onNext(t: ResponseBody) {
                val obj = JSONObject(t.string())
                val array = obj.getJSONArray("results")
                MALAnimeID= array.getJSONObject(0).getString("mal_id")

                Timber.e("mal id 2: " + MALAnimeID)

            }

            override fun onComplete() {

            }

            override fun onError(e: Throwable) {
            }

        }}

    fun isWatchedHelperUpdated():Boolean{
        return ::isWatchedHelper.isInitialized
    }

    private fun startVideoActivity(episodeModel: EpisodeModel, clickedView: View){
        val intent = Intent(clickedView.context, VideoPlayerActivity::class.java)
        intent.putExtra("episodeUrl",episodeModel.episodeurl)
        intent.putExtra("episodeNumber",episodeModel.episodeNumber)
        intent.putExtra("animeName",animeName)
        intent.putExtra("MALID",MALAnimeID)
        Timber.e("mal id 2: " + MALAnimeID)
//        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        clickedView.context.startActivity(intent)
    }

}