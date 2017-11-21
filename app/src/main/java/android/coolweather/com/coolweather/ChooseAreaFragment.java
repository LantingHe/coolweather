package android.coolweather.com.coolweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.coolweather.com.coolweather.db.City;
import android.coolweather.com.coolweather.db.County;
import android.coolweather.com.coolweather.db.Province;
import android.coolweather.com.coolweather.util.HttpUtil;
import android.coolweather.com.coolweather.util.Utility;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 编写用于遍历省市县的数据碎片
 */

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    //省列表
    private List<Province> provinceList;
    //市列表
    private List<City> cityList;
    //县列表
    private List<County> countyList;
    //选中的省份
    private Province selectedProvince;
    //选中的城市
    private City selectedCity;
    private County selectedCounty;
    //当前选中的级别
    private int currentLevel;

    /**
     * 加载choose_area布局，一些控件的实例
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //加载choose_area布局及里面的内容
        View view = inflater.inflate(R.layout.choose_area,container,false);
        titleText = (TextView)view.findViewById(R.id.title_text);
        backButton = (Button)view.findViewById(R.id.back_button);
        listView = (ListView)view.findViewById(R.id.list_view);
        //ArrayAdapter初始化，设置ArrayAdapter为listView的适配器；
        adapter =new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }
    //listView的点击事件和backButton的点击事件
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //listView的点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);
                    queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();
                }else if(currentLevel == LEVEL_COUNTY){
                    //把当前选中的县天气id传递过来
                    selectedCounty = countyList.get(position);
                    String weatherId = countyList.get(position).getWeatherId();
                    if(getActivity()instanceof  MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if(getActivity() instanceof WeatherActivity){
                        WeatherActivity activity = (WeatherActivity)getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefreshLayout.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }
                }
            }
        });
        //backButton的点击事件
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel == LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel ==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        //初始化省级数据
        queryProvinces();
    }
    /**
     *查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryProvinces(){
        //设置头布局设置为中国
        titleText.setText("中国");
        //隐藏按钮
        backButton.setVisibility(View.GONE);
        //调用LitePal的findAll查询接口从Province数据库中读取数据省级数据，如果有地址就直接显示到界面
        provinceList = DataSupport.findAll(Province.class);
        if(provinceList.size()>0){
            dataList.clear();
            //遍历出来adapter
            for (Province province:provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            //界面显示
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else {
            //如果没有数据，就调用queryFromServer()方法就把请求地址传入
            String address ="http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }
    /**
     *查询选中省内所有市，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?",String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size()>0){
            dataList.clear();
            for (City city:cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else {
            int provinceCode= selectedProvince.getProvinceCode();
            String address ="http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address,"city");
        }
    }
    /**
     *查询选中市内所有县，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?",String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size()>0){
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else {
            int provinceCode= selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address ="http://guolin.tech/api/china/" + provinceCode +"/" +cityCode;
            queryFromServer(address,"county");
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据
     * @param address
     * @param type
     */
    private void queryFromServer(String address,final String type){
        //显示进度对话框
        showProgressDialog();
        //向服务器发送请求HttpUtil.sendOkHttpRequest()
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //通过runOnUiThread()方法回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            /**
             * 根据出入的地址和类型从服务器上查询省市县数据
             * 响应数据会回调到onResponse()中，之后调用Utility.handleProvinceResponse()来解析和处理服务器的数据并保存到数据库中，
             * @param call
             * @param response
             * @throws IOException
             */
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if("province".equals(type)){
                    result = Utility.handleProvinceResponse(responseText);
                }else if("city".equals(type)){
                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if ("county".equals(type)){
                    result = Utility.handleCountyResponse(responseText,selectedCity.getId());
                }
                if(result){
                    //因为牵扯到UI所以在主线程调用
                    //runOnUiThread()从子线程切换到主线程；
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //关闭进度对话框
                            closeProgressDialog();
                            if("province".equals(type)){
                                //重新加载数据
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }

        });
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog(){
        if(progressDialog == null){
            progressDialog =new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载....");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog(){
        if(progressDialog !=null){
            progressDialog.dismiss();
        }
    }
}
