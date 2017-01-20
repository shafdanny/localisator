package com.polytech.mathieu.localisator1.Maps;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.polytech.mathieu.localisator1.Localisation.MainActivity;
import com.polytech.mathieu.localisator1.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nahia on 28/12/2016.
 */

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    public Traitement traitement = new Traitement();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        traitement.recuperation();

        // Add a marker in Biot and move the camera
        LatLng biot = new LatLng(43.616745, 7.066663);
        List<MarkerOptions> markers = new ArrayList<>();

        for(int i = 0; i< MainActivity.clusters.size(); i++){
            MarkerOptions marker = new MarkerOptions()
                    .position(new LatLng(MainActivity.clusters.get(i).getCentroid().getX(),
                            MainActivity.clusters.get(i).getCentroid().getY()))
                    .title("Cluster"+i);
            mMap.addMarker(marker);
            markers.add(marker);
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (MarkerOptions marker : markers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();

        int padding = 300; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        /*
        mMap.moveCamera(CameraUpdateFactory.newLatLng(biot));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12.0f));*/
        mMap.animateCamera(cu);
    }


}
