package com.example.productivitylauncher;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.productivitylauncher.Widget.WidgetHost;
import com.example.productivitylauncher.Widget.WidgetInfo;
import com.example.productivitylauncher.Widget.WidgetView;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WidgetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WidgetFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    WidgetHost mAppWidgetHost;
    AppWidgetManager mAppWidgetManager;
    ViewGroup widgetLinearLayout;

    public WidgetFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WidgetFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WidgetFragment newInstance(String param1, String param2) {
        WidgetFragment fragment = new WidgetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //mParam1 = getArguments().getString(ARG_PARAM1);
            //mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_widget, container, false);

        widgetLinearLayout = view.findViewById(R.id.widgetLinearLayout);
        mAppWidgetManager = AppWidgetManager.getInstance(getActivity().getApplicationContext());
        mAppWidgetHost = new WidgetHost(getActivity().getApplicationContext(),R.integer.APPWIDGET_HOST_ID);
        mAppWidgetHost.startListening();
        restoreState();
        return view;
    }

    @Override
    public void onStart() {
        mAppWidgetHost.startListening();
        super.onStart();
    }
    @Override
    public void onStop() {
        //mAppWidgetHost.stopListening();
        super.onStop();
    }
    @Override
    public void onDestroy() {
        //mAppWidgetHost.stopListening();
        super.onDestroy();
    }

    private void saveState() {
        SharedPreferences widgetPrefs = getActivity().getSharedPreferences(String.valueOf(R.string.widget_preferences), Context.MODE_PRIVATE);
        WidgetInfo[] widgetInfos = new WidgetInfo[widgetLinearLayout.getChildCount()];
        for (int i = 0; i < widgetLinearLayout.getChildCount(); i++){
            WidgetView widget = (WidgetView) widgetLinearLayout.getChildAt(i);
            WidgetInfo info = new WidgetInfo(widget.getAppWidgetId(), widgetLinearLayout.getChildAt(i).getHeight());
            widgetInfos[i] = info;
        }
        String widgetInfosJson = new Gson().toJson(widgetInfos);
        widgetPrefs.edit().putString(String.valueOf(R.string.widget_key), widgetInfosJson).apply();
    }

    private void restoreState(){
        SharedPreferences widgetPrefs = getActivity().getSharedPreferences(String.valueOf(R.string.widget_preferences),Context.MODE_PRIVATE);
        String widgetInfosJson = widgetPrefs.getString(String.valueOf(R.string.widget_key),"");
        if (!widgetInfosJson.equals("")){
            WidgetInfo[] widgetInfos = new Gson().fromJson(widgetInfosJson,WidgetInfo[].class);
            for (WidgetInfo widget : widgetInfos){
                loadWidget(widget);
                System.out.println(widget.toString());
            }
        }
    }

    //Calls the intent for picking a widget
    public void removeWidget(WidgetView hostView) {
        mAppWidgetHost.deleteAppWidgetId(hostView.getAppWidgetId());
        widgetLinearLayout.removeView(hostView);
        saveState();
    }
    void selectWidget() {
        int appWidgetId = this.mAppWidgetHost.allocateAppWidgetId();
        Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        startActivityForResult(pickIntent,R.integer.REQUEST_PICK_APPWIDGET);
    }
    private void configureWidget(Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        if (appWidgetInfo.configure != null) {
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(appWidgetInfo.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            startActivityForResult(intent,R.integer.REQUEST_CREATE_APPWIDGET);
        } else {
            createWidget(data);
        }
    }
    //adding it to you view
    public void createWidget(Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        loadWidget(new WidgetInfo(appWidgetId,-1));
        saveState();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode ==R.integer.REQUEST_PICK_APPWIDGET) {
                configureWidget(data);
            } else if (requestCode ==R.integer.REQUEST_CREATE_APPWIDGET) {
                createWidget(data);
            }
        } else if (resultCode == getActivity().RESULT_CANCELED && data != null) {
            int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (appWidgetId != -1) {
                mAppWidgetHost.deleteAppWidgetId(appWidgetId);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //create and load the widget
    public void loadWidget(WidgetInfo widget){
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(widget.widgetId);
        widget.height = (widget.height>120) ? widget.height : Math.max(120,Math.max(appWidgetInfo.minResizeHeight,appWidgetInfo.minHeight));
        if(getAvailableSpace()<widget.height){
            Toast.makeText(getActivity(),getResources().getString(R.string.widget_space_error_message),Toast.LENGTH_LONG).show();
            return;
        }
        WidgetView hostView = (WidgetView) mAppWidgetHost.createView(getActivity().getApplicationContext(), widget.widgetId, appWidgetInfo);
        hostView.setAppWidget(widget.widgetId, appWidgetInfo);

        setWidgetSize(hostView, widget.height);

        hostView.setLongClickable(true);
        hostView.setOnLongClickListener(view -> {
            final WidgetView widgetWithMenuCurrentlyDisplayed = (WidgetView) view;
            final ViewGroup parent = (ViewGroup) widgetWithMenuCurrentlyDisplayed.getParent();
            int currentIndex = parent.indexOfChild(widgetWithMenuCurrentlyDisplayed);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            List<String> dialogChoices = new ArrayList<>();
            if(canBeBigger(widgetWithMenuCurrentlyDisplayed)){dialogChoices.add(getResources().getString(R.string.make_widget_bigger));}
            if(canBeSmaller(widgetWithMenuCurrentlyDisplayed)) {dialogChoices.add(getResources().getString(R.string.make_widget_smaller));}
            if(currentIndex>0) {dialogChoices.add(getResources().getString(R.string.move_widget_up));}
            if(currentIndex< widgetLinearLayout.getChildCount()-1) {dialogChoices.add(getResources().getString(R.string.move_widget_down));}
            dialogChoices.add(getResources().getString(R.string.delete));
            CharSequence[] dialogChoicesSequence = dialogChoices.toArray(new CharSequence[dialogChoices.size()]);

            builder.setItems( dialogChoicesSequence, (dialogInterface, i) -> {
                if(dialogChoices.get(i).equals(getResources().getString(R.string.make_widget_bigger))){
                    makeWidgetBigger(widgetWithMenuCurrentlyDisplayed);
                }
                else if(dialogChoices.get(i).equals(getResources().getString(R.string.make_widget_smaller))){
                    makeWidgetSmaller(widgetWithMenuCurrentlyDisplayed);
                }
                else if(dialogChoices.get(i).equals(getResources().getString(R.string.move_widget_up))){
                    moveWidgetUp(widgetWithMenuCurrentlyDisplayed,parent);
                }
                else if(dialogChoices.get(i).equals(getResources().getString(R.string.move_widget_down))){
                    moveWidgetDown(widgetWithMenuCurrentlyDisplayed,parent);
                }
                else if(dialogChoices.get(i).equals(getResources().getString(R.string.delete))){
                    removeWidget(widgetWithMenuCurrentlyDisplayed);
                }
            });
            AlertDialog dialog = builder.create();

            dialog.show();
            return true;
        });
                widgetLinearLayout.addView(hostView);
    }

    private void setWidgetSize(WidgetView hostView, int height) {
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(hostView.getAppWidgetId());
        hostView.setMinimumHeight(height);
        hostView.setMinimumWidth(Math.min(appWidgetInfo.minWidth, appWidgetInfo.minResizeWidth));
        ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        hostView.setLayoutParams(params);
    }

    private boolean canBeBigger(WidgetView widgetView){
        AppWidgetProviderInfo widgetInfo = mAppWidgetManager.getAppWidgetInfo(widgetView.getAppWidgetId());
        int height = widgetView.getHeight();
            if (height < 0 || widgetInfo == null||50>getAvailableSpace()) {
                return false;
            }
        return true;
    }
    private boolean canBeSmaller(WidgetView widgetView){
        AppWidgetProviderInfo widgetInfo = mAppWidgetManager.getAppWidgetInfo(widgetView.getAppWidgetId());
        int height = widgetView.getHeight();
        if (height < 0 || widgetInfo == null||height-50<Math.max(widgetInfo.minHeight,widgetInfo.minResizeHeight)) {
            return false;
        }
        return true;
    }
    private void makeWidgetBigger(WidgetView widgetView){
        if(canBeBigger(widgetView)){
            setWidgetSize(widgetView, widgetView.getHeight()+50);
        }
        saveState();
    }
    private void makeWidgetSmaller(WidgetView widgetView){
        if(canBeSmaller(widgetView)){
            setWidgetSize(widgetView, widgetView.getHeight()-50);
        }
        saveState();
    }
    private void moveWidgetUp(WidgetView widgetView, ViewGroup group){
        int currentIndex = group.indexOfChild(widgetView);
        if(currentIndex>0){
            group.removeViewAt(currentIndex);
            group.addView(widgetView, currentIndex - 1);
            saveState();
        }
    }
    private void moveWidgetDown(WidgetView widgetView, ViewGroup group){
        int currentIndex = group.indexOfChild(widgetView);
        if(currentIndex< widgetLinearLayout.getChildCount()-1){
            group.removeViewAt(currentIndex);
            group.addView(widgetView, currentIndex + 1);
            saveState();
        }
    }

    public int getAvailableSpace(){

        View progressBar = getActivity().findViewById(R.id.progressBar);
        View shortcutAppsList = getActivity().findViewById(R.id.shortcutAppsListView);
        View widgetFragment = getActivity().findViewById(R.id.widgetFragment);
        if(progressBar!= null && shortcutAppsList != null && widgetFragment!=null){
            RectF oneRect = calculateRectOnScreen(progressBar);
            RectF otherRect = calculateRectOnScreen(shortcutAppsList);
            float space = Math.abs(oneRect.bottom - otherRect.top);
            if (space==0){
                return Integer.MAX_VALUE;
            }
            space = space - widgetFragment.getHeight();
            return (int) space;
        }
        else return -1;

    }
    public static RectF calculateRectOnScreen(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return new RectF(location[0], location[1], location[0] + view.getMeasuredWidth(), location[1] + view.getMeasuredHeight());
    }

}