package com.example.zs.iotproj;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ActivitySetting extends AppCompatActivity {

    public Switch alarm_switch;
    public TextView muteOnOrOff;

    public boolean silent;
    public boolean firstTime = true;
    public Vibrator vibrator;
    public ToneGenerator toneGen1;

    // Network constant and variable
    public String responseForShow;
    public TextView responseText;
    public String urlToSeverSetKeyWords = "https://3drfiyermc.execute-api.us-east-1.amazonaws.com/dev/setspamornotkeyword/set";
    public String urlToSeverClearKeyWords = "https://3drfiyermc.execute-api.us-east-1.amazonaws.com/dev/setspamornotkeyword/delete";
    public String messageSend = "";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent intent1 = new Intent(ActivitySetting.this, ActivityHome.class);
                    startActivity(intent1);
                    return true;
                case R.id.navigation_notifications:
                    Intent intent3 = new Intent(ActivitySetting.this, ActivityNotification.class);
                    startActivity(intent3);
                    return true;
                case R.id.navigation_settings:
                    //
                    return true;
            }
            return false;
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        TextView title = (TextView) findViewById(R.id.setting_text_1);
        title.setText("Setting");
        responseText = (TextView) findViewById(R.id.response);

        // Button settings
        Button set = (Button) findViewById(R.id.Set_Keywords);
        Button clear = (Button) findViewById(R.id.Clear_Keywords);

        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setKeywords(v);
            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearKeywords(v);
            }
        });

        // Vibrate setting
        vibrator = (Vibrator)this.getSystemService(Context.VIBRATOR_SERVICE);
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Beep Sound
        muteOnOrOff = (TextView) findViewById(R.id.Alarm);
        alarm_switch = findViewById(R.id.AlarmSwitch);
        alarm_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // MODE_PRIVATE = 0;
                SharedPreferences settings = getSharedPreferences("com.example.xyz", MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("Alarm_Switch", isChecked);
                editor.apply();
                if (isChecked){
                    alarm_switch.setText("ON");
                    muteOnOrOff.setText("Vibrate");
                    silent = settings.getBoolean("Alarm_Switch", true);
                    toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                    if (!firstTime){
                        vibrator.vibrate(100);
                        toneGen1.startTone(ToneGenerator.TONE_CDMA_LOW_SS, 50);
                    }

                    firstTime = false;
                    System.out.println("mHandler True");
                    //vibrator.cancel();
                    //toneGen1.stopTone();

                    // vibrator and toneGen1 in MainActivity
                }else{
                    alarm_switch.setText("OFF");
                    vibrator.cancel();
                    toneGen1.stopTone();
                    muteOnOrOff.setText("Don't disturb");
                    System.out.println("mHandler False");
                    MainActivity.vibrator.cancel();
                    MainActivity.toneGen1.stopTone();
                }
            }
        });
        SharedPreferences settings = getSharedPreferences("com.example.xyz", 0);
        silent = settings.getBoolean("Alarm_Switch", true);
        alarm_switch.setChecked(silent);


        if (silent){
            muteOnOrOff.setText("Vibrate");
            alarm_switch.setText("ON");
            System.out.println("mHandler True");
        } else {
            muteOnOrOff.setText("Don't disturb");
            vibrator.cancel();
            alarm_switch.setText("OFF");
            System.out.println("mHandler False");
        }


        //new ActivitySetting.TimeThread().start(); // Create a new thread

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }


    /** Called when the user taps the Send button */
    public void setKeywords(View view) {
        EditText editText = (EditText) findViewById(R.id.Key_words);
        String message = editText.getText().toString();
        responseText.setText("set"+message);

        // Set key words
        messageSend = "{\"username\":\"user1\",\"keyword\":\"" + message + "\"}";
        ActivitySetting.CallAPI sendCommand;
        sendCommand = new ActivitySetting.CallAPI();
        sendCommand.execute(urlToSeverSetKeyWords, messageSend);
    }

    public void clearKeywords(View view) {
        EditText editText = (EditText) findViewById(R.id.Key_words);
        String message = editText.getText().toString();
        responseText.setText("clear"+message);

        // Set key words
        messageSend = "{\"username\":\"user1\",\"keyword\":\"" + message + "\"}";
        ActivitySetting.CallAPI sendCommand;
        sendCommand = new ActivitySetting.CallAPI();
        sendCommand.execute(urlToSeverClearKeyWords, messageSend);
    }

    // Reformation
    public void reFormatKeywords(String response){
        //response {"statusCode": 200,"body": "delete successfully!"}
        //response {"statusCode": 200,"body": "insert and set spam successfully!"}
        int msg_1 = response.indexOf("body");
        int msg_2 = response.indexOf("!");
        responseText.setText(response.substring(msg_1+9,msg_2+1));
        System.out.println("onPostExecute"+response);
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
            responseForShow = response;
            System.out.println("onPostExecute"+response);
            //responseText.setText("onPostExecute\n"+"200OK");
            reFormatKeywords(response);
            super.onPostExecute(response);
            Log.v("result",response);
        }
    }
}
