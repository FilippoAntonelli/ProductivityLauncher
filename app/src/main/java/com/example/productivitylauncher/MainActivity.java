package com.example.productivitylauncher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, GestureDetector.OnGestureListener {
    GestureDetector gestureDetector;
    ProgressBar progressBar;
    InfinityBarThread infinityBarThread;
    AppInfo defaultCamera;

    ListView shortcutAppsListView;
    static ArrayAdapter<AppInfo> shortcutAppsAdapter;
    static int availableSpace = 0;

    WidgetFragment fragment;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("CREATE","CREAte");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.view).setOnTouchListener(this);
        gestureDetector = new GestureDetector(this, this);
        progressBar = findViewById(R.id.progressBar);
        //loadPreferences();
        //--WIDGET STUFF--
        fragment = (WidgetFragment) getSupportFragmentManager().findFragmentById(R.id.widgetFragment);
        //--WIDGET STUFF--
        //checkWallpaperContrast();

        List<AppInfo> shortcutAppsList = new ArrayList();
        shortcutAppsListView = findViewById(R.id.shortcutAppsListView);
        SharedPreferences shortcutApps = getSharedPreferences(getResources().getString(R.string.shortcut_apps), Context.MODE_PRIVATE);
        Iterator hiddenAppsIterator = shortcutApps.getAll().entrySet().iterator();
        while (hiddenAppsIterator.hasNext()) {
            Map.Entry pair = (Map.Entry) hiddenAppsIterator.next();
            shortcutAppsList.add(new AppInfo((CharSequence) pair.getValue(), (CharSequence) pair.getKey()));
        }
        shortcutAppsAdapter = new ArrayAdapter<AppInfo>(this, R.layout.list_item, R.id.list_content, shortcutAppsList);
        shortcutAppsListView.setAdapter(shortcutAppsAdapter);
        shortcutAppsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AppInfo app = shortcutAppsList.get(i);
                app.launchApp(getApplicationContext());
            }
        });
        shortcutAppsListView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            AppInfo app = shortcutAppsList.get(i);
            SharedPreferences.Editor editor = shortcutApps.edit();
            editor.remove((String) app.packageName);
            editor.apply();
            shortcutAppsAdapter.remove(app);
            shortcutAppsAdapter.notifyDataSetChanged();
            return true;
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        infinityBarThread = new InfinityBarThread(this, progressBar);
        infinityBarThread.start();
        super.onResume();
    }

    @Override
    protected void onPause() {
        infinityBarThread.interrupt();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        infinityBarThread.interrupt();
        super.onDestroy();
    }

    public void onClockClick(View v) {
        Intent clock = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
        try {
            startActivity(clock);
        } catch (Exception e) {
            Snackbar.make(v, "Oups, an error occoured: " + e, Snackbar.LENGTH_SHORT).show();
        }
    }

    public void onCalendarClick(View v) {
        Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
        builder.appendPath("time");
        ContentUris.appendId(builder, Calendar.getInstance().getTimeInMillis());
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(builder.build());
        startActivity(intent);
    }

    public void onPhoneClick(View v) {
        Intent dial = new Intent(Intent.ACTION_DIAL);
        try {
            startActivity(dial);
        } catch (Exception e) {
            Snackbar.make(v, "Oups, an error occoured: " + e, Snackbar.LENGTH_SHORT).show();
        }
    }

    public void setDefaultCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        PackageManager packageManager = MainActivity.this.getPackageManager();
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(intent, 0);
        this.defaultCamera = new AppInfo("",listCam.get(0).activityInfo.packageName);
    }

    public void onCameraClick(View v) {
        if(defaultCamera != null){
            defaultCamera.launchApp(this);
        }else{
            setDefaultCamera();
            defaultCamera.launchApp(this);
        }
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        gestureDetector.onTouchEvent(motionEvent);
        Log.d("GESTURES", "TOUCH");
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        List<String> dialogChoices = new ArrayList<>();
        dialogChoices.add(getResources().getString(R.string.add_widget));
        dialogChoices.add(getResources().getString(R.string.change_wallpaper));
        dialogChoices.add(getResources().getString(R.string.open_settings));
        CharSequence[] dialogChoicesSequence = dialogChoices.toArray(new CharSequence[dialogChoices.size()]);
        builder.setItems(dialogChoicesSequence, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogChoices.get(i).equals(getResources().getString(R.string.add_widget))) {
                    fragment.selectWidget();
                } else if (dialogChoices.get(i).equals(getResources().getString(R.string.change_wallpaper))) {
                    pickImage();
                } else if(dialogChoices.get(i).equals(getResources().getString(R.string.open_settings))){
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void pickImage() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), R.integer.REQUEST_PICK_IMAGE);
    }

    private void setWallpaper(Uri imageUri) {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                InputStream is = getContentResolver().openInputStream(imageUri);
                wallpaperManager.setStream(is, null, true, WallpaperManager.FLAG_SYSTEM);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        Log.d("GESTURES", "FLING v:" + v + "  v1:" + v1);
        if (v1 < -500) {
            Intent searchDrawer = new Intent(this, SearchDrawerActivity.class);
            availableSpace = getAvailableSpace();
            startActivity(searchDrawer);
            //overridePendingTransition(R.anim.slide_in_bottom,R.anim.slide_out_bottom);
        }
        return false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == R.integer.REQUEST_PICK_IMAGE) {
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    setWallpaper(selectedImageUri);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public int getAvailableSpace() {

        View progressBar = findViewById(R.id.progressBar);
        View shortcutAppsList = findViewById(R.id.shortcutAppsListView);
        View widgetFragment = findViewById(R.id.widgetFragment);
        if (progressBar != null && shortcutAppsList != null && widgetFragment != null) {
            RectF oneRect = calculateRectOnScreen(progressBar);
            RectF otherRect = calculateRectOnScreen(shortcutAppsList);
            float space = Math.abs(oneRect.bottom - otherRect.top);
            if (space == 0) {
                return Integer.MAX_VALUE;
            }
            space = space - widgetFragment.getHeight();
            return (int) space;
        } else return -1;

    }

    public static RectF calculateRectOnScreen(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return new RectF(location[0], location[1], location[0] + view.getMeasuredWidth(), location[1] + view.getMeasuredHeight());
    }


}