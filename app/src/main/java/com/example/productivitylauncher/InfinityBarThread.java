package com.example.productivitylauncher;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.ProgressBar;

import java.util.Calendar;

public class InfinityBarThread extends Thread{
    private Context context;
    Boolean running;
    ProgressBar progressBar;
    Calendar calendar;
    public InfinityBarThread(Context context,ProgressBar progressBar) {
        this.context = context;
        this.progressBar = progressBar;
    }
    @Override
    public void run() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.infinity_bar_time_shared_preferences), Context.MODE_PRIVATE);
        int start_of_the_day = sharedPreferences.getInt("start_of_the_day",0);
        int end_of_the_day = sharedPreferences.getInt("end_of_the_day",11*60+59);
        running = true;
        while (running) {
            progressBar.setProgress(computePercent(start_of_the_day,end_of_the_day));
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public void interrupt(){
        running= false;
    }
    public int computeDayPercent(int min,int max){
        calendar= Calendar.getInstance();
        int goneMinutes = 60*calendar.get(Calendar.HOUR_OF_DAY)+calendar.get(Calendar.MINUTE);
        int totalMinutes = 1439;
        float multiplier = (float)(max-min)/totalMinutes;
        float percentage = multiplier * goneMinutes;
        return (int)percentage;
    }
    public int computePercent(int start_day, int end_day){
        calendar= Calendar.getInstance();
        int delta = 0;
        int goneMinutes = 60*calendar.get(Calendar.HOUR_OF_DAY)+calendar.get(Calendar.MINUTE)-delta;
        if(end_day<start_day){
            delta = end_day;
            start_day = start_day-delta;
            goneMinutes = goneMinutes-delta;
            end_day = 1439;
        }
        int totalMinutes = end_day-start_day;
        float multiplier = (float)1000/totalMinutes;
        if(goneMinutes<0){
            goneMinutes = 1439+goneMinutes;
            goneMinutes = 700;
        }

        int percent = (int) ((goneMinutes-start_day)*multiplier);

        percent = Math.min(percent, 1000);
        percent = Math.max(percent, 0);
        Log.d("progressBar","pid:"+Thread.currentThread().getName());
        return percent;
    }
    private String minutesToString(int minutes){
        int hours = minutes/60;
        int mins = minutes-(hours*60);
        String h = (hours<9) ? "0"+hours : ""+hours;
        String m = (mins<9) ? "0"+mins : ""+mins;
        return h+":"+m;
    }
}

