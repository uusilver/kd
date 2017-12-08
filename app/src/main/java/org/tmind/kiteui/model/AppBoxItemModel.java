package org.tmind.kiteui.model;

import android.graphics.drawable.Drawable;

/**
 * Created by vali on 12/6/2017.
 */

public class AppBoxItemModel {

    private String applicationName;
    private Drawable packageImage;

    private String startTimeHour;
    private String startTimeMinute;
    private String endTimeHour;
    private String endTimeMinute;

    private String pkg;
    private String mainCls;

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public Drawable getPackageImage() {
        return packageImage;
    }

    public void setPackageImage(Drawable packageImage) {
        this.packageImage = packageImage;
    }

    public String getStartTimeHour() {
        return startTimeHour;
    }

    public void setStartTimeHour(String startTimeHour) {
        this.startTimeHour = startTimeHour;
    }

    public String getStartTimeMinute() {
        return startTimeMinute;
    }

    public void setStartTimeMinute(String startTimeMinute) {
        this.startTimeMinute = startTimeMinute;
    }

    public String getEndTimeHour() {
        return endTimeHour;
    }

    public void setEndTimeHour(String endTimeHour) {
        this.endTimeHour = endTimeHour;
    }

    public String getEndTimeMinute() {
        return endTimeMinute;
    }

    public void setEndTimeMinute(String endTimeMinute) {
        this.endTimeMinute = endTimeMinute;
    }

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public String getMainCls() {
        return mainCls;
    }

    public void setMainCls(String mainCls) {
        this.mainCls = mainCls;
    }
}
