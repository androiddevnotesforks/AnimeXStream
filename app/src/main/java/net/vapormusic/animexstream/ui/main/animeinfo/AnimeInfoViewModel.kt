package net.vapormusic.animexstream.ui.main.animeinfo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.realm.Realm
import net.vapormusic.animexstream.ui.main.favourites.FavouriteRepository
import net.vapormusic.animexstream.ui.main.search.SearchRepository
import net.vapormusic.animexstream.utils.CommonViewModel
import net.vapormusic.animexstream.utils.CommonViewModel2
import net.vapormusic.animexstream.utils.constants.C
import net.vapormusic.animexstream.utils.model.AnimeInfoModel
import net.vapormusic.animexstream.utils.model.EpisodeModel
import net.vapormusic.animexstream.utils.model.FavouriteModel
import net.vapormusic.animexstream.utils.model.SettingsModel
import net.vapormusic.animexstream.utils.parser.HtmlParser
import net.vapormusic.animexstream.utils.realm.InitalizeRealm
import okhttp3.ResponseBody
import org.json.JSONObject
import timber.log.Timber


class AnimeInfoViewModel(categoryUrl: String) : CommonViewModel() {

    private var categoryUrl: String? = null
    private var _animeInfoModel: MutableLiveData<AnimeInfoModel> = MutableLiveData()
    private var _episodeList: MutableLiveData<ArrayList<EpisodeModel>> = MutableLiveData()
    var episodeList: LiveData<ArrayList<EpisodeModel>> = _episodeList
    var animeInfoModel: LiveData<AnimeInfoModel> = _animeInfoModel
    private val animeInfoRepository = AnimeInfoRepository()
    private var compositeDisposable = CompositeDisposable()
    private var _isFavourite: MutableLiveData<Boolean> = MutableLiveData(false)
    var isFavourite: LiveData<Boolean> = _isFavourite
    var malnewcategoryUrl: String? = "MAL_NULL"
    var malnewid: String? = "MAL_NULL"

    init {
        this.categoryUrl = categoryUrl
        fetchAnimeInfo()
    }

    fun fetchAnimeInfo() {
        updateLoading(loading = true)
        updateErrorModel(false, null, false)

        categoryUrl?.let {
            if (categoryUrl!!.startsWith("MAL_NULL")){

              compositeDisposable.add(
                  SearchRepository().fetchSearchList(
                      categoryUrl!!.substring(8).replace(":",""),
                      1
                  ).subscribeWith(getSearchObserver()))
            } else {
            compositeDisposable.add(
                    animeInfoRepository.fetchAnimeInfo(it)
                            .subscribeWith(getAnimeInfoObserver(C.TYPE_ANIME_INFO))
            )
        }}
    }
    private fun getSearchObserver(): DisposableObserver<ResponseBody> {
        return object : DisposableObserver<ResponseBody>() {
            override fun onComplete() {
            }

            override fun onNext(response: ResponseBody) {
                val list =
                    HtmlParser.parseMovie(response = response.string(), typeValue = C.TYPE_DEFAULT)
                if(!list.isEmpty()){
                    malnewcategoryUrl = list[0].categoryUrl!!
                compositeDisposable.add(
                    animeInfoRepository.fetchAnimeInfo(list[0].categoryUrl!!)
                        .subscribeWith(getAnimeInfoObserver(C.TYPE_ANIME_INFO))
                )}
            }

            override fun onError(e: Throwable) {
            }

        }
    }
    private fun getAnimeInfoObserver(typeValue: Int): DisposableObserver<ResponseBody> {
        return object : DisposableObserver<ResponseBody>() {
            override fun onNext(response: ResponseBody) {
                if (typeValue == C.TYPE_ANIME_INFO) {
                    val animeInfoModel = HtmlParser.parseAnimeInfo(response = response.string())
//                    compositeDisposable.add(
//                            animeInfoRepository.MALAnimeID(animeInfoModel.animeTitle).subscribeWith(MALAnimeIDObserver())
//                    )
                    _animeInfoModel.value = animeInfoModel
                    compositeDisposable.add(
                            animeInfoRepository.fetchEpisodeList(
                                    id = animeInfoModel.id,
                                    endEpisode = animeInfoModel.endEpisode,
                                    alias = animeInfoModel.alias
                            )
                                    .subscribeWith(getAnimeInfoObserver(C.TYPE_EPISODE_LIST))
                    )
                    _isFavourite.value = animeInfoRepository.isFavourite(animeInfoModel.id)
                    if(categoryUrl!!.contains("MAL_NULL")){
                        val realm = Realm.getInstance(InitalizeRealm.getConfig())
                        val result = realm.where(FavouriteModel::class.java).equalTo("categoryUrl",categoryUrl).findFirst()
                        if(result!=null){
                            realm.executeTransaction {
                                val result2 = realm.createObject(FavouriteModel::class.java,animeInfoModel.id)
                                result2.categoryUrl = malnewcategoryUrl
                                result2.releasedDate = animeInfoModel.releasedTime
                                result2.imageUrl = animeInfoModel.imageUrl
                                result2.animeName = animeInfoModel.animeTitle
                                result2.MAL_ID = result.MAL_ID
                                result2.insertionTime = result.insertionTime
                                result.deleteFromRealm()
                                realm.insertOrUpdate(result2)
                            }
                        }
                    }



                } else if (typeValue == C.TYPE_EPISODE_LIST) {
                    _episodeList.value = HtmlParser.fetchEpisodeList(response = response.string())
                    updateLoading(loading = false)

                }
            }

            override fun onComplete() {

            }

            override fun onError(e: Throwable) {
                updateLoading(loading = false)
                if (typeValue == C.TYPE_ANIME_INFO) {
                    updateErrorModel(show = true, e = e, isListEmpty = false)
                } else {
                    updateErrorModel(show = true, e = e, isListEmpty = true)
                }

            }

        }
    }

    private fun MALAnimeIDObserver(): DisposableObserver<ResponseBody> {
        val model = animeInfoModel.value
        return object : DisposableObserver<ResponseBody>() {
            override fun onNext(t: ResponseBody) {
                val obj = JSONObject(t.string())
                val array = obj.getJSONArray("results")
                animeInfoModel.value?.MALAnimeID = array.getJSONObject(0).getString("mal_id")
                malnewid  = array.getJSONObject(0).getString("mal_id")
                animeInfoRepository.addMALToFavourite(malnewid!!,
                    FavouriteModel(
                        ID = model?.id,
                        categoryUrl = categoryUrl,
                        animeName = model?.animeTitle,
                        releasedDate = model?.releasedTime,
                        MAL_ID =  malnewid!! ,
                        imageUrl = model?.imageUrl
                    )
                )
                updateMALFavorite(1)
            }

            override fun onComplete() {

            }

            override fun onError(e: Throwable) {
                animeInfoRepository.addToFavourite(
                    FavouriteModel(
                        ID = model?.id,
                        categoryUrl = categoryUrl,
                        animeName = model?.animeTitle,
                        releasedDate = model?.releasedTime,
                        MAL_ID = "-1" ,
                        imageUrl = model?.imageUrl
                    )
                )
            }

        }}

    fun toggleFavourite() {
        if (_isFavourite.value!!) {
            animeInfoModel.value?.id?.let { animeInfoRepository.removeFromFavourite(it) }
            _isFavourite.value = false
             updateMALFavorite(0)

        } else {
            saveFavourite()

        }
    }

    fun updateMALFavorite(int: Int){
        val realm: Realm = Realm.getInstance(InitalizeRealm.getConfig());
        val  settings = realm.where(SettingsModel::class.java).findFirst()

        if(settings!= null && settings.malsyncon){
        if (int == 1){
            compositeDisposable.add(
                FavouriteRepository().SetMALFavorite(settings.malaccesstoken,malnewid!!).subscribeWith(getAnimeInfoObserver(999))
            )
        } else {
            compositeDisposable.add(
                FavouriteRepository().DeleteMALFavorite(settings.malaccesstoken,malnewid!!).subscribeWith(getAnimeInfoObserver(999))
            )
        }
        }

    }

    private fun saveFavourite() {
        val model = animeInfoModel.value
        compositeDisposable.add(
            animeInfoRepository.MALAnimeID(model!!.animeTitle)
                .subscribeWith(MALAnimeIDObserver())
        )

        _isFavourite.value = true
    }

//    fun setUrl(url: String) {
//        this.categoryUrl = url
//    }

    override fun onCleared() {
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
        if (isFavourite.value!!) {
            saveFavourite()
        }
        super.onCleared()
    }
}