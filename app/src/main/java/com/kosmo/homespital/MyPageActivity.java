package com.kosmo.homespital;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.IntentCompat;

import com.google.android.material.appbar.MaterialToolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MyPageActivity extends AppCompatActivity {

    @BindView(R.id.profile_name)
    TextView name;
    @BindView(R.id.profile_email)
    TextView email;
    @BindView(R.id.profile_phone)
    TextView tel;
    @BindView(R.id.profile_gender)
    TextView gender;
    @BindView(R.id.profile_age)
    TextView age;
    @BindView(R.id.profile_height)
    TextView height;
    @BindView(R.id.profile_weight)
    TextView weight;
    @BindView(R.id.recentApt_HospName)
    TextView hospName;
    @BindView(R.id.recentApt_Time)
    TextView resTime;
    @BindView(R.id.recentApt_Approved)
    TextView approved;
    @BindView(R.id.logout)
    Button logout;
    @BindView(R.id.topAppBar)
    MaterialToolbar topAppBar;

    @OnClick(R.id.logout)
    void logout()
    {
        SharedPreferences autologin = getSharedPreferences("autologin",MODE_PRIVATE);
        autologin.edit().clear().commit();


        SharedPreferences preferences = getSharedPreferences("loginInfo",MODE_PRIVATE);
        preferences.edit().clear().commit();


        PackageManager packageManager = getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        startActivity(mainIntent);
        System.exit(0);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);
        ButterKnife.bind(this);
        Intent intent = getIntent(); /*데이터 수신*/
        SharedPreferences preferences = getSharedPreferences("loginInfo",MODE_PRIVATE);

        String user_name = preferences.getString("name", "비었음");
        String user_email = preferences.getString("email", "비었음");
        String user_tel = preferences.getString("tel", "비었음");
        String user_gender = preferences.getString("gender", "비었음");
        String user_age = preferences.getString("age", "비었음");
        String user_height = preferences.getString("height", "비었음");
        String user_weight = preferences.getString("weight", "비었음");

        String apt_hospName = intent.getExtras().getString("hospName");
        String apt_resTime = intent.getExtras().getString("resTime");
        String apt_resDate = intent.getExtras().getString("resDate");
        String apt_symptom = intent.getExtras().getString("symptom");
        String apt_approved = intent.getExtras().getString("approved");

        Log.i("com.kosmo.homespital" ,"user_name: " + user_name);
        Log.i("com.kosmo.homespital" ,"user_email: " + user_email);
        Log.i("com.kosmo.homespital" ,"user_tel: " + user_tel);
        Log.i("com.kosmo.homespital" ,"user_gender: " + user_gender);

        Log.i("com.kosmo.homespital" ,"hospName: " + apt_hospName);
        Log.i("com.kosmo.homespital" ,"resTime: " + apt_resTime);
        Log.i("com.kosmo.homespital" ,"symptom: " + apt_symptom);
        Log.i("com.kosmo.homespital" ,"approved: " + apt_approved);

        hospName.setText(apt_hospName);
        resTime.setText(apt_resDate + "  " +  apt_resTime);
        approved.setText(apt_approved);
        name.setText(user_name);
        email.setText(user_email);
        tel.setText("Tel: " + user_tel);
        gender.setText(user_gender);
        age.setText(user_age + "세");
        height.setText(user_height + "(cm)");
        weight.setText(user_weight + "(kg)");

        topAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

/*                Intent intent = new Intent(
                        getApplicationContext(), // 현재 화면의 제어권자
                        MainActivity.class);// 다음 넘어갈 클래스 지정
                startActivity(intent);*/
                finish();

                return false;
            }
        });


//        swipeRefreshLayout = findViewById(R.id.profile_refresh);
//
//        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                swipeRefreshLayout.setRefreshing(false);
//            }
//        });
    }

//    public void logout(View view) {
//        Toast.makeText(this, "Logout Clicked", Toast.LENGTH_SHORT).show();
//    }
//
//    public void change_pass(View view) {
//        Toast.makeText(this, "Change Password Clicked", Toast.LENGTH_SHORT).show();
//    }
}
