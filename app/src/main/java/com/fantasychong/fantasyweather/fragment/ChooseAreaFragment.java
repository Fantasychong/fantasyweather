package com.fantasychong.fantasyweather.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fantasychong.fantasyweather.R;
import com.fantasychong.fantasyweather.db.City;
import com.fantasychong.fantasyweather.db.County;
import com.fantasychong.fantasyweather.db.Province;
import com.fantasychong.fantasyweather.util.JSONParseUtils;
import com.fantasychong.fantasyweather.util.OkHttpUtils;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by lenovo on 2017/12/27.
 */

public class ChooseAreaFragment extends Fragment {
    private static final int LEVEL_PROVINCE = 0;
    private static final int LEVEL_CITY = 1;
    private static final int LEVEL_COUNTY = 2;
    private ListView lv;
    private RelativeLayout rl;
    private TextView tv;
    private ArrayAdapter<String> adapter;
    private int currentLevel;
    private List<Province> provinceList= new ArrayList<>();
    private Province selectedProvince;
    private City selectedCity;
    private List<City> cityList= new ArrayList<>();
    private List<String> dataList= new ArrayList<>();
    private ProgressDialog progressDialog;
    private List<County> countyList= new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.choose_area, container, false);
        lv= view.findViewById(R.id.area_lv);
        rl= view.findViewById(R.id.area_rl);
        tv= view.findViewById(R.id.area_tv);
        adapter= new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        lv.setAdapter(adapter);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel== LEVEL_PROVINCE){
                    selectedProvince= provinceList.get(position);
                    queryCities();
                }else if(currentLevel== LEVEL_CITY){
                    selectedCity= cityList.get(position);
                    queryCounties();
                }
            }
        });
        rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel== LEVEL_COUNTY){
                    queryCities();
                }else if (currentLevel== LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    private void queryProvinces() {
        tv.setText("中国");
        rl.setVisibility(View.GONE);
        provinceList= DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province: provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            lv.setSelection(0);
            currentLevel= LEVEL_PROVINCE;
        }else {
            String address= "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    private void queryCities() {
        tv.setText(selectedProvince.getProvinceName());
        rl.setVisibility(View.VISIBLE);
        cityList= DataSupport.where("provinceid= ?", String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size()> 0){
            dataList.clear();
            for (City city: cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            lv.setSelection(0);
            currentLevel= LEVEL_CITY;
        }else {
            int provinceCode= selectedProvince.getProvinceCode();
            String address= "http://guolin.tech/api/china/"+ provinceCode;
            queryFromServer(address, "city");
        }
    }

    private void queryCounties() {
        tv.setText(selectedCity.getCityName());
        rl.setVisibility(View.VISIBLE);
        countyList= DataSupport.where("cityid= ?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size()> 0){
            dataList.clear();
            for (County county: countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            lv.setSelection(0);
            currentLevel= LEVEL_COUNTY;
        }else {
            int provinceCode= selectedProvince.getProvinceCode();
            int cityCode= selectedCity.getCityCode();
            String address= "http://guolin.tech/api/china/"+ provinceCode+ "/"+ cityCode;
            queryFromServer(address, "county");
        }
    }

    private void queryFromServer(String address, final String type) {
        showDialog();
        OkHttpUtils.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "请求失败，请检查您的网络", Toast.LENGTH_SHORT).show();
                        closeDialog();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData= response.body().string();
                boolean result= false;
                if ("province".equals(type)){
                    result= JSONParseUtils.handleProvinceResponse(responseData);
                }else if ("city".equals(type)){
                    result= JSONParseUtils.handleCityResponse(responseData, selectedProvince.getId());
                }else if ("county".equals(type)){
                    result= JSONParseUtils.handleCountyResponse(responseData, selectedCity.getId());
                }
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "请求成功", Toast.LENGTH_SHORT).show();
                            closeDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }




    private void closeDialog() {
        if (progressDialog!= null){
            progressDialog.dismiss();
        }
    }

    private void showDialog() {
        if (progressDialog== null){
            progressDialog= new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载中。。。");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
}
