package com.example.zs.iotproj;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    public BottomNavigationView navigation;
    // Only get lantitude and longitude in MainActivity
    //public String Lanti_Longi = "";
    public static double mailboxlatitude = 0;
    public static double mailboxlongitude = 0;
    public static double phonelatitude = 40.8141824;
    public static double phonelongitude = -73.9622912;
    public static int NumMail = 0;

    // Network constant
    public String urlToSeverForBoardLoc = "https://3drfiyermc.execute-api.us-east-1.amazonaws.com/dev/cs4764/single";
    public String urlToSeverForNumMail  = "https://3drfiyermc.execute-api.us-east-1.amazonaws.com/dev/getemailnum/single";
    public String urlToAPI_Geo  = "https://www.googleapis.com/geolocation/v1/geolocate?key=AIzaSyBbGEjWKnZGQmE12n6ieVIGi0IUxqUFYs4";

    public String messageSend = "";
    public String messageSendForBoardLoc = "{\"board\": \"board1\"}";
    public static String messageSendForNumMail = "{\"username\": \"user1\"}";

    // Alarm variables
    public boolean silent;
    public static Vibrator vibrator;
    public static ToneGenerator toneGen1;

    public Handler handler ;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent intent1 = new Intent(MainActivity.this, ActivityHome.class);
                    startActivity(intent1);
                    return true;
                case R.id.navigation_notifications:
                    Intent intent3 = new Intent(MainActivity.this, ActivityNotification.class);
                    startActivity(intent3);
                    return true;
                case R.id.navigation_settings:
                    Intent intent2 = new Intent(MainActivity.this, ActivitySetting.class);
                    startActivity(intent2);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Vibrate setting
        vibrator = (Vibrator)this.getSystemService(Context.VIBRATOR_SERVICE);
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

        mTextMessage = (TextView) findViewById(R.id.HelloText);
        //mTextMessage.setText("hello world!");
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        MainActivity.CallAPI sendCommand2;
        sendCommand2 = new MainActivity.CallAPI();
        sendCommand2.execute(urlToSeverForBoardLoc, messageSendForBoardLoc);


        handler=new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 0) {
                    // Alarm on or off
                    SharedPreferences settings = getSharedPreferences("com.example.xyz", 0);
                    silent = settings.getBoolean("Alarm_Switch", true);

                    if (silent){
                        // Geolocation
                        MainActivity.CallAPI sendCommand;
                        sendCommand = new MainActivity.CallAPI();
                        sendCommand.execute(urlToAPI_Geo, messageSend);
                        System.out.println("\nShould alarm or not?\nlan&lon = "+phonelatitude+phonelongitude);

                        MainActivity.CallAPI sendCommand3;
                        sendCommand3 = new MainActivity.CallAPI();
                        sendCommand3.execute(urlToSeverForNumMail, messageSendForNumMail);
                        System.out.println("\nShould alarm or not?\nNumMail = "+NumMail);

                        double lan_distance = Math.abs(phonelatitude - mailboxlatitude);
                        double lon_distance = Math.abs(phonelongitude - mailboxlongitude);
                        System.out.println("\nShould alarm or not?\ndistance = "+lan_distance+" "+lon_distance);

                        // the distance within 0.0015 is about the distance of a block
                        if ((NumMail>0) && (lan_distance<0.0015) && (lon_distance<0.0015)){
                            vibrator.vibrate(new long[]{300,500},0);
                            toneGen1.startTone(ToneGenerator.TONE_CDMA_LOW_SS, 8000);
                            System.out.println("mHandler True");
                        }
                    } else {
                        vibrator.cancel();
                        toneGen1.stopTone();
                        //vibrator.vibrate(new long[]{1,0},-1);
                        System.out.println("mHandler False");
                    }
                    System.out.println("Main_hanlder");
                    Log.d("MainActivity", "123456");
                }
            }
        };

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // (1) 使用handler发送消息
                Message message=new Message();
                message.what=0;
                handler.sendMessage(message);
            }
        },0,30000);
    }

    private void createNotificationChannel(String NumMail) {
        /*navigation = (BottomNavigationView) findViewById(R.id.navigation);
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setIcon(getResources().getDrawable(R.drawable.ic_msgcoming_24dp));*/
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        // Notification test
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle("Smart Mailbox")
                .setContentText(NumMail)
                //.setStyle(new NotificationCompat.BigTextStyle()
                       // .bigText("Much longer text that cannot fit one line..."))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        Intent createNotification = new Intent(this, ActivityNotification.class);
        PendingIntent pendingNotification = PendingIntent.getActivity(this, 0, createNotification, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingNotification);

        // Add as notification
        NotificationManager notiManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "YOUR_CHANNEL_ID";
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notiManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        }

        notiManager.notify(0, mBuilder.build());
    }

    // Reformation
    // Reformation
    public void reFormation(String response){
        if(response.indexOf("location") != -1) {
            int pre = response.indexOf(":");

            String lan = response.substring(pre+1);
            int lan_1 = lan.indexOf(":"); // lantitude start
            int lan_2 = lan.indexOf(","); // lantitude end
            lan = lan.substring(lan_1+1,lan_2); // substring

            String lon = response.substring(pre+1).substring(lan_2+1);
            int lon_1 = lon.indexOf(":"); // longitude start
            int lon_2 = lon.indexOf(","); // longitude end
            lon = lon.substring(lon_1+1,lon_2-2); // substring

            phonelatitude = Double.valueOf(lan);
            phonelongitude = Double.valueOf(lon);

            System.out.println("Current Location"+phonelatitude+phonelongitude);
        }
        // {"emailNum": "3"}
        else if(response.indexOf("emailNum") != -1){
            int NumStart = response.indexOf("emailNum");
            int NumEnd = response.indexOf("\"}");
            int tempNumMail = Integer.valueOf(response.substring(NumStart+11,NumEnd));
            if (tempNumMail != NumMail){
                NumMail = tempNumMail;
                if (silent) {
                    vibrator.vibrate(200);
                    toneGen1.startTone(ToneGenerator.TONE_CDMA_LOW_SS, 200);
                }
                if (tempNumMail == 0){
                    createNotificationChannel("Letters are picked up!");
                }
                else if (tempNumMail == 1) {
                    createNotificationChannel("New letter coming!\nThere is "+tempNumMail+" letter in Smart Mailbox");
                }
                else {
                    createNotificationChannel("New letter coming!\nThere are "+tempNumMail+" letters in Smart Mailbox");
                }
            }
            System.out.println("Current NumMail"+tempNumMail+"\n"+NumMail);
        }
        else if(response.indexOf("boardloc") != -1){
            int loc_1 = response.indexOf("boardLatitude");
            int loc_2 = response.indexOf("boardLongitude");
            int loc_3 = response.indexOf("\"}");

            mailboxlatitude = Double.valueOf(response.substring(loc_1+13,loc_2));
            mailboxlongitude = Double.valueOf(response.substring(loc_2+14,loc_3));

            System.out.println("\n\n\n\nBoard Location" + mailboxlatitude + mailboxlongitude);
        }
    }


    //////////////////////////////////////////////////////////////////
    public class CallAPI extends AsyncTask<String, String, String> {

        /*public CallAPI(){
            //set context variables if required
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }*/
        //public String response = "Successfully";

        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0]; // URL to call
            String data = params[1]; //data to post
            OutputStream out = null;
            String response = "";
            try {
                //URL url = new URL(host);
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                // https://www.wikihow.com/Execute-HTTP-POST-Requests-in-Android
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Key","Value");
                urlConnection.setRequestProperty("Content-Type","application/json");
                urlConnection.setDoOutput(true);

                out = new BufferedOutputStream(urlConnection.getOutputStream());
                /*
                //set read time = 10s
                urlConnection.setReadTimeout(10000);
                //set connect time out = 15s
                urlConnection.setConnectTimeout(15000);
                //set method = get
                urlConnection.setRequestMethod("GET");*/


                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                writer.write(data);
                writer.flush();
                writer.close();
                out.close();

                urlConnection.connect();
                // int responseCode=urlConnection.getResponseCode();
                //   if (responseCode == HttpsURLConnection.HTTP_OK)
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                while ((line = br.readLine()) != null) {
                    Log.v("uiyfiu",line) ;
                    response += line;
                }
                urlConnection.disconnect();
                System.out.println("Success #1");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            //response = "changeable?";
            System.out.println("doInBackground");
            return response;
        }
        @Override
        protected void onPostExecute(String response) {
            System.out.println("Main_onPostExecute"+response);
            reFormation(response);
            super.onPostExecute(response);
            Log.v("result",response);
        }
    }
}
