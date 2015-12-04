package motionspeak.csc212.motionspeak;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.glass.widget.CardScrollView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class MainActivity extends Activity implements SensorEventListener {

    private boolean isRunning = false;
    private Timer timer;
    private Timer hashTimer;
    private Timer iconTimer;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private float mPrevMagField;
    private float mMagField;
    private float baseValue;
    private boolean baseValueBoolean = false;
    private boolean isMoving = false;
    private float[] mPrevMagValues = new float[3];
    private ArrayList<Boolean> isMovingBooleans = new ArrayList<>();
    private TextView mTextView;
    private ImageView mImageView;
    private String sensorValues = "";
    private String IDNumber;
    private JSONArray valuesArray;

    /**
     * {@link CardScrollView} to use as the main content view.
     */
    private CardScrollView mCardScroller;

    private View mView;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.text_view_movement);
        mImageView = (ImageView) findViewById(R.id.icon_movement);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);



        if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }

        mTextView.setText("Tap to start!");

        //motionSensing();

    }

    public void motionSensing(){
        timer = new Timer();
        hashTimer = new Timer();
        iconTimer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                if (mMagField > (baseValue + 15) || mMagField < (baseValue - 15)) {
                    Log.d("MainActivity", "YOU ARE MOVING (if statement)");
                    isMoving = true;
                    listBooleans(isMoving);
                } else {
                    Log.d("MainActivity", "You're apparently not moving");
                    isMoving = false;
                    listBooleans(isMoving);
                }
                if (baseValueBoolean == false && mMagField != 0.0) {
                    baseValue = mMagField;
                    baseValueBoolean = true;
                }
                updatePrevMagValues(mMagField);

                numInRange();
                sensorValues = sensorValues + System.currentTimeMillis() + " " + mMagField + ",";

            }
        }, 0, 500);

        iconTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateIcon();
                    }
                });
            }
        }, 0, 1000);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                hashTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateCard();
                                isMovingBooleans.clear();
                            }
                        });

                    }
                }, 0, 5000);
            }
        }, 10000);

    }

    @Override
    protected void onResume() {
        super.onResume();
        timer = new Timer();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        cancelEverything();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Do something I guess if the accuracy changes
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        //Light sensors returns a single value, some return 3 values (for each axis)
        mPrevMagField = mMagField;
        mMagField = event.values[0];

    }

    public void updateIcon(){
        if(isMoving == true){
            mImageView.setImageResource(R.drawable.ic_directions_run_white_24dp);
        } else if (isMoving == false) {
            mImageView.setImageResource(R.drawable.ic_directions_run_black_24dp);
        }
    }

    public void updateCard() {
        if (isMovingBooleans.contains(true)) {
            //mTextView.setText("You are moving!");
            mTextView.setText("");
            Log.d("Main", "you are supposedly moving update view");
        } else if (!isMovingBooleans.contains(true)){
            mTextView.setText("Move a little!");
            Log.d("Main", "you aRENT' !!moving update view");

        }
    }

    public void cancelEverything(){
        timer.cancel();
        hashTimer.cancel();
        iconTimer.cancel();
    }

    public void updatePrevMagValues(float mf) {
        mPrevMagValues[2] = mPrevMagValues[1];
        mPrevMagValues[1] = mPrevMagValues[0];
        mPrevMagValues[0] = mf;
    }

    public void numInRange() {
        int maxIndex = getMaxIndex();
        int index1, index2;
        if (maxIndex == 0) {
            index1 = 1;
            index2 = 2;
        } else if (maxIndex == 1) {
            index1 = 0;
            index2 = 2;
        } else {
            index1 = 0;
            index2 = 1;
        }

        if (mPrevMagValues[maxIndex] > 0) {
            if (mPrevMagValues[maxIndex] - mPrevMagValues[index1] <= 5 && mPrevMagValues[maxIndex] - mPrevMagValues[index2] <= 5) {
                baseValue = mPrevMagValues[2];
            }
        } else if (mPrevMagValues[maxIndex] < 0) {
            if (mPrevMagValues[maxIndex] - mPrevMagValues[index1] >= -5 && mPrevMagValues[maxIndex] - mPrevMagValues[index2] >= -5) {
                baseValue = mPrevMagValues[2];
            }
        }
    }

    public int getMaxIndex() {
        int maxIndex;
        if (Math.max(mPrevMagValues[0], mPrevMagValues[1]) == mPrevMagValues[0]) {
            maxIndex = 0;
        } else {
            maxIndex = 1;
        }
        if (Math.max(mPrevMagValues[maxIndex], mPrevMagValues[2]) == mPrevMagValues[2]) {
            maxIndex = 2;
        }
        return maxIndex;
    }

    public void listBooleans(boolean moving){
       isMovingBooleans.add(moving);
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent event){
        if(keycode == KeyEvent.KEYCODE_DPAD_CENTER){
            if(isRunning == false){
                motionSensing();
                mTextView.setText("");
                isRunning = true;
            } else if(isRunning == true){
                cancelEverything();
                valuesArray = parseTextIntoJSON(sensorValues);

                JSONObject obj = new JSONObject();
                try {
                    obj.put("result", valuesArray);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //print out JSON Object
                Log.d("Main", obj.toString());

//                try {
//                    postJSON("http://google.com", valuesArray);
//                } catch (IOException ie) {
//                    ie.printStackTrace();
//                }

                mTextView.setText(IDNumber);
            }
            return true;
        }
        return super.onKeyDown(keycode, event);
    }


//    public void writeTextFile(){
//
//        try {
//
//            UUID uuid = UUID.randomUUID();
//            String UUIDString = uuid.toString();
//            String split[] = UUIDString.split("-");
//            IDNumber = split[0] + ".txt";
//
//            File root = android.os.Environment.getExternalStorageDirectory();
//            File dir = new File (root.getAbsolutePath() + "/MotionSpeak");
//            File f = new File(dir, IDNumber);
//
//            f.createNewFile();
//            Log.d("file path", "result"+f.getAbsolutePath());
//            Log.d("file created", "result"+f.createNewFile());
//            FileOutputStream fOut = new FileOutputStream(f);
//            OutputStreamWriter outputWriter=new OutputStreamWriter(fOut);
//
//            outputWriter.write(sensorValues);
//            /** Closing the writer object */
//            outputWriter.close();
//            Log.d("success", "success"+ Environment.getExternalStorageState()+Environment.getStorageState(dir));
//        }
//        catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }

    public JSONArray parseTextIntoJSON(String data){
        String [] values = data.split(",");
        JSONObject [] jsonValues = new JSONObject[values.length];
        for(int i = 0; i < values.length; i++){
            JSONObject jo = new JSONObject();
            try {
                String [] separatedValues = values[i].split(" ");
                jo.put("time", separatedValues[0]);
                jo.put("value", Float.valueOf(separatedValues[1]));
                jsonValues[i] = jo;
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        JSONArray ja = new JSONArray();
        for(int i = 0; i < jsonValues.length; i++){
            ja.put(jsonValues[i]);
        }
        return ja;
    }

    public static String postJSON(String myurl, JSONArray jarr) throws IOException {
        StringBuffer response = null;

        try {
            JSONObject parameters = new JSONObject();
            parameters.put("jsonArray", jarr);

            URL url = new URL(myurl);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            OutputStream out = new BufferedOutputStream(conn.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));

            writer.write(parameters.toString());
            writer.close();
            out.close();

            int responseCode = conn.getResponseCode();
            Log.d("Main", "\nSending 'POST' request to URL : " + url);
            Log.d("Main", "Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            Log.d("Main", "Response in universal: " + response.toString());
        } catch (Exception exception) {
            Log.d("Error", "Exception: " + exception);
        }

        return response.toString();
    }


}