package br.com.luisfga.talkingz.app.utils;

import android.graphics.PorterDuff;
import android.view.MotionEvent;
import android.view.View;

public class ButtonEffectOnTouchListener implements View.OnTouchListener {

    private int color;

    public ButtonEffectOnTouchListener(int color) {
        this.color = color;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                v.getBackground().setColorFilter(color, PorterDuff.Mode.LIGHTEN);
                v.invalidate();
                break;
            }
            case MotionEvent.ACTION_UP: {
                v.getBackground().clearColorFilter();
                v.invalidate();
                break;
            }
        }
        return false;
    }
}
