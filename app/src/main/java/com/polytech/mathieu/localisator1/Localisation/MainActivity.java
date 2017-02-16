package com.polytech.mathieu.localisator1.Localisation;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.polytech.mathieu.localisator1.Maps.MapsActivity;
import com.polytech.mathieu.localisator1.R;
import com.polytech.mathieu.localisator1.data.IdGenerator;
import com.polytech.mathieu.localisator1.model.Cluster;
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
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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
    public static EditText editText;
    public static Spinner spinner;

    public static final String PREFS_NAME = "LocalisatorPrefs";

    public static List<Cluster> clusters;

    File mDir = null;
    File mFile = null;
    String fichier = "param.json";

    static final int SocketServerPORT = 8181;
    private String ServerAdress; // = editText.getText().toString();
    private String nbCluster;

    Socket clientSocket;
    public boolean launch = false;

    //ServerSocketThread serverSocketThread;
    static String uuid;

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
        editText = (EditText) findViewById(R.id.editIP);
        spinner = (Spinner) findViewById(R.id.spinner);
        final Intent intent = new Intent(this, MyService.class);

        addItemsOnSpinner();

        final Button buttonStart = (Button) findViewById(R.id.start);
        final Button buttonStop = (Button) findViewById(R.id.stop);
        final Button buttonErase = (Button) findViewById(R.id.erase);
        final Button buttonLaunch = (Button) findViewById(R.id.launch);
        final Button buttonMap = (Button) findViewById(R.id.bmap);
        final Button buttonFindMatch = (Button) findViewById(R.id.match);

        uuid = getUserId();

        Log.i(TAG, "onCreate: unique id: " + uuid);

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
                ServerAdress = editText.getText().toString();
                if (ServerAdress == null || ServerAdress.equals("")){
                    showToast("Merci d'entrer l'adresse IP du serveur");
                    Log.e(TAG, "Merci d'entrer l'adresse IP du serveur");
                }
                else{

                    try {
                        ecritureParam();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Log.e(TAG, String.valueOf(spinner.getSelectedItem()));
                    Log.d(MyService.TAG, "Envoie données à " + ServerAdress + "\n");
                    uploadFile(ServerAdress);
                    //serverSocketThread = new ServerSocketThread();
                    //serverSocketThread.start();
                }
            }
        });

        buttonMap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(MyService.TAG, "Lancement acitivité map!\n");
                Intent intent1 = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent1);
            }
        });

        buttonFindMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ServerAdress = editText.getText().toString();
                if (ServerAdress == null || ServerAdress.equals("")){
                    showToast("Merci d'entrer l'adresse IP du serveur");
                    Log.e(TAG, "Merci d'entrer l'adresse IP du serveur");
                }
                else{

                    try {
                        ecritureParam();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Log.d(MyService.TAG, "Finding match for " + uuid + " ServerAdress " + ServerAdress + "\n");
                    getMatch(ServerAdress);
                    //serverSocketThread = new ServerSocketThread();
                    //serverSocketThread.start();
                }
            }
        });

    }

    /**
     *
     * Get a user id that is saved in SharedPreferences.
     * If an id does not exist, create one.
     *
     * @return userId
     */
    private String getUserId() {
        String uuid = "";
        String prefIdKey = "id";

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        uuid = settings.getString(prefIdKey, "");

        if(uuid.equals("")) {
            uuid = IdGenerator.generate();
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(prefIdKey, uuid);
            editor.apply();
        }

        return uuid;
    }

    /**
     * Find a match from the server
     *
     * @param serverAdress
     */
    private void getMatch(String serverAdress) {
        ServiceGenerator.changeApiBaseUrl(serverAdress);
        FileUploadService service =
                ServiceGenerator.createService(FileUploadService.class);

        Call<ResponseBody> call = service.getMatch(uuid);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {
                Log.v("Find Match response: ", "" + response.code());

                if(response.code() == 200) {
                    try {
                        String jsonResponse = response.body().string();
                        Log.i(TAG, "onResponse: response message: " + jsonResponse);
                        showToast("ID ayant les mêmes clusters : " + jsonResponse);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    showToast("Request failed, please check server address");
                }
            };

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
            }
        });
    }


    /**
     * Upload the GPS data to the server
     *
     * @param serverAdress
     */
    private void uploadFile(String serverAdress) {
        ServiceGenerator.changeApiBaseUrl(serverAdress);
        // create upload service client
        FileUploadService service =
                ServiceGenerator.createService(FileUploadService.class);

        File mDir = new File(Environment.getExternalStorageDirectory().getPath() + "/Coordonnees");
        File file = new File(mDir, "donnees.json");

        // create RequestBody instance from file
        final RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), file);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        int nbCluster = Integer.parseInt(String.valueOf(spinner.getSelectedItem()));

        // finally, execute the request
        Call<ResponseBody> call = service.upload(uuid, nbCluster, body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {
                Log.v("Upload response code: ", "" + response.code());

                if(response.code() == 200) {
                    try {
                        String jsonResponse = response.body().string();
                        Log.i(TAG, "onResponse: response message: " + jsonResponse);

                        clusters = new Gson().fromJson(jsonResponse, new TypeToken<List<Cluster>>(){}.getType());

                        showToast("Request OK, response received");

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    showToast("Request failed, please check server address");
                }
            };

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
            }
        });
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

    //Méthode pour afficher un toast
    public void showToast(final String toast)
    {
        runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(MainActivity.this, toast, Toast.LENGTH_LONG).show();
            }
        });
    }

    // add items into spinner dynamically
    public void addItemsOnSpinner() {

        List<String> list = new ArrayList<String>();
        //list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        /*list.add("5");
        list.add("6");
        list.add("7");
        list.add("8");
        list.add("9");*/
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    private void ecritureParam() throws IOException {

        String donnees = "Données GPS... \n" + String.valueOf(spinner.getSelectedItem()) + "\n";
        // Ecriture dans le .json

        //Création du dossier "Coordonnees" à la racine de la mémoire internet du téléphone
        mDir = new File(Environment.getExternalStorageDirectory().getPath() + "/Coordonnees");
        mDir.mkdirs();

        // Création du fichier "donnees.json" dans le dossier "Coordonnees"
        mFile = new File(mDir, fichier);
        try {
            mFile.createNewFile();
            FileWriter fileWriter = new FileWriter(mFile, false);
            fileWriter.write(donnees);
            fileWriter.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


/*
    //Création du thread qui attend une connexion
    public class ServerSocketThread extends Thread {

        @Override
        public void run() {
            Socket socket = null;

           // Log.e(TAG, "En attente d'une connexion...\n");
            try {
                    clientSocket = new Socket(ServerAdress, SocketServerPORT);

                Log.e(TAG, "Connexion au serveur...\n");;
                Log.e(TAG, "Connexion établie...\n");
                MainActivity.FileTxThread fileTxThread = new MainActivity.FileTxThread(clientSocket);
                fileTxThread.start();


            } catch (IOException e) {
                //Log.e(TAG, "Impossible de se connecter");
                showToast("Adresse IP invalide");
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
*/
    //Thread qui envoie le .json quand une demande est faite
  /*  public class FileTxThread extends Thread {
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

            File file3 = new File(
                    Environment.getExternalStorageDirectory().getPath()+"/Coordonnees",
                    "param.json");

            Log.e(TAG, "Envoie du fichier...\n");

            byte[] bytes1 = new byte[(int) file1.length()];
            BufferedInputStream bis1;
            byte[] bytes3 = new byte[(int) file3.length()];
            BufferedInputStream bis3;
            try {
                int available = -1;
                bis1 = new BufferedInputStream(new FileInputStream(file1));
                OutputStream os1 = socket.getOutputStream();


                while ((available = bis1.read(bytes1)) > 0){
                    os1.write(bytes1, 0, available);
                }

                os1.flush();


                bis3 = new BufferedInputStream(new FileInputStream(file3));
                bis3.read(bytes3, 0, bytes3.length);
                OutputStream os3 = socket.getOutputStream();
                os3.write(bytes3, 0, bytes3.length);
                os3.flush();


                //Reception
                System.out.println("Reception du fichier...");
                byte[] bytes2 = new byte[2048];
                InputStream is = socket.getInputStream();
                FileOutputStream fos = new FileOutputStream(file2);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                int bytesRead = is.read(bytes2, 0, bytes2.length);
                bos.write(bytes2, 0, bytesRead);
                bos.close();

                socket.close();

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
    } */

}
