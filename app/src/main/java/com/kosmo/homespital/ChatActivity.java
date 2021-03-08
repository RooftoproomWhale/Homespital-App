package com.kosmo.homespital;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.Intent;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import com.google.cloud.dialogflow.v2.TextInput;
import com.kosmo.homespital.adapter.ChatsAdapter;
import com.kosmo.homespital.dialog.hospDialog;
import com.kosmo.homespital.model.Cards;
import com.kosmo.homespital.model.Message;
import com.kosmo.homespital.model.MessageType;
import com.kosmo.homespital.model.Status;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import kr.co.prnd.YouTubePlayerView;

import static android.text.Html.FROM_HTML_MODE_LEGACY;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = ChatActivity.class.getSimpleName();
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private Vector<Message> chatMessages = new Vector<>();
    private ChatsAdapter chatsAdapter;
    private RecyclerView chatList;
    private TextView messageText;
    private ImageButton btnSpeak,send;
    private Message currentMessage;
    private SessionsClient sessionsClient;
    private SessionName sessionName;
    private UUID uuid = UUID.randomUUID();
    private String hosp_name = null;
    private String resv_no = null;
    private TextToSpeech tts;
    private String userEmail;
    private String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        SharedPreferences preferences = getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        userEmail = preferences.getString("email", "비었음");
        Log.i("com.kosmo.homespital", "userEmail: " + userEmail);

        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(getResources().openRawResource(R.raw.homespital));
            String projectID = ((ServiceAccountCredentials) credentials).getProjectId();
            sessionsClient = SessionsClient.create(SessionsSettings.newBuilder().setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build());
            sessionName = SessionName.of(projectID, uuid.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        sendWelcomeEvent();

        final FloatingActionButton fab = findViewById(R.id.move_to_down);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chatList.scrollToPosition(chatsAdapter.getItemCount() - 1);
            }
        });

        chatsAdapter = new ChatsAdapter(this, chatMessages);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        chatList = findViewById(R.id.chat_list_view);
        chatList.setLayoutManager(linearLayoutManager);
        chatList.setAdapter(chatsAdapter);
        chatList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int position = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (position != RecyclerView.NO_POSITION && position >= chatsAdapter.getItemCount() - 4) {
                    fab.hide();
                } else if (fab.getVisibility() != View.VISIBLE) {
                    fab.show();
                }
            }
        });

        messageText = findViewById(R.id.chat_edit_text1);

        messageText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    send.performClick();
                }

                return false;
            }
        });

        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });
        send = findViewById(R.id.enter_chat1);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(messageText.getText().toString().trim())) {
                    return;
                }
                final Message message = new Message();
                message.setText(messageText.getText().toString());
                message.setStatus(Status.WAIT);
                message.setTimeStamp(new Date().getTime());
                message.setMessageType(MessageType.MINE);
                chatMessages.add(message);
                currentMessage = message;
                sendMessage(message.getText());
                chatsAdapter.notifyDataSetChanged();
                messageText.setText("");
                chatList.smoothScrollToPosition(chatsAdapter.getItemCount());
            }
        });

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {

                    int result = tts.setLanguage(Locale.KOREA);

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    } else {

                    }

                } else {
                    Log.e("TTS", "Initilization Failed!");
                }
            }
        });

        android.content.Intent intent = getIntent();
        Log.i(TAG,"intent:"+intent);
        if(intent.getExtras() != null)
        {
            final Message message = new Message();
            message.setText("예약");
            message.setStatus(com.kosmo.homespital.model.Status.WAIT);
            message.setTimeStamp(new Date().getTime());
            message.setMessageType(MessageType.MINE);
            chatMessages.add(message);
            currentMessage = message;
            sendMessage(message.getText());
            chatsAdapter.notifyDataSetChanged();
            messageText.setText("");
            chatList.smoothScrollToPosition(chatsAdapter.getItemCount());


            /*message.setText(intent.getExtras().getString("hospname"));
            message.setStatus(com.kosmo.homespital.model.Status.WAIT);
            message.setTimeStamp(new Date().getTime());
            message.setMessageType(MessageType.MINE);
            chatMessages.add(message);
            currentMessage = message;
            sendMessage(message.getText());
            chatsAdapter.notifyDataSetChanged();
            messageText.setText("");
            chatList.smoothScrollToPosition(chatsAdapter.getItemCount());*/
        }
    }

    void sendWelcomeEvent() {
        new RequestTask(this).execute();
    }

    private void sendMessage(String message) {
        new RequestTask(this).execute(message);
    }

    class RequestTask extends AsyncTask<String, Void, DetectIntentResponse> {

        private WeakReference<ChatActivity> activity;
        private SessionsClient sessionsClient;

        RequestTask(ChatActivity activity) {
            this.activity = new WeakReference<>(activity);
            this.sessionsClient = activity.sessionsClient;
            Log.i(TAG,activity.toString());
            Log.i(TAG,sessionsClient.toString());
        }

        @Override
        protected DetectIntentResponse doInBackground(String... requests) {
            try {
                return sessionsClient.detectIntent(activity.get().sessionName,
                        QueryInput.newBuilder()
                                .setText(TextInput.newBuilder()
                                        .setText(requests[0])
                                        .setLanguageCode("ko")
                                        .build())
                                .build());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(DetectIntentResponse response) {

            if (response != null) {
                if (activity.get().currentMessage != null) {
                    activity.get().currentMessage.setStatus(com.kosmo.homespital.model.Status.SENT);
                }

                List<Intent.Message> messages = response.getQueryResult().getFulfillmentMessagesList();
                Log.i(TAG,"response.getAction():"+response.getQueryResult().getAction());
                Log.i(TAG,"response.getQueryResult():"+response.getQueryResult());
                Log.i(TAG,"response.getQueryText():"+response.getQueryResult().getQueryText());
                Log.i(TAG,"response.getIntent().getDisplayName():"+response.getQueryResult().getIntent().getDisplayName());
                Log.i(TAG,"response.getIntent().getFulfillmentMessages():"+response.getQueryResult().getFulfillmentMessages(0));
//                Log.i(TAG,"response.getParameters():"+response.getQueryResult().getOutputContexts(0).getParameters());
//                Log.i(TAG,"response.getFieldsMap():"+response.getQueryResult().getOutputContexts(0).getParameters().getFieldsMap());
//                Log.i(TAG,"response.getFieldsMap().get(hosp_name):"+response.getQueryResult().getOutputContexts(0).getParameters().getFieldsMap().get("hosp_name"));
                if(response.getQueryResult().getOutputContextsCount() > 0)
                {
                    if(response.getQueryResult().getOutputContexts(0).getParameters().getFieldsMap().get("hosp_name") != null)
                    {
                        hosp_name = response.getQueryResult().getOutputContexts(0).getParameters().getFieldsMap().get("hosp_name").getStringValue();
                        Log.i(TAG,"hosp_name:"+hosp_name);
                    }
                    if(response.getQueryResult().getOutputContexts(0).getParameters().getFieldsMap().get("number") != null)
                    {
                        Log.i(TAG,"get(number):"+response.getQueryResult().getOutputContexts(0).getParameters().getFieldsMap().get("number"));
                        resv_no = String.valueOf((int)response.getQueryResult().getOutputContexts(0).getParameters().getFieldsMap().get("number").getNumberValue());
                        Log.i(TAG,"resv_no:"+resv_no);
                    }
                }

                Log.i(TAG,"messages:"+messages);

                if(response.getQueryResult().getAction().equals("res_list"))
                {
                    try {
                        String data = new resListAsyncTask().execute("https://homespital.ngrok.io/proj/Android/ChatBot/GetReservation",userEmail).get();
                        Message msg = new Message();
                        msg.setTimeStamp(new Date().getTime());
                        msg.setMessageType(MessageType.OTHER);
                        if(data.length() > 0)
                        {
                            msg.setText(Html.fromHtml(data,FROM_HTML_MODE_LEGACY).toString());
                        }
                        else
                        {
                            msg.setText("현재 예약내역이 존재하지 않습니다");
                        }
                        addMessage(msg);

                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else if(response.getQueryResult().getAction().equals("PRE_List"))
                {
                    try {
                        String data = new preListAsyncTask().execute("https://homespital.ngrok.io/proj/Android/ChatBot/GetPrescription",userEmail).get();
                        Message msg = new Message();
                        msg.setTimeStamp(new Date().getTime());
                        msg.setMessageType(MessageType.OTHER);
                        if(data.length() > 0)
                        {
                            msg.setText(Html.fromHtml(data,FROM_HTML_MODE_LEGACY).toString());
                        }
                        else
                        {
                            msg.setText("현재 복용중인 약이 존재하지 않습니다");
                        }
                        addMessage(msg);

                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else if(response.getQueryResult().getAction().equals("res_delete"))
                {
                    new resDeleteAsyncTask().execute("https://homespital.ngrok.io/proj/Android/ChatBot/DeleteReservation",resv_no);
                    Message msg = new Message();
                    msg.setTimeStamp(new Date().getTime());
                    msg.setMessageType(MessageType.OTHER);
                    msg.setText("예약이 취소 되었습니다");
                    addMessage(msg);
                }

                else if(response.getQueryResult().getAction().equals("modal-res"))
                {
                    Message msg = new Message();
                    msg.setTimeStamp(new Date().getTime());
                    msg.setMessageType(MessageType.OTHER);
                    msg.setText("예약을 도와드리겠습니다");
                    addMessage(msg);

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);


                    hospDialog hospdialog = new hospDialog(ChatActivity.this);
                    hospdialog.show();
                    hospdialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            Log.i(TAG,"hospdialog:"+hospdialog.getName()+"/"+hospdialog.getAddress());
                            address = hospdialog.getAddress();
                            final Message message = new Message();
                            message.setText(hospdialog.getName());
                            message.setStatus(com.kosmo.homespital.model.Status.WAIT);
                            message.setTimeStamp(new Date().getTime());
                            message.setMessageType(MessageType.MINE);
                            chatMessages.add(message);
                            currentMessage = message;
                            sendMessage(message.getText());
                            chatsAdapter.notifyDataSetChanged();
                            messageText.setText("");
                            chatList.smoothScrollToPosition(chatsAdapter.getItemCount());
                        }
                    });

                }

                else if(response.getQueryResult().getAction().equals("uri"))
                {
                    response.getQueryResult().getFulfillmentMessages(0);
                    Log.i(TAG,"uri:"+ response.getQueryResult().getFulfillmentMessages(0).getText());
                    Message msg = new Message();
                    msg.setTimeStamp(new Date().getTime());
                    msg.setMessageType(MessageType.YOUTUBE);
                    msg.setYouTubePlayerView(response.getQueryResult().getFulfillmentMessages(0).getText().getText(0));
                    addMessage(msg);
                }

                else {
                    if(response.getQueryResult().getAction().equals("RES-yes"))
                    {
                        String dateTime;
                        Log.i(TAG,"dept_name:"+response.getQueryResult().getOutputContexts(0).getParameters().getFieldsMap().get("dept_name").getStringValue());
                        Log.i(TAG,"sel-symp:"+response.getQueryResult().getOutputContexts(0).getParameters().getFieldsMap().get("sel-symp").getStringValue());
                        dateTime = response.getQueryResult().getOutputContexts(0).getParameters().getFieldsMap().get("date_time").getStructValue().getFieldsMap().get("date_time").getStringValue();
                        Log.i(TAG,"date_time:"+dateTime);
                        Log.i(TAG,"date_time.res_date:"+dateTime.substring(0,dateTime.indexOf("T")));
                        Log.i(TAG,"date_time.res_time:"+dateTime.substring(dateTime.indexOf("T")+1,dateTime.indexOf("+")-3));
                        Log.i(TAG,"hosp_name:"+hosp_name);
                        //"hosp_name="+params[1]+"&mem_email="+params[2]+"&dept_name="+params[3]+"&res_date="+params[4]+"&res_time="+params[5]+"&sel_symp="+params[6];
                        new resInsertAsyncTask().execute("https://homespital.ngrok.io/proj/Android/ChatBot/InsertReservation?",
                                address,
                                userEmail,
                                response.getQueryResult().getOutputContexts(0).getParameters().getFieldsMap().get("dept_name").getStringValue(),
                                dateTime.substring(0,dateTime.indexOf("T")),
                                dateTime.substring(dateTime.indexOf("T")+1,dateTime.indexOf("+")-3),
                                response.getQueryResult().getOutputContexts(0).getParameters().getFieldsMap().get("sel-symp").getStringValue());
                    }
                    for (Intent.Message m : messages) {
                        Log.i(TAG,"Intent.Message:"+m);
                        Message msg = new Message();
                        msg.setTimeStamp(new Date().getTime());
                        msg.setMessageType(MessageType.OTHER);
                        /*if(m.getText().getText(0).contains("T"))
                        {
                            String beforeText = m.getText().getText(0);
                            beforeText.substring(0)
                            beforeText.indexOf("T")
                        }*/
                        if(m.getText().getText(0).contains("예약날짜"))
                        {
                            Log.i(TAG,"m:"+m.getText().getText(0));
                            Log.i(TAG,"m:"+m.getText().getText(0).indexOf("res_date\">"));
                            Log.i(TAG,"m:"+m.getText().getText(0).indexOf("</span>"));
                            Log.i(TAG,"m:"+m.getText().getText(0).substring(m.getText().getText(0).indexOf("2020"),m.getText().getText(0).indexOf("T")));
                            Log.i(TAG,"m:"+m.getText().getText(0).substring(m.getText().getText(0).indexOf("T")+1,m.getText().getText(0).indexOf("T")+3));
                            Log.i(TAG,"m:"+m.getText().getText(0).substring(m.getText().getText(0).indexOf("2020"),m.getText().getText(0).indexOf("</span>")));
                            Log.i(TAG,"m:"+m.getText().getText(0).replace(m.getText().getText(0).substring(m.getText().getText(0).indexOf("2020"),m.getText().getText(0).indexOf("</span>"))
                                    ,m.getText().getText(0).substring(m.getText().getText(0).indexOf("2020"),m.getText().getText(0).indexOf("T"))+" "+m.getText().getText(0).substring(m.getText().getText(0).indexOf("T")+1,m.getText().getText(0).indexOf("T")+3)+"시"));
                            msg.setText(Html.fromHtml(m.getText().getText(0).replace(m.getText().getText(0).substring(m.getText().getText(0).indexOf("2020"),m.getText().getText(0).indexOf("</span>"))
                                    ,m.getText().getText(0).substring(m.getText().getText(0).indexOf("2020"),m.getText().getText(0).indexOf("T"))+" "+m.getText().getText(0).substring(m.getText().getText(0).indexOf("T")+1,m.getText().getText(0).indexOf("T")+3)+"시"),FROM_HTML_MODE_LEGACY).toString());
                            addMessage(msg);
                            tts.speak(msg.getText(), TextToSpeech.QUEUE_FLUSH, null);
                        }
                        else
                        {
                            Log.i(TAG,"m:"+m.getText().getText(0));
                            msg.setText(Html.fromHtml(m.getText().getText(0),FROM_HTML_MODE_LEGACY).toString());
                            addMessage(msg);
                            tts.speak(msg.getText(), TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }
                }
/*
                for (Intent.Message m : messages) {
                    Log.i(TAG,"message:"+m);
                    Log.i(TAG,"m.hasPayload():"+m.hasPayload());
                    Log.i(TAG,"m.hasCard():"+m.hasCard());
                    Log.i(TAG,"m.hasText():"+m.hasText());
                    if (m.hasPayload()) {
                        boolean isEventsLists = m.getPayload().getFieldsMap().containsKey("EVENT_LISTS");
                        if (isEventsLists)
                            isEventsLists = m.getPayload().getFieldsMap().get("EVENT_LISTS").getBoolValue();
                        if (isEventsLists) {
                            new RequestTask(activity.get()).execute("technical events");
                            new RequestTask(activity.get()).execute("non technical events");
                            new RequestTask(activity.get()).execute("online events");
                            return;
                        }
                    }
                    else if (m.hasText()) {
                        Message msg = new Message();
                        msg.setTimeStamp(new Date().getTime());
                        msg.setMessageType(MessageType.OTHER);
                        msg.setText(m.getText().getText(0));
                        addMessage(msg);
                    }
                }*/
            }/* else {
                Toast.makeText(activity.get(), "Oops! Something went wrong.\nPlease Check your Network.", Toast.LENGTH_SHORT).show();
            }*/
        }

        void addMessage(Message message) {
            activity.get().currentMessage.setStatus(com.kosmo.homespital.model.Status.SENT);
            activity.get().chatMessages.add(message);
            activity.get().chatsAdapter.notifyDataSetChanged();
            activity.get().chatList.smoothScrollToPosition(activity.get().chatsAdapter.getItemCount());
        }
    }

    private void promptSpeechInput() {
        android.content.Intent intent = new android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "말해보세요!");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "음성 기능은 지원하지 않는 기기입니다",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    final Message message = new Message();
                    message.setText(result.get(0));
                    message.setStatus(Status.WAIT);
                    message.setTimeStamp(new Date().getTime());
                    message.setMessageType(MessageType.MINE);
                    chatMessages.add(message);
                    currentMessage = message;
                    sendMessage(message.getText());
                    chatsAdapter.notifyDataSetChanged();
                    messageText.setText("");
                    chatList.smoothScrollToPosition(chatsAdapter.getItemCount());
                }
                break;
            }

        }
    }

    private class resListAsyncTask extends AsyncTask<String,Void,String> {

        @Override
        protected void onPreExecute() {

        }///////////onPreExecute

        @Override
        protected String doInBackground(String... params) {
            StringBuffer buf = new StringBuffer();
            StringBuilder str = new StringBuilder();

            try {
                URL url = new URL(String.format("%s?mem_email=%s",params[0],params[1]));
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
                if(buf.length()>2) {
                    try {
                        JSONArray json = new JSONArray(buf.toString());
                        Log.i(TAG,json.toString());
                        str.append("<span style='text-align: center;font-weight: bold;'>----회원님의 현재예약 목록-----</span><hr>");
                        for(int i = 0; i < json.length(); i++)
                        {
                            str.append("<div style='text-align: center'>").append(json.getJSONObject(i).getString("HOSP_NAME")).append("<br/>").append(json.getJSONObject(i).getString("RES_DATE")).append(" ").append(json.getJSONObject(i).getString("RES_TIME")).append("<br/>예약번호:").append(json.getJSONObject(i).getString("RESERV_NO")).append("<hr></div>");
                        }
                        str.append("<div style='text-align:center'>[예약,예약취소]</div>");
                    }
                    catch(Exception e){e.printStackTrace();}
                }
                conn.disconnect();
            }
            catch(Exception e){e.printStackTrace();}

            return str.toString();
        }///////////doInBackground
    }///////////////LoginAsyncTask

    private class preListAsyncTask extends AsyncTask<String,Void,String> {

        @Override
        protected void onPreExecute() {

        }///////////onPreExecute

        @Override
        protected String doInBackground(String... params) {
            StringBuffer buf = new StringBuffer();
            StringBuilder str = new StringBuilder();

            try {
                URL url = new URL(String.format("%s?mem_email=%s",params[0],params[1]));
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
                if(buf.length()>2) {
                    try {
                        JSONArray json = new JSONArray(buf.toString());
                        Log.i(TAG,json.toString());
                        str.append("<span style='text-align: center;font-weight: bold;'>--------현재 복용중인 약--------</span><hr>");
                        for(int i = 0; i < json.length(); i++)
                        {
                            str.append("<div style='text-align: center'>처방 날짜:").append(json.getJSONObject(i).getString("PRES_DATE").substring(0,10)).append("<br/> <hr>복용기간: ").append(json.getJSONObject(i).getString("DURATION")).append("일 <br/>복용중인 약<br/>").append(json.getJSONObject(i).getString("MEDI_NAME")).append("<br/><hr></div>");
                        }
                    }
                    catch(Exception e){e.printStackTrace();}
                }
                conn.disconnect();
            }
            catch(Exception e){e.printStackTrace();}

            return str.toString();
        }///////////doInBackground
    }

    private class resInsertAsyncTask extends AsyncTask<String,Void,String> {
        private String sendMsg, receiveMsg;
        @Override
        protected void onPreExecute() {

        }///////////onPreExecute

        @Override
        protected String doInBackground(String... params) {

            try {
                URL url = new URL(String.format("%s",params[0]));
                HttpURLConnection conn=(HttpURLConnection)url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "address="+params[1]+"&email="+params[2]+"&department="+params[3]+"&datepick="+params[4]+"&hourMinute="+params[5]+"&symptom="+params[6];
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
                conn.disconnect();
            }
            catch(Exception e){e.printStackTrace();}
            return receiveMsg;

        }///////////doInBackground
    }

    private class resDeleteAsyncTask extends AsyncTask<String,Void,String> {
        @Override
        protected void onPreExecute() {

        }///////////onPreExecute

        @Override
        protected String doInBackground(String... params) {
            StringBuffer buf = new StringBuffer();
            StringBuilder str = new StringBuilder();

            try {
                URL url = new URL(String.format("%s?reserv_no=%s",params[0],params[1]));
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

            return str.toString();
        }///////////doInBackground
    }
}

