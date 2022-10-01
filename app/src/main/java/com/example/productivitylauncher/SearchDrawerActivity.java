package com.example.productivitylauncher;



import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class SearchDrawerActivity extends AppCompatActivity {
    View viewBG;
    ListView appList;
    ArrayAdapter<AppInfo> listViewAdapter;
    ArrayList <AppInfo> installedApps;
    SearchView searchBar;
    Activity activity;
    SoftInputAssist softInputAssist;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_drawer);

        softInputAssist = new SoftInputAssist(this);

        //BINDINGS
        appList=findViewById(android.R.id.list);
        searchBar =findViewById(R.id.searchView);
        viewBG =findViewById(R.id.viewBG);

        //POPULATE LISTVIEW
        installedApps = new ArrayList<>();
        listViewAdapter = new ArrayAdapter<>(this, R.layout.list_item, R.id.list_content,installedApps);
        appList.setAdapter(listViewAdapter);

        new AppListScraper(this, appList).execute();

        listViewAdapter.getFilter().filter("qwertyuiopasdfghjkl");
        searchBar.requestFocus();
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                try {
                    if (listViewAdapter.getItem(0) != null) {
                        searchBar.setQuery("", false);
                        listViewAdapter.getItem(0).launchApp(getApplicationContext());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                System.out.println(s.isEmpty());
                if(s.isEmpty())
                    listViewAdapter.getFilter().filter("qwertyuiopasdfghjkl");
                else
                    listViewAdapter.getFilter().filter(s);
                return false;
            }
        });
        appList.setOnItemClickListener((adapterView, view, i, l) -> {
            searchBar.setQuery("", false);
            listViewAdapter.getItem(i).launchApp(getApplicationContext());
        });
        appList.setOnItemLongClickListener((adapterView, view, i, l) -> { //TODO: support different languages
            AppInfo app = listViewAdapter.getItem(i);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            List<String> dialogChoices = new ArrayList<>();
            dialogChoices.add("Nascondi app");
            if (!app.isSystemApp){
                dialogChoices.add("Disinstalla");
            }
            CharSequence[] dialogChoicesSequence = dialogChoices.toArray(new CharSequence[dialogChoices.size()]);

            builder.setItems( dialogChoicesSequence, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(dialogChoices.get(i).equals("Disinstalla")){
                        startActivityForResult(app.getUninstallIntent(), 1);
                        listViewAdapter.remove(app);
                    }
                    else if(dialogChoices.get(i).equals("Nascondi app")){
                        hideApp(app);
                    }
                    Log.d("CLICK", (String) dialogChoicesSequence[i]);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        });
        viewBG.setOnTouchListener((view, motionEvent) -> {
            onBackPressed();
            return false;
        });
    }

    @Override
    protected  void onResume() {
        softInputAssist.onResume();
        super.onResume();
    }
    @Override
    protected void onPause() {
        softInputAssist.onPause();
        super.onPause();
    }
    @Override
    protected void onDestroy(){
        softInputAssist.onDestroy();
        super.onDestroy();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                listViewAdapter.notifyDataSetChanged();
                listViewAdapter.getFilter().filter(searchBar.getQuery());
            }
        }
    }
    private void hideApp(AppInfo app){
        SharedPreferences.Editor hidden_apps_editor = getSharedPreferences(getResources().getString(R.string.hidden_apps_shared_preferences), Context.MODE_PRIVATE).edit();
        hidden_apps_editor.putString((String) app.packageName, (String) app.label);
        hidden_apps_editor.apply();
        listViewAdapter.remove(app);
        listViewAdapter.notifyDataSetChanged();
        listViewAdapter.getFilter().filter(searchBar.getQuery());
    }
    public void onSettingsClick(View view){
        Intent settingsActivity = new Intent(this, SettingsActivity.class);
        startActivity(settingsActivity);
    }

}