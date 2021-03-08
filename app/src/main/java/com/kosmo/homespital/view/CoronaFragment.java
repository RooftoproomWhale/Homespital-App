package com.kosmo.homespital.view;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.kosmo.homespital.R;
import com.kosmo.homespital.adapter.NewsAdapter;
import com.kosmo.homespital.model.ItemNews;
import com.kosmo.homespital.viewholder.NewsHolder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CoronaFragment extends Fragment {

    private Context context;
    private int domesticORglobal = 0;
    private int totalORtoday = 0;

    private int totalAffected,totalRecover,totalDeath,totalActive,totalIng;
    private int todayAffected,todayRecover,todayDeath,todayActive,todayIng;
    private int GlobalAffected, GlobalDeath;
    private int todayGlobalAffected, todayGlobalDeath;

    @BindView(R.id.shimmer_recycler_view)
    ShimmerRecyclerView recyclerView;

    @BindView(R.id.domesticButton)
    Button domesticButton;
    @BindView(R.id.globalButton)
    Button globalButton;

    @BindView(R.id.totalTextView)
    TextView totalTextView;
    @BindView(R.id.todayTextView)
    TextView todayTextView;
    @BindView(R.id.today)
    TextView today;

    @BindView(R.id.cardView3)
    CardView cardView3;
    @BindView(R.id.cardView4)
    CardView cardView4;
    @BindView(R.id.cardView5)
    CardView cardView5;

    @BindView(R.id.AffectedTextView)
    TextView AffectedTextView;
    @BindView(R.id.DeathTextView)
    TextView DeathTextView;
    @BindView(R.id.RecoverTextView)
    TextView RecoverTextView;
    @BindView(R.id.ActiveTextView)
    TextView ActiveTextView;
    @BindView(R.id.IngTextView)
    TextView IngTextView;

    @BindView(R.id.coronaRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    private NewsAdapter adapter;
    private AlertDialog progressDialog;
    private DecimalFormat myFormatter = new DecimalFormat("###,###");
    private String startDate, endDate;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.i("com.kosmo.homespital","onAttach:Corona");
        this.context = context;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i("com.kosmo.homespital","onCreateView:Corona");
        Log.i("com.kosmo.homespital","getUserVisibleHint:"+getUserVisibleHint());
        View view = inflater.inflate(R.layout.corona_tablayout,null,false);
        ButterKnife.bind(this,view);

        return view;

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.i("com.kosmo.homespital","onViewCreated:Corona");

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new LoadNewsTask().execute();
                new LoadCoronaTask().execute();
                new LoadGlobalCoronaTask().execute();


            }
        });

        RecyclerView.LayoutManager layoutManager;
        layoutManager = new LinearLayoutManager(context);

        adapter = new NewsAdapter();

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        recyclerView.showShimmerAdapter();

        /*Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일 기준");
        String strDate = dateFormat.format(date);

        today.setText(strDate);*/

        toggleView();

        new LoadNewsTask().execute();
        new LoadCoronaTask().execute();
        new LoadGlobalCoronaTask().execute();
    }

    private void startTextAnimation(int max,TextView textView) {
        ValueAnimator animator = ValueAnimator.ofInt(0, max);
        animator.setDuration(1000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                textView.setText(myFormatter.format((int)animation.getAnimatedValue()));
            }
        });
        animator.start();
    }

    private void toggleView()
    {
        domesticButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                domesticButton.setBackgroundResource(R.drawable.statistic_button);
                domesticButton.setTextColor(Color.parseColor("#FFFFFF"));
                globalButton.setBackgroundResource(R.drawable.spinner_bg);
                globalButton.setTextColor(Color.parseColor("#000000"));

                domesticORglobal = 0;

                cardView3.setVisibility(View.VISIBLE);
                cardView4.setVisibility(View.VISIBLE);
                cardView5.setVisibility(View.VISIBLE);

                if(totalORtoday==0)
                {
                    startTextAnimation(totalAffected,AffectedTextView);
                    startTextAnimation(totalDeath,DeathTextView);
                    startTextAnimation(totalRecover,RecoverTextView);
                    startTextAnimation(totalActive,ActiveTextView);
                    startTextAnimation(totalIng,IngTextView);
                }
                else
                {
                    startTextAnimation(todayAffected,AffectedTextView);
                    startTextAnimation(todayDeath,DeathTextView);
                    startTextAnimation(todayRecover,RecoverTextView);
                    startTextAnimation(todayActive,ActiveTextView);
                    startTextAnimation(todayIng,IngTextView);
                }
            }
        });

        globalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                domesticButton.setBackgroundResource(R.drawable.spinner_bg);
                domesticButton.setTextColor(Color.parseColor("#000000"));
                globalButton.setBackgroundResource(R.drawable.statistic_button);
                globalButton.setTextColor(Color.parseColor("#FFFFFF"));

                domesticORglobal = 1;

                cardView3.setVisibility(View.GONE);
                cardView4.setVisibility(View.GONE);
                cardView5.setVisibility(View.GONE);

                if(totalORtoday==0)
                {
                    startTextAnimation(GlobalAffected,AffectedTextView);
                    startTextAnimation(GlobalDeath,DeathTextView);
                }
                else
                {
                    startTextAnimation(todayGlobalAffected,AffectedTextView);
                    startTextAnimation(todayGlobalDeath,DeathTextView);
                }
            }
        });

        totalTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                totalORtoday = 0;
                totalTextView.setTextColor(getResources().getColor(R.color.primary_text));
                totalTextView.setTypeface(Typeface.DEFAULT_BOLD);
                todayTextView.setTextColor(getResources().getColor(R.color.secondary_text));
                todayTextView.setTypeface(Typeface.DEFAULT);

                if(domesticORglobal == 0)
                {
                    startTextAnimation(totalAffected,AffectedTextView);
                    startTextAnimation(totalDeath,DeathTextView);
                    startTextAnimation(totalRecover,RecoverTextView);
                    startTextAnimation(totalActive,ActiveTextView);
                    startTextAnimation(totalIng,IngTextView);
                }
                else
                {
                    startTextAnimation(GlobalAffected,AffectedTextView);
                    startTextAnimation(GlobalDeath,DeathTextView);
                }
            }
        });

        todayTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                totalORtoday = 1;
                totalTextView.setTextColor(getResources().getColor(R.color.secondary_text));
                totalTextView.setTypeface(Typeface.DEFAULT);
                todayTextView.setTextColor(getResources().getColor(R.color.primary_text));
                todayTextView.setTypeface(Typeface.DEFAULT_BOLD);

                if(domesticORglobal == 0) {
                    startTextAnimation(todayAffected,AffectedTextView);
                    startTextAnimation(todayDeath,DeathTextView);
                    startTextAnimation(todayRecover,RecoverTextView);
                    startTextAnimation(todayActive,ActiveTextView);
                    startTextAnimation(todayIng,IngTextView);
                }
                else
                {
                    startTextAnimation(todayGlobalAffected,AffectedTextView);
                    startTextAnimation(todayGlobalDeath,DeathTextView);
                }
            }
        });
    }

    private void loadNews(List<ItemNews> newsList) {
        adapter.setNews(newsList);
        recyclerView.hideShimmerAdapter();
    }

    private class LoadNewsTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
//            AlertDialog.Builder builder = new AlertDialog.Builder(context);
//            builder.setCancelable(false);
//            builder.setView(R.layout.progress);
//            //빌더로 다이얼로그창 생성
//            progressDialog = builder.create();
//            progressDialog.show();
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected String doInBackground(Void... params) {
            StringBuffer buf = new StringBuffer();
            try {
                URL url = new URL("https://homespital.com.ngrok.io/covid/");
                TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {

                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {

                    }
                } };
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

//                HttpURLConnection conn=(HttpURLConnection)url.openConnection();
                HttpsURLConnection conn=(HttpsURLConnection)url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                conn.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });

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

        protected void onPostExecute(String result) {
            if(result !=null && result.length()!=0)
            {
                try {
                    Log.i("com.kosmo.homespital","뉴스 정보 로드 성공");

                    JSONArray json = new JSONArray(result);
                    Log.i("com.kosmo.homespital","result:"+result);
                    Log.i("com.kosmo.homespital","JSONArray:"+json);
                    Log.i("com.kosmo.homespital","JSONArray.get:"+json.getJSONObject(0));
                    List<ItemNews> newsList = new ArrayList<ItemNews>();
                    for(int i = 0; i < json.length(); i++)
                    {
                        ItemNews itemNews = new ItemNews();
                        itemNews.setTitle(json.getJSONObject(i).getString("title"));

                        newsList.add(itemNews);
                    }

                    loadNews(newsList);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            else{
                Log.i("com.kosmo.homespital","뉴스 정보 로드 실패");
            }

            /*if(progressDialog!=null && progressDialog.isShowing())
                progressDialog.dismiss();*/
        }
    }
    private class LoadCoronaTask extends AsyncTask<Void, Void, String> {



        @Override
        protected void onPreExecute() {
            /*AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(false);
            builder.setView(R.layout.progress);
            //빌더로 다이얼로그창 생성
            progressDialog = builder.create();
            progressDialog.show();*/
            Date date = new Date();
            Calendar cal = new GregorianCalendar(Locale.KOREA);
            cal.setTime(date);
            Log.i("com.kosmo.homespital","Calendar.HOUR_OF_DAY:"+cal.get(Calendar.HOUR_OF_DAY));
            if(cal.get(Calendar.HOUR_OF_DAY)<12)
            {
                cal.add(Calendar.DAY_OF_YEAR, -2);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                startDate = dateFormat.format(cal.getTime());

                cal.add(Calendar.DAY_OF_YEAR, 1);
                endDate = dateFormat.format(cal.getTime());

                SimpleDateFormat textdateFormat = new SimpleDateFormat("yyyy년 MM월 dd일 기준");
                String strDate = textdateFormat.format(cal.getTime());

                today.setText(strDate);
            }
            else
            {
                cal.add(Calendar.DAY_OF_YEAR, -1);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                startDate = dateFormat.format(cal.getTime());
                endDate = dateFormat.format(date);

                SimpleDateFormat textdateFormat = new SimpleDateFormat("yyyy년 MM월 dd일 기준");
                String strDate = textdateFormat.format(date);

                today.setText(strDate);
            }
            Log.i("com.kosmo.homespital","startDate:"+startDate);
            Log.i("com.kosmo.homespital","endDate:"+endDate);



        }

        @Override
        protected String doInBackground(Void... params) {
            StringBuffer buf = new StringBuffer();
            try {
                URL url = new URL(String.format("https://homespital.ngrok.io/proj/Android/Corona/Api?startDate=%s&endDate=%s",startDate,endDate));

                TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {

                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {

                    }
                } };
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

                HttpsURLConnection conn=(HttpsURLConnection)url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
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

        protected void onPostExecute(String result) {
            if(result !=null && result.length()!=0)
            {
                try {
                    Log.i("com.kosmo.homespital","코로나 정보 로드 성공");

                    JSONObject json = new JSONObject(result);
                    Log.i("com.kosmo.homespital","result:"+result);
                    Log.i("com.kosmo.homespital","JSONObject:"+json);
                    Log.i("com.kosmo.homespital","items:"+json.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item"));

                    totalAffected =json.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item").getJSONObject(0).getInt("decideCnt");
                    totalDeath = json.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item").getJSONObject(0).getInt("deathCnt");
                    totalRecover = json.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item").getJSONObject(0).getInt("clearCnt");
                    totalActive = json.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item").getJSONObject(0).getInt("careCnt");
                    totalIng = json.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item").getJSONObject(0).getInt("examCnt");

                    todayAffected = totalAffected - json.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item").getJSONObject(1).getInt("decideCnt");
                    todayDeath = totalDeath - json.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item").getJSONObject(1).getInt("deathCnt");
                    todayRecover = totalRecover - json.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item").getJSONObject(1).getInt("clearCnt");
                    todayActive = Math.abs(totalActive - json.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item").getJSONObject(1).getInt("careCnt"));
                    todayIng = Math.abs(totalIng-json.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item").getJSONObject(1).getInt("examCnt"));

                    //AffectedTextView.setText(myFormatter.format(totalAffected));
//                    DeathTextView.setText(myFormatter.format(totalDeath));
//                    RecoverTextView.setText(myFormatter.format(totalRecover));
//                    ActiveTextView.setText(myFormatter.format(totalActive));
//                    IngTextView.setText(myFormatter.format(totalIng));
                    if(domesticORglobal==0)
                    {
                        if(totalORtoday==0)
                        {
                            startTextAnimation(totalAffected,AffectedTextView);
                            startTextAnimation(totalDeath,DeathTextView);
                            startTextAnimation(totalRecover,RecoverTextView);
                            startTextAnimation(totalActive,ActiveTextView);
                            startTextAnimation(totalIng,IngTextView);
                        }
                        else
                        {
                            startTextAnimation(todayAffected,AffectedTextView);
                            startTextAnimation(todayDeath,DeathTextView);
                            startTextAnimation(todayRecover,RecoverTextView);
                            startTextAnimation(todayActive,ActiveTextView);
                            startTextAnimation(todayIng,IngTextView);
                        }
                    }


                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            else{
                Log.i("com.kosmo.homespital","코로나 정보 로드 실패");
            }

            /*if(progressDialog!=null && progressDialog.isShowing())
                progressDialog.dismiss();*/
        }
    }

    private class LoadGlobalCoronaTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            /*AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(false);
            builder.setView(R.layout.progress);
            //빌더로 다이얼로그창 생성
            progressDialog = builder.create();
            progressDialog.show();*/
        }

        @Override
        protected String doInBackground(Void... params) {
            StringBuffer buf = new StringBuffer();
            try {
                URL url = new URL(String.format("https://homespital.ngrok.io/proj/Android/Corona/Global?startDate=%s&endDate=%s",startDate,endDate));
                HttpURLConnection conn=(HttpURLConnection)url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
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

        protected void onPostExecute(String result) {
            if(result !=null && result.length()!=0)
            {
                try {
                    Log.i("com.kosmo.homespital","해외 코로나 정보 로드 성공");

                    JSONObject json = new JSONObject(result);
                    Log.i("com.kosmo.homespital","result:"+result);
                    Log.i("com.kosmo.homespital","JSONObject:"+json);
                    Log.i("com.kosmo.homespital","totalCount:"+json.getJSONObject("response").getJSONObject("body").getInt("totalCount"));
                    Log.i("com.kosmo.homespital","items:"+json.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item"));
                    Log.i("com.kosmo.homespital","length:"+json.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item").length());

                    int AffectedTotal = 0;
                    int DeathTotal = 0;

                    int YesterAffectedTotal = 0;
                    int YesterDeathTotal = 0;

                    for(int i = 0; i<json.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item").length()/2; i++)
                    {
                        Log.i("com.kosmo.homespital","item["+i+"]:"+json.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item").getJSONObject(i).getString("stdDay"));
                        AffectedTotal += json.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item").getJSONObject(i).getInt("natDefCnt");
                        DeathTotal += json.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item").getJSONObject(i).getInt("natDeathCnt");
                        Log.i("com.kosmo.homespital","AffectedTotal:"+AffectedTotal);
                        Log.i("com.kosmo.homespital","DeathTotal:"+DeathTotal);

                        GlobalAffected = AffectedTotal;
                        GlobalDeath = DeathTotal;
                    }

                    for(int i = json.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item").length()/2; i<json.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item").length(); i++)
                    {
                        Log.i("com.kosmo.homespital","item["+i+"]:"+json.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item").getJSONObject(i).getString("stdDay"));
                        YesterAffectedTotal += json.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item").getJSONObject(i).getInt("natDefCnt");
                        YesterDeathTotal += json.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item").getJSONObject(i).getInt("natDeathCnt");
                        Log.i("com.kosmo.homespital","YesterAffectedTotal:"+YesterAffectedTotal);
                        Log.i("com.kosmo.homespital","YesterDeathTotal:"+YesterDeathTotal);

                        todayGlobalAffected = AffectedTotal-YesterAffectedTotal;
                        todayGlobalDeath = DeathTotal-YesterDeathTotal;
                    }

//                    totalAffected = json.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item").getJSONObject(0).getString("decideCnt");
//                    totalDeath = json.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item").getJSONObject(0).getString("deathCnt");
//
//                    todayAffected = String.valueOf(Integer.parseInt(totalAffected) - json.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item").getJSONObject(1).getInt("decideCnt"));
//                    todayDeath = String.valueOf(Integer.parseInt(totalDeath) - json.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item").getJSONObject(1).getInt("deathCnt"));
//
//                    AffectedTextView.setText(totalAffected);
//                    DeathTextView.setText(totalDeath);;
                    if(domesticORglobal==1)
                    {
                        if(totalORtoday==0)
                        {
                            startTextAnimation(GlobalAffected,AffectedTextView);
                            startTextAnimation(GlobalDeath,DeathTextView);
                        }
                        else
                        {
                            startTextAnimation(todayGlobalAffected,AffectedTextView);
                            startTextAnimation(todayGlobalDeath,DeathTextView);
                        }
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            else{
                Log.i("com.kosmo.homespital","해외 코로나 정보 로드 실패");
            }

//            if(progressDialog!=null && progressDialog.isShowing())
//                progressDialog.dismiss();
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}

