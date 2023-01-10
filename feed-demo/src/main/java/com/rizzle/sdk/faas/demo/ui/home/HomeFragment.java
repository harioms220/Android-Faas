package com.rizzle.sdk.faas.demo.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import com.rizzle.sdk.faas.demo.databinding.FragmentHomeBinding;
import com.rizzle.sdk.faas.demo.ui.adapters.ItemAdapter;
import com.rizzle.sdk.faas.demo.utils.DataGenerator;

import timber.log.Timber;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private String TAG = HomeFragment.class.getSimpleName();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.tag(TAG).d("fragment lifecycle: creating");
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        Timber.tag(TAG).d("fragment lifecycle: creating view");

        binding = FragmentHomeBinding.inflate(inflater, container, false);

        setupRecyclerView();
        return binding.getRoot();
    }

    private void setupRecyclerView() {
        ItemAdapter adapter = new ItemAdapter(DataGenerator.getItemsData());
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.tag(TAG).d("fragment lifecycle: view created");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Timber.tag(TAG).d("fragment lifecycle: view destroyed");
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