package org.tmind.kiteui.model;

import android.graphics.drawable.Drawable;

/**
 * Created by vali on 11/29/2017.
 */

public class PackageInfoModel {

    private String applicationName;
    private Drawable packageImage;
    private String allowFlag;
    private String startTimeHour;
    private String startTimeMinute;
    private String endTimeHour;
    private String endTimeMinute;
    private String systemFlag;

    private String pkg;
    private String mainCls;

    private boolean oldAppFlag;


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

    public String getAllowFlag() {
        return allowFlag;
    }

    public void setAllowFlag(String allowFlag) {
        this.allowFlag = allowFlag;
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

    public boolean isOldAppFlag() {
        return oldAppFlag;
    }

    public void setOldAppFlag(boolean oldAppFlag) {
        this.oldAppFlag = oldAppFlag;
    }

    public String getSystemFlag() {
        return systemFlag;
    }

    public void setSystemFlag(String systemFlag) {
        this.systemFlag = systemFlag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PackageInfoModel that = (PackageInfoModel) o;

        return pkg != null ? pkg.equals(that.pkg) : that.pkg == null;

    }

    @Override
    public int hashCode() {
        return pkg != null ? pkg.hashCode() : 0;
    }
}
