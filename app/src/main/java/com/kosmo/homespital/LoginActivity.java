package com.kosmo.homespital;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.kakao.auth.AuthType;
import com.kakao.auth.Session;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kosmo.homespital.kakao.SessionCallback;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    private static String OAUTH_CLIENT_ID = "GVe_m816Ap0X5nw8XFXQ";
    private static String OAUTH_CLIENT_SECRET = "6JAsTo47hF";
    private static String OAUTH_CLIENT_NAME = "νμ€νΌν";

    private static OAuthLogin mOAuthLoginInstance;
    private SessionCallback sessionCallback = new SessionCallback();
    Session session;
    private static Context mContext;

    @BindView(R.id.etLogEmail)
    EditText etLogEmail;

    @BindView(R.id.etLoginPassword)
    EditText etLoginPassword;

    @BindView(R.id.buttonOAuthLoginImg)
    OAuthLoginButton mOAuthLoginButton;

    @BindView(R.id.autoLogin)
    CheckedTextView checkedTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mContext = this;
        initData();
        ButterKnife.bind(this);

        mOAuthLoginButton.setOAuthLoginHandler(mOAuthLoginHandler);

        session = Session.getCurrentSession();
        session.addCallback(sessionCallback);

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(LoginActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(LoginActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }


        };

        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("λ§μ½ κΆνμ κ±°λΆνμ λ€λ©΄ μ± μλΉμ€λ₯Ό μ¬μ©ν  μ μμ΅λλ€\n\n [μ€μ ] > [κΆν]μμ κΆνμ μΉλν΄μ£ΌμΈμ")
                .setPermissions(Manifest.permission.INTERNET,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA)
                .check();

    }

    @OnClick(R.id.autoLogin)
    void onAutoLogin(View v) {
        CheckedTextView textView = (CheckedTextView) v;
        textView.toggle();

        Log.i("com.kosmo.homespital","Checked:"+textView.isChecked());
    }

    @OnClick(R.id.btnLogin)
    void onButtonClicked() {

        String inEmail = etLogEmail.getText().toString();
        String inPassword = etLoginPassword.getText().toString();

        if(validateInput(inEmail, inPassword)){
            new LoginAsyncTask().execute(
                    "https://homespital.ngrok.io/proj/Android/Auth/member/json",
                    inEmail,
                    inPassword);
        }
    }

    @OnClick(R.id.tvRegister)
    void onRegisterClicked() {
        Intent intent = new Intent(this,RegisterActivity.class);
        startActivity(intent);
    }

    public boolean validateInput(String inemail, String inpassword){

        if(inemail.isEmpty()){
            etLogEmail.setError("μ΄λ©μΌμ μλ ₯νμΈμ");
            return false;
        }
        else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(inemail).matches())
        {
            etLogEmail.setError("μ΄λ©μΌνμμ΄ μλλλ€");
            return false;
        }


        if(inpassword.isEmpty()){
            etLoginPassword.setError("λΉλ°λ²νΈλ₯Ό μλ ₯νμΈμ");
            return false;
        }

        return true;
    }

    //μλ²λ‘ λ°μ΄ν μ μ‘ λ° μλ΅μ λ°κΈ° μν μ€λ λ μ μ
    private class LoginAsyncTask extends AsyncTask<String,Void,String> {

        private AlertDialog progressDialog;
        @Override
        protected void onPreExecute() {
            //νλ‘κ·Έλμ€λ°μ© λ€μ΄μΌλ‘κ·Έ μμ±]
            //λΉλ μμ± λ° λ€μ΄μΌλ‘κ·Έμ°½ μ€μ 
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setCancelable(false);
            builder.setView(R.layout.progress);


            //λΉλλ‘ λ€μ΄μΌλ‘κ·Έμ°½ μμ±
            progressDialog = builder.create();
            progressDialog.show();
        }///////////onPreExecute

        @Override
        protected String doInBackground(String... params) {
            StringBuffer buf = new StringBuffer();
            try {
                URL url = new URL(String.format("%s?mem_email=%s&mem_pwd=%s",params[0],params[1],params[2]));
                HttpURLConnection conn=(HttpURLConnection)url.openConnection();
                //μλ²μ μμ²­ λ° μλ΅μ½λ λ°κΈ°
                int responseCode=conn.getResponseCode();
                Log.i("com.kosmo.homespital","responseCode:"+responseCode);
                if(responseCode ==HttpURLConnection.HTTP_OK){
                    //μ°κ²°λ μ»€λ₯μμμ μλ²μμ λ³΄λΈ λ°μ΄ν μ½κΈ°
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

            //μλ²λ‘λΆν° λ°μ λ°μ΄ν(JSONνμ) νμ±
            //νμμ΄ μλ κ²½μ° λΉ λ¬Έμμ΄
            Log.i("com.kosmo.homespital","result:"+result);
            if(result !=null && result.length()!=0) {//νμμΈ κ²½μ°
                try {

                    JSONObject json = new JSONObject(result);
                    String name = json.getString("mem_name");
                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    intent.putExtra("name",name);
                    startActivity(intent);
                    finish();

                    //μμ΄λ λΉλ²μ μ₯
                    SharedPreferences preferences = mContext.getSharedPreferences("loginInfo",MODE_PRIVATE);
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

                    if(checkedTextView.isChecked())
                    {
                        SharedPreferences autologin = mContext.getSharedPreferences("autologin",MODE_PRIVATE);
                        SharedPreferences.Editor edit = autologin.edit();
                        edit.putString("autologin", "autologin");
                        edit.putString("email",json.getString("mem_email"));
                        edit.putString("pwd",json.getString("mem_pwd"));
                        edit.apply();
                    }
                    else
                    {
                        SharedPreferences autologin = mContext.getSharedPreferences("autologin",MODE_PRIVATE);
                        SharedPreferences.Editor edit = autologin.edit();
                        edit.remove("autologin");
                        edit.remove("email");
                        edit.remove("pwd");
                        edit.apply();
                    }
                }
                catch(Exception e){e.printStackTrace();}

            }
            else{//νμμ΄ μλ κ²½μ°
                Toast.makeText(LoginActivity.this,"μμ΄λμ λΉλ²μ΄ μΌμΉνμ§ μμμ",Toast.LENGTH_SHORT).show();
            }

            //λ€μ΄μΌλ‘κ·Έ λ«κΈ°
            if(progressDialog!=null && progressDialog.isShowing())
                progressDialog.dismiss();



        }
    }///////////////LoginAsyncTask


    private void initData() {
        mOAuthLoginInstance = OAuthLogin.getInstance();

        mOAuthLoginInstance.showDevelopersLog(true);
        mOAuthLoginInstance.init(mContext, OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET, OAUTH_CLIENT_NAME);

        /*
         * 2015λ 8μ μ΄μ μ λ±λ‘νκ³  μ± μ λ³΄ κ°±μ μ μν κ²½μ° κΈ°μ‘΄μ μ€μ ν΄μ€ callback intent url μ λ£μ΄μ€μΌ λ‘κ·ΈμΈνλλ° λ¬Έμ κ° μμκΈ΄λ€.
         * 2015λ 8μ μ΄νμ λ±λ‘νκ±°λ κ·Έ λ€μ μ± μ λ³΄ κ°±μ μ νλ©΄μ package name μ λ£μ΄μ€ κ²½μ° callback intent url μ μλ΅ν΄λ λλ€.
         */
        //mOAuthLoginInstance.init(mContext, OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET, OAUTH_CLIENT_NAME, OAUTH_callback_intent_url);
    }
    static private OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {
        @Override
        public void run(boolean success) {
            if (success) {
                String accessToken = mOAuthLoginInstance.getAccessToken(mContext);
                String refreshToken = mOAuthLoginInstance.getRefreshToken(mContext);
                long expiresAt = mOAuthLoginInstance.getExpiresAt(mContext);
                String tokenType = mOAuthLoginInstance.getTokenType(mContext);
                Log.i("com.kosmo.homespital","accessToken:"+accessToken);
                Log.i("com.kosmo.homespital","refreshToken:"+refreshToken);
                Log.i("com.kosmo.homespital","String.valueOf(expiresAt):"+String.valueOf(expiresAt));
                Log.i("com.kosmo.homespital","tokenType:"+tokenType);
                Log.i("com.kosmo.homespital","mOAuthLoginInstance.getState(mContext).toString()"+mOAuthLoginInstance.getState(mContext).toString());
                new RequestApiTask().execute();
//                mOauthAT.setText(accessToken);
//                mOauthRT.setText(refreshToken);
//                mOauthExpires.setText(String.valueOf(expiresAt));
//                mOauthTokenType.setText(tokenType);
//                mOAuthState.setText(mOAuthLoginInstance.getState(mContext).toString());
            } else {
                String errorCode = mOAuthLoginInstance.getLastErrorCode(mContext).getCode();
                String errorDesc = mOAuthLoginInstance.getLastErrorDesc(mContext);
                Toast.makeText(mContext, "errorCode:" + errorCode + ", errorDesc:" + errorDesc, Toast.LENGTH_SHORT).show();
            }
        }
    };
    private static class RequestApiTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {//μμμ΄ μ€νλκΈ° μ μ λ¨Όμ  μ€ν.

        }

        @Override
        protected String doInBackground(Void... params) {//λ€νΈμν¬μ μ°κ²°νλ κ³Όμ μ΄ μμΌλ―λ‘ λ€λ₯Έ μ€λ λμμ μ€νλμ΄μΌ νλ€.
            String url = "https://openapi.naver.com/v1/nid/me";
            String at = mOAuthLoginInstance.getAccessToken(mContext);
            return mOAuthLoginInstance.requestApi(mContext, at, url);//url, ν ν°μ λκ²¨μ κ°μ λ°μμ¨λ€.json νμμΌλ‘ λ°μμ§λ€.
        }

        protected void onPostExecute(String content) {//doInBackground μμ λ¦¬ν΄λ κ°μ΄ μ¬κΈ°λ‘ λ€μ΄μ¨λ€.
            try {
                JSONObject jsonObject = new JSONObject(content);
                JSONObject response = jsonObject.getJSONObject("response");
                String email = response.getString("email");
                String age = response.getString("age");
                String gender = response.getString("gender");
                String name = response.getString("name");
                String birthday = response.getString("birthday");
                Log.i("com.kosmo.homespital","email:"+email);

                SharedPreferences preferences = mContext.getSharedPreferences("loginInfo",MODE_PRIVATE);
                SharedPreferences.Editor editor =preferences.edit();
                editor.putString("email",email);
                editor.putString("name", name);
                editor.putString("gender", gender);
//                editor.putString("tel", json.getString("tel"));
                editor.putString("age", age);
//                editor.putString("height", json.getString("height"));
//                editor.putString("weight", json.getString("weight"));
                editor.apply();

                Intent intent = new Intent(mContext,MainActivity.class);
                intent.putExtra("name",name);
                mContext.startActivity(intent);
                ((Activity)mContext).finish();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
            @Override
            public void onCompleteLogout() {
                
            }
        });
        // μΈμ μ½λ°± μ­μ 
        Session.getCurrentSession().removeCallback(sessionCallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // μΉ΄μΉ΄μ€ν‘|μ€ν λ¦¬ κ°νΈλ‘κ·ΈμΈ μ€ν κ²°κ³Όλ₯Ό λ°μμ SDKλ‘ μ λ¬
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            Log.i("com.kosmo.homespital","requestCode:"+requestCode);
            Log.i("com.kosmo.homespital","resultCode:"+resultCode);
            Log.i("com.kosmo.homespital","data:"+data);
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
