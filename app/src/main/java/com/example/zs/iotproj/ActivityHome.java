package com.example.zs.iotproj;


import android.content.Intent;

import android.location.Address;
import android.location.Geocoder;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;


public class ActivityHome extends AppCompatActivity {
    // Network response
    private TextView responseText;
    public String responseForShow = "$$$$$$$$$$$$$$$$$$$$";

    // Date, position and weather
    private TextView tvTime;
    private TextView tvTime2;
    private TextView position;
    private TextView weather;
    public Geocoder geocoder;
    public double latitude;
    public double longitude;

    // Network constant
    public String urlToSever = "https://3drfiyermc.execute-api.us-east-1.amazonaws.com/dev/getemailnum/single";
    // WeatherURL following
    public String urlToAPI_Wea  = "http://api.openweathermap.org/data/2.5/weather?lat=40.8141824&lon=-73.9622912&APPID=66ebaa306a862a64835746f948c13058";
    // PositionURL following
    public String urlToAPI_Geo  = "https://www.googleapis.com/geolocation/v1/geolocate?key=AIzaSyBbGEjWKnZGQmE12n6ieVIGi0IUxqUFYs4";

    public String messageSend = "";
    //public String messageSend = "{\"username\":\"user_id1\"}";
    //public String messageSend = "Hello World_Home Page";


    // Alarm settings
    public boolean silent;
    public Vibrator vibrator;
    public ToneGenerator toneGen1;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    ActivityHome.CallAPI sendCommand = new ActivityHome.CallAPI();
                    sendCommand.execute(urlToAPI_Geo, messageSend);
                    responseText.setText("Please refresh");
                    System.out.println("Again Touch");
                    return true;
                case R.id.navigation_notifications:
                    Intent intent3 = new Intent(ActivityHome.this, ActivityNotification.class);
                    startActivity(intent3);
                    return true;
                case R.id.navigation_settings:
                    Intent intent2 = new Intent(ActivityHome.this, ActivitySetting.class);
                    startActivity(intent2);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        geocoder = new Geocoder(this, Locale.getDefault());
        responseText = (TextView) findViewById(R.id.home_text_1);
        tvTime = (TextView) findViewById(R.id.mytime);
        tvTime2 = (TextView) findViewById(R.id.mytime2);
        position = (TextView) findViewById(R.id.position);
        weather = (TextView) findViewById(R.id.weather);

        new TimeThread().start(); // Create a new thread

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // following three line should be together, otherwise: Cannot execute task: the task is already running.
        ActivityHome.CallAPI sendCommand;
        sendCommand = new ActivityHome.CallAPI();
        sendCommand.execute(urlToAPI_Geo, messageSend);

        System.out.println("crate"+responseForShow);
    }


    //////////////////////////////////////////////////////////////////////////////////////////
    // Reformation
    public void reFormatLocation(String response){
        int pre = response.indexOf(":");

        String lan = response.substring(pre+1);
        int lan_1 = lan.indexOf(":"); // lantitude start
        int lan_2 = lan.indexOf(","); // lantitude end
        lan = lan.substring(lan_1+1,lan_2); // substring

        String lon = response.substring(pre+1).substring(lan_2+1);
        int lon_1 = lon.indexOf(":"); // longitude start
        int lon_2 = lon.indexOf(","); // longitude end
        lon = lon.substring(lon_1+1,lon_2-2); // substring
        System.out.println("onPostExecute--lan:"+lan+"lon:"+lon);
        latitude = Double.valueOf(lan);
        longitude = Double.valueOf(lon);
        try {
            List<Address> addresses=geocoder.getFromLocation(latitude, longitude, 1);
            StringBuilder stringBuilder=new StringBuilder();
            if(addresses.size()>0){
                Address address=addresses.get(0);
                for(int i=0;i<address.getMaxAddressLineIndex();i++){
                    stringBuilder.append(address.getAddressLine(i)).append("\n");
                }
                stringBuilder.append(address.getLocality()).append(",");
                if (address.getPostalCode() == null) stringBuilder.append("10027");
                else stringBuilder.append(address.getPostalCode());
                position.setText(stringBuilder);
                stringBuilder.append("/").append(address.getCountryCode()).append("_");
                stringBuilder.append(address.getCountryName()).append("_");
                System.out.println(stringBuilder.toString());
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Toast.makeText(this, "error", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        ActivityHome.CallAPI sendCommand;
        sendCommand = new ActivityHome.CallAPI();
        urlToAPI_Wea  = "http://api.openweathermap.org/data/2.5/weather?lat="+latitude+"&lon="+longitude+"&APPID=66ebaa306a862a64835746f948c13058";
        sendCommand.execute(urlToAPI_Wea, messageSend);

        ActivityHome.CallAPI sendCommand2;
        //messageSend = ;
        sendCommand2 = new ActivityHome.CallAPI();
        sendCommand2.execute(urlToSever, MainActivity.messageSendForNumMail);
    }

    public void reFormatWeather(String response){
        int temp_1 = response.indexOf("temp");
        int temp_2 = response.indexOf("pressure");
        String temp = response.substring(temp_1+6,temp_2-2);

        int desc_1 = response.indexOf("description");
        int desc_2 = response.indexOf("icon");
        String desc = response.substring(desc_1+14,desc_2-3);

        double Celsius = Double.valueOf(temp)-273.15;
        temp = String.format("%.1f", Celsius);

        weather.setText(temp+" °C    "+desc);
        // Double.toString() is also accessible
    }

    public void reFormatInformation(String response){
        int NumStart = response.indexOf("emailNum");
        int NumEnd = response.indexOf("\"}");
        MainActivity.NumMail = Integer.valueOf(response.substring(NumStart+11,NumEnd));
        if(MainActivity.NumMail>1){
            responseText.setText("There are  "+MainActivity.NumMail+"  letters");
        }
        else{
            responseText.setText("There is  "+MainActivity.NumMail+"  letter");
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////



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
            responseForShow = response;
            System.out.println("onPostExecute"+response);
            if(response.indexOf("location") != -1) {
                reFormatLocation(response);
            }
            else if(response.indexOf("description") != -1){
                reFormatWeather(response);
            }
            else if(response.indexOf("emailNum") != -1){
                reFormatInformation(response);
            }
            super.onPostExecute(response);
            Log.v("result",response);
        }
    }

    class TimeThread extends Thread {
        @Override
        public void run() {
            do {
                try {
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = 1;  //消息(一个整型值)
                    mHandler.sendMessage(msg);// 每隔1秒发送一个msg给mHandler
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (true);
        }
    }

    // Refresh in main thread
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    long sysTime = System.currentTimeMillis();
                    CharSequence sysTimeStr = DateFormat.format("HH : mm", sysTime);
                    tvTime.setText(sysTimeStr); //update time
                    sysTimeStr = DateFormat.format("E, MMM dd  ", sysTime);
                    tvTime2.setText(sysTimeStr);
                    break;
                default:
                    break;

            }
        }
    };


}

