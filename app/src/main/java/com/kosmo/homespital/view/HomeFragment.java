package com.kosmo.homespital.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.kosmo.homespital.R;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeFragment extends Fragment {

    @BindView(R.id.mediName)
    TextView mediName;
    @BindView(R.id.presDate)
    TextView presDate;
    @BindView(R.id.duration)
    TextView duration;
    @BindView(R.id.counts)
    TextView counts;
    @BindView(R.id.mediName_Soon)
    TextView mediNameSoon;
    @BindView(R.id.alarmTime)
    TextView alarmTime;
    @BindView(R.id.viewPager)
    CustomViewPager viewPager;
    @BindView(R.id.takeCounts)
    TextView takeCount;
    @BindView(R.id.TalarmTime)
    TextView TalarmTime;
    @BindView(R.id.TtakeCounts)
    TextView TtakeCounts;
    private Context context;

    @BindView(R.id.homeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    CarouselView carouselView;
    int[] images = {R.drawable.image1, R.drawable.image2, R.drawable.image3, R.drawable.image4};

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.i("com.kosmo.homespital", "onAttach:Home");
        this.context = context;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i("com.kosmo.homespital", "onCreateView:Home");

        View view = inflater.inflate(R.layout.home_tablayout, null, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        carouselView = view.findViewById(R.id.carouselView);
        carouselView.setImageListener(imageListener);
        carouselView.setPageCount(images.length);

        SharedPreferences preferences = this.getActivity().getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        String userEmail = preferences.getString("email", "비었음");
        Log.i("com.kosmo.homespital", "userEmail: " + userEmail);
        Log.i("com.kosmo.homespital", "onViewCreated:HomePage");

        new GetPresTask().execute(
                "https://homespital.ngrok.io/proj/Android/Basic/getPres",
                userEmail
        );
        new GetPreMediTask().execute(
                "https://homespital.ngrok.io/proj/Android/Basic/getPreMedi",
                userEmail
        );


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetPresTask().execute(
                        "https://homespital.ngrok.io/proj/Android/Basic/getPres",
                        userEmail
                );
                new GetPreMediTask().execute(
                        "https://homespital.ngrok.io/proj/Android/Basic/getPreMedi",
                        userEmail
                );

            }
        });

    }

    private class GetPresTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            StringBuffer buf = new StringBuffer();
            try {
                URL url = new URL(String.format("%s?userEmail=%s", params[0], params[1]));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                //서버에 요청 및 응답코드 받기
                int responseCode = conn.getResponseCode();
                Log.i("com.kosmo.homespital", "responseCode:" + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    //연결된 커넥션에서 서버에서 보낸 데이타 읽기
                    BufferedReader br =
                            new BufferedReader(
                                    new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    String line;
                    while ((line = br.readLine()) != null) {
                        buf.append(line);
                        Log.i("com.kosmo.homespital", "line:" + line);
                    }
                    br.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return buf.toString();
        }

        @Override
        protected void onPostExecute(String result) {

            //서버로부터 받은 데이타(JSON형식) 파싱
            //회원이 아닌 경우 빈 문자열
            Log.i("com.kosmo.homespital", "recentResult:" + result);

            try {
                JSONObject json = new JSONObject(result);
                String json_medi_name = json.getString("medi_name");
                String[] splitMediName = json_medi_name.split(",");
                String medi_name =  "";
                for (int i=0; i < splitMediName.length; i++) {
                    if (i < splitMediName.length - 1) {
                        medi_name += splitMediName[i] + ", ";
                    } else {
                        medi_name += splitMediName[i];
                    }
                }
                String pres_date = json.getString("pres_date");
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(Long.parseLong(pres_date));
                Date d = (Date) c.getTime();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                String preDate = format.format(d);

                Log.i("com.kosmo.homespital", "preDate:" + preDate);
                String duration_ = json.getString("duration");
                String count = json.getString("count");

                mediName.setText(medi_name);
                presDate.setText(preDate);
                duration.setText(duration_);
                counts.setText(count);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class GetPreMediTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            StringBuffer buf = new StringBuffer();
            try {
                URL url = new URL(String.format("%s?userEmail=%s", params[0], params[1]));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                //서버에 요청 및 응답코드 받기
                int responseCode = conn.getResponseCode();
                Log.i("com.kosmo.homespital", "responseCode:" + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    //연결된 커넥션에서 서버에서 보낸 데이타 읽기
                    BufferedReader br =
                            new BufferedReader(
                                    new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    String line;
                    while ((line = br.readLine()) != null) {
                        buf.append(line);
                        Log.i("com.kosmo.homespital", "line:" + line);
                    }
                    br.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return buf.toString();
        }

        @Override
        protected void onPostExecute(String result) {

            //서버로부터 받은 데이타(JSON형식) 파싱
            //회원이 아닌 경우 빈 문자열
            Log.i("com.kosmo.homespital", "soonResult:" + result);

            Date date = new Date();
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(date.getTime());
            Date d = (Date) c.getTime();
            int currHour = d.getHours() + 9;
            int currMin = d.getMinutes();
            int currTime = currHour * 60 + currMin;
//            SimpleDateFormat format = new SimpleDateFormat("hh:mm");
//            String hhh = format.format(d);
            Log.i("com.kosmo.homespital", "currTime:" + currTime);

            try {
                JSONArray json = new JSONArray(result);
                String[] timeArray = new String[json.length()];
                String medi_name = "";
                String strTime = "";
                String takeCounts = "";

                for (int i = 0; i < json.length(); i++) {
                    JSONObject medi = json.getJSONObject(i);
                    Log.i("com.kosmo.homespital", "medi:" + medi);
                    Log.i("com.kosmo.homespital", "medi_nameLog:" + medi.getString("medi_name"));
                    Log.i("com.kosmo.homespital", "alarmLog:" + medi.getString("alarm"));
                    Log.i("com.kosmo.homespital", "alarmLog:" + medi.getString("count"));
                    medi_name = medi.getString("medi_name");
                    takeCounts = medi.getString("count");
                    strTime = medi.getString("alarm");
                    String hour = strTime.substring(0, 2);
                    String min = strTime.substring(2, 4);
                    int takeTime = Integer.parseInt(hour) * 60 + Integer.parseInt(min);
                    Log.i("com.kosmo.homespital", "takeTime:" + takeTime);
                    int timeGap = takeTime - currTime;
                    if (timeGap <= 30 && timeGap > 0) {
                        mediNameSoon.setText(medi_name);
                        alarmTime.setText(strTime);
                        takeCount.setText(takeCounts);
                    } else {
                        mediNameSoon.setText("30분 이내에 복용할 약이 없습니다");
                        TalarmTime.setText("");
                        TtakeCounts.setText("");
                        alarmTime.setText("");
                        takeCount.setText("");
                    }

                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    ImageListener imageListener = new ImageListener() {
        @Override
        public void setImageForPosition(int position, ImageView imageView) {
            imageView.setImageResource(images[position]);
        }
    };
}
