package com.example.location_pointer;


import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private ImageView compassImage;
    private float currentAzimuth = 0f;
    TextView degree;
    Button button;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private float[] gravity;
    private float[] geomagnetic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                startActivity(intent);
            }
        });
        compassImage = findViewById(R.id.compass);
        degree = findViewById(R.id.degree);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        final float alpha = 0.97f;

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (gravity == null)
                gravity = event.values.clone();
            else {
                for (int i = 0; i < 3; i++)
                    gravity[i] = alpha * gravity[i] + (1 - alpha) * event.values[i];
            }
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            if (geomagnetic == null)
                geomagnetic = event.values.clone();
            else {
                for (int i = 0; i < 3; i++)
                    geomagnetic[i] = alpha * geomagnetic[i] + (1 - alpha) * event.values[i];
            }
        }

        if (gravity != null && geomagnetic != null) {
            float[] R = new float[9];
            float[] I = new float[9];

            if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(R, orientation);

                float azimuthInRadians = orientation[0];
                float azimuthInDeg = (float) Math.toDegrees(azimuthInRadians);
                azimuthInDeg = (azimuthInDeg + 360) % 360;

                // Update the degree TextView
                degree.setText(String.format("Degree: %.0f°", azimuthInDeg));

                RotateAnimation ra = new RotateAnimation(
                        currentAzimuth,
                        -azimuthInDeg,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);

                ra.setDuration(500);
                ra.setFillAfter(true);

                int degree_value  = (int) azimuthInDeg;
                degree.setText(String.valueOf(degree_value));

                compassImage.startAnimation(ra);
                currentAzimuth = -azimuthInDeg;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}