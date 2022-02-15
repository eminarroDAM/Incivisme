package org.ecaib.incivisme.ui.notifications;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.ecaib.incivisme.Incidencia;
import org.ecaib.incivisme.R;
import org.ecaib.incivisme.SharedViewModel;
import org.ecaib.incivisme.databinding.FragmentNotificationsBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NotificationsFragment extends Fragment {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private NotificationsViewModel notificationsViewModel;
    private FragmentNotificationsBinding binding;
    private FusedLocationProviderClient mFusedLocationClient;
    private ProgressBar mLoading;
    private boolean mTrackingLocation;
    private LocationCallback mLocationCallback;
    private TextInputEditText txtLatitud;
    private TextInputEditText txtLongitud;
    private TextInputEditText txtDireccio;
    private TextInputEditText txtDescripcio;

    private Button buttonNotificar;
    private SharedViewModel model;

    public static NotificationsFragment newInstance() {
        return new NotificationsFragment();
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();



        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());


        mLoading = binding.loading;

        mLocationCallback = new LocationCallback () {
            @Override
            public void onLocationResult (LocationResult locationResult) {
                if (mTrackingLocation) {
                    new FetchAddressTask(getContext()).execute(locationResult.getLastLocation());
                }
            }
        };

        model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);

        txtLatitud =  root.findViewById(R.id.txtLatitud);
        txtLongitud = root.findViewById(R.id.txtLongitud);
        txtDireccio = root.findViewById(R.id.txtDireccio);
        txtDescripcio = root.findViewById(R.id.txtDescripcio);
        buttonNotificar = root.findViewById(R.id.button_notificar);

        model.getCurrentAddress().observe(this, address -> {
            txtDireccio.setText(getString(R.string.address_text,
                    address, System.currentTimeMillis()));
        });
        model.getCurrentLatLng().observe(this, latlng -> {
            txtLatitud.setText(String.valueOf(latlng.latitude));
            txtLongitud.setText(String.valueOf(latlng.longitude));
        });

        model.getProgressBar().observe(this, visible -> {
            if(visible)
                mLoading.setVisibility(ProgressBar.VISIBLE);
            else
                mLoading.setVisibility(ProgressBar.INVISIBLE);
        });

        model.switchTrackingLocation();



        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private final String TAG = this.getClass().getSimpleName();

    private void startTrackingLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            mFusedLocationClient.requestLocationUpdates(
                    getLocationRequest(),
                    mLocationCallback,
                    null
            );
        }

        mLoading.setVisibility(ProgressBar.VISIBLE);
        mTrackingLocation = true;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                // Si es concedeix permís, obté la ubicació,
                // d'una altra manera, mostra un Toast

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startTrackingLocation();
                } else {
                    Toast.makeText(getContext(),
                            "Permís denegat",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public class FetchAddressTask extends AsyncTask<Location, Void, String> {
        private final String TAG = FetchAddressTask.class.getSimpleName();
        private Context mContext;


        FetchAddressTask(Context applicationContext) {
            mContext = applicationContext;
        }


        @Override
        protected String doInBackground(Location... locations) {
            Geocoder geocoder = new Geocoder(mContext,
                    Locale.getDefault());

            Location location = locations[0];

            List<Address> addresses = null;
            String resultMessage = "";

            try {
                addresses = geocoder.getFromLocation(
                        location.getLatitude(),
                        location.getLongitude(),
                        // En aquest cas, sols volem una única adreça:
                        1);
            }catch (IOException ioException) {
                resultMessage = "Servei no disponible";
                Log.e(TAG, resultMessage, ioException);
            }catch (IllegalArgumentException illegalArgumentException) {
                resultMessage = "Coordenades no vàlides";
                Log.e(TAG, resultMessage + ". " +
                        "Latitude = " + location.getLatitude() +
                        ", Longitude = " +
                        location.getLongitude(), illegalArgumentException);
            }

            if (addresses == null || addresses.size() == 0) {
                if (resultMessage.isEmpty()) {
                    resultMessage = "No s'ha trobat cap adreça";
                    Log.e(TAG, resultMessage);
                }
            }else {
                Address address = addresses.get(0);
                ArrayList<String> addressParts = new ArrayList<>();

                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressParts.add(address.getAddressLine(i));
                }

                resultMessage = TextUtils.join("\n", addressParts);
            }

            return resultMessage;
        }

        @Override
        protected void onPostExecute(String address) {
            if(mTrackingLocation==true){
                super.onPostExecute(address);
            }

        }
    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        buttonNotificar.setOnClickListener(button -> {
            Incidencia incidencia = new Incidencia();
            incidencia.setDireccio(txtDireccio.getText().toString());
            incidencia.setLatitud(txtLatitud.getText().toString());
            incidencia.setLongitud(txtLongitud.getText().toString());
            incidencia.setProblema(txtDescripcio.getText().toString());


            DatabaseReference base = FirebaseDatabase.getInstance("https://incivisme-9417e-default-rtdb.europe-west1.firebasedatabase.app" +
                    "").getReference();

            DatabaseReference users = base.child("users");

            model.getUser().observe(getViewLifecycleOwner(), user -> {
                DatabaseReference uid = users.child(user.getUid());
                DatabaseReference incidencies = uid.child("incidencies");
                DatabaseReference reference = incidencies.push();
                reference.setValue(incidencia);
                Toast.makeText(getContext(), "Avís donat", Toast.LENGTH_SHORT).show();
            });



        });




    }

    @Override
    public void onStart() {
        super.onStart();

    }


}