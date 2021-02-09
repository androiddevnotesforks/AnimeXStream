package net.xblacky.animexstream.ui.main.favourites

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import net.xblacky.animexstream.utils.realm.InitalizeRealm
import net.xblacky.animexstream.utils.rertofit.NetworkInterface
import net.xblacky.animexstream.utils.rertofit.RetrofitHelper
import okhttp3.ResponseBody

class FavouriteRepository{
    private val retrofit = RetrofitHelper.getRetrofitInstance()
    private val realm = Realm.getInstance(InitalizeRealm.getConfig())

    fun fetchMALFavoriteList(access_token: String): Observable<ResponseBody> {
        val animeInfoService = retrofit.create(NetworkInterface.MALGetFavoriteList::class.java)
        return animeInfoService.get(access_token = access_token).subscribeOn(Schedulers.io()).observeOn(
            AndroidSchedulers.mainThread())
    }

    fun SetMALFavorite(access_token: String, mal_id: String): Observable<ResponseBody> {
        val animeEpisodeService = retrofit.create(NetworkInterface.MALSetFavorite::class.java)
        return animeEpisodeService.get(access_token = access_token, anime_id = mal_id).subscribeOn(
            Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    fun DeleteMALFavorite(access_token: String, mal_id: String): Observable<ResponseBody> {

        val animeEpisodeService = retrofit.create(NetworkInterface.MALRemoveFavorite::class.java)
        return animeEpisodeService.get(access_token = access_token, anime_id = mal_id)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

}