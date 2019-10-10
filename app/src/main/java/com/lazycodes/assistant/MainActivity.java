package com.lazycodes.assistant;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class MainActivity extends Activity implements RecognitionListener {

    Button btnRec;
    TextView dispTxt;
    String str;
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private SpeechRecognizer recognizer;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnRec = findViewById(R.id.btnRec);
        dispTxt = findViewById(R.id.dsplyRslt);
        new SetupSphinx(this).execute();
        btnRec.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        recognizer.stop();
                        dispTxt.setText("Listening...");
                        recognizer.startListening("lm");
                        break;
                    case MotionEvent.ACTION_UP:
                        recognizer.stop();
                        break;

                }
                return false;
            }
        });
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d("AfterListening", "onBeginningOfSpeech Run");
    }

    @Override
    public void onEndOfSpeech() {
        Log.d("AfterListening", "onEndOfSpeech Run");
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null){
            Log.d("AfterListening","Hypothesis is NULL");
            return;
        }
        str = hypothesis.getHypstr();
        Log.d("AfterListening","Current output: " + str);
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null){
            dispTxt.setText("");
            String res = hypothesis.getHypstr();
            Log.d("AfterListening", "Final Output: "+res);
            dispTxt.setText(res);
        }
    }

    @Override
    public void onError(Exception e) {
        Log.d("AfterListening", "onError Run");
    }

    @Override
    public void onTimeout() {
        Log.d("AfterListening", "onTimeOut Run");
    }


    private static class SetupSphinx extends AsyncTask<Void, Void, Exception>{
        WeakReference<MainActivity> activityWeakReference;
        SetupSphinx(MainActivity activity){
            this.activityWeakReference = new WeakReference<>(activity);
        }
        @Override
        protected Exception doInBackground(Void... voids) {
            try {
                Assets assets = new Assets(activityWeakReference.get());
                File assetDir = assets.syncAssets();
                activityWeakReference.get().setupRecognizer(assetDir);
            }catch (IOException e){
                e.printStackTrace();
                Log.d("AsyncTask","Faild in DoInBG bla bla bla");
            }
            return null;
        }

        protected void onPostExecute(Exception result) {
            if (result != null) {
                activityWeakReference.get().dispTxt.setText("Failed to init recognizer " + result);
            }
            else {
                Log.d("AsyncTask","Successfully Loaded Models");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new SetupSphinx(this).execute();
            } else {
                finish();
            }
        }
    }

    private void setupRecognizer(File assetsDir) throws IOException {

        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "model"))
                .setDictionary(new File(assetsDir, "test2.dic"))

                .setRawLogDir(assetsDir)

                .getRecognizer();
        recognizer.addListener(this);
        Log.d("setupRecognozer","Recognizer Initialized");

        File languageModel = new File(assetsDir, "test2.lm.DMP");
        recognizer.addNgramSearch("lm", languageModel);

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }
}
