package com.rizzle.sdk.faas.demo.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.rizzle.sdk.faas.demo.databinding.FragmentDashboardBinding;

import timber.log.Timber;


public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private String TAG = DashboardFragment.class.getSimpleName();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.tag(TAG).d("fragment lifecycle: creating");
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);
        Timber.tag(TAG).d("fragment lifecycle: creating view");
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        final TextView textView = binding.textDashboard;
//        dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.tag(TAG).d("fragment lifecycle: view created");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Timber.tag(TAG).d("fragment lifecycle: view destroyed");
        binding = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Timber.tag(TAG).d("fragment lifecycle: detached");
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.tag(TAG).d("fragment lifecycle: paused");
    }

    @Override
    public void onStop() {
        super.onStop();
        Timber.tag(TAG).d("fragment lifecycle: stopped");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.tag(TAG).d("fragment lifecycle: destroyed");
    }
}