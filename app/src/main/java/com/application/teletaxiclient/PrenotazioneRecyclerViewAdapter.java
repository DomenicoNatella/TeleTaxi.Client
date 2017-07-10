package com.application.teletaxiclient;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.restlet.data.Protocol;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import model.Prenotazione;

/**
 * Created by dn on 08/07/17.
 */

public class PrenotazioneRecyclerViewAdapter extends RecyclerView.Adapter<PrenotazioneRecyclerViewAdapter
        .PrenotazioneViewHolder> {


    private static List<?> contactList;
    private List<String> url;
    private Context c;

    public PrenotazioneRecyclerViewAdapter(List<?> contactList, List<String> url, Context c) {
        this.contactList = contactList;
        this.url = url;
        this.c = c;
    }

    @Override
    public PrenotazioneViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.element, parent, false);

        return new PrenotazioneViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PrenotazioneViewHolder holder, int position) {
        Object ci = contactList.get(position);
        String image = url.get(position);
        if (ci instanceof Prenotazione) {
            Prenotazione prenotazione = (Prenotazione) ci;
            holder.vSubject.setText(prenotazione.getDestinazione() + " " + prenotazione.getData());
            holder.vTitle.setText(prenotazione.getProgressivo() + ", Taxi assegnato: " + prenotazione.getTaxi());
        }
        try {
            Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(image).getContent());
            Drawable d = new BitmapDrawable(c.getResources(), bitmap);
            holder.vImage.setBackground(d);
        } catch (IOException e) {
        }
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }


    class PrenotazioneViewHolder extends RecyclerView.ViewHolder implements OnCreateContextMenuListener {

        protected TextView vTitle, vSubject;
        protected ImageButton vImage;
        protected DatabaseHelper databaseHelper;


        public PrenotazioneViewHolder(View v) {
            super(v);
            vSubject = (TextView) v.findViewById(R.id.subject);
            vTitle = (TextView) v.findViewById(R.id.title);
            vImage = (ImageButton) v.findViewById(R.id.imageStreetView);
            vImage.setOnCreateContextMenuListener(this);
            databaseHelper = new DatabaseHelper(v.getContext());
        }

        @Override
        public void onCreateContextMenu(final ContextMenu menu, final View v,
                                        final ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Seleziona una opzione");
            menu.add(0, v.getId(), getAdapterPosition(), "Dettagli").setOnMenuItemClickListener(
                    new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            Cursor cursorPrenotazione = databaseHelper.getPrenotazione();
                            cursorPrenotazione.moveToPosition(getAdapterPosition());
                            Prenotazione prenotazione = new Prenotazione(cursorPrenotazione.getString(cursorPrenotazione.getColumnIndex(ListTable.PROGRESSIVO_PRENOTAZIONE)),
                                    cursorPrenotazione.getInt(cursorPrenotazione.getColumnIndex(ListTable.IDENTIFICATIVO_TAXI)),
                                    cursorPrenotazione.getString(cursorPrenotazione.getColumnIndex(ListTable.DESTINAZIONE)),
                                    new Gson().fromJson(cursorPrenotazione.getString(cursorPrenotazione.getColumnIndex(ListTable.SERVIZI_SPECIALI)), String[].class),
                                    cursorPrenotazione.getString(cursorPrenotazione.getColumnIndex(ListTable.POSIZIONE_CLIENTE)),
                                    new Date(cursorPrenotazione.getLong(cursorPrenotazione.getColumnIndex(ListTable.DATA_PRENOTAZIONE))),
                                    Boolean.valueOf(cursorPrenotazione.getString(cursorPrenotazione.getColumnIndex(ListTable.PRENOTAZIONE_ASSEGNATA))));
                            v.getContext().startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("https://www.google.com/maps/dir/?api=1" + "&destination=" + prenotazione.getDestinazione() + "&travelmode=car")));
                            return true;
                        }
                    });
            menu.add(0, v.getId(), 0, "Cancella").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Cursor cursorPrenotazione = databaseHelper.getPrenotazione();
                    cursorPrenotazione.moveToPosition(getAdapterPosition());
                    databaseHelper.deleteProiezioneByID(cursorPrenotazione.getString(cursorPrenotazione.getColumnIndex(ListTable.PROGRESSIVO_PRENOTAZIONE)));
                    new DeletePrenotazioneTaxi().execute(cursorPrenotazione.getString(cursorPrenotazione.getColumnIndex(ListTable.PROGRESSIVO_PRENOTAZIONE)));
                    return true;
                }
            });
        }

        class DeletePrenotazioneTaxi extends AsyncTask<String, Void, Void> {
            final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setDateFormat("dd/MM/yyyy HH:mm:ss").create();
            String g = null;
            ClientResource clientResource = null;

            @Override
            protected Void doInBackground(String... params) {
                try {
                    clientResource = new ClientResource("http://192.168.1.7/teletaxi/prenotazione/" + params[0]);
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
                    g = clientResource.delete().getText();
                    int status = clientResource.getStatus().getCode();
                    if (status == 200) {
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
}
