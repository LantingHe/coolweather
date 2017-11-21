package android.coolweather.com.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Weather总的实体类
 */

public class Weather {
    public String status;

    public Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;
    //总的实体类
    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
