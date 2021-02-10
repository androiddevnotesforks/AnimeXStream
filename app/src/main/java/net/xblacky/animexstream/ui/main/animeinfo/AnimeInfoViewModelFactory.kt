package net.vapormusic.animexstream.ui.main.animeinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import retrofit2.http.Url
import java.lang.IllegalArgumentException

class AnimeInfoViewModelFactory(private val categoryUrl: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(AnimeInfoViewModel::class.java)){
            return AnimeInfoViewModel(categoryUrl = categoryUrl) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }

}