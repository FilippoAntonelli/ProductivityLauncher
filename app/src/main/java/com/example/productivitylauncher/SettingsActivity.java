package com.example.productivitylauncher;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {
    ListView settingsList;
    ArrayAdapter<String> settingsAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        settingsList = findViewById(R.id.settingsList);
        ArrayList<String> settings = new ArrayList<>();
        settings.add("App nascoste");
        settings.add("Infinity bar");
        settingsAdapter = new ArrayAdapter<String>(this, R.layout.list_item, R.id.list_content,settings);
        //settingsAdapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,settings);
        settingsList.setAdapter(settingsAdapter);
        settingsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (settingsList.getItemAtPosition(i).equals("App nascoste")){
                    Intent hiddenAppsActivity = new Intent(getApplicationContext(), HiddenAppsActivity.class);
                    startActivity(hiddenAppsActivity);
                }
                if (settingsList.getItemAtPosition(i).equals("Infinity bar")){
                    Intent infinityBarActivity = new Intent(getApplicationContext(), InfinityBarSettingsActivity.class);
                    startActivity(infinityBarActivity);
                }
            }
        });
    }



}