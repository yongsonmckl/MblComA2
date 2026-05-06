package com.mckl.assignment1.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.mckl.assignment1.R;
import com.mckl.assignment1.databinding.FragmentResultBinding;
import com.mckl.assignment1.model.CareerArchetype;

/**
 * Fragment for the Result Screen.
 * Displays the user's identified career archetype, description, and job examples.
 * Implements dynamic UI by changing the background color based on the result.
 */
public class ResultFragment extends Fragment {
    private FragmentResultBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentResultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        String archetypeStr = getArguments() != null ? getArguments().getString("archetype") : null;
        if (archetypeStr != null) {
            CareerArchetype archetype = CareerArchetype.valueOf(archetypeStr);
            displayResult(archetype);
        }

        binding.btnRestart.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_resultFragment_to_homeFragment)
        );
    }

    private void displayResult(CareerArchetype archetype) {
        binding.tvResultTitle.setText(archetype.getTitle());
        binding.tvResultField.setText(archetype.getField());
        binding.tvResultDescription.setText(archetype.getDescription());
        binding.tvResultExamples.setText(archetype.getExamples());
        
        // Dynamic theming: Change background color based on the identified career path.
        int colorRes;
        
        switch (archetype) {
            case ARCHITECT:
                colorRes = R.color.bg_architect_light;
                break;
            case VISIONARY:
                colorRes = R.color.bg_visionary_light;
                break;
            case GUARDIAN:
                colorRes = R.color.bg_guardian_light;
                break;
            case CAPTAIN:
                colorRes = R.color.bg_captain_light;
                break;
            default:
                colorRes = R.color.background;
        }
        
        binding.resultContainer.setBackgroundResource(colorRes);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
