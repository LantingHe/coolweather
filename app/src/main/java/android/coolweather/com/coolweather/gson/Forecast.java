package android.coolweather.com.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 数组，声明实体类引用的时候使用集体类进行声明
 */

public class Forecast {
    public String date;
    @SerializedName("tmp")
    public Temperature temperature;

    @SerializedName("cond")
    public More more;

    public class Temperature{
        public String max;
        public String min;
    }
    public class More{
        @SerializedName("txt_d")
        public String info;
    }
}
