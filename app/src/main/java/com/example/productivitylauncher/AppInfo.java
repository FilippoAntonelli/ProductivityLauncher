package com.example.productivitylauncher;



import android.content.Context;
import android.content.Intent;
import android.net.Uri;

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
