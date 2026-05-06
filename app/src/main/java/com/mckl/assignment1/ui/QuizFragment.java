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
import com.mckl.assignment1.databinding.FragmentQuizBinding;
import com.mckl.assignment1.viewmodel.QuizViewModel;

/**
 * Fragment for the Quiz Screen.
 * Displays the current question, options, and progress.
 * Observes the QuizViewModel for state updates.
 */
public class QuizFragment extends Fragment {
    private FragmentQuizBinding binding;
    private QuizViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentQuizBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(QuizViewModel.class);

        viewModel.getCurrentQuestion().observe(getViewLifecycleOwner(), question -> {
            if (question != null) {
                binding.tvQuestionText.setText(question.getText());
                binding.btnOption0.setText(question.getOptions().get(0));
                binding.btnOption1.setText(question.getOptions().get(1));
                binding.btnOption2.setText(question.getOptions().get(2));
                binding.btnOption3.setText(question.getOptions().get(3));
            }
        });

        viewModel.getCurrentQuestionIndex().observe(getViewLifecycleOwner(), index -> {
            binding.progressIndicator.setProgress(index + 1);
            binding.tvQuestionNumber.setText(getString(R.string.question_format, index + 1, viewModel.getTotalQuestions()));
        });

        viewModel.getResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                Bundle args = new Bundle();
                args.putString("archetype", result.name());
                Navigation.findNavController(requireView()).navigate(R.id.action_quizFragment_to_resultFragment, args);
            }
        });

        binding.btnOption0.setOnClickListener(v -> viewModel.answerQuestion(0));
        binding.btnOption1.setOnClickListener(v -> viewModel.answerQuestion(1));
        binding.btnOption2.setOnClickListener(v -> viewModel.answerQuestion(2));
        binding.btnOption3.setOnClickListener(v -> viewModel.answerQuestion(3));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
