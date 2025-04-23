package com.example.echoenglish_mobile.view.activity.chatbot;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.echoenglish_mobile.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConversationInfoDialog extends DialogFragment {

    public interface AudioSettingsListener {
        void onAudioSettingsChanged(String voiceId, float speed);
        String getCurrentVoiceId();
        float getCurrentSpeed();
    }
    public static final String TAG = "ConversationInfoDialog";
    private AudioSettingsListener audioSettingsListener;

    private TextView textViewContextDetail;
    private LinearLayout requirementsContainer;
    private TextView textViewInstructionsDetail;
    private LinearLayout playButtonContainer;
    private ImageView playButtonIcon;
    private TextView playButtonText;
    private Spinner voiceSpinner;
    private Spinner speedSpinner;

    private boolean isPlaying = false;
    private boolean isInternalChange = false;

    private Map<String, String> voiceMap = new HashMap<>();
    private Map<String, Float> speedMap = new HashMap<>();
    private List<String> voiceDisplayNames;
    private List<String> speedDisplayNames;

    public static ConversationInfoDialog newInstance() {
        return new ConversationInfoDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.item_task_conversation_info, null);

        initializeMaps();
        bindViews(dialogView);
        populateFakeData();
        setupAudioControls();

        builder.setTitle("Task Information")
                .setView(dialogView)
                .setPositiveButton("Close", (dialog, which) -> {
                    notifySettingsChanged();
                    stopInternalPlayback();
                    dialog.dismiss();
                });

        return builder.create();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        stopInternalPlayback();
        if (getActivity() instanceof ConversationActivity) {
            ((ConversationActivity) getActivity()).onDialogDismissed();
        }
    }

    private void initializeMaps() {
        voiceMap.put("Standard Female", "1");
        voiceMap.put("Standard Male", "2");
        voiceMap.put("Australian Female", "3");
        voiceMap.put("British Male", "4");
        voiceDisplayNames = new ArrayList<>(voiceMap.keySet());
        Collections.sort(voiceDisplayNames);

        speedMap.put("0.25x", 0.25f);
        speedMap.put("0.75x", 0.75f);
        speedMap.put("Normal (1.0x)", 1.0f);
        speedMap.put("1.25x", 1.25f);
        speedMap.put("1.5x", 1.5f);
        speedMap.put("2.0x", 2.0f);
        speedMap.put("4.0x", 4.0f);
        speedDisplayNames = new ArrayList<>(speedMap.keySet());

        speedDisplayNames.sort(Comparator.comparing(speedMap::get));
    }


    private void bindViews(View view) {
        textViewContextDetail = view.findViewById(R.id.textViewContextDetail);
        requirementsContainer = view.findViewById(R.id.requirementsContainer);
        textViewInstructionsDetail = view.findViewById(R.id.textViewInstructionsDetail);
        playButtonContainer = view.findViewById(R.id.playButtonContainer);
        playButtonIcon = view.findViewById(R.id.playButtonIcon);
        playButtonText = view.findViewById(R.id.playButtonText);
        voiceSpinner = view.findViewById(R.id.voiceSpinner);
        speedSpinner = view.findViewById(R.id.speedSpinner);
    }

    private void populateFakeData() {
        textViewContextDetail.setText("Engage in a casual conversation with a close friend about their hobbies and interests.");
        textViewInstructionsDetail.setText("This is the instruction text that will be played by TTS. You can practice listening here.");

        List<String> requirements = Arrays.asList(
                "Ask about the listener's hobbies.",
                "Inquire about their reasons for choosing that hobby.",
                "Share a related personal anecdote (optional)."
        );
        addRequirementCheckboxes(requirements);
    }

    private void addRequirementCheckboxes(List<String> requirements) {
        requirementsContainer.removeAllViews();

        for (String requirement : requirements) {
            CheckBox checkBox = new CheckBox(requireContext());
            checkBox.setText(requirement);
            checkBox.setTextSize(15f);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.topMargin = 4;
            params.bottomMargin = 4;
            checkBox.setLayoutParams(params);
            checkBox.setPadding(checkBox.getPaddingLeft() + 8, checkBox.getPaddingTop(), checkBox.getPaddingRight(), checkBox.getPaddingBottom());

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                Log.d(TAG, "Requirement '" + requirement + "' checked: " + isChecked);
            });
            requirementsContainer.addView(checkBox);
        }
    }

    private void setupAudioControls() {
        setupVoiceSpinner();
        setupSpeedSpinner();
        setupPlayButton();
    }

    private void setupPlayButton() {
        updatePlayButtonState();
        playButtonContainer.setOnClickListener(v -> {
            if (isPlaying) {
                Log.d(TAG, "Requesting TTS Stop...");
                stopInternalPlayback();
            } else {
                String textToSpeak = textViewInstructionsDetail.getText().toString();
                if (textToSpeak.trim().isEmpty()) {
                    Toast.makeText(getContext(), "No instruction text to play.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String selectedVoiceDisplay = (String) voiceSpinner.getSelectedItem();
                String selectedSpeedDisplay = (String) speedSpinner.getSelectedItem();

                String voiceId = voiceMap.getOrDefault(selectedVoiceDisplay, "1");
                float speedValue = speedMap.getOrDefault(selectedSpeedDisplay, 1.0f);

                Log.d(TAG, "Requesting Dialog TTS Play: Voice='" + selectedVoiceDisplay + "' (ID: " + voiceId + "), Speed='" + selectedSpeedDisplay + "' (Val: " + speedValue + ")");

                isPlaying = true;
                updatePlayButtonState();
            }
        });
    }

    private void updatePlayButtonState() {
        if (isPlaying) {
            playButtonIcon.setImageResource(android.R.drawable.ic_media_pause);
            playButtonText.setText("Stop Speech");
        } else {
            playButtonIcon.setImageResource(android.R.drawable.ic_media_play);
            playButtonText.setText("Play Instructions");
        }
    }
    private void stopInternalPlayback() {
        isPlaying = false;
        updatePlayButtonState();
    }


    private void setupVoiceSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, voiceDisplayNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        voiceSpinner.setAdapter(adapter);

        String currentVoiceId = "1";
        if (audioSettingsListener != null && audioSettingsListener.getCurrentVoiceId() != null) {
            currentVoiceId = audioSettingsListener.getCurrentVoiceId();
        }
        String currentVoiceName = "";
        for (Map.Entry<String, String> entry : voiceMap.entrySet()) {
            if (entry.getValue().equals(currentVoiceId)) {
                currentVoiceName = entry.getKey();
                break;
            }
        }
        int voicePosition = voiceDisplayNames.indexOf(currentVoiceName);
        if (voicePosition >= 0) {
            isInternalChange = true;
            voiceSpinner.setSelection(voicePosition, false);
        }

        voiceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInternalChange) {
                    isInternalChange = false;
                    return;
                }
                String selectedVoice = (String) parent.getItemAtPosition(position);
                Log.d(TAG, "Voice selected: " + selectedVoice);
                notifySettingsChanged();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSpeedSpinner() {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, speedDisplayNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        speedSpinner.setAdapter(adapter);

        float currentSpeed = 1;
        if(audioSettingsListener != null) {
            currentSpeed = audioSettingsListener.getCurrentSpeed();
        }
        String currentSpeedName = "";
        float minDiff = Float.MAX_VALUE;

        for(Map.Entry<String, Float> entry : speedMap.entrySet()) {
            float diff = Math.abs(entry.getValue() - currentSpeed);
            if (diff < minDiff) {
                minDiff = diff;
                currentSpeedName = entry.getKey();
            }
        }


        int speedPosition = speedDisplayNames.indexOf(currentSpeedName);
        if (speedPosition >= 0) {
            isInternalChange = true;
            speedSpinner.setSelection(speedPosition, false);
        }


        speedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInternalChange) {
                    isInternalChange = false;
                    return;
                }
                String selectedSpeed = (String) parent.getItemAtPosition(position);
                Log.d(TAG, "Speed selected: " + selectedSpeed);
                notifySettingsChanged();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void notifySettingsChanged() {
        if (audioSettingsListener != null && voiceSpinner != null && speedSpinner != null) {
            String selectedVoiceDisplay = (String) voiceSpinner.getSelectedItem();
            String selectedSpeedDisplay = (String) speedSpinner.getSelectedItem();

            String voiceId = voiceMap.getOrDefault(selectedVoiceDisplay, "1");
            float speedValue = speedMap.getOrDefault(selectedSpeedDisplay, 1.0f);

            audioSettingsListener.onAudioSettingsChanged(voiceId, speedValue);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        audioSettingsListener = null;
    }
}