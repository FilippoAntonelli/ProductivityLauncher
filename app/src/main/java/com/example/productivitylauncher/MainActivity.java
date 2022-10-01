package com.example.productivitylauncher;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, GestureDetector.OnGestureListener {
    GestureDetector gestureDetector;
    ProgressBar progressBar;
    InfinityBarThread infinityBarThread;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.view).setOnTouchListener(this);
        gestureDetector = new GestureDetector(this,this);
        progressBar = findViewById(R.id.progressBar);


    }
    @Override
    protected void onResume() {
        infinityBarThread = new InfinityBarThread(this,progressBar);
        infinityBarThread.start();
        super.onResume();
    }
    @Override
    protected void onPause() {
        infinityBarThread.interrupt();
        super.onPause();
    }
    @Override
    protected void onDestroy(){
        infinityBarThread.interrupt();
        super.onDestroy();
    }


    public void onClockClick(View v)
    {
        Intent clock = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
        try{
            startActivity(clock);
        } catch (Exception e) {
            Snackbar.make(v,"Oups, an error occoured: "+e, Snackbar.LENGTH_SHORT).show();
        }
    }
    public void onCalendarClick(View v){
        Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
        builder.appendPath("time");
        ContentUris.appendId(builder, Calendar.getInstance().getTimeInMillis());
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(builder.build());
        startActivity(intent);
    }
    public void onPhoneClick(View v){
        Intent dial = new Intent(Intent.ACTION_DIAL);
        try{
            startActivity(dial);
        } catch (Exception e) {
            Snackbar.make(v,"Oups, an error occoured: "+e, Snackbar.LENGTH_SHORT).show();
        }
    }
    public void onCameraClick(View v){

        Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        Log.d("TEST", String.valueOf(intent.resolveActivity(getPackageManager())));
        try {
            startActivity(getPackageManager().getLaunchIntentForPackage(
                    intent.resolveActivity(getPackageManager()).getPackageName()));
        } catch (Exception e) {
            Snackbar.make(v,"Oups, an error occoured: "+e, Snackbar.LENGTH_SHORT).show();
        }
        /*
        Intent camera = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        try{
            startActivity(camera);
        } catch (Exception e) {
            Snackbar.make(v,"Oups, an error occoured: "+e, Snackbar.LENGTH_SHORT).show();
        }*/
    }
    @Override
    public void onBackPressed(){

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        gestureDetector.onTouchEvent(motionEvent);
        Log.d("GESTURES","TOUCH");
        return true;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        Log.d("GESTURES","FLING v:"+v+"  v1:"+v1);
        if (v1<-500) {
            Log.d("GESTURES", "OPEN SEARCH MENU");
            Intent searchDrawer = new Intent(this, SearchDrawerActivity.class);
            //Intent searchDrawer = new Intent(this, TestActivity.class);
            startActivity(searchDrawer);
        }
        if (v1 >500){

        }
        return false;
    }

}