package com.example.productivitylauncher;



import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import com.example.productivitylauncher.Widget.WidgetHost;
import com.example.productivitylauncher.Widget.WidgetInfo;
import com.example.productivitylauncher.Widget.WidgetView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class AppInfo {
    CharSequence label;
    CharSequence packageName;
    Boolean isSystemApp;

    public AppInfo() {
    }

    public AppInfo(CharSequence label, CharSequence packageName) {
        this.label = label;
        this.packageName = packageName;
    }

    @Override
    public String toString() {
        return (String) label;
    }
    public Boolean launchApp(Context context){
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName.toString());
        context.startActivity(launchIntent);
        return true;
    }
    public Intent getUninstallIntent(){
        int UNINSTALL_REQUEST_CODE = 1;
        Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
        intent.setData(Uri.parse("package:" + packageName));
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        return intent;
    }
}
