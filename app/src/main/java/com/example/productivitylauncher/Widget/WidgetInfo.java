package com.example.productivitylauncher.Widget;

public class WidgetInfo {
    public int widgetId;
    public int height;
    public WidgetInfo(int widgetId, int height) {
        this.widgetId = widgetId;
        this.height = height;
    }

    @Override
    public String toString() {
        return
                "widgetId='" + widgetId +", height=" + height;
    }
}
