package com.kosmo.homespital;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.kosmo.homespital.MyPageActivity;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.StringTokenizer;

//https://firebase.google.com/docs/cloud-messaging/android/receive?hl=ko
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final String TAG="com.kosmo.fcmessaging";
    //포그라운드 상태인 앱에서 알림 메시지(FCM에서 자동처리)수신하려면 onMessageReceived 콜백 오버라이딩

    //파이어베이스 콘솔에서 알림 메시지 및 데이타 메시지를 보낼때
    //포그라운드 일때:모든 경우 onMessageRecieved가 호출됨
    //백그라운드일때:데이타메시지를 포함한 경우에만 호출된다

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.i(TAG,"From:"+remoteMessage.getFrom());
        //알림메시지:키값이 정해져 있다 :제목-title,내용-body 예:{"notification":{"title":"입력한 제목","body":"입력한 내용"}
        if(remoteMessage.getNotification()!=null){
            Log.i(TAG,"알림 제목:"+remoteMessage.getNotification().getTitle());
            Log.i(TAG,"알림 텍스트:"+remoteMessage.getNotification().getBody());
        }
        //데이타 메시지(추가 옵션인 키/값항목에 입력한 데이타)
        if(remoteMessage.getData().size() >0){
            Log.i(TAG,"데이타 메시지:"+remoteMessage.getData());
            Log.i(TAG,"데이타 메시지 제목:"+remoteMessage.getData().get("title"));
            Log.i(TAG,"데이타 메시지 내용:"+remoteMessage.getData().get("message"));
        }
        //데이타 메시지가 있는 경우
        //포그라운드일때:알림도 데이타 메시지로 변경
        //백그라운드 일때:노티는 알림메시지가 뜨고 데이터 메시지는 MainActivity의 인텐트 부가 정보로 전송
        if(remoteMessage.getData().size()>0){
            //추가 옵션에서 [키와 값]에 입력한 경우
            //키값을 noti_title 및 noti_message로 입력한 경우
            showNotification(
                    remoteMessage.getData().get("noti_title"),
                    remoteMessage.getData().get("noti_message"));

        }
        else if(remoteMessage.getNotification()!=null){
            showNotification(
                    remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody());
        }

    }///////////////onMessageReceived
    private void showNotification(String title,String message){
        Intent intent = new Intent(this, MyPageActivity.class);
        //인텐트에 부가정보 저장
        intent.putExtra("noti_title",title);
        intent.putExtra("noti_message",message);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder noBuilder=
                new NotificationCompat.Builder(this,"com.kosmo.homespital")
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setSound(defaultSoundUri)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setContentIntent(pendingIntent);
        //InboxStyle스타일 추가-여러줄의 내용입력시 표시하기 위함
        //한줄 짜리 내용입력시는 생략 가능
        NotificationCompat.InboxStyle inboxStyle=
                new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(title);
        StringTokenizer tokenizer= new StringTokenizer("\r\n");
        while(tokenizer.hasMoreTokens()){
            inboxStyle.addLine(tokenizer.nextToken());
        }
        noBuilder.setStyle(inboxStyle);
        //InboxStyle스타일 추가 끝
        NotificationManager notificationManager=
                (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        //오레오 부터 아래 코드 추가해야 함 시작
        int importace = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel notificationChannel =
                new NotificationChannel("com.kosmo.homespital","CHANNEL_KOSMO",importace);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);
        notificationChannel.setShowBadge(true);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        notificationChannel.setVibrationPattern(new long[]{100,200,300,400,500,400,300,200,400});
        notificationManager.createNotificationChannel(notificationChannel);
        //오레오 부터 아래 코드 추가해야 함 끝

        notificationManager.notify(101,noBuilder.build());
    }/////////////showNotification
    //토큰이 새롭게 갱신 될때마다 아래메소드 자동 호출
    //예:앱 삭제후 재설치시 혹은 데이타 삭제시 등
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        //설정에서 앱의 데이타 삭제후 LOGCAT확인
        Log.i(TAG,"FCM token:"+token);
        //생성 등록된 토큰을 서버에 보내기.
        sendRegistrationToServer(token);


    }/////////////
    //토큰을 웹서버에 전송하기 위해 아래 메소드 구현
    private void sendRegistrationToServer(String token) {
        new AsyncToServer().execute("https://homespital.ngrok.io/proj/Android/Auth/myPage/recentApt","token="+token);
    }
    private class AsyncToServer extends AsyncTask<String,Void,Void>{
        @Override
        protected Void doInBackground(String... params) {
            //[POST방식]
            try{
                URL url = new URL(params[0]);
                HttpURLConnection conn= (HttpURLConnection)url.openConnection();
                //연결설정
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                //서버로 보낼 데이타 설정
                OutputStream out= conn.getOutputStream();
                // Request Body에 Data 셋팅 및 서버로 전송.
                out.write(params[1].getBytes("UTF-8"));
                out.flush();
                out.close();
                //※getResponseCode() 나 getInputStream()는 서버로부터
                // 응답을 받을때
                //서버에 요청 및 응답코드 받기
                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                    Log.i(TAG,"서버 전송 성공");
                }
                else{
                    Log.i(TAG,"서버 전송 실패");
                }
            }
            catch(Exception e){e.printStackTrace();}
            return null;
        }
    }
}/////////////////class
