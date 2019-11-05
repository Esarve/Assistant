package com.lazycodes.assistant;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import androidx.appcompat.app.AppCompatActivity;
import com.lazycodes.assistant.RecyclerView.AllCommandListActivity;
import com.lazycodes.assistant.db.Command;
import com.lazycodes.assistant.db.CommandDatabase;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.UUID;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

public class MainActivity extends AppCompatActivity implements RecognitionListener {

    Button btnRec;
    TextView dispTxt;
    private ToggleButton saveTggBtn;
    private boolean boolSaveMode; // Check if save mood is on or not
    int pinNumber;
    boolean nothingSelect; // Determine if anything is selected on the Spinner or not
    String str;
    private SpeechRecognizer recognizer;


    // Bluetooth Staff
    Button btNotConnectedBtn,btConnectedBtn;
    private final String DEVICE_ADDRESS="98:D3:34:F5:9E:09";
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    boolean deviceConnected=false;
    byte buffer[];
    boolean stopThread;
    boolean isBooleanBTConncted =  false;




    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //main
        btnRec = findViewById(R.id.btnRec);
        dispTxt = findViewById(R.id.dsplyRslt);
        saveTggBtn = findViewById(R.id.saveToggleBtn);

        //Bluetooth Staff
        btNotConnectedBtn = findViewById(R.id.bluetoothNotConnectedBtn);
        btConnectedBtn = findViewById(R.id.bluetoothConnectedBtn);

        btNotConnectedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(BTinit())
                {
                    if(BTconnect())
                    {
                        setUiEnabled(true);
                        deviceConnected=true;
                        beginListenForData();
                        Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                        isBooleanBTConncted = true;
                    }

                }
            }
        });

        btConnectedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    stopBtConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // BT staff ends

        new SetupSphinx(this).execute();
        btnRec.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        recognizer.stop();
                        dispTxt.setText("Listening...");
                        recognizer.startListening("lm");
                        break;
                    case MotionEvent.ACTION_UP:
                        recognizer.stop();
/*                        if (boolSaveMode) {
                            ShowAlertDialog();
                        }
                        else{
                        if(isBooleanBTConncted){
                            // Method to send data
                            sendCommandToArduino();
                        }

                        else{
                            Toast.makeText(MainActivity.this, "Please Connect with bluetooth first", Toast.LENGTH_LONG).show();
                        }
                    }*/

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
                    boolSaveMode = false;
                }
            }
        });
    }

    //BT methods starts

    public void setUiEnabled(boolean bool)
    {

        if(bool){
            btConnectedBtn.setVisibility(View.VISIBLE);
            btNotConnectedBtn.setVisibility(View.GONE);
        }else{
            btConnectedBtn.setVisibility(View.GONE);
            btNotConnectedBtn.setVisibility(View.VISIBLE);
        }
    }

    public boolean BTinit()
    {
        boolean found=false;
        BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Device doesn't Support Bluetooth",Toast.LENGTH_SHORT).show();
        }
        if(!bluetoothAdapter.isEnabled())
        {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if(bondedDevices.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"Please Pair the Device first",Toast.LENGTH_SHORT).show();
        }
        else
        {
            for (BluetoothDevice iterator : bondedDevices)
            {
                if(iterator.getAddress().equals(DEVICE_ADDRESS))
                {
                    device=iterator;
                    found=true;
                    break;
                }
            }
        }
        return found;
    }

    public boolean BTconnect()
    {
        boolean connected=true;
        try {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            connected=false;
        }
        if(connected)
        {
            try {
                outputStream=socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream=socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        return connected;
    }

    void beginListenForData()
    {
        final Handler handler = new Handler();
        stopThread = false;
        buffer = new byte[1024];
        Thread thread  = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopThread)
                {
                    try
                    {
                        int byteCount = inputStream.available();
                        if(byteCount > 0)
                        {
                            byte[] rawBytes = new byte[byteCount];
                            inputStream.read(rawBytes);
                            final String string=new String(rawBytes,"UTF-8");
                            handler.post(new Runnable() {
                                public void run()
                                {
                                    //textView.append(string);
                                }
                            });

                        }
                    }
                    catch (IOException ex)
                    {
                        stopThread = true;
                    }
                }
            }
        });

        thread.start();
    }

    public void stopBtConnection() throws IOException {
        stopThread = true;
        outputStream.close();
        inputStream.close();
        socket.close();
        setUiEnabled(false);
        deviceConnected=false;
        Toast.makeText(this, "Connection Closed", Toast.LENGTH_SHORT).show();
        isBooleanBTConncted = false;
    }

    public void sendCommandToArduino() {

       // str.concat("\n");
        try {
            outputStream.write(str.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

// BT Staff Finished

    private void ShowAlertDialog() {
        View v = getLayoutInflater().inflate(R.layout.save_pop_up_resource, null);
        Spinner spinner = v.findViewById(R.id.pinSpinner);
        Button cancelPopup = v.findViewById(R.id.cancelPopUpBtn);
        Button savepopUp = v.findViewById(R.id.savePopUpBtn);
        TextView textViewPopup = v.findViewById(R.id.popUpTV);

        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        if (v.getParent()!= null){
            ((ViewGroup)v.getParent()).removeView(v);
        }
        alert.setView(v);
        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(false);

        textViewPopup.setText(str);

        //All spinner jobs
        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_spinner_dropdown_item,
                GetPinNumber.getPinNumber());
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if (GetPinNumber.getPinNumber().get(i) == "Choose One") {
                    nothingSelect = true;
                } else {
                    pinNumber = Integer.parseInt(GetPinNumber.getPinNumber().get(i));
                    nothingSelect = false;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                nothingSelect = true;
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
                if (!nothingSelect) {
                    Command currentCommand = new Command(str, pinNumber);
                    final long insertedRow = CommandDatabase.getInstance(MainActivity.this)
                            .getCommandDao()
                            .insertNewCommand(currentCommand);
                    if (insertedRow > 0) {
                        Toast.makeText(MainActivity.this, "Save", Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                    } else {
                        Toast.makeText(MainActivity.this, "Not saved", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(MainActivity.this, "Please select A PIN number first", Toast.LENGTH_LONG).show();
                }
            }
        });
        alertDialog.show();
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
        if (hypothesis == null) {
            Log.d("AfterListening", "Hypothesis is NULL");
            return;
        }
        //str = hypothesis.getHypstr();
        Log.d("AfterListening", "Current output: " + str);
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            dispTxt.setText("");
            String res = hypothesis.getHypstr();
            str = res;
            Log.d("AfterListening", "Final Output: " + res);
            dispTxt.setText(res);

            if (boolSaveMode) {
                ShowAlertDialog();
            }
            else{
                if(isBooleanBTConncted){
                    // Method to send data
                    sendCommandToArduino();
                }

                else{
                    Toast.makeText(MainActivity.this, "Please Connect with bluetooth first", Toast.LENGTH_LONG).show();
                }
            }

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

    public void AllCommandActivitySwitch(View view) {
        Intent intent = new Intent(this, AllCommandListActivity.class);
        startActivity(intent);
    }

    private static class SetupSphinx extends AsyncTask<Void, Void, Exception> {
        WeakReference<MainActivity> activityWeakReference;

        SetupSphinx(MainActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected Exception doInBackground(Void... voids) {
            try {
                Assets assets = new Assets(activityWeakReference.get());
                File assetDir = assets.syncAssets();
                activityWeakReference.get().setupRecognizer(assetDir);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("AsyncTask", "Faild in DoInBG bla bla bla");
            }
            return null;
        }

        protected void onPostExecute(Exception result) {
            if (result != null) {
                activityWeakReference.get().dispTxt.setText("Failed to init recognizer " + result);
            } else {
                Log.d("AsyncTask", "Successfully Loaded Models");
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
        Log.d("setupRecognizer", "Recognizer Initialized");

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