package com.example.vhchat;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SoundPool.OnLoadCompleteListener {
    TextView textView;
    static EditText editText;
    Button buttonSend;
    String serverWord = "";
    final static String CLIENT_NAME = "Papa";
    private final int SERVER_PORT = 7777;
    final String LOG_TAG = "myLogs";
    ArrayList<String> listOfServerWords = new ArrayList<>();
    SoundPool soundPool;
    int birdSound;

    private static Socket socket;

    private static BufferedReader bufferedReader;
    private static BufferedWriter bufferedWriter;

    Thread thread;

    boolean flagEditText = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main_two);

        textView = findViewById(R.id.textView);

        buttonSend = findViewById(R.id.buttonSend);
        buttonSend.setOnClickListener(this);

        editText = findViewById(R.id.editText);

        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(this);
        birdSound = soundPool.load(this, R.raw.bird_sound, 1);

        /////////////////
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket("192.168.0.105", SERVER_PORT);
                    bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                    bufferedWriter.write(CLIENT_NAME + "\n");
                    bufferedWriter.flush();

                    while (true){
                        if(socket.isConnected()){
                            serverWord = bufferedReader.readLine();
                        }
                        soundPool.play(birdSound,1,1,0,0,1);
                        listOfServerWords.add(serverWord);
                        if(listOfServerWords.size() > 7){
                            listOfServerWords.remove(0);
                        }
                    }
                } catch (IOException e) {
                    try {
                        socket.close();
                        bufferedReader.close();
                        bufferedWriter.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    e.printStackTrace();
                }finally {
                    try {
                        socket.close();
                        bufferedReader.close();
                        bufferedWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
        ////////////////

        final Handler handler = new Handler();
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String result = "";
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < listOfServerWords.size(); i++) {
                    stringBuilder.append(listOfServerWords.get(i));
                    stringBuilder.append("\n");
                }
                result = stringBuilder.toString();
                textView.setText(result);

                if(flagEditText){
                    editText.setText("");
                    flagEditText = false;
                }

                handler.postDelayed(this, 300);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.buttonConnect: {
                Log.d(LOG_TAG, "onClick : ");
                break;
            }
            case R.id.buttonSend: {
                try {
                    bufferedWriter.write(editText.getText().toString() + "\n");
                    bufferedWriter.flush();
                    flagEditText = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "MainActivity: onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "MainActivity: onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "MainActivity: onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "MainActivity: onStop()");
        try {
            socket.close();
            bufferedReader.close();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG,"onDestroy");
        try {
            thread.interrupt();
            socket.close();
            bufferedReader.close();
            bufferedWriter.close();
        } catch (IOException e) {
            Log.d(LOG_TAG, "onDestroy : socket close error");
            e.printStackTrace();
        }
    }

    @Override
    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {

    }
}