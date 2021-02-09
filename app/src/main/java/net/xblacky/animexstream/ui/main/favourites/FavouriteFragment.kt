package net.xblacky.animexstream.ui.main.favourites

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_favourite.view.*
import kotlinx.android.synthetic.main.fragment_favourite.view.toolbarText
import kotlinx.android.synthetic.main.fragment_favourite.view.topView
import kotlinx.android.synthetic.main.fragment_search.view.*
import net.xblacky.animexstream.R
import net.xblacky.animexstream.ui.main.favourites.epoxy.FavouriteController
import net.xblacky.animexstream.utils.ItemOffsetDecoration
import net.xblacky.animexstream.utils.Utils
import net.xblacky.animexstream.utils.constants.C
import net.xblacky.animexstream.utils.model.FavouriteModel
import net.xblacky.animexstream.utils.model.SettingsModel
import net.xblacky.animexstream.utils.realm.InitalizeRealm
import okhttp3.ResponseBody
import org.json.JSONObject
import timber.log.Timber

class FavouriteFragment: Fragment(), FavouriteController.EpoxySearchAdapterCallbacks,View.OnClickListener {
    private lateinit var rootView: View
    private lateinit var viewModel: FavouriteViewModel
    private val favouriteController by lazy {
        FavouriteController(this)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
         rootView = inflater.inflate(R.layout.fragment_favourite, container, false)
        setAdapters()
        transitionListener()
        setClickListeners()
        a()
        return rootView
    }

    fun a(){
        CompositeDisposable().add(

            FavouriteRepository().fetchMALFavoriteList("Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6IjQwNmE1MDhjNzhmMDM3MmQzZWZiNDIzNTIyNmY1N2IwZmY5NTQ4YmU4NDgxZGU3ODdhMjJlNDQ2MWE3MDVjZWEzMGFkNmQ3OTY5MDI1ODIzIn0.eyJhdWQiOiJkZjM2OGMwYjgyODZiNzM5ZWU3N2YwYjkwNTk2MDcwMCIsImp0aSI6IjQwNmE1MDhjNzhmMDM3MmQzZWZiNDIzNTIyNmY1N2IwZmY5NTQ4YmU4NDgxZGU3ODdhMjJlNDQ2MWE3MDVjZWEzMGFkNmQ3OTY5MDI1ODIzIiwiaWF0IjoxNjEyNzA3NTk0LCJuYmYiOjE2MTI3MDc1OTQsImV4cCI6MTYxNTEyNjc5NCwic3ViIjoiNTMyNjcwOSIsInNjb3BlcyI6W119.pve5D2w_PCOBP2pwM23JE3HqwQBNWxNnFINt2u90Ox1l70P6-KHScQQj0KXez8FyPo4-scenAj56uHEbf0fhSl7jxSpIaCDdBZd6ut9sDy4xFJeqZtFUd9Z54b4fb7lbsHnoaBzoeUnp36tV2WeIhUB5uQyCkrEl4Y_DEN3FCMSFcrSBHfI-FKBPQHxWkJoE6-qKvt5UM3mfIJ7nZrFRIRKDB04fnnEGPhyqFOA2kB_8MXv68vc9ctL2fyCljQrnyaVOlxUtmwZBM7P_54971WWgBLnMYRfD4Px6plSpShaZeKMkhwp5ctV-rYLzVZ0c5NVeLIUQ0VQ0QIHvxKXgAA").subscribeWith(helloobserver())
        )
    }

    private fun helloobserver(): DisposableObserver<ResponseBody> {
        return object : DisposableObserver<ResponseBody>() {
            override fun onNext(t: ResponseBody) {
                Timber.e("mal 4 :" + t.string())
            }

            override fun onComplete() {

            }

            override fun onError(e: Throwable) {
                Timber.e("vapor 6 :" + e)
            }

        }}




    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FavouriteViewModel::class.java)
        setObserver()

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            favouriteController.spanCount =5
            (rootView.searchRecyclerView.layoutManager as GridLayoutManager).spanCount = 5
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            favouriteController.spanCount = 3
            (rootView.searchRecyclerView.layoutManager as GridLayoutManager).spanCount = 3
        }

    }

    private fun setObserver(){
        viewModel.favouriteList.observe(viewLifecycleOwner, Observer {
            favouriteController.setData(it)
        })
    }


    private fun setAdapters(){
        favouriteController.spanCount = Utils.calculateNoOfColumns(context!!, 150f)
        rootView.recyclerView.apply {
            layoutManager = GridLayoutManager(context, Utils.calculateNoOfColumns(context!!, 150f))
            adapter = favouriteController.adapter
            (layoutManager as GridLayoutManager).spanSizeLookup = favouriteController.spanSizeLookup
        }
        rootView.recyclerView.addItemDecoration(ItemOffsetDecoration(context,R.dimen.episode_offset_left))

    }

    private fun getSpanCount(): Int {
        val orientation = resources.configuration.orientation
        return if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            5
        } else {
            3
        }
    }

    private fun transitionListener(){
        rootView.motionLayout.setTransitionListener(
            object: MotionLayout.TransitionListener{
                override fun onTransitionTrigger(
                    p0: MotionLayout?,
                    p1: Int,
                    p2: Boolean,
                    p3: Float
                ) {

                }

                override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
                    rootView.topView.cardElevation = 0F
                }

                override fun onTransitionChange(p0: MotionLayout?, startId: Int, endId: Int, progress: Float) {
                    if(startId == R.id.start){
                        rootView.topView.cardElevation = 20F * progress
                        rootView.toolbarText.alpha = progress
                    }
                    else{
                        rootView.topView.cardElevation = 10F * (1 - progress)
                        rootView.toolbarText.alpha = (1-progress)
                    }
                }

                override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
                }

            }
        )
    }
    private fun setClickListeners(){
        rootView.back.setOnClickListener(this)
    }

    override fun animeTitleClick(model: FavouriteModel) {
        findNavController().navigate(FavouriteFragmentDirections.actionFavouriteFragmentToAnimeInfoFragment(categoryUrl = model.categoryUrl))
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.back ->{
                findNavController().popBackStack()
            }
        }
    }

}

