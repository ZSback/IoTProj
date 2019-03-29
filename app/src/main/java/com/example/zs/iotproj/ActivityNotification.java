package com.example.zs.iotproj;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ActivityNotification extends AppCompatActivity {

    private TextView [] responseText = new TextView[5];
    public String responseForShow = "$$$$$$$$$$$$$$$$$$$$";

    public static int NumMail;
    public String username = "Bari";

    // Network constant
    // public String urlToSeverForNumMail = "https://3drfiyermc.execute-api.us-east-1.amazonaws.com/dev/getemailnum/single";
    public String urlToSeverForContent = "https://3drfiyermc.execute-api.us-east-1.amazonaws.com/dev/cs4764/getnonspam";
    public String urlToServerReport = "https://3drfiyermc.execute-api.us-east-1.amazonaws.com/dev/sendemailtodoorman";
    // public String messageSendForNumMail = "{\"username\": \"user1\"}";
    public String messageSendForContent = "{\"username\": \"user1\",\"username2\": \"user1\"}";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent intent1 = new Intent(ActivityNotification.this, ActivityHome.class);
                    startActivity(intent1);
                    return true;
                case R.id.navigation_notifications:
                    NumMail = MainActivity.NumMail;

                    ActivityNotification.CallAPI sendCommand = new ActivityNotification.CallAPI();
                    sendCommand.execute(urlToSeverForContent, messageSendForContent);
                    responseText[0].setText("Again Touch");
                    System.out.println("Again Touch");
                    return true;
                case R.id.navigation_settings:
                    Intent intent2 = new Intent(ActivityNotification.this, ActivitySetting.class);
                    startActivity(intent2);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        NumMail = MainActivity.NumMail;

        responseText[0] = (TextView) findViewById(R.id.notification_text_1);
        responseText[1] = (TextView) findViewById(R.id.notification_text_2);
        responseText[2] = (TextView) findViewById(R.id.notification_text_3);
        responseText[3] = (TextView) findViewById(R.id.notification_text_4);
        responseText[4] = (TextView) findViewById(R.id.notification_text_5);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        ActivityNotification.CallAPI sendCommand = new ActivityNotification.CallAPI();
        sendCommand.execute(urlToSeverForContent, messageSendForContent);

        // Button settings
        Button report = (Button) findViewById(R.id.reportToBuilding);
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportToBuilding(v);
            }
        });

        System.out.println("crate"+responseForShow);
    }

    //////////////////////////////////////////////////////////////////
    public void reFormatInformation(String response){
        String a = "text:";
        //String b = "------------";
        String[] array = response.split(a);
        int index1 = response.indexOf("emailnum");
        int index2 = response.indexOf("statusCode");
        NumMail = Integer.valueOf(response.substring(index1+11,index2-3));
        System.out.println("\n\nNumMail = \n\n"+NumMail+"\n"+response);
        if (NumMail != 0) {
            for (int i = 1; i < NumMail+1; i++) {
                //array[i] = array[i].substring(array[i].indexOf(username));
                //array[i] = array[i].substring(array[i].indexOf("-"));
                //String[] comingMsg = array[i].split(b);
                //array[i] = array[i].replaceAll("(^| )[^ ]*[^A-Za-z ][^ ]*(?=$| )", "");
                array[NumMail+1-i] = array[NumMail+1-i].replaceAll("[\",\"]", "");
                array[NumMail+1-i] = array[NumMail+1-i].replaceAll("[\\\\]", "");
                array[NumMail+1-i] = array[NumMail+1-i].replaceAll("emailnum", "");
                array[NumMail+1-i] = array[NumMail+1-i].replaceAll("\\d+", "");
                array[NumMail+1-i] = array[NumMail+1-i].replaceAll("statusCode", "");
                array[NumMail+1-i] = array[NumMail+1-i].replaceAll(": ", "");

                responseText[i-1].setText(array[NumMail+1-i]);
                if(i == 4){
                    break;
                }
            }
            if(NumMail>4){
                responseText[4].setText("There are more than 4 letters\n in the Smart Mailbox.");
            }
            else{
                responseText[4].setText("—⭐—");
            }
        }
        else{
            responseText[1].setText("There has no letter.");
            for (int i = 1; i < 5; i++) {
                responseText[i].setText("");
            }
        }
    }

    //////////////////////////////////////////////////////////////////
    public void reportToBuilding(View view) {
        ActivityNotification.CallAPI sendCommand = new ActivityNotification.CallAPI();
        sendCommand.execute(urlToServerReport, "");
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
            System.out.println("onPostExecute"+response);
            responseText[4].setText("successfully");
            //responseText[0].setText("onPostExecute"+response);
            if(response.indexOf("emailnum") != -1){
                reFormatInformation(response);
            }
            super.onPostExecute(response);
            Log.v("result",response);
        }
    }
}
