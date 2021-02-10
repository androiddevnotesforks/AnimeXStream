package net.vapormusic.animexstream.ui.main.search


import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import net.vapormusic.animexstream.utils.model.SuggestionModel
import net.vapormusic.animexstream.utils.rertofit.NetworkInterface
import net.vapormusic.animexstream.utils.rertofit.RetrofitHelper
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback

class SearchRepository {

    private val retrofit = RetrofitHelper.getRetrofitInstance()

    fun fetchSearchList(keyWord: String, pageNumber: Int): Observable<ResponseBody>{
        val searchService = retrofit.create(NetworkInterface.FetchSearchData::class.java)
        return searchService.get(keyWord,pageNumber).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    fun fetchSearchSuggestions(keyWord: String): Call<SuggestionModel> {
        val searchService = retrofit.create(NetworkInterface.FetchSearchSuggestionData::class.java)
        return searchService.get(keyWord)
    }

    fun fetchSearchGenre(genre: String, pageNumber: Int): Observable<ResponseBody> {
        val searchService = retrofit.create(NetworkInterface.FetchSearchViaGenreData::class.java)
        return searchService.get(genre,pageNumber).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }
}