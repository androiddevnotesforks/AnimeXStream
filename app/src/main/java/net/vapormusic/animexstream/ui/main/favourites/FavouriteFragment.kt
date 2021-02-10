package net.vapormusic.animexstream.ui.main.favourites

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
import com.google.gson.reflect.TypeToken.getArray
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_favourite.view.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import net.vapormusic.animexstream.R
import net.vapormusic.animexstream.ui.main.animeinfo.AnimeInfoRepository
import net.vapormusic.animexstream.ui.main.favourites.epoxy.FavouriteController
import net.vapormusic.animexstream.utils.ItemOffsetDecoration
import net.vapormusic.animexstream.utils.Utils
import net.vapormusic.animexstream.utils.model.FavouriteModel
import net.vapormusic.animexstream.utils.model.SettingsModel
import net.vapormusic.animexstream.utils.realm.InitalizeRealm
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
      //  convertPreviousFavtoMAL()
        loadMALFavList()
        return rootView
    }

    fun convertPreviousFavtoMAL(){
        val realm: Realm = Realm.getInstance(InitalizeRealm.getConfig());
        val result = realm.where(FavouriteModel::class.java).equalTo("MAL_ID", "-1").findAll()
        if( result != null ){
            for (item in result){
//                CompositeDisposable().add(
//                    AnimeInfoRepository().MALAnimeID(item.animeName!!).subscribeWith(MALAnimeIDObserver())
//                )
            }
        }

    }

    fun loadMALFavList(){
        val realm: Realm = Realm.getInstance(InitalizeRealm.getConfig());
        realm.executeTransaction { realm1: Realm ->
            val  settings = realm1.where(SettingsModel::class.java).findFirst()
            if (settings != null  ) {
                if (settings.malsyncon){
                CompositeDisposable().add(
                        FavouriteRepository().fetchMALFavoriteList(settings.malaccesstoken).subscribeWith(helloobserver())
                )
            }}

        }

    }

    private fun helloobserver(): DisposableObserver<ResponseBody> {
        return object : DisposableObserver<ResponseBody>() {
            override fun onNext(t: ResponseBody) {
              //  Timber.e("mal 4 :" + t.string())
                val obj = JSONObject(t.string())
                val array = obj.getJSONArray("data")

                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i).getJSONObject("node")
                    AnimeInfoRepository().addMALToFavourite(obj.getString("id"),
                            FavouriteModel(
                                    ID = "MAL_NULL" + obj.getString("id"),
                                    categoryUrl = "MAL_NULL" + obj.getString("title"),
                                    animeName = obj.getString("title"),
                                    releasedDate = obj.getString("start_date").substring(0,4),
                                    MAL_ID = obj.getString("id"),
                                    imageUrl = obj.getJSONObject("main_picture").getString("medium")
                            )
                    )

                    //Iterate through the elements of the array i.
                    //Get thier value.
                    //Get the value for the first element and the value for the last element.
                }


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

