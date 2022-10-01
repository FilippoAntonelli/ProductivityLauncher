package com.example.productivitylauncher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.xml.sax.helpers.AttributesImpl;

import java.util.ArrayList;
import java.util.List;

public class AppListScraper extends AsyncTask<Void,Void,String> {
    ArrayAdapter<AppInfo> adapter;
    ListView appList;
    Context context;
    Activity activity;
    public AppListScraper(Activity activity,ListView appList) {
        this.appList = appList;
        this.context = activity.getApplicationContext();
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        adapter = (ArrayAdapter<AppInfo>)appList.getAdapter();
        super.onPreExecute();
    }
    @Override
    protected String doInBackground(Void... voids) {
        SharedPreferences hiddenApps = context.getSharedPreferences(context.getResources().getString(R.string.hidden_apps_shared_preferences).toString(),Context.MODE_PRIVATE);
        PackageManager pm = context.getPackageManager();
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> allApps = pm.queryIntentActivities(i, 0);
        for(ResolveInfo ri:allApps) {
            if(!ri.loadLabel(pm).toString().startsWith("com.")&&!hiddenApps.contains(ri.activityInfo.packageName)) {
                AppInfo app = new AppInfo();
                app.label = ri.loadLabel(pm);
                app.packageName = ri.activityInfo.packageName;
                app.isSystemApp = ((ri.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) ? Boolean.FALSE : Boolean.TRUE;

                Log.d("APPLIST", (String) app.label);

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.add(app);
                    }
                });
            }
        }
        return null;
    }
}
