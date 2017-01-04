package com.polytech.mathieu.localisator1.Localisation;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.polytech.mathieu.localisator1.Maps.MapsActivity;
import com.polytech.mathieu.localisator1.R;
import com.polytech.mathieu.localisator1.network.FileUploadService;
import com.polytech.mathieu.localisator1.network.ServiceGenerator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "TestGPS";
    public static TextView textView;

    static final int SocketServerPORT = 8181;
    ServerSocket serverSocket;
    public boolean launch = false;

    ServerSocketThread serverSocketThread;

    private static final String[] LOCATION_PERMS={
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(LOCATION_PERMS, 15);
        }
        textView = (TextView) findViewById(R.id.coordonnees);
        final Intent intent = new Intent(this, MyService.class);

        final Button buttonStart = (Button) findViewById(R.id.start);
        final Button buttonStop = (Button) findViewById(R.id.stop);
        final Button buttonErase = (Button) findViewById(R.id.erase);
        final Button buttonLaunch = (Button) findViewById(R.id.launch);
        final Button buttonMap = (Button) findViewById(R.id.bmap);

        serverSocketThread = new ServerSocketThread();

        buttonStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                        Log.d(MyService.TAG, "Service lancé!\n");
                        startService(intent);
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(MyService.TAG, "Service arrêté!\n");
                stopService(intent);

            }
        });

        buttonErase.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(MyService.TAG, "Suppression json!\n");
                erase();
            }
        });

        buttonLaunch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (!launch){
                    launch = true;
                    Log.d(MyService.TAG, "Envoie données!\n");
                    uploadFile(Uri.parse(""));

                    serverSocketThread.start();
                }
/*
                Log.d(MyService.TAG, "Envoie données!\n");
                uploadFile(Uri.parse("")); */
            }
        });

        buttonMap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(MyService.TAG, "Lancement acitivité map!\n");
                Intent intent1 = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent1);
            }
        });
    }

    // Charger le fichier gps vers le serveur
    private void uploadFile(Uri fileUri) {
        // create upload service client
        FileUploadService service =
                ServiceGenerator.createService(FileUploadService.class);

        File mDir = new File(Environment.getExternalStorageDirectory().getPath() + "/Coordonnees");
        File file = new File(mDir, "donnees.json");

/*
        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), file);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        // add another part within the multipart request
        String descriptionString = "hello, this is description speaking";
        RequestBody description =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), descriptionString);

        // finally, execute the request
        Call<ResponseBody> call = service.upload(description, body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {
                Log.v("Upload", "success");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
            }
        }); */
    }

// Méthode qui supprime les données dans le json
    public void erase(){
        String fichier = "donnees.json";
        // Ecriture dans le .json

        //Création du dossier "Coordonnees" à la racine de la mémoire internet du téléphone
        File mDir = new File(Environment.getExternalStorageDirectory().getPath() + "/Coordonnees");
        mDir.mkdirs();

        // Création du fichier "donnees.json" dans le dossier "Coordonnees"
        File mFile = new File(mDir, fichier);
        try {
            mFile.createNewFile();
            FileWriter fileWriter = new FileWriter(mFile, false);
            fileWriter.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Création du thread qui attend une connexion
    public class ServerSocketThread extends Thread {

        @Override
        public void run() {
            Socket socket = null;
           // Log.e(TAG, "En attente d'une connexion...\n");
            try {
                serverSocket = new ServerSocket(SocketServerPORT);
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                    }});

                while (true) {
                    Log.e(TAG, "En attente d'une connexion...\n");
                    socket = serverSocket.accept();
                    Log.e(TAG, "Connexion établie...\n");
                    MainActivity.FileTxThread fileTxThread = new MainActivity.FileTxThread(socket);
                    fileTxThread.start();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    //Thread qui envoie le .json quand une demande est faite
    public class FileTxThread extends Thread {
        Socket socket;

        FileTxThread(Socket socket){
            this.socket= socket;
        }

        @Override
        public void run() {
            File file1 = new File(
                    Environment.getExternalStorageDirectory().getPath()+"/Coordonnees",
                    "donnees.json");

            File file2 = new File(
                    Environment.getExternalStorageDirectory().getPath()+"/Coordonnees",
                    "traitement.json");

            Log.e(TAG, "Envoie du fichier...\n");

            byte[] bytes = new byte[(int) file1.length()];
            BufferedInputStream bis;
            try {
                bis = new BufferedInputStream(new FileInputStream(file1));
                bis.read(bytes, 0, bytes.length);
                OutputStream os = socket.getOutputStream();
                os.write(bytes, 0, bytes.length);
                os.flush();


                //Test
                System.out.println("Reception du fichier...");
                byte[] bytes2 = new byte[1024];
                InputStream is = socket.getInputStream();
                FileOutputStream fos = new FileOutputStream(file2);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                int bytesRead = is.read(bytes2, 0, bytes2.length);
                bos.write(bytes2, 0, bytesRead);
                bos.close();


                socket.close();

        /*        final String sentMsg = "File sent to: " + socket.getInetAddress();
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,
                                sentMsg,
                                Toast.LENGTH_LONG).show();
                    }}); */

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                try {
                    launch = true;
                    Log.e(TAG, "Fermeture de la socket");
                    socket.close();
                    this.interrupt();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }
    }

}
