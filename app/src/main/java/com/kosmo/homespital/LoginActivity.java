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
    private static String OAUTH_CLIENT_NAME = "홈스피탈";

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
                .setDeniedMessage("만약 권한을 거부하신다면 앱 서비스를 사용할 수 없습니다\n\n [설정] > [권한]에서 권한을 승낙해주세요")
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
            etLogEmail.setError("이메일을 입력하세요");
            return false;
        }
        else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(inemail).matches())
        {
            etLogEmail.setError("이메일형식이 아닙니다");
            return false;
        }


        if(inpassword.isEmpty()){
            etLoginPassword.setError("비밀번호를 입력하세요");
            return false;
        }

        return true;
    }

    //서버로 데이타 전송 및 응답을 받기 위한 스레드 정의
    private class LoginAsyncTask extends AsyncTask<String,Void,String> {

        private AlertDialog progressDialog;
        @Override
        protected void onPreExecute() {
            //프로그래스바용 다이얼로그 생성]
            //빌더 생성 및 다이얼로그창 설정
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setCancelable(false);
            builder.setView(R.layout.progress);


            //빌더로 다이얼로그창 생성
            progressDialog = builder.create();
            progressDialog.show();
        }///////////onPreExecute

        @Override
        protected String doInBackground(String... params) {
            StringBuffer buf = new StringBuffer();
            try {
                URL url = new URL(String.format("%s?mem_email=%s&mem_pwd=%s",params[0],params[1],params[2]));
                HttpURLConnection conn=(HttpURLConnection)url.openConnection();
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
            }
            catch(Exception e){e.printStackTrace();}

            return buf.toString();
        }///////////doInBackground

        @Override
        protected void onPostExecute(String result) {

            //서버로부터 받은 데이타(JSON형식) 파싱
            //회원이 아닌 경우 빈 문자열
            Log.i("com.kosmo.homespital","result:"+result);
            if(result !=null && result.length()!=0) {//회원인 경우
                try {

                    JSONObject json = new JSONObject(result);
                    String name = json.getString("mem_name");
                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    intent.putExtra("name",name);
                    startActivity(intent);
                    finish();

                    //아이디 비번저장
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
            else{//회원이 아닌 경우
                Toast.makeText(LoginActivity.this,"아이디와 비번이 일치하지 않아요",Toast.LENGTH_SHORT).show();
            }

            //다이얼로그 닫기
            if(progressDialog!=null && progressDialog.isShowing())
                progressDialog.dismiss();



        }
    }///////////////LoginAsyncTask


    private void initData() {
        mOAuthLoginInstance = OAuthLogin.getInstance();

        mOAuthLoginInstance.showDevelopersLog(true);
        mOAuthLoginInstance.init(mContext, OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET, OAUTH_CLIENT_NAME);

        /*
         * 2015년 8월 이전에 등록하고 앱 정보 갱신을 안한 경우 기존에 설정해준 callback intent url 을 넣어줘야 로그인하는데 문제가 안생긴다.
         * 2015년 8월 이후에 등록했거나 그 뒤에 앱 정보 갱신을 하면서 package name 을 넣어준 경우 callback intent url 을 생략해도 된다.
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
        protected void onPreExecute() {//작업이 실행되기 전에 먼저 실행.

        }

        @Override
        protected String doInBackground(Void... params) {//네트워크에 연결하는 과정이 있으므로 다른 스레드에서 실행되어야 한다.
            String url = "https://openapi.naver.com/v1/nid/me";
            String at = mOAuthLoginInstance.getAccessToken(mContext);
            return mOAuthLoginInstance.requestApi(mContext, at, url);//url, 토큰을 넘겨서 값을 받아온다.json 타입으로 받아진다.
        }

        protected void onPostExecute(String content) {//doInBackground 에서 리턴된 값이 여기로 들어온다.
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
        // 세션 콜백 삭제
        Session.getCurrentSession().removeCallback(sessionCallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // 카카오톡|스토리 간편로그인 실행 결과를 받아서 SDK로 전달
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            Log.i("com.kosmo.homespital","requestCode:"+requestCode);
            Log.i("com.kosmo.homespital","resultCode:"+resultCode);
            Log.i("com.kosmo.homespital","data:"+data);
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
