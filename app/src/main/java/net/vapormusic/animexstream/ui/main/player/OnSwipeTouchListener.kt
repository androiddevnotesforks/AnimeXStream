package net.vapormusic.animexstream.ui.main.player;

import android.content.Context
import android.media.AudioManager
import android.util.DisplayMetrics
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import kotlinx.android.synthetic.main.fragment_video_player.*
import timber.log.Timber


open class OnSwipeTouchListener(c: Context?) : View.OnTouchListener {
        private var context: Context? = null
        init {

                        context = c


                /** you can do multi if else statements for multi activity classes  */
        }

        private val gestureDetector = GestureDetector(GestureListener())

        fun onTouch(event: MotionEvent): Boolean {

        return gestureDetector.onTouchEvent(event)
        }

        private  inner class GestureListener : SimpleOnGestureListener(),ModifyGestureDetector.MyGestureListener {

        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100


        override fun onDown(e: MotionEvent): Boolean {
        return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
             //   gestureDetector.setIsLongpressEnabled(true)
      //  onTouch(e)
        return true}

        override fun onUp(ev: MotionEvent?) {
                (context as VideoPlayerActivity).gesture_volume_layout.visibility = View.GONE
                (context as VideoPlayerActivity).gesture_bright_layout.visibility = View.GONE
                (context as VideoPlayerActivity).gesture_progress_layout.visibility = View.GONE
                }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                val metrics = DisplayMetrics()

                (context as VideoPlayerActivity).windowManager
                        .defaultDisplay
                        .getMetrics(metrics)

                val width = metrics.widthPixels;
                val height = metrics.heightPixels;
                Timber.e("vapor_res: " + width + "*" + height)
        val result = false
                val mOldX = e1.x
                val mOldY = e1.y
                var mode = -1
                val y = e2.rawY.toInt()
                        if (Math.abs(distanceX) >= Math.abs(distanceY)) {
                          //      (context as VideoPlayerActivity).gesture_progress_layout.setVisibility(View.VISIBLE)
                                (context as VideoPlayerActivity).gesture_volume_layout.setVisibility(View.GONE)
                                (context as VideoPlayerActivity).gesture_bright_layout.setVisibility(View.GONE)
                                mode = 0;
                        } else {
                                if (mOldX > width * 3.0 / 5) { //Volume
                                        (context as VideoPlayerActivity).gesture_volume_layout.setVisibility(View.VISIBLE)
                                        (context as VideoPlayerActivity).gesture_bright_layout.setVisibility(View.GONE)
                                        (context as VideoPlayerActivity).gesture_progress_layout.setVisibility(View.GONE)
                                        mode = 1;
                                        Timber.e("volume")
                                } else if (mOldX < width * 2.0 / 5) { // Brightness
                                        (context as VideoPlayerActivity).gesture_bright_layout.setVisibility(View.VISIBLE)
                                        (context as VideoPlayerActivity).gesture_volume_layout.setVisibility(View.GONE)
                                        (context as VideoPlayerActivity).gesture_progress_layout.setVisibility(View.GONE)
                                        mode = 2;
                                }
                        }
                if (mode == 0) {
                }

                // If the first scroll is to adjust the volume after each touch of the screen, then the subsequent scroll events will handle the volume adjustment until you leave the screen to perform the next operation
                else if (mode == 1) {

                        var audiomanager = (context as VideoPlayerActivity).getSystemService(Context.AUDIO_SERVICE)  as AudioManager
                        var currentVolume = audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC) // Get the current value

                       var maxVolume = audiomanager.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // Get the maximum volume of the system
                        currentVolume = audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC); // Get the current value
                        if (Math.abs(distanceY)> Math.abs(distanceX)) {// Vertical movement is greater than horizontal movement
                                if (distanceY >= DensityUtil.dip2px((context as VideoPlayerActivity), 1.0f)) {// Turn up the volume, pay attention to the coordinate system when the screen is horizontal, although the upper left corner is the origin, distanceY is positive when sliding up horizontally
                                        if (currentVolume <maxVolume) {// To avoid too fast adjustment, distanceY should be greater than a set value
                                                currentVolume++;

                                        }
                                        Timber.e("volume up")
                                     //   gesture_iv_player_volume.setImageResource(R.drawable.player_volume);
                                } else if (distanceY <= -DensityUtil.dip2px((context as VideoPlayerActivity), 1.0f)) {// Turn down the volume
                                        if (currentVolume > 0) {
                                                currentVolume--;
//                                                if (currentVolume == 0) {// Mute, set mute unique picture
//                                                        gesture_iv_player_volume.setImageResource(R.drawable.player_silence);
//                                                }
                                        }
                                        Timber.e("volume down")
                                }
                                var percentage = (currentVolume * 100) / maxVolume;
                                (context as VideoPlayerActivity).gesture_tv_volume_percentage.setText(percentage.toString() + "%");
                                audiomanager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
                        }
                }

                // If the first scroll is to adjust the brightness every time you touch the screen, then the subsequent scroll events will handle the brightness adjustment until you leave the screen to perform the next operation
                else if (mode == 2) {
//                        gesture_iv_player_bright.setImageResource(R.drawable.player_bright);

                             var mBrightness = (context as VideoPlayerActivity).window.getAttributes().screenBrightness;
                                if (mBrightness <= 0.00f)
                                        mBrightness = 0.50f;
                                if (mBrightness < 0.01f)
                                        mBrightness = 0.01f;

                        var lpa = (context as VideoPlayerActivity).window.getAttributes();
                        lpa.screenBrightness = mBrightness + (mOldY - y) / height;
                        if (lpa.screenBrightness > 1.0f)
                                lpa.screenBrightness = 1.0f;
                        else if (lpa.screenBrightness < 0.01f)
                                lpa.screenBrightness = 0.01f;
                        (context as VideoPlayerActivity).window.setAttributes(lpa);
                        (context as VideoPlayerActivity).gesture_tv_bright_percentage.setText((lpa.screenBrightness * 100).toInt().toString() + "%");
                }

                //firstScroll = false;// The first scroll execution is complete, modify the flag
                return true;

        }

//        try {
//        val diffY = e2.y - e1.y
//        val diffX = e2.x - e1.x
//        if (Math.abs(diffX) > Math.abs(diffY)) {
//        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
//        if (diffX > 0) {
//        onSwipeRight()
//        } else {
//        onSwipeLeft()
//        }
//        }
//        } else {
//        // onTouch(e);
//        }
//        } catch (exception: Exception) {
//        exception.printStackTrace()
//        }

    //    return result
        }


        override fun onTouch(v: View, event: MotionEvent): Boolean {
                Timber.e("vapor_action: " + event.getAction())
                if(event.getAction() == MotionEvent.ACTION_UP){
                (context as VideoPlayerActivity).gesture_volume_layout.visibility = View.GONE
                (context as VideoPlayerActivity).gesture_bright_layout.visibility = View.GONE
                (context as VideoPlayerActivity).gesture_progress_layout.visibility = View.GONE}
        return gestureDetector.onTouchEvent(event)
        }

        open fun onSwipeRight() {}

        open fun onSwipeLeft() {}

        open fun onSwipeTop() {}

        open fun onSwipeBottom() {}


}
internal object DensityUtil {
        fun dip2px(context: Context, dpValue: Float): Int {
                val scale = context.resources.displayMetrics.density
                return (dpValue * scale + 0.5f).toInt()
        }

        fun px2dip(context: Context, pxValue: Float): Int {
                val scale = context.resources.displayMetrics.density
                return (pxValue / scale + 0.5f).toInt()
        }
}

class MyGestureListener : SimpleOnGestureListener(), ModifyGestureDetector.MyGestureListener {
        override fun onUp(ev: MotionEvent) {
                //do what u want
        }
}
