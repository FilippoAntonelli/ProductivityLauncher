package com.example.productivitylauncher.Widget;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.view.MotionEvent;

import androidx.core.view.MotionEventCompat;

public class WidgetView extends AppWidgetHostView {

    private OnLongClickListener longClick;
    private long down;

    public WidgetView(Context context) {
        super(context);
    }

    public WidgetView(Context context, int animationIn, int animationOut) {
        super(context, animationIn, animationOut);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        this.longClick = l;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch(MotionEventCompat.getActionMasked( ev ) ) {
            case MotionEvent.ACTION_DOWN:
                down = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                boolean upVal = System.currentTimeMillis() - down > 300L;
                if( upVal ) {
                    longClick.onLongClick( WidgetView.this );
                    return true;
                }
                break;
        }

        return false;
    }
}