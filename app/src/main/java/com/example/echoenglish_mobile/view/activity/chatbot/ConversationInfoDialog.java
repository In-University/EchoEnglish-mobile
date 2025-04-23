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
import com.example.echoenglish_mobile.model.response.ChecklistItemResponse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConversationInfoDialog extends DialogFragment {

    // --- Interface and Constants ---
    public interface AudioSettingsListener {
        void onAudioSettingsChanged(String voiceId, float speed);
        String getCurrentVoiceId();
        float getCurrentSpeed();
    }
    public static final String TAG = "ConversationInfoDialog";
    private static final String ARG_CONTEXT = "arg_context";
    private static final String ARG_CHECKLIST = "arg_checklist";

    // --- Views ---
    private TextView textViewContextDetail;
    private LinearLayout requirementsContainer;
    private TextView textViewInstructionsDetail;
    private LinearLayout playButtonContainer;
    private ImageView playButtonIcon;
    private TextView playButtonText;
    private Spinner voiceSpinner;
    private Spinner speedSpinner;

    // --- State ---
    private AudioSettingsListener audioSettingsListener;
    private boolean isPlaying = false;
    private boolean isInternalChange = false; // For spinners

    // --- Data ---
    private String conversationContext;
    private List<ChecklistItemResponse> checklistItems;
    private Map<String, String> voiceMap = new HashMap<>();
    private Map<String, Float> speedMap = new HashMap<>();
    private List<String> voiceDisplayNames;
    private List<String> speedDisplayNames;

    public static ConversationInfoDialog newInstance(String context, ArrayList<ChecklistItemResponse> checklist) {
        ConversationInfoDialog fragment = new ConversationInfoDialog();
        Bundle args = new Bundle();
        args.putString(ARG_CONTEXT, context);
        args.putSerializable(ARG_CHECKLIST, checklist);
        fragment.setArguments(args);
        return fragment;
    }

    // --- Lifecycle and Dialog Creation ---
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Ensure the hosting activity implements the listener
        if (context instanceof AudioSettingsListener) {
            audioSettingsListener = (AudioSettingsListener) context;
        } else {
            Log.w(TAG, context.toString() + " must implement AudioSettingsListener");
            // Fallback: get listener from parent fragment if nested? Or throw exception?
            // For simplicity, we assume direct hosting Activity implements it.
            if (getParentFragment() instanceof AudioSettingsListener) {
                audioSettingsListener = (AudioSettingsListener) getParentFragment();
            } else if (getActivity() instanceof AudioSettingsListener) {
                audioSettingsListener = (AudioSettingsListener) getActivity();
            } else {
                Log.e(TAG, "Hosting Activity/Fragment must implement AudioSettingsListener");
                // Optionally throw new ClassCastException(...)
            }
        }
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // --- Retrieve Arguments ---
        if (getArguments() != null) {
            conversationContext = getArguments().getString(ARG_CONTEXT, "No Context Provided");
            // Correctly retrieve the Serializable ArrayList
            Serializable serializableChecklist = getArguments().getSerializable(ARG_CHECKLIST);
            if (serializableChecklist instanceof ArrayList) {
                // Type cast is safe here after the check
                // Use SuppressWarnings if needed, but the check makes it safe
                @SuppressWarnings("unchecked")
                ArrayList<ChecklistItemResponse> retrievedList = (ArrayList<ChecklistItemResponse>) serializableChecklist;
                checklistItems = new ArrayList<>(retrievedList); // Create a mutable copy if needed
            } else {
                checklistItems = new ArrayList<>(); // Fallback to empty list
                Log.w(TAG, "Could not deserialize checklist or checklist was null.");
            }

        } else {
            conversationContext = "Arguments missing";
            checklistItems = new ArrayList<>();
            Log.e(TAG, "Dialog arguments are null!");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.item_task_conversation_info, null);

        initializeMaps(); // Initialize voice/speed maps
        bindViews(dialogView);
        populateData(); // Populate with actual data
        setupAudioControls();

        builder.setTitle("Task Information")
                .setView(dialogView)
                .setPositiveButton("Close", (dialog, which) -> {
                    // Settings are usually notified onChange, but ensure final state is sent.
                    notifySettingsChanged();
                    stopInternalPlayback();
                    // No need to explicitly call dismiss, happens automatically
                });

        return builder.create();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        stopInternalPlayback();
        // Notify the Activity that the dialog is dismissed
        if (getActivity() instanceof ConversationActivity) {
            ((ConversationActivity) getActivity()).onDialogDismissed();
        }
    }

    // --- Initialization and Setup ---
    private void initializeMaps() {
        // Your existing voice/speed map initialization...
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

    // --- Data Population ---
    private void populateData() {
        textViewContextDetail.setText(conversationContext);
        // Use context as instructions for now, or pass separately if available
        textViewInstructionsDetail.setText("Instructions based on context: " + conversationContext);

        // Populate checklist from the passed data
        populateChecklist(checklistItems);
    }

    private void populateChecklist(List<ChecklistItemResponse> items) {
        requirementsContainer.removeAllViews();
        if (items == null || items.isEmpty()) {
            TextView emptyView = new TextView(requireContext());
            emptyView.setText("No checklist items available.");
            requirementsContainer.addView(emptyView);
            return;
        }


        for (ChecklistItemResponse item : items) {
            CheckBox checkBox = new CheckBox(requireContext());
            checkBox.setText(item.getDescription());
            checkBox.setChecked(item.isCompleted());
            checkBox.setEnabled(false); // Make checkboxes read-only displays
            checkBox.setTextSize(15f);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.topMargin = 4;
            params.bottomMargin = 4;
            checkBox.setLayoutParams(params);
            checkBox.setPadding(checkBox.getPaddingLeft() + 8, checkBox.getPaddingTop(), checkBox.getPaddingRight(), checkBox.getPaddingBottom());

            requirementsContainer.addView(checkBox);
        }
    }
    // --- Audio Controls Setup ---
    private void setupAudioControls() {
        setupVoiceSpinner();
        setupSpeedSpinner();
        setupPlayButton();
    }

    private void setupPlayButton() {
        updatePlayButtonState(); // Set initial state
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

                // Get selected settings
                String selectedVoiceDisplay = (String) voiceSpinner.getSelectedItem();
                String selectedSpeedDisplay = (String) speedSpinner.getSelectedItem();
                String voiceId = voiceMap.getOrDefault(selectedVoiceDisplay, "1");
                float speedValue = speedMap.getOrDefault(selectedSpeedDisplay, 1.0f);

                Log.d(TAG, "Requesting Dialog TTS Play: Voice='" + selectedVoiceDisplay + "' (ID: " + voiceId + "), Speed='" + selectedSpeedDisplay + "' (Val: " + speedValue + ")");
                // *** Implement Actual TTS Playback Call Here ***
                // For example, using Android's TextToSpeech engine
                // textToSpeech.setVoice(...)
                // textToSpeech.setSpeechRate(speedValue);
                // textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "dialogTTS");

                isPlaying = true; // Assume playback starts successfully
                updatePlayButtonState();
            }
        });
    }

    // --- Spinner Setup (Minor adjustments for listener safety) ---
    private void setupVoiceSpinner() {
        if (getContext() == null) return;
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

        int voicePosition = adapter.getPosition(currentVoiceName);
        if (voicePosition >= 0) {
            isInternalChange = true;
            voiceSpinner.setSelection(voicePosition, false);
            Log.d(TAG, "Setting initial voice selection to: " + currentVoiceName + " at pos " + voicePosition);
        } else {
            Log.w(TAG, "Initial voice ID '" + currentVoiceId + "' not found in map for display name.");
            if (!voiceDisplayNames.isEmpty()) {
                isInternalChange = true;
                voiceSpinner.setSelection(0, false);
                Log.w(TAG, "Falling back to first voice: " + voiceDisplayNames.get(0));
            }
        }

        voiceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Voice Spinner - onItemSelected - isInternalChange: " + isInternalChange);
                if (isInternalChange) {
                    isInternalChange = false;
                    return;
                }
                String selectedVoice = (String) parent.getItemAtPosition(position);
                Log.d(TAG, "Voice selected by user: " + selectedVoice);
                notifySettingsChanged();
                stopInternalPlayback();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }


    private void setupSpeedSpinner() {
        if (getContext() == null) return;

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, speedDisplayNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        speedSpinner.setAdapter(adapter);

        float currentSpeed = 1.0f;
        if (audioSettingsListener != null) {
            currentSpeed = audioSettingsListener.getCurrentSpeed();
        }

        String currentSpeedName = "";
        float minDiff = Float.MAX_VALUE;
        for (Map.Entry<String, Float> entry : speedMap.entrySet()) {
            float diff = Math.abs(entry.getValue() - currentSpeed);
            if (diff < minDiff) {
                minDiff = diff;
                currentSpeedName = entry.getKey();
            }
        }

        int speedPosition = adapter.getPosition(currentSpeedName);
        if (speedPosition >= 0) {
            isInternalChange = true; // Prevent listener trigger
            speedSpinner.setSelection(speedPosition, false); // Set initial selection
            Log.d(TAG, "Setting initial speed selection to: " + currentSpeedName + " at pos " + speedPosition);
        } else {
            Log.w(TAG, "Could not find display name for speed value: " + currentSpeed + ". Found closest: " + currentSpeedName);
            // Fallback logic if needed, e.g., select "Normal (1.0x)"
            int normalPos = adapter.getPosition("Normal (1.0x)");
            if (normalPos >= 0) {
                isInternalChange = true;
                speedSpinner.setSelection(normalPos, false);
                Log.w(TAG, "Falling back to 'Normal (1.0x)' speed.");
            } else if (!speedDisplayNames.isEmpty()) {
                isInternalChange = true;
                speedSpinner.setSelection(0, false); // Fallback to first item
                Log.w(TAG, "Falling back to first speed: " + speedDisplayNames.get(0));
            }
        }


        speedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Speed Spinner - onItemSelected - isInternalChange: " + isInternalChange);
                if (isInternalChange) {
                    isInternalChange = false;
                    return;
                }
                String selectedSpeed = (String) parent.getItemAtPosition(position);
                Log.d(TAG, "Speed selected by user: " + selectedSpeed);
                notifySettingsChanged();
                stopInternalPlayback();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // --- Helper Methods ---
    private void notifySettingsChanged() {
        if (audioSettingsListener != null && voiceSpinner != null && speedSpinner != null &&
                voiceSpinner.getSelectedItem() != null && speedSpinner.getSelectedItem() != null) {

            String selectedVoiceDisplay = (String) voiceSpinner.getSelectedItem();
            String selectedSpeedDisplay = (String) speedSpinner.getSelectedItem();

            // Get values from maps, providing defaults if somehow null/missing
            String voiceId = voiceMap.getOrDefault(selectedVoiceDisplay, "1");
            float speedValue = speedMap.getOrDefault(selectedSpeedDisplay, 1.0f);

            Log.d(TAG, "Notifying listener: VoiceID=" + voiceId + ", Speed=" + speedValue);
            audioSettingsListener.onAudioSettingsChanged(voiceId, speedValue);
        } else {
            Log.w(TAG, "Cannot notify settings changed - listener or spinners invalid.");
        }
    }

    private void updatePlayButtonState() {
        // Your existing implementation...
        if (isPlaying) {
            playButtonIcon.setImageResource(android.R.drawable.ic_media_pause);
            playButtonText.setText("Stop Speech");
        } else {
            playButtonIcon.setImageResource(android.R.drawable.ic_media_play);
            playButtonText.setText("Play Instructions");
        }
    }

    private void stopInternalPlayback() {
        // *** Implement Actual TTS Stop Logic Here ***
        // For example: if (textToSpeech != null) textToSpeech.stop();
        Log.d(TAG, "Stopping internal TTS playback (if any was playing).");
        isPlaying = false;
        updatePlayButtonState(); // Update button UI
    }


    @Override
    public void onDetach() {
        super.onDetach();
        stopInternalPlayback();
        audioSettingsListener = null;
    }
}