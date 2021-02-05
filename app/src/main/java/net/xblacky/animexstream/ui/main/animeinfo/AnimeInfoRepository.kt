package net.xblacky.animexstream.ui.main.animeinfo

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import net.xblacky.animexstream.utils.model.FavouriteModel
import net.xblacky.animexstream.utils.model.PaheModel.ResolutionURLs.ResolutionURLs
import net.xblacky.animexstream.utils.model.PaheModel.SessionURLs.SessionsURLs
import net.xblacky.animexstream.utils.realm.InitalizeRealm
import net.xblacky.animexstream.utils.rertofit.NetworkInterface
import net.xblacky.animexstream.utils.rertofit.RetrofitHelper
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

    fun fetchMALAccessToken(authcode : String, codeverifier : String): Observable<ResponseBody> {
        val animeEpisodeService = retrofit.create(NetworkInterface.MALAccessToken::class.java)
        return animeEpisodeService.get(authcode = authcode,code_verifier = codeverifier)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    fun RefreshMALAccessToken(refresh_token : String): Observable<ResponseBody> {
        val animeEpisodeService = retrofit.create(NetworkInterface.MALRefreshAccessToken::class.java)
        return animeEpisodeService.get(refresh_token = refresh_token)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    fun NetworkInterface.MALUpdateTracking(accessToken: String, animeID : String, animeEp : String): Observable<ResponseBody> {
        val animeEpisodeService = retrofit.create(NetworkInterface.MALUpdateTracking::class.java)
        return animeEpisodeService.set(access_token = accessToken, anime_id = animeID , episode = animeEp)
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

    fun removeFromFavourite(id: String){
        realm.executeTransaction {
            it.where(FavouriteModel::class.java).equalTo("ID", id).findAll().deleteAllFromRealm()
        }

    }

}