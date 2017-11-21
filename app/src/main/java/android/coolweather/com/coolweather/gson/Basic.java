package android.coolweather.com.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 *建立Basic
 */

public class Basic {
    //通过@SerializedName注解的方式来让JSON字段和java字段之间建立映射关系
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update{

        @SerializedName("loc")
        public String updateTime;
    }

}
