package org.ecaib.incivisme.ui.llistat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.ecaib.incivisme.Incidencia;
import org.ecaib.incivisme.R;
import org.ecaib.incivisme.databinding.FragmentLlistatBinding;

public class LlistatFragment extends Fragment {

    private LlistatViewModel llistatViewModel;
    private FragmentLlistatBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        llistatViewModel =
                new ViewModelProvider(this).get(LlistatViewModel.class);

        binding = FragmentLlistatBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference base = FirebaseDatabase.getInstance("https://incivisme-9417e-default-rtdb.europe-west1.firebasedatabase.app").getReference();

        DatabaseReference users = base.child("users");
        DatabaseReference uid = users.child(auth.getUid());
        DatabaseReference incidencies = uid.child("incidencies");


        FirebaseListOptions<Incidencia> options = new FirebaseListOptions.Builder<Incidencia>()
                .setQuery(incidencies, Incidencia.class)
                .setLayout(R.layout.incidencia)
                .setLifecycleOwner(this)
                .build();


        FirebaseListAdapter<Incidencia> adapter = new FirebaseListAdapter<Incidencia>(options) {
            @Override
            protected void populateView(View v, Incidencia model, int position) {
                TextView txtDescripcio = v.findViewById(R.id.txtDescripcio);
                TextView txtAdreca = v.findViewById(R.id.txtAdreca);

                txtDescripcio.setText(model.getProblema());
                txtAdreca.setText(model.getDireccio());
            }
        };

        ListView lvIncidencies = view.findViewById(R.id.lvIncidencies);
        lvIncidencies.setAdapter(adapter);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}