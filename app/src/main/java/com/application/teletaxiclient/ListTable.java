package com.application.teletaxiclient;

import android.provider.BaseColumns;

/**
 * Created by dn on 08/07/17.
 */

public interface ListTable extends BaseColumns {

    public static final String TAB_PRENOTAZIONI = "prenotazioni";
    public static final String PROGRESSIVO_PRENOTAZIONE = "progressivo";
    public static final String SERVIZI_SPECIALI = "servizi_speciali" ;
    public static final String DATA_PRENOTAZIONE = "data";
    public static final String PRENOTAZIONE_ASSEGNATA = "assegnata";
    public static final String POSIZIONE_CLIENTE = "posizione_cliente" ;
    public static final String IDENTIFICATIVO_TAXI = "identificativo_taxi";
    public static final String DESTINAZIONE = "destinazione";


    String[] COLUMNS = new String[]{PROGRESSIVO_PRENOTAZIONE, IDENTIFICATIVO_TAXI,  DESTINAZIONE,
            SERVIZI_SPECIALI, POSIZIONE_CLIENTE, DATA_PRENOTAZIONE, PRENOTAZIONE_ASSEGNATA};


}
