package com.polytech.mathieu.localisator1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Lancement du service MyService
        Log.d(MyService.TAG, "Service lancé!\n");
        startService(new Intent(this, MyService.class));


    }
}
