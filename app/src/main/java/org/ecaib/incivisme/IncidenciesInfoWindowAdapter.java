package org.ecaib.incivisme;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import org.ecaib.incivisme.Incidencia;
import org.ecaib.incivisme.R;

public class IncidenciesInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private final Activity activity;

    public IncidenciesInfoWindowAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = activity.getLayoutInflater()
                .inflate(R.layout.infoview, null);

        Incidencia incidencia = (Incidencia) marker.getTag();

        ImageView ivProblema = view.findViewById(R.id.iv_problema);
        TextView tvProblema = view.findViewById(R.id.tvProblema);
        TextView tvDescripcio = view.findViewById(R.id.tvDescripcio);


        tvProblema.setText(incidencia.getProblema());
        tvDescripcio.setText(incidencia.getDireccio());
        //Glide.with(activity).load(incidencia.getUrl()).into(ivProblema);

        return view;
    }
}