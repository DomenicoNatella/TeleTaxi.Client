package com.application.teletaxiclient;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.text.MessageFormat;

import java.util.Date;

/**
 * Created by dn on 08/07/17.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ListPrenotazioni.db";
    private Context context;
    private static final int SCHEMA_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE {0} ({1} TEXT, {2} INTEGER , {3} TEXT , {4} TEXT, {5} TEXT, {6} INTEGER, {7} TEXT);";
        db.execSQL(MessageFormat.format(sql, ListTable.TAB_PRENOTAZIONI, ListTable.PROGRESSIVO_PRENOTAZIONE,
                ListTable.IDENTIFICATIVO_TAXI, ListTable.DESTINAZIONE,
                ListTable.SERVIZI_SPECIALI, ListTable.POSIZIONE_CLIENTE, ListTable.DATA_PRENOTAZIONE, ListTable.PRENOTAZIONE_ASSEGNATA));

    }

    public void inserisciPrenotazione(String codice, int identificativoTaxi, String posizioneCliente, String destinazione,
                String serviziSpeciali, boolean assegnata) {
        ContentValues v = new ContentValues();
        v.put(ListTable.PROGRESSIVO_PRENOTAZIONE, codice);
        v.put(ListTable.IDENTIFICATIVO_TAXI, identificativoTaxi);
        v.put(ListTable.POSIZIONE_CLIENTE, posizioneCliente);
        v.put(ListTable.DESTINAZIONE, destinazione);
        v.put(ListTable.SERVIZI_SPECIALI, serviziSpeciali);
        v.put(ListTable.DATA_PRENOTAZIONE, new Date().getTime());
        v.put(ListTable.PRENOTAZIONE_ASSEGNATA, Boolean.valueOf(assegnata));
        getWritableDatabase().insert(ListTable.TAB_PRENOTAZIONI, null, v);
    }


    public Cursor getPrenotazione() {
        return (getReadableDatabase().query(ListTable.TAB_PRENOTAZIONI, ListTable.COLUMNS, null, null,
                null, null, ListTable.PROGRESSIVO_PRENOTAZIONE));
    }


    public int deleteProiezioneByID(String codice){
        return (getWritableDatabase().delete(ListTable.TAB_PRENOTAZIONI, ListTable.PROGRESSIVO_PRENOTAZIONE+" = '"+codice+"'", null));
    }

    /*
    public int updateProiezioneByID(String codice, String title, int sala, String date){
        ContentValues v = new ContentValues();
        v.put(ListTable., title);
        v.put(ListTable.SALA, sala);
        v.put(ListTable.DATE, date);
        return(getWritableDatabase().update(ListTable.TABLE_NAME, v, ListTable.CODE+" = '"+codice+"'", null));
    }*/

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
