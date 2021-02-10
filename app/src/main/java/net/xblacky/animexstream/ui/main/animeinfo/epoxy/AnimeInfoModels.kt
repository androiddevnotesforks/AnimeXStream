package net.vapormusic.animexstream.ui.main.animeinfo.epoxy

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import kotlinx.android.synthetic.main.recycler_episode_item.view.*
import net.vapormusic.animexstream.R



@EpoxyModelClass(layout = R.layout.recycler_episode_item)
abstract class EpisodeModel : EpoxyModelWithHolder<EpisodeModel.HomeHeaderHolder>(){

    @EpoxyAttribute
    lateinit var episodeModel: net.vapormusic.animexstream.utils.model.EpisodeModel
    @EpoxyAttribute
    lateinit var clickListener: View.OnClickListener
    @EpoxyAttribute
    var watchedProgress: Long = 0


    override fun bind(holder: HomeHeaderHolder) {
        super.bind(holder)

        holder.episodeText.text = episodeModel.episodeNumber
        holder.cardView.setOnClickListener(clickListener)
        holder.progressBar.progress = if(watchedProgress >90) 100  else if(watchedProgress in 1..10) 10 else watchedProgress.toInt()
        holder.cardView.setCardBackgroundColor(
            ResourcesCompat.getColor(
                holder.cardView.resources,
                R.color.episode_background,
                null
            )
        )
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            holder.progressBar.getProgressDrawable().setColorFilter(ResourcesCompat.getColor(holder.progress_color.resources, R.color.progress, null), PorterDuff.Mode.SRC_IN);
        }
    }

    class HomeHeaderHolder : EpoxyHolder(){
        lateinit var episodeText: TextView
        lateinit var cardView: CardView
        lateinit var progressBar: ProgressBar
        lateinit var progress_color : FrameLayout

        override fun bindView(itemView: View) {
            episodeText = itemView.episodeNumber
            cardView = itemView.cardView
            progressBar = itemView.watchedProgress
            progress_color = itemView.progress_color
        }
    }


}