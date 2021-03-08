package com.kosmo.homespital;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Checked;
import com.mobsandgeeks.saripaar.annotation.ConfirmPassword;
import com.mobsandgeeks.saripaar.annotation.Digits;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Password;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class RegisterActivity extends AppCompatActivity implements Validator.ValidationListener {

    @BindView(R.id.etRegName)
    @Length(min = 3,max = 5,message = "이름은 최소3~최대5자리 입니다")
    @NotEmpty(message = "내용을 입력해주세요")
    EditText etRegName;

    @BindView(R.id.etRegPhone)
    @NotEmpty(message = "내용을 입력해주세요")
    EditText etRegPhone;

    @BindView(R.id.etRegEmail)
    @Email(message = "이메일형식이 아닙니다")
    @NotEmpty(message = "내용을 입력해주세요")
    EditText etRegEmail;

    @BindView(R.id.etRegPassword)
    @Password(min = 6, scheme = Password.Scheme.ALPHA_NUMERIC_MIXED_CASE_SYMBOLS,message = "비번은 숫자, 영문대문자, 특수 문자를 조합해서 입력해주세요")
    @NotEmpty(message = "내용을 입력해주세요")
    EditText etRegPassword;

    @BindView(R.id.etRegConfirmPassword)
    @ConfirmPassword(message = "비밀번호가 일치하지 않습니다")
    @NotEmpty(message = "내용을 입력해주세요")
    EditText etRegConfirmPassword;

    @BindView(R.id.radiogroup)
    @Checked(message = "성별을 체크해주세요")
    RadioGroup radioGroup;

    @BindView(R.id.etRegAge)
    @NotEmpty(message = "내용을 입력해주세요")
    @Length(min = 1,max = 2,message = "나이는 최대2자리 입니다")
    EditText etRegAge;

    String radioString = "남자";

    Validator validator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        validator = new Validator(this);//필수
        validator.setValidationListener(this);//필수
    }

    @OnClick(R.id.btnRegRegister)
    void onButtonClicked() {
        validator.validate();
    }

    @OnClick(R.id.tvSignIn)
    void onTextClicked()
    {
        finish();
    }

    @OnCheckedChanged({R.id.radioMale,R.id.radioFemale})
    void onRadioChecked(CompoundButton button, boolean checked)
    {
        if(checked)
        {
            switch (button.getId()) {
                case R.id.radioMale:
                    radioString = "남자";
                    break;
                case R.id.radioFemale:
                    radioString = "여자";
                    break;
            }
        }

    }

    @Override
    public void onValidationSucceeded() {
        Toast.makeText(this,"회원가입 성공", Toast.LENGTH_SHORT).show();
        new RegisterTask().execute("https://homespital.ngrok.io/proj/Android/Auth/member/register?",
                etRegEmail.getText().toString(),
                etRegPassword.getText().toString(),
                etRegName.getText().toString(),
                radioString,
                etRegPhone.getText().toString(),
                etRegAge.getText().toString(),
                "175",
                "70",
                "ROLE_MEM",
                "1");//mem_email=kkk&mem_pwd=1234&mem_name=김기동&gender=남자&tel=01012345678&age=20&height=175&weight=75&role=ROLE_MEM&enable=1");
        finish();
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for(ValidationError error : errors){
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);
            if(view instanceof EditText){
                ((EditText)view).setError(message);
            }else{
                Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
            }
        }
    }
    private class RegisterTask extends AsyncTask<String, Void, String> {
        private AlertDialog progressDialog;
        private String sendMsg, receiveMsg;

        @Override
        protected void onPreExecute() {
            AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
            builder.setCancelable(false);
            builder.setView(R.layout.progress);


            //빌더로 다이얼로그창 생성
            progressDialog = builder.create();
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {//네트워크에 연결하는 과정이 있으므로 다른 스레드에서 실행되어야 한다.
            try{
                URL url = new URL(String.format("%s",params[0]));
                HttpURLConnection conn=(HttpURLConnection)url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "mem_email="+params[1]+"&mem_pwd="+params[2]+"&mem_name="+params[3]+"&gender="+params[4]+"&tel="+params[5]+"&age="+params[6]+"&height="+params[7]+"&weight="+params[8]+"&role="+params[9]+"&enable="+params[10];
                osw.write(sendMsg);
                osw.flush();
                //서버에 요청 및 응답코드 받기
                int responseCode=conn.getResponseCode();

                if(responseCode ==HttpURLConnection.HTTP_OK){
                    //연결된 커넥션에서 서버에서 보낸 데이타 읽기
                    BufferedReader br =
                            new BufferedReader(
                                    new InputStreamReader(conn.getInputStream(),"UTF-8"));
                    String line;
                    StringBuffer buffer = new StringBuffer();
                    while((line=br.readLine())!=null){
                        buffer.append(line);
                    }
                    receiveMsg = buffer.toString();
                    br.close();
                }
            }
            catch(Exception e){e.printStackTrace();}
            return receiveMsg;
        }

        protected void onPostExecute(String content) {//doInBackground 에서 리턴된 값이 여기로 들어온다.
            if(content !=null && content.length()!=0) {//회원가입 성공한 경우
                try {
                    Toast.makeText(RegisterActivity.this,"회원가입이 성공했습니다",Toast.LENGTH_SHORT).show();
                    finish();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            else{//회원이 아닌 경우
                Toast.makeText(RegisterActivity.this,"회원가입이 실패했습니다",Toast.LENGTH_SHORT).show();
            }

            //다이얼로그 닫기
            if(progressDialog!=null && progressDialog.isShowing())
                progressDialog.dismiss();

        }
    }
}
