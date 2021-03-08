package com.kosmo.homespital.dialog;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.kosmo.homespital.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class hospDialog extends AlertDialog {

    private Context context;
    private EditText searchEditText;
    private ImageButton searchBtn;
    private ListView searchList;
    private ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
    private HashMap<String,String> item;
    private OnDismissListener onDismissListener = null;
    private String name,address;

    public hospDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    protected hospDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
    }

    protected hospDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hosp_search_dialog);

        searchEditText = findViewById(R.id.searchEdit);
        searchBtn = findViewById(R.id.searchBtn);
        searchList = findViewById(R.id.hosList);


        searchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Log.i("com.kosmo.homespital", String.valueOf(hasFocus));
                    getWindow().clearFlags(
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                    | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                    getWindow().setSoftInputMode(
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(searchEditText.getText().length()>1)
                {
                    new hospListAsyncTask().execute("https://homespital.ngrok.io/proj/Android/ChatBot/GetHospital",searchEditText.getText().toString());
                }
                else
                {
                    searchEditText.setHint("2자 이상으로 입력해주세요");
                }
            }
        });
    }

    private class hospListAsyncTask extends AsyncTask<String,Void,String> {

        @Override
        protected void onPreExecute() {

        }///////////onPreExecute

        @Override
        protected String doInBackground(String... params) {
            StringBuffer buf = new StringBuffer();

            try {
                URL url = new URL(String.format("%s?hosp_name=%s",params[0],params[1]));
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
                Log.i("com.kosmo.homespital","buf.length():"+buf.length());

                conn.disconnect();
            }
            catch(Exception e){e.printStackTrace();}

            return buf.toString();
        }///////////doInBackground

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONArray json = new JSONArray(result);
                Log.i("com.kosmo.homespital","hospList:"+result);
                Log.i("com.kosmo.homespital","JSONArray:"+json);

                for(int i = 0; i < json.length(); i++)
                {
                    item = new HashMap<String,String>();
                    item.put("Title", json.getJSONObject(i).get("HOSP_NAME").toString());
                    item.put("Address", json.getJSONObject(i).get("ADDRESS").toString());
                    list.add(item);
                }
                SimpleAdapter adapter = new SimpleAdapter(context, list, android.R.layout.simple_list_item_2,
                        new String[] {"Title","Address"},
                        new int[] {android.R.id.text1, android.R.id.text2});

                searchList.setAdapter(adapter);

                searchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        name = list.get(position).get("Title");
                        address = list.get(position).get("Address");
                        if(onDismissListener != null)
                        {
                            onDismissListener.onDismiss(hospDialog.this);
                        }
                        dismiss();
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }///////////////AsyncTask

    public String getName() {
        return name;
    }
    public String getAddress() {
        return address ;
    }
}
