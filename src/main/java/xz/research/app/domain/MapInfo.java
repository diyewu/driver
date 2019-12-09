package xz.research.app.domain;

import java.util.List;

public class MapInfo {

    private String name;//显示名称

    private String latitude;//纬度

    private String longitude;//经度

    private String huoseholds;//户数

    private List<ProgressInfo> progressInfos; //步骤信息，用于显示进度条

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getHuoseholds() {
        return huoseholds;
    }

    public void setHuoseholds(String huoseholds) {
        this.huoseholds = huoseholds;
    }

    public List<ProgressInfo> getProgressInfos() {
        return progressInfos;
    }

    public void setProgressInfos(List<ProgressInfo> progressInfos) {
        this.progressInfos = progressInfos;
    }
}
