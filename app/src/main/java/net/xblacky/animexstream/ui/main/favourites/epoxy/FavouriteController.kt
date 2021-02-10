package net.vapormusic.animexstream.ui.main.favourites.epoxy

import com.airbnb.epoxy.TypedEpoxyController
import net.vapormusic.animexstream.utils.model.AnimeMetaModel
import net.vapormusic.animexstream.utils.model.FavouriteModel

class FavouriteController(private var adapterCallbacks: EpoxySearchAdapterCallbacks) :
    TypedEpoxyController<ArrayList<FavouriteModel>>() {
    override fun buildModels(data: ArrayList<FavouriteModel>?) {
        data?.let { arrayList ->
            arrayList.forEach {

                FavouriteModel_()
                    .id(it.ID)
                    .favouriteModel(it)
                    .spanSizeOverride { totalSpanCount, _, _ -> totalSpanCount / totalSpanCount }
                    .clickListener { model, _, _, _ ->
                        adapterCallbacks.animeTitleClick(model = model.favouriteModel())
                    }
                    .addTo(this)
            }
        }
    }

    interface EpoxySearchAdapterCallbacks {
        fun animeTitleClick(model: FavouriteModel)
    }

}