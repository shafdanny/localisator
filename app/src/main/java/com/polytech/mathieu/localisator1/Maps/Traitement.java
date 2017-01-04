package com.polytech.mathieu.localisator1.Maps;

import android.os.Environment;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 02/01/17.
 */

public class Traitement {

    List<Float> list = new ArrayList();
    public static LatLng cluster1;
    public static LatLng cluster2;

    public void recuperation() {

        //Ouverture du fichier
        FileReader input = null;
        try {
            File f = new File(Environment.getExternalStorageDirectory().getPath() + "/Coordonnees");
            File ff = new File(f, "Traitement.json");
            input = new FileReader(ff);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader bufRead = new BufferedReader(input);
        String myLine = null;

        try {
            myLine = bufRead.readLine();    //première ligne de consigne
        } catch (IOException e) {
            e.printStackTrace();
        }

        int i = 1;

        try {
            while ( (myLine = bufRead.readLine()) != null && i < 3){
                String[] array = myLine.split(",");
                i++;
                System.out.println("Array 1 " + array[1]);

                list.add(Float.parseFloat(array[1]));
                list.add(Float.parseFloat(array[3]));
            }

            cluster1 = new LatLng(list.get(0), list.get(1));
            cluster2 = new LatLng(list.get(2), list.get(3));

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            bufRead.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
