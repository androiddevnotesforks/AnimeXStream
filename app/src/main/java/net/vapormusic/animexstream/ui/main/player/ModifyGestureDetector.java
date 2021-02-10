package net.vapormusic.animexstream.ui.main.player;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;

import timber.log.Timber;

public class ModifyGestureDetector extends GestureDetector {

    MyGestureListener myGestureListener;

    public ModifyGestureDetector(Context context, OnGestureListener listener) {
        super(context, listener);
        init(listener);
    }

    void init(GestureDetector.OnGestureListener listener){
        if (listener instanceof MyGestureListener){
            myGestureListener = (MyGestureListener) listener;
        }
    }

    //u can write something more complex as long as u need
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        Timber.e("vapor_action: "+ev.getAction());
        if(ev.getAction() == MotionEvent.ACTION_UP
                && myGestureListener != null){
            myGestureListener.onUp(ev);
        }
        return super.onTouchEvent(ev);
    }

    public interface MyGestureListener{
        public void onUp(MotionEvent ev);
    }}
