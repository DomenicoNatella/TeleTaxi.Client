package com.application.teletaxiclient;

import android.database.Cursor;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.google.gson.Gson;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import model.Prenotazione;
import model.Taxi;

/**
 * Created by dn on 10/07/17.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class PrenotazioneTest {
    private Prenotazione prenotazione;
    private Taxi taxi;
    private DatabaseHelper databaseHelper;
    private Gson gson;

    @Before
    public void setUp() {
        gson = new Gson();
        databaseHelper = new DatabaseHelper(InstrumentationRegistry.getTargetContext());

        taxi = new Taxi(1);
        prenotazione = new Prenotazione("AC01", taxi, "Via Napoli, Benevento", new String[]{"bagagli", "animali"},
                "Viale Mellusi, Benevento", new Date(), false);

        databaseHelper.inserisciPrenotazione(prenotazione.getProgressivo(), taxi.getCodice(),
                prenotazione.getPosizioneCliente(), prenotazione.getDestinazione(),
                gson.toJson(prenotazione.getServiziSpeciali(), String[].class), prenotazione.isAssegnata());
    }

    @Test
    public void testInsertPrenotazione() {
        Cursor cursor = databaseHelper.getPrenotazione();
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals(prenotazione.getProgressivo(), cursor.getString(cursor.getColumnIndex(ListTable.PROGRESSIVO_PRENOTAZIONE)));
    }

    @Test
    public void testDeletePrenotazione() {
        Assert.assertNotEquals(0, databaseHelper.deleteProiezioneByID(prenotazione.getProgressivo()));
        Cursor cursor = databaseHelper.getPrenotazione();
        Assert.assertFalse(cursor.moveToFirst());
    }

}
