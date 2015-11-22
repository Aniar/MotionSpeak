package motionspeak.csc212.motionspeak;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollView;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity implements SensorEventListener {

    private Timer timer;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private float mPrevMagField;
    private float mMagField;
    private float baseValue;
    private boolean baseValueBoolean = false;
    private boolean isMoving = false;
    private float[] mPrevMagValues = new float[3];
    private CardBuilder card;
    private TextView mTextView;

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

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);



        if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }

        motionSensing();

    }

    public void motionSensing(){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

//                Log.d("MainActivity", "Magnetic Field is : " + mMagField);
//                Log.d("MainActivity", "Base Value is : " + baseValue);
                if (mMagField > (baseValue + 15) || mMagField < (baseValue - 15)) {
                    Log.d("MainActivity", "YOU ARE MOVING (if statement)");
                    isMoving = true;
                } else {
                    isMoving = false;
                }
                if (baseValueBoolean == false && mMagField != 0.0) {
                    baseValue = mMagField;
                    baseValueBoolean = true;
                }
                updatePrevMagValues(mMagField);

                numInRange();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateCard();
                    }
                });

            }
        }, 0, 500);
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
        motionSensing();
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

    public void updateCard() {
        if (isMoving == true) {
            mTextView.setText("You are moving!");
            Log.d("Main", "you are supposedly moving update view");
        } else if (isMoving == false) {
            mTextView.setText("You aren't moving");
            Log.d("Main", "you aRENT' !!moving update view");

        }
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

}
