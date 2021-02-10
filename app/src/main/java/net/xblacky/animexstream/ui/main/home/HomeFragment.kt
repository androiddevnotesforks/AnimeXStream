package net.vapormusic.animexstream.ui.main.home

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_home.view.*
import net.vapormusic.animexstream.BuildConfig
import net.vapormusic.animexstream.R
import net.vapormusic.animexstream.ui.main.home.epoxy.HomeController
import net.vapormusic.animexstream.utils.constants.C
import net.vapormusic.animexstream.utils.model.AnimeMetaModel
import timber.log.Timber
import java.lang.Exception

class HomeFragment : Fragment(), View.OnClickListener, HomeController.EpoxyAdapterCallbacks {


    private lateinit var rootView: View
    private lateinit var homeController: HomeController
    private var doubleClickLastTime = 0L
    private lateinit var viewModel: HomeViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_home, container, false)
        setAdapter()
        setClickListeners()
        checkUpdate()
        return rootView
    }

    fun checkUpdate(){
         AppUpdater(activity)
            //.setUpdateFrom(UpdateFrom.GITHUB)
            //.setGitHubUserAndRepo("javiersantos", "AppUpdater")
            .setUpdateFrom(UpdateFrom.GITHUB)
            .setGitHubUserAndRepo("vapormusic","AnimeXStream")
            .setDisplay(Display.SNACKBAR)
            .showAppUpdated(false)
            .start();
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        viewModelObserver()
    }

    private fun setAdapter() {
        homeController = HomeController(this)

        homeController.isDebugLoggingEnabled = true
        val homeRecyclerView = rootView.recyclerView
        homeRecyclerView.layoutManager = LinearLayoutManager(context)
        homeRecyclerView.adapter = homeController.adapter
    }

    private fun viewModelObserver() {
        viewModel.animeList.observe(viewLifecycleOwner, Observer {
            homeController.setData(it)
        })

        viewModel.updateModel.observe(viewLifecycleOwner, Observer {
            Timber.e(it.whatsNew)
            if (it.versionCode > BuildConfig.VERSION_CODE) {
                showDialog(it.whatsNew)
            }
        })
    }

    private fun setClickListeners() {
        rootView.header.setOnClickListener(this)
        rootView.settingsFragment.setOnClickListener(this)
        rootView.favorite.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.header -> {
                doubleClickLastTime = if (System.currentTimeMillis() - doubleClickLastTime < 300) {
                    rootView.recyclerView.smoothScrollToPosition(0)
                    0L
                } else {
                    System.currentTimeMillis()
                }

            }
            R.id.settingsFragment -> {
              try{  findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToSettingsFragment())} catch (e: Exception){}
            }
            R.id.favorite -> {
                val snackbar: Snackbar = Snackbar.make(rootView, "", Snackbar.LENGTH_SHORT)
                val snackbarLayout = snackbar.view
                val textView = snackbarLayout.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
                textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite, 0, 0, 0)
                snackbar.show()
            //    try{   findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToFavouriteFragment())} catch (e: Exception){}
          //      Snackbar.make(rootView, "\uD83D\uDE3B", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun recentSubDubEpisodeClick(model: AnimeMetaModel) {
       try{ findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToVideoPlayerActivity(
                episodeUrl = model.episodeUrl,
                animeName = model.title,
                episodeNumber = model.episodeNumber
            )
        )} catch (e: Exception){}
    }

    override fun animeTitleClick(model: AnimeMetaModel) {
        if(!model.categoryUrl.isNullOrBlank()){
            try{   findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToAnimeInfoFragment(
                    categoryUrl = model.categoryUrl
                )
            )} catch (e: Exception){}
        }

    }

    private fun showDialog(whatsNew: String) {
        AlertDialog.Builder(context!!).setTitle("New Update Available")
            .setMessage("What's New ! \n$whatsNew")
            .setCancelable(false)
            .setPositiveButton("Update") { _, _ ->
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(C.GIT_DOWNLOAD_URL)
                startActivity(i)
            }
            .setNegativeButton("Not now") { dialog, _ ->
                dialog.cancel()
            }.show()
    }

}