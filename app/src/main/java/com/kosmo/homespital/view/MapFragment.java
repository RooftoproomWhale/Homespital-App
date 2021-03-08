package com.kosmo.homespital.view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.chivorn.smartmaterialspinner.SmartMaterialSpinner;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.kosmo.homespital.ChatActivity;
import com.kosmo.homespital.R;
import com.kosmo.homespital.util.LocationDistance;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Align;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import belka.us.androidtoggleswitch.widgets.BaseToggleSwitch;
import belka.us.androidtoggleswitch.widgets.ToggleSwitch;

import static android.content.Context.LOCATION_SERVICE;

public class MapFragment extends Fragment implements LocationListener,OnMapReadyCallback {

    private NaverMap map;

    private LinearLayout pharAdditionalInfo;
    private TextView name,address,time,call,reservation;
    private ImageView maskimage;

    private SmartMaterialSpinner spinner;
    private List<String> spinnerList;

    private FusedLocationSource locationSource;

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };


    private LocationManager locationManager;
    private LatLng currentLatLng;

    private ToggleSwitch toggleSwitch;

    //병원 = 0, 약국 = 1
    private int APIStatus = 0;
    private List<Marker> Markers = new Vector<>();

    private BottomSheetBehavior sheetBehavior;
    private FrameLayout bottom_sheet;

    private Context context;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.i("com.kosmo.homespital","onAttach:Map");
        this.context = context;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i("com.kosmo.homespital","onCreateView:Map");

        View view = inflater.inflate(R.layout.activity_map,null,false);

        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.i("com.kosmo.homespital","onViewCreated:Map");

        locationManager = (LocationManager)context.getSystemService(LOCATION_SERVICE);

        Log.i("com.kosmo.homespital","locationManager:"+locationManager);

        locationSource = new FusedLocationSource(this, PERMISSION_REQUEST_CODE);

        name = view.findViewById(R.id.name);
        address = view.findViewById(R.id.address);
        time = view.findViewById(R.id.time);
        call = view.findViewById(R.id.call);
        reservation = view.findViewById(R.id.reservation);
        maskimage = view.findViewById(R.id.maskimage);
        spinner = view.findViewById(R.id.spinner1);

        time.setVisibility(View.GONE);

        pharAdditionalInfo = view.findViewById(R.id.pharAdditionalInfo);

        toggleSwitch = view.findViewById(R.id.toggleSwitch);
        bottom_sheet = view.findViewById(R.id.standardBottomSheet);
        sheetBehavior = BottomSheetBehavior.from(bottom_sheet);

        sheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                Log.i("com.kosmo.homespital","newState:"+newState);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        toggleSwitch.setOnToggleSwitchChangeListener(new BaseToggleSwitch.OnToggleSwitchChangeListener() {
            @Override
            public void onToggleSwitchChangeListener(int position, boolean isChecked) {
                Log.i("com.kosmo.homespital","position:"+position + "isChecked:" + isChecked);
                APIStatus = position;
                if(APIStatus==0)
                {
                    spinner.setVisibility(View.VISIBLE);
                }
                else
                {
                    spinner.setVisibility(View.GONE);
                }
                refreshMap(map.getContentBounds().getCenter().toLatLng());
                sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        spinnerList = new ArrayList<>();
        spinnerList.add("전체");
        spinnerList.add("내과");
        spinnerList.add("비뇨기과");
        spinnerList.add("산부인과");
        spinnerList.add("성형외과");
        spinnerList.add("소아청소년과");
        spinnerList.add("신경과");
        spinnerList.add("안과");
        spinnerList.add("이비인후과");
        spinnerList.add("일반외과");
        spinnerList.add("정신건강의학과");
        spinnerList.add("정형외과");
        spinnerList.add("치과");
        spinnerList.add("피부과");
        spinnerList.add("한방과");

        spinner.setItem(spinnerList);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position==0)
                {
                    for (Marker marker : Markers) {
                        marker.setMap(map);
                    }
                }
                else
                {
                    for (Marker marker : Markers) {
                        if(marker.getTag().equals(spinnerList.get(position)))
                        {
                            marker.setMap(map);
                        }
                        else
                        {
                            marker.setMap(null);
                        }
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        FragmentManager fm = getChildFragmentManager();
        com.naver.maps.map.MapFragment mapFragment = (com.naver.maps.map.MapFragment) fm.findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = com.naver.maps.map.MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && locationManager != null) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 1000, 10, this);
            }
            if (!locationSource.isActivated()) { // 권한 거부됨
                map.setLocationTrackingMode(LocationTrackingMode.None);
            }

            return;
        }

        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i("com.kosmo.homespital","onStart:Map");

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationManager != null) {

                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 1000, 10, this);
            }
        } else {
            ActivityCompat.requestPermissions(
                    (Activity) context, PERMISSIONS, PERMISSION_REQUEST_CODE);
        }
    }



    @Override
    public void onResume() {
        super.onResume();
        Log.i("com.kosmo.homespital","onResume:Map");

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("com.kosmo.homespital","onPause:Map");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i("com.kosmo.homespital","onSaveInstanceState:Map");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i("com.kosmo.homespital","onStop:Map");

        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i("com.kosmo.homespital","onDestroyView:Map");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.i("com.kosmo.homespital","onLowMemory:Map");
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        Log.i("com.kosmo.homespital","onMapReady()");

        map = naverMap;

        map.setLocationSource(locationSource);
        map.setLocationTrackingMode(LocationTrackingMode.NoFollow);
        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);
        uiSettings.setCompassEnabled(true);

        map.setMaxZoom(18);
        map.setMinZoom(14);

        Log.i("com.kosmo.homespital","getMinZoom:"+map.getMinZoom()+ "getMaxZoom:" + map.getMaxZoom());

        map.addOnCameraIdleListener(new NaverMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                Log.i("com.kosmo.homespital","getCameraPosition()"+map.getCameraPosition());
                Log.i("com.kosmo.homespital","getContentBounds()"+map.getContentBounds());
                Log.i("com.kosmo.homespital","getContentBounds().getCenter().toLatLng()"+map.getContentBounds().getCenter().toLatLng());

                //updateMarkers();
                //refreshMap(map.getContentBounds().getCenter().toLatLng());
                Log.i("com.kosmo.homespital","Distance : "+ LocationDistance.distance(
                        currentLatLng.latitude,
                        currentLatLng.longitude,
                        map.getContentBounds().getCenter().toLatLng().latitude,
                        map.getContentBounds().getCenter().toLatLng().longitude,
                        "kilometer"));

                if(APIStatus != 2)
                {
                    if(LocationDistance.distance(
                            currentLatLng.latitude,
                            currentLatLng.longitude,
                            map.getContentBounds().getCenter().toLatLng().latitude,
                            map.getContentBounds().getCenter().toLatLng().longitude,
                            "kilometer") > 1.5)
                    {
                        refreshMap(map.getContentBounds().getCenter().toLatLng());
                        currentLatLng = map.getContentBounds().getCenter().toLatLng();
                    }
                }
            }
        });


        getCurrentPosition();
    }

    private void refreshMap(LatLng latLng)
    {
        if (Markers.size() > 0)
        {
            removeMarkers();
            new LoadApiTask().execute(String.valueOf(latLng.latitude), String.valueOf(latLng.longitude));
        }
    }

    private void getCurrentPosition()
    {
        try {
            Log.i("com.kosmo.homespital","locationManager:"+ locationManager);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Log.i("com.kosmo.homespital","location:"+ location);
            if(location != null){
                Log.i("com.kosmo.homespital","위도:"+ location.getLatitude() +",경도:"+ location.getLongitude());

                currentLatLng = new LatLng(location.getLatitude(),location.getLongitude());
                new LoadApiTask().execute(String.valueOf(currentLatLng.latitude), String.valueOf(currentLatLng.longitude));

                map.moveCamera(CameraUpdate.scrollTo(currentLatLng));
            }
        }
        catch(SecurityException e){e.printStackTrace();}
    }

    private void updateMarkers()
    {
        LatLngBounds bounds = map.getContentBounds();

        for (Marker marker : Markers) {
            LatLng position = marker.getPosition();

            if (bounds.contains(position)) {
                if(marker.getMap()==null)
                    marker.setMap(map);
            }
            else {
                if(marker.getMap()!=null)
                    marker.setMap(null);
            }
        }
    }

    private void removeMarkers()
    {
        for (Marker marker : Markers) {
            marker.setMap(null);
        }
        Markers.clear();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private class LoadApiTask extends AsyncTask<String, Void, String> {
        private AlertDialog progressDialog;

        @Override
        protected void onPreExecute() {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(false);
            builder.setView(R.layout.progress);


            //빌더로 다이얼로그창 생성
            progressDialog = builder.create();
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {//네트워크에 연결하는 과정이 있으므로 다른 스레드에서 실행되어야 한다.
            StringBuffer buf = new StringBuffer();
            try {
                URL url;
                if(APIStatus == 0)
                {
                    url = new URL(String.format("https://homespital.ngrok.io/proj/Homespital/Map/Hospital.hst?lat=%s&lng=%s",params[0],params[1]));
                }
                else if(APIStatus == 1)
                {
                    url = new URL(String.format("https://homespital.ngrok.io/proj/Homespital/Map/Pharmacy.hst?lat=%s&lng=%s",params[0],params[1]));
                }
                else
                {
                    url = new URL("https://homespital.ngrok.io/proj/Homespital/Map/Covid.hst");
                }

                HttpURLConnection conn=(HttpURLConnection)url.openConnection();
                conn.setConnectTimeout(7000);
                conn.setReadTimeout(7000);
                //서버에 요청 및 응답코드 받기
                int responseCode=conn.getResponseCode();
                Log.i("com.kosmo.homespital","responseCode:"+responseCode);
                if(responseCode ==HttpURLConnection.HTTP_OK){
                    //연결된 커넥션에서 서버에서 보낸 데이타 읽기
                    BufferedReader br =
                            new BufferedReader(
                                    new InputStreamReader(conn.getInputStream(),"UTF-8"));
                    String line;
                    while((line=br.readLine())!=null){
                        buf.append(line);
                        Log.i("com.kosmo.homespital","line:"+line);
                    }
                    br.close();
                }
                conn.disconnect();
            }
            catch(Exception e){e.printStackTrace();}


            return buf.toString();
        }

        protected void onPostExecute(String result) {//doInBackground 에서 리턴된 값이 여기로 들어온다.
            if(result !=null && result.length()!=0)
            {
                try
                {
                    if(APIStatus == 0)
                    {
                        Log.i("com.kosmo.homespital","병원 정보 로드 성공");

                        JSONArray json = new JSONArray(result);
                        Log.i("com.kosmo.homespital","result:"+result);
                        Log.i("com.kosmo.homespital","JSONArray:"+json);

                        for (int i = 0; i < json.length(); i++) {
                            int index = i;
                            Log.i("com.kosmo.homespital","JSONObject Y:"+json.getJSONObject(i).get("cor_y"));
                            Log.i("com.kosmo.homespital","JSONObject X:"+json.getJSONObject(i).get("cor_x"));
                            Marker marker = new Marker();
                            marker.setPosition(new LatLng(Double.parseDouble(json.getJSONObject(i).get("cor_y").toString()),
                                    Double.parseDouble(json.getJSONObject(i).get("cor_x").toString())));
                            marker.setCaptionText(json.getJSONObject(i).get("hosp_name").toString());
                            marker.setCaptionRequestedWidth(200);
                            marker.setCaptionAligns(Align.Top);
                            marker.setTag(json.getJSONObject(i).get("dept_name").toString());
                            if(json.getJSONObject(i).get("auth").equals("제휴승인됨"))
                            {
                                marker.setIcon(OverlayImage.fromResource(R.drawable.hospital_marker));
                            }
                            else
                            {
                                marker.setIcon(OverlayImage.fromResource(R.drawable.hospital_noauth_marker));
                            }
                            marker.setWidth(90);
                            marker.setHeight(90);
                            marker.setHideCollidedMarkers(true);
                            marker.setOnClickListener(new Overlay.OnClickListener() {
                                @Override
                                public boolean onClick(@NonNull Overlay overlay) {
                                    Log.i("com.kosmo.homespital",marker.getCaptionText() +"클릭됨");

                                    try {
                                        name.setText(json.getJSONObject(index).get("hosp_name").toString());
                                        address.setText(json.getJSONObject(index).get("address").toString());
                                        pharAdditionalInfo.setVisibility(View.GONE);
                                        reservation.setVisibility(View.VISIBLE);

                                        if(json.getJSONObject(index).get("auth").equals("제휴승인됨"))
                                        {
                                            reservation.setVisibility(View.VISIBLE);
                                        }
                                        else
                                        {
                                            reservation.setVisibility(View.GONE);
                                        }

                                        reservation.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(context, ChatActivity.class);
                                                intent.putExtra("hospname",name.getText().toString());
                                                startActivity(intent);
                                            }
                                        });

                                        call.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent();
                                                intent.setAction(Intent.ACTION_DIAL);
                                                try {
                                                    intent.setData(Uri.parse("tel:"+json.getJSONObject(index).get("tel").toString()));
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                startActivity(intent);
                                            }
                                        });
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                    return true;
                                }
                            });

                            Markers.add(marker);
                        }

                        for (Marker marker : Markers) {
                            marker.setMap(map);
                        }
                    }
                    else if(APIStatus == 1)
                    {
                        Log.i("com.kosmo.homespital","약국 정보 로드 성공");

                        JSONArray json = new JSONArray(result);
                        Log.i("com.kosmo.homespital","result:"+result);
                        Log.i("com.kosmo.homespital","JSONObject:"+json);

                        for (int i = 0; i < json.length(); i++) {
                            int index = i;
                            Log.i("com.kosmo.homespital","JSONObject Y:"+json.getJSONObject(i).get("cor_y"));
                            Log.i("com.kosmo.homespital","JSONObject X:"+json.getJSONObject(i).get("cor_x"));
                            Marker marker = new Marker();
                            marker.setPosition(new LatLng(Double.parseDouble(json.getJSONObject(i).get("cor_y").toString()),
                                    Double.parseDouble(json.getJSONObject(i).get("cor_x").toString())));
                            marker.setCaptionText(json.getJSONObject(i).get("phar_name").toString());
                            marker.setCaptionRequestedWidth(200);
                            marker.setCaptionAligns(Align.Top);
                            marker.setIcon(OverlayImage.fromResource(R.drawable.pharmacy_marker));
                            marker.setWidth(90);
                            marker.setHeight(90);
                            marker.setOnClickListener(new Overlay.OnClickListener() {
                                @Override
                                public boolean onClick(@NonNull Overlay overlay) {
                                    Log.i("com.kosmo.homespital",marker.getCaptionText() +"클릭됨");

                                    try {
                                        name.setText(json.getJSONObject(index).get("phar_name").toString());
                                        address.setText(json.getJSONObject(index).get("address").toString());
//                                        pharAdditionalInfo.setVisibility(View.VISIBLE);
                                        reservation.setVisibility(View.GONE);
//                                        String remainMask = json.getJSONArray("stores").getJSONObject(index).get("remain_stat").toString();
//                                        switch (remainMask){
//                                            case "plenty":
//                                                maskimage.setImageResource(R.drawable.mask_plenty);
//                                                break;
//                                            case "some":
//                                                maskimage.setImageResource(R.drawable.mask_some);
//                                                break;
//                                            case "few":
//                                                maskimage.setImageResource(R.drawable.mask_few);
//                                                break;
//                                            default:
//                                                maskimage.setImageResource(R.drawable.mask_empty);
//                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                                    return true;
                                }
                            });

                            Markers.add(marker);
                        }

                        for (Marker marker : Markers) {
                            marker.setMap(map);
                        }
                    }
                    else
                    {
                        Log.i("com.kosmo.homespital","코로나 정보 로드 성공");

                        JSONArray json = new JSONArray(result);
                        Log.i("com.kosmo.homespital","result:"+result);
                        Log.i("com.kosmo.homespital","JSONArray:"+json);
                        Log.i("com.kosmo.homespital","length:"+json.length());

                        for (int i = 0; i < json.length(); i++) {
                            int index = i;
                            Log.i("com.kosmo.homespital","JSONObject Y:"+json.getJSONObject(i).get("LAT"));
                            Log.i("com.kosmo.homespital","JSONObject X:"+json.getJSONObject(i).get("LNG"));
                            Marker marker = new Marker();
                            marker.setPosition(new LatLng(Double.parseDouble(json.getJSONObject(i).get("LAT").toString()),
                                    Double.parseDouble(json.getJSONObject(i).get("LNG").toString())));
                            marker.setCaptionText(json.getJSONObject(i).get("CONTENT").toString());
                            marker.setSubCaptionText(json.getJSONObject(i).get("DATE_").toString());
                            marker.setCaptionRequestedWidth(200);
                            marker.setCaptionAligns(Align.Top);
                            marker.setIcon(OverlayImage.fromResource(R.drawable.corona_patient));
                            marker.setWidth(130);
                            marker.setHeight(130);

                            Markers.add(marker);
                        }

                        for (Marker marker : Markers) {
                            marker.setMap(map);
                        }
                    }

                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            else{
                Log.i("com.kosmo.homespital","맵 정보 로드 실패");
            }

            //다이얼로그 닫기
            if(progressDialog!=null && progressDialog.isShowing())
                progressDialog.dismiss();

        }
    }
}
