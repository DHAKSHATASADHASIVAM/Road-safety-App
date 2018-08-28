package com.dragosholban.androidfacedetection;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Locale;

//import static com.dragosholban.androidfacedetection.VideoFaceDetectionActivity.requLocationUpdates;

public class Moni extends Service implements SensorEventListener {
    private float lastX, lastY, lastZ;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float deltaXMax = 0;
    private float deltaYMax = 0;
    private float deltaZMax = 0;
    private float deltaX = 0;
    private float deltaY = 0;
    public static int i = 0;
    private float deltaZ = 0;


    private float vibrateThreshold = 0;

    // private TextView state;


    public Vibrator v;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

            vibrateThreshold = accelerometer.getMaximumRange() / 2;
        } else {

        }
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        requestLocationUpdates();
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        deltaX = Math.abs(lastX - event.values[0]);
        deltaY = Math.abs(lastY - event.values[1]);
        deltaZ = Math.abs(lastZ - event.values[2]);
        if (deltaX < 2)
            deltaX = 0;
        if (deltaY < 2)
            deltaY = 0;
        if ((deltaZ > vibrateThreshold) || (deltaY > vibrateThreshold) || (deltaZ > vibrateThreshold)) {

        }
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("BrBehav/SpeedCheck");
        ref.child("acc").child("x").setValue(deltaX);
        ref.child("acc").child("y").setValue(deltaY);
        ref.child("acc").child("z").setValue(deltaZ);
        if (deltaX > 20 || deltaX < -20 || deltaY > 20 || deltaY < -20 || deltaZ > 20 || deltaZ < -20) {
            v.vibrate(500);
        i=1;


        }



    }






    private void requestLocationUpdates() {



        LocationRequest request = new LocationRequest();
        request.setInterval(1000*2);
        request.setFastestInterval(1000*1);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        final String path = "BrBehav/SpeedCheck";
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {

            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    Geocoder geocoder = new Geocoder( Moni.this,Locale.getDefault());
                    List<Address> addresses = null;
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        if(i==1) {
                            DatabaseReference refe = FirebaseDatabase.getInstance().getReference("BrBehav/Acc/");
                            DatabaseReference summa = refe.push();
                            String onnula = summa.getKey();
                            refe.child(onnula).child("latitude").setValue(location.getLatitude());
                            refe.child(onnula).child("longitude").setValue(location.getLongitude());
                            i = 0;
                        }

                        ref.child("latitude").setValue(location.getLatitude());
                        ref.child("longitude").setValue(location.getLongitude());
                       // ref.child("speed").setValue((location.getSpeed()*18/5));
                       // Toast.makeText(getBaseContext(), String.valueOf((location.getSpeed()*18/5)), Toast.LENGTH_SHORT).show();
                        try {
                            addresses = geocoder.getFromLocation(
                                    location.getLatitude(),
                                    location.getLongitude(),
                                    1);
                        } catch (Exception ioException) {
                           // Log.e("", "Error in getting address for the location");
                        }
                        Address address = addresses.get(0);
                        ref.child("Address").setValue(address.getFeatureName());

                    }
                }
            }, null);
        }
    }
}
