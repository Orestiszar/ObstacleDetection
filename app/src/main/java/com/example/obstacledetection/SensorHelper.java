package com.example.obstacledetection;

import static android.content.ContentValues.TAG;
import static android.content.Context.SENSOR_SERVICE;
import static android.hardware.SensorManager.AXIS_X;
import static android.hardware.SensorManager.AXIS_Z;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class SensorHelper implements SensorEventListener {

    private MainActivity mainActivity;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magneticField;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] rotationMatrixRemapped = new float[9];
    protected final float[] orientationAngles = new float[3];

    public SensorHelper(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        sensorManager = (SensorManager) this.mainActivity.getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(accelerometer == null) {
            Log.e(TAG, "Accelerometer not available.");
            mainActivity.finish(); // Close app
        }
        magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if(magneticField == null) {
            Log.e(TAG, "Magnetometer not available.");
            mainActivity.finish(); // Close app
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading,
                    0, magnetometerReading.length);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //must be implemented
    }

    public void updateOrientationAngles() {
        // Compute the three orientation angles based on the most recent readings from
        // the device's accelerometer and magnetometer.
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);
        // "rotationMatrix" now has up-to-date information.

        //Change coordinate system
        SensorManager.remapCoordinateSystem(rotationMatrix, AXIS_X, AXIS_Z, rotationMatrixRemapped);

        SensorManager.getOrientation(rotationMatrixRemapped, orientationAngles);
        // "orientationAngles" now has up-to-date information.

        for(int i = 0; i < 3; i++) {
            orientationAngles[i] = (float)(Math.toDegrees(orientationAngles[i]));
        }
    }

    public void resumeSensors(){
        // Get updates from the accelerometer and magnetometer at a constant rate.
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void pauseSensors(){
        sensorManager.unregisterListener(this);
    }
}
