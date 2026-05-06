package com.mckl.assignment1.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.mckl.assignment1.R; // res folder with weird naming convention (i think)
import com.mckl.assignment1.databinding.FragmentHomeBinding;

/**
 * fragment for the Home Screen
 * displays the "Start Quiz" button and the result of the last assessment if available
 */
public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    // creating the page
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // using Shared Preferences to save the user's last result and date of that result
        SharedPreferences prefs = requireContext().getSharedPreferences("quiz_prefs", Context.MODE_PRIVATE);
        String lastResult = prefs.getString("last_result", null);
        String lastDate = prefs.getString("last_date", null);

        // checks if there is previous saved data, if so, then uses the formatting from the strings.xml
        if (lastResult != null && lastDate != null) {
            binding.tvLastResult.setText(getString(R.string.last_result_format, lastResult, lastDate));
        } else {
            binding.tvLastResult.setText(getString(R.string.last_result_none)); // if no data, then "none"
        }

        // changes the fragment to the quiz fragment when pressing the button
        binding.btnStart.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_quizFragment)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
