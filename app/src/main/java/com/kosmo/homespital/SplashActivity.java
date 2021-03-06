package com.kosmo.homespital;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashActivity extends AppCompatActivity {

    private Animation anim1, anim2, anim3;

    @BindView(R.id.ivLogoSplash)
    ImageView logoSplash;

    @BindView(R.id.ivText)
    TextView ivText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        init();
        ButterKnife.bind(this);

        logoSplash.startAnimation(anim1);
        anim1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ivText.startAnimation(anim3);
                anim3.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        ivText.setVisibility(View.VISIBLE);

                        SharedPreferences autologin = getSharedPreferences("autologin",MODE_PRIVATE);
                        String prefData = autologin.getString("autologin","");

                        if(prefData.equals("autologin"))
                        {
                            new LoginAsyncTask().execute(
                                    "https://homespital.ngrok.io/proj/Android/Auth/member/json",
                                    autologin.getString("email","yoonsj@gmail.com"),
                                    autologin.getString("pwd","1234"));

                        }
                        else
                        {
                            finish();
                            startActivity(new Intent(SplashActivity.this,LoginActivity.class));
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //????????? ????????? ?????? ??? ????????? ?????? ?????? ????????? ??????
    private class LoginAsyncTask extends AsyncTask<String,Void,String> {

        private AlertDialog progressDialog;
        @Override
        protected void onPreExecute() {
            //????????????????????? ??????????????? ??????]
            //?????? ?????? ??? ?????????????????? ??????
            AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
            builder.setCancelable(false);
            builder.setView(R.layout.progress);


            //????????? ?????????????????? ??????
            progressDialog = builder.create();
            progressDialog.show();
        }///////////onPreExecute

        @Override
        protected String doInBackground(String... params) {
            StringBuffer buf = new StringBuffer();
            try {
                URL url = new URL(String.format("%s?mem_email=%s&mem_pwd=%s",params[0],params[1],params[2]));
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
        }///////////doInBackground

        @Override
        protected void onPostExecute(String result) {

            //??????????????? ?????? ?????????(JSON??????) ??????
            //????????? ?????? ?????? ??? ?????????
            Log.i("com.kosmo.homespital","result:"+result);
            if(result !=null && result.length()!=0) {//????????? ??????
                try {

                    JSONObject json = new JSONObject(result);
                    String name = json.getString("mem_name");
                    Intent intent = new Intent(SplashActivity.this,MainActivity.class);
                    intent.putExtra("name",name);
                    startActivity(intent);
                    finish();

                    //????????? ????????????
                    SharedPreferences preferences = getSharedPreferences("loginInfo",MODE_PRIVATE);
                    SharedPreferences.Editor editor =preferences.edit();
                    editor.putString("email",json.getString("mem_email"));
                    editor.putString("pwd",json.getString("mem_pwd"));
                    editor.putString("name", json.getString("mem_name"));
                    editor.putString("gender", json.getString("gender"));
                    editor.putString("tel", json.getString("tel"));
                    editor.putString("age", json.getString("age"));
                    editor.putString("height", json.getString("height"));
                    editor.putString("weight", json.getString("weight"));
                    editor.apply();
                }
                catch(Exception e){e.printStackTrace();}

            }

            //??????????????? ??????
            if(progressDialog!=null && progressDialog.isShowing())
                progressDialog.dismiss();
        }
    }///////////////LoginAsyncTask

    private void init(){


        anim1 = AnimationUtils.loadAnimation(getBaseContext(), R.anim.rotate);
        anim2 = AnimationUtils.loadAnimation(getBaseContext(), R.anim.fadeout);
        anim3 = AnimationUtils.loadAnimation(getBaseContext(), R.anim.fadein);
    }
}