package com.example.productivitylauncher;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.productivitylauncher.Widget.WidgetHost;
import com.example.productivitylauncher.Widget.WidgetInfo;
import com.example.productivitylauncher.Widget.WidgetView;
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

    ListView shortcutAppsListView;
    static ArrayAdapter<AppInfo> shortcutAppsAdapter;

    WidgetHost mAppWidgetHost;
    AppWidgetManager mAppWidgetManager;
    ViewGroup widgetLinearLayout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.view).setOnTouchListener(this);
        gestureDetector = new GestureDetector(this, this);
        progressBar = findViewById(R.id.progressBar);

        //--WIDGET STUFF--
        widgetLinearLayout = findViewById(R.id.widgetLinearLayout);
        mAppWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        mAppWidgetHost = new WidgetHost(getApplicationContext(), R.integer.APPWIDGET_HOST_ID);
        mAppWidgetHost.startListening();
        restoreState();
        //--WIDGET STUFF--


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
        mAppWidgetHost.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAppWidgetHost.stopListening();
        saveState();
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
        mAppWidgetHost.stopListening();
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

    public void onCameraClick(View v) {

        Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        Log.d("TEST", String.valueOf(intent.resolveActivity(getPackageManager())));
        try {
            startActivity(getPackageManager().getLaunchIntentForPackage(
                    intent.resolveActivity(getPackageManager()).getPackageName()));
        } catch (Exception e) {
            Snackbar.make(v, "Oups, an error occoured: " + e, Snackbar.LENGTH_SHORT).show();
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
        dialogChoices.add("Aggiungi un widget");
        dialogChoices.add("Cambia sfondo");
        CharSequence[] dialogChoicesSequence = dialogChoices.toArray(new CharSequence[dialogChoices.size()]);
        builder.setItems(dialogChoicesSequence, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogChoices.get(i).equals("Aggiungi un widget")) {
                    selectWidget();
                } else if (dialogChoices.get(i).equals("Cambia sfondo")) {
                    pickImage();
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
            Log.d("GESTURES", "OPEN SEARCH MENU");
            Intent searchDrawer = new Intent(this, SearchDrawerActivity.class);
            startActivity(searchDrawer);
            //overridePendingTransition(R.anim.slide_in_bottom,R.anim.slide_out_bottom);
        }
        return false;
    }

    private void saveState() {
        SharedPreferences widgetPrefs = getSharedPreferences(String.valueOf(R.string.widget_preferences), Context.MODE_PRIVATE);
        WidgetInfo[] widgetInfos = new WidgetInfo[widgetLinearLayout.getChildCount()];
        for (int i = 0; i < widgetLinearLayout.getChildCount(); i++) {
            WidgetView widget = (WidgetView) widgetLinearLayout.getChildAt(i);
            WidgetInfo info = new WidgetInfo(widget.getAppWidgetId(), widgetLinearLayout.getChildAt(i).getHeight());
            widgetInfos[i] = info;
        }
        //ADD implementation 'com.google.code.gson:gson:2.8.7' to build.gradle
        String widgetInfosJson = new Gson().toJson(widgetInfos);
        widgetPrefs.edit().putString(String.valueOf(R.string.widget_key), widgetInfosJson).apply();
    }

    private void restoreState() {
        SharedPreferences widgetPrefs = getSharedPreferences(String.valueOf(R.string.widget_preferences), Context.MODE_PRIVATE);
        String widgetInfosJson = widgetPrefs.getString(String.valueOf(R.string.widget_key), "");
        if (!widgetInfosJson.equals("")) {
            WidgetInfo[] widgetInfos = new Gson().fromJson(widgetInfosJson, WidgetInfo[].class);
            for (WidgetInfo widget : widgetInfos) {
                loadWidget(widget);
                System.out.println(widget.toString());
            }
        }
    }

    //Calls the intent for picking a widget
    void selectWidget() {
        int appWidgetId = this.mAppWidgetHost.allocateAppWidgetId();
        Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        startActivityForResult(pickIntent, R.integer.REQUEST_PICK_APPWIDGET);
    }

    private void configureWidget(Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        if (appWidgetInfo.configure != null) {
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(appWidgetInfo.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            startActivityForResult(intent, R.integer.REQUEST_CREATE_APPWIDGET);
        } else {
            createWidget(data);
        }
    }

    //adding it to you view
    public void createWidget(Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        loadWidget(new WidgetInfo(appWidgetId, -1));
        saveState();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == R.integer.REQUEST_PICK_APPWIDGET) {
                configureWidget(data);
            } else if (requestCode == R.integer.REQUEST_CREATE_APPWIDGET) {
                createWidget(data);
            } else if (requestCode == R.integer.REQUEST_PICK_IMAGE) {
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    setWallpaper(selectedImageUri);
                }
            }
        } else if (resultCode == RESULT_CANCELED && data != null) {
            int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (appWidgetId != -1) {
                mAppWidgetHost.deleteAppWidgetId(appWidgetId);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //create and load the widget
    public void loadWidget(WidgetInfo widget) {
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(widget.widgetId);
        WidgetView hostView = (WidgetView) mAppWidgetHost.createView(getApplicationContext(), widget.widgetId, appWidgetInfo);
        hostView.setAppWidget(widget.widgetId, appWidgetInfo);
        widget.height = (widget.height > 0) ? widget.height : Math.max(appWidgetInfo.minResizeHeight, appWidgetInfo.minHeight);
        setWidgetSize(hostView, widget.height);

        hostView.setLongClickable(true);
        hostView.setOnLongClickListener(view -> {
            final WidgetView widgetWithMenuCurrentlyDisplayed = (WidgetView) view;
            final ViewGroup parent = (ViewGroup) widgetWithMenuCurrentlyDisplayed.getParent();
            int currentIndex = parent.indexOfChild(widgetWithMenuCurrentlyDisplayed);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            List<String> dialogChoices = new ArrayList<>();
            if (canBeBigger(widgetWithMenuCurrentlyDisplayed)) {
                dialogChoices.add("Ingrandisci");
            }
            if (canBeSmaller(widgetWithMenuCurrentlyDisplayed)) {
                dialogChoices.add("Rimpicciolisci");
            }
            if (currentIndex > 0) {
                dialogChoices.add("Sposta su");
            }
            if (currentIndex < widgetLinearLayout.getChildCount() - 1) {
                dialogChoices.add("Sposta giu");
            }
            dialogChoices.add("Elimina");
            CharSequence[] dialogChoicesSequence = dialogChoices.toArray(new CharSequence[dialogChoices.size()]);

            builder.setItems(dialogChoicesSequence, (dialogInterface, i) -> {
                if (dialogChoices.get(i).equals("Ingrandisci")) {
                    makeWidgetBigger(widgetWithMenuCurrentlyDisplayed);
                } else if (dialogChoices.get(i).equals("Rimpicciolisci")) {
                    makeWidgetSmaller(widgetWithMenuCurrentlyDisplayed);
                } else if (dialogChoices.get(i).equals("Sposta su")) {
                    moveWidgetUp(widgetWithMenuCurrentlyDisplayed, parent);
                } else if (dialogChoices.get(i).equals("Sposta giu")) {
                    moveWidgetDown(widgetWithMenuCurrentlyDisplayed, parent);
                } else if (dialogChoices.get(i).equals("Elimina")) {
                    removeWidget(widgetWithMenuCurrentlyDisplayed);
                }
                Log.d("CLICK", (String) dialogChoicesSequence[i]);
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        });
        widgetLinearLayout.addView(hostView);
    }

    public void removeWidget(WidgetView hostView) {
        mAppWidgetHost.deleteAppWidgetId(hostView.getAppWidgetId());
        widgetLinearLayout.removeView(hostView);
        saveState();
    }


    private void setWidgetSize(WidgetView hostView, int height) {
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(hostView.getAppWidgetId());
        hostView.setMinimumHeight(height);
        hostView.setMinimumWidth(Math.min(appWidgetInfo.minWidth, appWidgetInfo.minResizeWidth));
        ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        hostView.setLayoutParams(params);
    }

    private boolean canBeBigger(WidgetView widgetView) {
        AppWidgetProviderInfo widgetInfo = mAppWidgetManager.getAppWidgetInfo(widgetView.getAppWidgetId());
        int height = widgetView.getHeight();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (height < 0 || widgetInfo == null || height + 50 >= widgetInfo.maxResizeHeight) {
                return false;
            }
        }
        return true;
    }

    private boolean canBeSmaller(WidgetView widgetView) {
        AppWidgetProviderInfo widgetInfo = mAppWidgetManager.getAppWidgetInfo(widgetView.getAppWidgetId());
        int height = widgetView.getHeight();
        if (height < 0 || widgetInfo == null || height - 50 < Math.min(widgetInfo.minHeight, widgetInfo.minResizeHeight)) {
            return false;
        }
        return true;
    }

    private void makeWidgetBigger(WidgetView widgetView) {
        if (canBeBigger(widgetView)) {
            setWidgetSize(widgetView, widgetView.getHeight() + 50);
        }
        saveState();
    }

    private void makeWidgetSmaller(WidgetView widgetView) {
        if (canBeSmaller(widgetView)) {
            setWidgetSize(widgetView, widgetView.getHeight() - 50);
        }
        saveState();
    }

    private void moveWidgetUp(WidgetView widgetView, ViewGroup group) {
        int currentIndex = group.indexOfChild(widgetView);
        if (currentIndex > 0) {
            group.removeViewAt(currentIndex);
            group.addView(widgetView, currentIndex - 1);
            saveState();
        }
    }

    private void moveWidgetDown(WidgetView widgetView, ViewGroup group) {
        int currentIndex = group.indexOfChild(widgetView);
        if (currentIndex < widgetLinearLayout.getChildCount() - 1) {
            group.removeViewAt(currentIndex);
            group.addView(widgetView, currentIndex + 1);
            saveState();
        }
    }


}