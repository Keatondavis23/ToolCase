package org.ECEN499.level;

public class DataHolder {
    private static DataHolder instance = new DataHolder();
    private String distance;
    private String temp;

    private DataHolder() { }

    public static DataHolder getInstance() {
        return instance;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getTemp() {
        return temp != null ? temp : "0.0";
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }
}

