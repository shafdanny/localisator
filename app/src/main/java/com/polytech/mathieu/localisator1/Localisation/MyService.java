package com.polytech.mathieu.localisator1.Localisation;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.polytech.mathieu.localisator1.Localisation.MainActivity.textView;
import static com.polytech.mathieu.localisator1.R.id.time;


/*
*   Auteur : Mathieu Stackler
*   Service qui récupère l'heure, la latitude, la longitude et l'altitude de l'utilisateur
*
 */


public class MyService extends Service {


    DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    //TAG pour les tests
    public static final String TAG = "TestGPS";
    private LocationManager mLocationManager = null;

    // Interval de temps et de distance entre 2 mesures
    private static final int LOCATION_INTERVAL = 10000; //en ms
    private static final float LOCATION_DISTANCE = 0f;



    public class LocationListener implements android.location.LocationListener {

        //Variables pour l'écriture dans le json
        String fichier = "donnees.json";
        File mDir = null;
        File mFile = null;

        //Variables pour les données
        Location mLastLocation;
        String donnees;
        Double altitude;
        String date;


        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        //A chaque fois que la localisation change, cette fonction est appelée
        @Override
        public void onLocationChanged(Location location) {
            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);

            altitude = Location.convert(String.valueOf(location.getAltitude()));
            Date tmp = new Date(location.getTime());
            date = format.format(tmp);

            double latitude = mLastLocation.getLatitude();
            String sLatitude = String.format(Locale.US,"%8.6f", latitude);

            double longitude = mLastLocation.getLongitude();
            String sLongitude = String.format(Locale.US,"%8.6f", longitude);
//
            donnees = "Temps," + date +
                   // ",Latitude," + mLastLocation.convert(location.getLatitude(), Location.FORMAT_DEGREES) +
                    ",Latitude," + sLatitude +
                   // ",Longitude," + Location.convert(location.getLongitude(), Location.FORMAT_DEGREES) +
                    ",Longitude," + sLongitude +
                    ",Altitude," + altitude +
                    "\n";


            textView.setText(donnees);

            // Ecriture dans le .json

            //Création du dossier "Coordonnees" à la racine de la mémoire internet du téléphone
            mDir = new File(Environment.getExternalStorageDirectory().getPath() + "/Coordonnees");
            mDir.mkdirs();

            // Création du fichier "donnees.json" dans le dossier "Coordonnees"
            mFile = new File(mDir, fichier);
            try {
                mFile.createNewFile();
                FileWriter fileWriter = new FileWriter(mFile, true);
                fileWriter.write(donnees);
                fileWriter.close();


            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}