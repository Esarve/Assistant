package com.lazycodes.assistant;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RecognitionListener {

    Button btnRec;
    TextView dispTxt;
    private ToggleButton saveTggBtn;

    private boolean boolSaveMode; // Check if save mood is on or not
    int pinNumber;
    boolean nothingSelect; // Determine if anything is selected on the Spinner or not

    String str;
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private SpeechRecognizer recognizer;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //main
        btnRec = findViewById(R.id.btnRec);
        dispTxt = findViewById(R.id.dsplyRslt);
        saveTggBtn = findViewById(R.id.saveToggleBtn);


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
                        if(boolSaveMode)
                        {
/*
                            View v = getLayoutInflater().inflate(R.layout.save_pop_up_resource, null);

                            cancelPopup = v.findViewById(R.id.cancelPopUpBtn);
                            saveResultDialog.setContentView(R.layout.save_pop_up_resource);
                            saveResultDialog.show();
                            cancelPopup.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    saveResultDialog.dismiss();
                                }
                            });
                            Spinner spinner = v.findViewById(R.id.pinSpinner);
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.support_simple_spinner_dropdown_item,
                                    getResources().getStringArray(R.array.pinNo) );

                            spinner.setAdapter(adapter);*/

                            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                            View v = getLayoutInflater().inflate(R.layout.save_pop_up_resource, null);

                            Spinner spinner = v.findViewById(R.id.pinSpinner);
                            Button cancelPopup = v.findViewById(R.id.cancelPopUpBtn);
                            Button savepopUp = v.findViewById(R.id.savePopUpBtn);
                            TextView textViewPopup = v.findViewById(R.id.popUpTV);
// testing Commit

                            alert.setView(v);
                            final AlertDialog alertDialog = alert.create();
                            alertDialog.setCanceledOnTouchOutside(false);

                            textViewPopup.setText(str);


                           //All spinner jobs
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this,
                                    android.R.layout.simple_spinner_dropdown_item,
                                    getPinNumber());
                            spinner.setAdapter(adapter);

                            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                                    if (getPinNumber().get(i)=="Choose One"){
                                        nothingSelect =  true;
                                    }

                                    else {
                                        pinNumber = Integer.parseInt(getPinNumber().get(i));
                                        Log.d("PIN", "onItemSelected: "+pinNumber);
                                        nothingSelect = false;
                                    }

                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView) {
                                    nothingSelect =  true;
                                }
                            });



                            //Save and cancel Button
                            cancelPopup.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    alertDialog.dismiss();
                                }
                            });

                            savepopUp.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if(!nothingSelect){
                                        // We will save the pin
                                    }
                                }
                            });
                            alertDialog.show();

                        }

                        break;

                }
                return false;
            }
        });

        saveTggBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                   boolSaveMode = true;

                } else {
                   boolSaveMode =  false;
                }
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

    private List<String> getPinNumber()
    {
        ArrayList<String> pinNumber = new ArrayList<>();

        pinNumber.add("Choose One");
        pinNumber.add("0");
        pinNumber.add("1");
        pinNumber.add("2");
        pinNumber.add("3");
        pinNumber.add("4");
        pinNumber.add("5");
        pinNumber.add("6");
        pinNumber.add("7");
        pinNumber.add("8");
        pinNumber.add("9");
        pinNumber.add("10");
        pinNumber.add("11");
        pinNumber.add("12");
        pinNumber.add("13");

        return pinNumber;

    }

}
