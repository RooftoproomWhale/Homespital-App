package com.kosmo.homespital;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.fxn.BubbleTabBar;
import com.fxn.OnBubbleClickListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kosmo.homespital.view.CustomViewPager;
import com.kosmo.homespital.view.PagerAdapter;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.bubbleTabBar)
    BubbleTabBar bubbleTabBar;

    @BindView(R.id.viewPager)
    CustomViewPager viewPager;

    @BindView(R.id.topAppBar)
    MaterialToolbar topAppBar;

    @BindView(R.id.fab)
    FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Log.i("DeviceToken: ", FirebaseInstanceId.getInstance().getToken().toString());

        Log.i("com.kosmo.homespital","bubbleTabBar.get : "+bubbleTabBar.getChildCount());
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(),bubbleTabBar.getChildCount());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(3);

        bubbleTabBar.addBubbleListener(new OnBubbleClickListener() {
            @Override
            public void onBubbleClick(int i) {
                Log.i("com.kosmo.homespital","index : "+i);
                switch (i){
                    case R.id.home:
                        viewPager.setCurrentItem(0,false);
                        break;
                    case R.id.corona:
                        viewPager.setCurrentItem(1,false);
                        break;
                    case R.id.map:
                        viewPager.setCurrentItem(2,false);
                        break;
                    case R.id.myMedicine:
                        viewPager.setCurrentItem(3,false);
                        break;
                }
            }
        });

        bubbleTabBar.setupBubbleTabBar(viewPager);


        topAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if(item.getItemId() == R.id.search)
                {
                    Toast.makeText(getApplicationContext(),"??????",Toast.LENGTH_SHORT).show();
                    return true;
                }
                else if (item.getItemId() == R.id.mypage)
                {
                    SharedPreferences preferences = getSharedPreferences("loginInfo",MODE_PRIVATE);
                    String userEmail = preferences.getString("email", "?????????");
                    Log.i("com.kosmo.homespital" ,"userEmail: " + userEmail);
                    new MyAsyncTask().execute(
                            "https://homespital.ngrok.io/proj/Android/Basic/myPage/recentApt",
                            userEmail
                    );
                }
                return false;
            }
        });

        Intent intent = getIntent();

        //?????? ??????????????? ?????? destory?????? ???????????? ?????? ??????-??????(MainActivity)??? ?????? .????????? ????????????
        //??? ?????? ???????????? ???????????? ????????? onMessageReceived???
        //???????????? ???????????? ????????? ???????????? ?????? ??????
        if(intent.getStringExtra("noti_title")!=null){
            intent.setClass(this, MyPageActivity.class);
            startActivity(intent);
        }

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ChatActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
            }
        });

    }

    private class MyAsyncTask extends AsyncTask<String,Void,String>
    {
        @Override
        protected String doInBackground(String... params) {
            StringBuffer buf = new StringBuffer();
            try {
                URL url = new URL(String.format("%s?userEmail=%s",params[0],params[1]));
                HttpURLConnection conn=(HttpURLConnection)url.openConnection();
                //????????? ?????? ??? ???????????? ??????
                int responseCode=conn.getResponseCode();
                Log.i("com.kosmo.homespital","responseCode:"+responseCode);
                if(responseCode ==HttpURLConnection.HTTP_OK){
                    //????????? ??????????????? ???????????? ?????? ????????? ??????
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
            }
            catch(Exception e){e.printStackTrace();}

            return buf.toString();
        }

        @Override
        protected void onPostExecute(String result) {

            //??????????????? ?????? ?????????(JSON??????) ??????
            //????????? ?????? ?????? ??? ?????????
            Log.i("com.kosmo.homespital","myResult:"+result);

            try {
                JSONObject json = new JSONObject(result);
                String hospName = json.getString("hosp_NAME");
                String resTime = json.getString("res_TIME");
                String resDate = json.getString("res_DATE");
                String symptom = json.getString("sel_SYMP");
                String approved = json.getString("approved");
                Intent intent = new Intent(
                        getApplicationContext(), // ?????? ????????? ????????????
                        MyPageActivity.class);// ?????? ????????? ????????? ??????
                intent.putExtra("hospName", hospName);
                intent.putExtra("resTime", resTime);
                intent.putExtra("resDate", resDate);
                intent.putExtra("symptom", symptom);
                intent.putExtra("approved", approved);
                startActivity(intent);



            }
            catch(Exception e){e.printStackTrace();}
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences preferences = getSharedPreferences("loginInfo",MODE_PRIVATE);
        SharedPreferences.Editor editor =preferences.edit();
        editor.remove("email");
        editor.remove("pwd");
        editor.remove("name");
        editor.remove("gender");
        editor.remove("tel");
        editor.remove("age");
        editor.remove("height");
        editor.remove("weight");
        editor.commit();
    }
}
