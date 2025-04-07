package com.example.echoenglish_mobile.ui.activity; // Use your actual package name

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.echoenglish_mobile.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

public class DictionaryBottomSheetDialog extends BottomSheetDialogFragment {

    public static final String TAG = "DictionaryBottomSheet";
    private static final String ARG_SELECTED_WORD = "selected_word";

    // Updated View variables to match new IDs and types
    private TextView tvSelectedWord;
    private TextView tvPronunciation;
    private TextView tvWordType;
    private TextView tvDefinition;
    private MaterialButton btnCloseSheet;
    private MaterialButton btnSaveWord;

    private String selectedWord;
    private boolean isSaved = false;

    public static DictionaryBottomSheetDialog newInstance(String selectedWord) {
        DictionaryBottomSheetDialog fragment = new DictionaryBottomSheetDialog();
        Bundle args = new Bundle();
        args.putString(ARG_SELECTED_WORD, selectedWord);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedWord = getArguments().getString(ARG_SELECTED_WORD);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the NEW layout
        View view = inflater.inflate(R.layout.layout_bottom_dictionary, container, false);

        // Find views using the NEW IDs
        tvSelectedWord = view.findViewById(R.id.tvWord);
        tvPronunciation = view.findViewById(R.id.tv_pronunciation);
//        tvWordType = view.findViewById(R.id.tv_word_type);
//        tvDefinition = view.findViewById(R.id.tv_definition);
//        btnCloseSheet = view.findViewById(R.id.btn_close_sheet);
//        btnSaveWord = view.findViewById(R.id.btn_save_word);

        // --- Set Initial Text ---
        tvSelectedWord.setText(selectedWord);

        // --- Placeholder Data (Replace with actual dictionary lookup) ---
        tvPronunciation.setText("/" + selectedWord.toLowerCase() + "/"); // Simple placeholder
//        tvWordType.setText("..."); // Placeholder - e.g., "Noun", "Verb"
//        tvDefinition.setText("Đang tải định nghĩa cho '" + selectedWord + "'...");
        // --- End Placeholder Data ---

        // TODO: Perform actual dictionary lookup here and update tvPronunciation, tvWordType, tvDefinition

        // --- Check initial saved state ---
        // In a real app, query SharedPreferences or Database here
//        isSaved = checkWordSavedState(selectedWord); // Example method call
//        updateSaveButtonState(); // Set initial text/icon for Save button
//
//        // --- Setup Listeners ---
//        btnCloseSheet.setOnClickListener(v -> dismiss()); // Simply close the bottom sheet
//
//        btnSaveWord.setOnClickListener(v -> {
//            isSaved = !isSaved; // Toggle state
//            updateSaveButtonState(); // Update button appearance
//            saveWordState(selectedWord, isSaved); // Save the new state
//
//            String message = isSaved ? "Đã lưu '" : "Đã bỏ lưu '";
//            Toast.makeText(getContext(), message + selectedWord + "'", Toast.LENGTH_SHORT).show();
//        });

        // Direct styling example (use with caution, prefer themes/styles)
        // tvSelectedWord.setTextColor(Color.parseColor("#1a237e")); // Example: Dark blue color

        return view;
    }

    // Updated method to handle MaterialButton (text + optional icon)
    private void updateSaveButtonState() {
        if (isSaved) {
            btnSaveWord.setText("Unsave"); // Or "Đã lưu"
            // Set filled icon
            btnSaveWord.setIconResource(R.drawable.icon_timer); // Use your filled bookmark icon
            // Optional: Change button style slightly if needed (e.g., background tint)
            // btnSaveWord.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.your_saved_color))); // Requires color resource
            btnSaveWord.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DDDDDD"))); // Example direct grey tint for saved state
        } else {
            btnSaveWord.setText("Save"); // Or "Lưu"
            // Set outline icon
            btnSaveWord.setIconResource(R.drawable.btn2); // Use your outline bookmark icon
            // Optional: Reset tint to default (or primary color)
            btnSaveWord.setIconTint(null); // Use default icon tint
            btnSaveWord.setBackgroundTintList(null); // Use default background tint from theme/style
        }
        // Ensure the icon is visible (MaterialButton defaults might hide it if text is present)
        btnSaveWord.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START); // Or START
    }

    // --- Placeholder methods for saving/checking state ---
    // Replace these with your actual SharedPreferences or Database logic
    private boolean checkWordSavedState(String word) {
        // Example using SharedPreferences (very basic)
        // SharedPreferences prefs = requireActivity().getSharedPreferences("SavedWords", Context.MODE_PRIVATE);
        // return prefs.getBoolean(word, false);
        return false; // Default: word is not saved
    }

    private void saveWordState(String word, boolean saved) {
        // Example using SharedPreferences (very basic)
        // SharedPreferences prefs = requireActivity().getSharedPreferences("SavedWords", Context.MODE_PRIVATE);
        // SharedPreferences.Editor editor = prefs.edit();
        // if (saved) {
        //     editor.putBoolean(word, true);
        // } else {
        //     editor.remove(word);
        // }
        // editor.apply();
        System.out.println("Simulating save state for '" + word + "': " + saved); // Log simulation
    }
    // --- End Placeholder methods ---


    // Removed searchWordOnline method as the button is no longer present in the new layout.
    // If you need search functionality, you'll have to add a button or another trigger for it.

}