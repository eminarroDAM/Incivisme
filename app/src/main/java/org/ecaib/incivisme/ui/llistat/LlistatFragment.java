package org.ecaib.incivisme.ui.llistat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import org.ecaib.incivisme.databinding.FragmentLlistatBinding;

public class LlistatFragment extends Fragment {

    private LlistatViewModel llistatViewModel;
    private FragmentLlistatBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        llistatViewModel =
                new ViewModelProvider(this).get(LlistatViewModel.class);

        binding = FragmentLlistatBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textviewLocation;
        llistatViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}