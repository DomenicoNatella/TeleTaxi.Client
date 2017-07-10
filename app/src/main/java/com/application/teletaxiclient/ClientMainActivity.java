package com.application.teletaxiclient;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import model.Cliente;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.restlet.data.Protocol;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

import java.lang.reflect.GenericSignatureFormatError;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentMap;

import model.Cliente;
import model.OperatoreTelefonico;
import model.Prenotazione;
import model.Taxi;

public class ClientMainActivity extends AppCompatActivity {

    private static DatabaseHelper databaseHelper;
    private static int code = 0;
    private TextView mTextMessage;
    private ArrayList<String> mSelectedItems;
    private ArrayList<Integer> mSelectedItemsNotify;
    private RecyclerView recList;
    private Gson gson;
    private String cliente = "cliente";
    private String clientePreferences = "clientePref";
    private SharedPreferences sharedPreferences;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    createAdapterForAcvitity();
                    return true;
                case R.id.navigation_dashboard:
                    mSelectedItems = new ArrayList();
                    AlertDialog.Builder builder = new AlertDialog.Builder(ClientMainActivity.this);
                    builder.setTitle(R.string.servizi);
                    builder.setMultiChoiceItems(R.array.servizi_speciali_choiche, null,
                                    new DialogInterface.OnMultiChoiceClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which,
                                                            boolean isChecked) {
                                            if (isChecked) {
                                                if(which == 0) mSelectedItems.add("bagagli");
                                                else if(which == 1) mSelectedItems.add("animali");
                                                else if(which == 2) mSelectedItems.add("disabili");
                                            } else if (mSelectedItems.contains(which)) {
                                                mSelectedItems.remove(Integer.valueOf(which));
                                            }
                                        }
                                    })
                            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String posizioneLocalizzazione = getPosizione();
                                    if(posizioneLocalizzazione != null) {
                                        String[] scelteServiziSpeciali = mSelectedItems.toArray(new String[mSelectedItems.size()]);
                                        new PostPrenotazioneTaxi().execute(null, gson.toJson(scelteServiziSpeciali, String[].class), posizioneLocalizzazione);
                                    }
                                    }
                            })
                            .setNegativeButton(R.string.cancella, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {}
                            });
                    builder.create().show();
                    return true;
                case R.id.navigation_notifications:
                    mSelectedItemsNotify = new ArrayList();
                    builder = new AlertDialog.Builder(ClientMainActivity.this);
                    builder.setTitle(R.string.notifica);
                    builder.setMultiChoiceItems(R.array.timer_notifica_choiche, null,
                            new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which,
                                                    boolean isChecked) {
                                    if (isChecked) {
                                        if(which == 0) mSelectedItemsNotify.add(2);
                                        else if(which == 1) mSelectedItemsNotify.add(5);
                                        else if(which == 2) mSelectedItemsNotify.add(7);
                                    } else if (mSelectedItemsNotify.contains(which)) {
                                        mSelectedItemsNotify.remove(Integer.valueOf(which));
                                    }
                                }
                            })
                            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                        if(mSelectedItemsNotify.size() <= 0) stopService(new Intent(getApplicationContext(), NotificationService.class));
                                        else{
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putInt("preferenzeNotifica", mSelectedItemsNotify.get(0));
                                            editor.commit();
                                        }
                                }
                            })
                            .setNegativeButton(R.string.cancella, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {}
                            });
                    builder.create().show();
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_main);
        sharedPreferences = getSharedPreferences(clientePreferences, Context.MODE_PRIVATE);
        databaseHelper = new DatabaseHelper(ClientMainActivity.this);
        gson = new GsonBuilder()
                .setDateFormat("dd/MM/yyyy HH:mm:ss")
                .create();
        final Cliente clienteRcv = gson.fromJson(sharedPreferences.getString(cliente, null), Cliente.class);
        if(clienteRcv == null){
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final LayoutInflater inflater = this.getLayoutInflater();

            builder.setView(inflater.inflate(R.layout.dialog_id_cliente, null))
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            EditText etUser = (EditText) ((AlertDialog) dialog).findViewById(R.id.user);
                            EditText etNome = (EditText) ((AlertDialog) dialog).findViewById(R.id.nome);
                            EditText etCognome = (EditText) ((AlertDialog) dialog).findViewById(R.id.cognome);
                            EditText etTelefono = (EditText) ((AlertDialog) dialog).findViewById(R.id.telefono);
                            EditText etDataNascita = (EditText) ((AlertDialog) dialog).findViewById(R.id.data_di_nascita);
                            Cliente clienteToSave = null;
                            try {
                                clienteToSave = new Cliente(etUser.getText().toString(), etNome.getText().toString(),
                                        etCognome.getText().toString(),new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(etDataNascita.getText().toString()),
                                        Integer.valueOf(etTelefono.getText().toString()));
                            }catch (Exception e){
                                Log.e("error!", e.toString());
                            }
                            if(clienteToSave != null) {
                                editor.putString(cliente, gson.toJson(clienteToSave));
                                editor.commit();
                                new PutCliente().execute(((AlertDialog) dialog).getCurrentFocus());
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancella, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {}
                    });
            builder.create().show();
        }
        createAdapterForAcvitity();
        if(isActiveGPS() && isActiveConnection()){
            getSupportActionBar().setTitle(getPosizione());
        }

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private String getPosizione(){
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        if(isActiveGPS() && isActiveConnection()) {
            final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                } catch (Exception e) {
                    return null;
                }
                if (addresses != null && addresses.size() > 0 )
                    return ""+addresses.get(0).getThoroughfare()+", "+addresses.get(0).getLocality();
                else return null;
            }else return null;
        }else return null;
    }

    public boolean isActiveGPS() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final boolean[] actived = new boolean[1];

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            actived[0] = false;
        }
        boolean isGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(isGPS){
            Toast.makeText(getApplicationContext(), "Il GPS e' attivo", Toast.LENGTH_LONG).show();
            actived[0] = true;
        }else{
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                    .setMessage("Il servizio GPS non e' attivo, vuoi attivarlo?")
                    .setCancelable(false)
                    .setPositiveButton("Abilita", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),0);
                            actived[0] = true;
                        }
                    })
                    .setNegativeButton("Cancella", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            actived[0] = false;
                        }
                    });
            AlertDialog alert = alertDialogBuilder.create();
            alert.show();
        }
        return actived[0];
    }

    private boolean isActiveConnection(){
        final boolean[] actived = new boolean[1];
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo != null && activeNetworkInfo.isConnected()){
            return true;
        }else{
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                    .setMessage("Il servizio Internet non e' attivo, vuoi attivarlo?")
                    .setCancelable(false)
                    .setPositiveButton("Abilita", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS),0);
                            actived[0] = true;
                        }
                    })
                    .setNegativeButton("Cancella", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            actived[0] = false;
                        }
                    });
            AlertDialog alert = alertDialogBuilder.create();
            alert.show();
        }
        return false;
    }

    private void createAdapterForAcvitity() {
        List<Prenotazione> prenotaziones = new ArrayList<>();
        Cursor cursorPrenotazione  = databaseHelper.getPrenotazione();
        List<String> urls = new ArrayList<>();
        Log.e("database", ""+cursorPrenotazione.getCount());
        if(cursorPrenotazione != null) {
            if (cursorPrenotazione.moveToFirst()) {
                do {
                    Prenotazione prenotazione = new Prenotazione(cursorPrenotazione.getString(cursorPrenotazione.getColumnIndex(ListTable.PROGRESSIVO_PRENOTAZIONE)),
                            cursorPrenotazione.getInt(cursorPrenotazione.getColumnIndex(ListTable.IDENTIFICATIVO_TAXI)),
                            cursorPrenotazione.getString(cursorPrenotazione.getColumnIndex(ListTable.DESTINAZIONE)),
                            gson.fromJson(cursorPrenotazione.getString(cursorPrenotazione.getColumnIndex(ListTable.SERVIZI_SPECIALI)), String[].class),
                            cursorPrenotazione.getString(cursorPrenotazione.getColumnIndex(ListTable.POSIZIONE_CLIENTE)),
                            new Date(cursorPrenotazione.getLong(cursorPrenotazione.getColumnIndex(ListTable.DATA_PRENOTAZIONE))),
                            Boolean.valueOf(cursorPrenotazione.getString(cursorPrenotazione.getColumnIndex(ListTable.PRENOTAZIONE_ASSEGNATA))));
                    prenotaziones.add(prenotazione);
                    urls.add("https://maps.googleapis.com/maps/api/streetview?size=600x300&location=" + prenotazione.getDestinazione() + "&fov=90&heading=235&pitch=10");
                } while (cursorPrenotazione.moveToNext());
            }
        }
        if(prenotaziones.size() <= 0 && urls.size() <= 0){
            prenotaziones.add(new Prenotazione("Prenotazione non disponibile", 0, "Nessuna infomazione", null, null, new Date(), false));
            urls.add("https://maps.googleapis.com/maps/api/streetview?size=600x300&location="+"&fov=90&heading=235&pitch=10");
        }

        recList = (RecyclerView) findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        registerForContextMenu(recList);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        PrenotazioneRecyclerViewAdapter adapter = new PrenotazioneRecyclerViewAdapter(prenotaziones, urls , getApplicationContext());
        recList.setAdapter(adapter);
    }





    class PostPrenotazioneTaxi extends AsyncTask<String, Void, Void>{

        @Override
        protected Void doInBackground(final String... params) {
            final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setDateFormat("dd/MM/yyyy HH:mm:ss").create();
            String g = null;
            ClientResource clientResource = null;

            try {
                Cliente clienteRcv =  gson.fromJson(sharedPreferences.getString(cliente, null), Cliente.class);
                final Prenotazione prenotazione = new Prenotazione(clienteRcv.getCodiceCliente()+""+(code++), clienteRcv, params[0],
                        gson.fromJson(params[1], String[].class), params[2], new Date(), false);
                Log.e("toSend", gson.toJson(prenotazione, Prenotazione.class));

                if(clienteRcv != null){
                    clientResource = new ClientResource("http://192.168.1.7/teletaxi/prenotazione");
                    clientResource.setProtocol(Protocol.HTTP);
                    ConcurrentMap<String, Object> attrs = clientResource.getRequest().getAttributes();
                    Series<Header> headers = (Series<Header>) attrs.get(HeaderConstants.ATTRIBUTE_HEADERS);
                    if (headers == null) {
                        headers = new Series<Header>(Header.class);
                        Series<Header> prev = (Series<Header>)
                                attrs.putIfAbsent(HeaderConstants.ATTRIBUTE_HEADERS, headers);
                        if (prev != null) {
                            headers = prev;
                        }
                    }
                    headers.add("Content-Type", "application/json; charset=UTF-8");
                    headers.add("Authorization", "cHdk");
                    g = clientResource.put(gson.toJson(prenotazione, Prenotazione.class)).getText();
                    int status = clientResource.getStatus().getCode();
                    if(status == 200){
                       clientResource = new ClientResource("http://192.168.1.7/teletaxi/prenotazione/"+prenotazione.getProgressivo());
                        clientResource.setProtocol(Protocol.HTTP);
                        attrs = clientResource.getRequest().getAttributes();
                        headers = (Series<Header>) attrs.get(HeaderConstants.ATTRIBUTE_HEADERS);
                        if (headers == null) {
                            headers = new Series<Header>(Header.class);
                            Series<Header> prev = (Series<Header>)
                                    attrs.putIfAbsent(HeaderConstants.ATTRIBUTE_HEADERS, headers);
                            if (prev != null) {
                                headers = prev;
                            }
                        }
                        headers.add("Content-Type", "application/json; charset=UTF-8");
                        headers.add("Authorization", "cHdk");
                        g = clientResource.get().getText();
                        status = clientResource.getStatus().getCode();
                        Prenotazione prenotazioneRcv = new Gson().fromJson(g, Prenotazione.class);
                        databaseHelper.inserisciPrenotazione(prenotazione.getProgressivo(),
                                prenotazioneRcv.getTaxi().getCodice(),
                                prenotazione.getPosizioneCliente(), prenotazione.getDestinazione(), gson.toJson(prenotazione.getServiziSpeciali(),String[].class),
                                prenotazione.isAssegnata());
                        stopService(new Intent(getApplicationContext(), NotificationService.class));
                        Intent intentNotification = new Intent(getApplicationContext(), NotificationService.class);
                        intentNotification.putExtra("tempoDiAttesa", prenotazioneRcv.getTempoAttesa());
                        startService(intentNotification);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Prenotazione effettuata", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            } catch (final ResourceException e) {
                Log.e("error!", e.toString());
            } catch (Exception e) {
                Log.e("error!", e.toString());
            }
            return null;
        }
    }


    class PutCliente extends AsyncTask<View, Void, Void> {

        @Override
        protected Void doInBackground(final View... params) {
            String g = null;
            ClientResource clientResource = null;
            try {
                String clienteRcv =  sharedPreferences.getString(cliente, null);
                if(clienteRcv != null){
                    clientResource = new ClientResource("http://192.168.1.7/teletaxi/cliente");
                    clientResource.setProtocol(Protocol.HTTP);
                    ConcurrentMap<String, Object> attrs = clientResource.getRequest().getAttributes();
                    Series<Header> headers = (Series<Header>) attrs.get(HeaderConstants.ATTRIBUTE_HEADERS);
                    if (headers == null) {
                        headers = new Series<Header>(Header.class);
                        Series<Header> prev = (Series<Header>)
                                attrs.putIfAbsent(HeaderConstants.ATTRIBUTE_HEADERS, headers);
                        if (prev != null) headers = prev;
                    }
                    headers.add("Content-Type", "application/json; charset=UTF-8");
                    headers.add("Authorization", "cHdk");
                    g = clientResource.put(clienteRcv).getText();
                    int status = clientResource.getStatus().getCode();
                    if(status == 200){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(params[0], "Utente registrato", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            }
                        });
                    }
                }
            } catch (final ResourceException e) {
                Log.e("error!", e.toString());
            } catch (Exception e) {
                Log.e("error!", e.toString());
            }
            return null;
        }
    }
}
