package com.example.productivitylauncher;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

public class InfinityBarSettingsActivity extends AppCompatActivity {
    TextView startDayTextView;
    TextView endDayTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infinity_bar_settings);
        startDayTextView = findViewById(R.id.start_day_text_view);
        endDayTextView = findViewById(R.id.end_day_text_view);
        SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.infinity_bar_time_shared_preferences), Context.MODE_PRIVATE);
        initialize();
        int start_of_the_day = sharedPreferences.getInt("start_of_the_day",0);
        int end_of_the_day = sharedPreferences.getInt("end_of_the_day",11*60+59);
        startDayTextView.setText(minutesToString(start_of_the_day));
        endDayTextView.setText(minutesToString(end_of_the_day));


    }

    private void initialize(){
        SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.infinity_bar_time_shared_preferences), Context.MODE_PRIVATE);
        if(!sharedPreferences.contains("start_of_the_day")){
            sharedPreferences.edit().putInt("start_of_the_day",0);
        }
        if(!sharedPreferences.contains("end_of_the_day")){
            sharedPreferences.edit().putInt("end_of_the_day",11*60+59);
        }

    }

    public void setStartOfDay(View v){
        SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.infinity_bar_time_shared_preferences), Context.MODE_PRIVATE);
        int start_of_the_day = sharedPreferences.getInt("start_of_the_day",0);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                sharedPreferences.edit().putInt("start_of_the_day",getMinutes(timePicker.getHour(),timePicker.getMinute())).apply();
                startDayTextView.setText(minutesToString(getMinutes(timePicker.getHour(),timePicker.getMinute())));
            }
        },getHourMinutes(start_of_the_day).first,getHourMinutes(start_of_the_day).second,true);
        timePickerDialog.show();
    }
    public void setEndOfDay(View v){
        SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.infinity_bar_time_shared_preferences), Context.MODE_PRIVATE);
        int end_of_the_day = sharedPreferences.getInt("end_of_the_day",0);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                sharedPreferences.edit().putInt("end_of_the_day",getMinutes(timePicker.getHour(),timePicker.getMinute())).apply();
                endDayTextView.setText(minutesToString(getMinutes(timePicker.getHour(),timePicker.getMinute())));
            }
        },getHourMinutes(end_of_the_day).first,getHourMinutes(end_of_the_day).second,true);
        timePickerDialog.show();
    }
    private Pair<Integer, Integer> getHourMinutes(int minutes){
        int hours = minutes/60;
        int mins = minutes-(hours*60);
        return new Pair<>(hours,mins);
    }
    private String minutesToString(int minutes){
        int hours = minutes/60;
        int mins = minutes-(hours*60);
        String h = (hours<9) ? "0"+hours : ""+hours;
        String m = (mins<9) ? "0"+mins : ""+mins;
        return h+":"+m;
    }
    private int getMinutes(int hours, int minutes){
        return hours*60+minutes;
    }
}