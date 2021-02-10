package net.vapormusic.animexstream.ui.main.animeinfo

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import net.vapormusic.animexstream.utils.model.FavouriteModel
import net.vapormusic.animexstream.utils.model.PaheModel.ResolutionURLs.ResolutionURLs
import net.vapormusic.animexstream.utils.model.PaheModel.SessionURLs.SessionsURLs
import net.vapormusic.animexstream.utils.realm.InitalizeRealm
import net.vapormusic.animexstream.utils.rertofit.NetworkInterface
import net.vapormusic.animexstream.utils.rertofit.RetrofitHelper
import okhttp3.ResponseBody

class AnimeInfoRepository {

    private val retrofit = RetrofitHelper.getRetrofitInstance()
    private val realm = Realm.getInstance(InitalizeRealm.getConfig())

    fun fetchAnimeInfo(categoryUrl: String): Observable<ResponseBody> {
        val animeInfoService = retrofit.create(NetworkInterface.FetchAnimeInfo::class.java)
       return animeInfoService.get(categoryUrl).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    fun fetchEpisodeList(id: String, endEpisode: String, alias: String): Observable<ResponseBody>{
        val animeEpisodeService = retrofit.create(NetworkInterface.FetchEpisodeList::class.java)
        return animeEpisodeService.get(id= id, endEpisode = endEpisode, alias = alias).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    fun fetchPaheID(animename : String): Observable<ResponseBody> {

        val animeEpisodeService = retrofit.create(NetworkInterface.FetchPaheID::class.java)
        return animeEpisodeService.get(animename = animename)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }
    fun fetchPaheEpisodeSessionList(id : String): Observable<SessionsURLs> {
        val animeEpisodeService = retrofit.create(NetworkInterface.FetchPaheEpisodeSessionList::class.java)
        return animeEpisodeService.get(animeid = id)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }
    fun fetchPaheEpisodeResolutionURL(id : String , session : String ): Observable<ResolutionURLs> {
        val animeEpisodeService = retrofit.create(NetworkInterface.FetchPaheEpisodeResolutionURL::class.java)
        return animeEpisodeService.get(animeid2 = id, episodesession = session)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }
    fun fetchPaheEpisodeURL(lastkwikpath : String): Observable<ResponseBody> {
        val animeEpisodeService = retrofit.create(NetworkInterface.FetchPaheEpisodeURL::class.java)
        return animeEpisodeService.get(kwiklink = lastkwikpath)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    fun fetchMALAccessToken(code : String, code_verifier : String): Observable<ResponseBody> {
        val animeEpisodeService = retrofit.create(NetworkInterface.MALAccessToken::class.java)
        return animeEpisodeService.get(code = code,code_verifier = code_verifier)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }


    fun RefreshMALAccessToken(refresh_token : String): Observable<ResponseBody> {
        val animeEpisodeService = retrofit.create(NetworkInterface.MALRefreshAccessToken::class.java)
        return animeEpisodeService.get(refresh_token = refresh_token)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    fun MALAnimeID(query : String): Observable<ResponseBody> {
        val animeEpisodeService = retrofit.create(NetworkInterface.MALAnimeID::class.java)
        return animeEpisodeService.get(query = query)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }


    fun MALUpdateTracking(accessToken: String, animeID : String, animeEp : String): Observable<ResponseBody> {
        val animeEpisodeService = retrofit.create(NetworkInterface.MALUpdateTracking::class.java)
        return animeEpisodeService.set(access_token = "Bearer " + accessToken, anime_id = animeID , episode = animeEp)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    fun MALCurrentTracking(accessToken: String, animeID: String): Observable<ResponseBody> {
        val animeEpisodeService = retrofit.create(NetworkInterface.MALCurrentTracking::class.java)
        return animeEpisodeService.set(access_token = "Bearer " + accessToken, anime_id = animeID )
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    fun isFavourite(id: String): Boolean {
        val result = realm.where(FavouriteModel::class.java).equalTo("ID", id).findFirst()
        result?.let {
            return true
        } ?: return false
    }

    fun addToFavourite(favouriteModel: FavouriteModel){
        realm.executeTransaction {

            it.insertOrUpdate(favouriteModel)
        }
    }
    fun addMALToFavourite(id: String, favouriteModel: FavouriteModel){
        val result = realm.where(FavouriteModel::class.java).equalTo("MAL_ID", id).findFirst()
        if(result == null ){
        realm.executeTransaction {
            it.insertOrUpdate(favouriteModel)
        }}
    }

    fun removeFromFavourite(id: String){
        realm.executeTransaction {
            it.where(FavouriteModel::class.java).equalTo("ID", id).findAll().deleteAllFromRealm()
        }

    }

}