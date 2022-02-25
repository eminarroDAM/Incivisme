package org.ecaib.incivisme.ui.mapa;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.ecaib.incivisme.Incidencia;
import org.ecaib.incivisme.R;
import org.ecaib.incivisme.SharedViewModel;
import org.ecaib.incivisme.databinding.FragmentMapaBinding;

public class MapaFragment extends Fragment {

    private MapaViewModel mapaViewModel;
    private FragmentMapaBinding binding;
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mapaViewModel =
                new ViewModelProvider(this).get(MapaViewModel.class);

        binding = FragmentMapaBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
            .findFragmentById(R.id.map);


        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference base = FirebaseDatabase.getInstance().getReference();

        DatabaseReference users = base.child("users");
        DatabaseReference uid = users.child(auth.getUid());
        DatabaseReference incidencies = uid.child("incidencies");

        SharedViewModel model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);

        mapFragment.getMapAsync(map -> {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]
                                {Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSION);
            }
            map.setMyLocationEnabled(true);

            MutableLiveData<LatLng> currentLatLng = model.getCurrentLatLng();
            LifecycleOwner owner = getViewLifecycleOwner();
            currentLatLng.observe(owner, latLng -> {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                map.animateCamera(cameraUpdate);
                currentLatLng.removeObservers(owner);
            });

            incidencies.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Incidencia incidencia = dataSnapshot.getValue(Incidencia.class);

                    LatLng aux = new LatLng(
                            Double.valueOf(incidencia.getLatitud()),
                            Double.valueOf(incidencia.getLongitud())
                    );

                    IncidenciesInfoWindowAdapter customInfoWindow = new IncidenciesInfoWindowAdapter(
                            getActivity()
                    );


                    Marker marker = map.addMarker(new MarkerOptions()
                            .title(incidencia.getProblema())
                            .snippet(incidencia.getDireccio())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            .position(aux));
                    marker.setTag(incidencia);
                    map.setInfoWindowAdapter(customInfoWindow);

                    /*
                    map.addMarker(new MarkerOptions()

                            .title(incidencia.getProblema())
                            .snippet(incidencia.getDireccio())
                            .position(aux)
                            .icon(BitmapDescriptorFactory.defaultMarker
                                    (BitmapDescriptorFactory.HUE_GREEN)));

                     */
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        });


        return root;
    }

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

            return view;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}