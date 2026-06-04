package com.mckl.assignment1.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.mckl.assignment1.R;
import com.mckl.assignment1.databinding.FragmentHomeBinding;
import com.mckl.assignment1.model.QuizAttempt;
import com.mckl.assignment1.viewmodel.HomeViewModel;

/**
 * Fragment for the home screen.
 */
public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        viewModel.getLatestAttempt().observe(getViewLifecycleOwner(), this::renderLatestAttempt);

        binding.btnStart.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_quizFragment)
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refreshLatestAttempt();
        }
    }

    private void renderLatestAttempt(QuizAttempt attempt) {
        if (attempt != null && attempt.getArchetypeTitle() != null && attempt.getCompletedAt() != null) {
            binding.tvLastResult.setText(
                    getString(R.string.last_result_format, attempt.getArchetypeTitle(), attempt.getCompletedAt())
            );
        } else {
            binding.tvLastResult.setText(getString(R.string.last_result_none));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
