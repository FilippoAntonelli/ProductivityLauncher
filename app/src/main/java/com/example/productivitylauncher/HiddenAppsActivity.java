package com.example.productivitylauncher;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HiddenAppsActivity extends AppCompatActivity {
ListView hiddenAppsListView;
ArrayAdapter<AppInfo> hiddenAppsAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hidden_apps);
        hiddenAppsListView = findViewById(R.id.hiddenAppsListView);
        SharedPreferences hiddenAppsSharedPreference = getSharedPreferences(getResources().getString(R.string.hidden_apps_shared_preferences), Context.MODE_PRIVATE);
        List<AppInfo> hiddenAppsList = new ArrayList();
        Iterator hiddenAppsIterator = hiddenAppsSharedPreference.getAll().entrySet().iterator();
        while (hiddenAppsIterator.hasNext()){
            Map.Entry pair = (Map.Entry)hiddenAppsIterator.next();
            hiddenAppsList.add(new AppInfo((CharSequence) pair.getValue(), (CharSequence) pair.getKey()));
        }
        hiddenAppsAdapter = new ArrayAdapter<AppInfo>(this, R.layout.list_item, R.id.list_content,hiddenAppsList);
        hiddenAppsListView.setAdapter(hiddenAppsAdapter);
        hiddenAppsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AppInfo app = hiddenAppsList.get(i);
                app.launchApp(getApplicationContext());
            }
        });
        hiddenAppsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                AppInfo app = hiddenAppsList.get(i);
                SharedPreferences.Editor editor = hiddenAppsSharedPreference.edit();
                editor.remove((String) app.packageName);
                editor.apply();
                hiddenAppsAdapter.remove(app);
                hiddenAppsAdapter.notifyDataSetChanged();
                return true;
            }
        });
    }
}