package android.coolweather.com.coolweather.gson;

import java.security.PublicKey;

/**
 * Created by hlt on 2017/11/17.
 */

public class AQI {
    public AQICity city;
    public class AQICity{
        public String aqi;
        public String pm25;
    }
}
